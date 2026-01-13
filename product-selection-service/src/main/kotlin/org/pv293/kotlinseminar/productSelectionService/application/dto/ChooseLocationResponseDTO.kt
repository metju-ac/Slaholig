package org.pv293.kotlinseminar.productSelectionService.application.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.UUID

data class ChooseLocationResponseDTO(
    @field:Schema(example = "11111111-1111-1111-1111-111111111111")
    val locationId: UUID,
    @field:Schema(example = "49.1951")
    val latitude: BigDecimal,
    @field:Schema(example = "16.6068")
    val longitude: BigDecimal,
    val availableGoods: List<BakedGoodDTO>,
)
