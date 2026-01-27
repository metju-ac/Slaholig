package org.pv293.kotlinseminar.productDeliveryService.application.commands.impl

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.UUID

data class AssignCourierCommand(
    @TargetAggregateIdentifier
    val deliveryId: UUID,
    val courierId: UUID,
    val offerId: UUID,
)
