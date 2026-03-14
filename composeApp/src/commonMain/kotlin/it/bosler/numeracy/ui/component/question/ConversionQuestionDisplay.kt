package it.bosler.numeracy.ui.component.question

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.bosler.numeracy.model.Difficulty
import it.bosler.numeracy.model.Problem

// Colors for step-by-step hints
private val numBlue = Color(0xFF64B5F6)
private val opOrange = Color(0xFFFFB74D)
private val resultGreen = Color(0xFF81C784)

// ═══════════════════════════════════════════
// LENGTH -Road sign visual
// ═══════════════════════════════════════════

@Composable
fun LengthConversionQuestionDisplay(problem: Problem, difficulty: Difficulty = Difficulty.NORMAL) {
    val value = problem.metadata["value"] ?: ""
    val fromUnit = problem.metadata["fromUnit"] ?: ""
    val toUnit = problem.metadata["toUnit"] ?: ""
    val context = problem.metadata["context"] ?: ""
    val isPractice = difficulty == Difficulty.PRACTICE || difficulty == Difficulty.LEARNING
    val isLearning = difficulty == Difficulty.LEARNING

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Road sign shape
        Box(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF2E7D32))
                .padding(2.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF388E3C))
                .padding(vertical = 24.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = context.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 3.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = Color.White.copy(alpha = 0.7f),
                )
                Text(
                    text = "$value $fromUnit",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                    ),
                    color = Color.White,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "▼",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 18.sp,
                )
                Text(
                    text = "? $toUnit",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Color(0xFFFFEB3B),
                )
            }
        }

        if (isPractice) {
            Spacer(modifier = Modifier.height(12.dp))
            ConversionTrickBadge(problem.metadata["trick"] ?: "")
        }

        if (isLearning) {
            Spacer(modifier = Modifier.height(8.dp))
            ConversionSteps(problem.metadata["trickSteps"] ?: "")
        }
    }
}

// ═══════════════════════════════════════════
// WEIGHT -Kitchen scale visual
// ═══════════════════════════════════════════

@Composable
fun WeightConversionQuestionDisplay(problem: Problem, difficulty: Difficulty = Difficulty.NORMAL) {
    val value = problem.metadata["value"] ?: ""
    val fromUnit = problem.metadata["fromUnit"] ?: ""
    val toUnit = problem.metadata["toUnit"] ?: ""
    val context = problem.metadata["context"] ?: ""
    val isPractice = difficulty == Difficulty.PRACTICE || difficulty == Difficulty.LEARNING
    val isLearning = difficulty == Difficulty.LEARNING

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Scale display
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF263238))
                .padding(4.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF37474F))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = context.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                    color = Color(0xFF80CBC4),
                )
                Spacer(modifier = Modifier.height(8.dp))
                // LCD-style readout
                Text(
                    text = "$value $fromUnit",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                    ),
                    color = Color(0xFF76FF03),
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Target unit
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "→",
                        color = Color(0xFF80CBC4),
                        fontSize = 20.sp,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "? $toUnit",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                        ),
                        color = Color(0xFFFFD740),
                    )
                }
            }
        }

        if (isPractice) {
            Spacer(modifier = Modifier.height(12.dp))
            ConversionTrickBadge(problem.metadata["trick"] ?: "")
        }

        if (isLearning) {
            Spacer(modifier = Modifier.height(8.dp))
            ConversionSteps(problem.metadata["trickSteps"] ?: "")
        }
    }
}

// ═══════════════════════════════════════════
// TEMPERATURE -Thermometer visual
// ═══════════════════════════════════════════

