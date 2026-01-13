package org.pv293.kotlinseminar.productSelectionService.application.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.UUID

data class BakedGoodDTO(
    @field:Schema(example = "22222222-2222-2222-2222-222222222222")
    val id: UUID,
    @field:Schema(example = "Sourdough loaf")
    val name: String,
    @field:Schema(example = "Crusty and tangy")
    val description: String?,
    @field:Schema(example = "12")
    val stock: Int,
    @field:Schema(example = "4.99")
    val price: BigDecimal,
    @field:Schema(example = "49.1951")
    val latitude: BigDecimal,
    @field:Schema(example = "16.6068")
    val longitude: BigDecimal,
)
