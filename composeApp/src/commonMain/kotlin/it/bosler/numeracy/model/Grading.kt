package it.bosler.numeracy.model

import kotlin.math.abs

/** What an answer amounts to against a question. */
enum class Grade {
    /** The answer, however it was written: "8.6" answers "8.60". */
    EXACT,

    /** Inside the question's tolerance but not the answer: an estimate that counts. */
    CLOSE,
    WRONG,
}

/**
 * Grades [answer] against [problem]. Text is compared first, ignoring case, so weekdays and times
 * need nothing else; numbers are then compared by value, so trailing zeros and a leading plus do not
 * matter. A question with a percentage tolerance accepts anything within it, and never less than one
 * either side, since a percentage of a small number rounds to nothing.
 */
fun grade(answer: String, problem: Problem): Grade {
    val given = answer.trim()
    val expected = problem.correctAnswer.trim()
    if (given.isEmpty()) return Grade.WRONG
    if (given.equals(expected, ignoreCase = true)) return Grade.EXACT

    val givenValue = given.toNumberOrNull() ?: return Grade.WRONG
    val expectedValue = expected.toNumberOrNull() ?: return Grade.WRONG
    val apart = abs(givenValue - expectedValue)
    if (apart <= problem.absoluteTolerance + EQUAL_WITHIN) return Grade.EXACT

    if (problem.tolerancePercent > 0) {
        val allowed = maxOf(abs(expectedValue) * problem.tolerancePercent / 100.0, MINIMUM_TOLERANCE)
        if (apart <= allowed) return Grade.CLOSE
    }
    return Grade.WRONG
}

/**
 * Whether [typed] is as far as anyone would type before expecting the answer to be judged: as long as
 * the answer when it is a number, or exactly the answer written the way the question writes it.
 * Waiting for a key press on every question would slow the drill; judging a shorter number early
 * would mark "1" wrong on the way to "12".
 */
fun isComplete(typed: String, problem: Problem): Boolean = when (problem.inputType) {
    InputType.NUMBER -> typed.isNotEmpty() && typed != "-" && typed.length == problem.correctAnswer.length
    InputType.MONEY -> typed == problem.correctAnswer
    InputType.WEEKDAY -> typed.isNotEmpty()
    InputType.TIME -> false
}

/** A number as the keypad and a physical keyboard write it, with either minus sign. */
private fun String.toNumberOrNull(): Double? = replace('−', '-').removePrefix("+").toDoubleOrNull()

/** Two amounts closer than this are the same amount written differently. */
private const val EQUAL_WITHIN = 1e-6

private const val MINIMUM_TOLERANCE = 1.0
