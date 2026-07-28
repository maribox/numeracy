package it.bosler.numeracy.generator

import it.bosler.numeracy.model.Difficulty
import it.bosler.numeracy.model.InputType
import it.bosler.numeracy.model.Problem
import it.bosler.numeracy.model.ScenarioType
import it.bosler.numeracy.model.fullDeck
import kotlin.math.roundToInt
import kotlin.random.Random

class PokerGenerator(
    private val difficulty: Difficulty = Difficulty.NORMAL,
    private val rng: Random = Random.Default,
) : ProblemGenerator {

    override fun generate(): Problem {
        val (pot, callAmount) = generateNumbers()
        val totalPot = pot + callAmount
        val potOdds = (callAmount.toDouble() / totalPot * 100).roundToInt()

        val gcd = gcd(callAmount, totalPot)
        val simplNum = callAmount / gcd
        val simplDen = totalPot / gcd

        val deck = fullDeck().shuffled()
        val hole = deck.take(2)
        val boardSize = if (rng.nextBoolean()) 3 else 4
        val board = deck.drop(2).take(boardSize)
        val street = if (boardSize == 3) "Flop" else "Turn"

        // Win% shown for context on HARD and NORMAL (teaches call/fold decision)
        val winPercent = (15..65).random(rng)

        return Problem(
            scenarioType = ScenarioType.POT_ODDS,
            questionText = "What are your pot odds? (%)",
            correctAnswer = potOdds.toString(),
            inputType = InputType.NUMBER,
            explanation = "\$$callAmount \u00F7 (\$$pot + \$$callAmount) \u00D7 100 = $potOdds%",
            tolerancePercent = if (difficulty == Difficulty.HARD) 2.0 else 0.0,
            metadata = mapOf(
                "potAmount" to pot.toString(),
                "callAmount" to callAmount.toString(),
                "totalPot" to totalPot.toString(),
                "potOdds" to potOdds.toString(),
                "simplNum" to simplNum.toString(),
                "simplDen" to simplDen.toString(),
                "fraction" to "$simplNum/$simplDen",
                "holeCards" to hole.joinToString(",") { it.display },
                "boardCards" to board.joinToString(",") { it.display },
                "street" to street,
                "winPercent" to winPercent.toString(),
            ),
        )
    }

    private fun generateNumbers(): Pair<Int, Int> = when (difficulty) {
        Difficulty.HARD -> {
            // Non-round numbers — the fraction won't simplify to 1/n
            val call = listOf(35, 45, 55, 65, 75, 85, 95, 115, 125, 135).random(rng)
            val pot = listOf(70, 90, 110, 130, 150, 175, 195, 225, 250, 280, 320).random(rng)
            pot to call
        }
        else -> {
            // Clean numbers: call/totalPot = 1/n for easy mental math
            val pairs = listOf(
                50 to 150,   // 50/200 = 1/4 = 25%
                60 to 120,   // 60/180 = 1/3 = 33%
                50 to 100,   // 50/150 = 1/3 = 33%
                40 to 120,   // 40/160 = 1/4 = 25%
                50 to 200,   // 50/250 = 1/5 = 20%
                40 to 80,    // 40/120 = 1/3 = 33%
                60 to 180,   // 60/240 = 1/4 = 25%
                80 to 160,   // 80/240 = 1/3 = 33%
                50 to 250,   // 50/300 = 1/6 = 17%
                100 to 100,  // 100/200 = 1/2 = 50%
                60 to 240,   // 60/300 = 1/5 = 20%
            )
            val (call, pot) = pairs.random(rng)
            pot to call
        }
    }

    private fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)
}
