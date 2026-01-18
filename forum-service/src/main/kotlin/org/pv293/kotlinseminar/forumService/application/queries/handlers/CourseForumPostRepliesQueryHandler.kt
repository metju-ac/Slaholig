package org.pv293.kotlinseminar.forumService.application.queries.handlers

import org.axonframework.queryhandling.QueryHandler
import org.pv293.kotlinseminar.forumService.application.dto.ForumPostReplyDTO
import org.pv293.kotlinseminar.forumService.application.queries.impl.CourseForumPostRepliesQuery
import org.pv293.kotlinseminar.forumService.repository.CourseForumPostRepository
import org.springframework.stereotype.Component

@Component
class CourseForumPostRepliesQueryHandler(
    private val courseForumPostRepository: CourseForumPostRepository
) {
    @QueryHandler
    fun handle(query: CourseForumPostRepliesQuery): List<ForumPostReplyDTO> {
        val forumPost = courseForumPostRepository.findById(query.courseForumPostId).get()
        return forumPost.replies.map { ForumPostReplyDTO(
            content = it.content,
            authorId = it.authorId.toString(),
        ) }
    }
}