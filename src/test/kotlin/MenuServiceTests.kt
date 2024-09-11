import di.testModule
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import services.LunchService
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.assertTrue

class MenuServiceTests : KoinTest {
    private val menuHtml = Files.readString(Paths.get("src/test/resources/TestMenu.html"))
    private val lunchService: LunchService by inject()

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup(): Unit {
            startKoin {
                modules(testModule)
            }
        }
    }

    @Test
    fun `test menu parsing`() {
        val menuResult = lunchService.parseMenu(menuHtml)

        assertTrue(menuResult.isSuccess)

        val menuItems = menuResult.getOrThrow()

        assert(menuItems.any { menuItem ->
            menuItem.description == "Cod with broccoli pure & cabbage salad"
                    && menuItem.allergens.contains("Sulfites") && menuItem.allergens.contains("Fish")
        })

        assert(menuItems.any { menuItem ->
            menuItem.description == "Chili sin carne" && menuItem.allergens.isEmpty()
        })
    }
}