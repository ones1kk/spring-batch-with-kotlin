package io.github.batch.orderitem.batch.support

import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ItemWriter
import org.springframework.jdbc.core.JdbcTemplate

class OrderItemDeleteWriter(
    private val jdbcTemplate: JdbcTemplate,
) : ItemWriter<Long> {

    override fun write(items: Chunk<out Long>) {
        if (items.isEmpty()) return

        val inClause = items.items.joinToString(",")
        jdbcTemplate.execute(
            """
            delete from order_item
            where order_id in ($inClause)
            """.trimIndent()
        )
    }
}
