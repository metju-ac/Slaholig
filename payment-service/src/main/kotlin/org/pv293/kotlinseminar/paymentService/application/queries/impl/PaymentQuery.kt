package org.pv293.kotlinseminar.paymentService.application.queries.impl

import java.util.UUID

data class PaymentQuery(
    val orderId: UUID,
)
