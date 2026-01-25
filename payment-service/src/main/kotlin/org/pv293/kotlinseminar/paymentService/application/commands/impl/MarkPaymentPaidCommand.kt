package org.pv293.kotlinseminar.paymentService.application.commands.impl

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.UUID

data class MarkPaymentPaidCommand(
    @TargetAggregateIdentifier
    val orderId: UUID,
    val transactionId: String,
)
