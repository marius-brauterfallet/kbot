import discord4j.core.DiscordClient
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.event.domain.message.ReactionAddEvent
import discord4j.core.event.domain.message.ReactionRemoveEvent

fun main() {
    val token = System.getenv("KIMMOBOT_TOKEN")

    val client = DiscordClient.create(token)
    val gateway = client.login().block()

    if (gateway == null) {
        println("Something went wrong??")
        return
    }

    gateway.on(MessageCreateEvent::class.java).subscribe {
        val message = it.message

        if (message.content == "!ping") {
            message.channel.block()?.createMessage("Pong!")?.block()
        }
    }

    gateway.on(ReactionAddEvent::class.java).subscribe { event ->
        val message = event.messageId
        val emoji = event.emoji

        if (message.asLong() == Constants.rolesMessageId) {
            val guild = event.guild.block() ?: return@subscribe
            val member = event.user.block()?.asMember(guild.id)?.block() ?: return@subscribe
            val roles = guild.roles.collectList().block() ?: return@subscribe
            val roleName = Constants.roleEmojis[emoji.asUnicodeEmoji().get().raw] ?: return@subscribe

            runCatching {
                member.addRole(roles.single { it.name == roleName }.id).block()
            }
        }
    }

    gateway.on(ReactionRemoveEvent::class.java).subscribe { event ->
        val message = event.messageId
        val emoji = event.emoji

        if (message.asLong() == Constants.rolesMessageId) {
            val guild = event.guild.block() ?: return@subscribe
            val member = event.user.block()?.asMember(guild.id)?.block() ?: return@subscribe
            val roles = guild.roles.collectList().block() ?: return@subscribe
            val roleName = Constants.roleEmojis[emoji.asUnicodeEmoji().get().raw] ?: return@subscribe

            runCatching {
                member.removeRole(roles.single { it.name == roleName }.id).block()
            }
        }
    }

    gateway.onDisconnect().block()
}