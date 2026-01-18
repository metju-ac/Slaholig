package org.pv293.kotlinseminar.forumService.application.commands.impl

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.UUID

data class CreateForumPostCommand(
    @TargetAggregateIdentifier
    val id: UUID,
    val courseForumId: UUID,
    val authorId: UUID,
    val title: String,
    val content: String,
)
