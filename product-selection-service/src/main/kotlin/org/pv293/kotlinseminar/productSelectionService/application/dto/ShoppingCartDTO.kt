package org.pv293.kotlinseminar.productSelectionService.application.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class ShoppingCartDTO(
    @field:Schema(example = "11111111-1111-1111-1111-111111111111")
    val cartId: UUID,
    val items: List<ShoppingCartItemDTO>,
)

data class ShoppingCartItemDTO(
    @field:Schema(example = "22222222-2222-2222-2222-222222222222")
    val bakedGoodsId: UUID,
    @field:Schema(example = "3")
    val quantity: Int,
)
