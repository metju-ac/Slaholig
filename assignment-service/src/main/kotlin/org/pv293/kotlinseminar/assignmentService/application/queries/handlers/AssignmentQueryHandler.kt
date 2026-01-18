package org.pv293.kotlinseminar.assignmentService.application.queries.handlers

import org.pv293.kotlinseminar.assignmentService.application.dto.AssignmentDTO
import org.pv293.kotlinseminar.assignmentService.application.queries.impl.AssignmentQuery
import org.pv293.kotlinseminar.assignmentService.repository.AssignmentsRepository
import org.pv293.kotlinseminar.exceptions.NotFoundException
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Component


@Component
class AssignmentQueryHandler(
    private val assignmentRepository: AssignmentsRepository
) {
    @Query
    fun handle(query: AssignmentQuery): AssignmentDTO {
        val assignment = assignmentRepository.findById(query.id).orElseThrow {
            throw NotFoundException()
        }
        return AssignmentDTO(
            id = assignment.id,
            courseId = assignment.courseId,
            title = assignment.title,
            assignmentText = assignment.assignmentText,
        )
    }
}