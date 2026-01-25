package org.pv293.kotlinseminar.productSelectionService.application.queries.handlers

import org.axonframework.queryhandling.QueryHandler
import org.pv293.kotlinseminar.productSelectionService.application.dto.BakedGoodReviewDTO
import org.pv293.kotlinseminar.productSelectionService.application.queries.impl.BakedGoodsReviewsQuery
import org.pv293.kotlinseminar.productSelectionService.repository.BakedGoodRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class BakedGoodsReviewsQueryHandler(
    private val bakedGoodRepository: BakedGoodRepository,
) {
    private val logger = LoggerFactory.getLogger(BakedGoodsReviewsQueryHandler::class.java)

    @QueryHandler
    fun handle(query: BakedGoodsReviewsQuery): List<BakedGoodReviewDTO> {
        val bakedGood = bakedGoodRepository.findById(query.bakedGoodsId).orElseThrow {
            logger.warn("Could not find baked good by id: ${query.bakedGoodsId}")
            IllegalArgumentException("Baked good with id ${query.bakedGoodsId} not found")
        }

        return bakedGood.reviews.map {
            BakedGoodReviewDTO(
                id = it.id,
                authorId = it.authorId,
                rating = it.rating,
                content = it.content,
            )
        }
    }
}
