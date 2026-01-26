package org.pv293.kotlinseminar.productSelectionService.application.events

import org.axonframework.eventhandling.EventHandler
import org.pv293.kotlinseminar.paymentService.events.impl.OrderCreatedFromCartEvent
import org.pv293.kotlinseminar.paymentService.events.impl.PaymentMarkedPaidEvent
import org.pv293.kotlinseminar.productSelectionService.application.aggregates.Order
import org.pv293.kotlinseminar.productSelectionService.application.aggregates.OrderItem
import org.pv293.kotlinseminar.productSelectionService.application.aggregates.OrderStatus
import org.pv293.kotlinseminar.productSelectionService.repository.BakedGoodRepository
import org.pv293.kotlinseminar.productSelectionService.repository.OrderRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class OrderProjectionEventHandler(
    private val orderRepository: OrderRepository,
    private val bakedGoodRepository: BakedGoodRepository,
) {
    private val logger = LoggerFactory.getLogger(OrderProjectionEventHandler::class.java)

    @EventHandler
    fun on(event: OrderCreatedFromCartEvent) {
        logger.info("Creating order projection for orderId: ${event.orderId}")

        val orderItems = event.items.map { itemDTO ->
            // Fetch baked good name for denormalization
            val bakedGoodName = bakedGoodRepository.findById(itemDTO.bakedGoodsId)
                .map { it.name }
                .orElse("Unknown Product")

            OrderItem(
                bakedGoodsId = itemDTO.bakedGoodsId,
                bakedGoodsName = bakedGoodName,
                quantity = itemDTO.quantity,
                pricePerUnit = itemDTO.price,
                totalPrice = itemDTO.totalPrice,
            )
        }

        val subtotal = orderItems.fold(BigDecimal.ZERO) { acc, item -> acc + item.totalPrice }

        val order = Order(
            orderId = event.orderId,
            cartId = event.cartId,
            subtotal = subtotal,
            status = OrderStatus.CREATED,
            customerLatitude = event.customerLatitude,
            customerLongitude = event.customerLongitude,
        )

        // Set bidirectional relationship
        orderItems.forEach { it.order = order }
        order.items.addAll(orderItems)

        orderRepository.save(order)
        logger.info("Order projection created for orderId: ${event.orderId} with ${orderItems.size} items, subtotal: $subtotal")
    }

    @EventHandler
    fun on(event: PaymentMarkedPaidEvent) {
        logger.info("Updating order projection status to PAID for orderId: ${event.orderId}")

        orderRepository.findById(event.orderId).ifPresent { order ->
            order.status = OrderStatus.PAID
            orderRepository.save(order)
            logger.info("Order projection updated to PAID for orderId: ${event.orderId}")
        }
    }
}
