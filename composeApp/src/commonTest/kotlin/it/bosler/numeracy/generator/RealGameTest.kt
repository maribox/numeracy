package it.bosler.numeracy.generator

import it.bosler.numeracy.model.Card
import it.bosler.numeracy.model.HandCategory
import it.bosler.numeracy.model.Rank
import it.bosler.numeracy.model.ScenarioType
import it.bosler.numeracy.model.Suit
import it.bosler.numeracy.model.bestCategory
import it.bosler.numeracy.model.outs
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue

/** The card games dealt the way the real games deal them, checked hand by hand. */
class RealGameTest {

    private fun card(text: String): Card = Card(
        Rank.entries.first { it.symbol == text.dropLast(1) },
        Suit.entries.first { it.symbol == text.takeLast(1) },
    )

    private fun cards(csv: String) = csv.split(",").map(::card)

    private fun hardTotal(cards: List<String>) = cards.sumOf { if (it == "A") 1 else it.toIntOrNull() ?: 10 }
    private fun bestTotal(cards: List<String>) = hardTotal(cards).let { if ("A" in cards && it + 10 <= 21) it + 10 else it }

    @Test
    fun aBlackjackHandNeverTakesACardAt21OrPastIt() {
        val violations = Violations("hands no player would have drawn")
        for (sample in samplesOf(ScenarioType.BLACKJACK)) {
            val hand = sample.problem.metadata.getValue("cards").split(",")
            violations.check(hand.size >= 2, sample) { "a hand of ${hand.size}" }
            for (taken in 2 until hand.size) {
                val before = hand.take(taken)
                violations.check(bestTotal(before) < 21, sample) { "drew to $before at ${bestTotal(before)}" }
                // Nobody hits a hard seventeen or better.
                val soft = "A" in before && hardTotal(before) + 10 <= 21
                violations.check(soft || hardTotal(before) < 17, sample) { "hit a hard ${hardTotal(before)}: $hand" }
            }
        }
        violations.assertNone()
    }

    @Test
    fun aSixDeckShoeHoldsSixOfEachCardBetweenShuffles() {
        val shoe = Shoe(Random(5))
        val dealt = List(6 * 52 * 3 / 4) { shoe.draw() }
        val counts = dealt.groupingBy { it }.eachCount()
        assertTrue(counts.values.all { it <= 6 }, "more than six decks' worth of one card: ${counts.filterValues { it > 6 }}")
        assertTrue(counts.keys.map { it.suit }.toSet().size == 4, "every suit dealt")
    }

    @Test
    fun blackjackCardsShowTheSuitsTheyWereDealtIn() {
        val suits = mutableSetOf<String>()
        val violations = Violations("hands whose suits do not match their cards")
        for (sample in samplesOf(ScenarioType.BLACKJACK)) {
            val m = sample.problem.metadata
            val handSuits = m.getValue("suits").split(",")
            violations.check(handSuits.size == m.getValue("cards").split(",").size, sample) { "suits $handSuits" }
            suits += handSuits
        }
        violations.assertNone()
        assertTrue(suits == setOf("\u2660", "\u2665", "\u2666", "\u2663"), "suits dealt: $suits")
    }

    @Test
    fun everyDrawIsTheDrawItIsCalledAndNothingMore() {
        val names = DrawKind.entries.associateBy { it.displayName }
        val violations = Violations("draws that are not what they are called")
        for (sample in samplesOf(ScenarioType.DRAW_EQUITY, ScenarioType.POT_ODDS)) {
            val m = sample.problem.metadata
            val kind = names.getValue(m.getValue("drawName"))
            val hole = cards(m.getValue("holeCards"))
            val board = cards(m.getValue("boardCards"))
            violations.check(bestCategory(hole + board) == HandCategory.HIGH_CARD, sample) {
                "$hole on $board is already ${bestCategory(hole + board)}"
            }
            val counted = outs(hole, board, HandCategory.STRAIGHT).size
            if (kind == DrawKind.OVERCARDS) {
                violations.check(hole.minOf { it.rank.value } > board.maxOf { it.rank.value }, sample) { "$hole not over $board" }
                violations.check(counted == 0, sample) { "overcards with $counted straight or flush outs besides" }
            } else {
                violations.check(counted == kind.outs, sample) { "${kind.displayName} with $counted outs: $hole on $board" }
            }
        }
        violations.assertNone()
    }

    @Test
    fun potOddsQuoteTheRealChanceOfHitting() {
        val names = DrawKind.entries.associateBy { it.displayName }
        val violations = Violations("win percentages that are not the chance of hitting")
        for (sample in samplesOf(ScenarioType.POT_ODDS)) {
            val m = sample.problem.metadata
            val outs = names.getValue(m.getValue("drawName")).outs
            val boardSize = m.getValue("boardCards").split(",").size
            val unseen = 52 - 2 - boardSize
            val miss = if (boardSize == 4) (unseen - outs).toDouble() / unseen
            else (unseen - outs).toDouble() / unseen * (unseen - outs - 1) / (unseen - 1)
            val expected = ((1 - miss) * 100).roundToInt()
            violations.check(m.getValue("winPercent").toInt() == expected, sample) { "expected $expected%" }
        }
        violations.assertNone()
    }
}
