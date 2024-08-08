package commands

import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

object InfoCommand : Command {
    override val commands = listOf("info")
    override val description = "Responds with info about the bot"

    override fun execute(event: MessageCreateEvent): Mono<Unit> {
        return event.message.channel.flatMap { channel ->
            channel.createMessage("This is an instance of kbot version ${constants.appVersion}. You can take a look at my code [here](https://github.com/marius-brauterfallet/kbot)").then(Mono.empty())
        }
    }
}