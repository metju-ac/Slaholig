package org.pv293.kotlinseminar.paymentService.events.impl

import java.util.UUID

data class PaymentFailedEvent(
    val orderId: UUID,
    val reason: String,
)
