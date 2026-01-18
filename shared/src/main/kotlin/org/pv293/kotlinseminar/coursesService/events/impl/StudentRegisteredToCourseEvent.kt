package org.pv293.kotlinseminar.coursesService.events.impl

import java.util.UUID

data class StudentRegisteredToCourseEvent(
    val courseId: UUID,
    val studentId: UUID,
)
