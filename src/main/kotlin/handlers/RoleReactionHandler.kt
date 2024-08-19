package handlers

import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.message.ReactionAddEvent
import discord4j.core.event.domain.message.ReactionRemoveEvent
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.reaction.ReactionEmoji
import model.KbotConfig
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import reactor.core.publisher.Mono
import services.GuildRolesService
import kotlin.jvm.optionals.getOrElse

object RoleReactionHandler : KoinComponent {
    private val guildRolesService: GuildRolesService by inject()
    private val client: GatewayDiscordClient by inject()
    private val config: KbotConfig by inject()
    private val guild: Guild by inject()

    fun init() {
        client.on(ReactionAddEvent::class.java) { event ->
            if (event.messageId != config.rolesMessageId) return@on Mono.empty()

            val member = event.member.getOrElse { return@on Mono.empty() }

            handleEmojiRoleChange(member, event.emoji, true)
        }.subscribe()

        client.on(ReactionRemoveEvent::class.java) { event ->
            if (event.messageId != config.rolesMessageId) return@on Mono.empty()

            event.user
                .flatMap { it.asMember(guild.id) }
                .flatMap { member -> handleEmojiRoleChange(member, event.emoji, false) }

        }.subscribe()
    }


    private fun handleEmojiRoleChange(member: Member, emoji: ReactionEmoji, addRole: Boolean): Mono<Unit> {
        val emojiString = emoji.asUnicodeEmoji().getOrElse { return Mono.empty() }.raw

        val userRole = guildRolesService.getRoleByEmoji(emojiString) ?: return Mono.empty()

        return guild.getRoleById(userRole.id)
            .flatMap { role -> if (addRole) member.addRole(role.id) else member.removeRole(role.id) }
            .then(Mono.empty())
    }
}
