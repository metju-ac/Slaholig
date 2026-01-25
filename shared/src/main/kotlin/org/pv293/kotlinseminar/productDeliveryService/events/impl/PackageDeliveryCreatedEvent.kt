package org.pv293.kotlinseminar.productDeliveryService.events.impl

import java.time.Instant
import java.util.UUID

data class PackageDeliveryCreatedEvent(
    val deliveryId: UUID,
    val orderId: UUID,
    val transactionId: String,
    val status: String,
    val createdAt: Instant,
)
