package org.pv293.kotlinseminar.productSelectionService.application.commands.impl

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.UUID

data class AddBakedGoodToCartCommand(
    @TargetAggregateIdentifier
    val cartId: UUID,
    val bakedGoodsId: UUID,
    val quantity: Int,
)
