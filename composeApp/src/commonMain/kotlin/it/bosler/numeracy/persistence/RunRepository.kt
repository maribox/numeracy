package it.bosler.numeracy.persistence

import it.bosler.numeracy.model.AppData
import it.bosler.numeracy.model.RunRecord
import it.bosler.numeracy.model.ScenarioType
import kotlinx.serialization.json.Json

/** Every run and the one setting, kept as a single JSON file in [storage]. */
class RunRepository(private val storage: Storage) {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private var cachedData: AppData? = null

    fun loadAll(): AppData {
        cachedData?.let { return it }
        val raw = storage.read(FILE_NAME)
        val data = if (raw == null) AppData() else try {
            json.decodeFromString<AppData>(raw)
        } catch (_: Exception) {
            // A file that no longer parses is still the only copy of the history. It is set aside
            // before anything is written, so the next save starts afresh without destroying it.
            storage.write(UNREADABLE_FILE_NAME, raw)
            AppData()
        }
        cachedData = data
        return data
    }

    /**
     * Writes [run], replacing an earlier version of the same run. A run is saved after every answer
     * rather than when practice is left, so a process the system kills in the background loses at
     * most the answer being typed.
     */
    fun saveRun(run: RunRecord) {
        val data = loadAll()
        val others = data.runs.filterNot { it.id == run.id }
        store(data.copy(runs = others + run))
    }

    fun getRunsForScenario(type: ScenarioType): List<RunRecord> =
        loadAll().runs.filter { it.scenarioType == type.name }

    fun isGameModeEnabled(): Boolean = loadAll().gameModeEnabled

    fun setGameModeEnabled(enabled: Boolean) {
        store(loadAll().copy(gameModeEnabled = enabled))
    }

    fun getStats(type: ScenarioType): ScenarioStats {
        val runs = getRunsForScenario(type)
        val allAnswers = runs.flatMap { it.answers }
        return ScenarioStats(
            totalRuns = runs.size,
            totalQuestions = allAnswers.size,
            totalCorrect = allAnswers.count { it.isCorrect },
            bestStreak = runs.maxOfOrNull { it.bestStreak } ?: 0,
            averageTimeMillis = if (allAnswers.isEmpty()) 0 else allAnswers.map { it.timeMillis }.average().toLong(),
        )
    }

    private fun store(data: AppData) {
        storage.write(FILE_NAME, json.encodeToString(AppData.serializer(), data))
        cachedData = data
    }

    companion object {
        const val FILE_NAME = "numeracy_data.json"
        const val UNREADABLE_FILE_NAME = "numeracy_data.unreadable.json"
    }
}

data class ScenarioStats(
    val totalRuns: Int,
    val totalQuestions: Int,
    val totalCorrect: Int,
    val bestStreak: Int,
    val averageTimeMillis: Long,
) {
    val accuracy: Int get() = if (totalQuestions == 0) 0 else (totalCorrect * 100) / totalQuestions
}
