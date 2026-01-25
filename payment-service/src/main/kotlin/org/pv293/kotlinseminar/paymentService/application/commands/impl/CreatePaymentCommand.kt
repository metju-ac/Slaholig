package org.pv293.kotlinseminar.paymentService.application.commands.impl

import org.axonframework.modelling.command.TargetAggregateIdentifier
import org.pv293.kotlinseminar.paymentService.events.impl.OrderItemDTO
import java.util.UUID

data class CreatePaymentCommand(
    @TargetAggregateIdentifier
    val orderId: UUID,
    val cartId: UUID,
    val items: List<OrderItemDTO>,
)
