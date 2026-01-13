package org.pv293.kotlinseminar.productSelectionService.application.commands.impl

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.UUID

data class AddBakedGoodsReviewCommand(
    @TargetAggregateIdentifier
    val bakedGoodsId: UUID,
    val reviewId: UUID,
    val authorId: UUID,
    val rating: Int,
    val content: String?,
)
