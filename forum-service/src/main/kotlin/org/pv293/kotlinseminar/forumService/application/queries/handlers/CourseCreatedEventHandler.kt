package org.pv293.kotlinseminar.forumService.application.queries.handlers

import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.EventHandler
import org.pv293.kotlinseminar.coursesService.events.impl.CourseCreatedEvent
import org.pv293.kotlinseminar.forumService.application.commands.impl.CreateCourseForumCommand
import org.springframework.stereotype.Component
import java.util.UUID


@Component
class CourseCreatedEventHandler(
    private val commandGateway: CommandGateway,
) {
     @EventHandler
     fun on(event: CourseCreatedEvent) {
         val forumId = UUID.randomUUID() // Create new aggregate id
         commandGateway.send<CreateCourseForumCommand>(CreateCourseForumCommand(forumId, event.courseId))
     }
}