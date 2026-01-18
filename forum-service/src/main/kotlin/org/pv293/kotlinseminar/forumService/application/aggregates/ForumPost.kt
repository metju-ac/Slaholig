package org.pv293.kotlinseminar.forumService.application.aggregates

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
import org.axonframework.spring.stereotype.Aggregate
import org.pv293.kotlinseminar.forumService.application.commands.impl.CreateForumPostCommand
import org.pv293.kotlinseminar.forumService.application.commands.impl.CreateForumPostReplyCommand
import org.pv293.kotlinseminar.forumService.events.impl.CourseForumPostCreatedEvent
import org.pv293.kotlinseminar.forumService.events.impl.CourseForumPostReplyEvent
import java.util.UUID

@Entity
@Aggregate(repository = "forumPostRepository")
@Table(name = "forum_post")
class ForumPost() {
    @Id
    @AggregateIdentifier
    lateinit var id: UUID

    @Column(name = "course_forum_id")
    lateinit var courseForumId: UUID

    @Column(name = "title", length = 100)
    lateinit var title: String

    @Column(name = "content", length = 5000)
    lateinit var content: String

    @Column(name = "author_id")
    lateinit var authorId: UUID

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "forumPost", cascade = [CascadeType.ALL])
    open var replies: MutableList<ForumPostReply> = mutableListOf()

    @CommandHandler
    constructor(command: CreateForumPostCommand):  this() {
        this.id = command.id
        this.courseForumId = command.courseForumId
        this.title = command.title
        this.content = command.content
        this.authorId = command.authorId

        apply(
            CourseForumPostCreatedEvent(
                postId = command.id,
                courseForumId = command.courseForumId,
                authorId = command.authorId,
                title = command.title,
                content = command.content
            )
        )
    }

    @CommandHandler
    fun handle(command: CreateForumPostReplyCommand) {
        val reply = ForumPostReply().apply {
            id = UUID.randomUUID()
            authorId = command.authorId
            content = command.content
            forumPost = this@ForumPost
        }
        this.replies.add(reply)
        apply(
            CourseForumPostReplyEvent(
                courseForumPostId = this.id,
                replyId = reply.id,
            )
        )
    }

}