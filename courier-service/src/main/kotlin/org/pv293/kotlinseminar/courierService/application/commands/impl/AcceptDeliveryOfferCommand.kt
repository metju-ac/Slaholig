package org.pv293.kotlinseminar.courierService.application.commands.impl

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.UUID

data class AcceptDeliveryOfferCommand(
    @TargetAggregateIdentifier
    val offerId: UUID,
    val courierId: UUID,
)
