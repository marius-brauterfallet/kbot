package constants

import com.typesafe.config.ConfigFactory
import discord4j.common.util.Snowflake
import initializeKbot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.slf4j.LoggerFactory
import java.util.*

private val environment = System.getenv("ENV") ?: "default"

val config = when (environment) {
    "dev" -> ConfigFactory.parseResources("application.dev.conf").withFallback(ConfigFactory.load())
    else -> ConfigFactory.load()
} ?: throw IllegalStateException("Could not load bot configuration")

val properties = Properties().apply {
    load(Thread.currentThread().contextClassLoader.getResourceAsStream("application.properties"))
}

val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

val logger = LoggerFactory.getLogger("kbot") ?: throw Exception("Something went wrong initializing the Logback logger")

val appVersion = properties.getProperty("version")
    ?: IllegalStateException("Something went wrong when retrieving app properties")

val client = initializeKbot()

val guild = client.getGuildById(Snowflake.of(config.getLong("discord.guildId"))).block()
    ?: throw IllegalStateException("Something went wrong when retrieving the guild id. Environment variable GUILD_ID might be missing")

val rolesMessageChannelId = Snowflake.of(config.getLong("discord.rolesMessage.channelId"))
    ?: throw IllegalStateException("Something went wrong when retrieving the role selection channel id. Environment variable ROLES_MESSAGE_CHANNEL_ID might be missing")
val rolesMessageId = Snowflake.of(config.getLong("discord.rolesMessage.messageId"))
    ?: throw IllegalStateException("Somethign went wrong when retrieving the role selection message id. Environment variable ROLES_MESSAGE_ID might be missing")