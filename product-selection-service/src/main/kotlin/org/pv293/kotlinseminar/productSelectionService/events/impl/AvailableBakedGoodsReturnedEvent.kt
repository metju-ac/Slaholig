package org.pv293.kotlinseminar.productSelectionService.events.impl

import java.util.UUID

data class AvailableBakedGoodsReturnedEvent(
    val locationId: UUID,
    val radiusKm: Double,
    val bakedGoodsIds: List<UUID>,
)
