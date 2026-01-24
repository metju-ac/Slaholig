package org.pv293.kotlinseminar.productSelectionService.application.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class AddCartItemRequestDTO(
    @field:Schema(example = "22222222-2222-2222-2222-222222222222")
    val bakedGoodsId: UUID,
    @field:Schema(example = "2")
    val quantity: Int,
)
