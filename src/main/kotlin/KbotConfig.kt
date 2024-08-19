import com.typesafe.config.Config
import discord4j.common.util.Snowflake

class KbotConfig(config: Config, val environment: String) {
    val discordToken = config.getString("discord.token")
    val guildId = Snowflake.of(config.getLong("discord.guildId"))
    val rolesMessageChannelId = Snowflake.of(config.getLong("discord.rolesMessage.channelId"))
    val rolesMessageId = Snowflake.of(config.getLong("discord.rolesMessage.messageId"))
    val dailyUpdatesChannelId = Snowflake.of(config.getLong("discord.dailyUpdatesChannelId"))
}