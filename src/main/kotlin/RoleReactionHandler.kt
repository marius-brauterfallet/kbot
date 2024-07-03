import constants.client
import constants.guild
import discord4j.core.event.domain.message.ReactionAddEvent
import discord4j.core.event.domain.message.ReactionRemoveEvent
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.reaction.ReactionEmoji
import reactor.core.publisher.Mono
import resources.userRoles
import kotlin.jvm.optionals.getOrElse

fun roleReactionHandler(roleMessageId: Long) {
    client.on(ReactionAddEvent::class.java) { event ->
        if (event.messageId.asLong() != roleMessageId) return@on Mono.empty()

        val member = event.member.getOrElse { return@on Mono.empty() }

        handleEmojiRoleChange(member, event.emoji, true)
    }.subscribe()

    client.on(ReactionRemoveEvent::class.java) { event ->
        if (event.messageId.asLong() != roleMessageId) return@on Mono.empty()

        event.user
            .flatMap { it.asMember(guild.id) }
            .flatMap { member -> handleEmojiRoleChange(member, event.emoji, false) }

    }.subscribe()
}


fun handleEmojiRoleChange(member: Member, emoji: ReactionEmoji, addRole: Boolean): Mono<Unit> {
    val emojiString = emoji.asUnicodeEmoji().getOrElse { return Mono.empty() }.raw

    val (roleName, _) = userRoles.find { role -> role.emoji == emojiString } ?: return Mono.empty()

    return guild.roles
        .filter { role -> role.name == roleName }
        .singleOrEmpty()
        .flatMap { role -> if (addRole) member.addRole(role.id) else member.removeRole(role.id) }
        .then(Mono.empty())
}