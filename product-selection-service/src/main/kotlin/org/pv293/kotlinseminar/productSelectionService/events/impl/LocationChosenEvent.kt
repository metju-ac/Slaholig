package org.pv293.kotlinseminar.productSelectionService.events.impl

import java.util.UUID

data class LocationChosenEvent(
    val locationId: UUID,
    val latitude: Double,
    val longitude: Double,
)
