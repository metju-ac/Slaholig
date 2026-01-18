package org.pv293.kotlinseminar.forumService.events.impl


import java.util.UUID

data class CourseForumCreatedEvent(
    val forumId: UUID,
    val courseId: UUID
)