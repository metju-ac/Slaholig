package org.pv293.kotlinseminar.productSelectionService.application.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.pv293.kotlinseminar.productSelectionService.application.aggregates.OrderStatus
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class OrderDTO(
    @field:Schema(example = "33333333-3333-3333-3333-333333333333")
    val orderId: UUID,

    @field:Schema(example = "11111111-1111-1111-1111-111111111111")
    val cartId: UUID,

    @field:Schema(example = "2026-01-25T10:30:00Z")
    val createdAt: Instant,

    @field:Schema(example = "CREATED")
    val status: String,

    val items: List<OrderItemDetailDTO>,

    @field:Schema(example = "24.95")
    val subtotal: BigDecimal,

    @field:Schema(example = "24.95")
    val total: BigDecimal,
)

data class OrderItemDetailDTO(
    @field:Schema(example = "44444444-4444-4444-4444-444444444444")
    val id: UUID,

    @field:Schema(example = "22222222-2222-2222-2222-222222222222")
    val bakedGoodsId: UUID,

    @field:Schema(example = "Sourdough loaf")
    val bakedGoodsName: String,

    @field:Schema(example = "3")
    val quantity: Int,

    @field:Schema(example = "4.99")
    val pricePerUnit: BigDecimal,

    @field:Schema(example = "14.97")
    val totalPrice: BigDecimal,
)
