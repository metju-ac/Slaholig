package org.pv293.kotlinseminar.forumService.repository

import org.pv293.kotlinseminar.forumService.application.aggregates.ForumPost
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CourseForumPostRepository: JpaRepository<ForumPost, UUID> {
}