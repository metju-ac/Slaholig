package org.pv293.kotlinseminar.assignmentService.events.impl

import java.time.Instant
import java.util.UUID

data class AssignmentHandedOutEvent(
    val id: UUID,
    val assignmentId: UUID,
    val studentId: UUID,
    val handoutDate: Instant,
    val deadlineDate: Instant,
)
