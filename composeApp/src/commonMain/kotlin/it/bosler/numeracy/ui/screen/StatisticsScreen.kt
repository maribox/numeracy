package it.bosler.numeracy.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.bosler.numeracy.model.RunRecord
import it.bosler.numeracy.model.ScenarioType
import it.bosler.numeracy.persistence.AppContext
import it.bosler.numeracy.persistence.ScenarioStats
import it.bosler.numeracy.ui.component.CardBackgroundImage
import it.bosler.numeracy.ui.component.scenarioImageRes
import it.bosler.numeracy.util.showBackButton

@Composable
fun StatisticsScreen(
    scenarioType: ScenarioType,
    onPlay: () -> Unit,
    onBack: () -> Unit,
) {
    val stats = remember { AppContext.runRepository.getStats(scenarioType) }
    val runs = remember { AppContext.runRepository.getRunsForScenario(scenarioType).sortedByDescending { it.startedAt } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        // Back button
        if (showBackButton) {
            TextButton(
                onClick = onBack,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text("Back", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            // Gradient header
            item {
                ScenarioHeader(scenarioType = scenarioType)
            }

            if (stats.totalRuns > 0) {
                // Accuracy ring + streak + time row
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    AccuracyAndStreakRow(stats = stats, scenarioType = scenarioType)
                }

                // Totals row
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    TotalsRow(stats = stats)
                }

                // Milestone badges
                item {
                    val badges = computeBadges(stats = stats, runs = runs)
                    if (badges.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        BadgesSection(badges = badges, scenarioType = scenarioType)
                    }
                }

                // History header
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }

                items(runs) { run ->
                    RunHistoryItem(run = run, scenarioType = scenarioType)
                    Spacer(modifier = Modifier.height(6.dp))
                }
            } else {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 56.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "No runs yet",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "Start practicing to see your stats",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }

        // Play button
        Button(
            onClick = onPlay,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(56.dp),
            shape = RoundedCornerShape(20.dp),
        ) {
            Text(
                text = "Play",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
            )
        }
    }
}

