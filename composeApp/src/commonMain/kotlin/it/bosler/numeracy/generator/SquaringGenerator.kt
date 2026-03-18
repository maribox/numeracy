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

    /**
     * Builds all applicable tricks for squaring [n], sorted by priority (most relevant first).
     *
     * Priority guidelines:
     * - 100: Exact pattern match (ends-in-5 rule — instant shortcut, no real math needed)
     * - 80:  Very close to a special number (within 1-2 of a round ten, or near 50)
     * - 60:  Moderately close / nice structure (near-50 for numbers further away, adjust from neighbor)
     * - 40:  Always-applicable algebraic tricks ((a+b)², round & compensate)
     * - 20:  Fallback tricks (from neighbor square — always works but requires knowing (n-1)²)
     */
    private fun buildApplicableTricks(n: Int, answer: Long): List<MathTrick> {
        val tricks = mutableListOf<MathTrick>()

        val tens = (n / 10) * 10     // e.g. 56 → 50
        val ones = n % 10            // e.g. 56 → 6

        // === 1. Multiple of 10 — trivial shortcut ===
        // e.g. 90² = 9² × 100 = 8100
        if (ones == 0 && tens > 0) {
            val small = n / 10
            val smallSq = small * small
            tricks.add(
                MathTrick(
                    name = "Factor out the zeros",
                    hint = "$n is a multiple of 10 — square the small part, then append two zeros.",
                    steps = listOf(
                        "Factor: $n = #a{$small} \u00D7 10",
                        "Square #a{$small}: #a{$small}² = #a{$smallSq}",
                        "Add two zeros: #a{$smallSq} \u00D7 100 = #r{$answer}",
                    ),
                    priority = 100,
                )
            )
        }

        // === 2. Ends in 5 — instant shortcut ===
        // e.g. 65² → k=6, 6×7=42, append 25 → 4225
        if (ones == 5) {
            val k = n / 10
            val product = k * (k + 1)
            tricks.add(
                MathTrick(
                    name = "Ends-in-5 shortcut",
                    hint = "For numbers ending in 5: multiply the tens digit by the next integer, then append 25.",
                    steps = listOf(
                        "Tens digit: #a{$k}",
                        "Next integer: #b{${k + 1}}",
                        "Multiply: #a{$k} \u00D7 #b{${k + 1}} = #c{$product}",
                        "Append 25: #c{$product} \u2192 #r{${product}25}",
                    ),
                    priority = 100,
                )
            )
        }

        // === 3. Round to nearest ten ===
        // e.g. 59² → 59 is 1 from 60, 60×58=3480, +1²=1, =3481
        if (ones != 0) {
            val d = if (ones <= 5) ones else 10 - ones  // distance to nearest ten
            val r = if (ones <= 5) n - ones else n + (10 - ones) // nearest ten
            val partner = 2 * n - r  // the other number equally spaced from n
            val diffProduct = r.toLong() * partner.toLong()
            val dSquared = d.toLong() * d.toLong()
            val roundPriority = when (d) {
                1 -> 80; 2 -> 70; 3 -> 60; else -> 40
            }
            tricks.add(
                MathTrick(
                    name = "Round to nearest ten",
                    hint = "$n is only $d away from $r — multiply the two equidistant numbers, then add $d².",
                    steps = listOf(
                        "Nearest ten: $n is #b{$d} away from #a{$r}",
                        "Equal spacing: #a{$r} and #a{$partner} (both #b{$d} from $n)",
                        "Multiply: #a{$r} \u00D7 #a{$partner} = #c{$diffProduct}",
                        "Add #b{$d}²: #b{$d}² = #b{$dSquared}",
                        "Result: #c{$diffProduct} + #b{$dSquared} = #r{$answer}",
                    ),
                    priority = roundPriority,
                )
            )
        }

        // === 4. Adjust from a known square ===
        // e.g. 51² = 50² + (51−50)(51+50) = 2500 + 101 = 2601
        // Works best when n is 1-3 away from a "landmark" (multiples of 10, or 25, 50, 75, 100)
        val landmarks = listOf(10, 20, 25, 30, 40, 50, 60, 70, 75, 80, 90, 100)
        for (m in landmarks) {
            val dist = kotlin.math.abs(n - m)
            if (dist in 1..3 && m != n) {
                val mSq = m.toLong() * m.toLong()
                val diff = n - m  // signed
                val adjustment = diff.toLong() * (n.toLong() + m.toLong())
                val absDiff = kotlin.math.abs(diff)
                val absAdj = kotlin.math.abs(adjustment)
                val sign = if (adjustment >= 0) "+" else "\u2212"
                val priority = when (dist) { 1 -> 75; 2 -> 65; else -> 55 }
                tricks.add(
                    MathTrick(
                        name = "Adjust from $m²",
                        hint = "$n is close to $m, whose square ($mSq) you know. Adjust using the gap.",
                        steps = listOf(
                            "Known square: #a{$m}² = #a{$mSq}",
                            "Gap: $n \u2212 $m = $diff, and $n + $m = ${n + m}",
                            "Adjustment: $absDiff \u00D7 ${n + m} = #b{$absAdj}",
                            "Result: #a{$mSq} $sign #b{$absAdj} = #r{$answer}",
                        ),
                        priority = priority,
                    )
                )
                break  // only use the closest landmark
            }
        }

        // === 5. Near-50 trick ===
        // e.g. 53² = (53−25)×100 + 3² = 2800 + 9 = 2809
        if (n in 30..70 && ones != 0) {
            val absD = kotlin.math.abs(50 - n)
            val hundreds = (n - 25) * 100
            val tail = absD.toLong() * absD.toLong()
            if (hundreds + tail == answer) {
                val near50Priority = when {
                    absD <= 5 -> 80; absD <= 10 -> 60; else -> 50
                }
                val direction = if (n >= 50) "above" else "below"
                tricks.add(
                    MathTrick(
                        name = "Near-50 trick",
                        hint = "$n is $absD $direction 50. Subtract 25, multiply by 100, then add the distance squared.",
                        steps = listOf(
                            "Distance from 50: #b{$absD}",
                            "$n \u2212 25 = ${n - 25}",
                            "Hundreds: ${n - 25} \u00D7 100 = #a{$hundreds}",
                            "Tail: #b{$absD}² = #b{$tail}",
                            "Combine: #a{$hundreds} + #b{$tail} = #r{$answer}",
                        ),
                        priority = near50Priority,
                    )
                )
            }
        }

        // === 6. Split into tens + ones (a+b)² ===
        // e.g. 16² = (10+6)² = 100 + 120 + 36 = 256
        // Only when ones > 0 (otherwise it's just "factor out zeros")
        if (ones in 1..9) {
            val aSq = tens.toLong() * tens.toLong()
            val twoAB = 2L * tens.toLong() * ones.toLong()
            val bSq = ones.toLong() * ones.toLong()
            tricks.add(
                MathTrick(
                    name = "Split into tens + ones",
                    hint = "Expand ($tens + $ones)² using the identity a² + 2ab + b².",
                    steps = listOf(
                        "Split: $n = #a{$tens} + #b{$ones}",
                        "#a{$tens}² = #a{$aSq}",
                        "2 \u00D7 #a{$tens} \u00D7 #b{$ones} = #c{$twoAB}",
                        "#b{$ones}² = #b{$bSq}",
                        "#a{$aSq} + #c{$twoAB} + #b{$bSq} = #r{$answer}",
                    ),
                    priority = 40,
                )
            )
        }

        // === 7. From neighbor square — fallback ===
        // e.g. 16² = 15² + 2×16 − 1 = 225 + 31 = 256
        if (ones != 0) {
            val prev = n - 1
            val prevSq = prev.toLong() * prev.toLong()
            val step = 2L * n - 1
            tricks.add(
                MathTrick(
                    name = "From ${prev}²",
                    hint = "Build on ${prev}² = $prevSq by adding 2 \u00D7 $n \u2212 1.",
                    steps = listOf(
                        "Previous square: #a{$prev}² = #a{$prevSq}",
                        "Step: 2 \u00D7 $n \u2212 1 = #b{$step}",
                        "#a{$prevSq} + #b{$step} = #r{$answer}",
                    ),
                    priority = 20,
                )
            )
        }

        return tricks.sortedByDescending { it.priority }
    }
}

/**
 * A mental math trick with its explanation and step-by-step breakdown.
 *
 * [priority] controls display order: higher = shown first. Tricks that are most specific
 * to the current numbers get the highest priority so the user sees the most useful trick
 * immediately. For example:
 * - Ends-in-5 rule gets top priority when n ends in 5 (it's the fastest shortcut)
 * - ×11/×25/×50 shortcuts get top priority when those exact factors appear
 * - Near-100 / near-50 rules rank high when both numbers are in that range
 * - Round & compensate ranks higher the closer a number is to a round ten
 * - General-purpose tricks (distributive, (a+b)² expansion) get lower priority
 *   since they always apply but aren't as insightful
 */
internal data class MathTrick(
    val name: String,
    val hint: String,
    val steps: List<String>,
    val priority: Int = 0,
)
