package services

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.*
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import model.lunch.MenuItem
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.slf4j.Logger

class LunchServiceImpl(
    private val logger: Logger, private val httpClient: HttpClient
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
            "pizza" to "🍕",
            "chili" to "🌶️"
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

                val menuUrl = aElement.attribute("href").value
                val menuHtml = httpClient.get(menuUrl).bodyAsText()

                val menu = getMenu(menuHtml).getOrDefault("???")

                "## [$canteenName]($menuUrl)\n$menu"
            }
        }.awaitAll().filterNotNull().joinToString("\n\n").let {
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

    private fun getMenu(menuHtml: String): Result<String> {
        val menuItems = parseMenu(menuHtml).getOrElse { return Result.failure(it) }
        val dishesAndAllergens = menuItems.map { menuItem ->
            val emojis = lunchEmojis
                .filter { (ingredientName, _) -> menuItem.description.contains(ingredientName, ignoreCase = true) }
                .sortedBy { (string, _) -> menuItem.description.indexOf(string, ignoreCase = true) }
                .joinToString(" ") { it.second }

            val dishNameWithEmojis = if (emojis.isNotEmpty()) "${menuItem.description} $emojis" else menuItem.description

            "**$dishNameWithEmojis**" to menuItem.allergens
        }


        val completeMenu = dishesAndAllergens.joinToString("\n\n") { (dish, allergens) ->
            val dishAllergens = if (allergens.isNotEmpty()) {
                "\n*Allergens: ${allergens.joinToString(", ")}*"
            } else ""

            dish + dishAllergens
        }

        return Result.success(completeMenu)
    }

    override fun parseMenu(menuHtml: String): Result<List<MenuItem>> {
        val allergenMap = parseAllergens(menuHtml)

        val htmlDocument = Jsoup.parse(menuHtml)

        val menuItems = htmlDocument.descendants()
            .asSequence()
            .dropWhile { !it.ownText().contains(Regex("TODAY'?S LUNCH", RegexOption.IGNORE_CASE)) }
            .drop(1)
            .takeWhile { it.className() != "allergen-holder" }
            .filter { it.ownText().isNotEmpty() }
            .zipWithNext { a, b -> a.ownText() to b.ownText() }
            .filter { !it.first.contains(Regex("(Allergener|Allergens):", RegexOption.IGNORE_CASE)) }
            .map { (itemDescription, allergenText) ->
                val allergens = allergenText.split(":").last().split(",").mapNotNull {
                    val allergenNumber = it.trim().toIntOrNull() ?: return@mapNotNull null
                    allergenMap[allergenNumber]
                }

                MenuItem(itemDescription, allergens)
            }
            .toList()

        return Result.success(menuItems)
    }

    private fun Element.descendants(): List<Element> {
        return listOf(this) + children().flatMap { it.descendants() }
    }

    private fun parseAllergens(menuHtml: String): Map<Int, String> {
        val htmlDocument = Jsoup.parse(menuHtml)
        val allergenHolder = htmlDocument.select(".allergen-holder")

        return allergenHolder.select("td").mapNotNull { allergenCell ->
            val allergenNumber =
                allergenCell.select(".allergen-item-number").text().toIntOrNull() ?: return@mapNotNull null
            val allergenName = allergenCell.select(".allergen-item-name").text().split("/").last()

            allergenNumber to allergenName
        }.toMap()
    }
}