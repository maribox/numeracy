package it.bosler.numeracy.generator

import it.bosler.numeracy.model.InputType
import it.bosler.numeracy.model.Problem
import it.bosler.numeracy.model.ScenarioType
import kotlin.random.Random

class MultiplicationGenerator : ProblemGenerator {
    override fun generate(): Problem {
        val a = Random.nextInt(10, 100)
        val b = Random.nextInt(10, 100)
        val answer = a.toLong() * b.toLong()

        val tricks = buildApplicableTricks(a, b, answer)
        val selectedTrick = tricks.random()

        return Problem(
            scenarioType = ScenarioType.MULTIPLICATION,
            questionText = "$a \u00D7 $b",
            correctAnswer = answer.toString(),
            inputType = InputType.NUMBER,
            explanation = "$a \u00D7 $b = $answer",
            metadata = buildMap {
                put("a", a.toString())
                put("b", b.toString())
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
     * Builds all applicable tricks for [a] × [b], sorted by priority (most relevant first).
     *
     * Priority guidelines:
     * - 100: Exact special-factor match (×11, ×25, ×50, ×5 — instant shortcuts)
     * - 90:  Both factors match a pattern (both end in 5, both near 100)
     * - 70:  Structural shortcut (difference of squares with nice midpoint)
     * - 60:  Close to a round number (round & compensate, 1-2 away from a ten)
     * - 40:  Always-applicable algebraic (distributive property — works but no special insight)
     */
    private fun buildApplicableTricks(a: Int, b: Int, answer: Long): List<MathTrick> {
        val tricks = mutableListOf<MathTrick>()

        val bTens = (b / 10) * 10
        val bOnes = b % 10
        val aTens = (a / 10) * 10
        val aOnes = a % 10

        // 1. Distributive property — always works, general purpose
        // Color tracks: a=tens part (blue), b=ones part (pink), r=result
        if (bOnes != 0) {
            val partialTens = a.toLong() * bTens.toLong()
            val partialOnes = a.toLong() * bOnes.toLong()
            tricks.add(
                MathTrick(
                    name = "Split $b into tens + ones",
                    hint = "Break $b into $bTens + $bOnes, multiply each by $a, then add.",
                    steps = listOf(
                        "Split: $b = #a{$bTens} + #b{$bOnes}",
                        "Tens part: $a \u00D7 #a{$bTens} = #a{$partialTens}",
                        "Ones part: $a \u00D7 #b{$bOnes} = #b{$partialOnes}",
                        "Add together: #a{$partialTens} + #b{$partialOnes} = #r{$answer}",
                    ),
                    priority = 40,
                )
            )
        }

        // 2. Distribute the other way — also general purpose
        // Color tracks: a=tens part (blue), b=ones part (pink), r=result
        if (aOnes != 0 && aOnes != bOnes) {
            val pTens = aTens.toLong() * b.toLong()
            val pOnes = aOnes.toLong() * b.toLong()
            tricks.add(
                MathTrick(
                    name = "Split $a into tens + ones",
                    hint = "Break $a into $aTens + $aOnes, multiply each by $b, then add.",
                    steps = listOf(
                        "Split: $a = #a{$aTens} + #b{$aOnes}",
                        "Tens part: #a{$aTens} \u00D7 $b = #a{$pTens}",
                        "Ones part: #b{$aOnes} \u00D7 $b = #b{$pOnes}",
                        "Add together: #a{$pTens} + #b{$pOnes} = #r{$answer}",
                    ),
                    priority = 40,
                )
            )
        }

        // 3. ×11 trick — instant shortcut when one factor is exactly 11
        if (b == 11 || a == 11) {
            val other = if (b == 11) a else b
            val d1 = other / 10
            val d2 = other % 10
            val sum = d1 + d2
            // Color tracks: a=first digit (blue), b=second digit (pink), c=sum (amber), r=result
            if (sum <= 9) {
                tricks.add(
                    MathTrick(
                        name = "\u00D711 shortcut",
                        hint = "To multiply by 11: put the sum of the digits between them.",
                        steps = listOf(
                            "Digits of $other: #a{$d1} and #b{$d2}",
                            "Sum: #a{$d1} + #b{$d2} = #c{$sum}",
                            "Insert between: #a{$d1}#c{$sum}#b{$d2} = #r{$answer}",
                        ),
                        priority = 100,
                    )
                )
            } else {
                val carry = sum / 10
                val middle = sum % 10
                tricks.add(
                    MathTrick(
                        name = "\u00D711 shortcut (carry)",
                        hint = "Same \u00D711 rule, but the digit sum is $sum \u2265 10, so carry the 1.",
                        steps = listOf(
                            "Digits of $other: #a{$d1} and #b{$d2}",
                            "Sum: #a{$d1} + #b{$d2} = #c{$sum} (carry the #c{$carry})",
                            "Keep #c{$middle} in the middle, add #c{$carry} to #a{$d1}",
                            "Build: #a{${d1 + carry}}#c{$middle}#b{$d2} = #r{$answer}",
                        ),
                        priority = 100,
                    )
                )
            }
        }

        // 4. ×25 — instant shortcut when one factor is exactly 25
        if (b == 25 || a == 25) {
            val other = if (b == 25) a else b
            val times100 = other * 100L
            // Color tracks: a=×100 result (blue), r=result
            tricks.add(
                MathTrick(
                    name = "\u00D725 shortcut",
                    hint = "25 is 100 \u00F7 4. Multiply by 100 first (easy!), then divide by 4.",
                    steps = listOf(
                        "Key insight: 25 = 100 \u00F7 4",
                        "Multiply by 100: $other \u00D7 100 = #a{$times100}",
                        "Divide by 4: #a{$times100} \u00F7 4 = #r{$answer}",
                    ),
                    priority = 100,
                )
            )
        }

        // 5. ×50 — instant shortcut when one factor is exactly 50
        if (b == 50 || a == 50) {
            val other = if (b == 50) a else b
            val times100 = other * 100L
            // Color tracks: a=×100 result (blue), r=result
            tricks.add(
                MathTrick(
                    name = "\u00D750 shortcut",
                    hint = "50 is 100 \u00F7 2. Multiply by 100 first, then halve it.",
                    steps = listOf(
                        "Key insight: 50 = 100 \u00F7 2",
                        "Multiply by 100: $other \u00D7 100 = #a{$times100}",
                        "Halve it: #a{$times100} \u00F7 2 = #r{$answer}",
                    ),
                    priority = 100,
                )
            )
        }

        // 6. ×5 — instant shortcut when one factor is exactly 5
        if (b == 5 || a == 5) {
            val other = if (b == 5) a else b
            val times10 = other * 10L
            // Color tracks: a=×10 result (blue), r=result
            tricks.add(
                MathTrick(
                    name = "\u00D75 shortcut",
                    hint = "5 is 10 \u00F7 2. Multiply by 10 first (add a zero), then halve it.",
                    steps = listOf(
                        "Key insight: 5 = 10 \u00F7 2",
                        "Multiply by 10: $other \u00D7 10 = #a{$times10}",
                        "Halve it: #a{$times10} \u00F7 2 = #r{$answer}",
                    ),
                    priority = 100,
                )
            )
        }

        // 7. Both near 100 — high priority when both factors are 90+
        if (a in 90..99 && b in 90..99) {
            val da = 100 - a
            val db = 100 - b
            val firstPart = 100 - (da + db)
            val lastPart = da * db
            // Color tracks: a=deficit of a (blue), b=deficit of b (pink), c=front (amber), d=back (teal), r=result
            tricks.add(
                MathTrick(
                    name = "Both near 100",
                    hint = "$a is $da below 100, $b is $db below 100. Subtract deficits for the front, multiply for the back.",
                    steps = listOf(
                        "Deficit of $a: 100 \u2212 $a = #a{$da}",
                        "Deficit of $b: 100 \u2212 $b = #b{$db}",
                        "Front: 100 \u2212 #a{$da} \u2212 #b{$db} = #c{$firstPart}",
                        "Back: #a{$da} \u00D7 #b{$db} = #d{${lastPart.toString().padStart(2, '0')}}",
                        "Combine: #c{$firstPart}#d{${lastPart.toString().padStart(2, '0')}} = #r{$answer}",
                    ),
                    priority = 90,
                )
            )
        }

        // 8. Difference of squares — high priority when midpoint is a nice round number
        val sum = a + b
        if (sum % 2 == 0) {
            val mid = sum / 2
            val d = kotlin.math.abs(mid - a)
            if (d in 1..9 && mid % 5 == 0) {
                val midSq = mid.toLong() * mid.toLong()
                val dSq = d.toLong() * d.toLong()
                // Color tracks: a=midpoint square (blue), b=distance square (pink), r=result
                tricks.add(
                    MathTrick(
                        name = "Difference of squares",
                        hint = "$a and $b are both $d away from $mid. Use the difference of squares: $mid² \u2212 $d².",
                        steps = listOf(
                            "Midpoint: ($a + $b) \u00F7 2 = #a{$mid}",
                            "Distance: #b{$d}",
                            "Square midpoint: #a{$mid}² = #a{$midSq}",
                            "Square distance: #b{$d}² = #b{$dSq}",
                            "Subtract: #a{$midSq} \u2212 #b{$dSq} = #r{$answer}",
                        ),
                        priority = 70,
                    )
                )
            }
        }

        // 9. Round & compensate — better the closer b is to a multiple of 10
        val bDist = if (bOnes <= 5) bOnes else 10 - bOnes
        val bRounded = if (bOnes <= 5) b - bOnes else b + (10 - bOnes)
        if (bDist in 1..3 && bRounded > 0) {
            val roundProduct = a.toLong() * bRounded.toLong()
            val diff = b - bRounded
            val absDiff = kotlin.math.abs(diff)
            val adjustment = a.toLong() * absDiff.toLong()
            val op = if (diff > 0) "+" else "\u2212"
            val direction = if (diff > 0) "up" else "down"
            val roundPriority = when (bDist) {
                1 -> 65
                2 -> 60
                else -> 55
            }
            // Color tracks: a=round product (blue), b=adjustment (pink), r=result
            tricks.add(
                MathTrick(
                    name = "Round $b to $bRounded",
                    hint = "$b is only $absDiff away from $bRounded. Round, multiply, then adjust.",
                    steps = listOf(
                        "Round $b $direction to #a{$bRounded} (off by $absDiff)",
                        "Easy multiply: $a \u00D7 #a{$bRounded} = #a{$roundProduct}",
                        "Adjustment: $a \u00D7 $absDiff = #b{$adjustment}",
                        "Combine: #a{$roundProduct} $op #b{$adjustment} = #r{$answer}",
                    ),
                    priority = roundPriority,
                )
            )
        }

        // 10. Both end in 5 — pattern match for both factors
        if (a % 10 == 5 && b % 10 == 5) {
            val at = a / 10
            val bt = b / 10
            val tensProd = at * bt
            val tensSum = at + bt
            val isEvenSum = tensSum % 2 == 0
            val halfSum = tensSum / 2
            val leading = tensProd + halfSum
            val lastDigits = if (isEvenSum) "25" else "75"
            // Color tracks: a=tens product (blue), b=half sum (pink), c=leading digits (amber), r=result
            tricks.add(
                MathTrick(
                    name = "Both end in 5",
                    hint = "Both end in 5. Multiply tens digits, add half their sum, append ${if (isEvenSum) "25" else "75"}.",
                    steps = listOf(
                        "Tens digits: #a{$at} and #a{$bt}",
                        "Multiply: #a{$at} \u00D7 #a{$bt} = #a{$tensProd}",
                        "Half their sum: (#a{$at} + #a{$bt}) \u00F7 2 = #b{$halfSum}${if (!isEvenSum) " (round \u2192 +50 to ending)" else ""}",
                        "Leading: #a{$tensProd} + #b{$halfSum} = #c{$leading}",
                        "Append: #c{$leading}#d{$lastDigits} = #r{$answer}",
                    ),
                    priority = 90,
                )
            )
        }

        return tricks.sortedByDescending { it.priority }
    }
}
