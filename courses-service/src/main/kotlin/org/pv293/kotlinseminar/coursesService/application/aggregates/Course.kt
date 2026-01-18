package org.pv293.kotlinseminar.coursesService.application.aggregates

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle.apply
import org.axonframework.modelling.command.AggregateMember
import org.axonframework.modelling.command.ForwardMatchingInstances
import org.axonframework.spring.stereotype.Aggregate
import org.pv293.kotlinseminar.coursesService.application.commands.impl.CreateCourseCommand
import org.pv293.kotlinseminar.coursesService.application.commands.impl.RegisterStudentToCourseCommand
import org.pv293.kotlinseminar.coursesService.events.impl.CourseCreatedEvent
import org.pv293.kotlinseminar.coursesService.events.impl.StudentRegisteredToCourseEvent
import java.util.UUID

@Entity
@Aggregate(repository = "courseRepository")
@Table(name = "course")
class Course() {

    @Id
    @AggregateIdentifier
    lateinit var id: UUID

    @Column(name = "title", length = 100)
    lateinit var title: String

    @Column(name = "description", length = 500)
    lateinit var description: String

    @Column(name = "capacity")
    var capacity: Int = 0

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "course", cascade = [CascadeType.ALL])
    @AggregateMember(eventForwardingMode = ForwardMatchingInstances::class)
    open var courseRegistrations: MutableSet<CourseRegistration> = mutableSetOf()

    @CommandHandler
    constructor(command: CreateCourseCommand) : this() {
        this.id = command.id
        this.title = command.title
        this.description = command.description
        this.capacity = command.capacity

        apply(
            CourseCreatedEvent(
                courseId = command.id,
                title = command.title,
                description = command.description,
                capacity = command.capacity
            )
        )
    }

    @CommandHandler
    fun handle(command: RegisterStudentToCourseCommand): Unit {
        if (courseRegistrations.size >= capacity) {
            throw IllegalStateException("Course is full")
        }

        val registration = CourseRegistration().apply {
            id = UUID.randomUUID()
            studentId = command.studentId
            course = this@Course
        }

        courseRegistrations.add(registration)
        apply(
            StudentRegisteredToCourseEvent(
                courseId = this.id,
                studentId = command.studentId
            )
        )
    }
}