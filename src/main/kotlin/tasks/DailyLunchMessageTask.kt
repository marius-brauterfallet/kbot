package tasks

import constants.Constants.applicationScope
import constants.Constants.client
import constants.Constants.config
import constants.Constants.logger
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import services.LunchService

object DailyLunchMessageTask : KoinComponent {
    private val lunchService: LunchService by inject()

    fun registerDailyLunchMessage() {
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
