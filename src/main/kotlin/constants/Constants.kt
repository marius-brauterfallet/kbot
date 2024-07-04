package constants

import com.typesafe.config.ConfigFactory
import discord4j.common.util.Snowflake
import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import discord4j.gateway.intent.Intent
import discord4j.gateway.intent.IntentSet
import java.util.*

private val environment = System.getenv("ENV") ?: "default"

val config = when (environment) {
    "dev" -> ConfigFactory.parseResources("application.dev.conf").withFallback(ConfigFactory.load())
    else -> ConfigFactory.load()
} ?: throw IllegalStateException("Could not load bot configuration")

val properties = Properties().apply {
    load(Thread.currentThread().contextClassLoader.getResourceAsStream("application.properties"))
}

val appVersion = properties.getProperty("version")
    ?: IllegalStateException("Something went wrong when retrieving app properties")

val client = initializeKbot()

val guild = client.getGuildById(Snowflake.of(config.getLong("discord.guildId"))).block()
    ?: throw IllegalStateException("Something went wrong when retrieving the guild id. Environment variable GUILD_ID might be missing")

fun initializeKbot(): GatewayDiscordClient {
    val discordToken = config.getString("discord.token")
        ?: throw IllegalStateException("Something went wrong when retrieving the Discord token. Environment variable DISCORD_TOKEN might be missing")

    return DiscordClientBuilder.create(discordToken)
        .build()
        .gateway()
        .setEnabledIntents(IntentSet.of(Intent.GUILD_MEMBERS))
        .login()
        .block()
        ?: throw IllegalStateException("Something went wrong when initializing the Discord bot")
}

val rolesMessageChannelId = Snowflake.of(config.getLong("discord.rolesMessage.channelId"))
    ?: throw IllegalStateException("Something went wrong when retrieving the role selection channel id. Environment variable ROLES_MESSAGE_CHANNEL_ID might be missing")
val rolesMessageId = Snowflake.of(config.getLong("discord.rolesMessage.messageId"))
    ?: throw IllegalStateException("Somethign went wrong when retrieving the role selection message id. Environment variable ROLES_MESSAGE_ID might be missing")