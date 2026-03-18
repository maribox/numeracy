package it.bosler.numeracy.generator

import it.bosler.numeracy.model.InputType
import it.bosler.numeracy.model.Problem
import it.bosler.numeracy.model.ScenarioType
import kotlin.random.Random

class SquaringGenerator : ProblemGenerator {
    override fun generate(): Problem {
        val number = Random.nextInt(10, 100)
        val answer = number.toLong() * number.toLong()

        val tricks = buildApplicableTricks(number, answer)
        val selectedTrick = tricks.random()

        return Problem(
            scenarioType = ScenarioType.SQUARING,
            questionText = "$number\u00B2",
            correctAnswer = answer.toString(),
            inputType = InputType.NUMBER,
            explanation = "$number \u00D7 $number = $answer",
            metadata = buildMap {
                put("number", number.toString())
                put("answer", answer.toString())
                put("trick", selectedTrick.hint)
                put("trickName", selectedTrick.name)
                selectedTrick.steps.forEachIndexed { i, step ->
                    put("step${i + 1}", step)
                }
                put("trickCount", tricks.size.toString())
                tricks.forEachIndexed { i, trick ->
                    put("trickName$i", trick.name)
                    put("trickHint$i", trick.hint)
                    trick.steps.forEachIndexed { j, step ->
                        put("trick${i}_step${j + 1}", step)
                    }
                    put("trick${i}_stepCount", trick.steps.size.toString())
                }
            },
        )
    }

