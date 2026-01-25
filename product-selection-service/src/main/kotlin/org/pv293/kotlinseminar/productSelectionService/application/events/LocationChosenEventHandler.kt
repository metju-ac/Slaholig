package org.pv293.kotlinseminar.productSelectionService.application.events

import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.EventHandler
import org.pv293.kotlinseminar.productSelectionService.application.commands.impl.ShowAvailableBakedGoodsCommand
import org.pv293.kotlinseminar.productSelectionService.events.impl.LocationChosenEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class LocationChosenEventHandler(
    private val commandGateway: CommandGateway,
) {
    private val logger = LoggerFactory.getLogger(LocationChosenEventHandler::class.java)

    @EventHandler
    fun on(event: LocationChosenEvent) {
        logger.info("Location chosen ${event.locationId}; issuing ShowAvailableBakedGoodsCommand")
        commandGateway.send<Any>(ShowAvailableBakedGoodsCommand(locationId = event.locationId))
    }
}
