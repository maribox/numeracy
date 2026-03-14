package it.bosler.numeracy.generator

import it.bosler.numeracy.model.Problem
import it.bosler.numeracy.model.ScenarioType
import kotlin.random.Random

class BlackjackGenerator : ProblemGenerator {

    private val cardNames = listOf("2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A")

    override fun generate(): Problem {
        val numCards = Random.nextInt(2, 6)
        val cards = (1..numCards).map { cardNames[Random.nextInt(cardNames.size)] }
        val bestTotal = calculateBestTotal(cards)

        val cardsDisplay = cards.joinToString(", ")

        val aceCount = cards.count { it == "A" }
        val faceCards = cards.filter { it in listOf("J", "Q", "K") }
        val numberCards = cards.filter { it !in listOf("J", "Q", "K", "A") }

        // === LEARNING (hintEasy): Full step-by-step addition strategy ===
        val hintEasy = buildString {
            // Step 1: Group face cards
            if (faceCards.isNotEmpty()) {
                val faceTotal = faceCards.size * 10
                append("Face cards (${faceCards.joinToString(", ")}): ${faceCards.size} \u00D7 10 = $faceTotal\n")
            }

            // Step 2: Find pairs in number cards that make 10
            val remaining = numberCards.map { it.toInt() }.toMutableList()
            val pairs = mutableListOf<Pair<Int, Int>>()
            val used = mutableSetOf<Int>()
            for (i in remaining.indices) {
                for (j in i + 1 until remaining.size) {
                    if (i !in used && j !in used && remaining[i] + remaining[j] == 10) {
                        pairs.add(remaining[i] to remaining[j])
                        used.add(i); used.add(j)
                    }
                }
            }
            val unpaired = remaining.filterIndexed { idx, _ -> idx !in used }

            if (pairs.isNotEmpty()) {
                append("Pairs that make 10: ${pairs.joinToString(", ") { "${it.first}+${it.second}" }}\n")
            }
            if (unpaired.isNotEmpty()) {
                append("Remaining: ${unpaired.joinToString(" + ")} = ${unpaired.sum()}\n")
            }

            // Step 3: Aces
            if (aceCount > 0) {
                val nonAceTotal = faceCards.size * 10 + numberCards.sumOf { it.toInt() }
                if (nonAceTotal + 11 <= 21) {
                    append("Ace → try 11 first: $nonAceTotal + 11 = ${nonAceTotal + 11}")
                    if (aceCount > 1) append(" (extra Aces = 1 each)")
                } else {
                    append("Ace → 11 would bust ($nonAceTotal + 11 = ${nonAceTotal + 11}), so Ace = 1: $nonAceTotal + 1 = ${nonAceTotal + 1}")
                }
                append("\n")
            }
            append("Total: $bestTotal")
        }

        // === PRACTICE (hintMedium): Strategy without answer ===
        val hintMedium = buildString {
            if (faceCards.isNotEmpty()) {
                append("${faceCards.size} face card${if (faceCards.size > 1) "s" else ""} = ${faceCards.size * 10}. ")
            }

            // Teach pairing
            val numValues = numberCards.map { it.toInt() }
            val pairsExist = numValues.indices.any { i ->
                numValues.indices.any { j -> i != j && numValues[i] + numValues[j] == 10 }
            }
            if (pairsExist) {
                append("Look for pairs summing to 10 (e.g. 7+3, 6+4). They're anchors.\n")
            } else if (numValues.size > 1) {
                append("No obvious 10-pairs. Add numbers left to right, keeping a running total.\n")
            }

            if (aceCount > 0) {
                val nonAceTotal = faceCards.size * 10 + numValues.sum()
                append("Add non-Ace cards first (= $nonAceTotal). Then: if ≤10, Ace=11. If >10, Ace=1.")
            }
        }

        val hintHard = ""

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
                "hintEasy" to hintEasy,
                "hintMedium" to hintMedium,
                "hintHard" to hintHard,
                "tip" to buildTip(),
            ),
        )
    }

    private fun buildTip(): String =
        "Blackjack Hand Totals:\n" +
        "• Card values: 2–9 = face value, 10/J/Q/K = 10, Ace = 11 (or 1 to avoid bust).\n" +
        "• Scan for pairs summing to 10 first (e.g. 7+3, 6+4, 9+1). These become anchors.\n" +
        "• Face cards are all equal: three face cards = 30. Group them mentally before adding number cards.\n" +
        "• Soft hand (Ace=11): you can't bust on the next card. Hard hand (Ace=1 or no Ace): any 10-value card busts you if total ≥ 12.\n" +
        "• Running total trick: add cards left-to-right, keeping a running sum. Don't re-add from scratch.\n" +
        "• Ace last: if you have multiple cards, add all non-Ace cards first, then decide if Ace = 11 or 1."

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

    private fun buildExplanation(cards: List<String>, total: Int): String {
        val values = cards.map { card ->
            when (card) {
                "A" -> "A"
                "J", "Q", "K" -> "$card=10"
                else -> card
            }
        }
        val aceCount = cards.count { it == "A" }
        val suffix = if (aceCount > 0) " (Aces adjusted for best total)" else ""
        return "${values.joinToString(" + ")} = $total$suffix"
    }
}
