package org.pv293.kotlinseminar.productSelectionService.controllers

import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.pv293.kotlinseminar.productSelectionService.application.commands.impl.PublishBakedGoodsCommand
import org.pv293.kotlinseminar.productSelectionService.application.commands.impl.RestockBakedGoodsCommand
import org.pv293.kotlinseminar.productSelectionService.application.dto.BakedGoodDTO
import org.pv293.kotlinseminar.productSelectionService.application.dto.PublishBakedGoodsRequestDTO
import org.pv293.kotlinseminar.productSelectionService.application.dto.RestockBakedGoodsRequestDTO
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
class ProductSelectionController(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway,
) {
    private val logger = LoggerFactory.getLogger(ProductSelectionController::class.java)

    @PostMapping("")
    fun publishBakedGoods(@RequestBody request: PublishBakedGoodsRequestDTO): BakedGoodDTO {
        logger.info("Publishing baked goods with name: ${request.name}")
        val id = commandGateway.sendAndWait<UUID>(
            PublishBakedGoodsCommand(
                id = UUID.randomUUID(),
                name = request.name,
                description = request.description,
                initialStock = request.initialStock,
                latitude = request.latitude,
                longitude = request.longitude,
            ),
        )

        return queryGateway.query(BakedGoodQuery(id), BakedGoodDTO::class.java).get()
    }

    @PatchMapping("/{bakedGoodsId}/restock")
    fun restockBakedGoods(
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

    @GetMapping("")
    fun getBakedGoods(@RequestParam(required = false) locationId: String?): List<BakedGoodDTO> {
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
