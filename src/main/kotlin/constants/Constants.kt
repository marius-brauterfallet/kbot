package constants

import com.typesafe.config.ConfigFactory
import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient

private val environment = System.getenv("ENV") ?: "default"

val config = runCatching {
    when (environment) {
        "dev" -> ConfigFactory.parseResources("application.dev.conf").withFallback(ConfigFactory.load())
            ?: throw Exception()

        else -> ConfigFactory.load() ?: throw Exception()
    }
}.getOrElse { throw IllegalStateException("Some environment or config variables might be missing!") }

val client = DiscordClient.create(config.getString("discord.token")).login().block()
    ?: throw IllegalStateException("Something went wrong when initializing the Discord client")

val guild = client.getGuildById(Snowflake.of(config.getLong("discord.guildId"))).block()
    ?: throw Exception("Something went wrong when retrieving the guild")
