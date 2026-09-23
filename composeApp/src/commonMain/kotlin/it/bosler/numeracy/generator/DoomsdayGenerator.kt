package it.bosler.numeracy.generator

import it.bosler.numeracy.model.Problem
import it.bosler.numeracy.model.ScenarioType
import kotlin.random.Random

class DoomsdayGenerator(private val rng: Random = Random.Default) : ProblemGenerator {

    private val dayNames = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    private val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    override fun generate(): Problem {
        val year = rng.nextInt(1900, 2100)
        val month = rng.nextInt(1, 13)
        val maxDay = daysInMonth(month, year)
        val day = rng.nextInt(1, maxDay + 1)

        val dayOfWeek = calculateDayOfWeek(year, month, day)
        val answer = dayNames[dayOfWeek]

        val monthName = monthNames[month - 1]
        val daySuffix = getDaySuffix(day)

        val doomsdayAnchor = doomsdayForYear(year)
        val doomsdayName = dayNames[doomsdayAnchor]
        val doomsdayRef = doomsdayReferenceDay(month, year)
        val diffFromRef = ((day - doomsdayRef) % 7 + 7) % 7
        val centuryAnchor = centuryAnchorName(year)

        return Problem(
            scenarioType = ScenarioType.DOOMSDAY,
            questionText = "What day of the week is\n\n$monthName $day$daySuffix, $year?",
            correctAnswer = answer,
            inputType = it.bosler.numeracy.model.InputType.WEEKDAY,
            explanation = "$monthName $day, $year is a $answer",
            metadata = buildMap {
                put("day", day.toString())
                put("month", month.toString())
                put("monthName", monthName)
                put("year", year.toString())
                // Practice/Learning mode helpers
                put("centuryAnchor", centuryAnchor)
                put("centuryLabel", "${year / 100 * 100}s")
                put("yearDoomsday", doomsdayName)
                put("monthAnchorDate", "${doomsdayRef}${getDaySuffix(doomsdayRef)}")
                put("monthAnchorDay", doomsdayName)
                // Structured year calculation steps for practice mode
                val yy = year % 100
                val a = yy / 12
                val b = yy % 12
                val c = b / 4
                val sum = a + b + c
                put("yy", yy.toString())
                put("yyDiv12", a.toString())
                put("yyRemainder", b.toString())
                put("remainderDiv4", c.toString())
                put("yearCalcSum", sum.toString())
                put("centuryAnchorIndex", dayNames.indexOf(centuryAnchor).toString())
                put("doomsdayRef", doomsdayRef.toString())
                put("diffFromRef", diffFromRef.toString())
                put("monthMnemonic", monthMnemonic(month, year))
            },
        )
    }

    // Returns the doomsday weekday index (0=Sun…6=Sat) for a given year
    private fun doomsdayForYear(year: Int): Int {
        val century = year / 100
        val yy = year % 100
        // Century anchor: 1800→Fri(5), 1900→Wed(3), 2000→Tue(2), 2100→Sun(0)
        val centuryAnchor = ((5 * (century % 4) + 2) % 7 + 7) % 7
        val a = yy / 12
        val b = yy % 12
        val c = b / 4
        return (centuryAnchor + a + b + c) % 7
    }

    // Returns the canonical doomsday reference day of the month for use as anchor
    private fun doomsdayReferenceDay(month: Int, year: Int): Int = when (month) {
        1 -> if (isLeapYear(year)) 4 else 3
        2 -> if (isLeapYear(year)) 29 else 28
        3 -> 7
        4 -> 4
        5 -> 9
        6 -> 6
        7 -> 11
        8 -> 8
        9 -> 5
        10 -> 10
        11 -> 7
        12 -> 12
        else -> 1
    }

    private fun monthMnemonic(month: Int, year: Int): String = when (month) {
        1 -> if (isLeapYear(year)) "Jan 4th in leap years" else "Jan 3rd in common years"
        2 -> if (isLeapYear(year)) "Last day of Feb (29th)" else "Last day of Feb (28th)"
        3 -> "3/7, \"3 out of 7 days\""
        4, 6, 8, 10, 12 -> "Even months: 4/4, 6/6, 8/8, 10/10, 12/12"
        5, 9, 7, 11 -> "\"I work 9-to-5 at 7-Eleven\""
        else -> ""
    }

    private fun centuryAnchorName(year: Int): String {
        val century = year / 100
        return when (century % 4) {
            0 -> "Tuesday"   // e.g. 2000s
            1 -> "Sunday"    // e.g. 2100s
            2 -> "Friday"    // e.g. 1800s
            3 -> "Wednesday" // e.g. 1900s
            else -> "Tuesday"
        }
    }

    // Zeller's congruence - returns 0=Sunday, 1=Monday, ..., 6=Saturday
    private fun calculateDayOfWeek(year: Int, month: Int, day: Int): Int {
        var y = year
        var m = month
        if (m < 3) {
            m += 12
            y -= 1
        }
        val k = y % 100
        val j = y / 100
        val h = (day + (13 * (m + 1)) / 5 + k + k / 4 + j / 4 - 2 * j) % 7
        // Convert Zeller's result (0=Saturday) to 0=Sunday
        return ((h + 6) % 7 + 7) % 7
    }

    private fun isLeapYear(year: Int): Boolean =
        (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

    private fun daysInMonth(month: Int, year: Int): Int = when (month) {
        2 -> if (isLeapYear(year)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }

    private fun getDaySuffix(day: Int): String = when {
        day in 11..13 -> "th"
        day % 10 == 1 -> "st"
        day % 10 == 2 -> "nd"
        day % 10 == 3 -> "rd"
        else -> "th"
    }
}
