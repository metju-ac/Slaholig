package org.pv293.kotlinseminar.assignmentService.controllers

import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.queryhandling.QueryGateway
import org.pv293.kotlinseminar.assignmentService.application.dto.AssignmentDTO
import org.pv293.kotlinseminar.assignmentService.application.dto.CreateAssignmentDTO
import org.pv293.kotlinseminar.assignmentService.application.queries.impl.AssignmentQuery
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/assignments")
class AssignmentsController(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway,
) {
    private val logger = LoggerFactory.getLogger(AssignmentsController::class.java)

    @PostMapping("/")
    fun createAssignments(@RequestBody assignments: CreateAssignmentDTO): AssignmentDTO {
        logger.info("Received request to create assignment: $assignments")
        val id: UUID = commandGateway.sendAndWait<UUID>(
            org.pv293.kotlinseminar.assignmentService.application.commands.impl.CreateAssignmentCommand(
                id = UUID.randomUUID(),
                courseId = assignments.courseId,
                title = assignments.title,
                assignmentText = assignments.assignmentText,
            )
        )
        return queryGateway.query(AssignmentQuery(id), AssignmentDTO::class.java).get()
    }
}