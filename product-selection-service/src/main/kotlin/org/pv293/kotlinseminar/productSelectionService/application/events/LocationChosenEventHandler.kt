package org.pv293.kotlinseminar.productSelectionService.application.events

import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.gateway.EventGateway
import org.pv293.kotlinseminar.productSelectionService.application.geo.GeoDistance
import org.pv293.kotlinseminar.productSelectionService.events.impl.AvailableBakedGoodsReturnedEvent
import org.pv293.kotlinseminar.productSelectionService.events.impl.LocationChosenEvent
import org.pv293.kotlinseminar.productSelectionService.repository.BakedGoodRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class LocationChosenEventHandler(
    private val bakedGoodRepository: BakedGoodRepository,
    private val eventGateway: EventGateway,
) {
    private val logger = LoggerFactory.getLogger(LocationChosenEventHandler::class.java)

    @EventHandler
    fun on(event: LocationChosenEvent) {
        val radiusKm = 100.0

        val availableGoodsIds = bakedGoodRepository.findAll()
            .filter {
                GeoDistance.distanceKm(
                    lat1 = event.latitude,
                    lon1 = event.longitude,
                    lat2 = it.latitude,
                    lon2 = it.longitude,
                ) <= radiusKm
            }
            .map { it.id }

        logger.info(
            "Location chosen ${event.locationId}; computed ${availableGoodsIds.size} goods within ${radiusKm}km",
        )

        eventGateway.publish(
            AvailableBakedGoodsReturnedEvent(
                locationId = event.locationId,
                radiusKm = radiusKm,
                bakedGoodsIds = availableGoodsIds,
            ),
        )
    }
}
