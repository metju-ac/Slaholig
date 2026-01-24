package org.pv293.kotlinseminar.productSelectionService.application.commands.handlers

import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventhandling.gateway.EventGateway
import org.pv293.kotlinseminar.productSelectionService.application.commands.impl.ShowAvailableBakedGoodsCommand
import org.pv293.kotlinseminar.productSelectionService.application.geo.GeoDistance
import org.pv293.kotlinseminar.productSelectionService.events.impl.AvailableBakedGoodsShownEvent
import org.pv293.kotlinseminar.productSelectionService.repository.BakedGoodRepository
import org.pv293.kotlinseminar.productSelectionService.repository.ChosenLocationRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ShowAvailableBakedGoodsCommandHandler(
    private val chosenLocationRepository: ChosenLocationRepository,
    private val bakedGoodRepository: BakedGoodRepository,
    private val eventGateway: EventGateway,
) {
    private val logger = LoggerFactory.getLogger(ShowAvailableBakedGoodsCommandHandler::class.java)

    @CommandHandler
    fun handle(command: ShowAvailableBakedGoodsCommand) {
        val location = chosenLocationRepository.findById(command.locationId).orElse(null)
        if (location == null) {
            logger.warn("Could not show available goods; location not found: ${command.locationId}")
            return
        }

        val availableGoodsIds = bakedGoodRepository.findAll()
            .filter {
                GeoDistance.distanceKm(
                    lat1 = location.latitude,
                    lon1 = location.longitude,
                    lat2 = it.latitude,
                    lon2 = it.longitude,
                ) <= command.radiusKm
            }
            .map { it.id }

        logger.info(
            "Showing ${availableGoodsIds.size} goods within ${command.radiusKm}km for location ${command.locationId}",
        )

        eventGateway.publish(
            AvailableBakedGoodsShownEvent(
                locationId = command.locationId,
                radiusKm = command.radiusKm,
                bakedGoodsIds = availableGoodsIds,
            ),
        )
    }
}
