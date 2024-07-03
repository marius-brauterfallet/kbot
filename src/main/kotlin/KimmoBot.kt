import commands.Command
import commands.PingCommand
import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.`object`.entity.Guild
import reactor.core.publisher.Mono
import kotlin.jvm.optionals.getOrNull

fun kimmoBotInit() {
    val client = DiscordClient.create(KimmoConfig.config.getString("discord.token")).login().block()
        ?: throw IllegalStateException("Something went wrong when initializing the Discord client")

    val guildId = Snowflake.of(KimmoConfig.config.getLong("discord.guildId"))
    val guild = client.getGuildById(guildId).block()
        ?: throw Exception("Something went wrong when retrieving the guild")

    registerCommands(client)
    registerListeners(client, guild)

    client.onDisconnect().block()
}


fun registerListeners(client: GatewayDiscordClient, guild: Guild) {
    val messageId = KimmoConfig.config.getLong("discord.rolesMessage.messageId")

    roleReactionHandler(client, messageId, guild)
}


fun registerCommands(client: GatewayDiscordClient) {
    val commands: List<Command> = listOf(PingCommand)

    client.on(MessageCreateEvent::class.java) { event ->
        if (event.message.author.getOrNull()?.isBot == true) return@on Mono.empty()

        val content = event.message.content

        commands
            .find { command -> content.trim() == "!${command.name}" || content.startsWith("!${command.name} ") }
            ?.execute(event)

        Mono.empty<Unit>()
    }.subscribe()
}