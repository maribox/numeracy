package it.bosler.numeracy.ui.component.question

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.bosler.numeracy.model.Difficulty
import it.bosler.numeracy.model.Problem
import numeracy.composeapp.generated.resources.Res
import numeracy.composeapp.generated.resources.stix_two_text_bold
import numeracy.composeapp.generated.resources.stix_two_text_bold_italic
import numeracy.composeapp.generated.resources.stix_two_text_italic
import numeracy.composeapp.generated.resources.stix_two_text_regular
import org.jetbrains.compose.resources.Font

// Color tracks — each tracks a value through the computation
private val trackA = Color(0xFF64B5F6) // blue — first operand / track
private val trackB = Color(0xFFF48FB1) // pink — second operand / track
private val trackC = Color(0xFFFFD54F) // amber — third value / cross-term
private val trackD = Color(0xFF4DB6AC) // teal — fourth value
private val trackR = Color(0xFF81C784) // green — final result only
private val opOrange = Color(0xFFFFB74D)

private val trackColors = mapOf(
    'a' to trackA,
    'b' to trackB,
    'c' to trackC,
    'd' to trackD,
    'r' to trackR,
)

@Composable
fun stixMathFontFamily(): FontFamily = FontFamily(
    Font(Res.font.stix_two_text_regular, FontWeight.Normal, FontStyle.Normal),
    Font(Res.font.stix_two_text_bold, FontWeight.Bold, FontStyle.Normal),
    Font(Res.font.stix_two_text_italic, FontWeight.Normal, FontStyle.Italic),
    Font(Res.font.stix_two_text_bold_italic, FontWeight.Bold, FontStyle.Italic),
)

/**
 * Renders a math string with markup into styled AnnotatedString.
 *
 * Markup rules:
 * - `#a{...}` through `#r{...}` → colored text track (a=blue, b=pink, c=amber, d=teal, r=green result)
 * - Operators (×, ÷, +, −, =, →, ·, ±) get subtle orange coloring
 * - Unicode ² ³ are rendered as-is (use for superscripts in plain text)
 * - Everything else inherits the base color
 */
private fun parseMathText(
    text: String,
    baseColor: Color,
): AnnotatedString = buildAnnotatedString {
    var i = 0
    val len = text.length

    while (i < len) {
        when {
            // Color group: #x{content} where x is a track letter
            i + 2 < len && text[i] == '#' && text[i + 1] in trackColors && text[i + 2] == '{' -> {
                val color = trackColors[text[i + 1]]!!
                i += 3
                val start = i
                var depth = 1
                while (i < len && depth > 0) {
                    if (text[i] == '{') depth++
                    else if (text[i] == '}') depth--
                    if (depth > 0) i++
                }
                val content = text.substring(start, i)
                if (i < len) i++ // skip closing '}'
                withStyle(SpanStyle(
                    color = color,
                    fontWeight = FontWeight.Bold,
                )) {
                    append(content)
                }
            }

            // Math operators — subtle coloring, spacing
            text[i] in listOf('\u00D7', '\u00F7', '+', '\u2212', '=', '\u2192', '\u00B7', '\u00B1') -> {
                withStyle(SpanStyle(color = opOrange.copy(alpha = 0.8f))) {
                    append(" ")
                    append(text[i])
                    append(" ")
                }
                i++
            }

            // Everything else — base color
            else -> {
                withStyle(SpanStyle(color = baseColor)) {
                    append(text[i])
                }
                i++
            }
        }
    }
}


/**
 * Math question display with difficulty-aware helpers.
 *
 * - NORMAL: Just the expression. User solves it entirely in their head.
 * - PRACTICE: Shows the mental trick / decomposition strategy as a badge.
 *   Multiple tricks may be available — tap to cycle through them.
 * - LEARNING: Full step-by-step breakdown of the currently selected trick.
 */
@Composable
fun MathQuestionDisplay(problem: Problem, difficulty: Difficulty = Difficulty.NORMAL) {
    val isPractice = difficulty == Difficulty.PRACTICE || difficulty == Difficulty.LEARNING
    val isLearning = difficulty == Difficulty.LEARNING
    val mathFont = stixMathFontFamily()

    val trickCount = (problem.metadata["trickCount"] ?: "1").toIntOrNull() ?: 1
    var selectedTrickIndex by remember(problem) { mutableStateOf(0) }

    val trickName = problem.metadata["trickName$selectedTrickIndex"] ?: problem.metadata["trickName"] ?: ""
    val trickHint = problem.metadata["trickHint$selectedTrickIndex"] ?: problem.metadata["trick"] ?: ""
    val stepCount = (problem.metadata["trick${selectedTrickIndex}_stepCount"] ?: "0").toIntOrNull() ?: 0
    val steps = (1..stepCount).mapNotNull { problem.metadata["trick${selectedTrickIndex}_step$it"] }
        .ifEmpty {
            buildList {
                var i = 1
                while (true) {
                    val step = problem.metadata["step$i"] ?: break
                    add(step)
                    i++
                }
            }
        }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // The question — big and bold, in math serif font
        Text(
            text = problem.questionText,
            style = MaterialTheme.typography.displayLarge.copy(
                fontFamily = mathFont,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
            ),
            color = MaterialTheme.colorScheme.onBackground,
        )

        // PRACTICE: Show the mental trick with cycling
        if (isPractice) {
            Spacer(modifier = Modifier.height(16.dp))

            val containerColor = MaterialTheme.colorScheme.primaryContainer
            val contentColor = MaterialTheme.colorScheme.onPrimaryContainer

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(containerColor)
                    .clickable(enabled = trickCount > 1) {
                        selectedTrickIndex = (selectedTrickIndex + 1) % trickCount
                    }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = parseMathText(trickName, contentColor.copy(alpha = 0.7f)),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = mathFont,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                        ),
                    )
                    if (trickCount > 1) {
                        Text(
                            text = "${selectedTrickIndex + 1}/$trickCount  \u25B6",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            color = contentColor.copy(alpha = 0.5f),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = parseMathText(trickHint, contentColor),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = mathFont,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 24.sp,
                        fontSize = 16.sp,
                    ),
                )
            }
        }

        // LEARNING: Full step-by-step
        if (isLearning && steps.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            MathSteps(steps)
        }
    }
}

@Composable
private fun MathSteps(steps: List<String>) {
    val surfaceColor = MaterialTheme.colorScheme.onSurface
    val mathFont = stixMathFontFamily()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(14.dp),
    ) {
        Column {
            Text(
                text = "Step by step",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(6.dp))

            steps.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                ) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = mathFont,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = opOrange,
                        ),
                        modifier = Modifier.width(28.dp),
                    )
                    Text(
                        text = parseMathText(step, surfaceColor),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = mathFont,
                            fontSize = 16.sp,
                            lineHeight = 26.sp,
                        ),
                    )
                }
            }
        }
    }
}
