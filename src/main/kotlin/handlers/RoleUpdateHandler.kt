package handlers

import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.message.MessageUpdateEvent
import model.KbotConfig
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import reactor.core.publisher.Mono
import services.GuildRolesService

object RoleUpdateHandler : KoinComponent {
    private val guildRolesService: GuildRolesService by inject()
    private val client: GatewayDiscordClient by inject()
    private val config: KbotConfig by inject()

    fun init() {
        client.on(MessageUpdateEvent::class.java) { event ->
            if (event.messageId != config.rolesMessageId) return@on Mono.just(Unit)

            guildRolesService.updateUserRoles().then(Mono.just(Unit))
        }.subscribe()
    }
}
