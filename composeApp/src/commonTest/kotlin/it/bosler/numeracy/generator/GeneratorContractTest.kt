package it.bosler.numeracy.generator

import it.bosler.numeracy.model.InputType
import it.bosler.numeracy.model.ScenarioType
import it.bosler.numeracy.ui.component.MINUTE_STEP
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * What every generated question owes the screen that asks it: an answer the offered input can
 * produce, the metadata its display reads, and the same question twice under the same seed.
 */
class GeneratorContractTest {

    private val weekdays = setOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

    @Test
    fun everyAnswerCanBeEnteredOnTheInputItIsAskedWith() {
        val violations = Violations("answers the input cannot produce")
        for (sample in everySample()) {
            val problem = sample.problem
            val answer = problem.correctAnswer
            when (problem.inputType) {
                InputType.NUMBER -> {
                    violations.check(Regex("-?\\d+").matches(answer), sample) { "not a whole number: '$answer'" }
                    violations.check(!answer.startsWith("-") || problem.allowsNegative, sample) {
                        "negative answer '$answer' on a keypad without a sign key"
                    }
                }
                InputType.MONEY -> violations.check(Regex("\\d+(\\.\\d{1,2})?").matches(answer), sample) {
                    "not an amount: '$answer'"
                }
                InputType.TIME -> {
                    val match = Regex("([01]\\d|2[0-3]):([0-5]\\d)").matchEntire(answer)
                    violations.check(match != null, sample) { "not HH:MM: '$answer'" }
                    val minute = match?.groupValues?.get(2)?.toInt() ?: 0
                    violations.check(minute % MINUTE_STEP == 0, sample) {
                        "minute $minute is not reachable in steps of $MINUTE_STEP"
                    }
                }
                InputType.WEEKDAY -> violations.check(answer in weekdays, sample) { "not a weekday: '$answer'" }
            }
        }
        violations.assertNone()
    }

    @Test
    fun everyQuestionSaysWhatItAsksAndForWhichScenario() {
        val violations = Violations("malformed questions")
        for (sample in everySample()) {
            violations.check(sample.problem.scenarioType == sample.type, sample) {
                "asked for ${sample.type}, got ${sample.problem.scenarioType}"
            }
            violations.check(sample.problem.questionText.isNotBlank(), sample) { "blank question" }
            violations.check(sample.problem.correctAnswer.isNotBlank(), sample) { "blank answer" }
            violations.check(sample.problem.explanation.isNotBlank(), sample) { "blank explanation" }
        }
        violations.assertNone()
    }

    /** The keys each scenario's question display reads, taken from the displays themselves. */
    private val displayedKeys: Map<ScenarioType, Set<String>> = run {
        val conversion = setOf("value", "fromUnit", "toUnit", "context", "trick", "trickSteps")
        mapOf(
            ScenarioType.DARTS to setOf("currentScore", "throwName", "throwValue"),
            ScenarioType.BLACKJACK to setOf("cards", "faceTotal", "numberTotal", "aceCount"),
            ScenarioType.POT_ODDS to setOf(
                "potAmount", "callAmount", "totalPot", "potOdds", "fraction", "holeCards", "boardCards",
                "street", "winPercent",
            ),
            ScenarioType.DRAW_EQUITY to setOf(
                "drawName", "drawExplanation", "outs", "multiplier", "street", "potAmount", "callAmount",
                "holeCards", "boardCards",
            ),
            ScenarioType.MAKING_CHANGE to setOf("billAmount", "paymentAmount", "centsChange", "eurosChange"),
            ScenarioType.CURRENCY_EXCHANGE to setOf("fromAmount", "rate", "toCurrencyCode", "wholeResult", "practiceHint"),
            ScenarioType.TIME_ZONES to setOf("fromCity", "toCity", "time", "season", "offsetDiff"),
            ScenarioType.LENGTH_CONVERSION to conversion,
            ScenarioType.WEIGHT_CONVERSION to conversion,
            ScenarioType.VOLUME_CONVERSION to conversion,
            ScenarioType.SPEED_CONVERSION to conversion + "needleNorm",
            ScenarioType.TEMPERATURE_CONVERSION to conversion + setOf("nearestLandmarkC", "nearestLandmarkF", "tempNorm"),
            ScenarioType.DOOMSDAY to setOf(
                "day", "monthName", "year", "centuryAnchor", "centuryAnchorIndex", "centuryLabel", "yearDoomsday",
                "monthAnchorDate", "monthMnemonic", "doomsdayRef", "diffFromRef", "yy", "yyDiv12", "yyRemainder",
                "remainderDiv4", "yearCalcSum",
            ),
            ScenarioType.SQUARING to setOf("trick", "trickName", "trickCount"),
            ScenarioType.MULTIPLICATION to setOf("trick", "trickName", "trickCount"),
        )
    }

    @Test
    fun everyScenarioIsCoveredByTheDisplayContract() {
        assertEquals(ScenarioType.entries.toSet(), displayedKeys.keys, "a scenario without a display contract")
    }

    @Test
    fun everyQuestionCarriesWhatItsDisplayReads() {
        val violations = Violations("metadata the display needs and does not get")
        for (sample in everySample()) {
            val wanted = displayedKeys.getValue(sample.type)
            val missing = wanted - sample.problem.metadata.keys
            violations.check(missing.isEmpty(), sample) { "missing $missing" }
            val blank = wanted.filter { sample.problem.metadata[it]?.isBlank() == true && it != "hintHard" }
            violations.check(blank.isEmpty(), sample) { "blank $blank" }
        }
        violations.assertNone()
    }

    @Test
    fun theSameSeedAsksTheSameQuestions() {
        for (type in ScenarioType.entries) for (difficulty in type.availableDifficulties) {
            val first = generatorFor(type, difficulty, Random(7))
            val second = generatorFor(type, difficulty, Random(7))
            repeat(50) { index ->
                assertEquals(first.generate(), second.generate(), "$type/$difficulty diverged at question $index")
            }
        }
    }

    @Test
    fun conversionsAskForSomethingWorthConverting() {
        // With a tolerance of at least one, an answer of 0, 1 or 2 accepts nearly anything nearby, and
        // "1 cm = ? in" is not a question anyone is asked.
        val violations = Violations("conversions with a trivially small answer")
        for (sample in samplesOf(
            ScenarioType.LENGTH_CONVERSION, ScenarioType.WEIGHT_CONVERSION,
            ScenarioType.VOLUME_CONVERSION, ScenarioType.SPEED_CONVERSION,
        )) {
            val answer = sample.problem.correctAnswer.toInt()
            violations.check(answer >= 3, sample) { "answer $answer" }
        }
        violations.assertNone()
    }
}
