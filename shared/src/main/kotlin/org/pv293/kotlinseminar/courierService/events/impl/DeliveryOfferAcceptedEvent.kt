package org.pv293.kotlinseminar.courierService.events.impl

import java.time.Instant
import java.util.UUID

data class DeliveryOfferAcceptedEvent(
    val offerId: UUID,
    val deliveryId: UUID,
    val orderId: UUID,
    val courierId: UUID,
    val acceptedAt: Instant,
)
