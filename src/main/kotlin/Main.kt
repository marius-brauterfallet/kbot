import di.appModule
import discord4j.core.GatewayDiscordClient
import handlers.CommandHandler
import handlers.RoleReactionHandler
import handlers.RoleUpdateHandler
import model.KbotProperties
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.slf4j.Logger
import services.GuildRolesService
import tasks.AttendanceMessageTask

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

    private val roleUpdateHandler: RoleUpdateHandler by inject()
    private val roleReactionHandler: RoleReactionHandler by inject()
    private val commandHandler: CommandHandler by inject()

    private val attendanceMessageTask: AttendanceMessageTask by inject()

    fun start() {
        logger.info("Launching kbot version ${properties.appVersion}")

        guildRolesService.updateUserRoles().subscribe()

        registerHandlers()
        registerScheduledTasks()

        client.onDisconnect().block()
    }

    private fun registerHandlers() {
        roleUpdateHandler.init()
        roleReactionHandler.init()
        commandHandler.init()
    }

    private fun registerScheduledTasks() {
        attendanceMessageTask.register()
    }
}