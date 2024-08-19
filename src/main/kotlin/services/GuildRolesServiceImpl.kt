package services

import constants.Constants.client
import constants.Constants.config
import constants.Constants.guild
import constants.Constants.logger
import discord4j.common.util.Snowflake
import discord4j.core.`object`.reaction.ReactionEmoji
import model.UserRole
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class GuildRolesServiceImpl : GuildRolesService {
    private var _roles: List<UserRole> = emptyList()

    private fun updateRolesList(): Mono<Unit> {
        logger.info("Updating guild role list...")

        return client.getMessageById(config.rolesMessageChannelId, config.rolesMessageId).map { rolesMessage ->
            _roles = rolesMessage.content.lines().mapNotNull { line ->
                val match = Regex("(\\S+)\\s*->\\s*<@&(\\d+)>.*").matchEntire(line.trim()) ?: return@mapNotNull null

                val emoji = match.groups[1]?.value ?: return@mapNotNull null
                val id = match.groups[2]?.value?.toLongOrNull()?.let { Snowflake.of(it) } ?: return@mapNotNull null

                UserRole(emoji, id)
            }
        }.doOnSuccess { logger.info("Done updating guild role list") }
    }

    override fun getRoleByEmoji(emoji: String): UserRole? {
        return _roles.find { role -> role.emoji == emoji }
    }

    override fun updateUserRoles(): Flux<Void> {
        return updateRolesList().flatMapMany {
            logger.info("Updating user roles...")

            guild.members.collectList().flatMapMany { guildMembers ->
                client.getMessageById(config.rolesMessageChannelId, config.rolesMessageId).flatMapMany { rolesMessage ->
                    Flux.concat(_roles.map { userRole ->
                        rolesMessage.getReactors(ReactionEmoji.unicode(userRole.emoji)).collectList()
                            .flatMapMany { reactors ->
                                Flux.fromIterable(guildMembers).flatMap { member ->
                                    if (reactors.any { it.id == member.id })
                                        member.addRole(userRole.id)
                                    else
                                        member.removeRole(userRole.id)
                                }
                            }
                    })
                }
            }
        }.doOnComplete { logger.info("Done updating user roles") }
    }
}