package it.bosler.numeracy.ui.component.question

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.bosler.numeracy.model.Difficulty
import it.bosler.numeracy.model.Problem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DartsQuestionDisplay(
    problem: Problem,
    difficulty: Difficulty = Difficulty.NORMAL,
    hideScore: Boolean = false,
    answered: Boolean = false,
) {
    val currentScore = problem.metadata["currentScore"] ?: "301"
    val throwName = problem.metadata["throwName"] ?: ""
    val throwValue = problem.metadata["throwValue"] ?: "0"
    val throwValueInt = throwValue.toIntOrNull() ?: 0
    val currentScoreInt = currentScore.toIntOrNull() ?: 301

    val multiplier: Int
    val baseNumber: String
    val throwType: String
    when {
        throwName.startsWith("Triple ") -> {
            throwType = "Triple"; baseNumber = throwName.removePrefix("Triple "); multiplier = 3
        }
        throwName.startsWith("Double ") -> {
            throwType = "Double"; baseNumber = throwName.removePrefix("Double "); multiplier = 2
        }
        throwName.startsWith("Single ") -> {
            throwType = "Single"; baseNumber = throwName.removePrefix("Single "); multiplier = 1
        }
        throwName == "Bull" -> { throwType = "Bull"; baseNumber = "50"; multiplier = 1 }
        throwName == "Single Bull" -> { throwType = "S.Bull"; baseNumber = "25"; multiplier = 1 }
        else -> { throwType = throwName; baseNumber = throwValue; multiplier = 1 }
    }

    val typeColor = when (throwType) {
        "Triple" -> Color(0xFFC62828)
        "Double" -> Color(0xFF2E7D32)
        "Bull", "S.Bull" -> Color(0xFFE65100)
        else -> Color(0xFF455A64)
    }

    if (difficulty == Difficulty.LEARNING) {
        // LEARNING MODE: Visual column subtraction layout
        LearningModeDisplay(
            currentScore = currentScore,
            currentScoreInt = currentScoreInt,
            throwValueInt = throwValueInt,
            throwType = throwType,
            baseNumber = baseNumber,
            multiplier = multiplier,
            typeColor = typeColor,
            answered = answered,
        )
    } else {
        // NORMAL / HARD / PRACTICE: Animated throw display
        StandardModeDisplay(
            currentScore = currentScore,
            throwValue = throwValue,
            throwValueInt = throwValueInt,
            throwName = throwName,
            throwType = throwType,
            baseNumber = baseNumber,
            multiplier = multiplier,
            typeColor = typeColor,
            difficulty = difficulty,
            hideScore = hideScore,
            answered = answered,
        )
    }
}