@Composable
fun TemperatureConversionQuestionDisplay(problem: Problem, difficulty: Difficulty = Difficulty.NORMAL) {
    val value = problem.metadata["value"] ?: ""
    val fromUnit = problem.metadata["fromUnit"] ?: ""
    val toUnit = problem.metadata["toUnit"] ?: ""
    val context = problem.metadata["context"] ?: ""
    val tempNorm = (problem.metadata["tempNorm"] ?: "0.5").toFloatOrNull() ?: 0.5f
    val isPractice = difficulty == Difficulty.PRACTICE || difficulty == Difficulty.LEARNING
    val isLearning = difficulty == Difficulty.LEARNING

    // Thermometer color based on temperature
    val mercuryColor = when {
        tempNorm < 0.2f -> Color(0xFF1565C0)  // cold blue
        tempNorm < 0.4f -> Color(0xFF00897B)  // cool teal
        tempNorm < 0.6f -> Color(0xFF43A047)  // mild green
        tempNorm < 0.8f -> Color(0xFFFF8F00)  // warm amber
        else -> Color(0xFFD32F2F)             // hot red
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Thermometer
            Canvas(modifier = Modifier.size(40.dp, 140.dp)) {
                val w = size.width
                val h = size.height
                val tubeW = w * 0.35f
                val bulbR = w * 0.4f
                val tubeX = (w - tubeW) / 2f
                val tubeTop = 8f
                val tubeBottom = h - bulbR * 2f

                // Tube outline
                drawRoundRect(
                    color = Color(0xFFE0E0E0),
                    topLeft = Offset(tubeX - 2f, tubeTop - 2f),
                    size = Size(tubeW + 4f, tubeBottom - tubeTop + bulbR + 4f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(tubeW),
                )

                // Tube fill (mercury)
                val fillHeight = (tubeBottom - tubeTop) * tempNorm
                val fillTop = tubeBottom - fillHeight
                drawRoundRect(
                    color = mercuryColor,
                    topLeft = Offset(tubeX, fillTop),
                    size = Size(tubeW, tubeBottom - fillTop + bulbR),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(tubeW),
                )

                // Bulb
                drawCircle(
                    color = mercuryColor,
                    radius = bulbR,
                    center = Offset(w / 2f, h - bulbR),
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Temperature value and conversion
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = context.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "$value$fromUnit",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                    ),
                    color = mercuryColor,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "▼",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    fontSize = 18.sp,
                )
                Text(
                    text = "?$toUnit",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        if (isPractice) {
            Spacer(modifier = Modifier.height(12.dp))
            val nearC = problem.metadata["nearestLandmarkC"] ?: ""
            val nearF = problem.metadata["nearestLandmarkF"] ?: ""
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ConversionTrickBadge(problem.metadata["trick"] ?: "")
                HelperBadge(
                    label = "Landmark",
                    value = "$nearC = $nearF",
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }

        if (isLearning) {
            Spacer(modifier = Modifier.height(8.dp))
            ConversionSteps(problem.metadata["trickSteps"] ?: "")
        }
    }
}

// ═══════════════════════════════════════════
// VOLUME -Measuring cup visual
// ═══════════════════════════════════════════

@Composable
fun VolumeConversionQuestionDisplay(problem: Problem, difficulty: Difficulty = Difficulty.NORMAL) {
    val value = problem.metadata["value"] ?: ""
    val fromUnit = problem.metadata["fromUnit"] ?: ""
    val toUnit = problem.metadata["toUnit"] ?: ""
    val context = problem.metadata["context"] ?: ""
    val isPractice = difficulty == Difficulty.PRACTICE || difficulty == Difficulty.LEARNING
    val isLearning = difficulty == Difficulty.LEARNING

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Measuring cup
            Canvas(modifier = Modifier.size(60.dp, 100.dp)) {
                val w = size.width
                val h = size.height
                val cupColor = Color(0xFF90CAF9)
                val liquidColor = Color(0xFF42A5F5)
                val fillLevel = 0.65f

                // Cup outline (trapezoid)
                val cupPath = Path().apply {
                    moveTo(w * 0.15f, h * 0.1f)   // top-left
                    lineTo(w * 0.85f, h * 0.1f)   // top-right
                    lineTo(w * 0.78f, h * 0.95f)   // bottom-right
                    lineTo(w * 0.22f, h * 0.95f)   // bottom-left
                    close()
                }
                drawPath(cupPath, cupColor, style = Stroke(width = 3f))

                // Liquid fill
                val liquidTop = h * (0.95f - fillLevel * 0.85f)
                val leftAtFill = w * (0.15f + (0.22f - 0.15f) * (liquidTop - h * 0.1f) / (h * 0.85f))
                val rightAtFill = w * (0.85f - (0.85f - 0.78f) * (liquidTop - h * 0.1f) / (h * 0.85f))
                val liquidPath = Path().apply {
                    moveTo(leftAtFill, liquidTop)
                    lineTo(rightAtFill, liquidTop)
                    lineTo(w * 0.78f, h * 0.95f)
                    lineTo(w * 0.22f, h * 0.95f)
                    close()
                }
                drawPath(liquidPath, liquidColor.copy(alpha = 0.6f))

                // Measurement lines
                for (i in 1..4) {
                    val y = h * (0.1f + i * 0.17f)
                    val lx = w * (0.15f + (0.22f - 0.15f) * (y - h * 0.1f) / (h * 0.85f))
                    drawLine(cupColor, Offset(lx, y), Offset(lx + w * 0.12f, y), strokeWidth = 1.5f)
                }

                // Handle
                drawArc(
                    color = cupColor,
                    startAngle = -60f,
                    sweepAngle = 120f,
                    useCenter = false,
                    topLeft = Offset(w * 0.78f, h * 0.25f),
                    size = Size(w * 0.25f, h * 0.4f),
                    style = Stroke(width = 3f),
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = context.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "$value $fromUnit",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                    ),
                    color = Color(0xFF1565C0),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "▼",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    fontSize = 18.sp,
                )
                Text(
                    text = "? $toUnit",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        if (isPractice) {
            Spacer(modifier = Modifier.height(12.dp))
            ConversionTrickBadge(problem.metadata["trick"] ?: "")
        }

        if (isLearning) {
            Spacer(modifier = Modifier.height(8.dp))
            ConversionSteps(problem.metadata["trickSteps"] ?: "")
        }
    }
}

// ═══════════════════════════════════════════
// SPEED -Speedometer gauge visual
// ═══════════════════════════════════════════

@Composable
fun SpeedConversionQuestionDisplay(problem: Problem, difficulty: Difficulty = Difficulty.NORMAL) {
    val value = problem.metadata["value"] ?: ""
    val fromUnit = problem.metadata["fromUnit"] ?: ""
    val toUnit = problem.metadata["toUnit"] ?: ""
    val context = problem.metadata["context"] ?: ""
    val needleNorm = (problem.metadata["needleNorm"] ?: "0.5").toFloatOrNull() ?: 0.5f
    val isPractice = difficulty == Difficulty.PRACTICE || difficulty == Difficulty.LEARNING
    val isLearning = difficulty == Difficulty.LEARNING

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Speedometer gauge
        Box(
            modifier = Modifier.size(180.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(180.dp)) {
                val w = size.width
                val h = size.height
                val cx = w / 2f
                val cy = h * 0.55f
                val radius = w * 0.42f
                val gaugeColor = Color(0xFF455A64)
                val tickColor = Color(0xFF90A4AE)
                val needleColor = Color(0xFFD32F2F)

                // Arc background
                drawArc(
                    color = gaugeColor,
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = Offset(cx - radius, cy - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = 12f, cap = StrokeCap.Round),
                )

                // Colored arc fill
                val fillAngle = 270f * needleNorm
                drawArc(
                    color = when {
                        needleNorm < 0.3f -> Color(0xFF4CAF50)
                        needleNorm < 0.6f -> Color(0xFFFFB300)
                        needleNorm < 0.8f -> Color(0xFFFF9800)
                        else -> Color(0xFFD32F2F)
                    },
                    startAngle = 135f,
                    sweepAngle = fillAngle,
                    useCenter = false,
                    topLeft = Offset(cx - radius, cy - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = 12f, cap = StrokeCap.Round),
                )

                // Tick marks
                for (i in 0..10) {
                    val angle = 135f + i * 27f
                    val rad = Math.toRadians(angle.toDouble())
                    val cos = kotlin.math.cos(rad).toFloat()
                    val sin = kotlin.math.sin(rad).toFloat()
                    val innerR = radius - 18f
                    val outerR = radius - 8f
                    drawLine(
                        tickColor,
                        Offset(cx + innerR * cos, cy + innerR * sin),
                        Offset(cx + outerR * cos, cy + outerR * sin),
                        strokeWidth = if (i % 5 == 0) 3f else 1.5f,
                    )
                }

                // Needle
                val needleAngle = 135f + 270f * needleNorm
                val needleRad = Math.toRadians(needleAngle.toDouble())
                val needleCos = kotlin.math.cos(needleRad).toFloat()
                val needleSin = kotlin.math.sin(needleRad).toFloat()
                val needleLen = radius - 22f
                drawLine(
                    needleColor,
                    Offset(cx, cy),
                    Offset(cx + needleLen * needleCos, cy + needleLen * needleSin),
                    strokeWidth = 4f,
                    cap = StrokeCap.Round,
                )
                // Center dot
                drawCircle(needleColor, 6f, Offset(cx, cy))
                drawCircle(Color(0xFF37474F), 3f, Offset(cx, cy))
            }

            // Speed text overlay
            Column(
                modifier = Modifier.padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = fromUnit,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Context + target
        Text(
            text = context.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "? $toUnit",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
        )

        if (isPractice) {
            Spacer(modifier = Modifier.height(12.dp))
            ConversionTrickBadge(problem.metadata["trick"] ?: "")
        }

        if (isLearning) {
            Spacer(modifier = Modifier.height(8.dp))
            ConversionSteps(problem.metadata["trickSteps"] ?: "")
        }
    }
}

// ═══════════════════════════════════════════
// Shared helpers
// ═══════════════════════════════════════════

@Composable
private fun ConversionTrickBadge(trick: String) {
    HelperBadge(
        label = "Mental trick",
        value = trick,
        color = MaterialTheme.colorScheme.primaryContainer,
        textColor = MaterialTheme.colorScheme.onPrimaryContainer,
    )
}

@Composable
private fun ConversionSteps(steps: String) {
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

            for (line in steps.split("\n")) {
                Text(
                    text = buildAnnotatedString {
                        // Parse the line and color numbers, operators, results
                        var i = 0
                        val chars = line.toCharArray()
                        while (i < chars.size) {
                            when {
                                chars[i].isDigit() || (chars[i] == '-' && i + 1 < chars.size && chars[i + 1].isDigit()) -> {
                                    val start = i
                                    if (chars[i] == '-') i++
                                    while (i < chars.size && (chars[i].isDigit() || chars[i] == '.')) i++
                                    withStyle(SpanStyle(color = numBlue, fontWeight = FontWeight.Bold)) {
                                        append(line.substring(start, i))
                                    }
                                }
                                chars[i] in listOf('×', '÷', '+', '−', '=', '→', '≈') -> {
                                    withStyle(SpanStyle(color = opOrange, fontWeight = FontWeight.Bold)) {
                                        append(chars[i])
                                    }
                                    i++
                                }
                                else -> {
                                    append(chars[i])
                                    i++
                                }
                            }
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
