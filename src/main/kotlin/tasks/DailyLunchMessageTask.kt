package tasks

import discord4j.core.GatewayDiscordClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import model.KbotConfig
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import services.LunchService
import tasks.TaskScheduling.scheduleWeekdayTask

object DailyLunchMessageTask : KoinComponent {
    private val lunchService: LunchService by inject()
    private val applicationScope: CoroutineScope by inject()
    private val client: GatewayDiscordClient by inject()
    private val config: KbotConfig by inject()
    private val logger: Logger by inject()

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
