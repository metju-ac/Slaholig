package org.pv293.kotlinseminar.paymentService.events.impl

import java.util.UUID

data class PaymentSucceededEvent(
    val orderId: UUID,
    val transactionId: String,
)
