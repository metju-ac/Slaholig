package org.pv293.kotlinseminar.assignmentService.application.commands.impl

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.time.Instant
import java.util.UUID

data class HandAssignmentCommand(
    val id: UUID,
    @TargetAggregateIdentifier
    val assignmentId: UUID,
    val studentId: UUID,
    val handoutDate: Instant,
    val deadlineDate: Instant,
)
