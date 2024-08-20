package tasks

import discord4j.core.GatewayDiscordClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import model.KbotConfig
import org.slf4j.Logger
import tasks.TaskScheduling.scheduleWeekdayTask

class AttendanceMessageTask(
    private val applicationScope: CoroutineScope,
    private val client: GatewayDiscordClient,
    private val config: KbotConfig,
    private val logger: Logger,
) {
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