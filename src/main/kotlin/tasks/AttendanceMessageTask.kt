package tasks

import discord4j.core.GatewayDiscordClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import model.KbotConfig
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import tasks.TaskScheduling.scheduleWeekdayTask

object AttendanceMessageTask : KoinComponent {
    private val applicationScope: CoroutineScope by inject()
    private val client: GatewayDiscordClient by inject()
    private val config: KbotConfig by inject()
    private val logger: Logger by inject()

    fun register() {
        scheduleWeekdayTask(applicationScope, LocalTime(15, 0), TimeZone.of("Europe/Oslo")) {
            client.getChannelById(config.dailyUpdatesChannelId)
                .flatMap { channel ->
                    channel.restChannel.createMessage("Where will you be working from the next working day? React to this message with an appropriate emoji!")
                }
                .doOnError { logger.error("Failed to send message: ${it.message}") }
                .block()
        }
    }
}