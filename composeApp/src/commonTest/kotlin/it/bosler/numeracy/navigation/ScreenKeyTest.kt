package it.bosler.numeracy.navigation

import it.bosler.numeracy.model.Category
import it.bosler.numeracy.model.ScenarioType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ScreenKeyTest {

    private val everyScreen: List<Screen> = buildList {
        add(Screen.Home)
        add(Screen.Settings)
        Category.entries.forEach { category ->
            add(Screen.Scenarios(category))
            ScenarioType.subcategoriesForCategory(category).forEach { add(Screen.Subcategory(category, it)) }
        }
        ScenarioType.entries.forEach {
            add(Screen.Statistics(it))
            add(Screen.Practice(it))
        }
    }

    @Test
    fun everyScreenComesBackFromItsKey() {
        everyScreen.forEach { assertEquals(it, screenFromKey(it.toKey())) }
    }

    @Test
    fun aKeyFromAnOlderVersionOpensHomeRatherThanCrashing() {
        // Scenarios the poker rework renamed or removed, as a restored process would still name them.
        assertEquals(Screen.Home, screenFromKey("practice:EQUITY"))
        assertEquals(Screen.Home, screenFromKey("statistics:IMPLIED_ODDS"))
        assertEquals(Screen.Home, screenFromKey("scenarios:CARDS"))
        assertEquals(Screen.Home, screenFromKey("subcategory:GAMES:Blackjack"))
        assertEquals(Screen.Home, screenFromKey("practice"))
        assertEquals(Screen.Home, screenFromKey(""))
        assertEquals(Screen.Home, screenFromKey("gibberish:::"))
    }

    @Test
    fun backLeadsToTheListTheScreenWasChosenFrom() {
        assertNull(Screen.Home.parent())
        assertEquals(Screen.Home, Screen.Settings.parent())
        assertEquals(Screen.Home, Screen.Scenarios(Category.GAMES).parent())
        assertEquals(Screen.Scenarios(Category.GAMES), Screen.Subcategory(Category.GAMES, "Poker").parent())
        assertEquals(Screen.Subcategory(Category.GAMES, "Poker"), Screen.Practice(ScenarioType.POT_ODDS).parent())
        assertEquals(Screen.Scenarios(Category.GAMES), Screen.Statistics(ScenarioType.DARTS).parent())
    }
}
