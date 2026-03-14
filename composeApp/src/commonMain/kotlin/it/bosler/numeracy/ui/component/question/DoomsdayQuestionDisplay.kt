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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.bosler.numeracy.model.Difficulty
import it.bosler.numeracy.model.Problem

/**
 * Doomsday question display with difficulty-aware helpers.
 *
 * - NORMAL: Calendar page with date. User runs the full Doomsday algorithm.
 * - PRACTICE: Step-by-step algorithm guidance with intermediate results revealed.
 * - LEARNING: Full worked solution with all steps and the final answer.
 */
@Composable
fun DoomsdayQuestionDisplay(problem: Problem, difficulty: Difficulty = Difficulty.NORMAL) {
    val day = problem.metadata["day"] ?: "1"
    val monthName = problem.metadata["monthName"] ?: "January"
    val year = problem.metadata["year"] ?: "2000"
    val isPractice = difficulty == Difficulty.PRACTICE || difficulty == Difficulty.LEARNING
    val isLearning = difficulty == Difficulty.LEARNING

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Calendar page
        Box(
            modifier = Modifier
                .width(200.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Month header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.tertiary)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = monthName.uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 3.sp,
                        ),
                        color = MaterialTheme.colorScheme.onTertiary,
                    )
                }

                // Day number
                Text(
                    text = day,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 80.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 8.dp),
                )

                // Year
                Text(
                    text = year,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }
        }

        if (isPractice) {
            Spacer(modifier = Modifier.height(16.dp))
            PracticeSteps(problem, isLearning)
        }
    }
}

