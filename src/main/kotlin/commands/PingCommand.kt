package commands

import discord4j.core.event.domain.message.MessageCreateEvent

object PingCommand : Command {
    override val name = "ping"

    override fun execute(event: MessageCreateEvent) {
        event.message.channel.subscribe { message ->
            message.createMessage("Pong!").block()
        }
    }
}