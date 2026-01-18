package org.pv293.kotlinseminar.forumService.application.commands.impl

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.UUID

data class CreateCourseForumCommand(
    @TargetAggregateIdentifier
    val id: UUID,
    val courseId: UUID,
)
