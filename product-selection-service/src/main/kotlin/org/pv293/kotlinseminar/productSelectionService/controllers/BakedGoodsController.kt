package org.pv293.kotlinseminar.productSelectionService.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.pv293.kotlinseminar.productSelectionService.application.commands.impl.PublishBakedGoodsCommand
import org.pv293.kotlinseminar.productSelectionService.application.commands.impl.RestockBakedGoodsCommand
import org.pv293.kotlinseminar.productSelectionService.application.commands.impl.UpdateBakedGoodPriceCommand
import org.pv293.kotlinseminar.productSelectionService.application.dto.BakedGoodDTO
import org.pv293.kotlinseminar.productSelectionService.application.dto.PublishBakedGoodsRequestDTO
import org.pv293.kotlinseminar.productSelectionService.application.dto.RestockBakedGoodsRequestDTO
import org.pv293.kotlinseminar.productSelectionService.application.dto.UpdateBakedGoodPriceRequestDTO
import org.pv293.kotlinseminar.productSelectionService.application.queries.impl.BakedGoodQuery
import org.pv293.kotlinseminar.productSelectionService.application.queries.impl.BakedGoodsQuery
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestParam
import org.pv293.kotlinseminar.productSelectionService.application.queries.impl.VisibleBakedGoodsQuery
import java.util.UUID

@RestController
@RequestMapping("/product-selection/baked-goods")
class BakedGoodsController(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway,
) {
    private val logger = LoggerFactory.getLogger(BakedGoodsController::class.java)

    @PostMapping("")
    @Operation(summary = "Publish baked goods")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Baked goods published"),
        ],
    )
    fun publishBakedGoods(@RequestBody request: PublishBakedGoodsRequestDTO): BakedGoodDTO {
        logger.info("Publishing baked goods with name: ${request.name}")
        val id = commandGateway.sendAndWait<UUID>(
            PublishBakedGoodsCommand(
                id = UUID.randomUUID(),
                name = request.name,
                description = request.description,
                initialStock = request.initialStock,
                price = request.price,
                latitude = request.latitude,
                longitude = request.longitude,
            ),
        )

        return queryGateway.query(BakedGoodQuery(id), BakedGoodDTO::class.java).get()
    }

    @PatchMapping("/{bakedGoodsId}/restock")
    @Operation(summary = "Restock baked goods")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Baked goods restocked"),
        ],
    )
    fun restockBakedGoods(
        @Parameter(example = "22222222-2222-2222-2222-222222222222")
        @PathVariable bakedGoodsId: String,
        @RequestBody request: RestockBakedGoodsRequestDTO,
    ): BakedGoodDTO {
        logger.info("Restocking baked goods with id: $bakedGoodsId by amount: ${request.amount}")
        val id = UUID.fromString(bakedGoodsId)
        commandGateway.sendAndWait<UUID>(
            RestockBakedGoodsCommand(
                id = id,
                amount = request.amount,
            ),
        )

        return queryGateway.query(BakedGoodQuery(id), BakedGoodDTO::class.java).get()
    }

    @PatchMapping("/{bakedGoodsId}/price")
    @Operation(summary = "Update baked good price")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Price updated successfully"),
        ],
    )
    fun updateBakedGoodPrice(
        @Parameter(example = "22222222-2222-2222-2222-222222222222")
        @PathVariable bakedGoodsId: String,
        @RequestBody request: UpdateBakedGoodPriceRequestDTO,
    ): BakedGoodDTO {
        logger.info("Updating price for baked good with id: $bakedGoodsId to: ${request.newPrice}")
        val id = UUID.fromString(bakedGoodsId)
        commandGateway.sendAndWait<Unit>(
            UpdateBakedGoodPriceCommand(
                bakedGoodsId = id,
                newPrice = request.newPrice,
            ),
        )

        return queryGateway.query(BakedGoodQuery(id), BakedGoodDTO::class.java).get()
    }

    @GetMapping("")
    @Operation(summary = "Get baked goods (optionally filtered by locationId)")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Baked goods returned"),
        ],
    )
    fun getBakedGoods(
        @Parameter(example = "11111111-1111-1111-1111-111111111111")
        @RequestParam(required = false) locationId: String?,
    ): List<BakedGoodDTO> {
        if (locationId == null) {
            logger.info("Getting all baked goods")
            return queryGateway.query(
                BakedGoodsQuery(),
                ResponseTypes.multipleInstancesOf(BakedGoodDTO::class.java),
            ).get()
        }

        logger.info("Getting visible baked goods for locationId: $locationId")
        return queryGateway.query(
            VisibleBakedGoodsQuery(locationId = UUID.fromString(locationId)),
            ResponseTypes.multipleInstancesOf(BakedGoodDTO::class.java),
        ).get()
    }
}
