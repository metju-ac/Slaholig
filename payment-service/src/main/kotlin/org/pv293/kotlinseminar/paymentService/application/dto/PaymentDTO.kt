package org.pv293.kotlinseminar.paymentService.application.dto

import org.pv293.kotlinseminar.paymentService.application.aggregates.PaymentStatus
import java.math.BigDecimal
import java.util.UUID

data class PaymentDTO(
    val orderId: UUID,
    val cartId: UUID,
    val items: List<PaymentItemDTO>,
    val status: PaymentStatus,
    val transactionId: String?,
    val failureReason: String?,
    val walletAddress: String?,
)

data class PaymentItemDTO(
    val bakedGoodsId: UUID,
    val quantity: Int,
    val price: BigDecimal,
    val totalPrice: BigDecimal,
)
