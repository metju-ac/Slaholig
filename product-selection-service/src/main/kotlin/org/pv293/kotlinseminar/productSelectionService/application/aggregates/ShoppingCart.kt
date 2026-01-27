package org.pv293.kotlinseminar.productSelectionService.application.aggregates

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle.apply
import org.axonframework.modelling.command.AggregateLifecycle.markDeleted
import org.axonframework.modelling.command.AggregateMember
import org.axonframework.modelling.command.ForwardMatchingInstances
import org.axonframework.spring.stereotype.Aggregate
import org.pv293.kotlinseminar.productSelectionService.application.commands.impl.AddBakedGoodToCartCommand
import org.pv293.kotlinseminar.productSelectionService.application.commands.impl.AdjustCartItemQuantityCommand
import org.pv293.kotlinseminar.productSelectionService.application.commands.impl.CreateShoppingCartWithItemCommand
import org.pv293.kotlinseminar.productSelectionService.application.commands.impl.CreateOrderFromCartCommand
import org.pv293.kotlinseminar.productSelectionService.application.commands.impl.DeleteShoppingCartCommand
import org.pv293.kotlinseminar.productSelectionService.application.commands.impl.RemoveBakedGoodFromCartCommand
import org.pv293.kotlinseminar.productSelectionService.application.commands.impl.SetCartItemQuantityCommand
import org.pv293.kotlinseminar.productSelectionService.events.impl.CartItemQuantityDecreasedEvent
import org.pv293.kotlinseminar.productSelectionService.events.impl.CartItemQuantityIncreasedEvent
import org.pv293.kotlinseminar.productSelectionService.events.impl.CartItemQuantitySetEvent
import org.pv293.kotlinseminar.productSelectionService.events.impl.CartItemRemovedFromCartEvent
import org.pv293.kotlinseminar.productSelectionService.events.impl.ShoppingCartCreatedEvent
import org.pv293.kotlinseminar.productSelectionService.events.impl.ShoppingCartDeletedEvent
import org.pv293.kotlinseminar.paymentService.events.impl.OrderCreatedFromCartEvent
import org.pv293.kotlinseminar.paymentService.events.impl.OrderItemDTO
import java.util.UUID
import kotlin.math.min

@Entity
@Aggregate(repository = "shoppingCartAggregateRepository")
@Table(name = "shopping_cart")
class ShoppingCart() {

    @Id
    @AggregateIdentifier
    lateinit var id: UUID

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "cart", cascade = [CascadeType.ALL], orphanRemoval = true)
    @AggregateMember(eventForwardingMode = ForwardMatchingInstances::class)
    open var items: MutableList<ShoppingCartItem> = mutableListOf()

    @CommandHandler
    constructor(command: CreateShoppingCartWithItemCommand) : this() {
        require(command.quantity > 0) { "Quantity must be > 0" }

        id = command.cartId
        apply(ShoppingCartCreatedEvent(cartId = command.cartId))

        val item = ShoppingCartItem().apply {
            id = UUID.randomUUID()
            bakedGoodsId = command.bakedGoodsId
            quantity = command.quantity
            cart = this@ShoppingCart
        }
        items.add(item)

        apply(
            CartItemQuantityIncreasedEvent(
                cartId = id,
                bakedGoodsId = command.bakedGoodsId,
                delta = command.quantity,
                newQuantity = command.quantity,
            ),
        )
    }

    @CommandHandler
    fun handle(command: AddBakedGoodToCartCommand) {
        require(command.quantity > 0) { "Quantity must be > 0" }

        val existingItem = items.firstOrNull { it.bakedGoodsId == command.bakedGoodsId }
        val newQuantity = (existingItem?.quantity ?: 0) + command.quantity

        if (existingItem == null) {
            val item = ShoppingCartItem().apply {
                id = UUID.randomUUID()
                bakedGoodsId = command.bakedGoodsId
                quantity = newQuantity
                cart = this@ShoppingCart
            }
            items.add(item)
        } else {
            existingItem.quantity = newQuantity
        }

        apply(
            CartItemQuantityIncreasedEvent(
                cartId = id,
                bakedGoodsId = command.bakedGoodsId,
                delta = command.quantity,
                newQuantity = newQuantity,
            ),
        )
    }

    @CommandHandler
    fun handle(command: SetCartItemQuantityCommand) {
        require(command.quantity >= 0) { "Quantity must be >= 0" }

        val item = items.firstOrNull { it.bakedGoodsId == command.bakedGoodsId } ?: return

        if (command.quantity == 0) {
            removeItem(command.bakedGoodsId)
            return
        }

        item.quantity = command.quantity
        apply(
            CartItemQuantitySetEvent(
                cartId = id,
                bakedGoodsId = command.bakedGoodsId,
                quantity = command.quantity,
            ),
        )
    }

    @CommandHandler
    fun handle(command: AdjustCartItemQuantityCommand) {
        val item = items.firstOrNull { it.bakedGoodsId == command.bakedGoodsId } ?: return

        if (command.delta == 0) return

        if (command.delta > 0) {
            val newQuantity = item.quantity + command.delta
            item.quantity = newQuantity

            apply(
                CartItemQuantityIncreasedEvent(
                    cartId = id,
                    bakedGoodsId = command.bakedGoodsId,
                    delta = command.delta,
                    newQuantity = newQuantity,
                ),
            )
            return
        }

        val decrease = min(item.quantity, -command.delta)
        if (decrease == 0) return

        val newQuantity = item.quantity - decrease
        item.quantity = newQuantity
        apply(
            CartItemQuantityDecreasedEvent(
                cartId = id,
                bakedGoodsId = command.bakedGoodsId,
                delta = decrease,
                newQuantity = newQuantity,
            ),
        )
    }

    @CommandHandler
    fun handle(command: RemoveBakedGoodFromCartCommand) {
        removeItem(command.bakedGoodsId)
    }

    @CommandHandler
    fun handle(command: CreateOrderFromCartCommand) {
        require(items.isNotEmpty()) { "Cannot create order from empty cart" }

        apply(
            OrderCreatedFromCartEvent(
                orderId = command.orderId,
                cartId = id,
                items = command.items,
                customerLatitude = command.customerLatitude,
                customerLongitude = command.customerLongitude,
            ),
        )
    }

    @CommandHandler
    fun handle(command: DeleteShoppingCartCommand) {
        apply(ShoppingCartDeletedEvent(cartId = id))
        markDeleted()
    }

    private fun removeItem(bakedGoodsId: UUID): Boolean {
        val item = items.firstOrNull { it.bakedGoodsId == bakedGoodsId } ?: return false
        items.remove(item)

        apply(
            CartItemRemovedFromCartEvent(
                cartId = id,
                bakedGoodsId = bakedGoodsId,
            ),
        )
        return true
    }

}
