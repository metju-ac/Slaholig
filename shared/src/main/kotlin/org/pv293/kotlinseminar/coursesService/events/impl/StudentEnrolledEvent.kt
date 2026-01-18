package org.pv293.kotlinseminar.coursesService.events.impl

import java.util.*

data class StudentEnrolledEvent(
    val studentId: UUID
)