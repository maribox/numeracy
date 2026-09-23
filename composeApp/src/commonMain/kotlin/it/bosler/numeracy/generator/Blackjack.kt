package it.bosler.numeracy.generator

import it.bosler.numeracy.model.Card
import it.bosler.numeracy.model.fullDeck
import kotlin.random.Random

/** Card names as blackjack reads them: suits do not matter, and ten, jack, queen and king are all ten. */
internal val BLACKJACK_RANKS = listOf("2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A")

/**
 * A six-deck shoe, dealt without replacement and shuffled again once three quarters of it is gone,
 * where casinos put the cut card. A hand from it can hold at most twenty-four of any card.
 */
internal class Shoe(private val rng: Random, private val decks: Int = 6) {
    private var cards = ArrayDeque<Card>()

    fun draw(): Card {
        if (cards.size <= decks * 52 / 4) shuffle()
        return cards.removeFirst()
    }

    private fun shuffle() {
        cards = ArrayDeque(List(decks) { fullDeck() }.flatten().shuffled(rng))
    }
}

/** A card as blackjack names it: suits play no part, and the rank reads as on the card. */
internal val Card.blackjackName: String get() = rank.symbol

/** A hand's value: hard counts every ace as one; soft when one ace can count eleven without busting. */
internal data class HandValue(val total: Int, val soft: Boolean)

internal fun valueOf(cards: List<String>): HandValue {
    val hard = cards.sumOf { if (it == "A") 1 else it.toIntOrNull() ?: 10 }
    val soft = "A" in cards && hard + 10 <= 21
    return HandValue(if (soft) hard + 10 else hard, soft)
}

/**
 * How a player acts on [cards] against the dealer showing [upcard], by basic strategy for a six-deck
 * game, simplified to the plays that decide how many cards a hand ends with.
 */
internal enum class Action { HIT, STAND, DOUBLE, SPLIT }

internal fun basicStrategy(cards: List<String>, upcard: String): Action {
    val dealer = if (upcard == "A") 11 else upcard.toIntOrNull() ?: 10
    val (total, soft) = valueOf(cards)
    if (cards.size == 2 && cards[0] == cards[1] && cards[0] in setOf("A", "8")) return Action.SPLIT
    if (total >= 21) return Action.STAND
    val firstDecision = cards.size == 2
    return when {
        soft -> when {
            total >= 19 -> Action.STAND
            total == 18 -> if (dealer >= 9) Action.HIT else Action.STAND
            else -> if (firstDecision && dealer in 5..6) Action.DOUBLE else Action.HIT
        }
        total >= 17 -> Action.STAND
        total >= 13 -> if (dealer <= 6) Action.STAND else Action.HIT
        total == 12 -> if (dealer in 4..6) Action.STAND else Action.HIT
        total == 11 -> if (firstDecision) Action.DOUBLE else Action.HIT
        total == 10 -> if (firstDecision && dealer <= 9) Action.DOUBLE else Action.HIT
        else -> Action.HIT
    }
}

/**
 * Plays [start] against [upcard] to the end of the hand, drawing with [draw]. A split keeps the first
 * card and plays it as its own hand, as the table would; split aces take one card each and stop.
 */
internal fun playHand(start: List<String>, upcard: String, draw: () -> String): List<String> {
    var cards = start
    while (true) {
        when (basicStrategy(cards, upcard)) {
            Action.STAND -> return cards
            Action.HIT -> cards = cards + draw()
            Action.DOUBLE -> return cards + draw()
            Action.SPLIT -> {
                val aces = cards[0] == "A"
                cards = listOf(cards[0], draw())
                if (aces) return cards
            }
        }
        if (valueOf(cards).total >= 21) return cards
    }
}
