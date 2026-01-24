package org.pv293.kotlinseminar.productSelectionService.controllers

import org.axonframework.commandhandling.gateway.CommandGateway
import org.pv293.kotlinseminar.productSelectionService.application.commands.impl.ChooseLocationCommand
import org.pv293.kotlinseminar.productSelectionService.application.dto.ChooseLocationRequestDTO
import org.pv293.kotlinseminar.productSelectionService.application.dto.ChosenLocationDTO
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
) {
    private val logger = LoggerFactory.getLogger(LocationController::class.java)

    @PostMapping("")
    fun chooseLocation(@RequestBody request: ChooseLocationRequestDTO): ChosenLocationDTO {
        val locationId = UUID.randomUUID()
        logger.info("Choosing location: $locationId")

        commandGateway.sendAndWait<UUID>(
            ChooseLocationCommand(
                locationId = locationId,
                latitude = request.latitude,
                longitude = request.longitude,
            ),
        )

        return ChosenLocationDTO(
            locationId = locationId,
            latitude = request.latitude,
            longitude = request.longitude,
        )
    }
}
