package it.bosler.numeracy.model

enum class InputType {
    NUMBER,
    MONEY,
    TIME,
    WEEKDAY,
}

data class Problem(
    val scenarioType: ScenarioType,
    val questionText: String,
    val correctAnswer: String,
    val inputType: InputType = InputType.NUMBER,
    val explanation: String = "",
    val metadata: Map<String, String> = emptyMap(),
    /** Percentage tolerance for approximate answers (e.g. 5.0 = accept within 5%). 0 = exact only. */
    val tolerancePercent: Double = 0.0,
)

data class AnswerResult(
    val problem: Problem,
    val userAnswer: String,
    val isCorrect: Boolean,
)
