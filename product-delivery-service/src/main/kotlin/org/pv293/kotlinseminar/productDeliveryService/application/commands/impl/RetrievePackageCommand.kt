package org.pv293.kotlinseminar.productDeliveryService.application.commands.impl

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.UUID

data class RetrievePackageCommand(
    @TargetAggregateIdentifier
    val deliveryId: UUID,
)
