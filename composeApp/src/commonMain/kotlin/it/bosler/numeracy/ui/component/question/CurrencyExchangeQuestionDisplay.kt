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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.bosler.numeracy.model.Difficulty
import it.bosler.numeracy.model.Problem

/**
 * Currency Exchange question display with difficulty-aware helpers.
 *
 * - NORMAL: EUR amount + exchange rate shown. User multiplies.
 * - PRACTICE: Rate broken down visually. Shows the whole-part result pre-calculated
 *   (e.g., "×1 = 50") or "subtract X%" for rates < 1. User handles the rest.
 */
@Composable
fun CurrencyExchangeQuestionDisplay(problem: Problem, difficulty: Difficulty = Difficulty.NORMAL) {
    val fromAmount = problem.metadata["fromAmount"] ?: "0"
    val toCurrencyCode = problem.metadata["toCurrencyCode"] ?: "USD"
    val rate = problem.metadata["rate"] ?: "1.00"
    val isPractice = difficulty == Difficulty.PRACTICE || difficulty == Difficulty.LEARNING

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // From currency (EUR)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CurrencyBadge(code = "EUR", color = Color(0xFF1565C0))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "\u20AC$fromAmount",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                ),
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Arrow
        Text(
            text = "\u2193",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // To currency
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CurrencyBadge(code = toCurrencyCode, color = Color(0xFFBF360C))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "?",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                ),
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Rate badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = "1 EUR = $rate $toCurrencyCode",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Medium,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Practice mode: show calculation breakdown
        if (isPractice) {
            val wholeResult = problem.metadata["wholeResult"] ?: "0"
            val practiceHint = problem.metadata["practiceHint"] ?: ""

            Spacer(modifier = Modifier.height(12.dp))

            HelperBadge(
                label = "Breakdown",
                value = practiceHint,
                color = MaterialTheme.colorScheme.primaryContainer,
                textColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun CurrencyBadge(code: String, color: Color) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = code.take(2),
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            ),
            color = Color.White,
        )
    }
}
