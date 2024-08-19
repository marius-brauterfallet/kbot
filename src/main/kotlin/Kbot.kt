import commands.registeredCommands
import constants.Constants.client
import discord4j.core.event.domain.message.MessageCreateEvent
import handlers.roleChangeHandler
import handlers.roleReactionHandler
import reactor.core.publisher.Mono
import tasks.DailyLunchMessageTask.registerDailyLunchMessage
import tasks.registerAttendanceMessageTask
import kotlin.jvm.optionals.getOrNull

fun registerListeners() {
    roleChangeHandler()
    roleReactionHandler()
}

fun registerCommands() { // Ensure that command names are not used multiple times
    registeredCommands.flatMap { it.commands }
        .let { allCommands ->
            val duplicateCommands = allCommands.toSet()
                .map { command -> command to allCommands.count { it == command } }
                .filter { it.second != 1 }
                .map { it.first }

            require(duplicateCommands.isEmpty()) {
                "The following commands are used multiple times: ${duplicateCommands.joinToString(", ") { "'$it'" }}"
            }
        }

    client.on(MessageCreateEvent::class.java) { event ->
        if (event.message.author.getOrNull()?.isBot == true) return@on Mono.empty()

        val content = event.message.content

        val command = registeredCommands.find { command ->
            command.commands.any { commandName ->
                content.trim() == "!${commandName}" || content.startsWith("!${commandName} ")
            }
        } ?: return@on Mono.empty()

        command.execute(event)
            .flatMap { event.message.restMessage.delete("Command executed successfully") }
    }
        .subscribe()
}

fun registerScheduledTasks() {
    registerDailyLunchMessage()
    registerAttendanceMessageTask()
}