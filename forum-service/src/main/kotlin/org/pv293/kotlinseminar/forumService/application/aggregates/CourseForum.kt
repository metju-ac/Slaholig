package org.pv293.kotlinseminar.forumService.application.aggregates

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.extensions.kotlin.createNew
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle.apply
import org.axonframework.modelling.command.Repository
import org.axonframework.spring.stereotype.Aggregate
import org.pv293.kotlinseminar.forumService.application.commands.impl.CreateCourseForumCommand
import org.pv293.kotlinseminar.forumService.events.impl.CourseForumCreatedEvent
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

@Entity
@Aggregate(repository = "courseForumRepository")
@Table(name = "course_forum")
class CourseForum() {
    @Id
    @AggregateIdentifier
    lateinit var id: UUID

    @Column(name = "course_id")
    lateinit var courseId: UUID

    @CommandHandler
    constructor(command: CreateCourseForumCommand) : this() {
        this.id = command.id
        this.courseId = command.courseId

        apply(
            CourseForumCreatedEvent(
                forumId = command.id,
                courseId = command.courseId
            )
        )
    }


}