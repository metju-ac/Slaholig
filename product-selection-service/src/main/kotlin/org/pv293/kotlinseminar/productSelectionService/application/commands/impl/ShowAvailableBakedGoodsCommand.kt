package org.pv293.kotlinseminar.productSelectionService.application.commands.impl

import java.util.UUID

data class ShowAvailableBakedGoodsCommand(
    val locationId: UUID,
    val radiusKm: Double = 100.0,
)
