package org.pv293.kotlinseminar.productSelectionService.application.dto

import io.swagger.v3.oas.annotations.media.Schema

data class ChooseLocationRequestDTO(
    @field:Schema(example = "49.1951")
    val latitude: Double,
    @field:Schema(example = "16.6068")
    val longitude: Double,
)
