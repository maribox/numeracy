package it.bosler.numeracy.util

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToLong

/*
 * Arithmetic the JVM library has and common Kotlin does not, for code that runs on the web and iOS
 * as well as Android and desktop.
 */

/** [degrees] in radians. */
fun toRadians(degrees: Double): Double = degrees * PI / 180.0

/** [value] rounded to one decimal place and always written with it: 3.0, 5.5, -0.4. */
fun oneDecimal(value: Double): String {
    val tenths = (value * 10).roundToLong()
    val sign = if (tenths < 0) "-" else ""
    return "$sign${abs(tenths) / 10}.${abs(tenths) % 10}"
}
