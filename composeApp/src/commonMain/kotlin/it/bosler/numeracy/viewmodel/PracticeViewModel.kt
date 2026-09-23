package it.bosler.numeracy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.bosler.numeracy.generator.generatorFor
import it.bosler.numeracy.model.AnswerRecord
import it.bosler.numeracy.model.Difficulty
import it.bosler.numeracy.model.Grade
import it.bosler.numeracy.model.Problem
import it.bosler.numeracy.model.RunRecord
import it.bosler.numeracy.model.ScenarioType
import it.bosler.numeracy.model.grade
import it.bosler.numeracy.model.isComplete
import it.bosler.numeracy.persistence.AppContext
import it.bosler.numeracy.persistence.RunRepository
import it.bosler.numeracy.util.currentTimeMillis
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class PracticeState(
    val currentProblem: Problem,
    val userAnswer: String = "",
    val feedback: Feedback? = null,
    /** The last answer was wrong: the typed answer shakes and turns red until the next key. */
    val shake: Boolean = false,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val totalAnswered: Int = 0,
    val totalCorrect: Int = 0,
    val difficulty: Difficulty = Difficulty.NORMAL,
    val showInfo: Boolean = false,
    /** Hard darts hides the running score after a right answer; a wrong one shows it again. */
    val hideScore: Boolean = false,
    /** Right and wrong, in the order answered. */
    val answerHistory: List<Boolean> = emptyList(),
    val gameMode: Boolean = false,
    val points: Int = 0,
    val lastPointsEarned: Int = 0,
    /** Counts streak rewards; the confetti plays each time it changes. */
    val confettiTrigger: Int = 0,
    val fire: Fire = Fire(),
)

data class Feedback(
    val isCorrect: Boolean,
    /** Accepted as near enough rather than exact, so the exact answer is shown beside it. */
    val isClose: Boolean = false,
    val correctAnswer: String,
    val explanation: String,
)

/**
 * One run of practice on one scenario: asks questions, grades answers, keeps the streak, the points
 * and the fire, and writes the run after every answer.
 *
 * [clock] is the time answers are measured in and [repository] where the run goes; both default to
 * the app's own and are replaced in tests and when screens are drawn off-device.
 */
