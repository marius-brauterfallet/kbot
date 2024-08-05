package tasks

import constants.applicationScope
import constants.client
import constants.dailyUpdatesChannelId
import constants.logger
import kotlinx.datetime.LocalTime
import services.LunchService.getMenus

fun registerDailyLunchMessage() {
    scheduleWeekdayTask(applicationScope, LocalTime(9, 0)) {
        val menusMessage = getMenus().getOrElse { exception ->
            "Could not retrieve today's lunch menus: ${exception.message}".also(logger::error)
        }

        client.getChannelById(dailyUpdatesChannelId)
            .flatMap { channel -> channel.restChannel.createMessage(menusMessage) }
            .doOnError { logger.error("Failed to send message: ${it.message}") }
            .block()
    }
}