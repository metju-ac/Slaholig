package org.pv293.kotlinseminar.forumService.controllers

import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.pv293.kotlinseminar.forumService.application.commands.impl.CreateForumPostCommand
import org.pv293.kotlinseminar.forumService.application.commands.impl.CreateForumPostReplyCommand
import org.pv293.kotlinseminar.forumService.application.dto.CreateForumPostDTO
import org.pv293.kotlinseminar.forumService.application.dto.ForumPostDTO
import org.pv293.kotlinseminar.forumService.application.dto.ForumPostReplyDTO
import org.pv293.kotlinseminar.forumService.application.dto.ReplyToForumPostDTO
import org.pv293.kotlinseminar.forumService.application.queries.impl.CourseForumPostQuery
import org.pv293.kotlinseminar.forumService.application.queries.impl.CourseForumPostRepliesQuery
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/forums")
class CourseForumController(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway,
) {
    private val logger = LoggerFactory.getLogger(CourseForumController::class.java)

    @PostMapping("/{courseForumId}")
    fun createCourseForum(@RequestBody forumPost: CreateForumPostDTO, @PathVariable courseForumId: String): ForumPostDTO {
        logger.info("Creating course forum post for courseId: $courseForumId with title: ${forumPost.title}")
        val id = commandGateway.sendAndWait<UUID>(
            CreateForumPostCommand(
                id = UUID.randomUUID(),
                courseForumId = UUID.fromString(courseForumId),
                authorId = UUID.fromString(forumPost.authorId),
                title = forumPost.title,
                content = forumPost.content
            )
        )
        logger.info("Created forum post with id: $id")
        return queryGateway.query(CourseForumPostQuery(id), ForumPostDTO::class.java).get()
    }

    @PostMapping("/{courseForumId}/posts/{postId}/replies")
    fun replyToForumPost(@RequestBody replyToForumPostDTO: ReplyToForumPostDTO, @PathVariable courseForumId: String, @PathVariable postId: String) {
        logger.info("Replying to forum post with id: $postId for courseId: $courseForumId")
        commandGateway.sendAndWait<UUID>(
            CreateForumPostReplyCommand(
                forumPostId = UUID.fromString(postId),
                authorId = UUID.fromString(replyToForumPostDTO.authorId),
                content = replyToForumPostDTO.content
            )
        )

    }

    @GetMapping("/{courseForumId}/posts/{postId}/replies")
    fun getForumPostReplies(@PathVariable courseForumId: String, @PathVariable postId: String): List<ForumPostReplyDTO> {
        logger.info("Getting replies for forum post with id: $postId for courseId: $courseForumId")
        return queryGateway.query(
            CourseForumPostRepliesQuery(UUID.fromString(postId)), ResponseTypes.multipleInstancesOf(
                ForumPostReplyDTO::class.java
            )
        ).get()
    }




}