package it.bosler.numeracy.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GradingTest {

    private fun problem(
        answer: String,
        input: InputType = InputType.NUMBER,
        tolerance: Double = 0.0,
        absolute: Double = 0.0,
    ) = Problem(ScenarioType.DARTS, "?", answer, input, tolerancePercent = tolerance, absoluteTolerance = absolute)

    @Test
    fun theAnswerAsWrittenIsExact() {
        assertEquals(Grade.EXACT, grade("441", problem("441")))
        assertEquals(Grade.EXACT, grade(" 441 ", problem("441")))
        assertEquals(Grade.EXACT, grade("thursday", problem("Thursday", InputType.WEEKDAY)))
        assertEquals(Grade.EXACT, grade("14:30", problem("14:30", InputType.TIME)))
    }

    @Test
    fun theSameAmountWrittenDifferentlyIsExact() {
        assertEquals(Grade.EXACT, grade("8.6", problem("8.60", InputType.MONEY)))
        assertEquals(Grade.EXACT, grade("5", problem("5.00", InputType.MONEY)))
        assertEquals(Grade.EXACT, grade("-4", problem("-4")))
        assertEquals(Grade.EXACT, grade("−4", problem("-4")))
    }

    @Test
    fun aRoundedAmountIsAlsoExactAtItsUnroundedValue() {
        val euros250at785 = problem("1963", InputType.MONEY, absolute = 0.5)
        assertEquals(Grade.EXACT, grade("1962.5", euros250at785))
        assertEquals(Grade.EXACT, grade("1963", euros250at785))
        assertEquals(Grade.WRONG, grade("1961", euros250at785))
    }

    @Test
    fun aToleranceAcceptsEstimatesAsClose() {
        val inches72 = problem("183", tolerance = 5.0)
        assertEquals(Grade.CLOSE, grade("184", inches72))
        assertEquals(Grade.CLOSE, grade("174", inches72))
        assertEquals(Grade.WRONG, grade("173", inches72))
        // Five percent of a small number rounds to nothing, so one either side always counts.
        assertEquals(Grade.CLOSE, grade("1", problem("0", tolerance = 5.0)))
        assertEquals(Grade.CLOSE, grade("-5", problem("-4", tolerance = 5.0)))
    }

    @Test
    fun withoutAToleranceNearIsWrong() {
        assertEquals(Grade.WRONG, grade("442", problem("441")))
        assertEquals(Grade.WRONG, grade("", problem("441")))
        assertEquals(Grade.WRONG, grade("-", problem("-4")))
        assertEquals(Grade.WRONG, grade("Monday", problem("Thursday", InputType.WEEKDAY)))
    }

    @Test
    fun wholeNumbersAreJudgedWhenAsLongAsTheAnswer() {
        assertFalse(isComplete("4", problem("441")))
        assertTrue(isComplete("442", problem("441")))
        assertFalse(isComplete("-", problem("-4")))
        assertTrue(isComplete("-4", problem("-4")))
    }

    @Test
    fun amountsWaitForSubmitUnlessTypedAsTheAnswerIs() {
        // "1962" is on its way to "1962.50", not a wrong "1963"; only Submit judges it.
        assertFalse(isComplete("1962", problem("1963", InputType.MONEY, absolute = 0.5)))
        assertTrue(isComplete("12.65", problem("12.65", InputType.MONEY)))
        assertFalse(isComplete("12.6", problem("12.65", InputType.MONEY)))
    }

    @Test
    fun timesAreJudgedOnlyBySubmit() {
        assertFalse(isComplete("14:30", problem("14:30", InputType.TIME)))
    }
}
