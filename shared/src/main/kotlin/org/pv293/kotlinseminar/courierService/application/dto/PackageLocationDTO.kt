package org.pv293.kotlinseminar.courierService.application.dto

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class PackageLocationDTO(
    val deliveryId: UUID,
    val orderId: UUID,
    val latitude: BigDecimal,
    val longitude: BigDecimal,
    val photoUrl: String?,
    val isExactLocation: Boolean,
    val droppedAt: Instant,
)
