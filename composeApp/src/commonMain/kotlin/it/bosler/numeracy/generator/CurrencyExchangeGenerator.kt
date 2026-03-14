package it.bosler.numeracy.generator

import it.bosler.numeracy.model.Problem
import it.bosler.numeracy.model.ScenarioType
import kotlin.math.roundToInt
import kotlin.random.Random

class CurrencyExchangeGenerator : ProblemGenerator {

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
        val currency = currencies[Random.nextInt(currencies.size)]
        val eurAmount = listOf(10, 20, 25, 50, 75, 100, 150, 200, 250, 500)[Random.nextInt(10)]

        val converted = eurAmount * currency.rateToEur
        val answer = if (converted > 100) converted.roundToInt().toString()
        else {
            val rounded = (converted * 100).roundToInt() / 100.0
            formatAmount(rounded)
        }

        val rateStr = formatAmount(currency.rateToEur)
        // === LEARNING (hintEasy): Full multiplication walkthrough ===
        val hintEasy = buildFullBreakdown(eurAmount, currency.rateToEur, rateStr, currency.symbol, answer)

        // === PRACTICE (hintMedium): Strategy guidance ===
        val hintMedium = buildStrategyHint(eurAmount, currency.rateToEur, rateStr)

        val hintHard = ""

        // Practice mode helpers: break down the multiplication
        val wholeRate = currency.rateToEur.toInt()
        val fracRate = currency.rateToEur - wholeRate
        val wholeResult = eurAmount * wholeRate
        val practiceHint = if (currency.rateToEur < 1) {
            val pctOff = ((1.0 - currency.rateToEur) * 100).toInt()
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
            explanation = "€$eurAmount × ${currency.rateToEur} = ${currency.symbol}$answer",
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
                "hintEasy" to hintEasy,
                "hintMedium" to hintMedium,
                "hintHard" to hintHard,
                "tip" to buildTip(),
            ),
        )
    }

    private fun buildTip(): String =
        "Currency Conversion Tips:\n" +
        "• Round the rate to a nearby easy number first. Rate 1.47 → use 1.5 ('add half'). Rate 0.86 → use 0.9 ('subtract 10%').\n" +
        "• Split whole + fraction: amount × 1.08 = amount × 1 + amount × 0.08. E.g. €50 × 1.08 = 50 + 4 = 54.\n" +
        "• Build currency landmarks for the trip: memorise what €1, €5, €10, €50 equal, then scale for other amounts.\n" +
        "• Use powers-of-10 rates: rate ≈ 1.2 means +20%; rate ≈ 0.75 means ×3/4 (divide by 4 and multiply by 3).\n" +
        "• Large multipliers (e.g. ×162 for JPY): compute ×100 first, then ×60 (= ×6 × 10), then add. €10 × 162 = 1000 + 620 = 1620.\n" +
        "• Always verify direction: multiply when going from the '1' side, divide when going back."

    private fun buildFullBreakdown(eurAmount: Int, rate: Double, rateStr: String, symbol: String, answer: String): String {
        val wholeRate = rate.toInt()
        val fracRate = rate - wholeRate

        return buildString {
            append("€$eurAmount \u00D7 $rateStr:\n")

            if (rate < 1) {
                // Rate < 1: think of it as "subtract X%"
                val pctOff = ((1.0 - rate) * 100).toInt()
                val pctAmount = (eurAmount * (1.0 - rate)).let { formatAmount(it.toInt().toDouble()) }
                append("Rate < 1 → subtract ~$pctOff%\n")
                append("$pctOff% of $eurAmount ≈ $pctAmount\n")
                append("$eurAmount \u2212 $pctAmount = $answer\n")
            } else if (fracRate == 0.0) {
                // Whole rate
                if (eurAmount <= 10) {
                    append("$eurAmount \u00D7 $wholeRate = $answer")
                } else {
                    // Break amount into factors
                    val factor = when {
                        eurAmount % 100 == 0 -> 100
                        eurAmount % 50 == 0 -> 50
                        eurAmount % 10 == 0 -> 10
                        else -> eurAmount
                    }
                    if (factor < eurAmount) {
                        val multiplier = eurAmount / factor
                        val partial = factor * wholeRate
                        append("$factor \u00D7 $wholeRate = $partial\n")
                        append("$partial \u00D7 $multiplier = $answer")
                    } else {
                        append("$eurAmount \u00D7 $wholeRate = $answer")
                    }
                }
            } else {
                // Split whole + fraction
                val wholeContrib = eurAmount * wholeRate
                val fracContrib = eurAmount * fracRate
                val fracContribStr = formatAmount(fracContrib)
                append("Split: $eurAmount \u00D7 $wholeRate = $wholeContrib\n")
                append("       $eurAmount \u00D7 ${formatAmount(fracRate)} = $fracContribStr\n")
                append("Add: $wholeContrib + $fracContribStr = $answer")
            }
        }
    }

    private fun buildStrategyHint(eurAmount: Int, rate: Double, rateStr: String): String {
        return buildString {
            if (rate < 1) {
                val pctOff = ((1.0 - rate) * 100).toInt()
                append("Rate < 1: think \"subtract $pctOff%\"\n")
                append("Find $pctOff% of $eurAmount, then subtract from $eurAmount.\n")
                if (pctOff in listOf(6, 14)) {
                    append("Tip: $pctOff% ≈ ${pctOff / 2}% \u00D7 2. Find half first.")
                }
            } else {
                val wholeRate = rate.toInt()
                val fracRate = rate - wholeRate
                if (fracRate > 0) {
                    append("Split the rate: $rateStr = $wholeRate + ${formatAmount(fracRate)}\n")
                    append("Multiply by $wholeRate first, then add the fractional part.\n")
                    // Teach fractional shortcuts
                    val fracPct = (fracRate * 100).toInt()
                    when {
                        fracPct == 50 -> append("0.5 = half. Easy to add.")
                        fracPct == 25 -> append("0.25 = quarter. Divide by 4.")
                        fracPct == 10 -> append("0.1 = move decimal. $eurAmount → ${eurAmount / 10.0}")
                        fracPct in 1..9 -> append("Small fraction: ${formatAmount(fracRate)} ≈ ~${fracPct}%. Find ${fracPct}% of $eurAmount.")
                        else -> append("${formatAmount(fracRate)}: break further if needed.")
                    }
                } else {
                    append("Whole number rate. Break $eurAmount into easy parts if large.\n")
                    append("E.g. $eurAmount = ${eurAmount / 2} \u00D7 2, or find $eurAmount \u00D7 10 first and adjust.")
                }
            }
        }
    }

    private fun formatAmount(amount: Double): String {
        if (amount == amount.toLong().toDouble()) return amount.toLong().toString()
        val s = ((amount * 100).roundToInt() / 100.0).toString()
        val parts = s.split(".")
        return if (parts.size == 1) s
        else parts[0] + "." + parts[1].take(2).padEnd(2, '0')
    }
}
