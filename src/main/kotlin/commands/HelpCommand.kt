package commands

import discord4j.core.event.domain.message.MessageCreateEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import reactor.core.publisher.Mono

object HelpCommand: Command, KoinComponent {
    private val registeredCommands: List<Command> by inject()

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