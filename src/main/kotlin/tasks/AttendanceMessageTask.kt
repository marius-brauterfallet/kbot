package tasks

import constants.Constants.applicationScope
import constants.Constants.client
import constants.Constants.config
import constants.Constants.logger
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone

fun registerAttendanceMessageTask() {
    scheduleWeekdayTask(applicationScope, LocalTime(15, 0), TimeZone.of("Europe/Oslo")) {
        client.getChannelById(config.dailyUpdatesChannelId)
            .flatMap { channel ->
                channel.restChannel.createMessage("Where will you be working from the next working day? React to this message with an appropriate emoji!")
            }
            .doOnError { logger.error("Failed to send message: ${it.message}") }
            .block()
    }
}