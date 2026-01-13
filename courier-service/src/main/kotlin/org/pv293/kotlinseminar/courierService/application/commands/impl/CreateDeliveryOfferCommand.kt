package org.pv293.kotlinseminar.courierService.application.commands.impl

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class CreateDeliveryOfferCommand(
    @TargetAggregateIdentifier
    val offerId: UUID,
    val deliveryId: UUID,
    val orderId: UUID,
    val courierId: UUID,
    val approximateLatitude: BigDecimal,
    val approximateLongitude: BigDecimal,
    val droppedAt: Instant,
)
