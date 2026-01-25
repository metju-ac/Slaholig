package org.pv293.kotlinseminar.productSelectionService.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.Parameter
import org.axonframework.commandhandling.gateway.CommandGateway
import org.pv293.kotlinseminar.productSelectionService.application.commands.impl.CreateOrderFromCartCommand
import org.pv293.kotlinseminar.productSelectionService.application.dto.CreateOrderFromCartResponseDTO
import org.pv293.kotlinseminar.productSelectionService.repository.BakedGoodRepository
import org.pv293.kotlinseminar.productSelectionService.repository.ShoppingCartRepository
import org.pv293.kotlinseminar.paymentService.events.impl.OrderItemDTO
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
    private val bakedGoodRepository: BakedGoodRepository,
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
    fun createOrder(
        @Parameter(example = "11111111-1111-1111-1111-111111111111")
        @PathVariable cartId: String,
    ): CreateOrderFromCartResponseDTO {
        val cartUUID = UUID.fromString(cartId)
        logger.info("Creating order from cart $cartId")

        val cart = shoppingCartRepository.findById(cartUUID).orElseThrow {
            IllegalArgumentException("Shopping cart with id $cartUUID not found")
        }

        require(cart.items.isNotEmpty()) { "Cart is empty" }

        // Fetch current prices for all items and create enriched OrderItemDTOs
        val enrichedItems = cart.items.map { cartItem ->
            val bakedGood = bakedGoodRepository.findById(cartItem.bakedGoodsId).orElseThrow {
                IllegalArgumentException("BakedGood with id ${cartItem.bakedGoodsId} not found")
            }
            OrderItemDTO(
                bakedGoodsId = cartItem.bakedGoodsId,
                quantity = cartItem.quantity,
                price = bakedGood.price,
                totalPrice = bakedGood.price.multiply(cartItem.quantity.toBigDecimal()),
            )
        }

        val orderId = UUID.randomUUID()
        commandGateway.sendAndWait<UUID>(
            CreateOrderFromCartCommand(
                cartId = cartUUID,
                orderId = orderId,
                items = enrichedItems,
            ),
        )

        return CreateOrderFromCartResponseDTO(orderId = orderId)
    }
}
