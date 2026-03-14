package it.bosler.numeracy.generator

import it.bosler.numeracy.model.InputType
import it.bosler.numeracy.model.Problem
import it.bosler.numeracy.model.ScenarioType
import kotlin.math.roundToInt
import kotlin.random.Random

class SpeedConversionGenerator : ProblemGenerator {

    override fun generate(): Problem {
        val toKmh = Random.nextBoolean()

        return if (toKmh) {
            val mph = listOf(5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 100, 110, 120).random()
            val kmh = (mph * 1.609).roundToInt()

            Problem(
                scenarioType = ScenarioType.SPEED_CONVERSION,
                tolerancePercent = 5.0,
                questionText = "$mph mph = ? km/h",
                correctAnswer = kmh.toString(),
                inputType = InputType.NUMBER,
                explanation = "$mph × 1.609 ≈ $kmh km/h",
                metadata = mapOf(
                    "value" to mph.toString(),
                    "fromUnit" to "mph",
                    "toUnit" to "km/h",
                    "context" to speedContext(mph),
                    "trick" to "×8 ÷ 5",
                    "trickSteps" to run {
                        val x8 = mph * 8
                        val r = x8 / 5
                        "$mph × 8 = $x8\n$x8 ÷ 5 = $r"
                    },
                    "factor" to "1.609",
                    "needleNorm" to (mph.toDouble() / 130.0).coerceIn(0.0, 1.0).toString(),
                ),
            )
        } else {
            val kmh = listOf(10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120, 130, 140, 150, 160, 180, 200).random()
            val mph = (kmh * 0.6214).roundToInt()

            Problem(
                scenarioType = ScenarioType.SPEED_CONVERSION,
                tolerancePercent = 5.0,
                questionText = "$kmh km/h = ? mph",
                correctAnswer = mph.toString(),
                inputType = InputType.NUMBER,
                explanation = "$kmh × 0.6214 ≈ $mph mph",
                metadata = mapOf(
                    "value" to kmh.toString(),
                    "fromUnit" to "km/h",
                    "toUnit" to "mph",
                    "context" to speedContextKmh(kmh),
                    "trick" to "×5 ÷ 8",
                    "trickSteps" to run {
                        val x5 = kmh * 5
                        val r = x5 / 8
                        "$kmh × 5 = $x5\n$x5 ÷ 8 = $r"
                    },
                    "factor" to "0.6214",
                    "needleNorm" to (kmh.toDouble() / 210.0).coerceIn(0.0, 1.0).toString(),
                ),
            )
        }
    }

    private fun speedContext(mph: Int): String = when {
        mph <= 15 -> "School zone"
        mph <= 30 -> "City driving"
        mph <= 55 -> "Suburban road"
        mph <= 70 -> "Highway"
        mph <= 85 -> "Autobahn"
        else -> "Racing speed"
    }

    private fun speedContextKmh(kmh: Int): String = when {
        kmh <= 30 -> "School zone"
        kmh <= 50 -> "City driving"
        kmh <= 90 -> "Suburban road"
        kmh <= 120 -> "Highway"
        kmh <= 140 -> "Autobahn"
        else -> "Racing speed"
    }
}
