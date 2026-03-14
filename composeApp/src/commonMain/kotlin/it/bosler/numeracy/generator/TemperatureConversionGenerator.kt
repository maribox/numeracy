package it.bosler.numeracy.generator

import it.bosler.numeracy.model.InputType
import it.bosler.numeracy.model.Problem
import it.bosler.numeracy.model.ScenarioType
import kotlin.math.roundToInt
import kotlin.random.Random

class TemperatureConversionGenerator : ProblemGenerator {

    // Landmark pairs for practice mode reference
    private val landmarks = listOf(
        -40 to -40, 0 to 32, 10 to 50, 20 to 68, 30 to 86,
        37 to 99, 100 to 212,
    )

    override fun generate(): Problem {
        val toFahrenheit = Random.nextBoolean()

        return if (toFahrenheit) {
            val celsius = Random.nextInt(-20, 46)
            val fahrenheit = (celsius * 9.0 / 5.0 + 32).roundToInt()
            val nearest = landmarks.minBy { kotlin.math.abs(it.first - celsius) }

            Problem(
                scenarioType = ScenarioType.TEMPERATURE_CONVERSION,
                tolerancePercent = 5.0,
                questionText = "$celsius°C = ? °F",
                correctAnswer = fahrenheit.toString(),
                inputType = InputType.NUMBER,
                explanation = "$celsius × 9/5 + 32 = $fahrenheit°F",
                metadata = mapOf(
                    "value" to celsius.toString(),
                    "fromUnit" to "°C",
                    "toUnit" to "°F",
                    "context" to tempContext(celsius),
                    "trick" to "×2, subtract 10%, add 32",
                    "trickSteps" to run {
                        val x2 = celsius * 2
                        val pct = (x2 * 0.1).roundToInt()
                        val sub = x2 - pct
                        "$celsius × 2 = $x2\n- 10% = $x2 - $pct = $sub\n+ 32 = ${sub + 32}"
                    },
                    "nearestLandmarkC" to "${nearest.first}°C",
                    "nearestLandmarkF" to "${nearest.second}°F",
                    "tempNorm" to tempNormalized(celsius),
                ),
            )
        } else {
            val fahrenheit = Random.nextInt(0, 115)
            val celsius = ((fahrenheit - 32) * 5.0 / 9.0).roundToInt()
            val nearest = landmarks.minBy { kotlin.math.abs(it.second - fahrenheit) }

            Problem(
                scenarioType = ScenarioType.TEMPERATURE_CONVERSION,
                tolerancePercent = 5.0,
                questionText = "$fahrenheit°F = ? °C",
                correctAnswer = celsius.toString(),
                inputType = InputType.NUMBER,
                explanation = "($fahrenheit - 32) × 5/9 = $celsius°C",
                metadata = mapOf(
                    "value" to fahrenheit.toString(),
                    "fromUnit" to "°F",
                    "toUnit" to "°C",
                    "context" to tempContextF(fahrenheit),
                    "trick" to "- 32, ÷ 2, add 10%",
                    "trickSteps" to run {
                        val sub = fahrenheit - 32
                        val half = sub / 2.0
                        val pct = (half * 0.1).roundToInt()
                        "$fahrenheit - 32 = $sub\n÷ 2 = ${half.roundToInt()}\n+ 10% ≈ ${half.roundToInt()} + $pct = ${half.roundToInt() + pct}"
                    },
                    "nearestLandmarkC" to "${nearest.first}°C",
                    "nearestLandmarkF" to "${nearest.second}°F",
                    "tempNorm" to tempNormalizedF(fahrenheit),
                ),
            )
        }
    }

    private fun tempContext(c: Int): String = when {
        c <= -10 -> "Bitter cold"
        c <= 0 -> "Freezing"
        c <= 10 -> "Cold"
        c <= 20 -> "Cool"
        c <= 25 -> "Comfortable"
        c <= 35 -> "Hot"
        else -> "Extreme heat"
    }

    private fun tempContextF(f: Int): String = when {
        f <= 14 -> "Bitter cold"
        f <= 32 -> "Freezing"
        f <= 50 -> "Cold"
        f <= 68 -> "Cool"
        f <= 77 -> "Comfortable"
        f <= 95 -> "Hot"
        else -> "Extreme heat"
    }

    // 0.0 = coldest, 1.0 = hottest for thermometer fill
    private fun tempNormalized(c: Int): String =
        ((c + 20).toDouble() / 65.0).coerceIn(0.0, 1.0).toString()

    private fun tempNormalizedF(f: Int): String =
        ((f).toDouble() / 115.0).coerceIn(0.0, 1.0).toString()
}
