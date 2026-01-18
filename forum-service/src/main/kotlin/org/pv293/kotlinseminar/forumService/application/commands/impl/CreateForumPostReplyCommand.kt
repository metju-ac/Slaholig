package org.pv293.kotlinseminar.forumService.application.commands.impl

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.UUID

data class CreateForumPostReplyCommand(
    @TargetAggregateIdentifier
    val forumPostId: UUID,
    val authorId: UUID,
    val content: String
)