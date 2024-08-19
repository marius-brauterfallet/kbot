import constants.Constants.logger
import constants.Constants.properties
import di.appModule
import discord4j.core.GatewayDiscordClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import services.GuildRolesService

fun main() {
    startKoin {
        modules(appModule)
    }

    KbotApp().start()
}

class KbotApp : KoinComponent {
    private val client: GatewayDiscordClient by inject()
    private val guildRolesService: GuildRolesService by inject()

    fun start() {
        logger.info("Launching kbot version ${properties.appVersion}")

//        guildRolesService.updateRoles()
//            .flatMapMany { guildRolesService.updateUserRoles() }
//            .subscribe()

        guildRolesService.updateUserRoles().subscribe()

        registerCommands()
        registerListeners()
        registerScheduledTasks()

        client.onDisconnect().block()
    }
}