@Composable
private fun LearningModeDisplay(
    currentScore: String,
    currentScoreInt: Int,
    throwValueInt: Int,
    throwType: String,
    baseNumber: String,
    multiplier: Int,
    typeColor: Color,
    answered: Boolean,
) {
    val borrowColor = Color(0xFFFF7043)
    val numColor = Color(0xFF64B5F6)
    val resultColor = Color(0xFF81C784)
    val opColor = Color(0xFFFFB74D)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

    // Compute column subtraction details
    val scoreDigits = intArrayOf(currentScoreInt / 100, (currentScoreInt / 10) % 10, currentScoreInt % 10)
    val throwPadded = throwValueInt.toString().padStart(3, '0')
    val throwDigits = intArrayOf(throwPadded[0].digitToInt(), throwPadded[1].digitToInt(), throwPadded[2].digitToInt())

    // Compute borrows and results
    val borrow = intArrayOf(0, 0, 0)
    val resultDigits = IntArray(3)
    // Ones column
    if (scoreDigits[2] < throwDigits[2]) {
        borrow[2] = 1
        resultDigits[2] = scoreDigits[2] + 10 - throwDigits[2]
    } else {
        resultDigits[2] = scoreDigits[2] - throwDigits[2]
    }
    // Tens column
    val tensTop = scoreDigits[1] - borrow[2]
    if (tensTop < throwDigits[1]) {
        borrow[1] = 1
        resultDigits[1] = tensTop + 10 - throwDigits[1]
    } else {
        resultDigits[1] = tensTop - throwDigits[1]
    }
    // Hundreds column
    resultDigits[0] = scoreDigits[0] - borrow[1] - throwDigits[0]

    val digitStyle = MaterialTheme.typography.displaySmall.copy(
        fontWeight = FontWeight.Black,
        fontSize = 44.sp,
    )
    val colW = 44.dp

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Throw badge with multiplication
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(typeColor)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            ) {
                Text(
                    text = throwType,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (multiplier > 1) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = numColor, fontWeight = FontWeight.Bold)) { append(baseNumber) }
                        withStyle(SpanStyle(color = opColor)) { append(" \u00D7 $multiplier") }
                        withStyle(SpanStyle(color = opColor)) { append(" = ") }
                        withStyle(SpanStyle(color = numColor, fontWeight = FontWeight.Bold)) { append(throwValueInt.toString()) }
                    },
                    style = MaterialTheme.typography.titleMedium,
                )
            } else {
                Text(
                    text = throwValueInt.toString(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = numColor,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Column subtraction card
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Borrow indicators row (above score)
            Row {
                // Space for the minus sign column
                Box(modifier = Modifier.width(colW))
                for (col in 0..2) {
                    Box(
                        modifier = Modifier
                            .width(colW)
                            .height(20.dp),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        if (borrow.getOrElse(col + 1) { 0 } == 1 || (col < 2 && borrow[col + 1] == 1)) {
                            // nothing here -we show borrows differently below
                        }
                    }
                }
            }

            // Score row with borrow indicators
            Row(verticalAlignment = Alignment.Bottom) {
                Box(modifier = Modifier.width(colW).height(56.dp))
                for (col in 0..2) {
                    Box(
                        modifier = Modifier.width(colW).height(56.dp),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        // The digit
                        Text(
                            text = scoreDigits[col].toString(),
                            style = digitStyle,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        // Borrow marker: the column to the RIGHT borrowed from this one
                        if (col < 2 && borrow[col + 1] == 1) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 4.dp, y = (-2).dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(borrowColor.copy(alpha = 0.2f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp),
                            ) {
                                Text(
                                    text = "\u22121",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                    ),
                                    color = borrowColor,
                                )
                            }
                        }
                    }
                }
            }

            // Throw row (subtraction line)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(colW), contentAlignment = Alignment.Center) {
                    Text(
                        text = "\u2212",
                        style = digitStyle.copy(fontSize = 36.sp),
                        color = typeColor,
                    )
                }
                for (col in 0..2) {
                    Box(modifier = Modifier.width(colW), contentAlignment = Alignment.Center) {
                        Text(
                            text = throwDigits[col].toString(),
                            style = digitStyle,
                            color = typeColor,
                        )
                    }
                }
            }

            // Divider line
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .width(colW * 4)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f)),
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Answer placeholder
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(colW), contentAlignment = Alignment.Center) {
                    Text(
                        text = "=",
                        style = digitStyle.copy(fontSize = 30.sp),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f),
                    )
                }
                Text(
                    text = "?",
                    style = digitStyle.copy(fontSize = 40.sp),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Step-by-step hints -well formatted with color coding
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            // Column labels
            val columnNames = listOf("Hundreds", "Tens", "Ones")

            // Ones column hint
            if (throwDigits[2] > 0) {
                StepHint(
                    column = "Ones",
                    top = scoreDigits[2],
                    sub = throwDigits[2],
                    borrows = borrow[2] == 1,
                    result = resultDigits[2],
                    carryFromPrev = false,
                    numColor = numColor,
                    opColor = opColor,
                    borrowColor = borrowColor,
                    resultColor = resultColor,
                    labelColor = labelColor,
                )
            }
            // Tens column hint
            if (throwDigits[1] > 0 || borrow[2] == 1) {
                if (throwDigits[2] > 0) Spacer(modifier = Modifier.height(6.dp))
                StepHint(
                    column = "Tens",
                    top = scoreDigits[1],
                    sub = throwDigits[1],
                    borrows = borrow[1] == 1,
                    result = resultDigits[1],
                    carryFromPrev = borrow[2] == 1,
                    numColor = numColor,
                    opColor = opColor,
                    borrowColor = borrowColor,
                    resultColor = resultColor,
                    labelColor = labelColor,
                )
            }
            // Hundreds column hint
            if (throwDigits[0] > 0 || borrow[1] == 1) {
                Spacer(modifier = Modifier.height(6.dp))
                StepHint(
                    column = "Hundreds",
                    top = scoreDigits[0],
                    sub = throwDigits[0],
                    borrows = false,
                    result = resultDigits[0],
                    carryFromPrev = borrow[1] == 1,
                    numColor = numColor,
                    opColor = opColor,
                    borrowColor = borrowColor,
                    resultColor = resultColor,
                    labelColor = labelColor,
                )
            }
        }
    }
}

