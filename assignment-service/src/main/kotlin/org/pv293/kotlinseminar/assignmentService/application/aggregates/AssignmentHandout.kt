package org.pv293.kotlinseminar.assignmentService.application.aggregates

import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.axonframework.modelling.command.EntityId
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "assignment_handout")
open class AssignmentHandout() {
    @Id
    @EntityId
    open lateinit var id: UUID

    @Column(name = "student_id")
    open lateinit var studentId: UUID

    @Column(name = "handout_date")
    open lateinit var handoutDate: Instant

    @Column(name = "deadline_date")
    open lateinit var deadlineDate: Instant


    @JsonManagedReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id")
    open var assignment: Assignment? = null
}