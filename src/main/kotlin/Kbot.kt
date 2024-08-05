import commands.Command
import commands.InfoCommand
import commands.LunchCommand
import commands.LunchCommand.getMenus
import commands.PingCommand
import constants.*
import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.gateway.intent.IntentSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import reactor.core.publisher.Mono
import kotlin.jvm.optionals.getOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours


fun initializeKbot(): GatewayDiscordClient {
    val discordToken = config.getString("discord.token")
        ?: throw IllegalStateException("Something went wrong when retrieving the Discord token. Environment variable DISCORD_TOKEN might be missing")

    return DiscordClientBuilder.create(discordToken)
        .build()
        .gateway()
        .setEnabledIntents(IntentSet.all())
        .login()
        .block()
        ?: throw IllegalStateException("Something went wrong when initializing the Discord bot")
}


fun registerListeners() {
    roleChangeHandler()
    roleReactionHandler()
}


fun registerCommands() {
    val commands: List<Command> = listOf(PingCommand, InfoCommand, LunchCommand)

    // Ensure that command names are not used multiple times
    commands.flatMap { it.commands }
        .let { allCommands ->
            val duplicateCommands = allCommands
                .toSet()
                .map { command -> command to allCommands.count { it == command } }
                .filter { it.second != 1 }
                .map { it.first }

            require(duplicateCommands.isEmpty()) {
                "The following commands are used multiple times: ${duplicateCommands.joinToString(", ") { "'$it'" }}"
            }
        }

    client.on(MessageCreateEvent::class.java) { event ->
        if (event.message.author.getOrNull()?.isBot == true) return@on Mono.empty()

        val content = event.message.content

        commands.find { command ->
            command.commands.any { commandName ->
                content.trim() == "!${commandName}" || content.startsWith("!${commandName} ")
            }
        }?.execute(event) ?: Mono.empty()
    }.subscribe()
}

fun registerScheduledTasks() {
    registerDailyLunchMessage()
}

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