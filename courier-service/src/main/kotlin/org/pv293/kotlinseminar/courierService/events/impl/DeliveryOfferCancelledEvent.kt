package org.pv293.kotlinseminar.courierService.events.impl

import java.time.Instant
import java.util.UUID

data class DeliveryOfferCancelledEvent(
    val offerId: UUID,
    val deliveryId: UUID,
    val courierId: UUID,
    val reason: String,
    val cancelledAt: Instant,
)
