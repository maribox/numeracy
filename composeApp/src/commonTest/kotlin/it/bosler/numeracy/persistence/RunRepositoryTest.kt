package it.bosler.numeracy.persistence

import it.bosler.numeracy.model.AnswerRecord
import it.bosler.numeracy.model.RunRecord
import it.bosler.numeracy.model.ScenarioType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RunRepositoryTest {

    private fun run(id: String, vararg right: Boolean, millis: Long = 1000) = RunRecord(
        id = id,
        scenarioType = ScenarioType.DARTS.name,
        startedAt = 0,
        endedAt = 1,
        answers = right.map { AnswerRecord("q", "1", if (it) "1" else "2", it, millis) },
    )

    @Test
    fun runsSurviveBeingReadAgain() {
        val storage = InMemoryStorage()
        RunRepository(storage).saveRun(run("a", true, false))
        val runs = RunRepository(storage).getRunsForScenario(ScenarioType.DARTS)
        assertEquals(listOf("a"), runs.map { it.id })
        assertEquals(2, runs.single().answers.size)
    }

    @Test
    fun savingARunAgainReplacesIt() {
        val repository = RunRepository(InMemoryStorage())
        repository.saveRun(run("a", true))
        repository.saveRun(run("a", true, true))
        repository.saveRun(run("b", false))
        assertEquals(listOf("a", "b"), repository.getRunsForScenario(ScenarioType.DARTS).map { it.id }.sorted())
        assertEquals(2, repository.getRunsForScenario(ScenarioType.DARTS).first { it.id == "a" }.answers.size)
    }

    @Test
    fun statisticsAddUpEveryRun() {
        val repository = RunRepository(InMemoryStorage())
        repository.saveRun(run("a", true, true, true, false, millis = 2000))
        repository.saveRun(run("b", true, false, millis = 4000))
        val stats = repository.getStats(ScenarioType.DARTS)
        assertEquals(2, stats.totalRuns)
        assertEquals(6, stats.totalQuestions)
        assertEquals(4, stats.totalCorrect)
        assertEquals(66, stats.accuracy)
        assertEquals(3, stats.bestStreak)
        assertEquals((4 * 2000L + 2 * 4000L) / 6, stats.averageTimeMillis)
        assertEquals(0, repository.getStats(ScenarioType.SQUARING).totalRuns)
    }

    @Test
    fun anUnreadableHistoryIsSetAsideBeforeAnythingOverwritesIt() {
        val storage = InMemoryStorage(mapOf(RunRepository.FILE_NAME to "{\"runs\": [truncated"))
        val repository = RunRepository(storage)
        assertEquals(0, repository.getRunsForScenario(ScenarioType.DARTS).size)
        repository.saveRun(run("new", true))
        assertEquals("{\"runs\": [truncated", storage.read(RunRepository.UNREADABLE_FILE_NAME))
        assertEquals(listOf("new"), RunRepository(storage).getRunsForScenario(ScenarioType.DARTS).map { it.id })
    }

    @Test
    fun runsOfScenariosThatNoLongerExistAreKeptButNotCounted() {
        val storage = InMemoryStorage()
        val repository = RunRepository(storage)
        repository.saveRun(run("old", true).copy(scenarioType = "IMPLIED_ODDS"))
        repository.saveRun(run("a", true))
        assertEquals(1, repository.getStats(ScenarioType.DARTS).totalRuns)
        assertTrue("IMPLIED_ODDS" in storage.read(RunRepository.FILE_NAME)!!)
    }

    @Test
    fun gameModeIsOnUntilSwitchedOff() {
        val storage = InMemoryStorage()
        assertTrue(RunRepository(storage).isGameModeEnabled())
        RunRepository(storage).setGameModeEnabled(false)
        assertFalse(RunRepository(storage).isGameModeEnabled())
    }
}
