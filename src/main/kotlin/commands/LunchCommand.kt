package commands

import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono
import services.LunchService.getMenus

object LunchCommand : Command {
    override val commands = listOf("lunch", "lunsj")
    override val description = "Responds with today's lunch menus"

    override fun execute(event: MessageCreateEvent): Mono<Unit> {
        val menus = getMenus().getOrElse {
            return event.message.channel.flatMap { it.createMessage("Menus is unavailable!").then(Mono.empty()) }
        }

        return event.message.restChannel.createMessage(menus).then(Mono.empty())
    }
}