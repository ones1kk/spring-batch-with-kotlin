package io.github.batch.orderitem.batch.support

import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.jdbc.core.JdbcTemplate

open class OrderItemHoldLockTasklet(
    private val jdbcTemplate: JdbcTemplate,
    private val minOrderId: Long,
    private val maxOrderId: Long,
    private val holdMillis: Long,
) : Tasklet {

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        jdbcTemplate.update(
            """
            update order_item
            set product_name = product_name
            where order_id between ? and ?
            """.trimIndent(),
            minOrderId,
            maxOrderId,
        )

        if (holdMillis > 0) {
            Thread.sleep(holdMillis)
        }

        return RepeatStatus.FINISHED
    }
}
