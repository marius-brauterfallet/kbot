import com.typesafe.config.ConfigFactory

object KimmoConfig {
    private val environment = System.getenv("ENV") ?: "default"

    val config = runCatching {
        when (environment) {
            "dev" -> ConfigFactory.parseResources("application.dev.conf").withFallback(ConfigFactory.load()) ?: throw Exception()
            else -> ConfigFactory.load() ?: throw Exception()
        }
    }.getOrElse { throw IllegalStateException("Some environment or config variables might be missing!") }
}