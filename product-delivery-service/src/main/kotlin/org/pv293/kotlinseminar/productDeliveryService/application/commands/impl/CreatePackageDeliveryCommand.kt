package org.pv293.kotlinseminar.productDeliveryService.application.commands.impl

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.UUID

data class CreatePackageDeliveryCommand(
    @TargetAggregateIdentifier
    val deliveryId: UUID,
    val orderId: UUID,
    val transactionId: String,
)
