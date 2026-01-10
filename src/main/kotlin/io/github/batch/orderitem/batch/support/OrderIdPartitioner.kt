package io.github.batch.orderitem.batch.support

import org.springframework.batch.core.partition.support.Partitioner
import org.springframework.batch.item.ExecutionContext
import org.springframework.jdbc.core.JdbcTemplate

class OrderIdPartitioner(
    private val jdbcTemplate: JdbcTemplate,
) : Partitioner {

    override fun partition(gridSize: Int): Map<String, ExecutionContext> {
        val range = jdbcTemplate.queryForMap(
            """
            select min(order_id) as min_id, max(order_id) as max_id
            from order_item
            """.trimIndent()
        )

        val minId = (range["min_id"] as? Number)?.toLong()
        val maxId = (range["max_id"] as? Number)?.toLong()

        if (minId == null || maxId == null) {
            return emptyMap()
        }

        val targetSize = ((maxId - minId) / gridSize) + 1
        var start = minId
        var end = start + targetSize - 1
        var number = 0

        val partitions = LinkedHashMap<String, ExecutionContext>()
        while (start <= maxId) {
            val context = ExecutionContext()
            context.putLong("minId", start)
            context.putLong("maxId", minOf(end, maxId))
            partitions["partition$number"] = context

            start += targetSize
            end += targetSize
            number += 1
        }

        return partitions
    }


}
