package org.pv293.kotlinseminar.enrollmentService.application.commands.impl

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.*

data class EnrollStudentCommand(
    @TargetAggregateIdentifier
    val id: UUID,
    val studentID: UUID,
)