    private fun buildApplicableTricks(n: Int, answer: Long): List<MathTrick> {
        val tricks = mutableListOf<MathTrick>()

        val tens = (n / 10) * 10
        val ones = n % 10

        // === 1. Multiple of 10 ===
        // 90² = (9 × 10)² = 9² × 100
        if (ones == 0 && tens > 0) {
            val small = n / 10
            val smallSq = small * small
            tricks.add(
                MathTrick(
                    name = "Factor out the zeros",
                    hint = "(k \u00D7 10)\u00B2 = k\u00B2 \u00D7 100",
                    steps = listOf(
                        "Formula: (k \u00D7 10)\u00B2 = k\u00B2 \u00D7 100",
                        "$n = #a{$small} \u00D7 10",
                        "#a{$small}\u00B2 = #a{$smallSq}",
                        "#a{$smallSq} \u00D7 100 = #r{$answer}",
                    ),
                    priority = 100,
                )
            )
        }

        // === 2. Ends in 5 ===
        // 65² → 6 × 7 = 42, append 25 → 4225
        if (ones == 5) {
            val k = n / 10
            val product = k * (k + 1)
            tricks.add(
                MathTrick(
                    name = "Ends-in-5 shortcut",
                    hint = "n5\u00B2 = n \u00D7 (n + 1), then append 25",
                    steps = listOf(
                        "Formula: n5\u00B2 = n \u00D7 (n + 1) | 25",
                        "Tens digit n = #a{$k}",
                        "#a{$k} \u00D7 #b{${k + 1}} = #c{$product}",
                        "Append 25: #c{$product}|25 = #r{${product}25}",
                    ),
                    priority = 100,
                )
            )
        }

        // === 3. Round to nearest ten ===
        // 33² = (33-3)(33+3) + 3² = 30×36 + 9
        if (ones != 0) {
            val d = if (ones <= 5) ones else 10 - ones
            val r = if (ones <= 5) n - ones else n + (10 - ones)
            val partner = 2 * n - r
            val diffProduct = r.toLong() * partner.toLong()
            val dSquared = d.toLong() * d.toLong()
            val roundPriority = when (d) {
                1 -> 80; 2 -> 70; 3 -> 60; else -> 40
            }
            tricks.add(
                MathTrick(
                    name = "Round to nearest ten",
                    hint = "n\u00B2 = (n \u2212 d)(n + d) + d\u00B2",
                    steps = listOf(
                        "Formula: n\u00B2 = (n \u2212 d)(n + d) + d\u00B2",
                        "$n is #b{$d} away from #a{$r}",
                        "So $n\u00B2 = #a{$r} \u00D7 #a{$partner} + #b{$d}\u00B2",
                        "#a{$r} \u00D7 #a{$partner} = #c{$diffProduct}",
                        "#b{$d}\u00B2 = #b{$dSquared}",
                        "#c{$diffProduct} + #b{$dSquared} = #r{$answer}",
                    ),
                    priority = roundPriority,
                )
            )
        }

        // === 4. Adjust from a known square ===
        // 51² = 50² + (51-50)(51+50) = 2500 + 101
        val landmarks = listOf(10, 20, 25, 30, 40, 50, 60, 70, 75, 80, 90, 100)
        for (m in landmarks) {
            val dist = kotlin.math.abs(n - m)
            if (dist in 1..3 && m != n) {
                val mSq = m.toLong() * m.toLong()
                val diff = n - m
                val adjustment = diff.toLong() * (n.toLong() + m.toLong())
                val absDiff = kotlin.math.abs(diff)
                val absAdj = kotlin.math.abs(adjustment)
                val sign = if (adjustment >= 0) "+" else "\u2212"
                val priority = when (dist) { 1 -> 75; 2 -> 65; else -> 55 }
                tricks.add(
                    MathTrick(
                        name = "Adjust from $m\u00B2",
                        hint = "n\u00B2 = m\u00B2 + (n \u2212 m)(n + m)",
                        steps = listOf(
                            "Formula: n\u00B2 = m\u00B2 + (n \u2212 m)(n + m)",
                            "You know #a{$m}\u00B2 = #a{$mSq}",
                            "$n \u2212 $m = $diff, and $n + $m = ${n + m}",
                            "$absDiff \u00D7 ${n + m} = #b{$absAdj}",
                            "#a{$mSq} $sign #b{$absAdj} = #r{$answer}",
                        ),
                        priority = priority,
                    )
                )
                break
            }
        }

        // === 5. Near-50 trick ===
        // 53² = (53-25)×100 + 3² = 2800 + 9
        if (n in 30..70 && ones != 0) {
            val absD = kotlin.math.abs(50 - n)
            val hundreds = (n - 25) * 100
            val tail = absD.toLong() * absD.toLong()
            if (hundreds + tail == answer) {
                val near50Priority = when {
                    absD <= 5 -> 80; absD <= 10 -> 60; else -> 50
                }
                tricks.add(
                    MathTrick(
                        name = "Near-50 trick",
                        hint = "n\u00B2 = (n \u2212 25) \u00D7 100 + (n \u2212 50)\u00B2",
                        steps = listOf(
                            "Formula: n\u00B2 = (n \u2212 25) \u00D7 100 + (n \u2212 50)\u00B2",
                            "$n \u2212 25 = ${n - 25}",
                            "${n - 25} \u00D7 100 = #a{$hundreds}",
                            "$n is #b{$absD} from 50, so #b{$absD}\u00B2 = #b{$tail}",
                            "#a{$hundreds} + #b{$tail} = #r{$answer}",
                        ),
                        priority = near50Priority,
                    )
                )
            }
        }

        // === 6. Split into tens + ones ===
        // 16² = (10+6)² = 100 + 120 + 36 = 256
        if (ones in 1..9) {
            val aSq = tens.toLong() * tens.toLong()
            val twoAB = 2L * tens.toLong() * ones.toLong()
            val bSq = ones.toLong() * ones.toLong()
            tricks.add(
                MathTrick(
                    name = "Split into tens + ones",
                    hint = "(a + b)\u00B2 = a\u00B2 + 2ab + b\u00B2",
                    steps = listOf(
                        "Formula: (a + b)\u00B2 = a\u00B2 + 2ab + b\u00B2",
                        "$n = #a{$tens} + #b{$ones}",
                        "#a{$tens}\u00B2 = #a{$aSq}",
                        "2 \u00D7 #a{$tens} \u00D7 #b{$ones} = #c{$twoAB}",
                        "#b{$ones}\u00B2 = #b{$bSq}",
                        "#a{$aSq} + #c{$twoAB} + #b{$bSq} = #r{$answer}",
                    ),
                    priority = 40,
                )
            )
        }

        // === 7. From neighbor square ===
        // 16² = 15² + 2×16 − 1 = 225 + 31 = 256
        if (ones != 0) {
            val prev = n - 1
            val prevSq = prev.toLong() * prev.toLong()
            val step = 2L * n - 1
            tricks.add(
                MathTrick(
                    name = "From ${prev}\u00B2",
                    hint = "n\u00B2 = (n \u2212 1)\u00B2 + 2n \u2212 1",
                    steps = listOf(
                        "Formula: n\u00B2 = (n \u2212 1)\u00B2 + 2n \u2212 1",
                        "#a{$prev}\u00B2 = #a{$prevSq}",
                        "2 \u00D7 $n \u2212 1 = #b{$step}",
                        "#a{$prevSq} + #b{$step} = #r{$answer}",
                    ),
                    priority = 20,
                )
            )
        }

        return tricks.sortedByDescending { it.priority }
    }
}

internal data class MathTrick(
    val name: String,
    val hint: String,
    val steps: List<String>,
    val priority: Int = 0,
)
