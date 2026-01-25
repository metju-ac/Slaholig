package org.pv293.kotlinseminar.productDeliveryService.application.dto

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class PackageDeliveryDTO(
    val deliveryId: UUID,
    val orderId: UUID,
    val transactionId: String,
    val status: String,
    val createdAt: Instant,
    val droppedByBakerAt: Instant? = null,
    val latitude: BigDecimal? = null,
    val longitude: BigDecimal? = null,
    val photoUrl: String? = null,
)
