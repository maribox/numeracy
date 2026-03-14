package it.bosler.numeracy.model

import kotlinx.serialization.Serializable

@Serializable
data class RunRecord(
    val id: String,
    val scenarioType: String, // ScenarioType.name
    val startedAt: Long,
    val endedAt: Long,
    val answers: List<AnswerRecord>,
) {
    val totalCorrect: Int get() = answers.count { it.isCorrect }
    val accuracy: Int get() = if (answers.isEmpty()) 0 else (totalCorrect * 100) / answers.size
    val averageTimeMillis: Long get() = if (answers.isEmpty()) 0 else answers.map { it.timeMillis }.average().toLong()
    val bestStreak: Int get() {
        var best = 0
        var current = 0
        for (answer in answers) {
            if (answer.isCorrect) {
                current++
                if (current > best) best = current
            } else {
                current = 0
            }
        }
        return best
    }
}

@Serializable
data class AnswerRecord(
    val questionText: String,
    val correctAnswer: String,
    val userAnswer: String,
    val isCorrect: Boolean,
    val timeMillis: Long,
)

@Serializable
data class AppData(
    val runs: List<RunRecord> = emptyList(),
)
