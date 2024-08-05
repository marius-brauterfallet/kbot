package handlers

import GuildRoles
import constants.client
import constants.rolesMessageId
import discord4j.core.event.domain.message.MessageUpdateEvent
import reactor.core.publisher.Mono

fun roleChangeHandler() {
    client.on(MessageUpdateEvent::class.java) { event ->
        if (event.messageId != rolesMessageId) return@on Mono.empty<Unit>()

        GuildRoles.updateRoles()
            .flatMapMany { updateUserRoles() }
            .then(Mono.empty())
    }.subscribe()
}