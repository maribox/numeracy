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
 * Pot odds display -shows hole cards, board, pot and call amount.
 */
@Composable
fun PokerQuestionDisplay(problem: Problem, difficulty: Difficulty = Difficulty.NORMAL) {
    val potAmount = problem.metadata["potAmount"] ?: "0"
    val callAmount = problem.metadata["callAmount"] ?: "0"
    val holeCards = parseCardStrings(problem.metadata["holeCards"] ?: "")
    val boardCards = parseCardStrings(problem.metadata["boardCards"] ?: "")
    val street = problem.metadata["street"] ?: ""
    val isPractice = difficulty == Difficulty.PRACTICE || difficulty == Difficulty.LEARNING

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Cards
        if (holeCards.isNotEmpty()) {
            LabeledCardRow(label = "Your hand", cards = holeCards)
            Spacer(modifier = Modifier.height(12.dp))
        }
        if (boardCards.isNotEmpty()) {
            LabeledCardRow(label = if (street.isNotEmpty()) "$street" else "Board", cards = boardCards)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Pot + Call chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ChipBadge(label = "Pot", value = "$$potAmount", isPrimary = true)
            Spacer(modifier = Modifier.width(12.dp))
            ChipBadge(label = "To call", value = "$$callAmount", isPrimary = false)
        }

        // Practice helpers
        if (isPractice) {
            val totalPot = problem.metadata["totalPot"] ?: "0"
            val fraction = problem.metadata["fraction"] ?: ""

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                HelperBadge(
                    label = "Total pot",
                    value = "$$totalPot",
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f),
                )
                HelperBadge(
                    label = "Fraction",
                    value = fraction,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * Outs counting display -shows hole cards, board, and draw name.
 */
@Composable
fun OutsCountingQuestionDisplay(problem: Problem, difficulty: Difficulty = Difficulty.NORMAL) {
    val holeCards = parseCardStrings(problem.metadata["holeCards"] ?: "")
    val boardCards = parseCardStrings(problem.metadata["boardCards"] ?: "")
    val drawName = problem.metadata["drawName"] ?: ""

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Cards
        if (holeCards.isNotEmpty()) {
            LabeledCardRow(label = "Your hand", cards = holeCards)
            Spacer(modifier = Modifier.height(12.dp))
        }
        if (boardCards.isNotEmpty()) {
            LabeledCardRow(label = "Board", cards = boardCards)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Draw type badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f))
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = drawName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "How many outs?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Equity display -shows cards, draw info, and asks for equity %.
 */
@Composable
fun EquityQuestionDisplay(problem: Problem, difficulty: Difficulty = Difficulty.NORMAL) {
    val holeCards = parseCardStrings(problem.metadata["holeCards"] ?: "")
    val boardCards = parseCardStrings(problem.metadata["boardCards"] ?: "")
    val drawName = problem.metadata["drawName"] ?: ""
    val outs = problem.metadata["outs"] ?: ""
    val streetLabel = problem.metadata["streetLabel"] ?: ""
    val multiplier = problem.metadata["multiplier"] ?: ""
    val isPractice = difficulty == Difficulty.PRACTICE || difficulty == Difficulty.LEARNING

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Cards
        if (holeCards.isNotEmpty()) {
            LabeledCardRow(label = "Your hand", cards = holeCards)
            Spacer(modifier = Modifier.height(12.dp))
        }
        if (boardCards.isNotEmpty()) {
            LabeledCardRow(label = streetLabel.ifEmpty { "Board" }, cards = boardCards)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Draw info
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            HelperBadge(
                label = "Draw",
                value = drawName,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                textColor = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.weight(1f),
            )
            HelperBadge(
                label = "Outs",
                value = outs,
                color = MaterialTheme.colorScheme.secondaryContainer,
                textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(0.5f),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Equity % (Rule of $multiplier)?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (isPractice) {
            Spacer(modifier = Modifier.height(8.dp))
            HelperBadge(
                label = "Hint",
                value = "$outs × $multiplier = ?",
                color = MaterialTheme.colorScheme.primaryContainer,
                textColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

/**
 * Implied odds display -shows cards, pot/call/equity, asks for needed winnings.
 */
@Composable
fun ImpliedOddsQuestionDisplay(problem: Problem, difficulty: Difficulty = Difficulty.NORMAL) {
    val potAmount = problem.metadata["potAmount"] ?: "0"
    val callAmount = problem.metadata["callAmount"] ?: "0"
    val equity = problem.metadata["equity"] ?: "0"
    val holeCards = parseCardStrings(problem.metadata["holeCards"] ?: "")
    val boardCards = parseCardStrings(problem.metadata["boardCards"] ?: "")
    val isPractice = difficulty == Difficulty.PRACTICE || difficulty == Difficulty.LEARNING

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Cards
        if (holeCards.isNotEmpty()) {
            LabeledCardRow(label = "Your hand", cards = holeCards)
            Spacer(modifier = Modifier.height(12.dp))
        }
        if (boardCards.isNotEmpty()) {
            LabeledCardRow(label = "Flop", cards = boardCards)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Info chips row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HelperBadge(
                label = "Pot",
                value = "$$potAmount",
                color = MaterialTheme.colorScheme.tertiaryContainer,
                textColor = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.weight(1f),
            )
            HelperBadge(
                label = "Call",
                value = "$$callAmount",
                color = MaterialTheme.colorScheme.errorContainer,
                textColor = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f),
            )
            HelperBadge(
                label = "Equity",
                value = "$equity%",
                color = MaterialTheme.colorScheme.secondaryContainer,
                textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Extra $ needed to break even?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (isPractice) {
            val currentTotal = problem.metadata["currentTotal"] ?: "0"
            val breakEvenTotal = problem.metadata["breakEvenTotal"] ?: "0"

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                HelperBadge(
                    label = "Break-even total",
                    value = "$$breakEvenTotal",
                    color = MaterialTheme.colorScheme.primaryContainer,
                    textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                )
                HelperBadge(
                    label = "Current total",
                    value = "$$currentTotal",
                    color = MaterialTheme.colorScheme.primaryContainer,
                    textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ChipBadge(label: String, value: String, isPrimary: Boolean) {
    val bgColor = if (isPrimary) {
        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
    } else {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
    }
    val textColor = if (isPrimary) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor.copy(alpha = 0.7f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = textColor,
        )
    }
}
