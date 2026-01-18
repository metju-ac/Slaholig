package org.pv293.kotlinseminar.enrollmentService.application.aggregates

import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*
import org.axonframework.modelling.command.EntityId
import java.util.*

@Entity
@Table(name = "administrativeTask")
open class AdministrativeTask {
    @Id
    @EntityId
    open lateinit var id: UUID

    @Column(name = "description")
    open lateinit var description: String


    @JsonManagedReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administrative_assistant_id")
    open var administrativeAssistant: AdministrativeAssistant? = null
}