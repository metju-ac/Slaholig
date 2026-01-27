package org.pv293.kotlinseminar.courierService.application.commands.impl

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.UUID

data class CancelDeliveryOfferCommand(
    @TargetAggregateIdentifier
    val offerId: UUID,
    val reason: String,
)
