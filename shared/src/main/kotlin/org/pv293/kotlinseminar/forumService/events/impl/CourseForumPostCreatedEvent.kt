package org.pv293.kotlinseminar.forumService.events.impl

import java.util.UUID

class CourseForumPostCreatedEvent(
    val postId: UUID,
    val courseForumId: UUID,
    val authorId: UUID,
    val title: String,
    val content: String
)