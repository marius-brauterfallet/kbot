package di

import com.typesafe.config.ConfigFactory
import commands.HelpCommand
import commands.InfoCommand
import commands.LunchCommand
import commands.PingCommand
import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import discord4j.gateway.intent.IntentSet
import handlers.CommandHandler
import handlers.RoleReactionHandler
import handlers.RoleUpdateHandler
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import model.KbotConfig
import model.KbotProperties
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.slf4j.LoggerFactory
import services.GuildRolesService
import services.GuildRolesServiceImpl
import services.LunchService
import services.LunchServiceImpl
import tasks.AttendanceMessageTask
import tasks.DailyLunchMessageTask
import java.util.*

val appModule = module {
    single {
        val environment = System.getenv("ENV") ?: "default"

        val config = when (environment) {
            "dev" -> ConfigFactory.parseResources("application.dev.conf")
                .withFallback(ConfigFactory.load())

            else -> ConfigFactory.load()
        } ?: throw IllegalStateException("Could not load bot configuration")

        KbotConfig(config, environment)
    }

    single {
        val config: KbotConfig = get()

        DiscordClientBuilder.create(config.discordToken)
            .build()
            .gateway()
            .setEnabledIntents(IntentSet.all())
            .login()
            .block() ?: throw IllegalStateException("Something went wrong when initializing the Discord bot")
    }

    single {
        val properties = Properties().apply {
            load(Thread.currentThread().contextClassLoader.getResourceAsStream("application.properties"))
        }

        KbotProperties(properties)
    }

    single {
        val client: GatewayDiscordClient = get()
        val config: KbotConfig = get()

        client.getGuildById(config.guildId)
            .block()
            ?: throw IllegalStateException("Something went wrong when retrieving the guild id. Environment variable GUILD_ID might be missing")
    }

    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    single { LoggerFactory.getLogger("kbot") ?: throw Exception("Something went wrong initializing the Logback logger") }

    // Services
    singleOf(::LunchServiceImpl) bind LunchService::class
    singleOf(::GuildRolesServiceImpl) bind GuildRolesService::class

    // Handlers
    singleOf(::CommandHandler)
    singleOf(::RoleReactionHandler)
    singleOf(::RoleUpdateHandler)

    // Tasks
    singleOf(::DailyLunchMessageTask)
    singleOf(::AttendanceMessageTask)

    // Commands
    single {
        listOf(
            HelpCommand,
            InfoCommand,
            LunchCommand,
            PingCommand
        )
    }

    single { HttpClient(CIO) }
}