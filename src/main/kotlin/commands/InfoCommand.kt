package commands

import constants.Constants.appVersion
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

object InfoCommand : Command {
    override val commands = listOf("info")
    override val description = "Responds with info about the bot"

    override fun execute(event: MessageCreateEvent): Mono<Unit> =
        event.message.restChannel.createMessage("This is an instance of kbot version $appVersion")
            .thenReturn(Unit)
}