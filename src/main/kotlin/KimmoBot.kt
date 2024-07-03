import commands.Command
import commands.PingCommand
import constants.client
import constants.config
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono
import kotlin.jvm.optionals.getOrNull

fun kimmoBotInit() {
    registerCommands()
    registerListeners()

    client.onDisconnect().block()
}


fun registerListeners() {
    val messageId = config.getLong("discord.rolesMessage.messageId")

    roleReactionHandler(messageId)
}


fun registerCommands() {
    val commands: List<Command> = listOf(PingCommand)

    client.on(MessageCreateEvent::class.java) { event ->
        if (event.message.author.getOrNull()?.isBot == true) return@on Mono.empty()

        val content = event.message.content

        commands
            .find { command -> content.trim() == "!${command.name}" || content.startsWith("!${command.name} ") }
            ?.execute(event)
    }.subscribe()
}