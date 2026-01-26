package org.pv293.kotlinseminar.productDeliveryService.events.impl

import java.time.Instant
import java.util.UUID

data class PackageRetrievedEvent(
    val deliveryId: UUID,
    val orderId: UUID,
    val retrievedAt: Instant,
)
