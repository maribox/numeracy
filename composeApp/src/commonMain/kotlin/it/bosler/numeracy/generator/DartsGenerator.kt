package it.bosler.numeracy.generator

import it.bosler.numeracy.model.Problem
import it.bosler.numeracy.model.ScenarioType
import kotlin.random.Random

class DartsGenerator(private val rng: Random = Random.Default) : ProblemGenerator {

    private var currentScore = 501

    override fun generate(): Problem {
        if (currentScore <= 0) {
            currentScore = 501
        }

        val (throwName, throwValue) = throwWithin(currentScore)
        val previousScore = currentScore
        currentScore = previousScore - throwValue

        return Problem(
            scenarioType = ScenarioType.DARTS,
            questionText = "Score: $previousScore\nThrow: $throwName",
            correctAnswer = currentScore.toString(),
            explanation = "$previousScore \u2212 $throwValue = $currentScore",
            metadata = mapOf(
                "currentScore" to previousScore.toString(),
                "throwName" to throwName,
                "throwValue" to throwValue.toString(),
                "newScore" to currentScore.toString(),
            ),
        )
    }

    /**
     * A throw that does not take the score below zero. A dart worth more than what is left is a bust
     * in the game and an answer the keypad cannot type here, so the leg is played out instead: throws
     * are drawn until one fits, and a score too small for any of them is finished with a single.
     */
    private fun throwWithin(score: Int): Pair<String, Int> {
        repeat(FITTING_ATTEMPTS) {
            val attempt = randomThrow()
            if (attempt.second <= score) return attempt
        }
        return if (score in 1..20) "Single $score" to score else "Single 1" to 1
    }

    private fun randomThrow(): Pair<String, Int> {
        // Realistic pub darts distribution:
        // ~50% singles, ~20% doubles, ~8% triples, ~15% miss (still single low), ~5% bull area, ~2% miss board
        val roll = rng.nextInt(100)
        return when {
            // Bullseye (rare)
            roll < 2 -> "Bull" to 50
            // Single Bull
            roll < 5 -> "Single Bull" to 25
            // Triple (uncommon for casual player)
            roll < 13 -> {
                // Favor the common treble targets: 20, 19, 18
                val n = weightedDartNumber()
                "Triple $n" to n * 3
            }
            // Double
            roll < 30 -> {
                val n = weightedDartNumber()
                "Double $n" to n * 2
            }
            // Single (most common)
            else -> {
                val n = weightedDartNumber()
                "Single $n" to n
            }
        }
    }

    /** Weight toward commonly hit numbers on the board */
    private fun weightedDartNumber(): Int {
        // Players tend to aim at 20/19/18 area, adjacent numbers get hit often
        // 20 area: 20, 1, 5 (adjacent on board)
        // 19 area: 19, 7, 3
        val weights = mapOf(
            20 to 15, 1 to 10, 5 to 10,
            19 to 12, 7 to 8, 3 to 8,
            18 to 8, 4 to 6, 13 to 6,
            17 to 5, 2 to 5, 15 to 5,
            16 to 4, 8 to 4, 11 to 4,
            14 to 4, 9 to 4, 12 to 4,
            6 to 3, 10 to 3,
        )
        val total = weights.values.sum()
        var pick = rng.nextInt(total)
        for ((number, weight) in weights) {
            pick -= weight
            if (pick < 0) return number
        }
        return 20
    }

    private companion object {
        /** Enough that a fitting throw is found whenever one is likely; the fallback covers the rest. */
        const val FITTING_ATTEMPTS = 24
    }
}
