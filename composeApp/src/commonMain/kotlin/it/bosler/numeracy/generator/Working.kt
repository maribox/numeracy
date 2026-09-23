package it.bosler.numeracy.generator

import kotlin.math.abs
import kotlin.math.roundToInt

/*
 * The arithmetic a hint writes out. A learner copies the working, so a step written with "=" has to
 * be true: a division that does not come out even is written with "≈" and rounded, never truncated.
 */

/** [a] ÷ [b], rounded to the nearest whole number. */
internal fun roundedQuotient(a: Int, b: Int): Int = (a.toDouble() / b).roundToInt()

/** "a ÷ b = c" when [b] divides [a], "a ÷ b ≈ c" with c rounded when it does not. */
internal fun divisionStep(a: Int, b: Int): String =
    if (a % b == 0) "$a ÷ $b = ${a / b}" else "$a ÷ $b ≈ ${roundedQuotient(a, b)}"

/** "=" when [shown] is [exact], "≈" when it is [exact] rounded. */
internal fun relation(exact: Double, shown: Number): String = if (abs(exact - shown.toDouble()) < 1e-9) "=" else "≈"

/** "x + n" or "x − n" for adding a signed [n], so adding a negative never reads "x + -4". */
internal fun plusSigned(x: Int, n: Int): String = if (n >= 0) "$x + $n" else "$x − ${-n}"

/** "x − n" or "x + n" for subtracting a signed [n], so taking away a negative never reads "x - -4". */
internal fun minusSigned(x: Int, n: Int): String = if (n >= 0) "$x − $n" else "$x + ${-n}"