@Composable
private fun ScenarioHeader(scenarioType: ScenarioType) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(120.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(scenarioType.startColor, scenarioType.endColor),
                )
            ),
    ) {
        CardBackgroundImage(
            imageRes = scenarioImageRes(scenarioType),
            gradientColor = scenarioType.startColor,
            modifier = Modifier.fillMaxSize(),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp),
        ) {
            Text(
                text = scenarioType.displayName,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = Color.White,
            )
            Text(
                text = scenarioType.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
private fun AccuracyAndStreakRow(stats: ScenarioStats, scenarioType: ScenarioType) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Accuracy ring card
        Box(
            modifier = Modifier
                .weight(1.1f)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AccuracyRing(
                    accuracy = stats.accuracy,
                    startColor = scenarioType.startColor,
                    endColor = scenarioType.endColor,
                    size = 96.dp,
                    strokeWidth = 10.dp,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Accuracy",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Streak + avg time stacked
        Column(
            modifier = Modifier.weight(0.9f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Best streak card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Text(
                    text = "\uD83D\uDD25 ${stats.bestStreak}",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Best Streak",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Avg time card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = formatTime(stats.averageTimeMillis),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "avg",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 2.dp),
                    )
                }
                Text(
                    text = "Avg Time",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AccuracyRing(
    accuracy: Int,
    startColor: Color,
    endColor: Color,
    size: Dp,
    strokeWidth: Dp,
) {
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    val sweepAngle = (accuracy / 100f) * 300f

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(size),
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val inset = strokePx / 2f
            val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
            val topLeft = Offset(inset, inset)
            val startAngle = 120f

            // Track arc
            drawArc(
                color = trackColor,
                startAngle = startAngle,
                sweepAngle = 300f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )

            // Progress arc
            if (sweepAngle > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(startColor, endColor, startColor),
                        center = Offset(this.size.width / 2f, this.size.height / 2f),
                    ),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$accuracy%",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun TotalsRow(stats: ScenarioStats) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SmallStatCard(
            label = "Runs",
            value = stats.totalRuns.toString(),
            modifier = Modifier.weight(1f),
        )
        SmallStatCard(
            label = "Correct",
            value = "${stats.totalCorrect} / ${stats.totalQuestions}",
            modifier = Modifier.weight(1.6f),
        )
    }
}

@Composable
private fun SmallStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ---------- Badges ----------

private data class Badge(val emoji: String, val label: String)

private fun computeBadges(stats: ScenarioStats, runs: List<RunRecord>): List<Badge> {
    val badges = mutableListOf<Badge>()
    if (stats.totalRuns >= 1) badges += Badge("\uD83C\uDF1F", "First Run!")
    if (stats.totalRuns >= 10) badges += Badge("\uD83D\uDD1F", "10 Runs")
    if (stats.totalRuns >= 50) badges += Badge("\uD83D\uDE80", "50 Runs")
    if (stats.accuracy >= 90 && stats.totalQuestions >= 10) badges += Badge("\uD83C\uDFAF", "90%+ Club")
    if (runs.any { it.accuracy == 100 && it.answers.size >= 5 }) badges += Badge("\u2B50", "Perfect Run")
    if (stats.bestStreak >= 5) badges += Badge("\uD83D\uDD25", "5+ Streak")
    if (stats.bestStreak >= 10) badges += Badge("\uD83D\uDCA5", "10 Streak!")
    if (stats.averageTimeMillis in 1..3000 && stats.totalQuestions >= 10) badges += Badge("\u26A1", "Lightning Fast")
    return badges
}

@Composable
private fun BadgesSection(badges: List<Badge>, scenarioType: ScenarioType) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Achievements",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        // Wrap badges in rows of up to 3
        val chunked = badges.chunked(3)
        chunked.forEach { rowBadges ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            ) {
                rowBadges.forEach { badge ->
                    BadgeChip(badge = badge, scenarioType = scenarioType, modifier = Modifier.weight(1f))
                }
                // Fill remaining slots so layout stays uniform
                repeat(3 - rowBadges.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun BadgeChip(
    badge: Badge,
    scenarioType: ScenarioType,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        scenarioType.startColor.copy(alpha = 0.2f),
                        scenarioType.endColor.copy(alpha = 0.2f),
                    )
                )
            )
            .padding(horizontal = 8.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = badge.emoji,
                fontSize = 22.sp,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = badge.label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
        }
    }
}

// ---------- Run history ----------

@Composable
private fun RunHistoryItem(run: RunRecord, scenarioType: ScenarioType) {
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Left: score + accuracy bar
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${run.totalCorrect}/${run.answers.size}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${run.accuracy}%",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            // Mini accuracy bar
            MiniAccuracyBar(
                answers = run.answers.map { it.isCorrect },
                startColor = scenarioType.startColor,
                endColor = scenarioType.endColor,
                trackColor = trackColor,
            )
        }

        // Right: avg time + optional streak
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "\u23F1 ${formatTime(run.averageTimeMillis)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (run.bestStreak > 1) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "\uD83D\uDD25 ${run.bestStreak}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun MiniAccuracyBar(
    answers: List<Boolean>,
    startColor: Color,
    endColor: Color,
    trackColor: Color,
    height: Dp = 5.dp,
) {
    if (answers.isEmpty()) return
    val correctColor = startColor.copy(alpha = 0.85f)
    val wrongColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(50)),
    ) {
        val totalWidth = this.size.width
        val barH = this.size.height
        val segW = totalWidth / answers.size.toFloat()
        val gapPx = (segW * 0.12f).coerceAtMost(2.dp.toPx())

        answers.forEachIndexed { i, correct ->
            val x = i * segW
            val radius = barH / 2f
            drawRoundRect(
                color = if (correct) correctColor else wrongColor,
                topLeft = Offset(x + gapPx / 2f, 0f),
                size = Size((segW - gapPx).coerceAtLeast(1f), barH),
                cornerRadius = CornerRadius(radius, radius),
            )
        }
    }
}

// ---------- Helpers ----------

private fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    return if (totalSeconds < 60) {
        val tenths = (millis % 1000) / 100
        "${totalSeconds}.${tenths}s"
    } else {
        val m = totalSeconds / 60
        val s = totalSeconds % 60
        "${m}m ${s}s"
    }
}
