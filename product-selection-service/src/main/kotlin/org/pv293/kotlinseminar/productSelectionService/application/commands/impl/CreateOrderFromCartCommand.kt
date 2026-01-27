package org.pv293.kotlinseminar.productSelectionService.application.commands.impl

import org.axonframework.modelling.command.TargetAggregateIdentifier
import org.pv293.kotlinseminar.paymentService.events.impl.OrderItemDTO
import java.math.BigDecimal
import java.util.UUID

data class CreateOrderFromCartCommand(
    @TargetAggregateIdentifier
    val cartId: UUID,
    val orderId: UUID,
    val items: List<OrderItemDTO>,
    val customerLatitude: BigDecimal?,
    val customerLongitude: BigDecimal?,
)
