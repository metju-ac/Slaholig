package org.pv293.kotlinseminar.productSelectionService.controllers

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
import java.util.UUID

@RestController
@RequestMapping("/product-selection/dev")
class DevSeedController(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway,
) {
    private val logger = LoggerFactory.getLogger(DevSeedController::class.java)

    @PostMapping("/seed")
    fun seed(): List<BakedGoodDTO> {
        logger.info("Seeding baked goods")

        val goods = listOf(
            SeedGood(
                name = "Sourdough loaf",
                description = "Crusty and tangy",
                initialStock = 12,
                latitude = 49.1951,
                longitude = 16.6068,
            ),
            SeedGood(
                name = "Cinnamon roll",
                description = "Fresh out of the oven",
                initialStock = 30,
                latitude = 49.2000,
                longitude = 16.6100,
            ),
            SeedGood(
                name = "Chocolate croissant",
                description = "Buttery layers",
                initialStock = 20,
                latitude = 48.2082,
                longitude = 16.3738,
            ),
            SeedGood(
                name = "Blueberry muffin",
                description = "Berries and crumble",
                initialStock = 25,
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
    val latitude: Double,
    val longitude: Double,
)
