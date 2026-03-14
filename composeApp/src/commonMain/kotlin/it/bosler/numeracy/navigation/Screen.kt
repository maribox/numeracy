package it.bosler.numeracy.navigation

import it.bosler.numeracy.model.Category
import it.bosler.numeracy.model.ScenarioType

sealed class Screen {
    data object Home : Screen()
    data object Settings : Screen()
    data class Scenarios(val category: Category) : Screen()
    data class Subcategory(val category: Category, val subcategory: String) : Screen()
    data class Statistics(val scenarioType: ScenarioType) : Screen()
    data class Practice(val scenarioType: ScenarioType) : Screen()
}
