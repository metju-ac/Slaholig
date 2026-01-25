package org.pv293.kotlinseminar.productSelectionService.application.queries.handlers

import org.axonframework.queryhandling.QueryHandler
import org.pv293.kotlinseminar.productSelectionService.application.dto.OrderDTO
import org.pv293.kotlinseminar.productSelectionService.application.dto.OrderItemDetailDTO
import org.pv293.kotlinseminar.productSelectionService.application.queries.impl.OrderQuery
import org.pv293.kotlinseminar.productSelectionService.application.queries.impl.OrdersQuery
import org.pv293.kotlinseminar.productSelectionService.repository.OrderRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class OrderQueryHandler(
    private val orderRepository: OrderRepository,
) {
    private val logger = LoggerFactory.getLogger(OrderQueryHandler::class.java)

    @QueryHandler
    fun handle(query: OrderQuery): OrderDTO {
        val order = orderRepository.findById(query.orderId).orElseThrow {
            logger.warn("Could not find order by id: ${query.orderId}")
            IllegalArgumentException("Order with id ${query.orderId} not found")
        }

        return OrderDTO(
            orderId = order.orderId,
            cartId = order.cartId,
            createdAt = order.createdAt,
            status = order.status.name,
            items = order.items.map { item ->
                OrderItemDetailDTO(
                    id = item.id,
                    bakedGoodsId = item.bakedGoodsId,
                    bakedGoodsName = item.bakedGoodsName,
                    quantity = item.quantity,
                    pricePerUnit = item.pricePerUnit,
                    totalPrice = item.totalPrice,
                )
            },
            subtotal = order.subtotal,
            total = order.subtotal, // In future: could add tax, shipping, discounts
        )
    }

    @QueryHandler
    fun handle(query: OrdersQuery): List<OrderDTO> {
        return orderRepository.findAll().map { order ->
            OrderDTO(
                orderId = order.orderId,
                cartId = order.cartId,
                createdAt = order.createdAt,
                status = order.status.name,
                items = order.items.map { item ->
                    OrderItemDetailDTO(
                        id = item.id,
                        bakedGoodsId = item.bakedGoodsId,
                        bakedGoodsName = item.bakedGoodsName,
                        quantity = item.quantity,
                        pricePerUnit = item.pricePerUnit,
                        totalPrice = item.totalPrice,
                    )
                },
                subtotal = order.subtotal,
                total = order.subtotal,
            )
        }
    }
}
