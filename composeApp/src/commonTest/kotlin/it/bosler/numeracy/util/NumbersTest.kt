package it.bosler.numeracy.util

import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals

class NumbersTest {

    @Test
    fun oneDecimalRoundsAndAlwaysShowsTheTenths() {
        assertEquals("1.0", oneDecimal(1.0))
        assertEquals("3.5", oneDecimal(3.46))
        assertEquals("6.0", oneDecimal(5.99))
        assertEquals("0.0", oneDecimal(0.04))
        assertEquals("-0.4", oneDecimal(-0.4))
    }

    @Test
    fun toRadiansIsDegreesTimesPiOver180() {
        assertEquals(PI, toRadians(180.0), 1e-12)
        assertEquals(-PI / 2, toRadians(-90.0), 1e-12)
    }
}
