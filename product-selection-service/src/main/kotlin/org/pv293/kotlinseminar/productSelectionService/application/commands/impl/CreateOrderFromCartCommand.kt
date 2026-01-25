package org.pv293.kotlinseminar.productSelectionService.application.commands.impl

import org.axonframework.modelling.command.TargetAggregateIdentifier
import org.pv293.kotlinseminar.paymentService.events.impl.OrderItemDTO
import java.util.UUID

data class CreateOrderFromCartCommand(
    @TargetAggregateIdentifier
    val cartId: UUID,
    val orderId: UUID,
    val items: List<OrderItemDTO>,
)
