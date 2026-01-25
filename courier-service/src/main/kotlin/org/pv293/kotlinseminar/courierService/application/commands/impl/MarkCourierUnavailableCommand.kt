package org.pv293.kotlinseminar.courierService.application.commands.impl

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.UUID

data class MarkCourierUnavailableCommand(
    @TargetAggregateIdentifier
    val courierId: UUID,
)