@Composable
private fun PracticeSteps(problem: Problem, isLearning: Boolean) {
    val centuryAnchor = problem.metadata["centuryAnchor"] ?: ""
    val centuryLabel = problem.metadata["centuryLabel"] ?: ""
    val yearDoomsday = problem.metadata["yearDoomsday"] ?: ""
    val yy = problem.metadata["yy"] ?: ""
    val yyDiv12 = problem.metadata["yyDiv12"] ?: ""
    val yyRemainder = problem.metadata["yyRemainder"] ?: ""
    val remainderDiv4 = problem.metadata["remainderDiv4"] ?: ""
    val yearCalcSum = problem.metadata["yearCalcSum"] ?: ""
    val centuryAnchorIndex = problem.metadata["centuryAnchorIndex"] ?: ""
    val monthName = problem.metadata["monthName"] ?: ""
    val doomsdayRef = problem.metadata["doomsdayRef"] ?: ""
    val monthAnchorDate = problem.metadata["monthAnchorDate"] ?: ""
    val monthMnemonic = problem.metadata["monthMnemonic"] ?: ""
    val diffFromRef = problem.metadata["diffFromRef"] ?: "0"
    val day = problem.metadata["day"] ?: ""

    // Distinct colors per value so you can trace each number through the calculation
    val colYY = Color(0xFF42A5F5)         // blue -the 2-digit year
    val colQuotient = Color(0xFFAB47BC)   // purple -yy ÷ 12 quotient
    val colRemainder = Color(0xFFFF7043)  // deep orange -remainder
    val colRemDiv4 = Color(0xFFEC407A)    // pink -remainder ÷ 4
    val colSum = Color(0xFFFFCA28)        // amber -sum of the three
    val colAnchor = Color(0xFF26A69A)     // teal -century anchor
    val colDoomsday = Color(0xFF66BB6A)   // green -final doomsday result
    val colRef = Color(0xFF7E57C2)        // deep purple -month anchor date
    val colDay = Color(0xFF42A5F5)        // blue -the target day
    val colDiff = Color(0xFFFF7043)       // deep orange -difference
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Step 1: Century anchor
        StepBadge(
            stepNumber = "1",
            label = "Century anchor",
            content = {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = labelColor)) {
                            append(centuryLabel)
                        }
                        withStyle(SpanStyle(color = labelColor)) {
                            append(" \u2192 ")
                        }
                        withStyle(SpanStyle(color = colAnchor, fontWeight = FontWeight.Bold)) {
                            append("$centuryAnchor ($centuryAnchorIndex)")
                        }
                    },
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            color = MaterialTheme.colorScheme.secondaryContainer,
            textColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )

        // Step 2: Year's doomsday calculation
        StepBadge(
            stepNumber = "2",
            label = "Year's doomsday",
            content = {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = colYY, fontWeight = FontWeight.Bold)) {
                            append(yy)
                        }
                        withStyle(SpanStyle(color = labelColor)) { append(" \u00F7 12 = ") }
                        withStyle(SpanStyle(color = colQuotient, fontWeight = FontWeight.Bold)) {
                            append(yyDiv12)
                        }
                        withStyle(SpanStyle(color = labelColor)) { append(" r ") }
                        withStyle(SpanStyle(color = colRemainder, fontWeight = FontWeight.Bold)) {
                            append(yyRemainder)
                        }
                        withStyle(SpanStyle(color = labelColor)) { append(",  ") }
                        withStyle(SpanStyle(color = colRemainder, fontWeight = FontWeight.Bold)) {
                            append(yyRemainder)
                        }
                        withStyle(SpanStyle(color = labelColor)) { append(" \u00F7 4 = ") }
                        withStyle(SpanStyle(color = colRemDiv4, fontWeight = FontWeight.Bold)) {
                            append(remainderDiv4)
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = colQuotient, fontWeight = FontWeight.Bold)) {
                            append(yyDiv12)
                        }
                        withStyle(SpanStyle(color = labelColor)) { append(" + ") }
                        withStyle(SpanStyle(color = colRemainder, fontWeight = FontWeight.Bold)) {
                            append(yyRemainder)
                        }
                        withStyle(SpanStyle(color = labelColor)) { append(" + ") }
                        withStyle(SpanStyle(color = colRemDiv4, fontWeight = FontWeight.Bold)) {
                            append(remainderDiv4)
                        }
                        withStyle(SpanStyle(color = labelColor)) { append(" = ") }
                        withStyle(SpanStyle(color = colSum, fontWeight = FontWeight.Bold)) {
                            append(yearCalcSum)
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = labelColor)) { append("(") }
                        withStyle(SpanStyle(color = colAnchor, fontWeight = FontWeight.Bold)) {
                            append(centuryAnchorIndex)
                        }
                        withStyle(SpanStyle(color = labelColor)) { append(" + ") }
                        withStyle(SpanStyle(color = colSum, fontWeight = FontWeight.Bold)) {
                            append(yearCalcSum)
                        }
                        withStyle(SpanStyle(color = labelColor)) { append(") mod 7 \u2192 ") }
                        withStyle(SpanStyle(color = colDoomsday, fontWeight = FontWeight.Bold)) {
                            append(yearDoomsday)
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            color = MaterialTheme.colorScheme.tertiaryContainer,
            textColor = MaterialTheme.colorScheme.onTertiaryContainer,
        )

        // Step 3: Month anchor
        StepBadge(
            stepNumber = "3",
            label = "$monthName's Doomsday date",
            content = {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = colRef, fontWeight = FontWeight.Bold)) {
                            append("$monthName $monthAnchorDate")
                        }
                        withStyle(SpanStyle(color = labelColor)) {
                            append(" falls on ")
                        }
                        withStyle(SpanStyle(color = colDoomsday, fontWeight = FontWeight.Bold)) {
                            append(yearDoomsday)
                        }
                    },
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = monthMnemonic,
                    style = MaterialTheme.typography.bodySmall,
                    color = labelColor,
                )
            },
            color = MaterialTheme.colorScheme.primaryContainer,
            textColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )

        // Step 4: Count from anchor (only in learning mode)
        if (isLearning) {
            StepBadge(
                stepNumber = "4",
                label = "Count from anchor",
                content = {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = colDay, fontWeight = FontWeight.Bold)) {
                                append(day)
                            }
                            withStyle(SpanStyle(color = labelColor)) { append(" \u2212 ") }
                            withStyle(SpanStyle(color = colRef, fontWeight = FontWeight.Bold)) {
                                append(doomsdayRef)
                            }
                            withStyle(SpanStyle(color = labelColor)) { append(" = ") }
                            withStyle(SpanStyle(color = colDiff, fontWeight = FontWeight.Bold)) {
                                append("$diffFromRef days")
                            }
                        },
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    if (diffFromRef != "0") {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = colDoomsday, fontWeight = FontWeight.Bold)) {
                                    append(yearDoomsday)
                                }
                                withStyle(SpanStyle(color = labelColor)) { append(" + ") }
                                withStyle(SpanStyle(color = colDiff, fontWeight = FontWeight.Bold)) {
                                    append(diffFromRef)
                                }
                                withStyle(SpanStyle(color = labelColor)) { append(" \u2192 ") }
                                withStyle(SpanStyle(color = colDoomsday, fontWeight = FontWeight.Bold)) {
                                    append("???")
                                }
                            },
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                },
                color = MaterialTheme.colorScheme.errorContainer,
                textColor = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}

@Composable
private fun StepBadge(
    stepNumber: String,
    label: String,
    content: @Composable () -> Unit,
    color: Color,
    textColor: Color,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Step number circle
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(textColor.copy(alpha = 0.15f))
                    .padding(horizontal = 7.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stepNumber,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = textColor,
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = textColor,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        content()
    }
}
