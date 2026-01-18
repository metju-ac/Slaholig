package org.pv293.kotlinseminar.coursesService.application.aggregates

import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.axonframework.modelling.command.EntityId
import java.util.UUID

@Entity
@Table(name = "course_registration")
open class CourseRegistration {
    @Id
    @EntityId
    open lateinit var id: UUID

    @Column(name = "student_id")
    open lateinit var studentId: UUID

    @JsonManagedReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    open var course: Course? = null

}