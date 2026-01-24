package org.pv293.kotlinseminar.productSelectionService.application.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class AddBakedGoodsReviewRequestDTO(
    @field:Schema(example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
    val authorId: UUID,
    @field:Schema(example = "5")
    val rating: Int,
    @field:Schema(example = "Amazing crust, will buy again")
    val content: String?,
)
