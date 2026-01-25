package org.pv293.kotlinseminar.paymentService.events.impl

import java.util.UUID

data class PaymentProcessingStartedEvent(
    val orderId: UUID,
    val walletAddress: String? = null,
)
