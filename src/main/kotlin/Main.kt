import constants.client

fun main() {
    updateUserRoles().subscribe()

    registerCommands()
    registerListeners()

    client.onDisconnect().block()
}