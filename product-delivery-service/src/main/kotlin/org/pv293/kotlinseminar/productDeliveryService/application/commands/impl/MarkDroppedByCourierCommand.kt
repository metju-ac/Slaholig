package org.pv293.kotlinseminar.productDeliveryService.application.commands.impl

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.math.BigDecimal
import java.util.UUID

data class MarkDroppedByCourierCommand(
    @TargetAggregateIdentifier
    val deliveryId: UUID,
    val courierId: UUID,
    val latitude: BigDecimal,
    val longitude: BigDecimal,
    val photoUrl: String,
)
