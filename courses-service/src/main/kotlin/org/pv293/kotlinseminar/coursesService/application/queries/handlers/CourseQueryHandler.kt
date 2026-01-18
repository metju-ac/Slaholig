package org.pv293.kotlinseminar.coursesService.application.queries.handlers

import org.axonframework.queryhandling.QueryHandler
import org.pv293.kotlinseminar.coursesService.application.dto.CourseDTO
import org.pv293.kotlinseminar.coursesService.application.queries.impl.CourseQuery
import org.pv293.kotlinseminar.coursesService.repository.CoursesRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class CourseQueryHandler(
    private val coursesRepository: CoursesRepository
) {
    private val logger = LoggerFactory.getLogger(CourseQueryHandler::class.java)
    @QueryHandler
    fun handle(query: CourseQuery): CourseDTO {
        val courses = coursesRepository.findById(query.id).orElseThrow {
            logger.warn("Could not find courses by id: ${query.id}")
            IllegalArgumentException("Course with id ${query.id} not found")
        }
        return CourseDTO(
            id = courses.id,
            title = courses.title,
            description = courses.description,
            capacity = courses.capacity
        )
    }
}