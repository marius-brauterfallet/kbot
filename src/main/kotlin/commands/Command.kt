package commands

import discord4j.core.event.domain.message.MessageCreateEvent

interface Command {
    val name: String
    fun execute(event: MessageCreateEvent)
}