package org.pv293.kotlinseminar.assignmentService.repository

import org.pv293.kotlinseminar.assignmentService.application.aggregates.Assignment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AssignmentsRepository: JpaRepository<Assignment, UUID> {
}