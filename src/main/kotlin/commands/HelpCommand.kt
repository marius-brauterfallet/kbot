package commands

import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

object HelpCommand : Command {
    override val commands = listOf("help")
    override val description = "Lists all available commands"

    override fun execute(event: MessageCreateEvent): Mono<Unit> {
        return event.message.restChannel.createMessage("# Commands\n" + registeredCommands.joinToString("\n") { command ->
            "${command.commands.joinToString { "!$it" }} - ${command.description}"
        }).then(Mono.empty())
    }

}