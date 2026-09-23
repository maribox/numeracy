package it.bosler.numeracy.generator

import it.bosler.numeracy.model.Problem
import it.bosler.numeracy.model.ScenarioType
import kotlin.random.Random

class BlackjackGenerator(private val rng: Random = Random.Default) : ProblemGenerator {

    private val cardNames = listOf("2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A")

    override fun generate(): Problem {
        val numCards = rng.nextInt(2, 6)
        val cards = (1..numCards).map { cardNames[rng.nextInt(cardNames.size)] }
        val bestTotal = calculateBestTotal(cards)

        val cardsDisplay = cards.joinToString(", ")

        val aceCount = cards.count { it == "A" }
        val faceCards = cards.filter { it in listOf("J", "Q", "K") }
        val numberCards = cards.filter { it !in listOf("J", "Q", "K", "A") }

        return Problem(
            scenarioType = ScenarioType.BLACKJACK,
            questionText = "Your hand: $cardsDisplay\n\nWhat's your best total?",
            correctAnswer = bestTotal.toString(),
            explanation = buildExplanation(cards, bestTotal),
            metadata = mapOf(
                "cards" to cards.joinToString(","),
                // Practice mode helpers: pre-computed group totals
                "faceTotal" to (faceCards.size * 10).toString(),
                "numberTotal" to numberCards.sumOf { it.toInt() }.toString(),
                "aceCount" to aceCount.toString(),
            ),
        )
    }

    private fun calculateBestTotal(cards: List<String>): Int {
        var total = 0
        var aces = 0

        for (card in cards) {
            when (card) {
                "A" -> {
                    total += 11
                    aces++
                }
                "J", "Q", "K" -> total += 10
                else -> total += card.toInt()
            }
        }

        while (total > 21 && aces > 0) {
            total -= 10
            aces--
        }

        return total
    }

    /**
     * The hand as a sum twice over, cards then values, with each ace at what it counts for in the best
     * total: "A + K + 5 = 1 + 10 + 5 = 16". Every "=" in it is true.
     */
    private fun buildExplanation(cards: List<String>, total: Int): String {
        val hard = cards.sumOf { cardValue(it, aceHigh = false) }
        var aceHighLeft = if ("A" in cards && hard + 10 == total) 1 else 0
        val values = cards.map { card ->
            if (card == "A" && aceHighLeft > 0) { aceHighLeft--; 11 } else cardValue(card, aceHigh = false)
        }
        return "${cards.joinToString(" + ")} = ${values.joinToString(" + ")} = $total"
    }

    private fun cardValue(card: String, aceHigh: Boolean): Int = when (card) {
        "A" -> if (aceHigh) 11 else 1
        "J", "Q", "K" -> 10
        else -> card.toInt()
    }
}
