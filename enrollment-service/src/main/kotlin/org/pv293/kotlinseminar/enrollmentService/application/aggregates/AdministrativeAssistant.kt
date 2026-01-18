package org.pv293.kotlinseminar.enrollmentService.application.aggregates

import jakarta.persistence.*
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateMember
import org.axonframework.modelling.command.AggregateLifecycle.apply
import org.axonframework.modelling.command.ForwardMatchingInstances
import org.axonframework.spring.stereotype.Aggregate
import org.pv293.kotlinseminar.enrollmentService.application.commands.impl.EnrollStudentCommand
import org.pv293.kotlinseminar.enrollmentService.application.commands.impl.RegisterAdministrativeAssistantCommand
import org.pv293.kotlinseminar.enrollmentService.events.impl.AdministrativeAssistantRegisteredEvent
import org.pv293.kotlinseminar.enrollmentService.events.impl.StudentEnrolledEvent
import org.springframework.beans.factory.annotation.Autowired

import java.util.UUID

@Entity
@Aggregate(repository = "administrativeAssistantRepository")
@Table(name = "administrative_assistant")
class AdministrativeAssistant() {
    @Id
    @AggregateIdentifier
    open lateinit var id: UUID

    @Column(name = "name", length = 50)
    open lateinit var name: String

    @Column(name = "surname", length = 50)
    open lateinit var surname: String

    @Column(name = "email", length = 100)
    open lateinit var email: String


    @OneToMany(fetch = FetchType.EAGER, mappedBy = "administrativeAssistant", cascade = [CascadeType.ALL])
    @AggregateMember(eventForwardingMode = ForwardMatchingInstances::class)
    open var tasks: MutableList<AdministrativeTask> = mutableListOf()


    @CommandHandler
    constructor(cmd: RegisterAdministrativeAssistantCommand) : this() {
        id = cmd.id
        name = cmd.name
        surname = cmd.surname
        email = cmd.email
        apply(
            AdministrativeAssistantRegisteredEvent(
                adminId = cmd.id,
                name = cmd.name,
                surname = cmd.surname,
                email = cmd.email
            )
        )
    }

    @CommandHandler
    fun handle(cmd: EnrollStudentCommand) {
        apply(
            StudentEnrolledEvent(
                studentId = cmd.studentID
            )
        )
    }
}