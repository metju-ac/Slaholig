package org.pv293.kotlinseminar.paymentService.application.commands.impl

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.UUID

data class PayOrderCommand(
    @TargetAggregateIdentifier
    val orderId: UUID,
)
