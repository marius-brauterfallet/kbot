package services

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.*
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import org.jsoup.Jsoup
import org.slf4j.Logger

class LunchServiceImpl(
    private val logger: Logger,
    private val httpClient: HttpClient,
    private val coroutineScope: CoroutineScope
) : LunchService {
    companion object {
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
    }


    override suspend fun getMenus(withDate: Boolean): Result<String> = coroutineScope {
        val htmlMenu = httpClient.get(MENU_URL).bodyAsText()
        val document = Jsoup.parse(htmlMenu)

        val menusDiv = document.select("h3").find { it.text().lowercase().contains("todays menu") }?.parent()
            ?: return@coroutineScope Result.failure(Exception())

        val kotlinPattern = Regex("\\S+/lnk_(\\S+).jpg")

        val menus = menusDiv.select("div.link-item").map { div ->
            async {
                val aElement = div.selectFirst("a") ?: return@async null
                val imgElement = div.selectFirst("img") ?: return@async null
                val srcAttribute = imgElement.attribute("src").value

                val canteenName =
                    kotlinPattern.find(srcAttribute)?.groups?.get(1)?.value?.replace('_', ' ') ?: "Unknown canteen"

                val menu = getMenu(aElement.attribute("href").value).getOrDefault("???")

                "## $canteenName\n$menu"
            }
        }
            .awaitAll()
            .filterNotNull()
            .joinToString("\n\n").let {
            if (withDate) {
                val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                val dateFormat = LocalDate.Format {
                    dayOfMonth()
                    chars(". ")
                    monthName(MonthNames.ENGLISH_FULL)
                    char(' ')
                    year()
                }

                "# Lunch menu ${currentDate.format(dateFormat)}\n$it"
            } else it
        }

        Result.success(menus)
    }

    private suspend fun getMenu(url: String): Result<String> {
        val menuHtml = httpClient.get(url).bodyAsText()
        val document = Jsoup.parse(menuHtml)

        val menuDiv = document.select("h1").find { it.text().lowercase().contains(Regex("today'?s lunch")) }?.parent()
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