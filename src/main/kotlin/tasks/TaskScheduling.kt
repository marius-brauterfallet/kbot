package tasks

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

fun scheduleDailyTask(scope: CoroutineScope, time: LocalTime, task: () -> Unit) {
    val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val runTimeToday = LocalDateTime(currentDate, time).toInstant(TimeZone.currentSystemDefault())

    if ((runTimeToday - Clock.System.now()).isPositive()) {
        scheduleTask(scope, runTimeToday, 24.hours, task)
    } else {
        scheduleTask(scope, runTimeToday + 24.hours, 24.hours, task)
    }
}

fun scheduleWeekdayTask(scope: CoroutineScope, time: LocalTime, task: () -> Unit) {
    scheduleDailyTask(scope, time) {
        if (Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).dayOfWeek in listOf(
                DayOfWeek.SATURDAY,
                java.time.DayOfWeek.SUNDAY
            )
        ) return@scheduleDailyTask

        task.invoke()
    }
}

fun scheduleTask(scope: CoroutineScope, nextRun: Instant, interval: Duration, task: () -> Unit) {
    val firstDelay = nextRun - Clock.System.now()

    scope.launch {
        delay(firstDelay)

        while (true) {
            launch { task.invoke() }
            delay(interval)
        }
    }
}