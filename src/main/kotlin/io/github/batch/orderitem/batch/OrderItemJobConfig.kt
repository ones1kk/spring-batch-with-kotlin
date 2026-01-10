package io.github.batch.orderitem.batch

import io.github.batch.orderitem.batch.support.*
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.partition.support.Partitioner
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.database.JdbcCursorItemReader
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.transaction.PlatformTransactionManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import javax.sql.DataSource

@Configuration
class OrderItemJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val jdbcTemplate: JdbcTemplate,
    private val dataSource: DataSource,
) {

    @Bean
    fun orderItemTaskExecutor(): TaskExecutor =
        ThreadPoolTaskExecutor().apply {
            corePoolSize = 10
            maxPoolSize = 10
            queueCapacity = 0
            initialize()
        }

    @Bean
    fun orderItemLockWaitJob(
        orderItemSetupStep: Step,
        orderItemHoldLockStep: Step,
        orderItemDeleteStep: Step,
        orderItemCleanupStep: Step,
    ): Job = JobBuilder("orderItemLockWaitJob", jobRepository)
        .start(orderItemSetupStep)
        .next(orderItemHoldLockStep)
        .next(orderItemDeleteStep)
        .next(orderItemCleanupStep)
        .build()

    @Bean
    fun orderItemSetupStep(orderItemSetupTasklet: OrderItemSetupTasklet): Step =
        StepBuilder("orderItemSetupStep", jobRepository)
            .tasklet(orderItemSetupTasklet, transactionManager)
            .transactionManager(transactionManager)
            .build()

    @Bean
    fun orderItemDeleteStep(
        orderIdPartitioner: Partitioner,
        orderItemDeleteSlaveStep: Step,
        orderItemTaskExecutor: TaskExecutor,
    ): Step = StepBuilder("orderItemDeleteStep", jobRepository)
        .partitioner("orderItemDeleteSlaveStep", orderIdPartitioner)
        .step(orderItemDeleteSlaveStep)
        .taskExecutor(orderItemTaskExecutor)
        .gridSize(15)
        .build()

    @Bean
    fun orderItemDeleteSlaveStep(
        orderIdReader: JdbcCursorItemReader<Long>,
        orderItemDeleteWriter: OrderItemDeleteWriter,
    ): Step = StepBuilder("orderItemDeleteSlaveStep", jobRepository)
        .chunk<Long, Long>(1000, transactionManager)
        .reader(orderIdReader)
        .writer(orderItemDeleteWriter)
        .transactionManager(transactionManager)
        .build()

    @Bean
    fun orderItemCleanupStep(orderItemCleanupTasklet: OrderItemCleanupTasklet): Step =
        StepBuilder("orderItemCleanupStep", jobRepository)
            .tasklet(orderItemCleanupTasklet, transactionManager)
            .transactionManager(transactionManager)
            .build()

    @Bean
    fun orderItemHoldLockStep(orderItemHoldLockTasklet: OrderItemHoldLockTasklet): Step =
        StepBuilder("orderItemHoldLockStep", jobRepository)
            .tasklet(orderItemHoldLockTasklet, transactionManager)
            .transactionManager(transactionManager)
            .build()

    @Bean
    fun orderIdPartitioner(): Partitioner = OrderIdPartitioner(jdbcTemplate)

    @Bean
    fun orderItemDeleteWriter() = OrderItemDeleteWriter(jdbcTemplate)

    @Bean
    @StepScope
    fun orderIdReader(
        @Value("#{stepExecutionContext['minId']}") minId: Long?,
        @Value("#{stepExecutionContext['maxId']}") maxId: Long?,
    ): JdbcCursorItemReader<Long> {
        val safeMin = minId ?: 0
        val safeMax = maxId ?: 0
        return JdbcCursorItemReaderBuilder<Long>()
            .name("orderIdReader")
            .dataSource(dataSource)
            .sql(
                """
                select distinct order_id
                from order_item
                where order_id between ? and ?
                order by order_id
                """.trimIndent()
            )
            .preparedStatementSetter { ps: PreparedStatement ->
                ps.setLong(1, safeMin)
                ps.setLong(2, safeMax)
            }
            .rowMapper { rs: ResultSet, _ -> rs.getLong(1) }
            .build()
    }

    @Bean
    @StepScope
    fun orderItemSetupTasklet(
        @Value("#{jobParameters['rowCount'] ?: 200000}") rowCount: Int,
    ) = OrderItemSetupTasklet(jdbcTemplate, rowCount)

    @Bean
    @StepScope
    fun orderItemHoldLockTasklet(
        @Value("#{jobParameters['lockMinOrderId'] ?: 1}") minOrderId: Long,
        @Value("#{jobParameters['lockMaxOrderId'] ?: 100}") maxOrderId: Long,
        @Value("#{jobParameters['holdMillis'] ?: 15000}") holdMillis: Long,
    ) = OrderItemHoldLockTasklet(jdbcTemplate, minOrderId, maxOrderId, holdMillis)

    @Bean
    fun orderItemCleanupTasklet() = OrderItemCleanupTasklet(jdbcTemplate)
}
