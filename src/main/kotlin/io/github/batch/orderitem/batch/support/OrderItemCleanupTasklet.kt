package io.github.batch.orderitem.batch.support

import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.StepContribution
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.jdbc.core.JdbcTemplate

class OrderItemCleanupTasklet(
    private val jdbcTemplate: JdbcTemplate,
) : Tasklet {

    override fun execute(
        contribution: StepContribution,
        chunkContext: ChunkContext
    ): RepeatStatus? {
        jdbcTemplate.execute("drop table if exists order_item")
        return RepeatStatus.FINISHED
    }
}
