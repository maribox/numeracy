package it.bosler.numeracy.generator

import it.bosler.numeracy.model.ScenarioType
import kotlin.math.abs
import kotlin.math.floor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Every sum a hint writes out with "=" is true. A learner copies the working, and "14 ÷ 5 = 2" in
 * the Learning breakdown teaches exactly the mistake the scenario exists to remove. Approximate
 * steps are written with "≈", which this leaves alone.
 */
class HintHonestyTest {

    /** The text a question shows as working: hints, trick steps, the explanation. */
    private fun workingOf(sample: Sample): List<Pair<String, String>> {
        val m = sample.problem.metadata
        val keyed = m.filterKeys { key ->
            key.startsWith("hint") || key == "trickSteps" || key == "practiceHint" || key == "tip" ||
                Regex("step\\d+|trick\\d+_step\\d+").matches(key)
        }
        return keyed.map { it.key to it.value } + ("explanation" to sample.problem.explanation)
    }

    @Test
    fun everyWrittenSumIsTrue() {
        val violations = Violations("false sums in the working")
        for (sample in everySample()) {
            for ((key, text) in workingOf(sample)) {
                for (claim in falseClaims(text)) {
                    violations.check(false, sample) { "$key: '$claim'" }
                }
            }
        }
        violations.assertNone()
    }

    @Test
    fun conversionsSayApproximatelyWhenTheyRound() {
        val violations = Violations("rounded conversions written as exact")
        for (sample in samplesOf(
            ScenarioType.LENGTH_CONVERSION, ScenarioType.WEIGHT_CONVERSION,
            ScenarioType.VOLUME_CONVERSION, ScenarioType.SPEED_CONVERSION,
        )) {
            val m = sample.problem.metadata
            val exact = m.getValue("value").toDouble() * m.getValue("factor").toDouble()
            val rounds = abs(exact - sample.problem.correctAnswer.toDouble()) > 1e-9
            violations.check(!rounds || "≈" in sample.problem.explanation, sample) {
                "'${sample.problem.explanation}' rounds $exact"
            }
        }
        violations.assertNone()
    }

    // ── The checker ──────────────────────────────────────────────────────────

    /** Every "left = right" in [text] whose left side is arithmetic and whose value is not right. */
    internal fun falseClaims(text: String): List<String> {
        // A currency sign or a thousands separator is typography, not arithmetic.
        // The #a{…} markup colours a number in the trick displays and is not part of the sum.
        val plain = text.replace(Regex("#[a-z]\\{|}"), "")
            .replace(Regex("[€$£¥₹₩฿₺]"), "").replace(Regex("(?<=\\d),(?=\\d{3}\\b)"), "")
        val claims = mutableListOf<String>()
        var at = plain.indexOf('=')
        while (at >= 0) {
            val left = plain.substring(0, at).takeLastWhile { it in ARITHMETIC }.trim()
            val after = plain.substring(at + 1)
            // The right side runs to the next "=" when the sum is chained: "A + 5 = 11 + 5 = 16".
            val rightRun = after.takeWhile { it in ARITHMETIC }
            val rightNumber = Regex("^\\s*([+\\-−]?\\d+(?:\\.\\d+)?)").find(after)
            // A left side that starts with an operator continues a sum whose first operand is a word
            // ("J + 9 + 10 = …") or the line before, so what it adds up to is not on the page.
            val continues = Regex("^[+\\-−×÷*/]\\s").containsMatchIn(left)
            if (rightNumber != null && !continues && Regex("\\d\\s*[+\\-−×÷*/]\\s*\\(?\\s*[+\\-−]?\\d").containsMatchIn(left)) {
                val value = evaluate(left)
                val rightIsSum = Regex("\\d\\s*[+\\-−×÷*/]\\s*\\(?\\s*[+\\-−]?\\d").containsMatchIn(rightRun)
                val stated = if (rightIsSum) evaluate(rightRun.trim()) else rightNumber.groupValues[1].replace('−', '-').toDouble()
                val statedText = if (rightIsSum) rightRun.trim() else rightNumber.groupValues[1]
                val rest = if (rightIsSum) after.substring(rightRun.length) else after.substring(rightNumber.range.last + 1)
                if (value != null && stated != null && !agrees(left, value, stated, statedText, rest)) {
                    claims += "$left = $statedText${rest.take(14).substringBefore('\n')}"
                }
            }
            at = plain.indexOf('=', at + 1)
        }
        return claims
    }

