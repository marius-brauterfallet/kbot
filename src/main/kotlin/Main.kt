import constants.appVersion
import constants.client

fun main() {
    println("Launching kbot version $appVersion")

    updateUserRoles().subscribe()

    registerCommands()
    registerListeners()

    client.onDisconnect().block()
}