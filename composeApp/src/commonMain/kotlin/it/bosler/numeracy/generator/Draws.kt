package it.bosler.numeracy.generator

import it.bosler.numeracy.model.Card
import it.bosler.numeracy.model.HandCategory
import it.bosler.numeracy.model.Rank
import it.bosler.numeracy.model.Suit
import it.bosler.numeracy.model.bestCategory
import it.bosler.numeracy.model.fullDeck
import it.bosler.numeracy.model.outs
import kotlin.random.Random

/** The drawing hands the poker scenarios deal, with the outs each has when it is dealt cleanly. */
internal enum class DrawKind(val displayName: String, val outs: Int, val explanation: String) {
    FLUSH("Flush draw", 9, "9 remaining cards of your suit"),
    OPEN_ENDED("Open-ended straight draw", 8, "4 cards of two different ranks complete the straight"),
    GUTSHOT("Gutshot straight draw", 4, "Only 4 cards of one rank fill the gap"),
    OVERCARDS("Two overcards", 6, "3 aces + 3 kings pair up above the board"),
    FLUSH_AND_GUTSHOT("Flush draw + gutshot", 12, "9 flush outs + 4 straight outs − 1 overlap"),
}

/** A hand and a board on the flop (three cards) or the turn (four). */
internal data class DrawDeal(val kind: DrawKind, val hole: List<Card>, val board: List<Card>) {
    /** Cards neither in the hand nor on the board: 47 on the flop, 46 on the turn. */
    val unseen: Int get() = 52 - hole.size - board.size

    /**
     * The chance, in percent, that the draw comes in by the river: one card to come on the turn, two on
     * the flop. This is the number the rule of 2 and 4 approximates.
     */
    val hitPercent: Double
        get() = if (board.size == 4) 100.0 * kind.outs / unseen
        else 100.0 * (1 - (unseen - kind.outs).toDouble() * (unseen - kind.outs - 1) / (unseen.toDouble() * (unseen - 1)))
}

/**
 * Deals [kind] the way it arises at a real table: a hand that is nothing yet, on a board that does not
 * pair it, with exactly the outs the draw is named for. Cards are laid out for the draw and the rest
 * filled at random, and a deal whose counted outs disagree with its name is dealt again, so "flush
 * draw" never sits on a board that already made the flush or that also gives a straight draw.
 */
internal fun dealDraw(kind: DrawKind, onFlop: Boolean, rng: Random): DrawDeal {
    val boardSize = if (onFlop) 3 else 4
    repeat(MAX_DEALS) {
        val (hole, laidOut) = layOut(kind, rng)
        val rest = fullDeck().filter { it !in hole && it !in laidOut }.shuffled(rng)
        val board = (laidOut + rest.take(boardSize - laidOut.size)).shuffled(rng)
        val deal = DrawDeal(kind, hole, board)
        if (isClean(deal)) return deal
    }
    error("no clean ${kind.displayName} in $MAX_DEALS deals")
}

/** Whether [deal] is its draw and nothing more, counted card by card. */
internal fun isClean(deal: DrawDeal): Boolean {
    val (kind, hole, board) = deal
    if (bestCategory(hole + board) != HandCategory.HIGH_CARD) return false
    val straightOrBetter = outs(hole, board, HandCategory.STRAIGHT).size
    return when (kind) {
        DrawKind.OVERCARDS -> {
            val over = hole.minOf { it.rank.value } > board.maxOf { it.rank.value }
            over && straightOrBetter == 0
        }
        else -> straightOrBetter == kind.outs
    }
}

/** The hole cards and the board cards that make the draw; the rest of the board is dealt at random. */
private fun layOut(kind: DrawKind, rng: Random): Pair<List<Card>, List<Card>> {
    fun card(value: Int, suit: Suit) = Card(Rank.entries.first { it.value == value }, suit)
    fun anySuit() = Suit.entries.random(rng)
    return when (kind) {
        DrawKind.FLUSH -> {
            val suit = anySuit()
            val ranks = (2..14).shuffled(rng).take(4)
            ranks.take(2).map { card(it, suit) } to ranks.drop(2).map { card(it, suit) }
        }
        DrawKind.OPEN_ENDED -> {
            // Four in a row with room at both ends: from 2-3-4-5 (ace or six) up to 10-J-Q-K (nine or ace).
            val low = rng.nextInt(2, 11)
            val ranks = (low until low + 4).shuffled(rng)
            ranks.take(2).map { card(it, anySuit()) } to ranks.drop(2).map { card(it, anySuit()) }
        }
        DrawKind.GUTSHOT -> {
            val low = rng.nextInt(2, 11)
            val ranks = listOf(low, low + 1, low + 3, low + 4).shuffled(rng)
            ranks.take(2).map { card(it, anySuit()) } to ranks.drop(2).map { card(it, anySuit()) }
        }
        DrawKind.FLUSH_AND_GUTSHOT -> {
            val suit = anySuit()
            val low = rng.nextInt(2, 11)
            val ranks = listOf(low, low + 1, low + 3, low + 4).shuffled(rng)
            ranks.take(2).map { card(it, suit) } to ranks.drop(2).map { card(it, suit) }
        }
        DrawKind.OVERCARDS -> {
            val (aceSuit, kingSuit) = Suit.entries.shuffled(rng).take(2)
            listOf(card(14, aceSuit), card(13, kingSuit)) to emptyList()
        }
    }
}

/** Enough that a clean deal is found every time; the tests deal thousands without reaching it. */
private const val MAX_DEALS = 2_000
