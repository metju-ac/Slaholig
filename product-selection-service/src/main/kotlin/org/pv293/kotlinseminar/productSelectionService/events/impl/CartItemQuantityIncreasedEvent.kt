package org.pv293.kotlinseminar.productSelectionService.events.impl

import java.util.UUID

data class CartItemQuantityIncreasedEvent(
    val cartId: UUID,
    val bakedGoodsId: UUID,
    val delta: Int,
    val newQuantity: Int,
)
