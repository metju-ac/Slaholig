package org.pv293.kotlinseminar.coursesService.events.impl

import java.util.UUID

data class CourseCreatedEvent(
    val courseId: UUID,
    val title: String,
    val description: String,
    val capacity: Int,
)