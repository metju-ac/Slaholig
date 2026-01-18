package org.pv293.kotlinseminar.coursesService.application.dto

import java.util.UUID

class StudentDTO(
    val id: UUID,
    val name: String,
    val surname: String,
    val email: String,
)