package tasks

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

object TaskScheduling {
    fun registerScheduledTasks() {
        DailyLunchMessageTask.register()
        AttendanceMessageTask.register()
    }

    private fun scheduleDailyTask(
        scope: CoroutineScope,
        time: LocalTime,
        timeZone: TimeZone = TimeZone.currentSystemDefault(),
        task: () -> Unit,
    ) {
        val currentDate = Clock.System.now().toLocalDateTime(timeZone).date
        val runTimeToday = LocalDateTime(currentDate, time).toInstant(timeZone)

        val runTimeTodayHasPassed = (runTimeToday - Clock.System.now()).isNegative()

        val nextRunTime = if (runTimeTodayHasPassed) runTimeToday + 24.hours else runTimeToday

        scheduleTask(scope, nextRunTime, 24.hours, task)
    }

    fun scheduleWeekdayTask(
        scope: CoroutineScope,
        time: LocalTime,
        timeZone: TimeZone = TimeZone.currentSystemDefault(),
        task: () -> Unit,
    ) {
        scheduleDailyTask(scope, time, timeZone) {
            val weekdays = listOf(DayOfWeek.SATURDAY, java.time.DayOfWeek.SUNDAY)

            if (Clock.System.now().toLocalDateTime(timeZone).dayOfWeek in weekdays)
                return@scheduleDailyTask

            task.invoke()
        }
    }

    private fun scheduleTask(scope: CoroutineScope, nextRun: Instant, interval: Duration, task: () -> Unit) {
        val firstDelay = nextRun - Clock.System.now()

        scope.launch {
            delay(firstDelay)

            while (true) {
                launch { task.invoke() }
                delay(interval)
            }
        }
    }
}
