package org.pv293.kotlinseminar.productSelectionService.application.commands.impl

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.math.BigDecimal
import java.util.UUID

data class PublishBakedGoodsCommand(
    @TargetAggregateIdentifier
    val id: UUID,
    val name: String,
    val description: String?,
    val initialStock: Int,
    val price: BigDecimal,
    val latitude: BigDecimal,
    val longitude: BigDecimal,
)
