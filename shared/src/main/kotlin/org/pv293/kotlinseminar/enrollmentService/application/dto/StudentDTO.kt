package org.pv293.kotlinseminar.enrollmentService.application.dto

import java.util.UUID


data class StudentDTO(
    val id: UUID,
    val name: String,
    val surname: String,
    val email: String,
    val enrolled: Boolean,
)