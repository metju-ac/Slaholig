package org.pv293.kotlinseminar.enrollmentService.repository

import org.pv293.kotlinseminar.enrollmentService.application.aggregates.Student
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface StudentRepository: JpaRepository<Student, UUID> {
}