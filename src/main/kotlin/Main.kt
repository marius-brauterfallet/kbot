import constants.appVersion
import constants.client
import constants.logger
import handlers.updateUserRoles
import services.GuildRolesService

fun main() {
    logger.info("Launching kbot version $appVersion")

    GuildRolesService.updateRoles()
        .flatMapMany { updateUserRoles() }
        .subscribe()

    registerCommands()
    registerListeners()
    registerScheduledTasks()

    client.onDisconnect().block()
}