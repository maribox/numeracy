package it.bosler.numeracy.generator

import it.bosler.numeracy.model.InputType
import it.bosler.numeracy.model.Problem
import it.bosler.numeracy.model.ScenarioType
import kotlin.math.roundToInt
import kotlin.random.Random

class LengthConversionGenerator : ProblemGenerator {

    private data class Conversion(
        val fromUnit: String,
        val toUnit: String,
        val factor: Double,
        val range: IntRange,
        val icon: String,      // context icon for display
        val context: String,   // real-world context
        val trick: String,     // mental math shortcut
        val trickSteps: (Int) -> String, // step-by-step for learning mode
    )

    private val conversions = listOf(
        Conversion("mi", "km", 1.609, 1..200, "\uD83D\uDEE3", "Road trip",
            "×8 ÷ 5 (or ×1.6)",
            { v -> val x8 = v * 8; val r = x8 / 5; "$v × 8 = $x8\n$x8 ÷ 5 = $r" }),
        Conversion("km", "mi", 0.6214, 1..300, "\uD83D\uDEE3", "Road sign",
            "×5 ÷ 8 (or ×0.62)",
            { v -> val x5 = v * 5; val r = x5 / 8; "$v × 5 = $x5\n$x5 ÷ 8 = $r" }),
        Conversion("ft", "m", 0.3048, 5..500, "\uD83C\uDFD7", "Building height",
            "÷ 3, subtract 5%",
            { v -> val d3 = v / 3.0; val pct = d3 * 0.05; "$v ÷ 3 ≈ ${d3.roundToInt()}\n- 5% ≈ ${(d3 - pct).roundToInt()}" }),
        Conversion("m", "ft", 3.2808, 1..150, "\uD83C\uDFD7", "Building height",
            "×3, add 10%",
            { v -> val x3 = v * 3; val pct = (x3 * 0.1).roundToInt(); "$v × 3 = $x3\n+ 10% = $x3 + $pct = ${x3 + pct}" }),
        Conversion("in", "cm", 2.54, 1..80, "\uD83D\uDCCF", "Screen size",
            "×2.5 (or ×5 ÷ 2)",
            { v -> val x5 = v * 5; val r = x5 / 2; "$v × 5 = $x5\n$x5 ÷ 2 = $r" }),
        Conversion("cm", "in", 0.3937, 1..200, "\uD83D\uDCCF", "Screen size",
            "÷ 2.5 (or ×2 ÷ 5)",
            { v -> val x2 = v * 2; val r = x2 / 5; "$v × 2 = $x2\n$x2 ÷ 5 = $r" }),
    )

    override fun generate(): Problem {
        val conv = conversions[Random.nextInt(conversions.size)]
        val value = Random.nextInt(conv.range.first, conv.range.last + 1)
        val exact = value * conv.factor
        val answer = exact.roundToInt()

        return Problem(
            scenarioType = ScenarioType.LENGTH_CONVERSION,
            questionText = "$value ${conv.fromUnit} = ? ${conv.toUnit}",
            correctAnswer = answer.toString(),
            inputType = InputType.NUMBER,
            tolerancePercent = 5.0,
            explanation = "$value ${conv.fromUnit} × ${conv.factor} = $answer ${conv.toUnit}",
            metadata = mapOf(
                "value" to value.toString(),
                "fromUnit" to conv.fromUnit,
                "toUnit" to conv.toUnit,
                "icon" to conv.icon,
                "context" to conv.context,
                "trick" to conv.trick,
                "trickSteps" to conv.trickSteps(value),
                "factor" to conv.factor.toString(),
            ),
        )
    }
}
