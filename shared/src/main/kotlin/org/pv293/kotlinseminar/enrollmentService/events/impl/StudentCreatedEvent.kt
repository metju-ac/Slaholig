package org.pv293.kotlinseminar.enrollmentService.events.impl

import java.util.*

data class StudentCreatedEvent(

    val studentId: UUID,
    val name : String,
    val surname : String,
    val email : String,
)