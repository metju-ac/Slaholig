package org.pv293.kotlinseminar.enrollmentService.events.impl

import java.util.*

data class AdministrativeAssistantRegisteredEvent (
    val adminId: UUID,
    val name : String,
    val surname : String,
    val email : String,
)