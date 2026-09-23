package it.bosler.numeracy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import it.bosler.numeracy.navigation.Screen
import it.bosler.numeracy.navigation.listHolding
import it.bosler.numeracy.navigation.parent
import it.bosler.numeracy.navigation.screenFromKey
import it.bosler.numeracy.navigation.toKey
import it.bosler.numeracy.ui.screen.HomeScreen
import it.bosler.numeracy.ui.screen.PracticeScreen
import it.bosler.numeracy.ui.screen.ScenariosScreen
import it.bosler.numeracy.ui.screen.SettingsScreen
import it.bosler.numeracy.ui.screen.StatisticsScreen
import it.bosler.numeracy.ui.screen.SubcategoryScenariosScreen
import it.bosler.numeracy.ui.theme.NumeracyTheme
import it.bosler.numeracy.util.PlatformBackHandler
import it.bosler.numeracy.viewmodel.PracticeViewModel

@Composable
fun App() {
    NumeracyTheme {
        // The screen is held as its key, which is what survives the process being killed.
        var screenKey by rememberSaveable { mutableStateOf(Screen.Home.toKey()) }
        // Counts visits to practice, so each visit is its own run rather than a return to the last one.
        var practiceVisit by rememberSaveable { mutableIntStateOf(0) }
        val screen = screenFromKey(screenKey)
        fun go(to: Screen) {
            if (to is Screen.Practice) practiceVisit++
            screenKey = to.toKey()
        }

        // Practice leaves through its own back handler, which saves the run first.
        PlatformBackHandler(enabled = screen !is Screen.Home && screen !is Screen.Practice) {
            screen.parent()?.let { go(it) }
        }

        when (screen) {
            is Screen.Home -> HomeScreen(
                onCategorySelected = { go(Screen.Scenarios(it)) },
                onSettingsSelected = { go(Screen.Settings) },
            )

            is Screen.Settings -> SettingsScreen(onBack = { go(Screen.Home) })

            is Screen.Scenarios -> ScenariosScreen(
                category = screen.category,
                onScenarioSelected = { go(Screen.Practice(it)) },
                onStatsSelected = { go(Screen.Statistics(it)) },
                onSubcategorySelected = { go(Screen.Subcategory(screen.category, it)) },
                onBack = { go(Screen.Home) },
            )

            is Screen.Subcategory -> SubcategoryScenariosScreen(
                category = screen.category,
                subcategory = screen.subcategory,
                onScenarioSelected = { go(Screen.Practice(it)) },
                onStatsSelected = { go(Screen.Statistics(it)) },
                onBack = { go(Screen.Scenarios(screen.category)) },
            )

            is Screen.Statistics -> StatisticsScreen(
                scenarioType = screen.scenarioType,
                onPlay = { go(Screen.Practice(screen.scenarioType)) },
                onBack = { go(listHolding(screen.scenarioType)) },
            )

            is Screen.Practice -> VisitScope("practice-$practiceVisit") {
                val type = screen.scenarioType
                PracticeScreen(
                    scenarioType = type,
                    onBack = { go(listHolding(type)) },
                    viewModel = viewModel { PracticeViewModel(type) },
                )
            }
        }
    }
}

/**
 * View models that belong to one visit of a screen: they outlive a rotation, which rebuilds every
 * composable, and are cleared when the next visit begins. Held by a view model of the app's own,
 * which is what survives the activity being recreated.
 */
@Composable
private fun VisitScope(visit: String, content: @Composable () -> Unit) {
    val visits: Visits = viewModel()
    val owner = remember(visit) {
        object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore = visits.storeFor(visit)
        }
    }
    CompositionLocalProvider(LocalViewModelStoreOwner provides owner, content = content)
}

/** The view model store of the visit under way; starting another clears the one before. */
internal class Visits : ViewModel() {
    private val stores = mutableMapOf<String, ViewModelStore>()

    fun storeFor(visit: String): ViewModelStore {
        stores.keys.filter { it != visit }.forEach { stores.remove(it)?.clear() }
        return stores.getOrPut(visit) { ViewModelStore() }
    }

    override fun onCleared() {
        stores.values.forEach { it.clear() }
        stores.clear()
    }
}
