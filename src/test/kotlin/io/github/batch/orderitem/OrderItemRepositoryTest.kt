package io.github.batch.orderitem

import io.github.batch.orderitem.repository.OrderItemRepository
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.jdbc.Sql
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals

@SpringBootTest
@Transactional
@Sql(
    scripts = [
        "/order_item_schema.sql",
        "/order_item_data.sql",
    ]
)
class OrderItemRepositoryTest(
    private val orderItemRepository: OrderItemRepository,
) {

    @Test
    fun `findByOrderId returns items`() {
        val items = orderItemRepository.findByOrderId(1L)
        assertEquals(2, items.size)
    }
}
