package org.pv293.kotlinseminar.productSelectionService.events.impl

import java.util.UUID

data class CartItemRemovedFromCartEvent(
    val cartId: UUID,
    val bakedGoodsId: UUID,
)
