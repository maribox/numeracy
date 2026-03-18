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

    private fun buildApplicableTricks(a: Int, b: Int, answer: Long): List<MathTrick> {
        val tricks = mutableListOf<MathTrick>()

        val bTens = (b / 10) * 10
        val bOnes = b % 10
        val aTens = (a / 10) * 10
        val aOnes = a % 10

        // 1. Distributive — split b
        if (bOnes != 0) {
            val partialTens = a.toLong() * bTens.toLong()
            val partialOnes = a.toLong() * bOnes.toLong()
            tricks.add(
                MathTrick(
                    name = "Split $b into tens + ones",
                    hint = "a \u00D7 (b + c) = a\u00D7b + a\u00D7c",
                    steps = listOf(
                        "Formula: a \u00D7 (b + c) = a\u00D7b + a\u00D7c",
                        "$b = #a{$bTens} + #b{$bOnes}",
                        "$a \u00D7 #a{$bTens} = #a{$partialTens}",
                        "$a \u00D7 #b{$bOnes} = #b{$partialOnes}",
                        "#a{$partialTens} + #b{$partialOnes} = #r{$answer}",
                    ),
                    priority = 40,
                )
            )
        }

        // 2. Distributive — split a
        if (aOnes != 0 && aOnes != bOnes) {
            val pTens = aTens.toLong() * b.toLong()
            val pOnes = aOnes.toLong() * b.toLong()
            tricks.add(
                MathTrick(
                    name = "Split $a into tens + ones",
                    hint = "(a + b) \u00D7 c = a\u00D7c + b\u00D7c",
                    steps = listOf(
                        "Formula: (a + b) \u00D7 c = a\u00D7c + b\u00D7c",
                        "$a = #a{$aTens} + #b{$aOnes}",
                        "#a{$aTens} \u00D7 $b = #a{$pTens}",
                        "#b{$aOnes} \u00D7 $b = #b{$pOnes}",
                        "#a{$pTens} + #b{$pOnes} = #r{$answer}",
                    ),
                    priority = 40,
                )
            )
        }

        // 3. ×11 — put the digit sum in the middle
        if (b == 11 || a == 11) {
            val other = if (b == 11) a else b
            val d1 = other / 10
            val d2 = other % 10
            val sum = d1 + d2
            if (sum <= 9) {
                tricks.add(
                    MathTrick(
                        name = "\u00D711 shortcut",
                        hint = "n \u00D7 11: put the digit sum in the middle",
                        steps = listOf(
                            "Rule: n \u00D7 11 \u2192 first digit | digit sum | last digit",
                            "Digits of $other: #a{$d1} and #b{$d2}",
                            "#a{$d1} + #b{$d2} = #c{$sum}",
                            "#a{$d1}#c{$sum}#b{$d2} = #r{$answer}",
                        ),
                        priority = 100,
                    )
                )
            } else {
                val carry = sum / 10
                val middle = sum % 10
                tricks.add(
                    MathTrick(
                        name = "\u00D711 shortcut (with carry)",
                        hint = "n \u00D7 11: digit sum in the middle, carry if \u2265 10",
                        steps = listOf(
                            "Rule: n \u00D7 11 \u2192 first digit | digit sum | last digit",
                            "Digits of $other: #a{$d1} and #b{$d2}",
                            "#a{$d1} + #b{$d2} = #c{$sum} \u2265 10, so carry #c{$carry}",
                            "(#a{$d1} + #c{$carry})#c{$middle}#b{$d2} = #r{$answer}",
                        ),
                        priority = 100,
                    )
                )
            }
        }

        // 4. ×25 = ×100 ÷ 4
        if (b == 25 || a == 25) {
            val other = if (b == 25) a else b
            val times100 = other * 100L
            tricks.add(
                MathTrick(
                    name = "\u00D725 shortcut",
                    hint = "n \u00D7 25 = n \u00D7 100 \u00F7 4",
                    steps = listOf(
                        "Because 25 = 100 \u00F7 4",
                        "$other \u00D7 100 = #a{$times100}",
                        "#a{$times100} \u00F7 4 = #r{$answer}",
                    ),
                    priority = 100,
                )
            )
        }

        // 5. ×50 = ×100 ÷ 2
        if (b == 50 || a == 50) {
            val other = if (b == 50) a else b
            val times100 = other * 100L
            tricks.add(
                MathTrick(
                    name = "\u00D750 shortcut",
                    hint = "n \u00D7 50 = n \u00D7 100 \u00F7 2",
                    steps = listOf(
                        "Because 50 = 100 \u00F7 2",
                        "$other \u00D7 100 = #a{$times100}",
                        "#a{$times100} \u00F7 2 = #r{$answer}",
                    ),
                    priority = 100,
                )
            )
        }

        // 6. ×5 = ×10 ÷ 2
        if (b == 5 || a == 5) {
            val other = if (b == 5) a else b
            val times10 = other * 10L
            tricks.add(
                MathTrick(
                    name = "\u00D75 shortcut",
                    hint = "n \u00D7 5 = n \u00D7 10 \u00F7 2",
                    steps = listOf(
                        "Because 5 = 10 \u00F7 2",
                        "$other \u00D7 10 = #a{$times10}",
                        "#a{$times10} \u00F7 2 = #r{$answer}",
                    ),
                    priority = 100,
                )
            )
        }

        // 7. Both near 100
        if (a in 90..99 && b in 90..99) {
            val da = 100 - a
            val db = 100 - b
            val firstPart = 100 - (da + db)
            val lastPart = da * db
            tricks.add(
                MathTrick(
                    name = "Both near 100",
                    hint = "(100 \u2212 a)(100 \u2212 b) = (100 \u2212 a \u2212 b) | a\u00D7b",
                    steps = listOf(
                        "How far from 100? $a \u2192 #a{$da}, $b \u2192 #b{$db}",
                        "Front: 100 \u2212 #a{$da} \u2212 #b{$db} = #c{$firstPart}",
                        "Back: #a{$da} \u00D7 #b{$db} = #d{${lastPart.toString().padStart(2, '0')}}",
                        "Together: #c{$firstPart}#d{${lastPart.toString().padStart(2, '0')}} = #r{$answer}",
                    ),
                    priority = 90,
                )
            )
        }

        // 8. Difference of squares
        val sum = a + b
        if (sum % 2 == 0) {
            val mid = sum / 2
            val d = kotlin.math.abs(mid - a)
            if (d in 1..9 && mid % 5 == 0) {
                val midSq = mid.toLong() * mid.toLong()
                val dSq = d.toLong() * d.toLong()
                tricks.add(
                    MathTrick(
                        name = "Difference of squares",
                        hint = "a \u00D7 b = m\u00B2 \u2212 d\u00B2 where m is the midpoint",
                        steps = listOf(
                            "Formula: a \u00D7 b = ((a+b)/2)\u00B2 \u2212 ((a\u2212b)/2)\u00B2",
                            "Midpoint: ($a + $b) \u00F7 2 = #a{$mid}",
                            "Half-gap: #b{$d}",
                            "#a{$mid}\u00B2 = #a{$midSq}",
                            "#b{$d}\u00B2 = #b{$dSq}",
                            "#a{$midSq} \u2212 #b{$dSq} = #r{$answer}",
                        ),
                        priority = 70,
                    )
                )
            }
        }

        // 9. Round & compensate
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
                1 -> 65; 2 -> 60; else -> 55
            }
            tricks.add(
                MathTrick(
                    name = "Round & compensate",
                    hint = "a \u00D7 b = a \u00D7 round(b) \u00B1 a \u00D7 diff",
                    steps = listOf(
                        "Round $b $direction to #a{$bRounded} (off by $absDiff)",
                        "$a \u00D7 #a{$bRounded} = #a{$roundProduct}",
                        "Fix the rounding: $a \u00D7 $absDiff = #b{$adjustment}",
                        "#a{$roundProduct} $op #b{$adjustment} = #r{$answer}",
                    ),
                    priority = roundPriority,
                )
            )
        }

        // 10. Both end in 5
        if (a % 10 == 5 && b % 10 == 5) {
            val at = a / 10
            val bt = b / 10
            val tensProd = at * bt
            val tensSum = at + bt
            val isEvenSum = tensSum % 2 == 0
            val halfSum = tensSum / 2
            val leading = tensProd + halfSum
            val lastDigits = if (isEvenSum) "25" else "75"
            tricks.add(
                MathTrick(
                    name = "Both end in 5",
                    hint = "a5 \u00D7 b5 = (a\u00D7b + (a+b)/2) | 25 or 75",
                    steps = listOf(
                        "Tens digits: #a{$at} and #a{$bt}",
                        "#a{$at} \u00D7 #a{$bt} = #a{$tensProd}",
                        "(#a{$at} + #a{$bt}) \u00F7 2 = #b{$halfSum}${if (!isEvenSum) " (round down, ending becomes 75)" else ""}",
                        "#a{$tensProd} + #b{$halfSum} = #c{$leading}",
                        "#c{$leading} | $lastDigits = #r{$answer}",
                    ),
                    priority = 90,
                )
            )
        }

        return tricks.sortedByDescending { it.priority }
    }
}
