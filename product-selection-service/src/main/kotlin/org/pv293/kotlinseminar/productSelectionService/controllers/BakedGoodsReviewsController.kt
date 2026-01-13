package org.pv293.kotlinseminar.productSelectionService.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.pv293.kotlinseminar.productSelectionService.application.commands.impl.AddBakedGoodsReviewCommand
import org.pv293.kotlinseminar.productSelectionService.application.dto.AddBakedGoodsReviewRequestDTO
import org.pv293.kotlinseminar.productSelectionService.application.dto.BakedGoodReviewDTO
import org.pv293.kotlinseminar.productSelectionService.application.queries.impl.BakedGoodsReviewsQuery
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/product-selection/baked-goods/{bakedGoodsId}/reviews")
class BakedGoodsReviewsController(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway,
) {
    private val logger = LoggerFactory.getLogger(BakedGoodsReviewsController::class.java)

    @PostMapping("")
    @Operation(summary = "Add review to baked goods")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Review added"),
        ],
    )
    fun addReview(
        @Parameter(example = "22222222-2222-2222-2222-222222222222")
        @PathVariable bakedGoodsId: String,
        @RequestBody request: AddBakedGoodsReviewRequestDTO,
    ): BakedGoodReviewDTO {
        val bakedGoodsUUID = UUID.fromString(bakedGoodsId)
        logger.info("Adding review to baked goods with id: $bakedGoodsId")

        val reviewId = UUID.randomUUID()
        commandGateway.sendAndWait<UUID>(
            AddBakedGoodsReviewCommand(
                bakedGoodsId = bakedGoodsUUID,
                reviewId = reviewId,
                authorId = request.authorId,
                rating = request.rating,
                content = request.content,
            ),
        )

        val reviews: List<BakedGoodReviewDTO> = queryGateway.query(
            BakedGoodsReviewsQuery(bakedGoodsUUID),
            ResponseTypes.multipleInstancesOf(BakedGoodReviewDTO::class.java),
        ).get()

        return reviews.first { it.id == reviewId }
    }

    @GetMapping("")
    @Operation(summary = "Get baked goods reviews")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Reviews returned"),
        ],
    )
    fun getReviews(
        @Parameter(example = "22222222-2222-2222-2222-222222222222")
        @PathVariable bakedGoodsId: String,
    ): List<BakedGoodReviewDTO> {
        val bakedGoodsUUID = UUID.fromString(bakedGoodsId)
        logger.info("Getting reviews for baked goods with id: $bakedGoodsId")

        return queryGateway.query(
            BakedGoodsReviewsQuery(bakedGoodsUUID),
            ResponseTypes.multipleInstancesOf(BakedGoodReviewDTO::class.java),
        ).get()
    }
}
