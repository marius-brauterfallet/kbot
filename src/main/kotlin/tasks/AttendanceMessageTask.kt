package tasks

import constants.applicationScope
import constants.client
import constants.dailyUpdatesChannelId
import constants.logger
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone

fun registerAttendanceMessageTask() {
    scheduleWeekdayTask(applicationScope, LocalTime(15, 0), TimeZone.of("Europe/Oslo")) {
        client.getChannelById(dailyUpdatesChannelId)
            .flatMap { channel ->
                channel.restChannel.createMessage("Where will you be working from the next working day? React to this message with an appropriate emoji!")
            }
            .doOnError { logger.error("Failed to send message: ${it.message}") }
            .block()
    }
}