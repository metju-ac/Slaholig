package org.pv293.kotlinseminar.paymentService.application.queries.handlers

import org.axonframework.queryhandling.QueryHandler
import org.pv293.kotlinseminar.paymentService.application.dto.PaymentDTO
import org.pv293.kotlinseminar.paymentService.application.dto.PaymentItemDTO
import org.pv293.kotlinseminar.paymentService.application.queries.impl.PaymentQuery
import org.pv293.kotlinseminar.paymentService.repository.PaymentRepository
import org.springframework.stereotype.Component

@Component
class PaymentQueryHandler(
    private val paymentRepository: PaymentRepository,
) {
    @QueryHandler
    fun handle(query: PaymentQuery): PaymentDTO {
        val payment = paymentRepository.findById(query.orderId)
            .orElseThrow { IllegalArgumentException("Payment not found for order ${query.orderId}") }

        return PaymentDTO(
            orderId = payment.orderId,
            cartId = payment.cartId,
            items = payment.items.map {
                PaymentItemDTO(
                    bakedGoodsId = it.bakedGoodsId,
                    quantity = it.quantity,
                    price = it.price,
                    totalPrice = it.totalPrice,
                )
            },
            status = payment.status,
            transactionId = payment.transactionId,
            failureReason = payment.failureReason,
            walletAddress = payment.walletAddress,
        )
    }
}
