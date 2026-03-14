package it.bosler.numeracy.generator

import it.bosler.numeracy.model.Card
import it.bosler.numeracy.model.Problem
import it.bosler.numeracy.model.ScenarioType
import it.bosler.numeracy.model.fullDeck
import kotlin.math.roundToInt
import kotlin.random.Random

class PokerGenerator : ProblemGenerator {

    override fun generate(): Problem {
        val pot = Random.nextInt(4, 51) * 10 // 40 to 500
        val callAmount = Random.nextInt(1, (pot / 10).coerceAtMost(10) + 1) * 10 // 10 to ~100

        val totalPot = pot + callAmount
        val potOdds = (callAmount.toDouble() / totalPot * 100).roundToInt()

        val gcd = gcd(callAmount, totalPot)
        val simplNum = callAmount / gcd
        val simplDen = totalPot / gcd

        // === LEARNING (hintEasy): Full walkthrough with technique ===
        val hintEasy = buildString {
            append("Pot odds = Call ÷ Total Pot \u00D7 100\n")
            append("Total pot = \$$pot + \$$callAmount = \$$totalPot\n")
            append("Fraction: $callAmount/$totalPot")
            if (gcd > 1) {
                append(" → ÷$gcd → $simplNum/$simplDen")
            }
            append("\n")
            // Show the division technique
            when {
                simplDen == 2 -> append("$simplNum/$simplDen = 50%")
                simplDen == 3 -> append("1/3 ≈ 33%. ${if (simplNum > 1) "$simplNum/3 = ${simplNum * 33}% (approx)" else ""}")
                simplDen == 4 -> append("1/4 = 25%. ${if (simplNum > 1) "$simplNum/4 = ${simplNum * 25}%" else ""}")
                simplDen == 5 -> append("1/5 = 20%. ${if (simplNum > 1) "$simplNum/5 = ${simplNum * 20}%" else ""}")
                simplDen in listOf(6, 7, 8, 9, 10) -> {
                    val pct = (simplNum.toDouble() / simplDen * 100).toInt()
                    append("$simplNum ÷ $simplDen ≈ $pct%")
                }
                else -> append("$simplNum ÷ $simplDen ≈ $potOdds%")
            }
            append("\n→ $potOdds%")
        }

        // === PRACTICE (hintMedium): Technique guidance ===
        val hintMedium = buildString {
            append("Total pot = pot + call = \$$totalPot\n")
            if (gcd > 1) {
                append("Both divisible by $gcd → simplify to $simplNum/$simplDen\n")
            }
            // Teach fraction-to-percentage technique
            append("Key fractions: 1/2=50%, 1/3≈33%, 1/4=25%, 1/5=20%, 1/6≈17%, 1/7≈14%\n")
            append("Find the closest match, then adjust.")
        }

        val hintHard = ""

        // Deal random cards for visual context
        val deck = fullDeck().shuffled()
        val hole = deck.take(2)
        val boardSize = listOf(3, 4).random() // flop or turn
        val board = deck.drop(2).take(boardSize)
        val holeStr = hole.joinToString(",") { it.display }
        val boardStr = board.joinToString(",") { it.display }
        val street = if (boardSize == 3) "Flop" else "Turn"

        return Problem(
            scenarioType = ScenarioType.POT_ODDS,
            questionText = "What are your pot odds? (%)",
            correctAnswer = potOdds.toString(),
            explanation = "\$$callAmount / (\$$pot + \$$callAmount) × 100 = $potOdds%",
            metadata = mapOf(
                "potAmount" to pot.toString(),
                "callAmount" to callAmount.toString(),
                "totalPot" to totalPot.toString(),
                "fraction" to "$simplNum/$simplDen",
                "holeCards" to holeStr,
                "boardCards" to boardStr,
                "street" to street,
                "hintEasy" to hintEasy,
                "hintMedium" to hintMedium,
                "hintHard" to hintHard,
                "tip" to buildTip(),
            ),
        )
    }

    private fun buildTip(): String =
        "Poker Pot Odds:\n" +
        "• Formula: call ÷ (pot + call) × 100 = pot odds %. If your hand equity > pot odds %, calling is +EV.\n" +
        "• Round both numbers to the nearest easy figure first: \$23 → \$25, \$72 → \$75. Precision isn't needed.\n" +
        "• Rule of 4 & 2: on the flop multiply outs × 4 for % to hit by river; on the turn multiply outs × 2.\n" +
        "• Key draw equities to memorise: flush draw = 9 outs ≈ 36% (flop); open-ended straight = 8 outs ≈ 32%; gutshot = 4 outs ≈ 16%.\n" +
        "• Half-pot bet = 33% pot odds. Pot-sized bet = 50% pot odds. These benchmarks cover most spots.\n" +
        "• Simplify the fraction: find what both numbers divide by, e.g. 30/(90+30) = 30/120 = 1/4 = 25%."

    private fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)
}
