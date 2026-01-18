package org.pv293.kotlinseminar.enrollmentService.application.aggregates


import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*
import kotlinx.datetime.*
import org.axonframework.modelling.command.EntityId
import java.time.LocalDateTime
import java.util.UUID


@Entity
@Table(name = "student_payment")
open class StudentPayment {
    @Id
    @EntityId
    open lateinit var id: UUID
    @Column(name = "transaction_amount")
    open var transactionAmount: Double = 0.0
    @Column(name = "issued_at")
    open var transactionIssued: LocalDateTime = LocalDateTime.now()
    @Column(name = "status")
    open var transactionStatus: String = "PENDING"

    @JsonManagedReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    open var student: Student? = null
}