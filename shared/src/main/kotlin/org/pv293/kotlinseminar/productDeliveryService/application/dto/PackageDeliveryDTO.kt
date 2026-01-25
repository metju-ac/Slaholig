package org.pv293.kotlinseminar.productDeliveryService.application.dto

import java.time.Instant
import java.util.UUID

data class PackageDeliveryDTO(
    val deliveryId: UUID,
    val orderId: UUID,
    val transactionId: String,
    val status: String,
    val createdAt: Instant,
)
