package org.pv293.kotlinseminar.assignmentService.events.impl

import java.util.UUID

data class AssignmentCreatedEvent(
    val id: UUID,
    val courseId: UUID,
    val title: String,
    val assignmentText: String,
)