@Composable
private fun StepHint(
    column: String,
    top: Int,
    sub: Int,
    borrows: Boolean,
    result: Int,
    carryFromPrev: Boolean,
    numColor: Color,
    opColor: Color,
    borrowColor: Color,
    resultColor: Color,
    labelColor: Color,
) {
    val textStyle = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp)

    Column {
        // Column label
        Text(
            text = column,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
            ),
            color = labelColor,
        )
        Spacer(modifier = Modifier.height(2.dp))

        val effectiveTop = if (carryFromPrev) top - 1 else top

        if (carryFromPrev && !borrows) {
            // Carry applied, simple subtraction
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = numColor)) { append("$top") }
                    withStyle(SpanStyle(color = borrowColor, fontWeight = FontWeight.Bold)) { append(" \u2212 1") }
                    withStyle(SpanStyle(color = labelColor)) { append(" (carry)") }
                    withStyle(SpanStyle(color = opColor)) { append(" = ") }
                    withStyle(SpanStyle(color = numColor)) { append("$effectiveTop") }
                    withStyle(SpanStyle(color = opColor)) { append(" \u2212 ") }
                    withStyle(SpanStyle(color = numColor)) { append("$sub") }
                    withStyle(SpanStyle(color = opColor)) { append(" = ") }
                    withStyle(SpanStyle(color = resultColor, fontWeight = FontWeight.Bold)) { append("$result") }
                },
                style = textStyle,
            )
        } else if (borrows && !carryFromPrev) {
            // Need to borrow -show complement method
            val complement = 10 - sub
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = numColor)) { append("$top") }
                    withStyle(SpanStyle(color = opColor)) { append(" < ") }
                    withStyle(SpanStyle(color = numColor)) { append("$sub") }
                    withStyle(SpanStyle(color = labelColor)) { append(" \u2192 borrow!") }
                },
                style = textStyle,
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = labelColor)) { append("complement: ") }
                    withStyle(SpanStyle(color = opColor)) { append("10 \u2212 ") }
                    withStyle(SpanStyle(color = numColor)) { append("$sub") }
                    withStyle(SpanStyle(color = opColor)) { append(" = ") }
                    withStyle(SpanStyle(color = numColor, fontWeight = FontWeight.Bold)) { append("$complement") }
                    withStyle(SpanStyle(color = opColor)) { append(" + ") }
                    withStyle(SpanStyle(color = numColor)) { append("$top") }
                    withStyle(SpanStyle(color = opColor)) { append(" = ") }
                    withStyle(SpanStyle(color = resultColor, fontWeight = FontWeight.Bold)) { append("$result") }
                    withStyle(SpanStyle(color = borrowColor, fontWeight = FontWeight.Bold)) { append("  carry 1") }
                },
                style = textStyle,
            )
        } else if (borrows && carryFromPrev) {
            // Carry AND borrow
            val complement = 10 - sub
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = numColor)) { append("$top") }
                    withStyle(SpanStyle(color = borrowColor, fontWeight = FontWeight.Bold)) { append(" \u2212 1") }
                    withStyle(SpanStyle(color = labelColor)) { append(" (carry)") }
                    withStyle(SpanStyle(color = opColor)) { append(" = ") }
                    withStyle(SpanStyle(color = numColor)) { append("$effectiveTop") }
                    withStyle(SpanStyle(color = opColor)) { append(" < ") }
                    withStyle(SpanStyle(color = numColor)) { append("$sub") }
                    withStyle(SpanStyle(color = labelColor)) { append(" \u2192 borrow!") }
                },
                style = textStyle,
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = opColor)) { append("10 \u2212 ") }
                    withStyle(SpanStyle(color = numColor)) { append("$sub") }
                    withStyle(SpanStyle(color = opColor)) { append(" = ") }
                    withStyle(SpanStyle(color = numColor, fontWeight = FontWeight.Bold)) { append("$complement") }
                    withStyle(SpanStyle(color = opColor)) { append(" + ") }
                    withStyle(SpanStyle(color = numColor)) { append("$effectiveTop") }
                    withStyle(SpanStyle(color = opColor)) { append(" = ") }
                    withStyle(SpanStyle(color = resultColor, fontWeight = FontWeight.Bold)) { append("$result") }
                    withStyle(SpanStyle(color = borrowColor, fontWeight = FontWeight.Bold)) { append("  carry 1") }
                },
                style = textStyle,
            )
        } else {
            // Simple subtraction, no carry, no borrow
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = numColor)) { append("$top") }
                    withStyle(SpanStyle(color = opColor)) { append(" \u2212 ") }
                    withStyle(SpanStyle(color = numColor)) { append("$sub") }
                    withStyle(SpanStyle(color = opColor)) { append(" = ") }
                    withStyle(SpanStyle(color = resultColor, fontWeight = FontWeight.Bold)) { append("$result") }
                },
                style = textStyle,
            )
        }
    }
}

