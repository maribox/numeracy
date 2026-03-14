package it.bosler.numeracy.generator

import it.bosler.numeracy.model.Problem
import it.bosler.numeracy.model.ScenarioType
import kotlin.random.Random

class TimeZonesGenerator : ProblemGenerator {

    data class TimeZone(
        val name: String,
        val city: String,
        val offsetWinter: Int, // offset from UTC in minutes
        val offsetSummer: Int, // offset from UTC in minutes (DST)
    )

    private val timeZones = listOf(
        TimeZone("EST", "New York", -300, -240),
        TimeZone("CST", "Chicago", -360, -300),
        TimeZone("MST", "Denver", -420, -360),
        TimeZone("PST", "Los Angeles", -480, -420),
        TimeZone("GMT", "London", 0, 60),
        TimeZone("CET", "Berlin", 60, 120),
        TimeZone("EET", "Helsinki", 120, 180),
        TimeZone("IST", "Mumbai", 330, 330), // India doesn't observe DST
        TimeZone("JST", "Tokyo", 540, 540), // Japan doesn't observe DST
        TimeZone("AEST", "Sydney", 600, 660),
        TimeZone("NZST", "Auckland", 720, 780),
    )

    override fun generate(): Problem {
        val from = timeZones[Random.nextInt(timeZones.size)]
        var to = timeZones[Random.nextInt(timeZones.size)]
        while (to == from) {
            to = timeZones[Random.nextInt(timeZones.size)]
        }

        val isSummer = Random.nextBoolean()
        val season = if (isSummer) "summer" else "winter"

        val hour = Random.nextInt(6, 23)
        val minute = listOf(0, 15, 30, 45)[Random.nextInt(4)]

        val fromOffset = if (isSummer) from.offsetSummer else from.offsetWinter
        val toOffset = if (isSummer) to.offsetSummer else to.offsetWinter
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

        // === LEARNING (hintEasy): Full step-by-step ===
        val hintEasy = buildString {
            append("UTC offsets:\n")
            append("  ${from.city}: UTC$fromOffsetStr\n")
            append("  ${to.city}: UTC$toOffsetStr\n")
            append("Difference: $toOffsetStr \u2212 ($fromOffsetStr) = ")
            if (diffMins == 0) {
                append("${if (diffMinutes >= 0) "+" else ""}${diffMinutes / 60} hours\n")
            } else {
                append("${if (diffMinutes >= 0) "+" else ""}${diffMinutes / 60}h ${diffMins}m\n")
            }
            append("${formatTime(hour, minute)}")
            if (diffMinutes >= 0) {
                append(" + ${diffHours}h")
                if (diffMins > 0) append(" ${diffMins}m")
            } else {
                append(" \u2212 ${diffHours}h")
                if (diffMins > 0) append(" ${diffMins}m")
            }
            append(" = $answer")
            // Midnight crossing warning
            if ((hour * 60 + minute + diffMinutes) < 0 || (hour * 60 + minute + diffMinutes) >= 1440) {
                append("\n⚠ Crossed midnight! (±24h)")
            }
        }

        // === PRACTICE (hintMedium): Strategy without answer ===
        val hintMedium = buildString {
            append("${to.city} is $direction of ${from.city}.\n")
            append("→ $addOrSub ${diffHours}h")
            if (diffMins > 0) append(" ${diffMins}m")
            append(" from ${formatTime(hour, minute)}\n")
            // Teach hour arithmetic
            val resultRaw = hour + (if (diffMinutes >= 0) diffHours else -diffHours)
            if (resultRaw < 0) {
                append("Tip: negative hours → add 24. E.g. ${resultRaw} → ${resultRaw + 24}")
            } else if (resultRaw >= 24) {
                append("Tip: over 24 → subtract 24. E.g. $resultRaw → ${resultRaw - 24}")
            } else if (diffMins > 0 && minute + (if (diffMinutes >= 0) diffMins else -diffMins) >= 60) {
                append("Tip: minutes overflow → carry 1 hour")
            }
        }

        val hintHard = ""

        // Practice mode helper: offset difference as a readable string
        val offsetDiffDisplay = buildString {
            val sign = if (diffMinutes >= 0) "+" else "\u2212"
            append(sign)
            append("${diffHours}h")
            if (diffMins > 0) append(" ${diffMins}m")
        }

        return Problem(
            scenarioType = ScenarioType.TIME_ZONES,
            questionText = "It's ${formatTime(hour, minute)} in ${from.city} (${season} time).\n\nWhat time is it in ${to.city}?",
            correctAnswer = answer,
            inputType = it.bosler.numeracy.model.InputType.TIME,
            explanation = "${from.city} → ${to.city}: $diffStr → $answer",
            metadata = mapOf(
                "fromCity" to from.city,
                "toCity" to to.city,
                "time" to formatTime(hour, minute),
                "season" to season,
                // Practice mode helper
                "offsetDiff" to offsetDiffDisplay,
                "hintEasy" to hintEasy,
                "hintMedium" to hintMedium,
                "hintHard" to hintHard,
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
