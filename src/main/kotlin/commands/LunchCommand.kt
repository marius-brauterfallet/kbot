package commands

import discord4j.core.event.domain.message.MessageCreateEvent
import org.jsoup.Jsoup
import reactor.core.publisher.Mono
import java.util.regex.Pattern

object LunchCommand : Command {
    private const val MENU_URL =
        "https://widget.inisign.com/Widget/Customers/Customer.aspx?token=c5a641de-e74e-48eb-be4e-d847f261ec11"
    override val commands = listOf("lunch", "lunsj")

    override fun execute(event: MessageCreateEvent): Mono<Unit> {
        val menus = getMenus().getOrElse {
            return event.message.channel.flatMap { it.createMessage("Menus is unavailable!").then(Mono.empty()) }
        }

        return event.message.restChannel.createMessage(menus).then(Mono.empty())
    }

    private fun getMenus(): Result<String> {
        val document = Jsoup.connect(MENU_URL).get()

        val menusDiv = document
            .select("h3")
            .find { it.text().lowercase().contains("todays menu") }
            ?.parent()
            ?: return Result.failure(Exception())

        val canteenNamePattern = Pattern.compile("\\S+/lnk_(\\S+).jpg")

        val menus = menusDiv.select("div.link-item").map { div ->
            val aElement = div.selectFirst("a") ?: return@map null
            val imgElement = div.selectFirst("img") ?: return@map null
            val srcAttribute = imgElement.attribute("src").value

            val canteenNameMatcher = canteenNamePattern.matcher(srcAttribute)

            val canteenName = if (canteenNameMatcher.find()) {
                canteenNameMatcher.group(1).replace('_', ' ')
            } else "Unknown canteen"

            val menu = getMenu(aElement.attribute("href").value).getOrDefault("???")

            "# $canteenName\n$menu"
        }.joinToString("\n\n")

        return Result.success(menus)
    }

    private fun getMenu(url: String): Result<String> {
        val document = Jsoup.connect(url).get()

        val menuDiv = document
            .select("h1")
            .find { it.text().lowercase().contains("todays lunch") }
            ?.parent()
            ?: return Result.failure(Exception("Could not find lunch container"))

        val menu = menuDiv.select("h2").joinToString("\n") { it.text() }

        return Result.success(menu)
    }

}