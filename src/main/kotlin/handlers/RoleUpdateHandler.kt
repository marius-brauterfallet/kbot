package handlers

import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.message.MessageUpdateEvent
import model.KbotConfig
import reactor.core.publisher.Mono
import services.GuildRolesService

class RoleUpdateHandler(
    private val guildRolesService: GuildRolesService,
    private val client: GatewayDiscordClient,
    private val config: KbotConfig,
) {
    fun init() {
        client.on(MessageUpdateEvent::class.java) { event ->
            if (event.messageId != config.rolesMessageId) return@on Mono.just(Unit)

            guildRolesService.updateUserRoles().then(Mono.just(Unit))
        }.subscribe()
    }
}
