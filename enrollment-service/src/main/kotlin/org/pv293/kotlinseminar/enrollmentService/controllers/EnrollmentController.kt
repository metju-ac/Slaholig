package org.pv293.kotlinseminar.enrollmentService.controllers

import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.queryhandling.QueryGateway
import org.pv293.kotlinseminar.enrollmentService.application.commands.impl.EnrollStudentCommand
import org.pv293.kotlinseminar.enrollmentService.application.commands.impl.RegisterAdministrativeAssistantCommand
import org.pv293.kotlinseminar.enrollmentService.application.commands.impl.SubmitApplicationFormCommand
import org.pv293.kotlinseminar.enrollmentService.application.dto.ApplicationDTO
import org.pv293.kotlinseminar.enrollmentService.application.queries.impl.StudentQuery
import org.pv293.kotlinseminar.enrollmentService.application.dto.RegisterAdministrativeAssistantDTO
import org.pv293.kotlinseminar.enrollmentService.application.dto.StudentDTO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.util.*


@RestController
@RequestMapping("/enrollment")
class EnrollmentController (
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway
) {
    private val logger = LoggerFactory.getLogger(EnrollmentController::class.java)

    @PostMapping("/apply")
    fun submitApplication(@RequestBody application: ApplicationDTO): StudentDTO {
        logger.info("Submitting application for ${application.name} ${application.surname} with email ${application.email}")
        val studentResult: UUID = commandGateway.sendAndWait(
            SubmitApplicationFormCommand(
                studentId = UUID.randomUUID(),
                name = application.name,
                surname = application.surname,
                email = application.email,
            )
        )
        val student: StudentDTO = queryGateway.query(StudentQuery(studentResult), StudentDTO::class.java).get()
        return student
    }

    @PostMapping("/assistant")
    fun registerAdministrativeAssistant(@RequestBody registration: RegisterAdministrativeAssistantDTO): UUID {
        logger.info("Submitting assistant registration for ${registration.surname} with email ${registration.email}")
        return commandGateway.sendAndWait(
            RegisterAdministrativeAssistantCommand(
                id = UUID.randomUUID(),
                name = registration.name,
                surname = registration.surname,
                email = registration.surname
            )
        )
    }

    @PostMapping("/assistant/{assistantId}/enroll/{studentId}")
    fun enrollStudent(@PathVariable assistantId: String, @PathVariable studentId: String) {
        logger.info("Submitting enroll student for $studentId")
        return commandGateway.sendAndWait(
            EnrollStudentCommand(
                UUID.fromString(assistantId),
                UUID.fromString(studentId)
            )
        )
    }

}