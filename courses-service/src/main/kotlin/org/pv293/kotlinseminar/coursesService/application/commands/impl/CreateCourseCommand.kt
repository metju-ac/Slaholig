package org.pv293.kotlinseminar.coursesService.application.commands.impl

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.UUID

data class CreateCourseCommand(
    @TargetAggregateIdentifier
    val id: UUID,
    val title: String,
    val description: String,
    val capacity: Int,
)