package org.pv293.kotlinseminar.productSelectionService.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.pv293.kotlinseminar.productSelectionService.application.dto.OrderDTO
import org.pv293.kotlinseminar.productSelectionService.application.queries.impl.OrderQuery
import org.pv293.kotlinseminar.productSelectionService.application.queries.impl.OrdersQuery
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/product-selection/orders")
class OrderController(
    private val queryGateway: QueryGateway,
) {
    private val logger = LoggerFactory.getLogger(OrderController::class.java)

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Order found"),
            ApiResponse(responseCode = "404", description = "Order not found"),
        ],
    )
    fun getOrder(
        @Parameter(example = "33333333-3333-3333-3333-333333333333")
        @PathVariable orderId: String,
    ): OrderDTO {
        logger.info("Getting order with id: $orderId")
        val orderUUID = UUID.fromString(orderId)
        return queryGateway.query(OrderQuery(orderUUID), OrderDTO::class.java).get()
    }

    @GetMapping("")
    @Operation(summary = "Get all orders")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Orders returned"),
        ],
    )
    fun getOrders(): List<OrderDTO> {
        logger.info("Getting all orders")
        return queryGateway.query(
            OrdersQuery(),
            ResponseTypes.multipleInstancesOf(OrderDTO::class.java),
        ).get()
    }
}
