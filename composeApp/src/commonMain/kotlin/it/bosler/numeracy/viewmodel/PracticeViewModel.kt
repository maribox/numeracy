package it.bosler.numeracy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.bosler.numeracy.generator.generatorFor
import it.bosler.numeracy.model.AnswerRecord
import it.bosler.numeracy.model.Difficulty
import it.bosler.numeracy.model.InputType
import it.bosler.numeracy.model.Problem
import it.bosler.numeracy.model.RunRecord
import it.bosler.numeracy.model.ScenarioType
import it.bosler.numeracy.persistence.AppContext
import it.bosler.numeracy.util.currentTimeMillis
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PracticeState(
    val currentProblem: Problem,
    val userAnswer: String = "",
    val feedback: Feedback? = null,
    val shake: Boolean = false,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val totalAnswered: Int = 0,
    val totalCorrect: Int = 0,
    val questionStartMillis: Long = 0L,
    val difficulty: Difficulty = Difficulty.NORMAL,
    val showInfo: Boolean = false,
    val hideScore: Boolean = false,
    val answerHistory: List<Boolean> = emptyList(),
    val modeToastCounter: Int = 0,
    // Game mode
    val gameMode: Boolean = false,
    val points: Int = 0,
    val lastAnswerTimeMillis: Long = 0L,
    val lastPointsEarned: Int = 0,
    val confettiTrigger: Int = 0,
    val flameTrigger: Int = 0,
    val correctFlashTrigger: Int = 0,
    val wrongFlashTrigger: Int = 0,
    val shockwaveTrigger: Int = 0,
    val comboTrigger: Int = 0,
    // Fire bar: 0.0 = cold, 1.0 = max heat. Decays over time, grows on correct answers.
    val fireLevel: Float = 0f,
    val fireBoostTrigger: Int = 0,
)

data class Feedback(
    val isCorrect: Boolean,
    val isClose: Boolean = false, // close but not exact, show orange + correct answer
    val correctAnswer: String,
    val explanation: String,
)