    private fun agrees(left: String, value: Double, stated: Double, statedText: String, rest: String): Boolean {
        if (abs(value - stated) < 1e-6) return true
        // An amount written to the cent is right when it is the value to the cent.
        if (Regex("\\d+\\.\\d{2}").matches(statedText.trimStart('+', '-', '−')) && abs(value - stated) < 0.005) return true
        // A whole percentage is the question's own rounding.
        if (rest.startsWith("%") && '.' !in statedText && abs(value - stated) <= 0.5) return true
        // "85 ÷ 12 = 7 remainder 1" and "7 ÷ 4 = 1 (whole part)" are division that drops the rest, and say so.
        val droppingRest = rest.trimStart().let { it.startsWith("remainder") || it.startsWith("(whole") || it.startsWith("(round down") }
        if (droppingRest && '÷' in left && floor(value) == stated) return true
        return false
    }

    /** Arithmetic with + − × ÷ and brackets, the way the hints write it; null if it is not that. */
    internal fun evaluate(expression: String): Double? {
        val tokens = Regex("\\d+(?:\\.\\d+)?|[+\\-−×÷*/()]").findAll(expression).map { it.value }.toList()
        if (tokens.joinToString("") != expression.replace(" ", "")) return null
        var position = 0
        fun peek() = tokens.getOrNull(position)
        fun next() = tokens[position++]
        lateinit var sum: () -> Double?
        fun atom(): Double? {
            val token = peek() ?: return null
            return when (token) {
                "(" -> { next(); val inner = sum(); if (peek() != ")") return null; next(); inner }
                "+" -> { next(); atom() }
                "-", "−" -> { next(); atom()?.let { -it } }
                else -> token.toDoubleOrNull()?.also { next() }
            }
        }
        fun product(): Double? {
            var value = atom() ?: return null
            while (peek() in setOf("×", "÷", "*", "/")) {
                val op = next()
                val right = atom() ?: return null
                value = if (op == "×" || op == "*") value * right else if (right == 0.0) return null else value / right
            }
            return value
        }
        sum = {
            var value = product()
            while (value != null && peek() in setOf("+", "-", "−")) {
                val op = next()
                val right = product()
                value = if (right == null) null else if (op == "+") value + right else value - right
            }
            value
        }
        val result = sum()
        return if (position == tokens.size) result else null
    }

    @Test
    fun theCheckerReadsSumsTheWayTheHintsWriteThem() {
        assertEquals(listOf("14 ÷ 5 = 2"), falseClaims("7 × 2 = 14\n14 ÷ 5 = 2"))
        assertEquals(emptyList(), falseClaims("56 ÷ 5 ≈ 11"))
        assertEquals(emptyList(), falseClaims("85 ÷ 12 = 7 remainder 1"))
        assertEquals(emptyList(), falseClaims("€20.00 - €7.35 = €12.65"))
        assertEquals(listOf("20.00 - 7.38 = 12.61"), falseClaims("€20.00 - €7.38 = €12.61"))
        assertEquals(emptyList(), falseClaims("40 ÷ (120 + 40) × 100 = 25%"))
        assertEquals(emptyList(), falseClaims("+1 − (-5) = +6 hours"))
        assertEquals(emptyList(), falseClaims("Triple 20 = 60 pts, 170=T20 T20 Bull"))
        assertEquals(emptyList(), falseClaims("#a{50} + #b{4} = #r{54}"))
        assertEquals(emptyList(), falseClaims("(#a{3} + #a{4}) ÷ 2 = #b{3} (round down, ending becomes 75)"))
        assertEquals(emptyList(), falseClaims("A + K + 5 = 1 + 10 + 5 = 16"))
        assertEquals(listOf("1 + 10 + 5 = 17"), falseClaims("A + K + 5 = 1 + 10 + 5 = 17"))
        assertEquals(emptyList(), falseClaims("J + 9 + 10 = 10 + 9 + 10 = 29"))
        assertNull(evaluate("3 + "))
    }

    private companion object {
        const val ARITHMETIC = "0123456789. +-−×÷*/()"
    }
}
