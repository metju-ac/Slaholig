package org.pv293.kotlinseminar.productSelectionService.application.dto

import java.util.UUID

data class ShoppingCartDTO(
    val cartId: UUID,
    val items: List<ShoppingCartItemDTO>,
)

data class ShoppingCartItemDTO(
    val bakedGoodsId: UUID,
    val quantity: Int,
)
