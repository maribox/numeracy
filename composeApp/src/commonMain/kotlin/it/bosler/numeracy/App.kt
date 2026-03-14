package it.bosler.numeracy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import it.bosler.numeracy.model.Category
import it.bosler.numeracy.model.ScenarioType
import it.bosler.numeracy.navigation.Screen
import it.bosler.numeracy.ui.screen.HomeScreen
import it.bosler.numeracy.ui.screen.PracticeScreen
import it.bosler.numeracy.ui.screen.ScenariosScreen
import it.bosler.numeracy.ui.screen.SubcategoryScenariosScreen
import it.bosler.numeracy.ui.screen.SettingsScreen
import it.bosler.numeracy.ui.screen.StatisticsScreen
import it.bosler.numeracy.ui.theme.NumeracyTheme
import it.bosler.numeracy.util.PlatformBackHandler

private fun screenToString(screen: Screen): String = when (screen) {
    is Screen.Home -> "home"
    is Screen.Settings -> "settings"
    is Screen.Scenarios -> "scenarios:${screen.category.name}"
    is Screen.Subcategory -> "subcategory:${screen.category.name}:${screen.subcategory}"
    is Screen.Statistics -> "statistics:${screen.scenarioType.name}"
    is Screen.Practice -> "practice:${screen.scenarioType.name}"
}

private fun stringToScreen(value: String): Screen {
    val parts = value.split(":", limit = 3)
    return when (parts[0]) {
        "settings" -> Screen.Settings
        "scenarios" -> Screen.Scenarios(Category.valueOf(parts[1]))
        "subcategory" -> Screen.Subcategory(Category.valueOf(parts[1]), parts[2])
        "statistics" -> Screen.Statistics(ScenarioType.valueOf(parts[1]))
        "practice" -> Screen.Practice(ScenarioType.valueOf(parts[1]))
        else -> Screen.Home
    }
}

@Composable
fun App() {
    NumeracyTheme {
        var screenKey by rememberSaveable { mutableStateOf("home") }
        var currentScreen by remember(screenKey) { mutableStateOf(stringToScreen(screenKey)) }

        // Sync back to saveable key whenever currentScreen changes
        androidx.compose.runtime.LaunchedEffect(currentScreen) {
            screenKey = screenToString(currentScreen)
        }

        PlatformBackHandler(enabled = currentScreen !is Screen.Home && currentScreen !is Screen.Practice) {
            when (val screen = currentScreen) {
                is Screen.Home -> {}
                is Screen.Settings -> currentScreen = Screen.Home
                is Screen.Scenarios -> currentScreen = Screen.Home
                is Screen.Subcategory -> currentScreen = Screen.Scenarios(screen.category)
                is Screen.Statistics -> {
                    val st = screen.scenarioType
                    currentScreen = if (st.subcategory != null) {
                        Screen.Subcategory(st.category, st.subcategory!!)
                    } else {
                        Screen.Scenarios(st.category)
                    }
                }
                is Screen.Practice -> {}
            }
        }

        when (val screen = currentScreen) {
            is Screen.Home -> HomeScreen(
                onCategorySelected = { currentScreen = Screen.Scenarios(it) },
                onSettingsSelected = { currentScreen = Screen.Settings },
            )

            is Screen.Settings -> SettingsScreen(
                onBack = { currentScreen = Screen.Home },
            )

            is Screen.Scenarios -> ScenariosScreen(
                category = screen.category,
                onScenarioSelected = { currentScreen = Screen.Practice(it) },
                onStatsSelected = { currentScreen = Screen.Statistics(it) },
                onSubcategorySelected = { sub -> currentScreen = Screen.Subcategory(screen.category, sub) },
                onBack = { currentScreen = Screen.Home },
            )

            is Screen.Subcategory -> SubcategoryScenariosScreen(
                category = screen.category,
                subcategory = screen.subcategory,
                onScenarioSelected = { currentScreen = Screen.Practice(it) },
                onStatsSelected = { currentScreen = Screen.Statistics(it) },
                onBack = { currentScreen = Screen.Scenarios(screen.category) },
            )

            is Screen.Statistics -> {
                val st = screen.scenarioType
                StatisticsScreen(
                    scenarioType = st,
                    onPlay = { currentScreen = Screen.Practice(st) },
                    onBack = {
                        currentScreen = if (st.subcategory != null) {
                            Screen.Subcategory(st.category, st.subcategory!!)
                        } else {
                            Screen.Scenarios(st.category)
                        }
                    },
                )
            }

            is Screen.Practice -> {
                val st = screen.scenarioType
                PracticeScreen(
                    scenarioType = st,
                    onBack = {
                        currentScreen = if (st.subcategory != null) {
                            Screen.Subcategory(st.category, st.subcategory!!)
                        } else {
                            Screen.Scenarios(st.category)
                        }
                    },
                )
            }
        }
    }
}
