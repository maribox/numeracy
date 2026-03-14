package it.bosler.numeracy.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.bosler.numeracy.model.ScenarioType

@Composable
fun ScenarioInfoSheet(
    scenarioType: ScenarioType,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        when (scenarioType) {
            ScenarioType.DARTS -> DartsInfo()
            ScenarioType.BLACKJACK -> BlackjackInfo()
            ScenarioType.POT_ODDS -> PokerInfo()
            ScenarioType.OUTS_COUNTING -> OutsCountingInfo()
            ScenarioType.EQUITY -> EquityInfo()
            ScenarioType.IMPLIED_ODDS -> ImpliedOddsInfo()
            ScenarioType.MAKING_CHANGE -> MakingChangeInfo()
            ScenarioType.CURRENCY_EXCHANGE -> CurrencyExchangeInfo()
            ScenarioType.TIME_ZONES -> TimeZonesInfo()
            ScenarioType.LENGTH_CONVERSION -> LengthConversionInfo()
            ScenarioType.WEIGHT_CONVERSION -> WeightConversionInfo()
            ScenarioType.TEMPERATURE_CONVERSION -> TemperatureConversionInfo()
            ScenarioType.VOLUME_CONVERSION -> VolumeConversionInfo()
            ScenarioType.SPEED_CONVERSION -> SpeedConversionInfo()
            ScenarioType.DOOMSDAY -> DoomsdayInfo()
        }
    }
}

// ===== SHARED UI COMPONENTS =====

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            fontSize = 12.sp,
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp, top = 4.dp),
    )
}

@Composable
private fun SubTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.SemiBold,
        ),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 4.dp, top = 8.dp),
    )
}

@Composable
private fun BodyText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 20.sp,
        modifier = Modifier.padding(bottom = 4.dp),
    )
}

@Composable
private fun NumberedStep(number: Int, text: String) {
    Row(
        modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun BulletPoint(text: String) {
    Row(
        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp).fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = "\u2022",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 8.dp, top = 1.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ExampleBox(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(14.dp),
    ) {
        content()
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun MonoText(text: String) {
    // Syntax-highlighted monospace text for code blocks
    val numColor = Color(0xFF64B5F6)    // blue for numbers
    val opColor = Color(0xFFFFB74D)     // orange for operators/arrows
    val resultColor = Color(0xFF81C784) // green for results (after = or \u2714)
    val labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) // dimmed labels
    val defaultColor = MaterialTheme.colorScheme.onSurface

    val annotated = buildAnnotatedString {
        var i = 0
        val s = text
        // Detect if this line has a result marker (\u2714 or starts with "Answer:")
        val isResultLine = s.contains("\u2714") || s.trimStart().startsWith("Answer")

        while (i < s.length) {
            when {
                // Check mark
                s[i] == '\u2714' -> {
                    withStyle(SpanStyle(color = resultColor, fontWeight = FontWeight.Bold)) {
                        append("\u2714")
                    }
                    i++
                }
                // Arrow symbols
                s[i] == '\u2192' || s[i] == '\u2190' -> {
                    withStyle(SpanStyle(color = opColor)) {
                        append(s[i])
                    }
                    i++
                }
                // Operator symbols: +, -, \u00D7, \u00F7, =, \u2212, mod
                s[i] == '+' || s[i] == '\u00D7' || s[i] == '\u00F7' || s[i] == '\u2212' -> {
                    withStyle(SpanStyle(color = opColor, fontWeight = FontWeight.Bold)) {
                        append(s[i])
                    }
                    i++
                }
                s[i] == '=' -> {
                    withStyle(SpanStyle(color = opColor, fontWeight = FontWeight.Bold)) {
                        append("=")
                    }
                    i++
                }
                s[i] == '-' && i + 1 < s.length && s[i + 1].isDigit() && (i == 0 || !s[i - 1].isDigit()) -> {
                    // Negative number: treat minus as part of number
                    val start = i
                    i++ // skip minus
                    while (i < s.length && (s[i].isDigit() || s[i] == '.')) i++
                    val color = if (isResultLine) resultColor else numColor
                    withStyle(SpanStyle(color = color, fontWeight = FontWeight.Bold)) {
                        append(s.substring(start, i))
                    }
                }
                // Numbers (possibly with decimals)
                s[i].isDigit() -> {
                    val start = i
                    while (i < s.length && (s[i].isDigit() || s[i] == '.' || s[i] == ':')) i++
                    val color = if (isResultLine) resultColor else numColor
                    withStyle(SpanStyle(color = color, fontWeight = FontWeight.Bold)) {
                        append(s.substring(start, i))
                    }
                }
                // "mod" keyword
                i + 3 <= s.length && s.substring(i, i + 3) == "mod" -> {
                    withStyle(SpanStyle(color = opColor, fontWeight = FontWeight.Bold)) {
                        append("mod")
                    }
                    i += 3
                }
                // Day names (Mon, Tue, Wed, Thu, Fri, Sat, Sun, Monday-Sunday, Tuesday, etc.)
                else -> {
                    val dayMatch = dayNameAt(s, i)
                    if (dayMatch != null) {
                        withStyle(SpanStyle(color = Color(0xFFCE93D8), fontWeight = FontWeight.Bold)) {
                            append(dayMatch)
                        }
                        i += dayMatch.length
                    } else {
                        withStyle(SpanStyle(color = if (isResultLine) resultColor else defaultColor)) {
                            append(s[i])
                        }
                        i++
                    }
                }
            }
        }
    }

    Text(
        text = annotated,
        style = MaterialTheme.typography.bodySmall.copy(
            fontFamily = FontFamily.Monospace,
            lineHeight = 20.sp,
        ),
    )
}

private fun dayNameAt(s: String, i: Int): String? {
    val days = listOf(
        "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday",
        "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat",
    )
    for (d in days) {
        if (i + d.length <= s.length && s.substring(i, i + d.length) == d) {
            // Make sure it's not part of a longer word
            if (i + d.length < s.length && s[i + d.length].isLetter()) continue
            return d
        }
    }
    return null
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipRow(chips: List<Pair<String, String>>, color: Color = MaterialTheme.colorScheme.primaryContainer) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(bottom = 8.dp),
    ) {
        chips.forEach { (label, value) ->
            KeyValueChip(label, value, color)
        }
    }
}

@Composable
private fun KeyValueChip(label: String, value: String, color: Color = MaterialTheme.colorScheme.primaryContainer) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.3f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) {
                    append(label)
                }
                append("  ")
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                    append(value)
                }
            },
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 12.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
    )
}

