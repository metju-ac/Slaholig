package org.pv293.kotlinseminar.coursesService.application.dto

import java.util.UUID

data class CourseDTO(
    val id: UUID,
    val title: String,
    val description: String,
    val capacity: Int,
)