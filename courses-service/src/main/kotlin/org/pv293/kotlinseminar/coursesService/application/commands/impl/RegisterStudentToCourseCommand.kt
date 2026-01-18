package org.pv293.kotlinseminar.coursesService.application.commands.impl

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.UUID


data class RegisterStudentToCourseCommand(
    @TargetAggregateIdentifier
    val courseId: UUID,
    val studentId: UUID,
)