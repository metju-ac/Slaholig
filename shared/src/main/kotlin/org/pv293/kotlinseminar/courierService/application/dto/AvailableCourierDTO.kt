package org.pv293.kotlinseminar.courierService.application.dto

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class AvailableCourierDTO(
    val courierId: UUID,
    val latitude: BigDecimal,
    val longitude: BigDecimal,
    val lastUpdatedAt: Instant,
)
