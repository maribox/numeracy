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
import it.bosler.numeracy.model.Difficulty
import it.bosler.numeracy.model.Problem

// ─────────────────────────────────────────────────────────────────────────────
// POT ODDS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Difficulty progression:
 * HARD     — cards shown (estimate win% yourself), ugly numbers, ±2% accepted
 * NORMAL   — win% given, clean numbers
 * PRACTICE — win% given + total pot badge
 * LEARNING — win% given + total pot + simplified fraction + formula label
 */
@Composable
fun PokerQuestionDisplay(problem: Problem, difficulty: Difficulty = Difficulty.NORMAL) {
    val pot = problem.metadata["potAmount"] ?: "0"
    val call = problem.metadata["callAmount"] ?: "0"
    val totalPot = problem.metadata["totalPot"] ?: "0"
    val fraction = problem.metadata["fraction"] ?: ""
    val winPercent = problem.metadata["winPercent"] ?: ""
    val holeCards = parseCardStrings(problem.metadata["holeCards"] ?: "")
    val boardCards = parseCardStrings(problem.metadata["boardCards"] ?: "")
    val street = problem.metadata["street"] ?: ""

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // HARD: show cards so player must estimate win% themselves
        if (difficulty == Difficulty.HARD) {
            if (holeCards.isNotEmpty()) {
                LabeledCardRow(label = "Your hand", cards = holeCards)
                Spacer(Modifier.height(10.dp))
            }
            if (boardCards.isNotEmpty()) {
                LabeledCardRow(label = street, cards = boardCards)
                Spacer(Modifier.height(14.dp))
            }
        }

        // Pot + call chips — always shown
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PokerChip(label = "Pot", value = "\$$pot", isPrimary = true)
            Spacer(Modifier.width(12.dp))
            PokerChip(label = "To call", value = "\$$call", isPrimary = false)
        }

        // NORMAL/PRACTICE/LEARNING: win% is given (no cards needed)
        if (difficulty != Difficulty.HARD) {
            Spacer(Modifier.height(10.dp))
            HelperBadge(
                label = "Your win%",
                value = "$winPercent%",
                color = MaterialTheme.colorScheme.tertiaryContainer,
                textColor = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // PRACTICE+: show total pot
        if (difficulty == Difficulty.PRACTICE || difficulty == Difficulty.LEARNING) {
            Spacer(Modifier.height(8.dp))
            HelperBadge(
                label = "Total pot  (pot + call)",
                value = "\$$totalPot",
                color = MaterialTheme.colorScheme.secondaryContainer,
                textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // LEARNING: show simplified fraction + formula
        if (difficulty == Difficulty.LEARNING) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HelperBadge(
                    label = "Fraction  (call \u00F7 total)",
                    value = fraction,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                )
                HelperBadge(
                    label = "Formula",
                    value = "fraction \u00D7 100 = ?%",
                    color = MaterialTheme.colorScheme.primaryContainer,
                    textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DRAW EQUITY
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Difficulty progression:
 * HARD     — cards + street only. Know outs from memory, apply rule of 2/4.
 * NORMAL   — adds draw name badge. Know outs, apply rule.
 * PRACTICE — adds outs count. Just apply rule of 2/4.
 * LEARNING — adds multiplier badge + pot odds context. Just multiply.
 */
@Composable
fun DrawEquityQuestionDisplay(problem: Problem, difficulty: Difficulty = Difficulty.NORMAL) {
    val drawName = problem.metadata["drawName"] ?: ""
    val drawExplanation = problem.metadata["drawExplanation"] ?: ""
    val outs = problem.metadata["outs"] ?: ""
    val multiplier = problem.metadata["multiplier"] ?: ""
    val street = problem.metadata["street"] ?: ""
    val potOdds = problem.metadata["potOdds"] ?: ""
    val pot = problem.metadata["potAmount"] ?: "0"
    val call = problem.metadata["callAmount"] ?: "0"
    val holeCards = parseCardStrings(problem.metadata["holeCards"] ?: "")
    val boardCards = parseCardStrings(problem.metadata["boardCards"] ?: "")

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (difficulty == Difficulty.HARD) {
            if (holeCards.isNotEmpty()) {
                LabeledCardRow(label = "Your hand", cards = holeCards)
                Spacer(Modifier.height(10.dp))
            }
            if (boardCards.isNotEmpty()) {
                LabeledCardRow(label = street, cards = boardCards)
                Spacer(Modifier.height(14.dp))
            }
        }

        // Street badge — always shown (tells you ×4 or ×2)
        StreetBadge(street = street)

        // NORMAL+: draw name
        if (difficulty != Difficulty.HARD) {
            Spacer(Modifier.height(8.dp))
            HelperBadge(
                label = "Your draw",
                value = drawName,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                textColor = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // PRACTICE+: outs count
        if (difficulty == Difficulty.PRACTICE || difficulty == Difficulty.LEARNING) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HelperBadge(
                    label = "Outs",
                    value = outs,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f),
                )
                HelperBadge(
                    label = "Why $outs outs?",
                    value = drawExplanation,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(2f),
                )
            }
        }

        // LEARNING: multiplier + pot odds context
        if (difficulty == Difficulty.LEARNING) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HelperBadge(
                    label = "Rule of $multiplier",
                    value = "$outs \u00D7 $multiplier = ?",
                    color = MaterialTheme.colorScheme.primaryContainer,
                    textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                )
                HelperBadge(
                    label = "Pot odds",
                    value = "$potOdds%",
                    color = MaterialTheme.colorScheme.errorContainer,
                    textColor = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(6.dp))
            HelperBadge(
                label = "Context: pot \$${pot}, call \$${call}. If equity > pot odds \u2192 call",
                value = "",
                color = MaterialTheme.colorScheme.surfaceVariant,
                textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PokerChip(label: String, value: String, isPrimary: Boolean) {
    val bg = if (isPrimary)
        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f)
    else
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
    val fg = if (isPrimary)
        MaterialTheme.colorScheme.onTertiaryContainer
    else
        MaterialTheme.colorScheme.onErrorContainer

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = fg.copy(alpha = 0.7f))
        Text(value, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = fg)
    }
}

@Composable
private fun StreetBadge(street: String) {
    val multiplier = if (street == "Flop") "4" else "2"
    val description = if (street == "Flop") "2 cards to come \u2192 outs \u00D7 4" else "1 card to come \u2192 outs \u00D7 2"
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(horizontal = 14.dp, vertical = 6.dp),
    ) {
        Text(
            text = "$street \u2014 $description",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
