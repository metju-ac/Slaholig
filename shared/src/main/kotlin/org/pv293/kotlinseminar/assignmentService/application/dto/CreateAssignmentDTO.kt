package org.pv293.kotlinseminar.assignmentService.application.dto

import java.util.UUID

data class CreateAssignmentDTO(
    val courseId: UUID,
    val title: String,
    val assignmentText: String,
)
