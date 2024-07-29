package commands

import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

object PingCommand : Command {
    override val commands = listOf("ping")

    override fun execute(event: MessageCreateEvent): Mono<Unit> {
        return event.message.channel.flatMap { channel ->
            channel.createMessage("Pong!").then(Mono.empty())
        }
    }
}