package commands

import constants.logger
import discord4j.core.event.domain.message.MessageCreateEvent
import org.jsoup.Jsoup
import reactor.core.publisher.Mono

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

            val canteenName = kotlinPattern.find(srcAttribute)?.groups?.get(1)?.value?.replace('_', ' ') ?: "Unknown canteen"

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

        val menu = menuDiv.select("h2").joinToString("\n") { it.text() }

        return Result.success(menu)
    }

}