package it.bosler.numeracy.generator

import it.bosler.numeracy.model.InputType
import it.bosler.numeracy.model.Problem
import it.bosler.numeracy.model.ScenarioType
import kotlin.math.roundToInt
import kotlin.random.Random

class VolumeConversionGenerator : ProblemGenerator {

    private data class Conversion(
        val fromUnit: String,
        val toUnit: String,
        val factor: Double,
        val range: IntRange,
        val context: String,
        val trick: String,
        val trickSteps: (Int) -> String,
    )

    private val conversions = listOf(
        Conversion("gal", "L", 3.785, 1..30, "Fuel tank",
            "×4, subtract 5%",
            { v ->
                val x4 = v * 4
                val pct = (x4 * 0.05).roundToInt()
                "$v × 4 = $x4\n- 5% ≈ $x4 - $pct = ${x4 - pct}"
            }),
        Conversion("L", "gal", 0.2642, 1..100, "Fuel tank",
            "÷ 4, add 5%",
            { v ->
                val d4 = v / 4.0
                val pct = (d4 * 0.05).roundToInt()
                "$v ÷ 4 ≈ ${d4.roundToInt()}\n+ 5% ≈ ${d4.roundToInt()} + $pct = ${d4.roundToInt() + pct}"
            }),
        Conversion("cups", "mL", 236.6, 1..12, "Recipe",
            "×240 (or ×250 - 4%)",
            { v ->
                val x240 = v * 240
                "$v × 240 = $x240"
            }),
        Conversion("fl oz", "mL", 29.57, 1..32, "Drink size",
            "×30",
            { v ->
                val x30 = v * 30
                "$v × 30 = $x30"
            }),
        Conversion("mL", "fl oz", 0.03381, 100..1000, "Drink size",
            "÷ 30",
            { v ->
                val d30 = v / 30.0
                "$v ÷ 30 ≈ ${d30.roundToInt()}"
            }),
    )

    override fun generate(): Problem {
        val conv = conversions[Random.nextInt(conversions.size)]
        val value = Random.nextInt(conv.range.first, conv.range.last + 1)
        val exact = value * conv.factor
        val answer = exact.roundToInt()

        return Problem(
            scenarioType = ScenarioType.VOLUME_CONVERSION,
            tolerancePercent = 5.0,
            questionText = "$value ${conv.fromUnit} = ? ${conv.toUnit}",
            correctAnswer = answer.toString(),
            inputType = InputType.NUMBER,
            explanation = "$value ${conv.fromUnit} × ${conv.factor} ≈ $answer ${conv.toUnit}",
            metadata = mapOf(
                "value" to value.toString(),
                "fromUnit" to conv.fromUnit,
                "toUnit" to conv.toUnit,
                "context" to conv.context,
                "trick" to conv.trick,
                "trickSteps" to conv.trickSteps(value),
                "factor" to conv.factor.toString(),
            ),
        )
    }
}