class PracticeViewModel(
    private val scenarioType: ScenarioType,
    initialDifficulty: Difficulty = Difficulty.NORMAL,
    private val rng: Random = Random.Default,
    private val repository: RunRepository = AppContext.runRepository,
    private val clock: () -> Long = ::currentTimeMillis,
) : ViewModel() {

    private var difficulty: Difficulty = initialDifficulty
    private val gameMode = repository.isGameModeEnabled()
    private var generator = generatorFor(scenarioType, difficulty, rng)

    private val startedAt = clock()
    private val runId = "${startedAt}_${Random.Default.nextInt(10_000)}"
    private val answers = mutableListOf<AnswerRecord>()

    /** When the question on screen was put there, less any time the screen spent out of sight. */
    private var questionShownAt = startedAt
    private var pausedAt: Long? = null

    private val _state = MutableStateFlow(
        PracticeState(
            currentProblem = generator.generate(),
            difficulty = difficulty,
            gameMode = gameMode,
            fire = Fire(since = startedAt),
        )
    )
    val state: StateFlow<PracticeState> = _state.asStateFlow()

    /** The scenario's median answer time, which sets how fast the fire cools. */
    val medianAnswerMs: Long get() = scenarioType.expectedMedianTimeMs

    fun onAnswerChanged(answer: String) {
        val current = _state.value
        if (current.feedback != null) return
        _state.value = current.copy(userAnswer = answer, shake = false)
        if (isComplete(answer, current.currentProblem)) submit(answer)
    }

    /** Grades whatever has been typed: the Submit key, Enter, and the time picker's button. */
    fun onSubmit() {
        val current = _state.value
        if (current.feedback != null || current.userAnswer.isBlank()) return
        submit(current.userAnswer)
    }

    fun toggleInfo() {
        _state.update { it.copy(showInfo = !it.showInfo) }
    }

    /** Takes effect from the next question; the one on screen keeps its numbers and shows the new helpers. */
    fun changeDifficulty(newDifficulty: Difficulty) {
        if (newDifficulty == difficulty) return
        difficulty = newDifficulty
        generator = generatorFor(scenarioType, difficulty, rng)
        _state.update {
            it.copy(difficulty = newDifficulty, hideScore = newDifficulty == Difficulty.HARD && it.hideScore)
        }
    }

    /** The screen left sight. Time away is neither answering time nor time for the fire to cool. */
    fun onPause() {
        if (pausedAt == null) pausedAt = clock()
    }

    fun onResume() {
        val since = pausedAt ?: return
        pausedAt = null
        val away = clock() - since
        questionShownAt += away
        _state.update { it.copy(fire = it.fire.copy(since = it.fire.since + away)) }
    }

    /** Writes the run once more with its end time. It has been written after every answer already. */
    fun onQuit() {
        if (answers.isNotEmpty()) repository.saveRun(currentRun())
    }

    private fun currentRun() = RunRecord(
        id = runId,
        scenarioType = scenarioType.name,
        startedAt = startedAt,
        endedAt = clock(),
        answers = answers.toList(),
    )

    private fun submit(answer: String) {
        val current = _state.value
        val problem = current.currentProblem
        val given = answer.trim()
        val now = clock()
        val elapsed = now - questionShownAt
        val result = grade(given, problem)
        val right = result != Grade.WRONG

        answers += AnswerRecord(
            questionText = problem.questionText,
            correctAnswer = problem.correctAnswer,
            userAnswer = given,
            isCorrect = right,
            timeMillis = elapsed,
        )
        repository.saveRun(currentRun())

        val heat = current.fire.levelAt(now, medianAnswerMs)
        if (!right) {
            _state.value = current.copy(
                userAnswer = "",
                shake = true,
                streak = 0,
                bestStreak = maxOf(current.bestStreak, current.streak),
                totalAnswered = current.totalAnswered + 1,
                hideScore = false,
                answerHistory = current.answerHistory + false,
                fire = if (gameMode) Fire((heat - WRONG_ANSWER_COOLING).coerceAtLeast(0f), now) else current.fire,
            )
            return
        }

        val streak = current.streak + 1
        val newHeat = if (gameMode) (heat + heatFor(elapsed, medianAnswerMs)).coerceAtMost(1f) else 0f
        val earned = if (gameMode) pointsFor(newHeat) else 0
        val reward = gameMode && streak % STREAK_REWARD_EVERY == 0

        val next = { at: Long ->
            questionShownAt = at
            PracticeState(
                currentProblem = generator.generate(),
                streak = streak,
                bestStreak = maxOf(current.bestStreak, streak),
                totalAnswered = current.totalAnswered + 1,
                totalCorrect = current.totalCorrect + 1,
                difficulty = difficulty,
                hideScore = difficulty == Difficulty.HARD,
                answerHistory = current.answerHistory + true,
                gameMode = gameMode,
                points = current.points + earned,
                lastPointsEarned = earned,
                confettiTrigger = current.confettiTrigger + if (reward) 1 else 0,
                fire = Fire(newHeat, at),
            )
        }

        if (result == Grade.CLOSE) {
            // Near enough counts, and the exact answer stays on screen long enough to read before
            // the next question: accepted silently, an estimate would read as exactly right.
            _state.value = current.copy(
                userAnswer = given,
                feedback = Feedback(true, isClose = true, correctAnswer = problem.correctAnswer, explanation = problem.explanation),
            )
            viewModelScope.launch {
                delay(CLOSE_ANSWER_PAUSE_MS)
                _state.value = next(clock())
            }
        } else {
            _state.value = next(now)
        }
    }

    private companion object {
        /** Long enough to read the exact answer, short enough not to interrupt a run. */
        const val CLOSE_ANSWER_PAUSE_MS = 1200L

        /** Every this many right answers in a row is celebrated. */
        const val STREAK_REWARD_EVERY = 5
    }
}
