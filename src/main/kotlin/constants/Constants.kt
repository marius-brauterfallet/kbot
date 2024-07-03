package constants

import com.typesafe.config.ConfigFactory
import discord4j.common.util.Snowflake
import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import discord4j.gateway.intent.Intent
import discord4j.gateway.intent.IntentSet

private val environment = System.getenv("ENV") ?: "default"

val config = when (environment) {
    "dev" -> ConfigFactory.parseResources("application.dev.conf").withFallback(ConfigFactory.load())
    else -> ConfigFactory.load()
} ?: throw IllegalStateException("Could not load bot configuration")

val client = initializeKimmoBot()

val guild = client.getGuildById(Snowflake.of(config.getLong("discord.guildId"))).block()
    ?: throw Exception("Something went wrong when retrieving the guild")

fun initializeKimmoBot(): GatewayDiscordClient {
    return DiscordClientBuilder.create(config.getString("discord.token"))
        .build()
        .gateway()
        .setEnabledIntents(IntentSet.of(Intent.GUILD_MEMBERS))
        .login()
        .block()
        ?: throw IllegalStateException("Something went wrong when initializing the Discord bot")
}

val rolesMessageChannelId = Snowflake.of(config.getLong("discord.rolesMessage.channelId"))
    ?: throw IllegalStateException("Missing config field discord.rolesMessage.channelId")
val rolesMessageId = Snowflake.of(config.getLong("discord.rolesMessage.messageId"))
    ?: throw IllegalStateException("Missing config field discord.rolesMessage.messageId")