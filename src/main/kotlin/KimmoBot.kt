import com.typesafe.config.Config
import commands.Command
import commands.PingCommand
import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.event.domain.message.ReactionAddEvent
import discord4j.core.event.domain.message.ReactionRemoveEvent
import reactor.core.publisher.Mono
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull

fun kimmoBotInit(config: Config) {
    val client = DiscordClient.create(config.getString("discord.token")).login().block()
        ?: throw IllegalStateException("Something went wrong when initializing the Discord client")

    registerCommands(client)
    registerListeners(client, config)

    client.onDisconnect().block()
}

fun registerListeners(client: GatewayDiscordClient, config: Config) {
    val messageId = config.getLong("discord.rolesMessage.messageId")

    roleReactionHandler(client, messageId)
}

fun roleReactionHandler(client: GatewayDiscordClient, roleMessageId: Long) {
    client.on(ReactionAddEvent::class.java) { event ->
        if (event.messageId.asLong() != roleMessageId) return@on Mono.empty()

        val emoji = event.emoji.asUnicodeEmoji().getOrElse { return@on Mono.empty() }.raw

        val (roleName, _) = Resources.userRoles.find { role -> role.emoji == emoji } ?: return@on Mono.empty()

        val member = event.member.getOrElse { return@on Mono.empty() }

        event.guild
            .flatMap { guild -> guild.roles.filter { role -> role.name == roleName }.singleOrEmpty() }
            .flatMap { role -> member.addRole(role.id) }
            .then(Mono.empty<Unit>())
    }.subscribe()

    client.on(ReactionRemoveEvent::class.java) { event ->
        if (event.messageId.asLong() != roleMessageId) return@on Mono.empty()

        val emoji = event.emoji.asUnicodeEmoji().getOrElse { return@on Mono.empty() }.raw

        val (roleName, _) = Resources.userRoles.find { role -> role.emoji == emoji } ?: return@on Mono.empty()

        val guildId = event.guildId.getOrElse { return@on Mono.empty() }

        val member = event.user.flatMap { it.asMember(guildId) }

        event.guild
            .flatMap { guild -> guild.roles.filter { role -> role.name == roleName }.singleOrEmpty() }
            .flatMap { role -> member.flatMap { it.removeRole(role.id) } }
            .then(Mono.empty<Unit>())
    }.subscribe()
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