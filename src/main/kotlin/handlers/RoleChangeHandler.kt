package handlers

import GuildRoles
import constants.Constants.client
import constants.Constants.config
import discord4j.core.event.domain.message.MessageUpdateEvent
import reactor.core.publisher.Mono

fun roleChangeHandler() {
    client.on(MessageUpdateEvent::class.java) { event ->
        if (event.messageId != config.rolesMessageId) return@on Mono.empty<Unit>()

        GuildRoles.updateRoles()
            .flatMapMany { updateUserRoles() }
            .then(Mono.empty())
    }.subscribe()
}