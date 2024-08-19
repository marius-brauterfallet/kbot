import constants.Constants.appVersion
import constants.Constants.logger
import di.appModule
import discord4j.core.GatewayDiscordClient
import handlers.updateUserRoles
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin

fun main() {
    startKoin {
        modules(appModule)
    }

    KbotApp().start()
}

class KbotApp : KoinComponent {
    private val client: GatewayDiscordClient by inject()

    fun start() {
        logger.info("Launching kbot version $appVersion")

        GuildRoles.updateRoles()
            .flatMapMany { updateUserRoles() }
            .subscribe()

        registerCommands()
        registerListeners()
        registerScheduledTasks()

        client.onDisconnect().block()
    }
}