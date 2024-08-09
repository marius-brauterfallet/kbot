package commands

import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

object PingCommand : Command {
    override val commands = listOf("ping")
    override val description = "Responds with 'Pong!'"

    override fun execute(event: MessageCreateEvent): Mono<Unit> = event.message.channel.flatMap { channel ->
        channel.createMessage("Pong!")
            .thenReturn(Unit)
    }
}