// ===== DARTS =====

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DartsInfo() {
    SectionTitle("How Darts Scoring Works")
    BodyText(
        "In a standard 501 game, you start at 501 points and subtract the value of each throw. " +
        "The goal is to reach exactly zero. Each throw lands on a numbered segment (1\u201320) and can " +
        "be a Single (\u00D71), Double (\u00D72), or Triple (\u00D73). The bullseye is worth 50, " +
        "and the single bull (outer ring) is worth 25."
    )

    SectionDivider()

    SectionTitle("Values You Should Know by Heart")
    BodyText("Memorize these - they come up in almost every leg:")
    ChipRow(listOf(
        "T20" to "60", "T19" to "57", "T18" to "54", "T17" to "51", "T16" to "48",
        "D20" to "40", "D19" to "38", "D18" to "36", "D16" to "32", "D10" to "20",
        "Bull" to "50", "S.Bull" to "25",
    ))

    SectionDivider()

    SectionTitle("The Complement Method")
    BodyText(
        "This is the fastest way to subtract in your head. Instead of subtracting a digit directly, " +
        "you add its complement to 10 and carry 1 to the next column. The complement of a digit d is " +
        "simply 10 \u2212 d."
    )

    SubTitle("Why it works")
    BodyText(
        "Subtracting 7 from a ones digit is the same as adding 3 (the complement) and then subtracting " +
        "10, which means carrying 1 to the tens column. Your brain finds addition easier than subtraction, " +
        "so this is faster once you practice it."
    )

    SubTitle("Step-by-step example")
    ExampleBox {
        Column {
            MonoText("Score: 501, Throw: Triple 19 (= 57)")
            Spacer(modifier = Modifier.height(4.dp))
            MonoText("  501")
            MonoText("\u2212  57")
            MonoText("\u2500\u2500\u2500\u2500\u2500")
            Spacer(modifier = Modifier.height(4.dp))
            MonoText("Ones:  1 < 7 \u2192 complement of 7 = 3")
            MonoText("       3 + 1 = 4 \u2192 write 4, carry 1")
            MonoText("Tens:  0 \u2212 5 \u2212 1(carry) = 0 < 6")
            MonoText("       complement of 6 = 4")
            MonoText("       4 + 0 = 4 \u2192 write 4, carry 1")
            MonoText("Hundreds: 5 \u2212 0 \u2212 1(carry) = 4")
            Spacer(modifier = Modifier.height(4.dp))
            MonoText("Answer: 444 \u2714")
        }
    }

    SubTitle("Another example")
    ExampleBox {
        Column {
            MonoText("Score: 327, Throw: Double 18 (= 36)")
            Spacer(modifier = Modifier.height(4.dp))
            MonoText("  327")
            MonoText("\u2212  36")
            MonoText("\u2500\u2500\u2500\u2500\u2500")
            Spacer(modifier = Modifier.height(4.dp))
            MonoText("Ones:  7 \u2212 6 = 1 (no borrow needed)")
            MonoText("Tens:  2 < 3 \u2192 complement of 3 = 7")
            MonoText("       7 + 2 = 9 \u2192 write 9, carry 1")
            MonoText("Hundreds: 3 \u2212 0 \u2212 1(carry) = 2")
            Spacer(modifier = Modifier.height(4.dp))
            MonoText("Answer: 291 \u2714")
        }
    }

    SectionDivider()

    SectionTitle("The Round-and-Adjust Method")
    BodyText(
        "For larger throws, round to the nearest 10 and adjust. This is often faster for numbers " +
        "like 57, 54, 48 that are close to a round number."
    )

    ExampleBox {
        Column {
            MonoText("Score: 381, Throw: T19 = 57")
            MonoText("Round up: 57 \u2192 60")
            MonoText("381 \u2212 60 = 321")
            MonoText("Add back: 321 + 3 = 324 \u2714")
            Spacer(modifier = Modifier.height(8.dp))
            MonoText("Score: 265, Throw: T18 = 54")
            MonoText("Round up: 54 \u2192 60")
            MonoText("265 \u2212 60 = 205")
            MonoText("Add back: 205 + 6 = 211 \u2714")
        }
    }

    SectionDivider()

    SectionTitle("Practice Progression")
    BodyText("Build up your speed gradually:")
    NumberedStep(1, "Start with single-digit subtractions (Single throws). Get comfortable with ones-column subtraction.")
    NumberedStep(2, "Add doubles. Practice the complement method when the ones digit borrows.")
    NumberedStep(3, "Add triples. First multiply (e.g., 19\u00D73=57), then subtract. Practice both steps as one fluid motion.")
    NumberedStep(4, "Speed drill: try to answer within 3 seconds. Most competitive darts players can do this in under 2 seconds.")

    SectionDivider()

    SectionTitle("Pro Tips")
    BulletPoint("Three T20s = 180, the maximum score per turn. Most of your subtractions will involve multiples of 20.")
    BulletPoint("Learn the 60-step pattern: 501 \u2192 441 \u2192 381 \u2192 321 \u2192 261 \u2192 201 \u2192 141 \u2192 81. Each T20 drops you by 60.")
    BulletPoint("Bogey numbers (can't finish in 3 darts): 169, 168, 166, 165, 163, 162, 159. Know these to avoid them in strategy.")
    BulletPoint("When subtracting a number ending in 0 (like T20 = 60), you only need to handle the tens and hundreds columns.")

    Spacer(modifier = Modifier.height(16.dp))
}

// ===== BLACKJACK =====

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BlackjackInfo() {
    SectionTitle("Card Values")
    BodyText(
        "In Blackjack, every card has a point value. Number cards (2\u201310) are worth their face value. " +
        "Face cards (Jack, Queen, King) are each worth 10. An Ace is worth 11, but drops to 1 if counting " +
        "it as 11 would put your total over 21 (a \"bust\")."
    )
    ChipRow(listOf(
        "2\u201310" to "face value", "J, Q, K" to "10 each", "Ace" to "11 or 1",
    ))

    SectionDivider()

    SectionTitle("The Grouping Strategy")
    BodyText(
        "Don't add cards one by one. Instead, scan the hand and group cards that add up to convenient totals. " +
        "This is how casino dealers count quickly."
    )

    SubTitle("Pair to 10")
    BodyText("Look for pairs that sum to 10: 6+4, 7+3, 8+2, 9+1. Each pair is a clean 10.")

    SubTitle("Pair to 20")
    BodyText("Two face cards or any two cards summing to 20: K+Q = 20, K+10 = 20, J+J = 20.")

    SubTitle("Count face cards first")
    BodyText(
        "Since J, Q, and K are all 10, count how many face cards or tens you have, multiply by 10, " +
        "then add the rest. This reduces the problem to adding small numbers."
    )

    SectionDivider()

    SectionTitle("Worked Examples")

    ExampleBox {
        Column {
            MonoText("Hand: K \u2663  7 \u2665  3 \u2660  Q \u2666")
            Spacer(modifier = Modifier.height(4.dp))
            MonoText("Face cards: K + Q = 20")
            MonoText("Remaining: 7 + 3 = 10")
            MonoText("Total: 20 + 10 = 30 (bust!)")
        }
    }

    ExampleBox {
        Column {
            MonoText("Hand: 5 \u2665  A \u2660  6 \u2663  3 \u2666")
            Spacer(modifier = Modifier.height(4.dp))
            MonoText("Without Ace: 5 + 6 + 3 = 14")
            MonoText("With Ace as 11: 14 + 11 = 25 (bust)")
            MonoText("With Ace as 1:  14 + 1 = 15 \u2714")
        }
    }

    ExampleBox {
        Column {
            MonoText("Hand: 9 \u2660  A \u2665  A \u2663  8 \u2666")
            Spacer(modifier = Modifier.height(4.dp))
            MonoText("Cards: 9 + 8 = 17")
            MonoText("Two Aces: try first as 11 \u2192 17+11 = 28 (bust)")
            MonoText("Both as 1: 17 + 1 + 1 = 19 \u2714")
        }
    }

    ExampleBox {
        Column {
            MonoText("Hand: 4 \u2665  6 \u2660  J \u2663  2 \u2666  5 \u2665")
            Spacer(modifier = Modifier.height(4.dp))
            MonoText("Face cards: J = 10")
            MonoText("Group: 4 + 6 = 10")
            MonoText("Remaining: 2 + 5 = 7")
            MonoText("Total: 10 + 10 + 7 = 27 (bust)")
        }
    }

    SectionDivider()

    SectionTitle("Ace Decision Rule")
    BodyText("Simple rule for aces:")
    NumberedStep(1, "Add up everything except aces first.")
    NumberedStep(2, "If the non-ace total is 10 or less, count ONE ace as 11 (all others as 1).")
    NumberedStep(3, "If the non-ace total is 11 or more, count ALL aces as 1.")
    BodyText(
        "You can never use two aces as 11, that alone would be 22, an instant bust. So at most " +
        "one ace can be 11."
    )

    SectionDivider()

    SectionTitle("Speed Tips")
    BulletPoint("Scan for face cards first - they're visually distinct and each worth exactly 10.")
    BulletPoint("With 2 face cards, you start at 20. You just need to check if the remaining cards will bust you.")
    BulletPoint("Look for complementary pairs (3+7, 4+6, 5+5) before adding sequentially.")
    BulletPoint("Practice counting 5-card hands in under 2 seconds - that's dealer speed.")

    Spacer(modifier = Modifier.height(16.dp))
}

