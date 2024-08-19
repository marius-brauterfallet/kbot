package commands

import commands.Commands.registeredCommands
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

object HelpCommand : Command {
    override val commands = listOf("help")
    override val description = "Lists all available commands"

    override fun execute(event: MessageCreateEvent): Mono<Unit> {
        val commandsList = registeredCommands.joinToString("\n") { command ->
            "${command.commands.joinToString { "!$it" }} - ${command.description}"
        }

        val message = "# Commands\n$commandsList"

        return event.message.restChannel.createMessage(message)
            .thenReturn(Unit)
    }
}