package org.pv293.kotlinseminar.productSelectionService.events.impl

import java.math.BigDecimal
import java.util.UUID

data class LocationChosenEvent(
    val locationId: UUID,
    val latitude: BigDecimal,
    val longitude: BigDecimal,
)
