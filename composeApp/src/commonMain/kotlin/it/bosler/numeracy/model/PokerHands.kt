package it.bosler.numeracy.model

/** Poker hand categories, weakest first, as Texas hold'em ranks them. */
enum class HandCategory {
    HIGH_CARD, PAIR, TWO_PAIR, THREE_OF_A_KIND, STRAIGHT, FLUSH, FULL_HOUSE, FOUR_OF_A_KIND, STRAIGHT_FLUSH,
}

/** The best category any five of [cards] make; hold'em plays the best five of seven. */
fun bestCategory(cards: List<Card>): HandCategory {
    require(cards.size in 5..7) { "a hand is five to seven cards, not ${cards.size}" }
    val bySuit = cards.groupBy { it.suit }
    val flushSuit = bySuit.values.firstOrNull { it.size >= 5 }
    if (flushSuit != null && hasStraight(flushSuit.map { it.rank.value })) return HandCategory.STRAIGHT_FLUSH

    val counts = cards.groupingBy { it.rank }.eachCount().values.sortedDescending()
    return when {
        counts[0] == 4 -> HandCategory.FOUR_OF_A_KIND
        counts[0] == 3 && counts.getOrElse(1) { 0 } >= 2 -> HandCategory.FULL_HOUSE
        flushSuit != null -> HandCategory.FLUSH
        hasStraight(cards.map { it.rank.value }) -> HandCategory.STRAIGHT
        counts[0] == 3 -> HandCategory.THREE_OF_A_KIND
        counts[0] == 2 && counts.getOrElse(1) { 0 } == 2 -> HandCategory.TWO_PAIR
        counts[0] == 2 -> HandCategory.PAIR
        else -> HandCategory.HIGH_CARD
    }
}

/** Five consecutive ranks among [values]; the ace also plays low, below the two. */
fun hasStraight(values: List<Int>): Boolean {
    val ranks = values.toSet() + if (Rank.ACE.value in values) setOf(1) else emptySet()
    return (1..10).any { low -> (low until low + 5).all { it in ranks } }
}

/**
 * The cards still unseen that would take [hole] and [board] to [target] or better: the outs, counted
 * the way a player counts them, against the cards they can see.
 */
fun outs(hole: List<Card>, board: List<Card>, target: HandCategory): List<Card> {
    val seen = (hole + board).toSet()
    return fullDeck().filter { it !in seen && bestCategory(hole + board + it) >= target }
}