class PracticeViewModel(
    private val scenarioType: ScenarioType,
    initialDifficulty: Difficulty = Difficulty.NORMAL,
) : ViewModel() {

    private var difficulty: Difficulty = initialDifficulty
    private val gameMode = AppContext.runRepository.isGameModeEnabled()

    private var generator = generatorFor(scenarioType, difficulty)
    private val startedAt = currentTimeMillis()
    private val answerRecords = mutableListOf<AnswerRecord>()

    private val _state = MutableStateFlow(
        PracticeState(
            currentProblem = generator.generate(),
            questionStartMillis = currentTimeMillis(),
            difficulty = difficulty,
            gameMode = gameMode,
        )
    )
    val state: StateFlow<PracticeState> = _state.asStateFlow()

    init {
        // Continuous fire decay — exponential: fast at top, slow near bottom
        // Reaches near-zero in ~15 seconds from full
        if (gameMode) {
            viewModelScope.launch {
                while (true) {
                    delay(200)
                    val current = _state.value
                    if (current.fireLevel > 0.01f && current.feedback == null) {
                        // Inverse-quadratic decay: rate = k·f² + c
                        // At f=1.0: ~5% per tick (fast). At f=0.1: ~0.3% per tick (crawl).
                        // Reaches near-zero in ~20 seconds from full.
                        val decay = current.fireLevel * current.fireLevel * 0.05f + 0.003f
                        _state.value = current.copy(
                            fireLevel = (current.fireLevel - decay).coerceAtLeast(0f).let {
                                if (it < 0.01f) 0f else it
                            }
                        )
                    }
                }
            }
        }
    }

    fun onAnswerChanged(answer: String) {
        val current = _state.value
        if (current.feedback != null) return
        _state.value = current.copy(userAnswer = answer, shake = false)

        val problem = current.currentProblem
        val correctAnswer = problem.correctAnswer

        when (problem.inputType) {
            InputType.WEEKDAY -> tryAutoSubmit(answer, correctAnswer)
            InputType.NUMBER -> {
                if (answer.length == correctAnswer.length && answer.isNotEmpty()) {
                    tryAutoSubmit(answer, correctAnswer)
                }
            }
            InputType.MONEY -> {
                if (answer.length == correctAnswer.length && answer.isNotEmpty()) {
                    tryAutoSubmit(answer, correctAnswer)
                }
            }
            InputType.TIME -> {}
        }
    }

    fun onSubmit() {
        val current = _state.value
        if (current.feedback != null) return
        if (current.userAnswer.isBlank()) return
        tryAutoSubmit(current.userAnswer, current.currentProblem.correctAnswer)
    }

    fun toggleInfo() {
        _state.value = _state.value.copy(showInfo = !_state.value.showInfo)
    }

    fun changeDifficulty(newDifficulty: Difficulty) {
        if (newDifficulty == difficulty) return
        difficulty = newDifficulty
        generator = generatorFor(scenarioType, difficulty)
        _state.value = _state.value.copy(
            difficulty = newDifficulty,
            modeToastCounter = _state.value.modeToastCounter + 1,
            // Show score when switching away from Hard
            hideScore = if (newDifficulty != Difficulty.HARD) false else _state.value.hideScore,
        )
    }

    private fun tryAutoSubmit(answer: String, correctAnswer: String) {
        val current = _state.value
        val userAnswer = answer.trim()
        val result = checkAnswer(userAnswer, correctAnswer, current.currentProblem.tolerancePercent)
        val elapsed = currentTimeMillis() - current.questionStartMillis

        when (result) {
            AnswerResult.EXACT, AnswerResult.CLOSE -> {
                val isClose = result == AnswerResult.CLOSE
                val newStreak = current.streak + 1
                val newBest = maxOf(current.bestStreak, newStreak)

                answerRecords.add(
                    AnswerRecord(
                        questionText = current.currentProblem.questionText,
                        correctAnswer = correctAnswer,
                        userAnswer = userAnswer,
                        isCorrect = true,
                        timeMillis = elapsed,
                    )
                )

                val shouldHideScore = difficulty == Difficulty.HARD && current.totalAnswered >= 0

                // Game mode: fire bar + points
                val newFireLevel: Float
                val earnedPoints: Int
                if (gameMode) {
                    // Fire grows on correct answers. Faster answers = bigger boost.
                    val speedBoost = when {
                        elapsed < 1500 -> 0.35f
                        elapsed < 3000 -> 0.25f
                        elapsed < 6000 -> 0.15f
                        else -> 0.08f
                    }
                    newFireLevel = (current.fireLevel + speedBoost).coerceAtMost(1f)
                    // Points scale with fire level: 50 at 0, up to 300 at max
                    val multiplier = 1.0 + newFireLevel * 5.0
                    earnedPoints = (50 * multiplier).toInt()
                } else {
                    newFireLevel = current.fireLevel
                    earnedPoints = 0
                }

                val shouldConfetti = gameMode && (newStreak % 5 == 0 && newStreak > 0)
                val shouldCombo = gameMode && newStreak >= 3

                // Advance immediately — no delay
                val now = currentTimeMillis()
                _state.value = PracticeState(
                    currentProblem = generator.generate(),
                    streak = newStreak,
                    bestStreak = newBest,
                    totalAnswered = current.totalAnswered + 1,
                    totalCorrect = current.totalCorrect + 1,
                    questionStartMillis = now,
                    difficulty = difficulty,
                    hideScore = shouldHideScore,
                    answerHistory = current.answerHistory + true,
                    gameMode = gameMode,
                    points = current.points + earnedPoints,
                    lastPointsEarned = earnedPoints,
                    confettiTrigger = if (shouldConfetti) current.confettiTrigger + 1 else current.confettiTrigger,
                    fireLevel = newFireLevel,
                )
            }

            AnswerResult.WRONG -> {
                answerRecords.add(
                    AnswerRecord(
                        questionText = current.currentProblem.questionText,
                        correctAnswer = correctAnswer,
                        userAnswer = userAnswer,
                        isCorrect = false,
                        timeMillis = elapsed,
                    )
                )

                val newBest = maxOf(current.bestStreak, current.streak)

                // Wrong answer: big fire penalty
                val newFireLevel = (current.fireLevel - 0.3f).coerceAtLeast(0f)

                _state.value = current.copy(
                    userAnswer = "",
                    shake = true,
                    streak = 0,
                    bestStreak = newBest,
                    totalAnswered = current.totalAnswered + 1,
                    hideScore = false,
                    answerHistory = current.answerHistory + false,
                    wrongFlashTrigger = if (gameMode) current.wrongFlashTrigger + 1 else current.wrongFlashTrigger,
                    fireLevel = newFireLevel,
                )
            }
        }
    }

    fun onNext() {
        val now = currentTimeMillis()
        val current = _state.value
        _state.value = PracticeState(
            currentProblem = generator.generate(),
            streak = current.streak,
            bestStreak = current.bestStreak,
            totalAnswered = current.totalAnswered,
            totalCorrect = current.totalCorrect,
            questionStartMillis = now,
            difficulty = difficulty,
            hideScore = current.hideScore,
            answerHistory = current.answerHistory,
            gameMode = gameMode,
            points = current.points,
            fireLevel = current.fireLevel,
        )
    }

    fun onQuit() {
        if (answerRecords.isEmpty()) return
        val run = RunRecord(
            id = "${currentTimeMillis()}_${(0..9999).random()}",
            scenarioType = scenarioType.name,
            startedAt = startedAt,
            endedAt = currentTimeMillis(),
            answers = answerRecords.toList(),
        )
        AppContext.runRepository.saveRun(run)
    }

    private enum class AnswerResult { EXACT, CLOSE, WRONG }

    private fun checkAnswer(userAnswer: String, correctAnswer: String, tolerancePercent: Double): AnswerResult {
        if (userAnswer.equals(correctAnswer, ignoreCase = true)) return AnswerResult.EXACT
        val userNum = userAnswer.toDoubleOrNull()
        val correctNum = correctAnswer.toDoubleOrNull()
        if (userNum != null && correctNum != null) {
            if (kotlin.math.abs(userNum - correctNum) < 0.01) return AnswerResult.EXACT
            if (tolerancePercent > 0) {
                val tolerance = kotlin.math.abs(correctNum) * tolerancePercent / 100.0
                // At minimum, accept ±1 for small numbers
                val effectiveTolerance = maxOf(tolerance, 1.0)
                if (kotlin.math.abs(userNum - correctNum) <= effectiveTolerance) {
                    return AnswerResult.CLOSE
                }
            }
        }
        return AnswerResult.WRONG
    }
}
