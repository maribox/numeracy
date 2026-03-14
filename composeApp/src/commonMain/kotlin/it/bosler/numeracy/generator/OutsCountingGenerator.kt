package it.bosler.numeracy.generator

import it.bosler.numeracy.model.Card
import it.bosler.numeracy.model.Problem
import it.bosler.numeracy.model.Rank
import it.bosler.numeracy.model.ScenarioType
import it.bosler.numeracy.model.Suit
import it.bosler.numeracy.model.fullDeck
import kotlin.random.Random

/**
 * Generates "counting outs" problems with actual dealt cards.
 * The user sees their hole cards and the board, identifies the draw, and counts outs.
 */
class OutsCountingGenerator : ProblemGenerator {

    override fun generate(): Problem {
        val generators = listOf(
            ::flushDraw,
            ::openEndedStraight,
            ::gutshot,
            ::twoOvercards,
            ::flushDrawPlusGutshot,
            ::pocketPairToSet,
            ::doubleGutshot,
        )
        return generators[Random.nextInt(generators.size)]()
    }

    private fun flushDraw(): Problem {
        val suit = Suit.entries[Random.nextInt(4)]
        val otherSuit = Suit.entries.filter { it != suit }[Random.nextInt(3)]
        val ranks = Rank.entries.shuffled()
        // 2 hole cards same suit, 2 board cards same suit = 4 suited, need 1 more
        val hole = listOf(Card(ranks[0], suit), Card(ranks[1], suit))
        val board = listOf(
            Card(ranks[2], suit),
            Card(ranks[3], suit),
            Card(ranks[4], otherSuit),
        )
        return buildProblem(
            hole, board,
            drawName = "Flush draw",
            outs = 9,
            explanation = "13 cards of ${suit.symbol} minus 4 visible = 9 outs",
        )
    }

    private fun openEndedStraight(): Problem {
        // Pick 4 consecutive ranks that can be open-ended (3-6 through J-A would work, but A-high can't be open)
        val startIdx = Random.nextInt(1, 10) // index 1=THREE up to 9=JACK gives 4 consecutive with room both sides
        val straightRanks = (startIdx..startIdx + 3).map { Rank.entries[it] }
        val suits = Suit.entries.shuffled()
        // Put 2 in hole, 2 on board
        val hole = listOf(Card(straightRanks[0], suits[0]), Card(straightRanks[1], suits[1]))
        val boardStraight = listOf(Card(straightRanks[2], suits[2]), Card(straightRanks[3], suits[3]))
        // Add a random non-interfering board card
        val usedRanks = straightRanks.toSet() + setOf(Rank.entries.getOrNull(startIdx - 1), Rank.entries.getOrNull(startIdx + 4))
        val fillerRank = Rank.entries.filter { it !in usedRanks }.random()
        val board = boardStraight + Card(fillerRank, suits[0])

        return buildProblem(
            hole, board,
            drawName = "Open-ended straight draw",
            outs = 8,
            explanation = "Need a ${Rank.entries[startIdx - 1].symbol} or ${Rank.entries[startIdx + 4].symbol}, 4 of each = 8 outs",
        )
    }

    private fun gutshot(): Problem {
        // 4 cards with a gap, e.g. 5-6-_-8-9, need the 7
        val startIdx = Random.nextInt(1, 9)
        val gapPos = Random.nextInt(1, 3) // gap at position 1 or 2 in the sequence
        val straightRanks = (0..3).map { i ->
            val offset = if (i >= gapPos) i + 1 else i
            Rank.entries[startIdx + offset]
        }
        val neededRank = Rank.entries[startIdx + gapPos]
        val suits = Suit.entries.shuffled()
        val hole = listOf(Card(straightRanks[0], suits[0]), Card(straightRanks[1], suits[1]))
        val boardStraight = listOf(Card(straightRanks[2], suits[2]), Card(straightRanks[3], suits[3]))
        val fillerRank = Rank.entries.filter { it !in straightRanks.map { r -> r } + neededRank }.random()
        val board = boardStraight + Card(fillerRank, suits[0])

        return buildProblem(
            hole, board,
            drawName = "Gutshot straight draw",
            outs = 4,
            explanation = "Need a ${neededRank.symbol} to complete the straight = 4 outs",
        )
    }

