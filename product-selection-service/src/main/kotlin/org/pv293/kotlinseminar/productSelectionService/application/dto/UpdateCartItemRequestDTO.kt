package org.pv293.kotlinseminar.productSelectionService.application.dto

data class UpdateCartItemRequestDTO(
    val quantity: Int? = null,
    val delta: Int? = null,
)
