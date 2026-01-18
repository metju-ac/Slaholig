package org.pv293.kotlinseminar.assignmentService.application.aggregates

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle.apply
import org.axonframework.modelling.command.AggregateMember
import org.axonframework.modelling.command.ForwardMatchingInstances
import org.axonframework.spring.stereotype.Aggregate
import org.pv293.kotlinseminar.assignmentService.application.commands.impl.CreateAssignmentCommand
import org.pv293.kotlinseminar.assignmentService.application.commands.impl.HandAssignmentCommand
import org.pv293.kotlinseminar.assignmentService.events.impl.AssignmentCreatedEvent
import org.pv293.kotlinseminar.assignmentService.events.impl.AssignmentHandedOutEvent
import java.util.UUID


@Entity
@Aggregate(repository="assignmentRepository")
class Assignment() {

    @Id
    @AggregateIdentifier
    open lateinit var id: UUID

    @Column(name = "course_id")
    lateinit var courseId: UUID

    @Column(name = "title", length = 100)
    lateinit var title: String


    @Column(name = "text", length = 2000)
    lateinit var assignmentText: String

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "assignment", cascade = [CascadeType.ALL])
    @AggregateMember(eventForwardingMode = ForwardMatchingInstances::class)
    open var assignmentHandouts: MutableSet<AssignmentHandout> = mutableSetOf()

    @CommandHandler
    constructor(command: CreateAssignmentCommand) : this() {
        this.id = command.id
        this.courseId = command.courseId
        this.title = command.title
        this.assignmentText = command.assignmentText
        apply(
            AssignmentCreatedEvent(
                id = id,
                courseId = courseId,
                title = title,
                assignmentText = assignmentText
            )
        )
    }

    @CommandHandler
    fun handle(command: HandAssignmentCommand) {
        val handout = AssignmentHandout().apply {
            id = command.id
            studentId = command.studentId
            handoutDate = command.handoutDate
            deadlineDate = command.deadlineDate
            assignment = this@Assignment
        }
        this.assignmentHandouts.add(handout)
        apply(
            AssignmentHandedOutEvent(
                id = handout.id,
                assignmentId = this.id,
                studentId = handout.studentId,
                handoutDate = handout.handoutDate,
                deadlineDate = handout.deadlineDate
            )
        )
    }


}