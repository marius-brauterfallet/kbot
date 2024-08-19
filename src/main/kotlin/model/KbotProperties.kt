package model

import java.util.*

class KbotProperties(properties: Properties) {
    val appVersion = properties.getProperty("version")
        ?: throw IllegalStateException("Something went wrong when retrieving app properties")
}