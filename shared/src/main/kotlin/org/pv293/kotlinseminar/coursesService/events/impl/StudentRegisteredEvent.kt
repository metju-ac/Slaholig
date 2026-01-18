package org.pv293.kotlinseminar.coursesService.events.impl


import java.util.*

data class StudentRegisteredEvent (
    val studentId: UUID,
    val name : String,
    val surname : String,
    val email : String,
)