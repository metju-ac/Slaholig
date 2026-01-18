package org.pv293.kotlinseminar.assignmentService.application.commands.impl

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.UUID

data class CreateAssignmentCommand(
    @TargetAggregateIdentifier
    val id: UUID,
    val courseId: UUID,
    val title: String,
    val assignmentText: String,
)
