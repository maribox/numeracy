package it.bosler.numeracy.generator

import it.bosler.numeracy.model.Problem
import it.bosler.numeracy.model.ScenarioType
import kotlin.random.Random

class MakingChangeGenerator : ProblemGenerator {

    override fun generate(): Problem {
        val billCents = Random.nextInt(320, 4800)
        // Round to nearest 5 cents
        val roundedCents = (billCents / 5) * 5
        val billAmount = roundedCents / 100.0

        val paymentOptions = listOf(500, 1000, 2000, 5000, 10000)
        val payment = paymentOptions.first { it > roundedCents }
        val paymentAmount = payment / 100.0

        val changeCents = payment - roundedCents
        val changeAmount = changeCents / 100.0

        val billStr = formatMoney(billAmount)
        val paymentStr = formatMoney(paymentAmount)
        val changeStr = formatMoney(changeAmount)

        // === LEARNING (hintEasy): Full count-up walkthrough ===
        val hintEasy = buildDetailedCountUp(roundedCents, payment, billStr, paymentStr, changeStr)

        // === PRACTICE (hintMedium): Strategy without answer ===
        val hintMedium = buildCountUpStrategy(roundedCents, payment, billStr, paymentStr)

        val hintHard = ""

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
                "hintEasy" to hintEasy,
                "hintMedium" to hintMedium,
                "hintHard" to hintHard,
                "tip" to buildTip(),
            ),
        )
    }

    private fun buildTip(): String =
        "Making Change Tips:\n" +
        "• Count UP, not down: start at the bill total and count up to the amount paid. No subtraction needed.\n" +
        "• Steps: cents → whole euro → next round number → payment. E.g. €7.38 paid with €20: +€0.62→€8, +€2→€10, +€10→€20 = €12.62 change.\n" +
        "• Cents-to-euro bridge first: get to a whole euro amount before dealing with the larger bills.\n" +
        "• Round-number anchor: if the bill is €13.50 and payment is €20, think '€13.50 + €6.50 = €20'. The gap to the nearest €5/€10 is obvious.\n" +
        "• Double-check with the 'round and adjust' method: round the bill up to a whole euro, subtract from payment, then add back the overage."

    private fun buildDetailedCountUp(billCents: Int, paymentCents: Int, billStr: String, paymentStr: String, changeStr: String): String {
        return buildString {
            append("Count up from €$billStr to €$paymentStr:\n")
            var current = billCents
            val steps = mutableListOf<String>()

            // Step 1: Cents to next whole euro
            val billCentsPart = current % 100
            if (billCentsPart != 0) {
                val centsComplement = 100 - billCentsPart
                val nextEuro = current + centsComplement
                if (nextEuro <= paymentCents) {
                    append("  Cents complement: 100 \u2212 $billCentsPart = $centsComplement cents\n")
                    append("  €$billStr + €${formatMoney(centsComplement / 100.0)} → €${formatMoney(nextEuro / 100.0)}\n")
                    steps.add("+€${formatMoney(centsComplement / 100.0)}")
                    current = nextEuro
                }
            }

            // Step 2: Count up in €1, €2, €5, €10 steps
            val paymentEuros = paymentCents / 100
            val currentEuros = current / 100
            val euroGap = paymentEuros - currentEuros
            if (euroGap > 0) {
                val euroSteps = mutableListOf<Int>()
                var remaining = euroGap
                for (note in listOf(10, 5, 2, 1)) {
                    while (remaining >= note) {
                        euroSteps.add(note)
                        remaining -= note
                    }
                }
                append("  Euros: ${euroSteps.joinToString(" + ") { "€$it" }} = €$euroGap\n")
                append("  €${formatMoney(currentEuros.toDouble())} → €$paymentStr\n")
            }

            append("Change = €$changeStr")
        }
    }

    private fun buildCountUpStrategy(billCents: Int, paymentCents: Int, billStr: String, paymentStr: String): String {
        return buildString {
            val centsPart = billCents % 100
            if (centsPart != 0) {
                val complement = 100 - centsPart
                append("Cents: complement to 100. Bill ends in ${centsPart}¢ → change cents = ${complement}¢\n")
                append("(100 \u2212 $centsPart = $complement)\n")
            }
            val euroGap = paymentCents / 100 - (billCents + 99) / 100
            if (euroGap > 0) {
                append("Then count up €$euroGap in whole euros to reach €$paymentStr.\n")
            }
            append("Combine cents + euros for total change.")
        }
    }

    private fun formatMoney(amount: Double): String {
        val whole = amount.toLong()
        val cents = ((amount - whole) * 100).toLong()
        return if (cents == 0L) "$whole.00"
        else if (cents < 10) "$whole.0$cents"
        else "$whole.$cents"
    }
}
