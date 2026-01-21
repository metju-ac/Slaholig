package org.pv293.kotlinseminar.enrollmentService.application.aggregates

import jakarta.persistence.*
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventhandling.EventHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle.apply
import org.axonframework.modelling.command.AggregateMember
import org.axonframework.modelling.command.ForwardMatchingInstances
import org.axonframework.spring.stereotype.Aggregate
import org.pv293.kotlinseminar.enrollmentService.application.commands.impl.SubmitApplicationFormCommand
import org.pv293.kotlinseminar.enrollmentService.events.impl.StudentCreatedEvent
import org.pv293.kotlinseminar.enrollmentService.events.impl.StudentEnrolledEvent

import java.util.UUID

@Entity
@Aggregate(repository = "registeredStudentRepository")
@Table(name = "student")
open class Student() {
    @Id
    @AggregateIdentifier
    open lateinit var id: UUID

    @Column(name = "name", length = 50)
    open lateinit var name: String

    @Column(name = "surname", length = 50)
    open lateinit var surname: String

    @Column(name = "email", length = 100)
    open lateinit var email: String

    @Column(name = "enrolled")
    open var enrolled: Boolean = false


    @OneToMany(fetch = FetchType.EAGER, mappedBy = "student", cascade = [CascadeType.ALL])
    @AggregateMember(eventForwardingMode = ForwardMatchingInstances::class)
    open var payments: MutableList<StudentPayment> = mutableListOf()

    @CommandHandler
    constructor(command: SubmitApplicationFormCommand) : this() {
        id = command.studentId
        name = command.name
        surname = command.surname
        email = command.email
        apply(
            StudentCreatedEvent(
                studentId = command.studentId,
                name = command.name,
                surname = command.surname,
                email = command.email
            )
        )
    }

    @EventHandler
    fun on(event: StudentEnrolledEvent) {
        if (event.studentId == this.id) {
            this.enrolled = true
        }
    }

}