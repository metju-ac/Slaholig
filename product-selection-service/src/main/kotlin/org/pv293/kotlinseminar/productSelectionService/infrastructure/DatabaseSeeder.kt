package org.pv293.kotlinseminar.productSelectionService.infrastructure

import org.axonframework.commandhandling.gateway.CommandGateway
import org.pv293.kotlinseminar.productSelectionService.application.commands.impl.PublishBakedGoodsCommand
import org.pv293.kotlinseminar.productSelectionService.repository.BakedGoodRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.UUID

@Component
class DatabaseSeeder(
    private val bakedGoodRepository: BakedGoodRepository,
    private val commandGateway: CommandGateway,
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(DatabaseSeeder::class.java)

    override fun run(args: ApplicationArguments?) {
        val existingCount = bakedGoodRepository.count()
        
        if (existingCount > 0) {
            logger.info("Database already contains {} baked goods. Skipping seed.", existingCount)
            return
        }

        logger.info("Database is empty. Starting seed process...")

        val goods = listOf(
            SeedGood(
                name = "Sourdough loaf",
                description = "Crusty and tangy",
                initialStock = 12,
                price = BigDecimal("4.99"),
                latitude = BigDecimal("49.1951"),
                longitude = BigDecimal("16.6068"),
            ),
            SeedGood(
                name = "Cinnamon roll",
                description = "Fresh out of the oven",
                initialStock = 30,
                price = BigDecimal("2.49"),
                latitude = BigDecimal("49.2000"),
                longitude = BigDecimal("16.6100"),
            ),
            SeedGood(
                name = "Chocolate croissant",
                description = "Buttery layers",
                initialStock = 20,
                price = BigDecimal("3.99"),
                latitude = BigDecimal("48.2082"),
                longitude = BigDecimal("16.3738"),
            ),
            SeedGood(
                name = "Blueberry muffin",
                description = "Berries and crumble",
                initialStock = 25,
                price = BigDecimal("2.99"),
                latitude = BigDecimal("50.0755"),
                longitude = BigDecimal("14.4378"),
            ),
        )

        try {
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
            logger.info("Successfully seeded {} baked goods with IDs: {}", ids.size, ids)
        } catch (e: Exception) {
            logger.error("Failed to seed database", e)
            throw e
        }
    }
}

private data class SeedGood(
    val name: String,
    val description: String?,
    val initialStock: Int,
    val price: BigDecimal,
    val latitude: BigDecimal,
    val longitude: BigDecimal,
)
