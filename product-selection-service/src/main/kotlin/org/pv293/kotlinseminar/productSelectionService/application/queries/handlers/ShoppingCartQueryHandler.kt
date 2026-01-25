package org.pv293.kotlinseminar.productSelectionService.application.queries.handlers

import org.axonframework.queryhandling.QueryHandler
import org.pv293.kotlinseminar.productSelectionService.application.dto.ShoppingCartDTO
import org.pv293.kotlinseminar.productSelectionService.application.dto.ShoppingCartItemDTO
import org.pv293.kotlinseminar.productSelectionService.application.queries.impl.ShoppingCartQuery
import org.pv293.kotlinseminar.productSelectionService.repository.ShoppingCartRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ShoppingCartQueryHandler(
    private val shoppingCartRepository: ShoppingCartRepository,
) {
    private val logger = LoggerFactory.getLogger(ShoppingCartQueryHandler::class.java)

    @QueryHandler
    fun handle(query: ShoppingCartQuery): ShoppingCartDTO {
        val cart = shoppingCartRepository.findById(query.cartId).orElseThrow {
            logger.warn("Could not find shopping cart by id: ${query.cartId}")
            IllegalArgumentException("Shopping cart with id ${query.cartId} not found")
        }

        return ShoppingCartDTO(
            cartId = cart.id,
            items = cart.items
                .sortedBy { it.bakedGoodsId.toString() }
                .map {
                    ShoppingCartItemDTO(
                        bakedGoodsId = it.bakedGoodsId,
                        quantity = it.quantity,
                    )
                },
        )
    }
}
