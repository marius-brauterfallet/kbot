import commands.Commands.registerCommands
import di.appModule
import discord4j.core.GatewayDiscordClient
import handlers.Handlers.registerHandlers
import model.KbotProperties
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.slf4j.Logger
import services.GuildRolesService
import tasks.TaskScheduling.registerScheduledTasks

fun main() {
    startKoin {
        modules(appModule)
    }

    KbotApp().start()
}

class KbotApp : KoinComponent {
    private val client: GatewayDiscordClient by inject()
    private val guildRolesService: GuildRolesService by inject()
    private val logger: Logger by inject()
    private val properties: KbotProperties by inject()

    fun start() {
        logger.info("Launching kbot version ${properties.appVersion}")

        guildRolesService.updateUserRoles().subscribe()

        registerCommands()
        registerHandlers()
        registerScheduledTasks()

        client.onDisconnect().block()
    }
}