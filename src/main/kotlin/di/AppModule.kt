package di

import KbotConfig
import KbotProperties
import com.typesafe.config.ConfigFactory
import discord4j.core.DiscordClientBuilder
import discord4j.gateway.intent.IntentSet
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import services.LunchService
import services.LunchServiceImpl
import java.util.*

val appModule = module {
    single {
        val environment = System.getenv("ENV") ?: "default"

        val config = when (environment) {
            "dev" -> ConfigFactory.parseResources("application.dev.conf").withFallback(ConfigFactory.load())
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

    singleOf<LunchService>(::LunchServiceImpl)
}