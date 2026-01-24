package org.pv293.kotlinseminar.productSelectionService.application.dto

import io.swagger.v3.oas.annotations.media.Schema

data class RestockBakedGoodsRequestDTO(
    @field:Schema(example = "5")
    val amount: Int,
)
