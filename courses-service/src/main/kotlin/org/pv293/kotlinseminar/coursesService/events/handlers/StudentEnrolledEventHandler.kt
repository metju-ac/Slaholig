package org.pv293.kotlinseminar.coursesService.events.handlers

import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.gateway.EventGateway
import org.axonframework.queryhandling.QueryGateway
import org.pv293.kotlinseminar.coursesService.application.commands.impl.RegisterStudentCommand
import org.pv293.kotlinseminar.coursesService.application.dto.StudentDTO
import org.pv293.kotlinseminar.coursesService.events.impl.StudentEnrolledEvent
import org.pv293.kotlinseminar.enrollmentService.application.queries.impl.StudentQuery
import org.springframework.stereotype.Component
import java.util.*

@Component
class EnrollStudentEventHandler(
    private val queryGateway: QueryGateway,
    private val commandGateway: CommandGateway,
    private val eventGateway: EventGateway,
) {
    @EventHandler
    fun on(event: StudentEnrolledEvent) {
        val student: StudentDTO = queryGateway.query(StudentQuery(event.studentId), StudentDTO::class.java).get() // Create new aggregate id
        commandGateway.send<RegisterStudentCommand>(RegisterStudentCommand(student.id))
    }
}