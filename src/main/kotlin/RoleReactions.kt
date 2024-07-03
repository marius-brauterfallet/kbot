import constants.client
import constants.guild
import constants.rolesMessageChannelId
import constants.rolesMessageId
import discord4j.common.util.Snowflake
import discord4j.core.event.domain.message.ReactionAddEvent
import discord4j.core.event.domain.message.ReactionRemoveEvent
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.reaction.ReactionEmoji
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import resources.guildUserRoles
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

    val userRole = guildUserRoles.find { role -> role.emoji == emojiString } ?: return Mono.empty()

    return guild.getRoleById(Snowflake.of(userRole.id))
        .flatMap { role -> if (addRole) member.addRole(role.id) else member.removeRole(role.id) }
        .then(Mono.empty())
}


fun updateUserRoles(): Flux<Void> {
    val guildMembers = guild.members

    val rolesMessage = client.getMessageById(rolesMessageChannelId, rolesMessageId)

    return rolesMessage.flatMapMany { message ->
        Flux.fromIterable(guildUserRoles).flatMap { userRole ->
            val roleSnowflake = Snowflake.of(userRole.id)
            val emojiReaction = ReactionEmoji.unicode(userRole.emoji)
            val messageReactors = message.getReactors(emojiReaction)

            val addRolesFlux = messageReactors
                .filterWhen { reactorUser -> guildMembers.any { member -> member.id == reactorUser.id } }
                .flatMap { reactorUser ->
                    guild.getMemberById(reactorUser.id).flatMap { member -> member.addRole(roleSnowflake) }
                }

            val removeRolesFlux = guildMembers
                .filterWhen { member ->
                    message.getReactors(emojiReaction)
                        .any { reactorUser -> reactorUser.id == member.id }
                        .map { hasReacted -> !hasReacted }
                }
                .flatMap { member -> member.removeRole(roleSnowflake) }

            addRolesFlux.mergeWith(removeRolesFlux)
        }
    }
}