import constants.*
import discord4j.core.event.domain.message.ReactionAddEvent
import discord4j.core.event.domain.message.ReactionRemoveEvent
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.reaction.ReactionEmoji
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import kotlin.jvm.optionals.getOrElse

fun roleReactionHandler() {
    client.on(ReactionAddEvent::class.java) { event ->
        if (event.messageId != rolesMessageId) return@on Mono.empty()

        val member = event.member.getOrElse { return@on Mono.empty() }

        handleEmojiRoleChange(member, event.emoji, true)
    }.subscribe()

    client.on(ReactionRemoveEvent::class.java) { event ->
        if (event.messageId != rolesMessageId) return@on Mono.empty()

        event.user
            .flatMap { it.asMember(guild.id) }
            .flatMap { member -> handleEmojiRoleChange(member, event.emoji, false) }

    }.subscribe()
}


fun handleEmojiRoleChange(member: Member, emoji: ReactionEmoji, addRole: Boolean): Mono<Unit> {
    val emojiString = emoji.asUnicodeEmoji().getOrElse { return Mono.empty() }.raw

    val userRole = GuildRoles.roles.find { role -> role.emoji == emojiString } ?: return Mono.empty()

    return guild.getRoleById(userRole.id)
        .flatMap { role -> if (addRole) member.addRole(role.id) else member.removeRole(role.id) }
        .then(Mono.empty())
}


fun updateUserRoles(): Flux<Void> {
    logger.info("Updating user roles...")

    return guild.members.collectList().flatMapMany { guildMembers ->
        client.getMessageById(rolesMessageChannelId, rolesMessageId).flatMapMany { rolesMessage ->
            Flux.concat(GuildRoles.roles.map { userRole ->
                rolesMessage.getReactors(ReactionEmoji.unicode(userRole.emoji)).collectList().flatMapMany { reactors ->
                    Flux.fromIterable(guildMembers).flatMap { member ->
                        if (reactors.any { it.id == member.id })
                            member.addRole(userRole.id)
                        else
                            member.removeRole(userRole.id)
                    }
                }
            })
        }
    }.doOnComplete { logger.info("User roles update completed.") }
}