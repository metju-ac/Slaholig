package org.pv293.kotlinseminar.productSelectionService.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.pv293.kotlinseminar.productSelectionService.application.commands.impl.PublishBakedGoodsCommand
import org.pv293.kotlinseminar.productSelectionService.application.dto.BakedGoodDTO
import org.pv293.kotlinseminar.productSelectionService.application.queries.impl.BakedGoodQuery
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.util.UUID

@RestController
@RequestMapping("/product-selection/dev")
class DevSeedController(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway,
) {
    private val logger = LoggerFactory.getLogger(DevSeedController::class.java)

    @PostMapping("/seed")
    @Operation(summary = "Seed baked goods")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Seeded goods returned"),
        ],
    )
    fun seed(): List<BakedGoodDTO> {
        logger.info("Seeding baked goods")

        val goods = listOf(
            SeedGood(
                name = "Sourdough loaf",
                description = "Crusty and tangy",
                initialStock = 12,
                price = BigDecimal("4.99"),
                latitude = 49.1951,
                longitude = 16.6068,
            ),
            SeedGood(
                name = "Cinnamon roll",
                description = "Fresh out of the oven",
                initialStock = 30,
                price = BigDecimal("2.49"),
                latitude = 49.2000,
                longitude = 16.6100,
            ),
            SeedGood(
                name = "Chocolate croissant",
                description = "Buttery layers",
                initialStock = 20,
                price = BigDecimal("3.99"),
                latitude = 48.2082,
                longitude = 16.3738,
            ),
            SeedGood(
                name = "Blueberry muffin",
                description = "Berries and crumble",
                initialStock = 25,
                price = BigDecimal("2.99"),
                latitude = 50.0755,
                longitude = 14.4378,
            ),
        )

        val ids = goods.map { seed ->
            commandGateway.sendAndWait<UUID>(
                PublishBakedGoodsCommand(
                    id = UUID.randomUUID(),
                    name = seed.name,
                    description = seed.description,
                    initialStock = seed.initialStock,
                    price = seed.price,
                    latitude = seed.latitude,
                    longitude = seed.longitude,
                ),
            )
        }

        return ids.map { id ->
            queryGateway.query(
                BakedGoodQuery(id),
                ResponseTypes.instanceOf(BakedGoodDTO::class.java),
            ).get()
        }
    }
}

private data class SeedGood(
    val name: String,
    val description: String?,
    val initialStock: Int,
    val price: BigDecimal,
    val latitude: Double,
    val longitude: Double,
)
