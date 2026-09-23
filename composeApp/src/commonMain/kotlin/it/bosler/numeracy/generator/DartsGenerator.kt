package it.bosler.numeracy.generator

import it.bosler.numeracy.model.Problem
import it.bosler.numeracy.model.ScenarioType
import kotlin.random.Random

/**
 * A leg of 501, double out, played dart by dart the way a club player throws it: aiming at treble
 * twenty while the score is high, setting up a double near the end, and missing into the segments a
 * real dart misses into.
 *
 * Each question is one dart. Three darts make a visit, and the scoreboard shows the score at the start
 * of the visit. A dart that takes the score below zero, to one, or to zero on anything but a double is
 * a bust: the whole visit counts for nothing and the score goes back to where the visit began. The
 * leg is won by reaching zero on a double, the bull included, and the next leg starts at 501.
 */
class DartsGenerator(private val rng: Random = Random.Default) : ProblemGenerator {

    private var score = START
    private var visitStart = START
    private var dartInVisit = 1

    override fun generate(): Problem {
        if (score == 0) {
            score = START
            visitStart = START
            dartInVisit = 1
        }
        val dart = throwAt(aimFor(score))
        val before = score
        val scoreboard = visitStart
        val left = before - dart.value
        val bust = left < 0 || left == 1 || (left == 0 && !dart.isDouble)
        val after = if (bust) visitStart else left
        val dartNumber = dartInVisit

        score = after
        if (bust || dartInVisit == 3 || after == 0) {
            visitStart = after
            dartInVisit = 1
        } else {
            dartInVisit++
        }

        return Problem(
            scenarioType = ScenarioType.DARTS,
            questionText = "Score: $before\nThrow: ${dart.name}",
            correctAnswer = after.toString(),
            explanation = when {
                bust -> "$before − ${dart.value} leaves ${if (left < 0) "less than nothing" else if (left == 1) "1" else "0 without a double"}: bust, back to $after"
                after == 0 -> "$before − ${dart.value} = 0 on a double: game shot"
                else -> "$before − ${dart.value} = $after"
            },
            metadata = mapOf(
                "currentScore" to before.toString(),
                "throwName" to dart.name,
                "throwValue" to dart.value.toString(),
                "newScore" to after.toString(),
                "visitStart" to scoreboard.toString(),
                "dartInVisit" to dartNumber.toString(),
                "bust" to bust.toString(),
            ),
        )
    }

    private fun aimFor(score: Int): Target = when {
        score == 50 -> Target.Bull
        score in 2..40 && score % 2 == 0 -> Target.Segment(score / 2, 2)
        score <= 60 -> {
            // Leave a double: 32 and 40 first, the doubles a player practises most.
            val single = listOf(32, 40, 16, 24, 8, 4, 2).map { score - it }.firstOrNull { it in 1..20 }
            if (single != null) Target.Segment(single, 1) else Target.Segment(minOf(score - 2, 20).coerceAtLeast(1), 1)
        }
        score - 60 >= 2 -> Target.Segment(20, 3)
        else -> Target.Segment(19, 3)
    }

    /** Where a dart aimed at [target] lands. */
    private fun throwAt(target: Target): Dart {
        val roll = rng.nextDouble()
        return when (target) {
            is Target.Bull -> when {
                roll < 0.25 -> Dart("Bull", 50, isDouble = true)
                roll < 0.60 -> Dart("Single Bull", 25, isDouble = false)
                else -> single(BOARD.random(rng))
            }
            is Target.Segment -> when (target.ring) {
                3 -> when {
                    roll < 0.25 -> treble(target.number)
                    roll < 0.80 -> single(target.number)
                    roll < 0.87 -> treble(neighbour(target.number))
                    else -> single(neighbour(target.number))
                }
                2 -> when {
                    roll < 0.35 -> double(target.number)
                    roll < 0.65 -> single(target.number)
                    roll < 0.85 -> Dart("Miss", 0, isDouble = false)
                    else -> double(neighbour(target.number))
                }
                else -> when {
                    roll < 0.78 -> single(target.number)
                    roll < 0.93 -> single(neighbour(target.number))
                    roll < 0.97 -> treble(target.number)
                    else -> double(target.number)
                }
            }
        }
    }

    private fun neighbour(number: Int): Int {
        val at = BOARD.indexOf(number)
        return BOARD[(at + if (rng.nextBoolean()) 1 else BOARD.size - 1) % BOARD.size]
    }

    private fun single(n: Int) = Dart("Single $n", n, isDouble = false)
    private fun double(n: Int) = Dart("Double $n", 2 * n, isDouble = true)
    private fun treble(n: Int) = Dart("Triple $n", 3 * n, isDouble = false)

    private data class Dart(val name: String, val value: Int, val isDouble: Boolean)

    private sealed interface Target {
        data object Bull : Target
        data class Segment(val number: Int, val ring: Int) : Target
    }

    private companion object {
        const val START = 501

        /** The numbers round a dartboard, clockwise from the top. */
        val BOARD = listOf(20, 1, 18, 4, 13, 6, 10, 15, 2, 17, 3, 19, 7, 16, 8, 11, 14, 9, 12, 5)
    }
}
