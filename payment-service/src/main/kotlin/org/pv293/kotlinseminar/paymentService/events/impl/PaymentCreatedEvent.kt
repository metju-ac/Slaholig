package org.pv293.kotlinseminar.paymentService.events.impl

import org.pv293.kotlinseminar.paymentService.application.aggregates.PaymentStatus
import java.util.UUID

data class PaymentCreatedEvent(
    val orderId: UUID,
    val cartId: UUID,
    val items: List<OrderItemDTO>,
    val status: PaymentStatus,
)

data class OrderItemDTO(
    val bakedGoodsId: UUID,
    val quantity: Int,
)