@Composable
private fun StandardModeDisplay(
    currentScore: String,
    throwValue: String,
    throwValueInt: Int,
    throwName: String,
    throwType: String,
    baseNumber: String,
    multiplier: Int,
    typeColor: Color,
    difficulty: Difficulty,
    hideScore: Boolean,
    answered: Boolean,
) {
    val problemKey = "$currentScore-$throwName"
    val isPractice = difficulty == Difficulty.PRACTICE

    // Flying number state
    var flyingText by remember(problemKey) { mutableStateOf(baseNumber) }
    val flyScale = remember(problemKey) { Animatable(1f) }
    val flyOffsetY = remember(problemKey) { Animatable(0f) }
    val flyAlpha = remember(problemKey) { Animatable(1f) }
    val dockedAlpha = remember(problemKey) { Animatable(0f) }
    val scoreBump = remember(problemKey) { Animatable(1f) }

    // Entrance pop
    val throwEntrance = remember(problemKey) { Animatable(0.5f) }

    // In Practice: show product immediately
    LaunchedEffect(problemKey, difficulty) {
        flyingText = baseNumber
        flyScale.snapTo(1f)
        flyOffsetY.snapTo(0f)
        flyAlpha.snapTo(1f)
        dockedAlpha.snapTo(0f)
        scoreBump.snapTo(1f)
        throwEntrance.snapTo(0.5f)

        throwEntrance.animateTo(1f, tween(200, easing = FastOutSlowInEasing))

        if (isPractice && multiplier > 1) {
            delay(300)
            flyingText = throwValueInt.toString()
            flyScale.snapTo(1.1f)
            flyScale.animateTo(1f, tween(150))
        }
    }

    // Bump animation triggers AFTER user answers
    LaunchedEffect(answered) {
        if (!answered) return@LaunchedEffect

        // In Normal/Hard, morph now (wasn't shown before)
        if (!isPractice && multiplier > 1) {
            delay(50)
            flyingText = throwValueInt.toString()
            flyScale.snapTo(1.1f)
            flyScale.animateTo(1f, tween(120))
            delay(100)
        }

        launch { flyScale.animateTo(0.4f, tween(280, easing = FastOutSlowInEasing)) }
        launch { flyOffsetY.animateTo(-180f, tween(280, easing = FastOutSlowInEasing)) }
        launch { flyAlpha.animateTo(0f, tween(250, delayMillis = 50)) }
        launch { dockedAlpha.animateTo(1f, tween(150, delayMillis = 200)) }

        delay(250)
        scoreBump.animateTo(1.06f, tween(70))
        scoreBump.animateTo(1f, tween(100))
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            horizontalAlignment = Alignment.End,
        ) {
            // Score odometer
            if (!hideScore) {
                Box(contentAlignment = Alignment.CenterEnd) {
                    Box(modifier = Modifier.scale(scoreBump.value)) {
                        ScoreOdometer(score = currentScore)
                    }

                    // Docked "−value" after bump
                    Text(
                        text = "\u2212$throwValue",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = typeColor.copy(alpha = dockedAlpha.value),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(y = 28.dp)
                            .alpha(dockedAlpha.value),
                    )
                }
            } else {
                Text(
                    text = "? ? ?",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 64.sp,
                        letterSpacing = 8.sp,
                    ),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f),
                )
            }

            // Type badge + multiplication hint for Practice
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                Box(
                    modifier = Modifier
                        .scale(throwEntrance.value)
                        .clip(RoundedCornerShape(8.dp))
                        .background(typeColor)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = throwType,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = Color.White,
                    )
                }
                // In Practice mode, show the multiplication clearly
                if (isPractice && multiplier > 1) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$baseNumber\u00D7$multiplier",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = typeColor.copy(alpha = 0.7f),
                    )
                }
            }

            // Throw number
            Text(
                text = flyingText,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 64.sp,
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .scale(flyScale.value)
                    .offset { IntOffset(0, flyOffsetY.value.toInt()) }
                    .alpha(flyAlpha.value),
            )
        }

        // Practice mode: show subtraction strategy hint below
        if (isPractice && !hideScore) {
            val scoreInt = currentScore.toIntOrNull() ?: 0
            val onesScore = scoreInt % 10
            val onesSub = throwValueInt % 10

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                val hintText = buildAnnotatedString {
                    val numC = Color(0xFF64B5F6)
                    val opC = Color(0xFFFFB74D)
                    val borrowC = Color(0xFFFF7043)

                    if (throwValueInt >= 40 && onesSub != 0) {
                        // Round-and-adjust hint
                        val roundUp = ((throwValueInt + 9) / 10) * 10
                        val adj = roundUp - throwValueInt
                        withStyle(SpanStyle(color = opC)) { append("\u2212") }
                        withStyle(SpanStyle(color = numC, fontWeight = FontWeight.Bold)) { append("$roundUp") }
                        withStyle(SpanStyle(color = opC)) { append(" then +") }
                        withStyle(SpanStyle(color = numC, fontWeight = FontWeight.Bold)) { append("$adj") }
                    } else if (onesSub > onesScore && onesSub != 0) {
                        // Complement hint
                        val complement = 10 - onesSub
                        withStyle(SpanStyle(color = borrowC, fontWeight = FontWeight.Bold)) { append("borrow ") }
                        withStyle(SpanStyle(color = opC)) { append("10\u2212") }
                        withStyle(SpanStyle(color = numC, fontWeight = FontWeight.Bold)) { append("$onesSub") }
                        withStyle(SpanStyle(color = opC)) { append("=") }
                        withStyle(SpanStyle(color = numC, fontWeight = FontWeight.Bold)) { append("$complement") }
                        withStyle(SpanStyle(color = opC)) { append(", +") }
                        withStyle(SpanStyle(color = numC, fontWeight = FontWeight.Bold)) { append("$onesScore") }
                    } else if (onesSub == 0) {
                        // Ends in 0 -easy
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                            append("ends in 0, subtract tens directly")
                        }
                    } else {
                        // Simple subtraction
                        withStyle(SpanStyle(color = numC, fontWeight = FontWeight.Bold)) { append("$onesScore") }
                        withStyle(SpanStyle(color = opC)) { append(" \u2212 ") }
                        withStyle(SpanStyle(color = numC, fontWeight = FontWeight.Bold)) { append("$onesSub") }
                        withStyle(SpanStyle(color = opC)) { append(" = ") }
                        withStyle(SpanStyle(color = Color(0xFF81C784), fontWeight = FontWeight.Bold)) { append("${onesScore - onesSub}") }
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) { append(" (no borrow)") }
                    }
                }
                Text(
                    text = hintText,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun ScoreOdometer(score: String) {
    val digits = score.padStart(3, ' ')

    Row {
        for (i in digits.indices) {
            DigitWheel(
                digit = digits[i],
                modifier = Modifier.width(48.dp),
            )
        }
    }
}

@Composable
private fun DigitWheel(
    digit: Char,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = digit,
        modifier = modifier,
        transitionSpec = {
            val old = if (initialState.isDigit()) initialState.digitToInt() else -1
            val new = if (targetState.isDigit()) targetState.digitToInt() else -1
            val direction = if (new <= old || old == -1) 1 else -1
            (slideInVertically { direction * it } + fadeIn(tween(350, easing = FastOutSlowInEasing))) togetherWith
                    (slideOutVertically { -direction * it } + fadeOut(tween(200)))
        },
    ) { d ->
        Text(
            text = if (d == ' ') "" else d.toString(),
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Black,
                fontSize = 64.sp,
            ),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
