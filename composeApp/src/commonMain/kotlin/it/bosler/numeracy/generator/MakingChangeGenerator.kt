package it.bosler.numeracy.generator

import it.bosler.numeracy.model.Problem
import it.bosler.numeracy.model.ScenarioType
import kotlin.random.Random

class MakingChangeGenerator(private val rng: Random = Random.Default) : ProblemGenerator {

    override fun generate(): Problem {
        // Every amount is held in whole cents and only written out at the end: a euro amount held as
        // a Double is 12.619999... for 12.62, and truncating that misprints the bill and the change.
        val billCents = rng.nextInt(320, 4800) / 5 * 5
        val payment = PAYMENT_NOTES_CENTS.firstOrNull { it > billCents } ?: PAYMENT_NOTES_CENTS.last()
        val changeCents = payment - billCents

        val billStr = formatCents(billCents)
        val paymentStr = formatCents(payment)
        val changeStr = formatCents(changeCents)

        // Practice mode helpers: break change into cents and euros
        val centsPartOfChange = changeCents % 100
        val eurosPartOfChange = changeCents / 100

        return Problem(
            scenarioType = ScenarioType.MAKING_CHANGE,
            questionText = "The bill is €$billStr.\nThe customer pays with €$paymentStr.\n\nHow much change do you give back?",
            correctAnswer = changeStr,
            inputType = it.bosler.numeracy.model.InputType.MONEY,
            explanation = "€$paymentStr - €$billStr = €$changeStr",
            metadata = mapOf(
                "billAmount" to billStr,
                "paymentAmount" to paymentStr,
                // Practice mode helpers
                "centsChange" to centsPartOfChange.toString(),
                "eurosChange" to eurosPartOfChange.toString(),
            ),
        )
    }

    /** An amount of cents as euros to the cent: 1262 is "12.62", 500 is "5.00". */
    private fun formatCents(cents: Int): String = "${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"

    private companion object {
        /** The notes a customer hands over, smallest first; the first one above the bill is used. */
        val PAYMENT_NOTES_CENTS = listOf(500, 1000, 2000, 5000, 10000)
    }
}
