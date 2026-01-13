package org.pv293.kotlinseminar.productSelectionService.application.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

data class UpdateBakedGoodPriceRequestDTO(
    @field:Schema(example = "5.99")
    val newPrice: BigDecimal,
)
