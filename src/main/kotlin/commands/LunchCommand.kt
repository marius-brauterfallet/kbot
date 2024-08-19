package commands

import discord4j.core.event.domain.message.MessageCreateEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import reactor.core.publisher.Mono
import services.LunchService

object LunchCommand : Command, KoinComponent {
    private val lunchService: LunchService by inject()

    override val commands = listOf("lunch", "lunsj", "menu", "meny")
    override val description = "Responds with today's lunch menus"

    override fun execute(event: MessageCreateEvent): Mono<Unit> {
        val message = lunchService.getMenus(false).getOrDefault("Menus is unavailable!")

        return event.message.restChannel.createMessage(message)
            .thenReturn(Unit)
    }
}