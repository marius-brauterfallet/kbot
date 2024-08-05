import constants.appVersion
import constants.client
import constants.logger
import handlers.updateUserRoles

fun main() {
    logger.info("Launching kbot version $appVersion")

    GuildRoles.updateRoles()
        .flatMapMany { updateUserRoles() }
        .subscribe()

    registerCommands()
    registerListeners()
    registerScheduledTasks()

    client.onDisconnect().block()
}