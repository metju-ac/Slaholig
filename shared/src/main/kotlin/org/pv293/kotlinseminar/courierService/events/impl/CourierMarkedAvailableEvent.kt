package org.pv293.kotlinseminar.courierService.events.impl

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class CourierMarkedAvailableEvent(
    val courierId: UUID,
    val latitude: BigDecimal,
    val longitude: BigDecimal,
    val availableAt: Instant,
)
