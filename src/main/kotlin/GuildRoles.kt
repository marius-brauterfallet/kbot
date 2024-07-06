import constants.client
import constants.rolesMessageChannelId
import constants.rolesMessageId
import discord4j.common.util.Snowflake
import model.UserRole
import reactor.core.publisher.Mono
import java.util.regex.Pattern

object GuildRoles {
    private var _roles: List<UserRole> = emptyList()

    val roles: List<UserRole>
        get() = _roles

    fun updateRoles(): Mono<Unit> {
        return client.getMessageById(rolesMessageChannelId, rolesMessageId).map { rolesMessage ->
            _roles = rolesMessage.content.lines().mapNotNull { line ->
                val matcher = Pattern
                    .compile("(\\S)\\s*->\\s*<@&(\\d+)>.*")
                    .matcher(line)

                if (!matcher.matches()) return@mapNotNull null

                val emoji = matcher.group(1)
                val id = matcher.group(2).toLongOrNull()?.let { Snowflake.of(it) } ?: return@mapNotNull null

                UserRole(emoji, id)
            }
        }

    }
}