package it.bosler.numeracy.generator

import it.bosler.numeracy.model.Problem
import it.bosler.numeracy.model.ScenarioType
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.random.Random

class CurrencyExchangeGenerator(private val rng: Random = Random.Default) : ProblemGenerator {

    data class Currency(
        val code: String,
        val name: String,
        val symbol: String,
        val rateToEur: Double, // how many of this currency per 1 EUR
    )

    private val currencies = listOf(
        Currency("USD", "US Dollar", "$", 1.08),
        Currency("GBP", "British Pound", "£", 0.86),
        Currency("JPY", "Japanese Yen", "¥", 162.0),
        Currency("CHF", "Swiss Franc", "CHF", 0.94),
        Currency("CAD", "Canadian Dollar", "C$", 1.47),
        Currency("AUD", "Australian Dollar", "A$", 1.66),
        Currency("SEK", "Swedish Krona", "kr", 11.20),
        Currency("NOK", "Norwegian Krone", "kr", 11.50),
        Currency("PLN", "Polish Złoty", "zł", 4.28),
        Currency("CZK", "Czech Koruna", "Kč", 25.10),
        Currency("TRY", "Turkish Lira", "₺", 34.50),
        Currency("BRL", "Brazilian Real", "R$", 5.30),
        Currency("INR", "Indian Rupee", "₹", 90.50),
        Currency("CNY", "Chinese Yuan", "¥", 7.85),
        Currency("KRW", "South Korean Won", "₩", 1420.0),
        Currency("MXN", "Mexican Peso", "MX$", 18.50),
        Currency("THB", "Thai Baht", "฿", 37.80),
        Currency("ZAR", "South African Rand", "R", 20.20),
    )

    override fun generate(): Problem {
        val currency = currencies[rng.nextInt(currencies.size)]
        val eurAmount = listOf(10, 20, 25, 50, 75, 100, 150, 200, 250, 500)[rng.nextInt(10)]

        // Rates carry at most two decimals, so the converted amount is a whole number of cents.
        val exactCents = (eurAmount * currency.rateToEur * 100).roundToLong()
        // Above a hundred the answer is asked in whole units, and the unrounded amount is right too.
        val inWholeUnits = exactCents > 100_00
        val answer = if (inWholeUnits) ((exactCents + 50) / 100).toString() else formatCents(exactCents)

        val rateStr = formatAmount(currency.rateToEur)
        // Practice mode helpers: break down the multiplication
        val wholeRate = currency.rateToEur.toInt()
        val fracRate = currency.rateToEur - wholeRate
        val wholeResult = eurAmount * wholeRate
        val practiceHint = if (currency.rateToEur < 1) {
            val pctOff = ((1.0 - currency.rateToEur) * 100).roundToInt()
            "subtract $pctOff%"
        } else if (fracRate > 0.001) {
            "×$wholeRate = $wholeResult, +×${formatAmount(fracRate)}"
        } else {
            "×$wholeRate"
        }

        return Problem(
            scenarioType = ScenarioType.CURRENCY_EXCHANGE,
            questionText = "Convert €$eurAmount to ${currency.name} (${currency.code}).\n\nRate: 1 EUR = ${formatAmount(currency.rateToEur)} ${currency.code}",
            correctAnswer = answer,
            inputType = it.bosler.numeracy.model.InputType.MONEY,
            explanation = "€$eurAmount × $rateStr ${if (exactCents % 100 == 0L || !inWholeUnits) "=" else "\u2248"} ${currency.symbol}$answer",
            absoluteTolerance = if (inWholeUnits) 0.5 else 0.0,
            metadata = mapOf(
                "fromAmount" to eurAmount.toString(),
                "fromCurrency" to "EUR",
                "toCurrencyCode" to currency.code,
                "toCurrencyName" to currency.name,
                "toCurrencySymbol" to currency.symbol,
                "rate" to formatAmount(currency.rateToEur),
                // Practice mode helpers
                "wholeResult" to wholeResult.toString(),
                "practiceHint" to practiceHint,
            ),
        )
    }

    /** Whole cents as an amount to the cent: 860 is "8.60". */
    private fun formatCents(cents: Long): String = "${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"

    private fun formatAmount(amount: Double): String {
        if (amount == amount.toLong().toDouble()) return amount.toLong().toString()
        val s = ((amount * 100).roundToInt() / 100.0).toString()
        val parts = s.split(".")
        return if (parts.size == 1) s
        else parts[0] + "." + parts[1].take(2).padEnd(2, '0')
    }
}
