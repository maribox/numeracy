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

    private val generator = generatorFor(scenarioType)
    private val startedAt = currentTimeMillis()
    private val answerRecords = mutableListOf<AnswerRecord>()

    private val _state = MutableStateFlow(
        PracticeState(
            currentProblem = generator.generate(),
            questionStartMillis = currentTimeMillis(),
            difficulty = difficulty,
        )
    )
    val state: StateFlow<PracticeState> = _state.asStateFlow()

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

                _state.value = current.copy(
                    userAnswer = answer,
                    feedback = Feedback(
                        isCorrect = true,
                        isClose = isClose,
                        correctAnswer = correctAnswer,
                        explanation = current.currentProblem.explanation,
                    ),
                    streak = newStreak,
                    bestStreak = newBest,
                    totalAnswered = current.totalAnswered + 1,
                    totalCorrect = current.totalCorrect + 1,
                    showInfo = false,
                    hideScore = shouldHideScore,
                    answerHistory = current.answerHistory + true,
                )

                viewModelScope.launch {
                    delay(if (isClose) 1200 else 600)
                    onNext()
                }
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

                _state.value = current.copy(
                    userAnswer = answer,
                    shake = true,
                    streak = 0,
                    bestStreak = newBest,
                    totalAnswered = current.totalAnswered + 1,
                    hideScore = false,
                    answerHistory = current.answerHistory + false,
                )

                viewModelScope.launch {
                    delay(250)
                    val s = _state.value
                    _state.value = s.copy(userAnswer = "", shake = false)
                }
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
