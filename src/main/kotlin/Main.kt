import com.typesafe.config.ConfigFactory
import discord4j.core.DiscordClient

fun main() {
    val config = loadConfig()

    kimmoBotInit(config)
}