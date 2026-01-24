package org.pv293.kotlinseminar.productSelectionService.controllers

import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.queryhandling.QueryGateway
import org.pv293.kotlinseminar.productSelectionService.application.commands.impl.AddBakedGoodToCartCommand
import org.pv293.kotlinseminar.productSelectionService.application.commands.impl.AdjustCartItemQuantityCommand
import org.pv293.kotlinseminar.productSelectionService.application.commands.impl.CreateShoppingCartWithItemCommand
import org.pv293.kotlinseminar.productSelectionService.application.commands.impl.RemoveBakedGoodFromCartCommand
import org.pv293.kotlinseminar.productSelectionService.application.commands.impl.SetCartItemQuantityCommand
import org.pv293.kotlinseminar.productSelectionService.application.dto.AddCartItemRequestDTO
import org.pv293.kotlinseminar.productSelectionService.application.dto.ShoppingCartDTO
import org.pv293.kotlinseminar.productSelectionService.application.dto.UpdateCartItemRequestDTO
import org.pv293.kotlinseminar.productSelectionService.application.queries.impl.ShoppingCartQuery
import org.pv293.kotlinseminar.productSelectionService.repository.ShoppingCartRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/product-selection/carts")
class ShoppingCartController(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway,
    private val shoppingCartRepository: ShoppingCartRepository,
) {
    private val logger = LoggerFactory.getLogger(ShoppingCartController::class.java)

    @PostMapping("")
    fun createCartWithFirstItem(@RequestBody request: AddCartItemRequestDTO): ShoppingCartDTO {
        logger.info("Creating cart with first item: ${request.bakedGoodsId}")
        val cartId = UUID.randomUUID()

        commandGateway.sendAndWait<UUID>(
            CreateShoppingCartWithItemCommand(
                cartId = cartId,
                bakedGoodsId = request.bakedGoodsId,
                quantity = request.quantity,
            ),
        )

        return queryGateway.query(ShoppingCartQuery(cartId), ShoppingCartDTO::class.java).get()
    }

    @PostMapping("/{cartId}/items")
    fun addItem(
        @PathVariable cartId: String,
        @RequestBody request: AddCartItemRequestDTO,
    ): ShoppingCartDTO {
        val cartUUID = UUID.fromString(cartId)
        logger.info("Adding item ${request.bakedGoodsId} to cart $cartId")

        commandGateway.sendAndWait<UUID>(
            AddBakedGoodToCartCommand(
                cartId = cartUUID,
                bakedGoodsId = request.bakedGoodsId,
                quantity = request.quantity,
            ),
        )

        return queryGateway.query(ShoppingCartQuery(cartUUID), ShoppingCartDTO::class.java).get()
    }

    @PatchMapping("/{cartId}/items/{bakedGoodsId}")
    fun updateItemQuantity(
        @PathVariable cartId: String,
        @PathVariable bakedGoodsId: String,
        @RequestBody request: UpdateCartItemRequestDTO,
    ): ResponseEntity<ShoppingCartDTO> {
        val cartUUID = UUID.fromString(cartId)
        val bakedGoodsUUID = UUID.fromString(bakedGoodsId)
        logger.info("Updating item $bakedGoodsId in cart $cartId")

        if (!shoppingCartRepository.existsById(cartUUID)) {
            throw IllegalArgumentException("Shopping cart with id $cartUUID not found")
        }

        val cart = shoppingCartRepository.findById(cartUUID).get()
        val itemExists = cart.items.any { it.bakedGoodsId == bakedGoodsUUID }
        if (!itemExists) {
            val cartDTO = queryGateway.query(ShoppingCartQuery(cartUUID), ShoppingCartDTO::class.java).get()
            return ResponseEntity.ok(cartDTO)
        }

        val hasQuantity = request.quantity != null
        val hasDelta = request.delta != null
        require(hasQuantity.xor(hasDelta)) { "Provide exactly one of: quantity or delta" }

        if (hasQuantity) {
            val quantity = request.quantity!!
            if (quantity == 0) {
                commandGateway.sendAndWait<UUID>(
                    RemoveBakedGoodFromCartCommand(
                        cartId = cartUUID,
                        bakedGoodsId = bakedGoodsUUID,
                    ),
                )
                return getOrDeleted(cartUUID)
            }

            commandGateway.sendAndWait<UUID>(
                SetCartItemQuantityCommand(
                    cartId = cartUUID,
                    bakedGoodsId = bakedGoodsUUID,
                    quantity = quantity,
                ),
            )
            val cartDTO = queryGateway.query(ShoppingCartQuery(cartUUID), ShoppingCartDTO::class.java).get()
            return ResponseEntity.ok(cartDTO)
        }

        commandGateway.sendAndWait<UUID>(
            AdjustCartItemQuantityCommand(
                cartId = cartUUID,
                bakedGoodsId = bakedGoodsUUID,
                delta = request.delta!!,
            ),
        )

        return getOrDeleted(cartUUID)
    }

    @DeleteMapping("/{cartId}/items/{bakedGoodsId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeItem(
        @PathVariable cartId: String,
        @PathVariable bakedGoodsId: String,
    ) {
        val cartUUID = UUID.fromString(cartId)
        val bakedGoodsUUID = UUID.fromString(bakedGoodsId)
        logger.info("Removing item $bakedGoodsId from cart $cartId")

        if (!shoppingCartRepository.existsById(cartUUID)) return

        commandGateway.sendAndWait<UUID>(
            RemoveBakedGoodFromCartCommand(
                cartId = cartUUID,
                bakedGoodsId = bakedGoodsUUID,
            ),
        )
    }

    @GetMapping("/{cartId}")
    fun getCart(@PathVariable cartId: String): ShoppingCartDTO {
        val cartUUID = UUID.fromString(cartId)
        logger.info("Getting cart $cartId")
        return queryGateway.query(ShoppingCartQuery(cartUUID), ShoppingCartDTO::class.java).get()
    }

    private fun getOrDeleted(cartId: UUID): ResponseEntity<ShoppingCartDTO> {
        if (!shoppingCartRepository.existsById(cartId)) {
            return ResponseEntity.noContent().build()
        }

        val cartDTO = queryGateway.query(ShoppingCartQuery(cartId), ShoppingCartDTO::class.java).get()
        return ResponseEntity.ok(cartDTO)
    }
}
