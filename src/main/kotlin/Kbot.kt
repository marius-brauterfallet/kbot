import commands.Command
import commands.InfoCommand
import commands.LunchCommand
import commands.PingCommand
import constants.client
import constants.config
import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.gateway.intent.IntentSet
import reactor.core.publisher.Mono
import kotlin.jvm.optionals.getOrNull


fun initializeKbot(): GatewayDiscordClient {
    val discordToken = config.getString("discord.token")
        ?: throw IllegalStateException("Something went wrong when retrieving the Discord token. Environment variable DISCORD_TOKEN might be missing")

    return DiscordClientBuilder.create(discordToken)
        .build()
        .gateway()
        .setEnabledIntents(IntentSet.all())
        .login()
        .block()
        ?: throw IllegalStateException("Something went wrong when initializing the Discord bot")
}


fun registerListeners() {
    roleChangeHandler()
    roleReactionHandler()
}


fun registerCommands() {
    val commands: List<Command> = listOf(PingCommand, InfoCommand, LunchCommand)

    // Ensure that command names are not used multiple times
    commands.flatMap { it.commands }
        .let { allCommands ->
            val duplicateCommands = allCommands
                .toSet()
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

        commands.find { command ->
            command.commands.any { commandName ->
                content.trim() == "!${commandName}" || content.startsWith("!${commandName} ")
            }
        }?.execute(event) ?: Mono.empty()
    }.subscribe()
}