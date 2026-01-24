package org.pv293.kotlinseminar.productSelectionService.application.dto

import java.util.UUID

data class AddCartItemRequestDTO(
    val bakedGoodsId: UUID,
    val quantity: Int,
)
