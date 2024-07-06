package commands

import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

interface Command {
    val name: String
    fun execute(event: MessageCreateEvent): Mono<Unit>
}