package org.pv293.kotlinseminar.productSelectionService.events.impl

import java.util.UUID

data class CartItemQuantitySetEvent(
    val cartId: UUID,
    val bakedGoodsId: UUID,
    val quantity: Int,
)
