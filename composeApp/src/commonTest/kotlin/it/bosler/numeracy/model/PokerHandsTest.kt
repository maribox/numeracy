package it.bosler.numeracy.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PokerHandsTest {

    /** "A♠ K♥ 10♦" as cards. */
    private fun cards(text: String): List<Card> = text.split(" ").map { token ->
        val suit = Suit.entries.first { it.symbol == token.takeLast(1) }
        val rank = Rank.entries.first { it.symbol == token.dropLast(1) }
        Card(rank, suit)
    }

    @Test
    fun everyCategoryIsRecognised() {
        assertEquals(HandCategory.HIGH_CARD, bestCategory(cards("A♠ K♥ 9♦ 5♣ 2♠")))
        assertEquals(HandCategory.PAIR, bestCategory(cards("A♠ A♥ 9♦ 5♣ 2♠")))
        assertEquals(HandCategory.TWO_PAIR, bestCategory(cards("A♠ A♥ 9♦ 9♣ 2♠")))
        assertEquals(HandCategory.THREE_OF_A_KIND, bestCategory(cards("A♠ A♥ A♦ 9♣ 2♠")))
        assertEquals(HandCategory.STRAIGHT, bestCategory(cards("5♠ 6♥ 7♦ 8♣ 9♠")))
        assertEquals(HandCategory.FLUSH, bestCategory(cards("A♠ J♠ 9♠ 5♠ 2♠")))
        assertEquals(HandCategory.FULL_HOUSE, bestCategory(cards("A♠ A♥ A♦ 9♣ 9♠")))
        assertEquals(HandCategory.FOUR_OF_A_KIND, bestCategory(cards("A♠ A♥ A♦ A♣ 9♠")))
        assertEquals(HandCategory.STRAIGHT_FLUSH, bestCategory(cards("5♠ 6♠ 7♠ 8♠ 9♠")))
    }

    @Test
    fun theAcePlaysHighAndLowButNeverAroundTheCorner() {
        assertTrue(hasStraight(listOf(14, 2, 3, 4, 5)))
        assertTrue(hasStraight(listOf(10, 11, 12, 13, 14)))
        assertFalse(hasStraight(listOf(12, 13, 14, 2, 3)))
    }

    @Test
    fun theBestFiveOfSevenCount() {
        assertEquals(HandCategory.FLUSH, bestCategory(cards("A♠ J♠ 9♠ 5♠ 2♠ K♥ K♦")))
        assertEquals(HandCategory.FULL_HOUSE, bestCategory(cards("9♠ 9♥ 9♦ 5♣ 5♠ 2♥ K♦")))
        // A flush and a straight in the same seven cards are not a straight flush unless the same five.
        assertEquals(HandCategory.FLUSH, bestCategory(cards("5♠ 6♠ 7♠ 8♥ 9♥ K♠ 2♠")))
    }

    @Test
    fun outsAreCountedFromTheCardsInSight() {
        // Four hearts: the nine hearts left make the flush.
        assertEquals(9, outs(cards("A♥ 7♥"), cards("K♥ 2♥ 9♣"), HandCategory.FLUSH).size)
        // 5-6-7-8, open at both ends: four nines and four fours.
        assertEquals(8, outs(cards("5♠ 6♥"), cards("7♦ 8♣ K♠"), HandCategory.STRAIGHT).size)
        // 8-9-_-J-Q: only the four tens.
        assertEquals(4, outs(cards("8♠ 9♥"), cards("J♦ Q♣ 2♠"), HandCategory.STRAIGHT).size)
        // Flush draw and gutshot together: nine hearts, three more tens.
        assertEquals(12, outs(cards("8♥ 9♥"), cards("J♥ Q♣ 2♥"), HandCategory.STRAIGHT).size)
    }
}
