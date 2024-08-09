package commands

import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono
import services.LunchService.getMenus

object LunchCommand : Command {
    override val commands = listOf("lunch", "lunsj", "menu", "meny")
    override val description = "Responds with today's lunch menus"

    override fun execute(event: MessageCreateEvent): Mono<Unit> {
        val message = getMenus().getOrDefault("Menus is unavailable!")

        return event.message.restChannel.createMessage(message)
            .thenReturn(Unit)
    }
}