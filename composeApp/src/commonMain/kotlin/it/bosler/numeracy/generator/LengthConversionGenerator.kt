package it.bosler.numeracy.generator

import it.bosler.numeracy.model.InputType
import it.bosler.numeracy.model.Problem
import it.bosler.numeracy.model.ScenarioType
import kotlin.math.roundToInt
import kotlin.random.Random

class LengthConversionGenerator(private val rng: Random = Random.Default) : ProblemGenerator {

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
        Conversion("mi", "km", 1.609, 2..200, "\uD83D\uDEE3", "Road trip",
            "×8 ÷ 5 (or ×1.6)",
            { v -> "$v × 8 = ${v * 8}\n${divisionStep(v * 8, 5)}" }),
        Conversion("km", "mi", 0.6214, 5..300, "\uD83D\uDEE3", "Road sign",
            "×5 ÷ 8 (or ×0.62)",
            { v -> "$v × 5 = ${v * 5}\n${divisionStep(v * 5, 8)}" }),
        Conversion("ft", "m", 0.3048, 10..500, "\uD83C\uDFD7", "Building height",
            "÷ 3, subtract 5%",
            { v -> val d3 = v / 3.0; val pct = d3 * 0.05; "$v ÷ 3 ≈ ${d3.roundToInt()}\n- 5% ≈ ${(d3 - pct).roundToInt()}" }),
        Conversion("m", "ft", 3.2808, 1..150, "\uD83C\uDFD7", "Building height",
            "×3, add 10%",
            { v -> val x3 = v * 3; val pct = (x3 * 0.1).roundToInt(); "$v × 3 = $x3\n+ 10% = $x3 + $pct = ${x3 + pct}" }),
        Conversion("in", "cm", 2.54, 1..80, "\uD83D\uDCCF", "Screen size",
            "×2.5 (or ×5 ÷ 2)",
            { v -> "$v × 5 = ${v * 5}\n${divisionStep(v * 5, 2)}" }),
        Conversion("cm", "in", 0.3937, 8..200, "\uD83D\uDCCF", "Screen size",
            "÷ 2.5 (or ×2 ÷ 5)",
            { v -> "$v × 2 = ${v * 2}\n${divisionStep(v * 2, 5)}" }),
    )

    override fun generate(): Problem {
        val conv = conversions[rng.nextInt(conversions.size)]
        val value = rng.nextInt(conv.range.first, conv.range.last + 1)
        val exact = value * conv.factor
        val answer = exact.roundToInt()

        return Problem(
            scenarioType = ScenarioType.LENGTH_CONVERSION,
            questionText = "$value ${conv.fromUnit} = ? ${conv.toUnit}",
            correctAnswer = answer.toString(),
            inputType = InputType.NUMBER,
            tolerancePercent = 5.0,
            explanation = "$value ${conv.fromUnit} × ${conv.factor} ${relation(exact, answer)} $answer ${conv.toUnit}",
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
