package org.pv293.kotlinseminar.forumService.application.dto

import java.util.UUID

data class ForumPostDTO(
    val id: UUID,
    val courseForumId: UUID,
    val authorId: UUID,
    val title: String,
    val content: String
)
