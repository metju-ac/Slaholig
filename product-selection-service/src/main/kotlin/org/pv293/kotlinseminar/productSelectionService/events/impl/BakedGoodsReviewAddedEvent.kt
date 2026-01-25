package org.pv293.kotlinseminar.productSelectionService.events.impl

import java.util.UUID

data class BakedGoodsReviewAddedEvent(
    val bakedGoodsId: UUID,
    val reviewId: UUID,
    val authorId: UUID,
    val rating: Int,
    val content: String?,
)
