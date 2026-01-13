package org.pv293.kotlinseminar.productSelectionService.application.commands.impl

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.math.BigDecimal
import java.util.UUID

data class ChooseLocationCommand(
    @TargetAggregateIdentifier
    val locationId: UUID,
    val latitude: BigDecimal,
    val longitude: BigDecimal,
)
