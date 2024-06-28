import com.typesafe.config.ConfigFactory

fun main() {
    val environment = System.getenv("ENV") ?: "default"

    val config = when (environment) {
        "dev" -> ConfigFactory.parseResources("application.dev.conf").withFallback(ConfigFactory.load())
        else -> ConfigFactory.load()
    }

    startKimmoBot(config.getString("discord.token")) {}
}