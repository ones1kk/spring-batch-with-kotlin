package io.github.batch.orderitem.batch.support

import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.jdbc.core.JdbcTemplate

open class OrderItemSetupTasklet(
    private val jdbcTemplate: JdbcTemplate,
    private val rowCount: Int,
) : Tasklet {


    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        jdbcTemplate.execute(
            """
            create table if not exists order_item (
                order_item_id bigint auto_increment primary key comment '주문 항목 pk',
                order_id bigint not null comment '주문 id',
                product_name varchar(100) not null comment '상품명',
                created_at datetime default current_timestamp comment '생성일'
            ) engine=innodb
            """.trimIndent()
        )

        jdbcTemplate.execute(
            """
            create index if not exists idx_order_item_order_id
                on order_item (order_id)
            """.trimIndent()
        )

        if (rowCount > 0) {
            val limit = rowCount.coerceAtMost(1_000_000)
            jdbcTemplate.execute(
                """
                insert into order_item (order_id, product_name)
                select
                    floor(n / 5),
                    concat('product_', n)
                from (
                    select @row := @row + 1 as n
                    from information_schema.columns,
                         (select @row := 0) r
                    limit $limit
                ) t
                """.trimIndent()
            )
        }

        return RepeatStatus.FINISHED
    }
}
