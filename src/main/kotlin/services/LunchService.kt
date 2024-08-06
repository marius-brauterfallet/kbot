package services

import constants.logger
import org.jsoup.Jsoup

object LunchService {
    private const val MENU_URL =
        "https://widget.inisign.com/Widget/Customers/Customer.aspx?token=c5a641de-e74e-48eb-be4e-d847f261ec11"

    private val lunchEmojis = listOf(
        "wings" to "🍗",
        "soup" to "🍲",
        "shrimp" to "🦐",
        "scampi" to "🦐",
        "rice" to "🍚",
        "chicken" to "🍗",
        "pasta" to "🍝",
        "pork" to "🥩",
        "bacon" to "🥓",
        "beef" to "🍖",
        "potato" to "🥔",
        "salmon" to "🐟",
        "stew" to "🍲",
        "sausage" to "🌭",
        "pizza" to "🍕"
    )

    fun getMenus(): Result<String> {
        val document = Jsoup.connect(MENU_URL).get()

        val menusDiv = document
            .select("h3")
            .find { it.text().lowercase().contains("todays menu") }
            ?.parent()
            ?: return Result.failure(Exception())

        val kotlinPattern = Regex("\\S+/lnk_(\\S+).jpg")

        val menus = menusDiv.select("div.link-item").map { div ->
            val aElement = div.selectFirst("a") ?: return@map null
            val imgElement = div.selectFirst("img") ?: return@map null
            val srcAttribute = imgElement.attribute("src").value

            val canteenName = kotlinPattern.find(srcAttribute)?.groups?.get(1)?.value?.replace('_', ' ')
                ?: "Unknown canteen"

            val menu = getMenu(aElement.attribute("href").value).getOrDefault("???")

            "# $canteenName\n$menu"
        }.joinToString("\n\n")

        return Result.success(menus)
    }

    private fun getMenu(url: String): Result<String> {
        val document = Jsoup.connect(url).get()

        val menuDiv = document
            .select("h1")
            .find { it.text().lowercase().contains(Regex("today'?s lunch")) }
            ?.parent()
            ?: return Result.failure<String>(Exception("Could not find lunch container"))
                .also { logger.error(it.exceptionOrNull()?.message) }

        val menu = menuDiv.select("h2").joinToString("\n") { menuElement ->
            val line = menuElement.text()
            val lineLower = line.lowercase()
            val emojis = lunchEmojis.filter { lineLower.contains(it.first) }.joinToString(" ") { it.second }

            "$line $emojis"
        }

        return Result.success(menu)
    }
}