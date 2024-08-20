package tasks

import discord4j.core.GatewayDiscordClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import model.KbotConfig
import org.slf4j.Logger
import services.LunchService
import tasks.TaskScheduling.scheduleWeekdayTask

class DailyLunchMessageTask(
    private val applicationScope: CoroutineScope,
    private val client: GatewayDiscordClient,
    private val lunchService: LunchService,
    private val config: KbotConfig,
    private val logger: Logger,
) {
    fun register() {
        scheduleWeekdayTask(applicationScope, LocalTime(9, 0), TimeZone.of("Europe/Oslo")) {
            val menusMessage = lunchService.getMenus(true).getOrElse { exception ->
                "Could not retrieve today's lunch menus: ${exception.message}".also(logger::error)
            }

            client.getChannelById(config.dailyUpdatesChannelId)
                .flatMap { channel -> channel.restChannel.createMessage(menusMessage) }
                .doOnError { logger.error("Failed to send message: ${it.message}") }
                .block()
        }
    }
}
