package org.pv293.kotlinseminar.productSelectionService.repository

import org.pv293.kotlinseminar.productSelectionService.application.aggregates.Order
import org.pv293.kotlinseminar.productSelectionService.application.aggregates.OrderStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OrderRepository : JpaRepository<Order, UUID> {
    fun findByCartId(cartId: UUID): Order?
    fun findByStatus(status: OrderStatus): List<Order>
}
