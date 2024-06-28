import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient

fun startKimmoBot(token: String, block: GatewayDiscordClient.() -> Unit) {
    DiscordClient.create(token).login().block()?.run {
        block()

        onDisconnect().block()
    }
}