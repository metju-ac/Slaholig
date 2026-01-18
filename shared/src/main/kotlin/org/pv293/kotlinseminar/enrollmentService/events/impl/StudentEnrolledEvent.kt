package org.pv293.kotlinseminar.enrollmentService.events.impl

import java.util.UUID


data class StudentEnrolledEvent(
    val studentId: UUID
)