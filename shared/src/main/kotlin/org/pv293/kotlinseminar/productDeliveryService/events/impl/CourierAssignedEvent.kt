package org.pv293.kotlinseminar.productDeliveryService.events.impl

import java.time.Instant
import java.util.UUID

data class CourierAssignedEvent(
    val deliveryId: UUID,
    val courierId: UUID,
    val offerId: UUID,
    val assignedAt: Instant,
)
