package org.pv293.kotlinseminar.paymentService.events.impl

import java.math.BigDecimal
import java.util.UUID

data class PaymentMarkedPaidEvent(
    val orderId: UUID,
    val transactionId: String,
    val customerLatitude: BigDecimal?,
    val customerLongitude: BigDecimal?,
)
