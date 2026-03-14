package it.bosler.numeracy.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.bosler.numeracy.model.Category
import it.bosler.numeracy.model.ScenarioType
import it.bosler.numeracy.persistence.AppContext
import it.bosler.numeracy.persistence.ScenarioStats
import it.bosler.numeracy.ui.component.CardBackgroundImage
import it.bosler.numeracy.ui.component.scenarioImageRes
import it.bosler.numeracy.util.showBackButton

@Composable
fun ScenariosScreen(
    category: Category,
    onScenarioSelected: (ScenarioType) -> Unit,
    onStatsSelected: (ScenarioType) -> Unit,
    onSubcategorySelected: (String) -> Unit,
    onBack: () -> Unit,
) {
    val topLevelScenarios = ScenarioType.topLevelForCategory(category)
    val subcategories = ScenarioType.subcategoriesForCategory(category)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        Spacer(modifier = Modifier.height(if (showBackButton) 4.dp else 24.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showBackButton) {
                TextButton(onClick = onBack) {
                    Text("Back", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 36.sp,
                    letterSpacing = (-1).sp,
                ),
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        Text(
            text = category.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(topLevelScenarios) { scenario ->
                val stats = remember { AppContext.runRepository.getStats(scenario) }
                ScenarioCard(
                    scenario = scenario,
                    stats = stats,
                    onClick = { onScenarioSelected(scenario) },
                    onStatsClick = { onStatsSelected(scenario) },
                )
            }
            items(subcategories) { sub ->
                val subScenarios = ScenarioType.forSubcategory(category, sub)
                SubcategoryFolderCard(
                    name = sub,
                    description = "${subScenarios.size} scenarios",
                    firstScenario = subScenarios.first(),
                    onClick = { onSubcategorySelected(sub) },
                )
            }
        }
    }
}

@Composable
fun SubcategoryScenariosScreen(
    category: Category,
    subcategory: String,
    onScenarioSelected: (ScenarioType) -> Unit,
    onStatsSelected: (ScenarioType) -> Unit,
    onBack: () -> Unit,
) {
    val scenarios = ScenarioType.forSubcategory(category, subcategory)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        Spacer(modifier = Modifier.height(if (showBackButton) 4.dp else 24.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showBackButton) {
                TextButton(onClick = onBack) {
                    Text("Back", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(
                text = subcategory,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 36.sp,
                    letterSpacing = (-1).sp,
                ),
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        Text(
            text = "${scenarios.size} scenarios",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(scenarios) { scenario ->
                val stats = remember { AppContext.runRepository.getStats(scenario) }
                ScenarioCard(
                    scenario = scenario,
                    stats = stats,
                    onClick = { onScenarioSelected(scenario) },
                    onStatsClick = { onStatsSelected(scenario) },
                )
            }
        }
    }
}

@Composable
private fun ScenarioCard(
    scenario: ScenarioType,
    stats: ScenarioStats,
    onClick: () -> Unit,
    onStatsClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(
                Brush.linearGradient(
                    colors = listOf(scenario.startColor, scenario.endColor),
                )
            )
            .clickable(onClick = onClick),
    ) {
        CardBackgroundImage(
            imageRes = scenarioImageRes(scenario),
            gradientColor = scenario.startColor,
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp),
        ) {
            Text(
                text = scenario.displayName,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                ),
                color = Color.White,
            )
            Text(
                text = scenario.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
            )
            if (stats.totalRuns > 0) {
                Text(
                    text = "${stats.totalRuns} runs \u00B7 ${stats.accuracy}%",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Color.White.copy(alpha = 0.65f),
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }

        // Stats button
        if (stats.totalRuns > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f))
                    .clickable(onClick = onStatsClick),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.size(16.dp)) {
                    val w = size.width
                    val h = size.height
                    val barW = w / 5f
                    val gap = barW * 0.4f
                    val totalBarsW = 3 * barW + 2 * gap
                    val offsetX = (w - totalBarsW) / 2f
                    val color = Color.White
                    drawRoundRect(color, topLeft = androidx.compose.ui.geometry.Offset(offsetX, h * 0.5f), size = androidx.compose.ui.geometry.Size(barW, h * 0.5f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f))
                    drawRoundRect(color, topLeft = androidx.compose.ui.geometry.Offset(offsetX + barW + gap, h * 0.2f), size = androidx.compose.ui.geometry.Size(barW, h * 0.8f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f))
                    drawRoundRect(color, topLeft = androidx.compose.ui.geometry.Offset(offsetX + 2 * (barW + gap), h * 0.35f), size = androidx.compose.ui.geometry.Size(barW, h * 0.65f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f))
                }
            }
        }
    }
}

@Composable
private fun SubcategoryFolderCard(
    name: String,
    description: String,
    firstScenario: ScenarioType,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(
                Brush.linearGradient(
                    colors = listOf(firstScenario.startColor, firstScenario.endColor),
                )
            )
            .clickable(onClick = onClick),
    ) {
        CardBackgroundImage(
            imageRes = scenarioImageRes(firstScenario),
            gradientColor = firstScenario.startColor,
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp),
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                ),
                color = Color.White,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
            )
        }
    }
}
