package it.bosler.numeracy.generator

import it.bosler.numeracy.model.Difficulty
import it.bosler.numeracy.model.InputType
import it.bosler.numeracy.model.Problem
import it.bosler.numeracy.model.ScenarioType
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Equity by the rule of 2 and 4, for a real drawing hand. On the flop the opponent is all in, which
 * is the one spot where both remaining cards come for a single call and the rule of 4 applies; facing
 * a bet with more betting to come, only the turn is paid for, and the rule of 2 is the one to use.
 */
class DrawEquityGenerator(
    private val difficulty: Difficulty = Difficulty.NORMAL,
    private val rng: Random = Random.Default,
) : ProblemGenerator {

    override fun generate(): Problem {
        val kind = DrawKind.entries.random(rng)
        val isFlop = rng.nextBoolean()
        val deal = dealDraw(kind, isFlop, rng)
        val street = if (isFlop) "Flop" else "Turn"
        val multiplier = if (isFlop) 4 else 2
        val equity = kind.outs * multiplier

        val pot = (4..25).random(rng) * 40
        val callOptions = listOf(pot / 4, pot / 3, pot / 2, pot * 2 / 3)
        val callAmount = callOptions.random(rng).let { (it / 10) * 10 }.coerceAtLeast(20)
        val totalPot = pot + callAmount
        val potOdds = (callAmount.toDouble() / totalPot * 100).roundToInt()

        return Problem(
            scenarioType = ScenarioType.DRAW_EQUITY,
            questionText = "What is your equity? (%)",
            correctAnswer = equity.toString(),
            inputType = InputType.NUMBER,
            explanation = "${kind.outs} outs \u00D7 $multiplier (Rule of $multiplier) = $equity%",
            metadata = mapOf(
                "drawName" to kind.displayName,
                "drawExplanation" to kind.explanation,
                "outs" to kind.outs.toString(),
                "multiplier" to multiplier.toString(),
                "equity" to equity.toString(),
                "exactEquity" to deal.hitPercent.roundToInt().toString(),
                "street" to street,
                "potAmount" to pot.toString(),
                "callAmount" to callAmount.toString(),
                "totalPot" to totalPot.toString(),
                "potOdds" to potOdds.toString(),
                "holeCards" to deal.hole.joinToString(",") { it.display },
                "boardCards" to deal.board.joinToString(",") { it.display },
            ),
        )
    }
}
