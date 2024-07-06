import commands.Command
import commands.InfoCommand
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
    roleReactionHandler()
}


fun registerCommands() {
    val commands: List<Command> = listOf(PingCommand, InfoCommand)

    client.on(MessageCreateEvent::class.java) { event ->
        if (event.message.author.getOrNull()?.isBot == true) return@on Mono.empty()

        val content = event.message.content

        commands
            .find { command -> content.trim() == "!${command.name}" || content.startsWith("!${command.name} ") }
            ?.execute(event) ?: Mono.empty()
    }.subscribe()
}