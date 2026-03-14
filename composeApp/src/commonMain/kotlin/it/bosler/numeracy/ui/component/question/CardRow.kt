package it.bosler.numeracy.ui.component.question

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val cardRed = Color(0xFFD32F2F)
private val cardBlack = Color(0xFF212121)

/**
 * A single playing card visual.
 */
@Composable
fun CardView(
    rankSymbol: String,
    suitSymbol: String,
    isRed: Boolean,
    faceDown: Boolean = false,
) {
    val textColor = if (isRed) cardRed else cardBlack

    Box(
        modifier = Modifier
            .width(48.dp)
            .height(68.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (faceDown) Color(0xFF1565C0) else Color.White)
            .border(1.dp, Color(0x33000000), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (!faceDown) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = rankSymbol,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    lineHeight = 22.sp,
                )
                Text(
                    text = suitSymbol,
                    fontSize = 16.sp,
                    color = textColor,
                    lineHeight = 18.sp,
                )
            }
        }
    }
}

/**
 * Displays a labeled row of cards (e.g. "Your hand", "Board").
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LabeledCardRow(
    label: String,
    cards: List<Triple<String, String, Boolean>>, // (rank, suit, isRed)
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            for ((rank, suit, isRed) in cards) {
                CardView(rankSymbol = rank, suitSymbol = suit, isRed = isRed)
            }
        }
    }
}

/** Parse card strings from metadata like "A♠,K♥,10♦" into display triples. */
fun parseCardStrings(encoded: String): List<Triple<String, String, Boolean>> {
    if (encoded.isBlank()) return emptyList()
    return encoded.split(",").map { card ->
        val trimmed = card.trim()
        // Last char is the suit symbol
        val suitChar = trimmed.last().toString()
        val rank = trimmed.dropLast(1)
        val isRed = suitChar == "\u2665" || suitChar == "\u2666"
        Triple(rank, suitChar, isRed)
    }
}
