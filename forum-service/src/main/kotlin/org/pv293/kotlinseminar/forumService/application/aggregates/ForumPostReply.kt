package org.pv293.kotlinseminar.forumService.application.aggregates

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
@Table(name = "forum_course_post_reply")
class ForumPostReply {
    @Id
    @EntityId
    lateinit var id: UUID

    @Column(name = "author_id")
    lateinit var authorId: UUID

    @Column(name = "content", length = 3000)
    lateinit var content: String

    @JsonManagedReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "forum_post_id")
    open var forumPost: ForumPost? = null
}