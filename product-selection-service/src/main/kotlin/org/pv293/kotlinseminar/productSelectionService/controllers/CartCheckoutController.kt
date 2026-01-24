package org.pv293.kotlinseminar.productSelectionService.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.axonframework.commandhandling.gateway.CommandGateway
import org.pv293.kotlinseminar.productSelectionService.application.commands.impl.CreateOrderFromCartCommand
import org.pv293.kotlinseminar.productSelectionService.application.dto.CreateOrderFromCartResponseDTO
import org.pv293.kotlinseminar.productSelectionService.repository.ShoppingCartRepository
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/product-selection/carts")
class CartCheckoutController(
    private val commandGateway: CommandGateway,
    private val shoppingCartRepository: ShoppingCartRepository,
) {
    private val logger = LoggerFactory.getLogger(CartCheckoutController::class.java)

    @PostMapping("/{cartId}/order")
    @Operation(summary = "Create order from cart")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Order created"),
            ApiResponse(responseCode = "404", description = "Cart not found"),
            ApiResponse(responseCode = "400", description = "Cart empty"),
        ],
    )
    fun createOrder(@PathVariable cartId: String): CreateOrderFromCartResponseDTO {
        val cartUUID = UUID.fromString(cartId)
        logger.info("Creating order from cart $cartId")

        if (!shoppingCartRepository.existsById(cartUUID)) {
            throw IllegalArgumentException("Shopping cart with id $cartUUID not found")
        }

        val orderId = UUID.randomUUID()
        commandGateway.sendAndWait<UUID>(
            CreateOrderFromCartCommand(
                cartId = cartUUID,
                orderId = orderId,
            ),
        )

        return CreateOrderFromCartResponseDTO(orderId = orderId)
    }
}
