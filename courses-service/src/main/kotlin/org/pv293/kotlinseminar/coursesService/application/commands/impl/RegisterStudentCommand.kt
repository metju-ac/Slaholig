package org.pv293.kotlinseminar.coursesService.application.commands.impl

import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.*

data class RegisterStudentCommand(
    @TargetAggregateIdentifier
    val id: UUID,
)