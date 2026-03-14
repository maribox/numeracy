package it.bosler.numeracy.generator

import it.bosler.numeracy.model.Problem
import it.bosler.numeracy.model.ScenarioType
import kotlin.random.Random

class DartsGenerator : ProblemGenerator {

    private var currentScore = 501

    override fun generate(): Problem {
        if (currentScore <= 0) {
            currentScore = 501
        }

        val throwResult = randomThrow()
        val throwName = throwResult.first
        val throwValue = throwResult.second
        val previousScore = currentScore
        currentScore = previousScore - throwValue

        val (hintEasy, hintMedium, hintHard) = buildHints(throwName, throwValue, previousScore)

        return Problem(
            scenarioType = ScenarioType.DARTS,
            questionText = "Score: $previousScore\nThrow: $throwName",
            correctAnswer = currentScore.toString(),
            explanation = "$previousScore - $throwValue = $currentScore",
            metadata = mapOf(
                "currentScore" to previousScore.toString(),
                "throwName" to throwName,
                "throwValue" to throwValue.toString(),
                "newScore" to currentScore.toString(),
                "hintEasy" to hintEasy,
                "hintMedium" to hintMedium,
                "hintHard" to hintHard,
                "tip" to buildTip(),
            ),
        )
    }

    private fun buildHints(throwName: String, throwValue: Int, score: Int): Triple<String, String, String> {
        val result = score - throwValue

        // === LEARNING (hintEasy): Full step-by-step walkthrough ===
        val hintEasy = buildString {
            // First explain the throw value if multiplier
            when {
                throwName.startsWith("Triple") -> {
                    val n = throwValue / 3
                    append("Triple $n → $n \u00D7 3 = $throwValue\n")
                }
                throwName.startsWith("Double") -> {
                    val n = throwValue / 2
                    append("Double $n → $n \u00D7 2 = $throwValue\n")
                }
            }

            // Now do digit-by-digit subtraction walkthrough
            append("$score \u2212 $throwValue:\n")

            val scoreOnes = score % 10
            val scoreTens = (score / 10) % 10
            val scoreHundreds = score / 100
            val throwOnes = throwValue % 10
            val throwTens = (throwValue / 10) % 10

            if (throwOnes == 0 && throwTens == 0) {
                append("  Just subtract $throwValue from hundreds: $scoreHundreds \u2212 0 = $scoreHundreds → $result")
            } else if (throwOnes <= scoreOnes) {
                // No borrow needed in ones
                val newOnes = scoreOnes - throwOnes
                if (throwTens <= scoreTens) {
                    val newTens = scoreTens - throwTens
                    append("  Ones: $scoreOnes \u2212 $throwOnes = $newOnes\n")
                    if (throwTens > 0) append("  Tens: $scoreTens \u2212 $throwTens = $newTens\n")
                    append("  → $result")
                } else {
                    // Borrow in tens from hundreds
                    val newTens = scoreTens + 10 - throwTens
                    val newHundreds = scoreHundreds - 1
                    append("  Ones: $scoreOnes \u2212 $throwOnes = $newOnes\n")
                    append("  Tens: $scoreTens < $throwTens → borrow: ${scoreTens + 10} \u2212 $throwTens = $newTens, carry 1\n")
                    append("  Hundreds: $scoreHundreds \u2212 1 = $newHundreds\n")
                    append("  → $result")
                }
            } else {
                // Borrow needed in ones
                val complement = 10 - throwOnes
                val newOnes = scoreOnes + complement
                val onesDigit = newOnes % 10
                val effectiveTens = scoreTens - 1 // after borrow
                append("  Ones: $scoreOnes < $throwOnes → complement: 10 \u2212 $throwOnes = $complement, then $complement + $scoreOnes = $newOnes (write $onesDigit, carry 1)\n")
                if (throwTens > 0) {
                    val totalTensSub = throwTens + 1 // +1 for borrow
                    if (totalTensSub <= scoreTens) {
                        val newTens = scoreTens - totalTensSub
                        append("  Tens: $scoreTens \u2212 $throwTens \u2212 1(carry) = $newTens\n")
                    } else {
                        val newTens = scoreTens + 10 - totalTensSub
                        val newHundreds = scoreHundreds - 1
                        append("  Tens: $scoreTens < ${throwTens}+1 → borrow: ${scoreTens + 10} \u2212 $totalTensSub = $newTens, carry 1\n")
                        append("  Hundreds: $scoreHundreds \u2212 1 = $newHundreds\n")
                    }
                } else {
                    if (effectiveTens >= 0) {
                        append("  Tens: $scoreTens \u2212 1(carry) = $effectiveTens\n")
                    } else {
                        append("  Tens: $scoreTens \u2212 1(carry) → borrow from hundreds\n")
                        append("  Hundreds: $scoreHundreds \u2212 1 = ${scoreHundreds - 1}\n")
                    }
                }
                append("  → $result")
            }
        }

        // === PRACTICE (hintMedium): Strategy guidance without the answer ===
        val hintMedium = buildString {
            val scoreOnes = score % 10
            val throwOnes = throwValue % 10

            if (throwValue >= 40) {
                // Round-and-adjust strategy
                val roundUp = ((throwValue + 9) / 10) * 10
                val adjustment = roundUp - throwValue
                append("Round up: subtract $roundUp, add $adjustment back.\n")
            }

            if (throwOnes > scoreOnes && throwOnes != 0) {
                val complement = 10 - throwOnes
                append("Ones digit: $scoreOnes < $throwOnes → need to borrow.\n")
                append("Complement of $throwOnes is $complement (10\u2212$throwOnes).\n")
                append("New ones = $complement + $scoreOnes. Remember to carry 1 to tens.\n")
                // Teach which ranges need borrows
                if (throwOnes >= 6) {
                    append("Tip: subtracting 6-9 almost always borrows (unless ones digit is big).")
                }
            } else if (throwOnes != 0) {
                append("Ones: $scoreOnes \u2212 $throwOnes, no borrow needed.\n")
                append("Then handle tens column normally.")
            } else {
                append("Ends in 0, just subtract the tens and hundreds directly.")
            }
        }

        return Triple(hintEasy, hintMedium, "")
    }

    private fun buildTip(): String =
        "Darts Scoring Tips:\n" +
        "• Triple 20 = 60 pts. Three treble-20s = 180 (max). Always aim T20 until ≤ 170.\n" +
        "• Work in multiples of 60: 501 → 441 → 381 → 321 → 261 → 201 → 141 → 81 → checkout.\n" +
        "• Learn key finishes by heart: 170=T20 T20 Bull, 167=T20 T19 Bull, 160=T20 T20 D20, 121=T20 S11 D25.\n" +
        "• For scores 41–170, think 'two darts to leave a double': e.g. 81 → T19 D12, or 100 → T20 D20.\n" +
        "• Bogey numbers cannot be finished in 3 darts: 169, 168, 166, 165, 163, 162, 159. Know these to avoid them.\n" +
        "• Complement trick: to subtract e.g. 57 from 183, think '183 − 60 = 123, + 3 = 126' (round then adjust)."

    private fun randomThrow(): Pair<String, Int> {
        // Realistic pub darts distribution:
        // ~50% singles, ~20% doubles, ~8% triples, ~15% miss (still single low), ~5% bull area, ~2% miss board
        val roll = Random.nextInt(100)
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
        var pick = Random.nextInt(total)
        for ((number, weight) in weights) {
            pick -= weight
            if (pick < 0) return number
        }
        return 20
    }
}
