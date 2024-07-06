package commands

import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

object InfoCommand : Command {
    override val name = "info"

    override fun execute(event: MessageCreateEvent): Mono<Unit> {
        return event.message.channel.flatMap { channel ->
            channel.createMessage("This is an instance of kbot version ${constants.appVersion}").then(Mono.empty())
        }
    }
}