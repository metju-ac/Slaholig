package org.pv293.kotlinseminar.productSelectionService.application.dto

import java.util.UUID

data class AddBakedGoodsReviewRequestDTO(
    val authorId: UUID,
    val rating: Int,
    val content: String?,
)
