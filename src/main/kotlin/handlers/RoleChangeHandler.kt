package handlers

import constants.client
import constants.rolesMessageId
import discord4j.core.event.domain.message.MessageUpdateEvent
import reactor.core.publisher.Mono
import services.GuildRolesService

fun roleChangeHandler() {
    client.on(MessageUpdateEvent::class.java) { event ->
        if (event.messageId != rolesMessageId) return@on Mono.empty<Unit>()

        GuildRolesService.updateRoles()
            .flatMapMany { updateUserRoles() }
            .then(Mono.empty())
    }.subscribe()
}