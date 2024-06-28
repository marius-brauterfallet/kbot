import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

fun loadConfig(): Config {
    val environment = System.getenv("ENV") ?: "default"

    return runCatching {
        when (environment) {
            "dev" -> ConfigFactory.parseResources("application.dev.conf").withFallback(ConfigFactory.load())
            else -> ConfigFactory.load()
        }
    }.getOrElse { throw IllegalStateException("Some environment or config variables might be messing!") }
}