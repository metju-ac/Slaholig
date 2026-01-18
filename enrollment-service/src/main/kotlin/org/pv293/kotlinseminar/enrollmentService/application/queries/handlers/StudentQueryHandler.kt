package org.pv293.kotlinseminar.enrollmentService.application.queries.handlers

import org.axonframework.queryhandling.QueryHandler
import org.pv293.kotlinseminar.enrollmentService.application.aggregates.Student
import org.pv293.kotlinseminar.enrollmentService.repository.StudentRepository
import org.pv293.kotlinseminar.enrollmentService.application.dto.StudentDTO
import org.pv293.kotlinseminar.enrollmentService.application.queries.impl.StudentQuery
import org.pv293.kotlinseminar.exceptions.NotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class StudentQueryHandler (
    private val studentRepository: StudentRepository
) {
    @QueryHandler
    fun handle(query: StudentQuery): StudentDTO {
        val student: Student = studentRepository.findById(query.id).orElseThrow(::NotFoundException)
        return StudentDTO(
            id = student.id,
            name = student.name,
            surname = student.surname,
            email = student.email,
            enrolled = student.enrolled,
        )
    }
}