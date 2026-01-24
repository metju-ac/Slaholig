package org.pv293.kotlinseminar.productSelectionService.application.dto

import java.util.UUID

data class BakedGoodReviewDTO(
    val id: UUID,
    val authorId: UUID,
    val rating: Int,
    val content: String?,
)
