package org.pv293.kotlinseminar.forumService.application.dto

data class CreateForumPostDTO(
    val authorId: String,
    val title: String,
    val content: String
)