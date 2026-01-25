package org.pv293.kotlinseminar.productSelectionService.application.queries.handlers

import org.axonframework.queryhandling.QueryHandler
import org.pv293.kotlinseminar.productSelectionService.application.dto.BakedGoodDTO
import org.pv293.kotlinseminar.productSelectionService.application.queries.impl.BakedGoodsQuery
import org.pv293.kotlinseminar.productSelectionService.repository.BakedGoodRepository
import org.springframework.stereotype.Component

@Component
class BakedGoodsQueryHandler(
    private val bakedGoodRepository: BakedGoodRepository,
) {
    @QueryHandler
    fun handle(query: BakedGoodsQuery): List<BakedGoodDTO> {
        return bakedGoodRepository.findAll().map {
            BakedGoodDTO(
                id = it.id,
                name = it.name,
                description = it.description,
                stock = it.stock,
                latitude = it.latitude,
                longitude = it.longitude,
            )
        }
    }
}
