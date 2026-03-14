package it.bosler.numeracy.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.bosler.numeracy.model.InputType
import it.bosler.numeracy.model.ScenarioType
import it.bosler.numeracy.ui.component.NumPad
import it.bosler.numeracy.ui.component.ScenarioInfoSheet
import it.bosler.numeracy.ui.component.TimeInput
import it.bosler.numeracy.ui.component.WeekdayPicker
import it.bosler.numeracy.ui.component.question.QuestionDisplay
import it.bosler.numeracy.util.PlatformBackHandler
import it.bosler.numeracy.util.showBackButton
import it.bosler.numeracy.viewmodel.PracticeViewModel

@Composable
fun PracticeScreen(
    scenarioType: ScenarioType,
    onBack: () -> Unit,
) {
    val viewModel = remember { PracticeViewModel(scenarioType) }
    val state by viewModel.state.collectAsState()

    var timeHours by remember { mutableStateOf(12) }
    var timeMinutes by remember { mutableStateOf(0) }

    PlatformBackHandler {
        viewModel.onQuit()
        onBack()
    }

    // Shake animation
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(state.shake) {
        if (state.shake) {
            repeat(3) {
                shakeOffset.animateTo(14f, tween(40))
                shakeOffset.animateTo(-14f, tween(40))
            }
            shakeOffset.animateTo(0f, tween(40))
        }
    }

    val answerColor by animateColorAsState(
        targetValue = when {
            state.feedback?.isClose == true -> Color(0xFFFF9800) // orange for close
            state.feedback?.isCorrect == true -> Color(0xFF4CAF50)
            state.shake -> Color(0xFFF44336)
            else -> MaterialTheme.colorScheme.primary
        },
        animationSpec = tween(150),
    )

    // Keyboard input -capture number/letter keys
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                if (state.feedback != null) return@onKeyEvent false
                val inputType = state.currentProblem.inputType
                when (event.key) {
                    Key.Zero, Key.NumPad0 -> { viewModel.onAnswerChanged(state.userAnswer + "0"); true }
                    Key.One, Key.NumPad1 -> { viewModel.onAnswerChanged(state.userAnswer + "1"); true }
                    Key.Two, Key.NumPad2 -> { viewModel.onAnswerChanged(state.userAnswer + "2"); true }
                    Key.Three, Key.NumPad3 -> { viewModel.onAnswerChanged(state.userAnswer + "3"); true }
                    Key.Four, Key.NumPad4 -> { viewModel.onAnswerChanged(state.userAnswer + "4"); true }
                    Key.Five, Key.NumPad5 -> { viewModel.onAnswerChanged(state.userAnswer + "5"); true }
                    Key.Six, Key.NumPad6 -> { viewModel.onAnswerChanged(state.userAnswer + "6"); true }
                    Key.Seven, Key.NumPad7 -> { viewModel.onAnswerChanged(state.userAnswer + "7"); true }
                    Key.Eight, Key.NumPad8 -> { viewModel.onAnswerChanged(state.userAnswer + "8"); true }
                    Key.Nine, Key.NumPad9 -> { viewModel.onAnswerChanged(state.userAnswer + "9"); true }
                    Key.Period, Key.NumPadDot -> {
                        if ((inputType == InputType.MONEY) && !state.userAnswer.contains(".")) {
                            viewModel.onAnswerChanged(if (state.userAnswer.isEmpty()) "0." else state.userAnswer + ".")
                        }
                        true
                    }
                    Key.Backspace, Key.Delete -> {
                        if (state.userAnswer.isNotEmpty()) viewModel.onAnswerChanged(state.userAnswer.dropLast(1))
                        true
                    }
                    Key.Enter, Key.NumPadEnter -> {
                        if (inputType == InputType.TIME) viewModel.onSubmit()
                        true
                    }
                    else -> false
                }
            },
    ) {
        val isLandscape = maxWidth > maxHeight

        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar with mode selector + progress bar
            TopBar(
                state = state,
                scenarioType = scenarioType,
                viewModel = viewModel,
                onBack = onBack,
            )

            if (isLandscape) {
                // Landscape: question left, input right
                Row(modifier = Modifier.fillMaxSize()) {
                    // Question area (left)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        AnimatedVisibility(
                            visible = state.showInfo,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically(),
                        ) {
                            ScenarioInfoSheet(scenarioType = scenarioType)
                        }
                        if (!state.showInfo) {
                            Spacer(modifier = Modifier.height(8.dp))
                            QuestionDisplay(
                                problem = state.currentProblem,
                                difficulty = state.difficulty,
                                hideScore = state.hideScore,
                                answered = state.feedback != null,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // Input area (right)
                    if (!state.showInfo) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            InputArea(
                                state = state,
                                viewModel = viewModel,
                                answerColor = answerColor,
                                shakeOffset = shakeOffset.value,
                                timeHours = timeHours,
                                timeMinutes = timeMinutes,
                                onTimeHoursChange = { timeHours = it },
                                onTimeMinutesChange = { timeMinutes = it },
                                compact = true,
                            )
                        }
                    }
                }
            } else {
                // Portrait: question top, input bottom
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AnimatedVisibility(
                        visible = state.showInfo,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        ScenarioInfoSheet(scenarioType = scenarioType)
                    }
                    if (!state.showInfo) {
                        Spacer(modifier = Modifier.height(8.dp))
                        QuestionDisplay(
                            problem = state.currentProblem,
                            difficulty = state.difficulty,
                            hideScore = state.hideScore,
                            answered = state.feedback != null,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (!state.showInfo) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        InputArea(
                            state = state,
                            viewModel = viewModel,
                            answerColor = answerColor,
                            shakeOffset = shakeOffset.value,
                            timeHours = timeHours,
                            timeMinutes = timeMinutes,
                            onTimeHoursChange = { timeHours = it },
                            onTimeMinutesChange = { timeMinutes = it },
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

    }
}

@Composable
private fun TopBar(
    state: it.bosler.numeracy.viewmodel.PracticeState,
    scenarioType: ScenarioType,
    viewModel: PracticeViewModel,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showBackButton) {
                    TextButton(onClick = {
                        viewModel.onQuit()
                        onBack()
                    }) {
                        Text("Back", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                val availableDifficulties = scenarioType.availableDifficulties
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(3.dp),
                ) {
                    availableDifficulties.forEach { diff ->
                        val isSelected = state.difficulty == diff
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .then(
                                    if (isSelected) Modifier.background(scenarioType.startColor.copy(alpha = 0.2f))
                                    else Modifier
                                )
                                .clickable { viewModel.changeDifficulty(diff) }
                                .padding(vertical = 8.dp),
                        ) {
                            Text(
                                text = diff.label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                ),
                                color = if (isSelected) scenarioType.startColor
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Info button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (state.showInfo) scenarioType.startColor.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .clickable { viewModel.toggleInfo() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "i",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = if (state.showInfo) scenarioType.startColor
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                }
            }

            // Segmented progress bar + streak (fixed height)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp),
                ) {
                    if (state.answerHistory.isNotEmpty()) {
                        SegmentedProgressBar(
                            answers = state.answerHistory,
                            accentColor = scenarioType.startColor,
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(3.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        )
                    }
                }
                Box(
                    modifier = Modifier.width(40.dp),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    if (state.streak > 1) {
                        Text(
                            text = "\uD83D\uDD25${state.streak}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
        }
    }

@Composable
private fun InputArea(
    state: it.bosler.numeracy.viewmodel.PracticeState,
    viewModel: PracticeViewModel,
    answerColor: Color,
    shakeOffset: Float,
    timeHours: Int,
    timeMinutes: Int,
    onTimeHoursChange: (Int) -> Unit,
    onTimeMinutesChange: (Int) -> Unit,
    compact: Boolean = false,
) {
    // Answer display -right above the numpad
    if (state.currentProblem.inputType == InputType.NUMBER || state.currentProblem.inputType == InputType.MONEY) {
        Text(
            text = state.userAnswer.ifEmpty { "\u200B" },
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
            ),
            color = answerColor,
            modifier = Modifier
                .padding(vertical = 4.dp)
                .offset { IntOffset(shakeOffset.toInt(), 0) },
        )
        // Show correct answer when close but not exact
        if (state.feedback?.isClose == true) {
            Text(
                text = "Exact: ${state.feedback!!.correctAnswer}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color(0xFFFF9800),
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
    }

    when (state.currentProblem.inputType) {
        InputType.NUMBER -> {
            NumPad(
                value = state.userAnswer,
                onValueChange = { viewModel.onAnswerChanged(it) },
                showDecimal = false,
                enabled = state.feedback == null,
                compact = compact,
            )
        }
        InputType.MONEY -> {
            NumPad(
                value = state.userAnswer,
                onValueChange = { viewModel.onAnswerChanged(it) },
                showDecimal = true,
                enabled = state.feedback == null,
                compact = compact,
            )
        }
        InputType.TIME -> {
            TimeInput(
                hours = timeHours,
                minutes = timeMinutes,
                onHoursChange = { newHours ->
                    onTimeHoursChange(newHours)
                    val h = newHours.toString().padStart(2, '0')
                    val m = timeMinutes.toString().padStart(2, '0')
                    viewModel.onAnswerChanged("$h:$m")
                },
                onMinutesChange = { newMinutes ->
                    onTimeMinutesChange(newMinutes)
                    val h = timeHours.toString().padStart(2, '0')
                    val m = newMinutes.toString().padStart(2, '0')
                    viewModel.onAnswerChanged("$h:$m")
                },
                enabled = state.feedback == null,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.onSubmit() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = state.feedback == null,
                shape = RoundedCornerShape(20.dp),
            ) {
                Text(
                    text = "Submit",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        }
        InputType.WEEKDAY -> {
            WeekdayPicker(
                selected = state.userAnswer,
                onSelected = { viewModel.onAnswerChanged(it) },
                enabled = state.feedback == null,
            )
        }
    }
}

@Composable
private fun SegmentedProgressBar(
    answers: List<Boolean>,
    accentColor: Color,
) {
    val correctColor = accentColor.copy(alpha = 0.85f)
    val wrongColor = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(3.dp)),
    ) {
        val totalWidth = size.width
        val barH = size.height
        val segW = totalWidth / answers.size.toFloat()
        val gapPx = (segW * 0.12f).coerceAtMost(2.dp.toPx())

        answers.forEachIndexed { i, correct ->
            val x = i * segW
            val radius = barH / 2f
            drawRoundRect(
                color = if (correct) correctColor else wrongColor,
                topLeft = Offset(x + gapPx / 2f, 0f),
                size = Size((segW - gapPx).coerceAtLeast(1f), barH),
                cornerRadius = CornerRadius(radius, radius),
            )
        }
    }
}
