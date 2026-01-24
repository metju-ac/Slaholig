package org.pv293.kotlinseminar.productSelectionService.application.dto

import java.util.UUID

data class ChosenLocationDTO(
    val locationId: UUID,
    val latitude: Double,
    val longitude: Double,
)
