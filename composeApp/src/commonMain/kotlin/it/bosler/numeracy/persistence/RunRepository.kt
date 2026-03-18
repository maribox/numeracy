package it.bosler.numeracy.persistence

import it.bosler.numeracy.model.AppData
import it.bosler.numeracy.model.RunRecord
import it.bosler.numeracy.model.ScenarioType
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class RunRepository(private val storage: FileStorage) {

    private val fileName = "numeracy_data.json"
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private var cachedData: AppData? = null

    fun loadAll(): AppData {
        cachedData?.let { return it }
        val raw = storage.read(fileName)
        val data = if (raw != null) {
            try {
                json.decodeFromString<AppData>(raw)
            } catch (_: Exception) {
                AppData()
            }
        } else {
            AppData()
        }
        cachedData = data
        return data
    }

    fun saveRun(run: RunRecord) {
        val data = loadAll()
        val updated = data.copy(runs = data.runs + run)
        cachedData = updated
        storage.write(fileName, json.encodeToString(updated))
    }

    fun getRunsForScenario(type: ScenarioType): List<RunRecord> {
        return loadAll().runs.filter { it.scenarioType == type.name }
    }

    fun isGameModeEnabled(): Boolean = loadAll().gameModeEnabled

    fun setGameModeEnabled(enabled: Boolean) {
        val data = loadAll()
        val updated = data.copy(gameModeEnabled = enabled)
        cachedData = updated
        storage.write(fileName, json.encodeToString(updated))
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
