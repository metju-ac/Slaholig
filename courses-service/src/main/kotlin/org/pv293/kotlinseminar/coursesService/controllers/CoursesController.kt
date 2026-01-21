package org.pv293.kotlinseminar.coursesService.controllers

import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.extensions.kotlin.query
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.pv293.kotlinseminar.coursesService.application.commands.impl.CreateCourseCommand
import org.pv293.kotlinseminar.coursesService.application.commands.impl.RegisterStudentToCourseCommand
import org.pv293.kotlinseminar.coursesService.application.dto.CourseDTO
import org.pv293.kotlinseminar.coursesService.application.dto.CreateCourseDTO
import org.pv293.kotlinseminar.enrollmentService.application.dto.StudentDTO
import org.pv293.kotlinseminar.coursesService.application.queries.impl.CourseQuery
import org.pv293.kotlinseminar.coursesService.application.queries.impl.CourseStudentsQuery
import org.pv293.kotlinseminar.enrollmentService.application.queries.impl.StudentQuery
import org.pv293.kotlinseminar.exceptions.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.data.crossstore.ChangeSetPersister
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*
import kotlin.jvm.java

@RestController
@RequestMapping("/courses")
class CoursesController(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway
) {
    private val logger = LoggerFactory.getLogger(CoursesController::class.java)

    @PostMapping("/")
    fun createCourse(@RequestBody course: CreateCourseDTO): CourseDTO {
        logger.info("Creating course with title: ${course.title}, description: ${course.description}, capacity: ${course.capacity}")
        val id: UUID = commandGateway.sendAndWait(
            CreateCourseCommand(
                id = UUID.randomUUID(),
                title = course.title,
                description = course.description,
                capacity = course.capacity,
            )
        )
        return queryGateway.query(CourseQuery(id), CourseDTO::class.java).get()

    }

    @PostMapping("/{courseId}/register/{studentId}")
    fun registerStudent(@PathVariable courseId: String, @PathVariable studentId: String) {
        val studentId: UUID = UUID.fromString(studentId)
        try {
            queryGateway.query(StudentQuery(studentId), StudentDTO::class.java).get()
        } catch (e: NotFoundException) {
            logger.error("Student with id $studentId not found")
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Student with id $studentId not found")
        }


        commandGateway.sendAndWait<UUID>(
            RegisterStudentToCourseCommand(
                courseId = UUID.fromString(courseId),
                studentId = studentId
            )
        )
    }

    @GetMapping("/{courseId}/students")
    fun getCourseStudents(@PathVariable courseId: String): List<StudentDTO> {
        return queryGateway.query(
            CourseStudentsQuery(UUID.fromString(courseId)), ResponseTypes.multipleInstancesOf(
            StudentDTO::class.java)).get()
    }

}