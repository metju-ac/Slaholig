package org.pv293.kotlinseminar.productSelectionService.events.impl

import java.util.UUID

data class BakedGoodsPublishedEvent(
    val bakedGoodsId: UUID,
    val name: String,
    val description: String?,
    val initialStock: Int,
)
