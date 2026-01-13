package org.pv293.kotlinseminar.paymentService.events.impl

import java.util.UUID

data class FundsReleasedEvent(
    val orderId: UUID,
)
