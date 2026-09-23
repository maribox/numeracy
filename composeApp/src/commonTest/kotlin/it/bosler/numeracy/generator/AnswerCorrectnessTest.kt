package it.bosler.numeracy.generator

import it.bosler.numeracy.model.ScenarioType
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.test.Test

/**
 * Every right answer, worked out a second time by code that shares nothing with the generator: a
 * question whose stated answer is wrong teaches the wrong sum.
 */
class AnswerCorrectnessTest {

    private fun cents(amount: String): Long = (amount.toDouble() * 100).roundToLong()

    @Test
    fun changeIsThePaymentLessTheBillToTheCent() {
        val violations = Violations("wrong change")
        for (sample in samplesOf(ScenarioType.MAKING_CHANGE)) {
            val m = sample.problem.metadata
            val bill = cents(m.getValue("billAmount"))
            val paid = cents(m.getValue("paymentAmount"))
            val change = cents(sample.problem.correctAnswer)
            violations.check(Regex("\\d+\\.\\d{2}").matches(sample.problem.correctAnswer), sample) {
                "answer '${sample.problem.correctAnswer}' is not written to the cent"
            }
            // Bills are made in steps of five cents, so a bill that is not is one that was misprinted.
            violations.check(bill % 5 == 0L, sample) { "bill ${m["billAmount"]} is not a multiple of 5 cents" }
            violations.check(change == paid - bill, sample) { "${m["paymentAmount"]} - ${m["billAmount"]} is not $change cents" }
            violations.check(m.getValue("eurosChange").toLong() == change / 100, sample) { "euro part ${m["eurosChange"]}" }
            violations.check(m.getValue("centsChange").toLong() == change % 100, sample) { "cent part ${m["centsChange"]}" }
        }
        violations.assertNone()
    }

    /** 1 January 1900 was a Monday; everything else is counting days. */
    private fun weekday(year: Int, month: Int, day: Int): String {
        fun leap(y: Int) = (y % 4 == 0 && y % 100 != 0) || y % 400 == 0
        val monthLengths = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var days = 0L
        for (y in 1900 until year) days += if (leap(y)) 366 else 365
        for (m in 1 until month) days += monthLengths[m - 1] + if (m == 2 && leap(year)) 1 else 0
        days += day - 1
        val names = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        return names[(days % 7).toInt()]
    }

    @Test
    fun doomsdayAnswersTheRealWeekday() {
        val violations = Violations("wrong weekdays")
        for (sample in samplesOf(ScenarioType.DOOMSDAY)) {
            val m = sample.problem.metadata
            val expected = weekday(m.getValue("year").toInt(), m.getValue("month").toInt(), m.getValue("day").toInt())
            violations.check(sample.problem.correctAnswer == expected, sample) { "expected $expected" }
        }
        violations.assertNone()
    }

    @Test
    fun doomsdayWorkingArrivesAtTheAnswer() {
        val names = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val violations = Violations("doomsday working that does not lead to the answer")
        for (sample in samplesOf(ScenarioType.DOOMSDAY)) {
            val m = sample.problem.metadata
            val year = m.getValue("year").toInt()
            val yy = m.getValue("yy").toInt()
            val a = m.getValue("yyDiv12").toInt()
            val b = m.getValue("yyRemainder").toInt()
            val c = m.getValue("remainderDiv4").toInt()
            violations.check(yy == year % 100 && a == yy / 12 && b == yy % 12 && c == b / 4, sample) { "year steps $yy $a $b $c" }
            violations.check(m.getValue("yearCalcSum").toInt() == a + b + c, sample) { "sum ${m["yearCalcSum"]}" }
            val century = names.indexOf(m.getValue("centuryAnchor"))
            violations.check(century.toString() == m["centuryAnchorIndex"], sample) { "century index" }
            val doomsday = (century + a + b + c) % 7
            violations.check(names[doomsday] == m["yearDoomsday"], sample) { "year doomsday ${m["yearDoomsday"]}, worked ${names[doomsday]}" }
            val landed = names[(doomsday + m.getValue("diffFromRef").toInt()) % 7]
            violations.check(landed == sample.problem.correctAnswer, sample) { "counting from the anchor lands on $landed" }
            val day = m.getValue("day").toInt()
            val reference = m.getValue("doomsdayRef").toInt()
            violations.check(((day - reference) % 7 + 7) % 7 == m.getValue("diffFromRef").toInt(), sample) { "offset from anchor" }
        }
        violations.assertNone()
    }

    @Test
    fun conversionsMultiplyByTheFactorTheyName() {
        val violations = Violations("conversions that do not match their factor")
        for (sample in samplesOf(
            ScenarioType.LENGTH_CONVERSION, ScenarioType.WEIGHT_CONVERSION,
            ScenarioType.VOLUME_CONVERSION, ScenarioType.SPEED_CONVERSION,
        )) {
            val m = sample.problem.metadata
            val expected = (m.getValue("value").toDouble() * m.getValue("factor").toDouble()).roundToInt()
            violations.check(sample.problem.correctAnswer == expected.toString(), sample) { "expected $expected" }
        }
        violations.assertNone()
    }

