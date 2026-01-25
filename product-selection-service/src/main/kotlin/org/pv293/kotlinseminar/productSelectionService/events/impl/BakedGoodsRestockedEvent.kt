package org.pv293.kotlinseminar.productSelectionService.events.impl

import java.util.UUID

data class BakedGoodsRestockedEvent(
    val bakedGoodsId: UUID,
    val amount: Int,
    val newStock: Int,
)
