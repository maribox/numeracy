package it.bosler.numeracy.ui.component.question

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.bosler.numeracy.model.Difficulty
import it.bosler.numeracy.model.Problem

/**
 * Time Zones question display with difficulty-aware helpers.
 *
 * - NORMAL: Source city + time, target city. User figures out offset and converts.
 * - PRACTICE: A badge between the cities shows the time offset (e.g., "+3h" or "−5h 30m").
 *   User just adds/subtracts the shown offset.
 */
@Composable
fun TimeZonesQuestionDisplay(problem: Problem, difficulty: Difficulty = Difficulty.NORMAL) {
    val fromCity = problem.metadata["fromCity"] ?: ""
    val toCity = problem.metadata["toCity"] ?: ""
    val time = problem.metadata["time"] ?: "00:00"
    val season = problem.metadata["season"] ?: "winter"
    val isPractice = difficulty == Difficulty.PRACTICE || difficulty == Difficulty.LEARNING

    val isSummer = season == "summer"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Season badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isSummer) MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.primaryContainer
                )
                .padding(horizontal = 12.dp, vertical = 4.dp),
        ) {
            Text(
                text = if (isSummer) "\u2600\uFE0F Summer" else "\u2744\uFE0F Winter",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = if (isSummer) MaterialTheme.colorScheme.onSecondaryContainer
                        else MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // From city + time
        CityTimeCard(
            city = fromCity,
            time = time,
            isSource = true,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Arrow -in practice mode, show the offset difference inline
        if (isPractice) {
            val offsetDiff = problem.metadata["offsetDiff"] ?: ""
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "\u2193",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = offsetDiff,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        } else {
            Text(
                text = "\u2193",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // To city + ?
        CityTimeCard(
            city = toCity,
            time = "??:??",
            isSource = false,
        )
    }
}

@Composable
private fun CityTimeCard(
    city: String,
    time: String,
    isSource: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSource) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = city,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = time,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = null,
                letterSpacing = 2.sp,
            ),
            color = if (isSource) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.primary,
        )
    }
}
