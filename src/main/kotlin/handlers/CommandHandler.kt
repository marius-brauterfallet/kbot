package handlers

import commands.Command
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono
import kotlin.jvm.optionals.getOrNull

class CommandHandler(
    private val client: GatewayDiscordClient,
    private val commands: List<Command>
) {
    fun init() { // Ensure that command names are not used multiple times
        commands.flatMap { it.commands }
            .let { allCommands ->
                val duplicateCommands = allCommands.toSet()
                    .map { command -> command to allCommands.count { it == command } }
                    .filter { it.second != 1 }
                    .map { it.first }

                require(duplicateCommands.isEmpty()) {
                    "The following command names are used multiple times: ${duplicateCommands.joinToString(", ") { "'$it'" }}"
                }
            }

        client.on(MessageCreateEvent::class.java) { event ->
            if (event.message.author.getOrNull()?.isBot == true) return@on Mono.empty()

            val content = event.message.content

            val command = commands.find { command ->
                command.commands.any { commandName ->
                    content.trim() == "!${commandName}" || content.startsWith("!${commandName} ")
                }
            } ?: return@on Mono.empty()

            command.execute(event)
        }
            .subscribe()
    }
}