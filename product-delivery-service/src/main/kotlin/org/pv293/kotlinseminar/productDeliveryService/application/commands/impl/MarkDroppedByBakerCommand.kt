package org.pv293.kotlinseminar.productDeliveryService.application.commands.impl

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.math.BigDecimal
import java.util.UUID

data class MarkDroppedByBakerCommand(
    @TargetAggregateIdentifier
    val deliveryId: UUID,
    val latitude: BigDecimal,
    val longitude: BigDecimal,
    val photoUrl: String,
)
