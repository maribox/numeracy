package it.bosler.numeracy.ui.component.question

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.bosler.numeracy.model.Difficulty
import it.bosler.numeracy.model.Problem

/**
 * Blackjack question display with difficulty-aware helpers.
 *
 * - NORMAL: Cards in fan layout. User calculates total from scratch.
 * - PRACTICE: Cards shown with helper badges -face card total, number card total,
 *   ace count. User just combines groups and decides ace value.
 */
@Composable
fun BlackjackQuestionDisplay(problem: Problem, difficulty: Difficulty = Difficulty.NORMAL) {
    val cards = problem.metadata["cards"]?.split(",") ?: emptyList()
    val isPractice = difficulty == Difficulty.PRACTICE || difficulty == Difficulty.LEARNING

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Cards in a fan layout
        Row(
            horizontalArrangement = Arrangement.spacedBy((-12).dp),
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            cards.forEachIndexed { index, card ->
                PlayingCard(
                    value = card,
                    rotation = (index - cards.size / 2f) * 5f,
                )
            }
        }

        // Practice mode: show group totals as helper badges
        if (isPractice) {
            val faceTotal = problem.metadata["faceTotal"]?.toIntOrNull() ?: 0
            val numberTotal = problem.metadata["numberTotal"]?.toIntOrNull() ?: 0
            val aceCount = problem.metadata["aceCount"]?.toIntOrNull() ?: 0

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                if (faceTotal > 0) {
                    HelperBadge(
                        label = "Face",
                        value = "= $faceTotal",
                        color = MaterialTheme.colorScheme.errorContainer,
                        textColor = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
                if (numberTotal > 0) {
                    HelperBadge(
                        label = "Numbers",
                        value = "= $numberTotal",
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
                if (aceCount > 0) {
                    HelperBadge(
                        label = "Aces",
                        value = "×$aceCount",
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        textColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayingCard(value: String, rotation: Float) {
    val isRed = value in listOf("2", "4", "6", "8", "10")
    val cardColor = if (isRed) MaterialTheme.colorScheme.error
                   else MaterialTheme.colorScheme.onSurface
    val suit = if (isRed) "\u2666" else "\u2660"

    Box(
        modifier = Modifier
            .width(72.dp)
            .height(100.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(12.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                ),
                color = cardColor,
            )
            Text(
                text = suit,
                style = MaterialTheme.typography.bodyLarge,
                color = cardColor,
            )
        }

        // Top-left corner pip
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = cardColor,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(6.dp),
        )
    }
}