    @Test
    fun temperaturesFollowTheFormula() {
        val violations = Violations("wrong temperatures")
        for (sample in samplesOf(ScenarioType.TEMPERATURE_CONVERSION)) {
            val m = sample.problem.metadata
            val value = m.getValue("value").toInt()
            val expected = if (m["fromUnit"] == "°C") (value * 9.0 / 5.0 + 32).roundToInt()
            else ((value - 32) * 5.0 / 9.0).roundToInt()
            violations.check(sample.problem.correctAnswer == expected.toString(), sample) { "expected $expected" }
        }
        violations.assertNone()
    }

    @Test
    fun blackjackTotalsAreTheBestHand() {
        val violations = Violations("wrong blackjack totals")
        for (sample in samplesOf(ScenarioType.BLACKJACK)) {
            val cards = sample.problem.metadata.getValue("cards").split(",")
            val hard = cards.sumOf { when (it) { "A" -> 1; "J", "Q", "K" -> 10; else -> it.toInt() } }
            // One ace counted as eleven is the only upgrade that can ever help.
            val best = if ("A" in cards && hard + 10 <= 21) hard + 10 else hard
            violations.check(sample.problem.correctAnswer == best.toString(), sample) { "expected $best from $cards" }
        }
        violations.assertNone()
    }

    /** What a dart named [name] scores, and whether it is a double for finishing. */
    private fun dartValue(name: String): Pair<Int, Boolean> {
        val number = name.substringAfterLast(' ').toIntOrNull()
        return when {
            name == "Bull" -> 50 to true
            name == "Single Bull" -> 25 to false
            name == "Miss" -> 0 to false
            name.startsWith("Triple") -> 3 * number!! to false
            name.startsWith("Double") -> 2 * number!! to true
            else -> number!! to false
        }
    }

    @Test
    fun dartsScoresByTheRulesOf501DoubleOut() {
        val violations = Violations("darts scored against the rules")
        for (sample in samplesOf(ScenarioType.DARTS)) {
            val m = sample.problem.metadata
            val (value, isDouble) = dartValue(m.getValue("throwName"))
            violations.check(m.getValue("throwValue").toInt() == value, sample) { "${m["throwName"]} is worth $value" }
            val before = m.getValue("currentScore").toInt()
            val left = before - value
            val bust = left < 0 || left == 1 || (left == 0 && !isDouble)
            violations.check(m.getValue("bust").toBoolean() == bust, sample) { "bust should be $bust" }
            val expected = if (bust) m.getValue("visitStart").toInt() else left
            violations.check(sample.problem.correctAnswer == expected.toString(), sample) { "expected $expected" }
        }
        violations.assertNone()
    }

    @Test
    fun aLegOfDartsIsPlayedInVisitsOfThreeFrom501() {
        val generator = DartsGenerator(kotlin.random.Random(11))
        var score = 501
        var visitStart = 501
        var dart = 1
        var legsWon = 0
        repeat(20_000) { index ->
            val m = generator.generate().metadata
            if (score == 0) { score = 501; visitStart = 501; dart = 1; legsWon++ }
            check(m.getValue("currentScore").toInt() == score) { "dart $index: score ${m["currentScore"]}, expected $score" }
            check(m.getValue("visitStart").toInt() == visitStart) { "dart $index: scoreboard ${m["visitStart"]}, expected $visitStart" }
            check(m.getValue("dartInVisit").toInt() == dart) { "dart $index: dart ${m["dartInVisit"]} of the visit, expected $dart" }
            score = m.getValue("newScore").toInt()
            val bust = m.getValue("bust").toBoolean()
            if (score == 0) check(dartValue(m.getValue("throwName")).second) { "a leg won on ${m["throwName"]}" }
            if (bust || dart == 3 || score == 0) { visitStart = score; dart = 1 } else dart++
        }
        check(legsWon > 100) { "only $legsWon legs finished in 20,000 darts" }
    }

