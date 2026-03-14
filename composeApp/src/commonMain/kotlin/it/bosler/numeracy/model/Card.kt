package it.bosler.numeracy.model

enum class Suit(val symbol: String, val isRed: Boolean) {
    SPADES("\u2660", false),
    HEARTS("\u2665", true),
    DIAMONDS("\u2666", true),
    CLUBS("\u2663", false),
}

enum class Rank(val symbol: String, val value: Int) {
    TWO("2", 2), THREE("3", 3), FOUR("4", 4), FIVE("5", 5),
    SIX("6", 6), SEVEN("7", 7), EIGHT("8", 8), NINE("9", 9),
    TEN("10", 10), JACK("J", 11), QUEEN("Q", 12), KING("K", 13), ACE("A", 14);
}

data class Card(val rank: Rank, val suit: Suit) {
    val display: String get() = "${rank.symbol}${suit.symbol}"
}

fun fullDeck(): List<Card> = Rank.entries.flatMap { rank -> Suit.entries.map { suit -> Card(rank, suit) } }