    private fun twoOvercards(): Problem {
        // Hole: two high cards, board: all lower
        val highRanks = listOf(Rank.ACE, Rank.KING, Rank.QUEEN, Rank.JACK).shuffled()
        val hole = listOf(
            Card(highRanks[0], Suit.entries.random()),
            Card(highRanks[1], Suit.entries.random()),
        )
        val lowRanks = Rank.entries.filter { it.value < highRanks[1].value }.shuffled()
        val suits = Suit.entries.shuffled()
        val board = listOf(
            Card(lowRanks[0], suits[0]),
            Card(lowRanks[1], suits[1]),
            Card(lowRanks[2], suits[2]),
        )

        return buildProblem(
            hole, board,
            drawName = "Two overcards",
            outs = 6,
            explanation = "3 ${highRanks[0].symbol}s + 3 ${highRanks[1].symbol}s = 6 outs to make top pair",
        )
    }

    private fun flushDrawPlusGutshot(): Problem {
        val suit = Suit.entries[Random.nextInt(4)]
        val otherSuit = Suit.entries.filter { it != suit }[Random.nextInt(3)]
        // Make a gutshot within the flush draw cards
        val startIdx = Random.nextInt(1, 9)
        val gapPos = Random.nextInt(1, 3)
        val straightRanks = (0..3).map { i ->
            val offset = if (i >= gapPos) i + 1 else i
            Rank.entries[startIdx + offset]
        }
        val neededRank = Rank.entries[startIdx + gapPos]

        // 2 hole suited + 2 board suited = flush draw, and 4 of these form gutshot
        val hole = listOf(Card(straightRanks[0], suit), Card(straightRanks[1], suit))
        val board = listOf(
            Card(straightRanks[2], suit),
            Card(straightRanks[3], suit),
            Card(Rank.entries.filter { it !in straightRanks + neededRank }.random(), otherSuit),
        )

        return buildProblem(
            hole, board,
            drawName = "Flush draw + gutshot",
            outs = 12,
            explanation = "9 flush outs + 4 gutshot (${neededRank.symbol}) − 1 overlap = 12 outs",
        )
    }

    private fun pocketPairToSet(): Problem {
        val rank = Rank.entries[Random.nextInt(Rank.entries.size)]
        val pairSuits = Suit.entries.shuffled().take(2)
        val hole = listOf(Card(rank, pairSuits[0]), Card(rank, pairSuits[1]))
        val boardRanks = Rank.entries.filter { it != rank }.shuffled().take(3)
        val board = boardRanks.mapIndexed { i, r -> Card(r, Suit.entries[i % 4]) }

        return buildProblem(
            hole, board,
            drawName = "Pocket pair → set",
            outs = 2,
            explanation = "2 remaining ${rank.symbol}s in the deck = 2 outs",
        )
    }

    private fun doubleGutshot(): Problem {
        // e.g. holding 6,9 with board 5,7,8 → need 4 or T for straight
        // Or 5-_-7-8-_-T pattern
        val startIdx = Random.nextInt(1, 7) // enough room for 5 consecutive with 2 gaps
        // Cards: startIdx, startIdx+2, startIdx+3 form the middle, need startIdx+1 or startIdx+4
        val cardRanks = listOf(
            Rank.entries[startIdx],
            Rank.entries[startIdx + 2],
            Rank.entries[startIdx + 3],
            Rank.entries[startIdx + 5],
        )
        val needed1 = Rank.entries[startIdx + 1]
        val needed2 = Rank.entries[startIdx + 4]
        val suits = Suit.entries.shuffled()
        val hole = listOf(Card(cardRanks[0], suits[0]), Card(cardRanks[3], suits[1]))
        val board = listOf(
            Card(cardRanks[1], suits[2]),
            Card(cardRanks[2], suits[3]),
            Card(Rank.entries.filter { it !in cardRanks + needed1 + needed2 }.random(), suits[0]),
        )

        return buildProblem(
            hole, board,
            drawName = "Double gutshot",
            outs = 8,
            explanation = "Need ${needed1.symbol} or ${needed2.symbol}, 4 of each = 8 outs",
        )
    }

    private fun buildProblem(
        hole: List<Card>,
        board: List<Card>,
        drawName: String,
        outs: Int,
        explanation: String,
    ): Problem {
        val holeStr = hole.joinToString(",") { it.display }
        val boardStr = board.joinToString(",") { it.display }

        return Problem(
            scenarioType = ScenarioType.OUTS_COUNTING,
            questionText = "How many outs do you have?",
            correctAnswer = outs.toString(),
            explanation = explanation,
            metadata = mapOf(
                "drawName" to drawName,
                "outs" to outs.toString(),
                "holeCards" to holeStr,
                "boardCards" to boardStr,
            ),
        )
    }
}
