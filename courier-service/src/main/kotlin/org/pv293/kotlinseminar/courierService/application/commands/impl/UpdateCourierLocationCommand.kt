package org.pv293.kotlinseminar.courierService.application.commands.impl

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.math.BigDecimal
import java.util.UUID

data class UpdateCourierLocationCommand(
    @TargetAggregateIdentifier
    val courierId: UUID,
    val latitude: BigDecimal,
    val longitude: BigDecimal,
)
