package org.pv293.kotlinseminar.coursesService.application.queries.handlers

import org.axonframework.queryhandling.QueryGateway
import org.axonframework.queryhandling.QueryHandler
import org.pv293.kotlinseminar.enrollmentService.application.dto.StudentDTO
import org.pv293.kotlinseminar.coursesService.application.queries.impl.CourseStudentsQuery
import org.pv293.kotlinseminar.coursesService.repository.CoursesRepository
import org.pv293.kotlinseminar.enrollmentService.application.queries.impl.StudentQuery
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class CourseStudentsQueryHandler(
    private val coursesRepository: CoursesRepository,
    private val queryGateway: QueryGateway
) {
    private val logger = LoggerFactory.getLogger(CourseStudentsQueryHandler::class.java)
    @QueryHandler
    fun handle(query: CourseStudentsQuery): List<StudentDTO> {
        val course = coursesRepository.findById(query.courseId)
            .orElseThrow {
                logger.error("Cannot find course with id: ${query.courseId}")
                IllegalArgumentException("Course with id ${query.courseId} not found")
            }
        return course.courseRegistrations.map { registration ->
            queryGateway.query(
                StudentQuery(registration.studentId),
                StudentDTO::class.java
            ).get()
        }
    }
}