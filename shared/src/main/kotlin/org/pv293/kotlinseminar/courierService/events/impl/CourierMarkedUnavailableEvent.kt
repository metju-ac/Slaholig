package org.pv293.kotlinseminar.courierService.events.impl

import java.time.Instant
import java.util.UUID

data class CourierMarkedUnavailableEvent(
    val courierId: UUID,
    val unavailableAt: Instant,
)
