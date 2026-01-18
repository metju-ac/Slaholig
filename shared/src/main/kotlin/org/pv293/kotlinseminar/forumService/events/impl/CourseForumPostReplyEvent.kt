package org.pv293.kotlinseminar.forumService.events.impl

import java.util.UUID

data class CourseForumPostReplyEvent(
    val courseForumPostId: UUID,
    val replyId: UUID,
)