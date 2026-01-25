package org.pv293.kotlinseminar.productSelectionService.application.dto

import io.swagger.v3.oas.annotations.media.Schema

data class UpdateCartItemRequestDTO(
    @field:Schema(example = "5")
    val quantity: Int? = null,
    @field:Schema(example = "-2")
    val delta: Int? = null,
)