// ===== POKER =====

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PokerInfo() {
    SectionTitle("What Are Pot Odds?")
    BodyText(
        "Pot odds tell you what percentage of the time you need to win the hand to break even on a call. " +
        "If the pot odds are lower than your chance of winning (your equity), calling is profitable in the " +
        "long run. If the pot odds are higher than your equity, you should fold."
    )

    SectionDivider()

    SectionTitle("The Formula")

    ExampleBox {
        Column {
            Text(
                text = "Pot Odds % = Call \u00F7 (Pot + Call) \u00D7 100",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                ),
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }

    BodyText(
        "The pot is the money already in the middle. The call is what you need to add. " +
        "You divide your call by the total pot (existing pot + your call) to get the percentage."
    )

    SectionDivider()

    SectionTitle("Step-by-Step")
    NumberedStep(1, "Note the pot size (the money already in the middle).")
    NumberedStep(2, "Note the call amount (what your opponent bet, and what you need to put in).")
    NumberedStep(3, "Add them together to get the total pot.")
    NumberedStep(4, "Divide the call by the total pot.")
    NumberedStep(5, "Convert to a percentage - this is your break-even equity.")

    SectionDivider()

    SectionTitle("Worked Examples")

    ExampleBox {
        Column {
            MonoText("Pot: \$60, Call: \$20")
            Spacer(modifier = Modifier.height(4.dp))
            MonoText("Total pot: 60 + 20 = \$80")
            MonoText("Pot odds: 20/80 = 1/4 = 25%")
            MonoText("")
            MonoText("You need to win 25% of the time")
            MonoText("to break even on this call.")
        }
    }

    ExampleBox {
        Column {
            MonoText("Pot: \$150, Call: \$50")
            Spacer(modifier = Modifier.height(4.dp))
            MonoText("Total pot: 150 + 50 = \$200")
            MonoText("Pot odds: 50/200 = 1/4 = 25%")
        }
    }

    ExampleBox {
        Column {
            MonoText("Pot: \$40, Call: \$40")
            Spacer(modifier = Modifier.height(4.dp))
            MonoText("Total pot: 40 + 40 = \$80")
            MonoText("Pot odds: 40/80 = 1/2 = 50%")
            MonoText("")
            MonoText("A pot-sized bet always gives 50% odds.")
        }
    }

    SectionDivider()

    SectionTitle("Simplify the Fraction First")
    BodyText(
        "The key to fast calculation is simplifying the fraction before converting to a percentage. " +
        "Look for common factors:"
    )
    BulletPoint("\$30/\$150 \u2192 divide both by 30 \u2192 1/5 = 20%")
    BulletPoint("\$25/\$100 \u2192 divide both by 25 \u2192 1/4 = 25%")
    BulletPoint("\$15/\$90 \u2192 divide both by 15 \u2192 1/6 \u2248 17%")

    SectionDivider()

    SectionTitle("Common Ratios to Memorize")
    BodyText("These patterns come up constantly. Know them by heart:")
    ChipRow(listOf(
        "1/2" to "50%", "1/3" to "33%", "1/4" to "25%",
        "1/5" to "20%", "1/6" to "17%", "1/7" to "14%",
        "1/8" to "12.5%", "1/10" to "10%", "2/5" to "40%",
        "2/7" to "29%", "3/8" to "37.5%", "3/10" to "30%",
    ))

    SectionDivider()

    SectionTitle("Quick Estimation Tricks")
    BulletPoint("Half-pot bet \u2192 33% odds (you need to win 1 in 3).")
    BulletPoint("Pot-sized bet \u2192 50% odds (you need to win 1 in 2).")
    BulletPoint("Quarter-pot bet \u2192 20% odds (you need to win 1 in 5).")
    BulletPoint("Double-pot bet \u2192 67% odds, you almost always fold unless you have the nuts.")

    SectionDivider()

    SectionTitle("The Rule of 2 and 4")
    BodyText(
        "To estimate your equity (chance of winning), count your outs (cards that complete your hand):"
    )
    BulletPoint("With 2 cards to come (flop): outs \u00D7 4 \u2248 equity %")
    BulletPoint("With 1 card to come (turn): outs \u00D7 2 \u2248 equity %")
    BodyText(
        "Example: You have a flush draw (9 outs) on the flop. Equity \u2248 9 \u00D7 4 = 36%. " +
        "If the pot odds are 25%, calling is profitable because 36% > 25%."
    )

    Spacer(modifier = Modifier.height(16.dp))
}

// ===== OUTS COUNTING =====

@Composable
private fun OutsCountingInfo() {
    SectionTitle("What Are Outs?")
    BodyText(
        "Outs are the unseen cards that will complete your hand and likely win you the pot. " +
        "Counting outs accurately is the foundation of all poker math."
    )

    SectionDivider()

    SectionTitle("Common Draws and Their Outs")
    ExampleBox {
        Column {
            MonoText("Flush draw (4 suited)     = 9 outs")
            MonoText("Open-ended straight       = 8 outs")
            MonoText("Gutshot straight           = 4 outs")
            MonoText("Double gutshot             = 8 outs")
            MonoText("Two overcards              = 6 outs")
            MonoText("One overcard               = 3 outs")
            MonoText("Pocket pair \u2192 set          = 2 outs")
            MonoText("Two pair \u2192 full house      = 4 outs")
            MonoText("Set \u2192 full house/quads     = 7 outs")
        }
    }

    SectionDivider()

    SectionTitle("Combo Draws")
    BodyText("When you have multiple draws, add the outs but subtract any overlap (cards that complete both draws):")
    BulletPoint("Flush draw + gutshot: 9 + 4 \u2212 1 = 12 outs")
    BulletPoint("Flush draw + open-ended: 9 + 8 \u2212 2 = 15 outs")
    BulletPoint("Flush draw + two overcards: 9 + 6 = 15 outs (usually no overlap)")

    SectionDivider()

    SectionTitle("How to Count")
    NumberedStep(1, "Identify your draw type (flush? straight? overcards?).")
    NumberedStep(2, "Count cards of the needed rank/suit in the deck (13 per suit, 4 per rank).")
    NumberedStep(3, "Subtract cards you can already see (your hand + board).")
    NumberedStep(4, "For combo draws, add outs from each draw, then subtract cards counted twice.")

    Spacer(modifier = Modifier.height(16.dp))
}

// ===== EQUITY =====

@Composable
private fun EquityInfo() {
    SectionTitle("The Rule of 2 and 4")
    BodyText(
        "This shortcut lets you estimate your winning chance (equity) from your outs, " +
        "without a calculator. It's accurate within 1\u20132% for most situations."
    )

    SectionDivider()

    SectionTitle("The Rule")
    ExampleBox {
        Column {
            MonoText("Flop (2 cards to come):")
            MonoText("  Equity \u2248 outs \u00D7 4")
            MonoText("")
            MonoText("Turn (1 card to come):")
            MonoText("  Equity \u2248 outs \u00D7 2")
        }
    }

    SectionDivider()

    SectionTitle("When to Use Which")
    BulletPoint("Use \u00D74 only when your opponent is ALL IN on the flop (no more betting). You see both remaining cards for free.")
    BulletPoint("Use \u00D72 when there is more betting to come (most situations). You might face another bet on the turn.")
    BulletPoint("On the turn, always use \u00D72 (only one card left).")

    SectionDivider()

    SectionTitle("Quick Reference")
    ExampleBox {
        Column {
            MonoText("Outs   Flop(\u00D74)   Turn(\u00D72)")
            MonoText("  2       8%        4%")
            MonoText("  4      16%        8%")
            MonoText("  6      24%       12%")
            MonoText("  8      32%       16%")
            MonoText("  9      36%       18%")
            MonoText(" 12      48%       24%")
            MonoText(" 15      60%       30%")
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
}

// ===== IMPLIED ODDS =====

@Composable
private fun ImpliedOddsInfo() {
    SectionTitle("What Are Implied Odds?")
    BodyText(
        "Sometimes the pot odds don't justify a call right now, but you expect to win more money " +
        "on later streets when you hit your draw. Implied odds account for this future value."
    )

    SectionDivider()

    SectionTitle("The Formula")
    ExampleBox {
        Column {
            MonoText("Break-even total = Call \u00F7 Equity")
            MonoText("Need to win = Break-even \u2212 Current pot \u2212 Call")
        }
    }

    SectionDivider()

    SectionTitle("Worked Example")
    ExampleBox {
        Column {
            MonoText("Pot: \$80, Call: \$40, Equity: 18%")
            MonoText("")
            MonoText("Break-even total = \$40 \u00F7 0.18 = \$222")
            MonoText("Current total = \$80 + \$40 = \$120")
            MonoText("Need to win = \$222 \u2212 \$120 = \$102")
            MonoText("")
            MonoText("If you expect to win \$102+ on later")
            MonoText("streets when you hit, calling is +EV.")
        }
    }

    SectionDivider()

    SectionTitle("When Implied Odds Are Good")
    BulletPoint("Opponent has a deep stack (lots of money behind to win).")
    BulletPoint("Your draw is hidden (opponent won't see it coming, e.g., gutshot).")
    BulletPoint("Opponent tends to pay off big bets when they have a strong hand.")

    SectionDivider()

    SectionTitle("When Implied Odds Are Bad")
    BulletPoint("Short stacks: not enough money behind to win.")
    BulletPoint("Obvious draws: if a flush card hits, opponent may not pay you off.")
    BulletPoint("Multi-way pots: harder to extract value from multiple opponents.")

    Spacer(modifier = Modifier.height(16.dp))
}

// ===== MAKING CHANGE =====

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MakingChangeInfo() {
    SectionTitle("The Count-Up Method")
    BodyText(
        "Don't subtract. Trained cashiers count UP from the bill amount to the amount paid. " +
        "This is faster, less error-prone, and the same technique used in professional cashier training."
    )

    SectionDivider()

    SectionTitle("How It Works")
    NumberedStep(1, "Start at the bill amount.")
    NumberedStep(2, "Add coins to reach the next round euro amount.")
    NumberedStep(3, "Add euro bills to reach the amount paid.")
    NumberedStep(4, "The coins and bills you handed back ARE the change.")

    SectionDivider()

    SectionTitle("Worked Examples")

    ExampleBox {
        Column {
            MonoText("Bill: \u20AC7.35, Paid: \u20AC20")
            Spacer(modifier = Modifier.height(4.dp))
            MonoText("\u20AC7.35 \u2192 \u20AC7.40  (+5\u00A2)")
            MonoText("\u20AC7.40 \u2192 \u20AC7.50  (+10\u00A2)")
            MonoText("\u20AC7.50 \u2192 \u20AC8.00  (+50\u00A2)")
            MonoText("\u20AC8.00 \u2192 \u20AC10.00 (+\u20AC2)")
            MonoText("\u20AC10   \u2192 \u20AC20.00 (+\u20AC10)")
            Spacer(modifier = Modifier.height(4.dp))
            MonoText("Change: 5+10+50\u00A2 + \u20AC2 + \u20AC10 = \u20AC12.65")
        }
    }

    ExampleBox {
        Column {
            MonoText("Bill: \u20AC23.87, Paid: \u20AC50")
            Spacer(modifier = Modifier.height(4.dp))
            MonoText("\u20AC23.87 \u2192 \u20AC23.90 (+3\u00A2)")
            MonoText("\u20AC23.90 \u2192 \u20AC24.00 (+10\u00A2)")
            MonoText("\u20AC24.00 \u2192 \u20AC25.00 (+\u20AC1)")
            MonoText("\u20AC25.00 \u2192 \u20AC50.00 (+\u20AC25)")
            Spacer(modifier = Modifier.height(4.dp))
            MonoText("Change: 13\u00A2 + \u20AC1 + \u20AC25 = \u20AC26.13")
        }
    }

    SectionDivider()

    SectionTitle("The Complement Shortcut")
    BodyText(
        "For the cents part of the change, use the complement to 100. If the bill ends in .35, " +
        "the change cents = 100 \u2212 35 = 65 cents. Then handle the euros separately."
    )

    SubTitle("How to compute 100 \u2212 X quickly")
    BodyText("For a two-digit number X:")
    NumberedStep(1, "Subtract the ones digit from 10 \u2192 that's your cents ones digit.")
    NumberedStep(2, "Subtract the tens digit from 9 \u2192 that's your cents tens digit.")
    BodyText("Example: Bill is .73. Ones: 10\u22123 = 7. Tens: 9\u22127 = 2. Cents change = .27")

    ExampleBox {
        Column {
            MonoText("Complement examples:")
            MonoText("100 \u2212 35 = 65  (10\u22125=5, 9\u22123=6)")
            MonoText("100 \u2212 87 = 13  (10\u22127=3, 9\u22128=1)")
            MonoText("100 \u2212 42 = 58  (10\u22122=8, 9\u22124=5)")
            MonoText("100 \u2212 99 = 01  (10\u22129=1, 9\u22129=0)")
        }
    }

    SectionDivider()

    SectionTitle("Handling the Euros")
    BodyText(
        "Once you have the cents part, the euro part depends on whether the cents required a borrow:"
    )
    BulletPoint("If the bill cents are 0 (e.g., \u20AC23.00): no borrow. Euro change = paid euros \u2212 bill euros.")
    BulletPoint("If the bill has cents (e.g., \u20AC23.87): the complement \"uses up\" one euro. Euro change = paid euros \u2212 bill euros \u2212 1.")
    BodyText("Example: \u20AC23.87 from \u20AC50. Cents = 13\u00A2. Euros: 50 \u2212 23 \u2212 1 = 26. Total: \u20AC26.13.")

    SectionDivider()

    SectionTitle("Speed Tips")
    BulletPoint("Practice the complement to 100 until it's instant. This is the biggest bottleneck for beginners.")
    BulletPoint("For bills ending in .00 or .50, the math is trivial, skip straight to euros.")
    BulletPoint("Round the bill to the next euro, then the complement of the cents is what gets you there.")
    BulletPoint("With practice, you should be able to calculate change for any amount in under 3 seconds.")

    Spacer(modifier = Modifier.height(16.dp))
}

// ===== CURRENCY EXCHANGE =====

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CurrencyExchangeInfo() {
    SectionTitle("The Basic Idea")
    BodyText(
        "To convert euros to another currency, multiply the euro amount by the exchange rate. " +
        "The trick to doing this quickly in your head is to break the multiplication into easier parts."
    )

    SectionDivider()

    SectionTitle("Strategy 1: Split the Rate")
    BodyText(
        "Break the rate into a whole number part and a fractional part. Multiply each separately, " +
        "then add them together."
    )

    ExampleBox {
        Column {
            MonoText("\u20AC75 \u00D7 1.08 (USD)")
            Spacer(modifier = Modifier.height(4.dp))
            MonoText("Split: 1.08 = 1 + 0.08")
            MonoText("\u20AC75 \u00D7 1 = 75")
            MonoText("\u20AC75 \u00D7 0.08 = 75 \u00D7 8/100 = 6")
            MonoText("Total: 75 + 6 = \$81")
        }
    }

    ExampleBox {
        Column {
            MonoText("\u20AC50 \u00D7 11.2 (SEK)")
            Spacer(modifier = Modifier.height(4.dp))
            MonoText("Split: 11.2 = 11 + 0.2")
            MonoText("\u20AC50 \u00D7 11 = 550")
            MonoText("\u20AC50 \u00D7 0.2 = 10")
            MonoText("Total: 550 + 10 = 560 kr")
        }
    }

    SectionDivider()

    SectionTitle("Strategy 2: Percentage Adjustment")
    BodyText(
        "For rates close to 1 (like GBP or CHF), think of the rate as a percentage change from " +
        "the euro amount."
    )

    ExampleBox {
        Column {
            MonoText("\u20AC200 \u00D7 0.86 (GBP)")
            Spacer(modifier = Modifier.height(4.dp))
            MonoText("0.86 = 1 \u2212 0.14 = subtract 14%")
            MonoText("10% of 200 = 20")
            MonoText("4% of 200 = 8")
            MonoText("14% of 200 = 28")
            MonoText("200 \u2212 28 = \u00A3172")
        }
    }

    ExampleBox {
        Column {
            MonoText("\u20AC150 \u00D7 0.94 (CHF)")
            Spacer(modifier = Modifier.height(4.dp))
            MonoText("0.94 = 1 \u2212 0.06 = subtract 6%")
            MonoText("6% of 150 = 9")
            MonoText("150 \u2212 9 = CHF 141")
        }
    }

    SectionDivider()

    SectionTitle("Strategy 3: Anchor and Scale")
    BodyText(
        "For large rates (JPY, INR), find a convenient anchor and scale from there."
    )

    ExampleBox {
        Column {
            MonoText("\u20AC25 \u00D7 162 (JPY)")
            Spacer(modifier = Modifier.height(4.dp))
            MonoText("25 \u00D7 160 = 25 \u00D7 16 \u00D7 10")
            MonoText("25 \u00D7 16 = 400")
            MonoText("400 \u00D7 10 = 4000")
            MonoText("25 \u00D7 2 = 50")
            MonoText("Total: 4000 + 50 = \u00A54050")
        }
    }

    SectionDivider()

    SectionTitle("Key Exchange Rates (Approximate)")
    BodyText("These rates fluctuate, but knowing the ballpark helps you estimate quickly:")
    ChipRow(listOf(
        "USD" to "\u22481.08", "GBP" to "\u22480.86", "CHF" to "\u22480.94",
        "JPY" to "\u2248162", "SEK" to "\u224811.2", "NOK" to "\u224811.5",
        "PLN" to "\u22484.3", "CZK" to "\u224825", "TRY" to "\u224836",
        "AUD" to "\u22481.65", "CAD" to "\u22481.48", "INR" to "\u224890",
    ))

    SectionDivider()

    SectionTitle("Quick Mental Checks")
    BulletPoint("Rates > 1: the foreign amount should be BIGGER than the euro amount.")
    BulletPoint("Rates < 1: the foreign amount should be SMALLER.")
    BulletPoint("If the rate is near 1 (0.9\u20131.1), the amounts are nearly equal, just add or subtract a few percent.")
    BulletPoint("For rates like 11 or 25, think of it as multiplying by 10 or 25 and adjusting slightly.")

    Spacer(modifier = Modifier.height(16.dp))
}

// ===== TIME ZONES =====

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TimeZonesInfo() {
    SectionTitle("How Time Zones Work")
    BodyText(
        "The Earth is divided into 24 time zones, roughly one per hour. They're measured as offsets " +
        "from UTC (Coordinated Universal Time, based in Greenwich, London). Going east, time gets later. " +
        "Going west, time gets earlier."
    )

    SectionDivider()

    SectionTitle("Key City Offsets from UTC")
    BodyText("Standard / Daylight Saving Time (where applicable):")
    ChipRow(listOf(
        "London" to "0 / +1",
        "Berlin" to "+1 / +2",
        "Moscow" to "+3",
        "Dubai" to "+4",
        "Mumbai" to "+5:30",
        "Bangkok" to "+7",
        "Beijing" to "+8",
        "Tokyo" to "+9",
        "Sydney" to "+10 / +11",
        "Auckland" to "+12 / +13",
        "New York" to "\u22125 / \u22124",
        "Chicago" to "\u22126 / \u22125",
        "Denver" to "\u22127 / \u22126",
        "Los Angeles" to "\u22128 / \u22127",
    ))

    SectionDivider()

    SectionTitle("The Conversion Method")
    NumberedStep(1, "Find the UTC offset of the source city.")
    NumberedStep(2, "Find the UTC offset of the destination city.")
    NumberedStep(3, "Subtract: destination offset \u2212 source offset = time difference.")
    NumberedStep(4, "Add the difference to the source time.")

    SectionDivider()

    SectionTitle("Worked Examples")

    ExampleBox {
        Column {
            MonoText("Berlin (UTC+1) is 14:00.")
            MonoText("What time is it in New York (UTC\u22125)?")
            Spacer(modifier = Modifier.height(4.dp))
            MonoText("Difference: \u22125 \u2212 (+1) = \u22126 hours")
            MonoText("14:00 \u2212 6 = 08:00")
            MonoText("Answer: 08:00 in New York")
        }
    }

    ExampleBox {
        Column {
            MonoText("Tokyo (UTC+9) is 22:00.")
            MonoText("What time is it in London (UTC+0)?")
            Spacer(modifier = Modifier.height(4.dp))
            MonoText("Difference: 0 \u2212 9 = \u22129 hours")
            MonoText("22:00 \u2212 9 = 13:00")
            MonoText("Answer: 13:00 in London")
        }
    }

    ExampleBox {
        Column {
            MonoText("New York (UTC\u22125) is 20:00.")
            MonoText("What time is it in Mumbai (UTC+5:30)?")
            Spacer(modifier = Modifier.height(4.dp))
            MonoText("Difference: +5.5 \u2212 (\u22125) = +10.5 hours")
            MonoText("20:00 + 10:30 = 30:30")
            MonoText("30:30 \u2212 24 = 06:30 (next day)")
            MonoText("Answer: 06:30 (+1 day)")
        }
    }

    SectionDivider()

    SectionTitle("Common Shortcuts")
    BodyText("Memorize these direct differences instead of going through UTC:")
    BulletPoint("Berlin \u2194 New York: always 6 hours apart (both shift for DST at roughly the same time).")
    BulletPoint("London \u2194 New York: 5 hours apart.")
    BulletPoint("Berlin \u2194 Tokyo: 8 hours apart (Tokyo is ahead). Tokyo is 7 hours ahead in summer (Tokyo has no DST).")
    BulletPoint("US time zones are 1 hour apart: Eastern \u2192 Central \u2192 Mountain \u2192 Pacific.")

    SectionDivider()

    SectionTitle("Handling Midnight Crossings")
    BodyText("When your calculation crosses midnight:")
    BulletPoint("If the result is \u2265 24:00 \u2192 subtract 24 and add 1 day.")
    BulletPoint("If the result is < 0:00 \u2192 add 24 and subtract 1 day.")
    BodyText(
        "Example: It's 23:00 in Berlin, what time in Tokyo (+8h)? 23 + 8 = 31 \u2192 31 \u2212 24 = 07:00 next day."
    )

    SectionDivider()

    SectionTitle("DST Traps")
    BulletPoint("Not all countries observe DST. Japan, China, India, and most of Africa/Asia never change.")
    BulletPoint("Northern and Southern Hemisphere DST is opposite: when Europe springs forward, Australia falls back.")
    BulletPoint("The US and Europe switch DST on different dates, creating a few weeks each year where the usual offsets are off by 1 hour.")
    BulletPoint("When in doubt about DST, know the standard offset and note whether it's summer or winter in each location.")

    Spacer(modifier = Modifier.height(16.dp))
}

// ===== DOOMSDAY =====

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DoomsdayInfo() {
    SectionTitle("What Is the Doomsday Algorithm?")
    BodyText(
        "The Doomsday algorithm, invented by mathematician John Horton Conway, lets you calculate " +
        "the day of the week for any date in history, entirely in your head. It works by exploiting " +
        "the fact that certain easy-to-remember dates all fall on the same weekday every year. That shared " +
        "weekday is called the year's \"Doomsday.\""
    )
    BodyText(
        "Once you know the Doomsday for a given year, you can find the weekday for any date by counting " +
        "from the nearest anchor date. With practice, the whole process takes about 10 seconds."
    )

    SectionDivider()

    SectionTitle("Step 1: The Century Anchor")
    BodyText(
        "Each century has a fixed \"anchor day\": the Doomsday of every year ending in 00. The pattern " +
        "repeats every 400 years (because the Gregorian calendar has a 400-year cycle)."
    )
    ChipRow(
        listOf(
            "1800s" to "Friday",
            "1900s" to "Wednesday",
            "2000s" to "Tuesday",
            "2100s" to "Sunday",
        ),
        Color(0xFF26A69A).copy(alpha = 0.5f),
    )
    BodyText(
        "Mnemonic: remember the pattern Fri\u2013Wed\u2013Tue\u2013Sun, or think of it as " +
        "\"We-in-Tue-day\" for the 2000s (we live in the Tuesday century)."
    )

    SectionDivider()

    SectionTitle("Step 2: Find the Year's Doomsday")
    BodyText(
        "Take the last two digits of the year (call it yy) and apply the \"odd+11\" method or " +
        "the classic \"12s\" method:"
    )

    SubTitle("The 12s Method (Classic)")
    ExampleBox {
        Column {
            MonoText("a = yy \u00F7 12  (how many 12s fit)")
            MonoText("b = yy mod 12 (the remainder)")
            MonoText("c = b \u00F7 4    (how many 4s in remainder)")
            MonoText("")
            MonoText("Doomsday = century anchor + a + b + c")
            MonoText("         (all mod 7)")
        }
    }

    BodyText("Why this works: every 12 years, the calendar advances by exactly 1 weekday " +
        "(12 years = 12 regular years + 3 leap years = 15 extra days, but 14 is divisible by 7, " +
        "so the net shift is 1). The remainder b accounts for the additional years, and c accounts " +
        "for the leap years within those remaining years.")

    SubTitle("Worked example: 2024")
    ExampleBox {
        Column {
            MonoText("yy = 24")
            MonoText("a = 24 \u00F7 12 = 2")
            MonoText("b = 24 mod 12 = 0")
            MonoText("c = 0 \u00F7 4 = 0")
            MonoText("")
            MonoText("Century anchor (2000s) = Tuesday = 2")
            MonoText("Doomsday = 2 + 2 + 0 + 0 = 4")
            MonoText("4 mod 7 = 4 = Thursday")
            MonoText("")
            MonoText("So the Doomsday of 2024 is Thursday.")
        }
    }

    SubTitle("Worked example: 1969")
    ExampleBox {
        Column {
            MonoText("yy = 69")
            MonoText("a = 69 \u00F7 12 = 5")
            MonoText("b = 69 mod 12 = 9")
            MonoText("c = 9 \u00F7 4 = 2")
            MonoText("")
            MonoText("Century anchor (1900s) = Wednesday = 3")
            MonoText("Doomsday = 3 + 5 + 9 + 2 = 19")
            MonoText("19 mod 7 = 5 = Friday")
            MonoText("")
            MonoText("So the Doomsday of 1969 is Friday.")
        }
    }

    SubTitle("The Odd+11 Method (Alternative)")
    BodyText("Some people find this easier:")
    ExampleBox {
        Column {
            MonoText("1. Start with yy")
            MonoText("2. If odd, add 11")
            MonoText("3. Divide by 2")
            MonoText("4. If odd, add 11")
            MonoText("5. Take mod 7")
            MonoText("6. Subtract from century anchor (mod 7)")
        }
    }

    SectionDivider()

    SectionTitle("Step 3: Month Anchor Dates")
    BodyText(
        "These specific dates ALWAYS fall on the year's Doomsday. Memorize them:"
    )

    SubTitle("Even months: 4/4, 6/6, 8/8, 10/10, 12/12")
    BodyText(
        "For all even months, the date that matches the month number is a Doomsday. " +
        "April 4th, June 6th, August 8th, October 10th, December 12th. Easy!"
    )

    SubTitle("Odd months: \"I work 9-to-5 at 7-Eleven\"")
    BodyText(
        "For odd months (after January/February), use the mnemonic \"I work 9-to-5 at 7-Eleven\":"
    )
    BulletPoint("May 9th (month 5, day 9)")
    BulletPoint("July 11th (month 7, day 11 \u2192 \"7-Eleven\")")
    BulletPoint("September 5th (month 9, day 5 \u2192 \"9-to-5\")")
    BulletPoint("November 7th (month 11, day 7 \u2192 \"7-Eleven\" reversed)")

    SubTitle("January and February")
    BodyText("These two months are special because of leap years:")
    BulletPoint("January 3rd on regular years, January 4th on leap years.")
    BulletPoint("February 28th on regular years, February 29th on leap years.")
    BodyText("(Think: on leap years, both January and February anchors move forward by 1.)")

    SubTitle("March")
    BodyText("March 7th is always a Doomsday. Alternatively, the last day of February (Feb 28 or 29) is also a Doomsday, and March 0th = Feb 28/29.")

    ExampleBox {
        Column {
            MonoText("All Doomsday anchor dates:")
            MonoText("")
            MonoText("Jan  3 (or 4 in leap years)")
            MonoText("Feb 28 (or 29 in leap years)")
            MonoText("Mar  7    (also: Mar 14, 21, 28)")
            MonoText("Apr  4    Jun  6    Aug  8")
            MonoText("Oct 10    Dec 12")
            MonoText("May  9    Jul 11    Sep  5    Nov  7")
        }
    }

    SectionDivider()

    SectionTitle("Step 4: Count to Your Target Date")
    BodyText(
        "Find the anchor date in the same month as your target, then count the days forward or " +
        "backward to reach your target. Since you're counting mod 7, you only need to figure out " +
        "how many days away the target is from the anchor."
    )

    SectionDivider()

    SectionTitle("Full Worked Examples")

    SubTitle("Example 1: July 4th, 1776")
    ExampleBox {
        Column {
            MonoText("Century: 1700s \u2192 anchor = Sunday (0)")
            MonoText("(Pattern: ...Fri, Wed, Tue, Sun, Fri...)")
            MonoText("")
            MonoText("yy = 76")
            MonoText("a = 76 \u00F7 12 = 6")
            MonoText("b = 76 mod 12 = 4")
            MonoText("c = 4 \u00F7 4 = 1")
            MonoText("Doomsday = 0 + 6 + 4 + 1 = 11")
            MonoText("11 mod 7 = 4 = Thursday")
            MonoText("")
            MonoText("Month anchor: July 11 = Thursday")
            MonoText("Target: July 4 = 7 days before = Thursday")
            MonoText("")
            MonoText("July 4, 1776 was a Thursday! \u2714")
        }
    }

    SubTitle("Example 2: December 25, 2000")
    ExampleBox {
        Column {
            MonoText("Century: 2000s \u2192 anchor = Tuesday (2)")
            MonoText("")
            MonoText("yy = 00")
            MonoText("a = 0 \u00F7 12 = 0")
            MonoText("b = 0 mod 12 = 0")
            MonoText("c = 0 \u00F7 4 = 0")
            MonoText("Doomsday = 2 + 0 + 0 + 0 = 2 = Tuesday")
            MonoText("")
            MonoText("Month anchor: Dec 12 = Tuesday")
            MonoText("Target: Dec 25 = 13 days later")
            MonoText("13 mod 7 = 6 days later")
            MonoText("Tue + 6 = Monday")
            MonoText("")
            MonoText("December 25, 2000 was a Monday! \u2714")
        }
    }

    SubTitle("Example 3: March 15, 2023")
    ExampleBox {
        Column {
            MonoText("Century: 2000s \u2192 anchor = Tuesday (2)")
            MonoText("")
            MonoText("yy = 23")
            MonoText("a = 23 \u00F7 12 = 1")
            MonoText("b = 23 mod 12 = 11")
            MonoText("c = 11 \u00F7 4 = 2")
            MonoText("Doomsday = 2 + 1 + 11 + 2 = 16")
            MonoText("16 mod 7 = 2 = Tuesday")
            MonoText("")
            MonoText("Month anchor: Mar 7 = Tuesday")
            MonoText("Target: Mar 15 = 8 days later")
            MonoText("8 mod 7 = 1 day later")
            MonoText("Tue + 1 = Wednesday")
            MonoText("")
            MonoText("March 15, 2023 was a Wednesday! \u2714")
        }
    }

    SectionDivider()

    SectionTitle("Day Number Reference")
    BodyText("Use these numbers for weekday arithmetic:")
    ChipRow(listOf(
        "Sun" to "0", "Mon" to "1", "Tue" to "2", "Wed" to "3",
        "Thu" to "4", "Fri" to "5", "Sat" to "6",
    ))

    SectionDivider()

    SectionTitle("Leap Year Rules")
    BodyText("A year is a leap year if:")
    NumberedStep(1, "It's divisible by 4, AND")
    NumberedStep(2, "If divisible by 100, it must also be divisible by 400.")
    BodyText(
        "So 2000 was a leap year (div by 400), 1900 was NOT (div by 100 but not 400), and " +
        "2024 IS (div by 4, not by 100)."
    )

    SectionDivider()

    SectionTitle("Common Mistakes")
    BulletPoint("Forgetting to shift January and February anchors on leap years.")
    BulletPoint("Getting the century anchor wrong. Double-check with the Fri-Wed-Tue-Sun pattern.")
    BulletPoint("Miscounting the final step: remember to use mod 7, and count the right direction (forward for later dates, backward for earlier).")
    BulletPoint("The 12s method: don't forget the c term (leap year correction within the remainder).")

    Spacer(modifier = Modifier.height(16.dp))
}

// ═══════════════════════════════════════════
// LENGTH CONVERSION
// ═══════════════════════════════════════════

@Composable
private fun LengthConversionInfo() {
    SectionTitle("Length Conversions")
    BodyText("Convert between imperial and metric length units. Master the mental tricks to instantly estimate distances whether you're reading road signs, measuring rooms, or comparing screen sizes.")

    SectionDivider()
    SubTitle("Key Conversion Factors")
    ChipRow(listOf(
        "1 mile" to "1.609 km", "1 km" to "0.621 mi",
        "1 foot" to "0.305 m", "1 meter" to "3.28 ft",
        "1 inch" to "2.54 cm",
    ))

    SectionDivider()
    SubTitle("Miles \u2194 Kilometers")
    BodyText("The golden ratio trick: miles and km are related by approximately 1.6, which is close to the golden ratio (\u03C6). This means consecutive Fibonacci numbers approximate the conversion: 5 mi \u2248 8 km, 8 mi \u2248 13 km.")
    NumberedStep(1, "Multiply by 8")
    NumberedStep(2, "Divide by 5")
    ExampleBox {
        MonoText("87 miles \u2192 km:")
        MonoText("  87 \u00D7 8 = 696")
        MonoText("  696 \u00F7 5 = 139 km \u2713")
    }
    BodyText("For km to miles, reverse it: multiply by 5, divide by 8. Or simply multiply by 0.62.")

    SectionDivider()
    SubTitle("Feet \u2194 Meters")
    BodyText("There are about 3.28 feet per meter. For a quick estimate:")
    NumberedStep(1, "Divide feet by 3")
    NumberedStep(2, "Subtract about 5% for accuracy")
    ExampleBox {
        MonoText("100 feet \u2192 meters:")
        MonoText("  100 \u00F7 3 \u2248 33")
        MonoText("  33 - 5% \u2248 31 m \u2713")
    }

    SectionDivider()
    SubTitle("Inches \u2194 Centimeters")
    BodyText("1 inch = 2.54 cm. For mental math, just multiply by 2.5 (or \u00D75 \u00F72):")
    ExampleBox {
        MonoText("55 inches \u2192 cm:")
        MonoText("  55 \u00D7 5 = 275")
        MonoText("  275 \u00F7 2 = 138 cm \u2713")
    }

    Spacer(modifier = Modifier.height(16.dp))
}

// ═══════════════════════════════════════════
// WEIGHT CONVERSION
// ═══════════════════════════════════════════

@Composable
private fun WeightConversionInfo() {
    SectionTitle("Weight Conversions")
    BodyText("Convert between pounds/ounces and kilograms/grams. These come up constantly in cooking, fitness, and travel.")

    SectionDivider()
    SubTitle("Key Conversion Factors")
    ChipRow(listOf(
        "1 lb" to "0.454 kg", "1 kg" to "2.205 lb", "1 oz" to "28.35 g",
    ))

    SectionDivider()
    SubTitle("Pounds \u2192 Kilograms")
    BodyText("The \"halve and adjust\" method: since 1 kg \u2248 2.2 lb, dividing by 2 gives you roughly kg, but you're ~10% high:")
    NumberedStep(1, "Divide by 2")
    NumberedStep(2, "Subtract 10% of the result")
    ExampleBox {
        MonoText("176 lb \u2192 kg:")
        MonoText("  176 \u00F7 2 = 88")
        MonoText("  10% of 88 = 9")
        MonoText("  88 - 9 = 79 kg \u2713")
    }

    SectionDivider()
    SubTitle("Kilograms \u2192 Pounds")
    BodyText("Reverse the trick: double and add 10%:")
    NumberedStep(1, "Double the kg value")
    NumberedStep(2, "Add 10% of the doubled value")
    ExampleBox {
        MonoText("70 kg \u2192 lb:")
        MonoText("  70 \u00D7 2 = 140")
        MonoText("  10% of 140 = 14")
        MonoText("  140 + 14 = 154 lb \u2713")
    }

    SectionDivider()
    SubTitle("Ounces \u2194 Grams")
    BodyText("1 oz \u2248 28 g. For quick mental math, multiply by 30 and subtract a small bit (7%). Or just use \u00D728.")
    ExampleBox {
        MonoText("8 oz \u2192 grams:")
        MonoText("  8 \u00D7 30 = 240")
        MonoText("  240 - 7% \u2248 240 - 17 = 223 g")
        MonoText("  (exact: 227 g)")
    }

    Spacer(modifier = Modifier.height(16.dp))
}

// ═══════════════════════════════════════════
// TEMPERATURE CONVERSION
// ═══════════════════════════════════════════

@Composable
private fun TemperatureConversionInfo() {
    SectionTitle("Temperature Conversions")
    BodyText("Convert between Fahrenheit and Celsius. The exact formula is complex (\u00D79/5 + 32), but there are brilliant mental shortcuts that get you close enough for everyday use.")

    SectionDivider()
    SubTitle("Landmark Temperatures")
    BodyText("Memorize these anchor points and interpolate between them:")
    ChipRow(listOf(
        "0\u00B0C" to "32\u00B0F", "10\u00B0C" to "50\u00B0F",
        "20\u00B0C" to "68\u00B0F", "30\u00B0C" to "86\u00B0F",
        "37\u00B0C" to "98.6\u00B0F", "100\u00B0C" to "212\u00B0F",
    ))
    BodyText("Notice: every 10\u00B0C = 18\u00B0F. So each 5\u00B0C \u2248 9\u00B0F.")

    SectionDivider()
    SubTitle("Celsius \u2192 Fahrenheit")
    BodyText("The \"double, subtract 10%, add 32\" method:")
    NumberedStep(1, "Double the Celsius value")
    NumberedStep(2, "Subtract 10% of the doubled value")
    NumberedStep(3, "Add 32")
    ExampleBox {
        MonoText("25\u00B0C \u2192 \u00B0F:")
        MonoText("  25 \u00D7 2 = 50")
        MonoText("  50 - 10% = 50 - 5 = 45")
        MonoText("  45 + 32 = 77\u00B0F \u2713")
    }

    SectionDivider()
    SubTitle("Fahrenheit \u2192 Celsius")
    BodyText("Reverse: subtract 32, divide by 2, add 10%:")
    NumberedStep(1, "Subtract 32")
    NumberedStep(2, "Divide by 2")
    NumberedStep(3, "Add 10% of the result")
    ExampleBox {
        MonoText("77\u00B0F \u2192 \u00B0C:")
        MonoText("  77 - 32 = 45")
        MonoText("  45 \u00F7 2 = 22")
        MonoText("  22 + 10% \u2248 22 + 2 = 24\u00B0C")
        MonoText("  (exact: 25\u00B0C)")
    }

    SectionDivider()
    SubTitle("Quick Landmark Method")
    BodyText("Find the nearest landmark and adjust. Each 1\u00B0C \u2248 1.8\u00B0F:")
    ExampleBox {
        MonoText("33\u00B0C \u2192 \u00B0F:")
        MonoText("  Nearest: 30\u00B0C = 86\u00B0F")
        MonoText("  +3\u00B0C \u00D7 1.8 \u2248 +5\u00B0F")
        MonoText("  86 + 5 = 91\u00B0F \u2713")
    }

    Spacer(modifier = Modifier.height(16.dp))
}

// ═══════════════════════════════════════════
// VOLUME CONVERSION
// ═══════════════════════════════════════════

@Composable
private fun VolumeConversionInfo() {
    SectionTitle("Volume Conversions")
    BodyText("Convert between imperial volume units (gallons, cups, fluid ounces) and metric (liters, milliliters). Essential for cooking with international recipes and understanding fuel economy.")

    SectionDivider()
    SubTitle("Key Conversion Factors")
    ChipRow(listOf(
        "1 gal" to "3.785 L", "1 L" to "0.264 gal",
        "1 cup" to "237 mL", "1 fl oz" to "29.6 mL",
    ))

    SectionDivider()
    SubTitle("Gallons \u2194 Liters")
    BodyText("1 gallon is almost 4 liters (3.785 L). The trick: multiply by 4, subtract 5%.")
    NumberedStep(1, "Multiply gallons by 4")
    NumberedStep(2, "Subtract about 5% for precision")
    ExampleBox {
        MonoText("5 gallons \u2192 liters:")
        MonoText("  5 \u00D7 4 = 20")
        MonoText("  20 - 5% = 20 - 1 = 19 L \u2713")
    }
    BodyText("For liters to gallons: divide by 4, add a small bit.")

    SectionDivider()
    SubTitle("Cups \u2192 Milliliters")
    BodyText("1 cup \u2248 240 mL (a metric cup is 250 mL). For mental math:")
    ExampleBox {
        MonoText("3 cups \u2192 mL:")
        MonoText("  3 \u00D7 240 = 720 mL")
    }

    SectionDivider()
    SubTitle("Fluid Ounces \u2194 Milliliters")
    BodyText("1 fl oz \u2248 30 mL. This is the easiest conversion, just multiply by 30:")
    ExampleBox {
        MonoText("12 fl oz \u2192 mL:")
        MonoText("  12 \u00D7 30 = 360 mL \u2713")
        MonoText("  (exact: 355 mL)")
    }

    Spacer(modifier = Modifier.height(16.dp))
}

// ═══════════════════════════════════════════
// SPEED CONVERSION
// ═══════════════════════════════════════════

@Composable
private fun SpeedConversionInfo() {
    SectionTitle("Speed Conversions")
    BodyText("Convert between miles per hour and kilometers per hour. The conversion factor is exactly the same as for distance (1 mile = 1.609 km), so the same mental tricks apply.")

    SectionDivider()
    SubTitle("Key Conversion Factors")
    ChipRow(listOf(
        "1 mph" to "1.609 km/h", "1 km/h" to "0.621 mph",
    ))

    SectionDivider()
    SubTitle("mph \u2192 km/h")
    BodyText("Use the \u00D78 \u00F75 trick (same as miles to km):")
    NumberedStep(1, "Multiply by 8")
    NumberedStep(2, "Divide by 5")
    ExampleBox {
        MonoText("65 mph \u2192 km/h:")
        MonoText("  65 \u00D7 8 = 520")
        MonoText("  520 \u00F7 5 = 104 km/h \u2713")
    }

    SectionDivider()
    SubTitle("km/h \u2192 mph")
    BodyText("Reverse: \u00D75 \u00F78. Or multiply by 0.6:")
    ExampleBox {
        MonoText("100 km/h \u2192 mph:")
        MonoText("  100 \u00D7 5 = 500")
        MonoText("  500 \u00F7 8 = 62 mph \u2713")
    }

    SectionDivider()
    SubTitle("Common Speed Limits")
    BodyText("Memorize these pairs for quick reference:")
    ChipRow(listOf(
        "30 mph" to "48 km/h", "50 mph" to "80 km/h",
        "60 mph" to "97 km/h", "70 mph" to "113 km/h",
        "100 km/h" to "62 mph", "120 km/h" to "75 mph",
    ))

    SectionDivider()
    SubTitle("Fibonacci Trick")
    BodyText("Since 1.609 \u2248 \u03C6 (golden ratio), consecutive Fibonacci numbers approximate the conversion perfectly: 5\u21928, 8\u219213, 13\u219221, 21\u219234, 34\u219255, 55\u219289. For example, 55 mph \u2248 89 km/h.")

    Spacer(modifier = Modifier.height(16.dp))
}
