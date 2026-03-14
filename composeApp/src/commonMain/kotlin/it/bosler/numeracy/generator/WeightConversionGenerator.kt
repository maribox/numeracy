package it.bosler.numeracy.generator

import it.bosler.numeracy.model.InputType
import it.bosler.numeracy.model.Problem
import it.bosler.numeracy.model.ScenarioType
import kotlin.math.roundToInt
import kotlin.random.Random

class WeightConversionGenerator : ProblemGenerator {

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
        Conversion("lb", "kg", 0.4536, 50..300, "Body weight",
            "÷ 2, subtract 10%",
            { v ->
                val half = v / 2.0
                val pct = half * 0.1
                val r = (half - pct).roundToInt()
                "$v ÷ 2 = ${half.roundToInt()}\n- 10% ≈ ${half.roundToInt()} - ${pct.roundToInt()} = $r"
            }),
        Conversion("kg", "lb", 2.2046, 20..150, "Body weight",
            "×2, add 10%",
            { v ->
                val x2 = v * 2
                val pct = (x2 * 0.1).roundToInt()
                "$v × 2 = $x2\n+ 10% = $x2 + $pct = ${x2 + pct}"
            }),
        Conversion("oz", "g", 28.35, 1..32, "Cooking",
            "×28 (or ×30 - 7%)",
            { v ->
                val x30 = v * 30
                val adj = (x30 * 0.07).roundToInt()
                "$v × 30 = $x30\n- 7% ≈ $x30 - $adj = ${x30 - adj}"
            }),
        Conversion("g", "oz", 0.03527, 25..900, "Cooking",
            "÷ 28 (or ÷ 30 + a bit)",
            { v ->
                val d30 = v / 30.0
                "$v ÷ 30 ≈ ${d30.roundToInt()}\n(exact: ${(v * 0.03527).roundToInt()})"
            }),
    )

    override fun generate(): Problem {
        val conv = conversions[Random.nextInt(conversions.size)]
        val value = Random.nextInt(conv.range.first, conv.range.last + 1)
        val exact = value * conv.factor
        val answer = exact.roundToInt()

        return Problem(
            scenarioType = ScenarioType.WEIGHT_CONVERSION,
            questionText = "$value ${conv.fromUnit} = ? ${conv.toUnit}",
            correctAnswer = answer.toString(),
            inputType = InputType.NUMBER,
            tolerancePercent = 5.0,
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
