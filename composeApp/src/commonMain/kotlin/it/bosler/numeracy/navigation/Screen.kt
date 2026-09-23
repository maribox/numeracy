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

/** The screen as text, which is what survives the process being killed and restored. */
fun Screen.toKey(): String = when (this) {
    is Screen.Home -> "home"
    is Screen.Settings -> "settings"
    is Screen.Scenarios -> "scenarios:${category.name}"
    is Screen.Subcategory -> "subcategory:${category.name}:$subcategory"
    is Screen.Statistics -> "statistics:${scenarioType.name}"
    is Screen.Practice -> "practice:${scenarioType.name}"
}

/**
 * The screen a saved key names, or home. A key is restored by whatever version of the app is
 * installed when the process comes back, which may no longer have the scenario it names: a
 * scenario renamed in an update would otherwise crash every launch until the app's data is cleared.
 */
fun screenFromKey(key: String): Screen {
    val parts = key.split(":", limit = 3)
    fun category() = parts.getOrNull(1)?.let { name -> Category.entries.firstOrNull { it.name == name } }
    fun scenario() = parts.getOrNull(1)?.let { name -> ScenarioType.entries.firstOrNull { it.name == name } }
    return when (parts[0]) {
        "settings" -> Screen.Settings
        "scenarios" -> category()?.let { Screen.Scenarios(it) }
        "subcategory" -> category()?.let { category ->
            parts.getOrNull(2)
                ?.takeIf { it in ScenarioType.subcategoriesForCategory(category) }
                ?.let { Screen.Subcategory(category, it) }
        }
        "statistics" -> scenario()?.let { Screen.Statistics(it) }
        "practice" -> scenario()?.let { Screen.Practice(it) }
        else -> null
    } ?: Screen.Home
}

/** Where Back goes from this screen; null on home, where Back leaves the app. */
fun Screen.parent(): Screen? = when (this) {
    is Screen.Home -> null
    is Screen.Settings, is Screen.Scenarios -> Screen.Home
    is Screen.Subcategory -> Screen.Scenarios(category)
    is Screen.Statistics -> listHolding(scenarioType)
    is Screen.Practice -> listHolding(scenarioType)
}

/** The list a scenario is chosen from: its folder when it has one, else its category. */
fun listHolding(type: ScenarioType): Screen {
    val folder = type.subcategory
    return if (folder != null) Screen.Subcategory(type.category, folder) else Screen.Scenarios(type.category)
}
