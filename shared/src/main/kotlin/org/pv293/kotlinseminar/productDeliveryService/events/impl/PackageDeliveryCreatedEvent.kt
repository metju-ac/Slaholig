package org.pv293.kotlinseminar.productDeliveryService.events.impl

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class PackageDeliveryCreatedEvent(
    val deliveryId: UUID,
    val orderId: UUID,
    val transactionId: String,
    val status: String,
    val createdAt: Instant,
    val customerLatitude: BigDecimal?,
    val customerLongitude: BigDecimal?,
)
