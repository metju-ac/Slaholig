package org.pv293.kotlinseminar.forumService.application.queries.handlers

import org.axonframework.queryhandling.QueryHandler
import org.pv293.kotlinseminar.exceptions.NotFoundException
import org.pv293.kotlinseminar.forumService.application.dto.ForumPostDTO
import org.pv293.kotlinseminar.forumService.application.queries.impl.CourseForumPostQuery
import org.pv293.kotlinseminar.forumService.repository.CourseForumPostRepository
import org.springframework.stereotype.Component
import kotlin.jvm.optionals.getOrElse


@Component
class CourseForumPostQueryHandler(
    private val courseForumPostRepository: CourseForumPostRepository,
) {
    @QueryHandler
    fun handle(query : CourseForumPostQuery): ForumPostDTO {
        val post = courseForumPostRepository.findById(query.courseForumPostId).getOrElse {
            throw NotFoundException()
        }
        return ForumPostDTO(
            id = post.id,
            courseForumId = post.courseForumId,
            authorId = post.authorId,
            title = post.title,
            content = post.content
        )
    }
}