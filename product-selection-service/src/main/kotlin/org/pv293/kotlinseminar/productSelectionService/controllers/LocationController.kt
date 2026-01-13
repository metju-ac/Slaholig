package org.pv293.kotlinseminar.productSelectionService.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.pv293.kotlinseminar.productSelectionService.application.commands.impl.ChooseLocationCommand
import org.pv293.kotlinseminar.productSelectionService.application.dto.ChooseLocationRequestDTO
import org.pv293.kotlinseminar.productSelectionService.application.dto.ChooseLocationResponseDTO
import org.pv293.kotlinseminar.productSelectionService.application.dto.BakedGoodDTO
import org.pv293.kotlinseminar.productSelectionService.application.queries.impl.VisibleBakedGoodsQuery
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/product-selection/location")
class LocationController(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway,
) {
    private val logger = LoggerFactory.getLogger(LocationController::class.java)

    @PostMapping("")
    @Operation(summary = "Choose location")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Location chosen"),
        ],
    )
    fun chooseLocation(@RequestBody request: ChooseLocationRequestDTO): ChooseLocationResponseDTO {
        val locationId = UUID.randomUUID()
        logger.info("Choosing location: $locationId")

        commandGateway.sendAndWait<UUID>(
            ChooseLocationCommand(
                locationId = locationId,
                latitude = request.latitude,
                longitude = request.longitude,
            ),
        )

        val availableGoods = queryGateway.query(
            VisibleBakedGoodsQuery(locationId = locationId),
            ResponseTypes.multipleInstancesOf(BakedGoodDTO::class.java),
        ).get()

        return ChooseLocationResponseDTO(
            locationId = locationId,
            latitude = request.latitude,
            longitude = request.longitude,
            availableGoods = availableGoods,
        )
    }
}
