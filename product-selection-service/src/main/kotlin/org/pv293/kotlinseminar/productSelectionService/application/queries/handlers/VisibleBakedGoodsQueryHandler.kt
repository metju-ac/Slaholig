package org.pv293.kotlinseminar.productSelectionService.application.queries.handlers

import org.axonframework.queryhandling.QueryHandler
import org.pv293.kotlinseminar.productSelectionService.application.dto.BakedGoodDTO
import org.pv293.kotlinseminar.productSelectionService.application.geo.GeoDistance
import org.pv293.kotlinseminar.productSelectionService.application.queries.impl.VisibleBakedGoodsQuery
import org.pv293.kotlinseminar.productSelectionService.repository.BakedGoodRepository
import org.pv293.kotlinseminar.productSelectionService.repository.ChosenLocationRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class VisibleBakedGoodsQueryHandler(
    private val bakedGoodRepository: BakedGoodRepository,
    private val chosenLocationRepository: ChosenLocationRepository,
) {
    private val logger = LoggerFactory.getLogger(VisibleBakedGoodsQueryHandler::class.java)

    @QueryHandler
    fun handle(query: VisibleBakedGoodsQuery): List<BakedGoodDTO> {
        val location = chosenLocationRepository.findById(query.locationId).orElseThrow {
            logger.warn("Could not find chosen location by id: ${query.locationId}")
            IllegalArgumentException("Chosen location with id ${query.locationId} not found")
        }

        val radiusKm = 100.0

        return bakedGoodRepository.findAll()
            .filter {
                GeoDistance.distanceKm(
                    lat1 = location.latitude,
                    lon1 = location.longitude,
                    lat2 = it.latitude,
                    lon2 = it.longitude,
                ) <= radiusKm
            }
            .map {
                BakedGoodDTO(
                    id = it.id,
                    name = it.name,
                    description = it.description,
                    stock = it.stock,
                    price = it.price,
                    latitude = it.latitude,
                    longitude = it.longitude,
                )
            }
    }
}
