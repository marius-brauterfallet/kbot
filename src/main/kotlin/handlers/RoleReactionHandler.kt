package handlers

import constants.Constants.client
import constants.Constants.config
import constants.Constants.guild
import constants.Constants.logger
import discord4j.core.event.domain.message.ReactionAddEvent
import discord4j.core.event.domain.message.ReactionRemoveEvent
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.reaction.ReactionEmoji
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import services.GuildRolesService
import services.GuildRolesServiceImpl
import kotlin.jvm.optionals.getOrElse

object RoleReactionHandler : KoinComponent {
    private val guildRolesService: GuildRolesService by inject()

    fun roleReactionHandler() {
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


    fun handleEmojiRoleChange(member: Member, emoji: ReactionEmoji, addRole: Boolean): Mono<Unit> {
        val emojiString = emoji.asUnicodeEmoji().getOrElse { return Mono.empty() }.raw

        val userRole = guildRolesService.getRoleByEmoji(emojiString) ?: return Mono.empty()

        return guild.getRoleById(userRole.id)
            .flatMap { role -> if (addRole) member.addRole(role.id) else member.removeRole(role.id) }
            .then(Mono.empty())
    }
}
