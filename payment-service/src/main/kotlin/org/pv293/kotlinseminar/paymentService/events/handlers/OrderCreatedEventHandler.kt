package org.pv293.kotlinseminar.paymentService.events.handlers

import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.pv293.kotlinseminar.paymentService.application.commands.impl.CreatePaymentCommand
import org.pv293.kotlinseminar.paymentService.events.impl.OrderItemDTO
import org.pv293.kotlinseminar.paymentService.repository.PaymentRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("payment-service")
class OrderCreatedEventHandler(
    private val commandGateway: CommandGateway,
    private val paymentRepository: PaymentRepository,
) {
    private val logger = LoggerFactory.getLogger(OrderCreatedEventHandler::class.java)

    @EventHandler
    fun on(event: org.pv293.kotlinseminar.paymentService.events.impl.OrderCreatedFromCartEvent) {
        logger.info("Received OrderCreatedFromCartEvent for order ${event.orderId}")

        // Idempotency check: if payment already exists, ignore
        if (paymentRepository.existsById(event.orderId)) {
            logger.warn("Payment for order ${event.orderId} already exists, skipping creation")
            return
        }

        // Convert shared event DTOs to payment service DTOs (pass through all fields including prices)
        val items = event.items.map {
            OrderItemDTO(
                bakedGoodsId = it.bakedGoodsId,
                quantity = it.quantity,
                price = it.price,
                totalPrice = it.totalPrice,
            )
        }

        logger.info("Creating payment for order ${event.orderId}")
        commandGateway.sendAndWait<Any>(
            CreatePaymentCommand(
                orderId = event.orderId,
                cartId = event.cartId,
                items = items,
                customerLatitude = event.customerLatitude,
                customerLongitude = event.customerLongitude,
            ),
        )
    }
}
