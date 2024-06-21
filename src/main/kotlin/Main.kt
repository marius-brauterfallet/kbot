import discord4j.core.DiscordClient
import discord4j.core.event.domain.message.MessageCreateEvent

fun main() {
    val token = System.getenv("kimmoToken")

    val client = DiscordClient.create(token)
    val gateway = client.login().block()

    if (gateway == null) {
        println("Something went wrong??")
        return
    }

    gateway.on(MessageCreateEvent::class.java).subscribe {
        val message = it.message

        if (message.content == "!ping") {
            message.channel.block()?.createMessage("Pong!")?.block()
        }
    }

    gateway.onDisconnect().block()
}