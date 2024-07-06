import constants.appVersion
import constants.client
import constants.logger

fun main() {
    logger.info("Launching kbot version $appVersion")

    GuildRoles.updateRoles().subscribe {
        updateUserRoles().subscribe()
    }

    registerCommands()
    registerListeners()

    client.onDisconnect().block()
}