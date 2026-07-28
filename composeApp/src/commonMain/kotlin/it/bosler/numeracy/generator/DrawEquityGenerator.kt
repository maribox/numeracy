package it.bosler.numeracy.generator

import it.bosler.numeracy.model.Difficulty
import it.bosler.numeracy.model.InputType
import it.bosler.numeracy.model.Problem
import it.bosler.numeracy.model.Rank
import it.bosler.numeracy.model.ScenarioType
import it.bosler.numeracy.model.Suit
import it.bosler.numeracy.model.fullDeck
import kotlin.math.roundToInt
import kotlin.random.Random

class DrawEquityGenerator(
    private val difficulty: Difficulty = Difficulty.NORMAL,
    private val rng: Random = Random.Default,
) : ProblemGenerator {

    private data class Draw(val name: String, val outs: Int, val explanation: String)

    private val draws = listOf(
        Draw("Flush draw", 9, "9 remaining cards of your suit"),
        Draw("Open-ended straight draw", 8, "4 cards of two different ranks complete the straight"),
        Draw("Gutshot straight draw", 4, "Only 4 cards of one rank fill the gap"),
        Draw("Two overcards", 6, "3 aces + 3 kings pair up on the board"),
        Draw("Flush draw + gutshot", 12, "9 flush outs + 4 straight outs \u2212 1 overlap"),
    )

    override fun generate(): Problem {
        val draw = draws.random(rng)
        val isFlop = rng.nextBoolean()
        val street = if (isFlop) "Flop" else "Turn"
        val multiplier = if (isFlop) 4 else 2
        val equity = draw.outs * multiplier

        val (hole, board) = generateCards(draw, isFlop)

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
            explanation = "${draw.outs} outs \u00D7 $multiplier (Rule of $multiplier) = $equity%",
            metadata = mapOf(
                "drawName" to draw.name,
                "drawExplanation" to draw.explanation,
                "outs" to draw.outs.toString(),
                "multiplier" to multiplier.toString(),
                "equity" to equity.toString(),
                "street" to street,
                "potAmount" to pot.toString(),
                "callAmount" to callAmount.toString(),
                "totalPot" to totalPot.toString(),
                "potOdds" to potOdds.toString(),
                "holeCards" to hole.joinToString(",") { it.display },
                "boardCards" to board.joinToString(",") { it.display },
            ),
        )
    }

    private fun generateCards(draw: Draw, isFlop: Boolean): Pair<List<it.bosler.numeracy.model.Card>, List<it.bosler.numeracy.model.Card>> {
        val deck = fullDeck().toMutableList()
        deck.shuffle()

        val (hole, boardExtra) = when (draw.name) {
            "Flush draw" -> generateFlushDraw(deck)
            "Open-ended straight draw" -> generateOESD(deck)
            "Gutshot straight draw" -> generateGutshot(deck)
            "Two overcards" -> generateOvercards(deck)
            "Flush draw + gutshot" -> generateFlushGutshot(deck)
            else -> generateFlushDraw(deck)
        }

        val used = hole + boardExtra
        val extras = deck.filter { it !in used }.shuffled()
        val board = if (isFlop) boardExtra.take(2) + extras.take(1) else boardExtra.take(2) + extras.take(2)
        return hole to board
    }

    // hole: 2 of suit S; boardExtra: 2 more of suit S
    private fun generateFlushDraw(deck: List<it.bosler.numeracy.model.Card>): Pair<List<it.bosler.numeracy.model.Card>, List<it.bosler.numeracy.model.Card>> {
        val suit = Suit.entries.random(rng)
        val suited = deck.filter { it.suit == suit }.shuffled()
        return suited.take(2) to suited.drop(2).take(2)
    }

    // hole: rank r and r+1; boardExtra: r+2 and r+3 (open-ended)
    private fun generateOESD(deck: List<it.bosler.numeracy.model.Card>): Pair<List<it.bosler.numeracy.model.Card>, List<it.bosler.numeracy.model.Card>> {
        val start = rng.nextInt(2, 10)
        val rankGroup = { v: Int -> deck.filter { it.rank.value == v }.random(rng) }
        val c1 = rankGroup(start)
        val c2 = deck.filter { it.rank.value == start + 1 && it != c1 }.random(rng)
        val c3 = deck.filter { it.rank.value == start + 2 && it != c1 && it != c2 }.random(rng)
        val c4 = deck.filter { it.rank.value == start + 3 && it != c1 && it != c2 && it != c3 }.random(rng)
        return listOf(c1, c2) to listOf(c3, c4)
    }

    // hole: rank r and r+1; boardExtra: r+3 and r+4 (gap at r+2)
    private fun generateGutshot(deck: List<it.bosler.numeracy.model.Card>): Pair<List<it.bosler.numeracy.model.Card>, List<it.bosler.numeracy.model.Card>> {
        val start = rng.nextInt(2, 9)
        val pick = { v: Int, exclude: List<it.bosler.numeracy.model.Card> ->
            deck.filter { it.rank.value == v && it !in exclude }.random(rng)
        }
        val c1 = pick(start, emptyList())
        val c2 = pick(start + 1, listOf(c1))
        val c3 = pick(start + 3, listOf(c1, c2))
        val c4 = pick(start + 4, listOf(c1, c2, c3))
        return listOf(c1, c2) to listOf(c3, c4)
    }

    // hole: A + K; boardExtra: two low cards (rank ≤ 10)
    private fun generateOvercards(deck: List<it.bosler.numeracy.model.Card>): Pair<List<it.bosler.numeracy.model.Card>, List<it.bosler.numeracy.model.Card>> {
        val ace = deck.filter { it.rank == Rank.ACE }.random(rng)
        val king = deck.filter { it.rank == Rank.KING }.random(rng)
        val low = deck.filter { it !in listOf(ace, king) && it.rank.value <= 10 }.shuffled().take(2)
        return listOf(ace, king) to low
    }

    // hole: 2 suited + part of a gutshot; boardExtra: 2 more suited
    private fun generateFlushGutshot(deck: List<it.bosler.numeracy.model.Card>): Pair<List<it.bosler.numeracy.model.Card>, List<it.bosler.numeracy.model.Card>> {
        val suit = Suit.entries.random(rng)
        val suited = deck.filter { it.suit == suit && it.rank.value in 5..13 }.shuffled()
        return suited.take(2) to suited.drop(2).take(2)
    }
}
