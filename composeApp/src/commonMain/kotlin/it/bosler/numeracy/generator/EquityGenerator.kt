package it.bosler.numeracy.generator

import it.bosler.numeracy.model.Card
import it.bosler.numeracy.model.Problem
import it.bosler.numeracy.model.Rank
import it.bosler.numeracy.model.ScenarioType
import it.bosler.numeracy.model.Suit
import it.bosler.numeracy.model.fullDeck
import kotlin.random.Random

/**
 * Generates hand equity estimation problems using the Rule of 2 and 4.
 * Shows actual cards dealt, tells the user their outs, and asks for equity %.
 */
class EquityGenerator : ProblemGenerator {

    override fun generate(): Problem {
        val drawTypes = listOf(
            "flush draw" to 9,
            "open-ended straight draw" to 8,
            "gutshot straight draw" to 4,
            "two overcards" to 6,
            "flush draw + gutshot" to 12,
            "one overcard" to 3,
            "pocket pair to set" to 2,
            "double gutshot" to 8,
        )

        val (drawName, outs) = drawTypes[Random.nextInt(drawTypes.size)]
        val isFlop = Random.nextBoolean()
        val street = if (isFlop) "flop" else "turn"
        val multiplier = if (isFlop) 4 else 2
        val boardSize = if (isFlop) 3 else 4

        val equity = outs * multiplier
        val streetLabel = if (isFlop) "Flop (2 cards to come)" else "Turn (1 card to come)"

        // Deal random cards for visual context
        val deck = fullDeck().shuffled()
        val hole = deck.take(2)
        val board = deck.drop(2).take(boardSize)
        val holeStr = hole.joinToString(",") { it.display }
        val boardStr = board.joinToString(",") { it.display }

        return Problem(
            scenarioType = ScenarioType.EQUITY,
            questionText = "You have a $drawName ($outs outs).\nEstimate your equity using the Rule of $multiplier.",
            correctAnswer = equity.toString(),
            explanation = "$outs outs × $multiplier = $equity%",
            metadata = mapOf(
                "drawName" to drawName,
                "outs" to outs.toString(),
                "street" to street,
                "streetLabel" to streetLabel,
                "multiplier" to multiplier.toString(),
                "equity" to equity.toString(),
                "holeCards" to holeStr,
                "boardCards" to boardStr,
            ),
        )
    }
}
