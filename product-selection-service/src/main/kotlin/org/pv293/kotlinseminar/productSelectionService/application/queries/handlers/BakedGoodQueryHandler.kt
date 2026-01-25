package org.pv293.kotlinseminar.productSelectionService.application.queries.handlers

import org.axonframework.queryhandling.QueryHandler
import org.pv293.kotlinseminar.productSelectionService.application.dto.BakedGoodDTO
import org.pv293.kotlinseminar.productSelectionService.application.queries.impl.BakedGoodQuery
import org.pv293.kotlinseminar.productSelectionService.repository.BakedGoodRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class BakedGoodQueryHandler(
    private val bakedGoodRepository: BakedGoodRepository,
) {
    private val logger = LoggerFactory.getLogger(BakedGoodQueryHandler::class.java)

    @QueryHandler
    fun handle(query: BakedGoodQuery): BakedGoodDTO {
        val bakedGood = bakedGoodRepository.findById(query.id).orElseThrow {
            logger.warn("Could not find baked good by id: ${query.id}")
            IllegalArgumentException("Baked good with id ${query.id} not found")
        }

        return BakedGoodDTO(
            id = bakedGood.id,
            name = bakedGood.name,
            description = bakedGood.description,
            stock = bakedGood.stock,
            latitude = bakedGood.latitude,
            longitude = bakedGood.longitude,
        )
    }
}
