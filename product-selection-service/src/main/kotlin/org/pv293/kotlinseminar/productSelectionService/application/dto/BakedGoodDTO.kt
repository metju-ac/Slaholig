package org.pv293.kotlinseminar.productSelectionService.application.dto

import java.util.UUID

data class BakedGoodDTO(
    val id: UUID,
    val name: String,
    val description: String?,
    val stock: Int,
    val latitude: Double,
    val longitude: Double,
)
