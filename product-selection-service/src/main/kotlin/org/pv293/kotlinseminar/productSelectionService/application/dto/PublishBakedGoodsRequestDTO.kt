package org.pv293.kotlinseminar.productSelectionService.application.dto

data class PublishBakedGoodsRequestDTO(
    val name: String,
    val description: String?,
    val initialStock: Int,
)
