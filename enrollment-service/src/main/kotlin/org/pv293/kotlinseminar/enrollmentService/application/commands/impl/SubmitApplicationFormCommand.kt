package org.pv293.kotlinseminar.enrollmentService.application.commands.impl

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.UUID

data class SubmitApplicationFormCommand(
    @TargetAggregateIdentifier
    val studentId: UUID,
    val name : String,
    val surname : String,
    val email : String,
)