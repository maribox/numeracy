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
    /** Whether the answer can be below zero, which is what puts a sign key on the keypad. */
    val allowsNegative: Boolean = false,
    /**
     * How far from [correctAnswer] an answer may be and still be exact. An amount shown rounded to
     * whole units is exact at the unrounded value too: "1963" and "1962.50" both answer €250 × 7.85.
     */
    val absoluteTolerance: Double = 0.0,
)
