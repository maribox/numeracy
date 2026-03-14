package it.bosler.numeracy.generator

import it.bosler.numeracy.model.Problem
import it.bosler.numeracy.model.ScenarioType
import it.bosler.numeracy.model.fullDeck
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Generates implied odds problems. Given pot, call amount, and equity,
 * calculate how much you need to win on later streets to break even.
 *
 * Formula: Break-even total = call / equity
 * Need to win = break-even total - current pot - call
 */
class ImpliedOddsGenerator : ProblemGenerator {

    override fun generate(): Problem {
        val pot = Random.nextInt(4, 31) * 10       // 40-300
        val callAmount = Random.nextInt(2, 11) * 10 // 20-100

        // Pick a realistic draw equity (using rule of 2/4)
        val equityOptions = listOf(8, 12, 16, 18, 20, 24, 32, 36)
        val equity = equityOptions[Random.nextInt(equityOptions.size)]

        // Break-even total pot needed = call / (equity/100)
        val breakEvenTotal = (callAmount.toDouble() / (equity.toDouble() / 100)).roundToInt()
        val currentTotal = pot + callAmount
        val needToWin = (breakEvenTotal - currentTotal).coerceAtLeast(0)

        // Deal random cards for visual context
        val deck = fullDeck().shuffled()
        val hole = deck.take(2)
        val board = deck.drop(2).take(3) // flop scenario
        val holeStr = hole.joinToString(",") { it.display }
        val boardStr = board.joinToString(",") { it.display }

        return Problem(
            scenarioType = ScenarioType.IMPLIED_ODDS,
            questionText = "How much more do you need to win on later streets to break even?",
            correctAnswer = needToWin.toString(),
            explanation = "Break-even total = \$$callAmount / ${equity}% = \$$breakEvenTotal.\nCurrent total pot = \$$pot + \$$callAmount = \$$currentTotal.\nNeed to win: \$$breakEvenTotal \u2212 \$$currentTotal = \$$needToWin",
            metadata = mapOf(
                "potAmount" to pot.toString(),
                "callAmount" to callAmount.toString(),
                "equity" to equity.toString(),
                "breakEvenTotal" to breakEvenTotal.toString(),
                "currentTotal" to currentTotal.toString(),
                "needToWin" to needToWin.toString(),
                "holeCards" to holeStr,
                "boardCards" to boardStr,
            ),
        )
    }
}
