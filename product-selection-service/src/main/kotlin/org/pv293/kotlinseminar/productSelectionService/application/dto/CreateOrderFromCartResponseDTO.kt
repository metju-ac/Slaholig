package org.pv293.kotlinseminar.productSelectionService.application.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class CreateOrderFromCartResponseDTO(
    @field:Schema(example = "33333333-3333-3333-3333-333333333333")
    val orderId: UUID,
)
