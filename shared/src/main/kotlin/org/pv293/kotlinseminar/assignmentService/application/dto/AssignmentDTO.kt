package org.pv293.kotlinseminar.assignmentService.application.dto

import java.util.UUID

data class AssignmentDTO(
    val id: UUID,
    val courseId: UUID,
    val title: String,
    val assignmentText: String,
)
