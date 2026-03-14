package it.bosler.numeracy.ui.component.question

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
 * Making Change question display with difficulty-aware helpers.
 *
 * - NORMAL: Receipt shows bill total and payment. User calculates full change.
 * - PRACTICE: The cents part of the change is shown on the receipt. User only
 *   needs to figure out the euro part and combine.
 */
@Composable
fun MakingChangeQuestionDisplay(problem: Problem, difficulty: Difficulty = Difficulty.NORMAL) {
    val billAmount = problem.metadata["billAmount"] ?: "0.00"
    val paymentAmount = problem.metadata["paymentAmount"] ?: "0.00"
    val isPractice = difficulty == Difficulty.PRACTICE || difficulty == Difficulty.LEARNING

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Receipt
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(24.dp),
        ) {
            Text(
                text = "RECEIPT",
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 3.sp,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                thickness = 1.dp,
            )

            Spacer(modifier = Modifier.height(16.dp))

            ReceiptLine(label = "Total", amount = "\u20AC$billAmount")

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                thickness = 1.dp,
            )

            Spacer(modifier = Modifier.height(12.dp))

            ReceiptLine(
                label = "Paid",
                amount = "\u20AC$paymentAmount",
                amountColor = MaterialTheme.colorScheme.tertiary,
                bold = true,
            )

            // Practice mode: show cents helper only when there's also a euros part
            if (isPractice) {
                val centsChange = problem.metadata["centsChange"]?.toIntOrNull() ?: 0
                val eurosChange = problem.metadata["eurosChange"]?.toIntOrNull() ?: 0

                if (eurosChange > 0) {
                    Spacer(modifier = Modifier.height(12.dp))

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
                        thickness = 1.dp,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ReceiptLine(
                        label = "Change cents",
                        amount = "${centsChange}\u00A2",
                        amountColor = MaterialTheme.colorScheme.primary,
                        bold = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReceiptLine(
    label: String,
    amount: String,
    amountColor: Color? = null,
    bold: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = amount,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            ),
            color = amountColor ?: MaterialTheme.colorScheme.onSurface,
        )
    }
}
