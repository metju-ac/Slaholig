package org.pv293.kotlinseminar.productDeliveryService.events.impl

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class PackageDroppedByBakerEvent(
    val deliveryId: UUID,
    val orderId: UUID,
    val droppedAt: Instant,
    val latitude: BigDecimal,
    val longitude: BigDecimal,
    val photoUrl: String,
)
