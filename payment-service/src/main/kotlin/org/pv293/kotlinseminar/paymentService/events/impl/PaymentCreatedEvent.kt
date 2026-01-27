package org.pv293.kotlinseminar.paymentService.events.impl

import org.pv293.kotlinseminar.paymentService.application.aggregates.PaymentStatus
import java.math.BigDecimal
import java.util.UUID

data class PaymentCreatedEvent(
    val orderId: UUID,
    val cartId: UUID,
    val items: List<OrderItemDTO>,
    val status: PaymentStatus,
    val customerLatitude: BigDecimal?,
    val customerLongitude: BigDecimal?,
)

// OrderItemDTO is now defined in shared module at:
// org.pv293.kotlinseminar.paymentService.events.impl.OrderItemDTO
// (in shared/src/main/kotlin/.../OrderCreatedFromCartEvent.kt)
