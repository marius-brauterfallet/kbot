package handlers

import constants.Constants.client
import constants.Constants.config
import discord4j.core.event.domain.message.MessageUpdateEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import reactor.core.publisher.Mono
import services.GuildRolesService

object RoleUpdateHandler : KoinComponent {
    private val guildRolesService: GuildRolesService by inject()

    fun roleChangeHandler() {
        client.on(MessageUpdateEvent::class.java) { event ->
            if (event.messageId != config.rolesMessageId) return@on Mono.empty<Unit>()

//            guildRolesService.updateRoles()
//                .flatMapMany { updateUserRoles() }
//                .then(Mono.empty())

            guildRolesService.updateUserRoles().then(Mono.just(Unit))
        }.subscribe()
    }
}
