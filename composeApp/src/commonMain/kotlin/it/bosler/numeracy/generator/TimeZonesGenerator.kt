package it.bosler.numeracy.generator

import it.bosler.numeracy.model.Problem
import it.bosler.numeracy.model.ScenarioType
import kotlin.random.Random

class TimeZonesGenerator(private val rng: Random = Random.Default) : ProblemGenerator {

    /**
     * A city's offset from UTC, in minutes, in January and in July. Clocks go forward in their own
     * summer: the northern cities in July, Sydney and Auckland in January, India and Japan never.
     * Mid-January and mid-July lie clear of every changeover, where the US and Europe differ by weeks.
     */
    data class TimeZone(
        val city: String,
        val offsetJanuary: Int,
        val offsetJuly: Int,
    )

    private val timeZones = listOf(
        TimeZone("New York", -300, -240),
        TimeZone("Chicago", -360, -300),
        TimeZone("Denver", -420, -360),
        TimeZone("Los Angeles", -480, -420),
        TimeZone("London", 0, 60),
        TimeZone("Berlin", 60, 120),
        TimeZone("Helsinki", 120, 180),
        TimeZone("Mumbai", 330, 330),
        TimeZone("Tokyo", 540, 540),
        TimeZone("Sydney", 660, 600),
        TimeZone("Auckland", 780, 720),
    )

    override fun generate(): Problem {
        val from = timeZones[rng.nextInt(timeZones.size)]
        var to = timeZones[rng.nextInt(timeZones.size)]
        while (to == from) {
            to = timeZones[rng.nextInt(timeZones.size)]
        }

        val inJuly = rng.nextBoolean()
        val month = if (inJuly) "July" else "January"

        val hour = rng.nextInt(6, 23)
        val minute = listOf(0, 15, 30, 45)[rng.nextInt(4)]

        val fromOffset = if (inJuly) from.offsetJuly else from.offsetJanuary
        val toOffset = if (inJuly) to.offsetJuly else to.offsetJanuary
        val diffMinutes = toOffset - fromOffset

        var resultMinutes = hour * 60 + minute + diffMinutes
        // Wrap around midnight
        while (resultMinutes < 0) resultMinutes += 1440
        while (resultMinutes >= 1440) resultMinutes -= 1440

        val resultHour = resultMinutes / 60
        val resultMin = resultMinutes % 60
        val answer = formatTime(resultHour, resultMin)

        val fromOffsetStr = formatOffset(fromOffset)
        val toOffsetStr = formatOffset(toOffset)
        val diffStr = formatDiff(diffMinutes)

        val absDiffMinutes = kotlin.math.abs(diffMinutes)
        val diffHours = absDiffMinutes / 60
        val diffMins = absDiffMinutes % 60
        val direction = if (diffMinutes >= 0) "ahead" else "behind"
        val addOrSub = if (diffMinutes >= 0) "add" else "subtract"

        // Practice mode helper: offset difference as a readable string
        val offsetDiffDisplay = buildString {
            val sign = if (diffMinutes >= 0) "+" else "\u2212"
            append(sign)
            append("${diffHours}h")
            if (diffMins > 0) append(" ${diffMins}m")
        }

        return Problem(
            scenarioType = ScenarioType.TIME_ZONES,
            questionText = "It's ${formatTime(hour, minute)} in ${from.city} in $month.\n\nWhat time is it in ${to.city}?",
            correctAnswer = answer,
            inputType = it.bosler.numeracy.model.InputType.TIME,
            explanation = "${from.city} → ${to.city}: $diffStr → $answer",
            metadata = mapOf(
                "fromCity" to from.city,
                "toCity" to to.city,
                "time" to formatTime(hour, minute),
                "month" to month,
                // Practice mode helper
                "offsetDiff" to offsetDiffDisplay,
            ),
        )
    }

    private fun formatOffset(offsetMinutes: Int): String {
        val sign = if (offsetMinutes >= 0) "+" else "-"
        val abs = kotlin.math.abs(offsetMinutes)
        return if (abs % 60 == 0) "$sign${abs / 60}"
        else "$sign${abs / 60}:${(abs % 60).toString().padStart(2, '0')}"
    }

    private fun formatDiff(diffMinutes: Int): String {
        return if (diffMinutes % 60 == 0) {
            val h = diffMinutes / 60
            if (h >= 0) "+$h hours" else "$h hours"
        } else {
            val sign = if (diffMinutes >= 0) "+" else "-"
            val absDiff = kotlin.math.abs(diffMinutes)
            "${sign}${absDiff / 60}h ${absDiff % 60}m"
        }
    }

    private fun formatTime(hour: Int, minute: Int): String {
        val h = hour.toString().padStart(2, '0')
        val m = minute.toString().padStart(2, '0')
        return "$h:$m"
    }
}
