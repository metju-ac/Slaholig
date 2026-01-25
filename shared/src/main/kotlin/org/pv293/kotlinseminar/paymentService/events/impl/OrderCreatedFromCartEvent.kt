package org.pv293.kotlinseminar.paymentService.events.impl

import java.util.UUID

data class OrderCreatedFromCartEvent(
    val orderId: UUID,
    val cartId: UUID,
    val items: List<OrderItemDTO>,
)

data class OrderItemDTO(
    val bakedGoodsId: UUID,
    val quantity: Int,
)
