import constants.Constants.client
import constants.Constants.config
import constants.Constants.logger
import discord4j.common.util.Snowflake
import model.UserRole
import reactor.core.publisher.Mono

object GuildRoles {
    private var _roles: List<UserRole> = emptyList()

    val roles: List<UserRole>
        get() = _roles

    fun updateRoles(): Mono<Unit> {
        logger.info("Updating guild role list...")

        return client.getMessageById(config.rolesMessageChannelId, config.rolesMessageId).map { rolesMessage ->
            _roles = rolesMessage.content.lines().mapNotNull { line ->
                val match = Regex("(\\S+)\\s*->\\s*<@&(\\d+)>.*").matchEntire(line.trim()) ?: return@mapNotNull null

                val emoji = match.groups[1]?.value ?: return@mapNotNull null
                val id = match.groups[2]?.value?.toLongOrNull()?.let { Snowflake.of(it) } ?: return@mapNotNull null

                UserRole(emoji, id)
            }
        }
    }
}