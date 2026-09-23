package it.bosler.numeracy.viewmodel

import it.bosler.numeracy.model.AppData
import it.bosler.numeracy.model.Difficulty
import it.bosler.numeracy.model.ScenarioType
import it.bosler.numeracy.persistence.InMemoryStorage
import it.bosler.numeracy.persistence.RunRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PracticeViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private var now = 1_000_000L

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun repository(gameMode: Boolean) = RunRepository(
        InMemoryStorage(mapOf(RunRepository.FILE_NAME to Json.encodeToString(AppData.serializer(), AppData(gameModeEnabled = gameMode))))
    )

    private fun practice(
        scenario: ScenarioType = ScenarioType.DARTS,
        difficulty: Difficulty = Difficulty.NORMAL,
        gameMode: Boolean = false,
        repository: RunRepository = repository(gameMode),
    ) = PracticeViewModel(scenario, difficulty, Random(1), repository) { now }

    private fun PracticeViewModel.answerRight() = onAnswerChanged(state.value.currentProblem.correctAnswer)

    private fun PracticeViewModel.answerWrong() {
        val right = state.value.currentProblem.correctAnswer
        onAnswerChanged(right.map { if (it.isDigit()) '0' + ((it - '0') + 5) % 10 else it }.joinToString(""))
    }

    @Test
    fun aRightAnswerMovesOnAndCounts() = runTest(dispatcher) {
        val model = practice()
        val first = model.state.value.currentProblem
        now += 2_000
        model.answerRight()
        val state = model.state.value
        assertNotEquals(first, state.currentProblem)
        assertEquals(1, state.streak)
        assertEquals(1, state.totalCorrect)
        assertEquals(listOf(true), state.answerHistory)
        assertEquals("", state.userAnswer)
    }

    @Test
    fun aWrongAnswerKeepsTheQuestionAndBreaksTheStreak() = runTest(dispatcher) {
        val model = practice()
        model.answerRight()
        val question = model.state.value.currentProblem
        model.answerWrong()
        val state = model.state.value
        assertEquals(question, state.currentProblem)
        assertTrue(state.shake)
        assertEquals("", state.userAnswer)
        assertEquals(0, state.streak)
        assertEquals(1, state.bestStreak)
        assertEquals(listOf(true, false), state.answerHistory)
    }

    @Test
    fun theRunIsWrittenAfterEveryAnswerNotOnlyWhenLeaving() = runTest(dispatcher) {
        val storage = repository(gameMode = false)
        val model = practice(repository = storage)
        now += 1_500
        model.answerRight()
        now += 2_500
        model.answerWrong()
        // No onQuit: the app may be killed in the background without ever leaving the screen.
        val runs = RunRepository(storageOf(storage)).getRunsForScenario(ScenarioType.DARTS)
        assertEquals(1, runs.size)
        assertEquals(listOf(true, false), runs.single().answers.map { it.isCorrect })
        assertEquals(listOf(1_500L, 2_500L), runs.single().answers.map { it.timeMillis })
    }

    @Test
    fun leavingWithoutAnsweringWritesNothing() = runTest(dispatcher) {
        val storage = repository(gameMode = false)
        practice(repository = storage).onQuit()
        assertEquals(0, storage.getRunsForScenario(ScenarioType.DARTS).size)
    }

    @Test
    fun timeAwayFromTheScreenIsNotAnsweringTime() = runTest(dispatcher) {
        val storage = repository(gameMode = false)
        val model = practice(repository = storage)
        now += 1_000
        model.onPause()
        now += 60_000
        model.onResume()
        now += 2_000
        model.answerRight()
        assertEquals(3_000L, storage.getRunsForScenario(ScenarioType.DARTS).single().answers.single().timeMillis)
    }

    @Test
    fun aCloseAnswerShowsTheExactOneBeforeMovingOn() = runTest(dispatcher) {
        val model = practice(ScenarioType.LENGTH_CONVERSION)
        val question = model.state.value.currentProblem
        val near = (question.correctAnswer.toInt() + 1).let { if (it.toString().length == question.correctAnswer.length) it else it - 2 }
        model.onAnswerChanged(near.toString())

        val held = model.state.value
        assertEquals(question, held.currentProblem)
        val feedback = assertNotNull(held.feedback)
        assertTrue(feedback.isClose)
        assertEquals(question.correctAnswer, feedback.correctAnswer)

        // Keys during the pause do nothing: the answer has been judged.
        model.onAnswerChanged("9")
        assertEquals(near.toString(), model.state.value.userAnswer)

        advanceTimeBy(1_199)
        runCurrent()
        assertEquals(question, model.state.value.currentProblem)
        advanceTimeBy(2)
        runCurrent()
        assertNotEquals(question, model.state.value.currentProblem)
        assertNull(model.state.value.feedback)
        assertEquals(1, model.state.value.totalCorrect)
    }

    @Test
    fun gameModePaysForSpeedAndTheFireCools() = runTest(dispatcher) {
        val model = practice(gameMode = true)
        val median = model.medianAnswerMs
        now += median / 10
        model.answerRight()
        val hot = model.state.value
        assertEquals(0.75f, hot.fire.level)
        assertEquals(pointsFor(0.75f), hot.points)
        assertTrue(hot.fire.levelAt(now + median, median) < hot.fire.level)
        assertEquals(0f, hot.fire.levelAt(now + median * 4, median))

        model.answerWrong()
        assertEquals(0.75f - WRONG_ANSWER_COOLING, model.state.value.fire.level, 1e-6f)
    }

    @Test
    fun withoutGameModeThereAreNoPointsOrFire() = runTest(dispatcher) {
        val model = practice(gameMode = false)
        repeat(6) { model.answerRight() }
        val state = model.state.value
        assertEquals(0, state.points)
        assertEquals(0f, state.fire.level)
        assertEquals(0, state.confettiTrigger)
    }

    @Test
    fun everyFifthRightAnswerInARowIsCelebrated() = runTest(dispatcher) {
        val model = practice(gameMode = true)
        repeat(4) { model.answerRight() }
        assertEquals(0, model.state.value.confettiTrigger)
        model.answerRight()
        assertEquals(1, model.state.value.confettiTrigger)
    }

    @Test
    fun hardDartsHidesTheScoreUntilAMistake() = runTest(dispatcher) {
        val model = practice(difficulty = Difficulty.HARD)
        model.answerRight()
        assertTrue(model.state.value.hideScore)
        model.answerWrong()
        assertTrue(!model.state.value.hideScore)
    }

    @Test
    fun theFireStandsStillWhileTheScreenIsAway() = runTest(dispatcher) {
        val model = practice(gameMode = true)
        model.answerRight()
        val median = model.medianAnswerMs
        val before = model.state.value.fire.levelAt(now, median)
        model.onPause()
        now += 600_000
        model.onResume()
        assertEquals(before, model.state.value.fire.levelAt(now, median))
    }

    private fun TestScope.storageOf(repository: RunRepository) = object : it.bosler.numeracy.persistence.Storage {
        override fun read(fileName: String) = Json.encodeToString(AppData.serializer(), repository.loadAll())
        override fun write(fileName: String, content: String) = Unit
    }
}
