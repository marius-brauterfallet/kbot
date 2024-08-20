package commands

import discord4j.core.event.domain.message.MessageCreateEvent
import model.KbotProperties
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import reactor.core.publisher.Mono

object InfoCommand: Command, KoinComponent {
    private val properties: KbotProperties by inject()

    override val commands = listOf("info")
    override val description = "Responds with info about the bot"

    override fun execute(event: MessageCreateEvent): Mono<Unit> =
        event.message.restChannel.createMessage("This is an instance of kbot version ${properties.appVersion}")
            .thenReturn(Unit)
}