    @Test
    fun potOddsAreTheCallsShareOfThePot() {
        fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)
        val violations = Violations("wrong pot odds")
        for (sample in samplesOf(ScenarioType.POT_ODDS)) {
            val m = sample.problem.metadata
            val pot = m.getValue("potAmount").toInt()
            val call = m.getValue("callAmount").toInt()
            val expected = (call * 100.0 / (pot + call)).roundToInt()
            violations.check(sample.problem.correctAnswer == expected.toString(), sample) { "expected $expected" }
            violations.check(m.getValue("totalPot").toInt() == pot + call, sample) { "total pot" }
            val (num, den) = m.getValue("fraction").split("/").map { it.toInt() }
            violations.check(num * (pot + call) == den * call && gcd(num, den) == 1, sample) { "fraction $num/$den" }
            val cards = m.getValue("holeCards").split(",") + m.getValue("boardCards").split(",")
            violations.check(cards.toSet().size == cards.size, sample) { "a card dealt twice: $cards" }
        }
        violations.assertNone()
    }

    @Test
    fun drawEquityIsOutsTimesTheRuleForTheStreet() {
        val violations = Violations("wrong draw equity")
        for (sample in samplesOf(ScenarioType.DRAW_EQUITY)) {
            val m = sample.problem.metadata
            val board = m.getValue("boardCards").split(",")
            val hole = m.getValue("holeCards").split(",")
            val multiplier = if (m["street"] == "Flop") 4 else 2
            violations.check(board.size == if (multiplier == 4) 3 else 4, sample) { "${m["street"]} with ${board.size} board cards" }
            violations.check(m.getValue("multiplier").toInt() == multiplier, sample) { "multiplier" }
            val expected = m.getValue("outs").toInt() * multiplier
            violations.check(sample.problem.correctAnswer == expected.toString(), sample) { "expected $expected" }
            violations.check(hole.size == 2 && (hole + board).toSet().size == hole.size + board.size, sample) { "cards $hole $board" }
        }
        violations.assertNone()
    }

    @Test
    fun squaresAndProductsAreRight() {
        val violations = Violations("wrong products")
        for (sample in samplesOf(ScenarioType.SQUARING, ScenarioType.MULTIPLICATION)) {
            val numbers = Regex("\\d+").findAll(sample.problem.questionText).map { it.value.toLong() }.toList()
            val expected = if (sample.type == ScenarioType.SQUARING) numbers[0] * numbers[0] else numbers[0] * numbers[1]
            violations.check(sample.problem.correctAnswer == expected.toString(), sample) { "expected $expected" }
        }
        violations.assertNone()
    }

    @Test
    fun everyTrickArrivesAtTheAnswer() {
        val violations = Violations("tricks whose working ends somewhere else")
        for (sample in samplesOf(ScenarioType.SQUARING, ScenarioType.MULTIPLICATION)) {
            val m = sample.problem.metadata
            val count = m.getValue("trickCount").toInt()
            for (trick in 0 until count) {
                val steps = m.getValue("trick${trick}_stepCount").toInt()
                val last = m.getValue("trick${trick}_step$steps")
                val numbers = Regex("\\d[\\d,]*").findAll(last).map { it.value.replace(",", "") }.toList()
                violations.check(sample.problem.correctAnswer in numbers, sample) {
                    "${m["trickName$trick"]} ends at '$last'"
                }
            }
        }
        violations.assertNone()
    }

    /**
     * UTC offsets in minutes in January and in July. Sydney and Auckland keep daylight saving in
     * January, their summer; the northern cities in July; Mumbai and Tokyo never.
     */
    private val offsets = mapOf(
        "New York" to (-300 to -240), "Chicago" to (-360 to -300), "Denver" to (-420 to -360),
        "Los Angeles" to (-480 to -420), "London" to (0 to 60), "Berlin" to (60 to 120),
        "Helsinki" to (120 to 180), "Mumbai" to (330 to 330), "Tokyo" to (540 to 540),
        "Sydney" to (660 to 600), "Auckland" to (780 to 720),
    )

    @Test
    fun timeZonesMoveTheClockByTheOffset() {
        val violations = Violations("wrong times")
        for (sample in samplesOf(ScenarioType.TIME_ZONES)) {
            val m = sample.problem.metadata
            val july = m.getValue("month") == "July"
            fun offset(city: String) = offsets.getValue(city).let { if (july) it.second else it.first }
            val (h, min) = m.getValue("time").split(":").map { it.toInt() }
            val total = ((h * 60 + min + offset(m.getValue("toCity")) - offset(m.getValue("fromCity"))) % 1440 + 1440) % 1440
            val expected = "${(total / 60).toString().padStart(2, '0')}:${(total % 60).toString().padStart(2, '0')}"
            violations.check(sample.problem.correctAnswer == expected, sample) { "expected $expected" }
        }
        violations.assertNone()
    }

    @Test
    fun currencyIsTheAmountAtTheRate() {
        val violations = Violations("wrong conversions")
        for (sample in samplesOf(ScenarioType.CURRENCY_EXCHANGE)) {
            val m = sample.problem.metadata
            val exact = m.getValue("fromAmount").toDouble() * m.getValue("rate").toDouble()
            val answer = sample.problem.correctAnswer.toDouble()
            val allowed = if (exact > 100) 0.5 else 0.005
            violations.check(abs(answer - exact) <= allowed, sample) { "exact $exact" }
        }
        violations.assertNone()
    }
}
