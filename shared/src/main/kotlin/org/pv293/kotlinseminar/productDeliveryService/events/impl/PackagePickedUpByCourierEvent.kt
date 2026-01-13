package org.pv293.kotlinseminar.productDeliveryService.events.impl

import java.time.Instant
import java.util.UUID

data class PackagePickedUpByCourierEvent(
    val deliveryId: UUID,
    val courierId: UUID,
    val pickedUpAt: Instant,
)
