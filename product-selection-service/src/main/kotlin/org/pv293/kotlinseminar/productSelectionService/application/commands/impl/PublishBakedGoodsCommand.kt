package org.pv293.kotlinseminar.productSelectionService.application.commands.impl

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.UUID

data class PublishBakedGoodsCommand(
    @TargetAggregateIdentifier
    val id: UUID,
    val name: String,
    val description: String?,
    val initialStock: Int,
    val latitude: Double,
    val longitude: Double,
)
