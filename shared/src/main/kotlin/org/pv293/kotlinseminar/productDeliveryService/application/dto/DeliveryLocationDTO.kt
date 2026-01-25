package org.pv293.kotlinseminar.productDeliveryService.application.dto

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class DeliveryLocationDTO(
    val deliveryId: UUID,
    val orderId: UUID,
    val latitude: BigDecimal,
    val longitude: BigDecimal,
    val photoUrl: String,
    val droppedAt: Instant,
)
