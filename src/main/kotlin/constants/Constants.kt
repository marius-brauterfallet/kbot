package constants

import KbotConfig
import KbotProperties
import discord4j.common.util.Snowflake
import discord4j.core.GatewayDiscordClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory
import java.util.*

object Constants : KoinComponent {
    val client: GatewayDiscordClient by inject()
    val config: KbotConfig by inject()
    val properties: KbotProperties by inject()

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val logger = LoggerFactory.getLogger("kbot") ?: throw Exception("Something went wrong initializing the Logback logger")

    val guild = client.getGuildById(config.guildId).block()
        ?: throw IllegalStateException("Something went wrong when retrieving the guild id. Environment variable GUILD_ID might be missing")
}
