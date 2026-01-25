package org.pv293.kotlinseminar.productSelectionService.application.policies

import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.EventHandler
import org.pv293.kotlinseminar.paymentService.events.impl.OrderCreatedFromCartEvent
import org.pv293.kotlinseminar.productSelectionService.application.commands.impl.DeleteShoppingCartCommand
import org.pv293.kotlinseminar.productSelectionService.application.commands.impl.RemoveBakedGoodFromCartCommand
import org.pv293.kotlinseminar.productSelectionService.events.impl.CartItemQuantityDecreasedEvent
import org.pv293.kotlinseminar.productSelectionService.events.impl.CartItemRemovedFromCartEvent
import org.pv293.kotlinseminar.productSelectionService.repository.ShoppingCartRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ShoppingCartPolicies(
    private val shoppingCartRepository: ShoppingCartRepository,
    private val commandGateway: CommandGateway,
) {
    private val logger = LoggerFactory.getLogger(ShoppingCartPolicies::class.java)

    @EventHandler
    fun on(event: CartItemQuantityDecreasedEvent) {
        if (event.newQuantity != 0) return

        val cart = shoppingCartRepository.findById(event.cartId).orElse(null) ?: return
        val stillHasItem = cart.items.any { it.bakedGoodsId == event.bakedGoodsId }
        if (!stillHasItem) return

        logger.info("Quantity reached 0; removing item ${event.bakedGoodsId} from cart ${event.cartId}")
        commandGateway.send<Any>(
            RemoveBakedGoodFromCartCommand(
                cartId = event.cartId,
                bakedGoodsId = event.bakedGoodsId,
            ),
        )
    }

    @EventHandler
    fun on(event: CartItemRemovedFromCartEvent) {
        val cart = shoppingCartRepository.findById(event.cartId).orElse(null) ?: return

        if (cart.items.isNotEmpty()) return

        logger.info("Cart ${event.cartId} became empty; deleting")
        commandGateway.send<Any>(DeleteShoppingCartCommand(cartId = event.cartId))
    }

    @EventHandler
    fun on(event: OrderCreatedFromCartEvent) {
        if (!shoppingCartRepository.existsById(event.cartId)) return

        logger.info("Order ${event.orderId} created from cart ${event.cartId}; deleting cart")
        commandGateway.send<Any>(DeleteShoppingCartCommand(cartId = event.cartId))
    }
}
