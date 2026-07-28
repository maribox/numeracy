package it.bosler.numeracy.model

import androidx.compose.ui.graphics.Color

enum class ScenarioType(
    val category: Category,
    val displayName: String,
    val description: String,
    val gradientColors: Pair<Long, Long>,
    val availableDifficulties: List<Difficulty> = listOf(Difficulty.NORMAL, Difficulty.PRACTICE),
    val subcategory: String? = null,
    /** Expected median answer time in ms — used for fire bar speed boost scaling. */
    val expectedMedianTimeMs: Long = 4000L,
) {
    DARTS(
        category = Category.GAMES,
        displayName = "Darts",
        description = "Count down from 501",
        gradientColors = 0xFFC62828L to 0xFFEF5350L,
        availableDifficulties = listOf(Difficulty.HARD, Difficulty.NORMAL, Difficulty.PRACTICE, Difficulty.LEARNING),
        expectedMedianTimeMs = 3000L, // simple subtraction
    ),
    BLACKJACK(
        category = Category.GAMES,
        displayName = "Blackjack",
        description = "Calculate hand totals",
        gradientColors = 0xFF1B5E20L to 0xFF66BB6AL,
        availableDifficulties = listOf(Difficulty.NORMAL, Difficulty.PRACTICE),
        expectedMedianTimeMs = 2500L, // quick card addition
    ),
    DRAW_EQUITY(
        category = Category.GAMES,
        displayName = "Draw Equity",
        description = "Rule of 2 & 4",
        gradientColors = 0xFF4527A0L to 0xFF7E57C2L,
        availableDifficulties = listOf(Difficulty.HARD, Difficulty.NORMAL, Difficulty.PRACTICE, Difficulty.LEARNING),
        subcategory = "Poker",
        expectedMedianTimeMs = 5000L, // identify draw + multiply outs
    ),
    POT_ODDS(
        category = Category.GAMES,
        displayName = "Pot Odds",
        description = "Call ÷ (Pot + Call) × 100",
        gradientColors = 0xFF283593L to 0xFF5C6BC0L,
        availableDifficulties = listOf(Difficulty.HARD, Difficulty.NORMAL, Difficulty.PRACTICE, Difficulty.LEARNING),
        subcategory = "Poker",
        expectedMedianTimeMs = 6000L, // division + percentage
    ),
    MAKING_CHANGE(
        category = Category.WORK,
        displayName = "Making Change",
        description = "Give the right change back",
        gradientColors = 0xFF4E342EL to 0xFF8D6E63L,
        availableDifficulties = listOf(Difficulty.NORMAL, Difficulty.PRACTICE),
        expectedMedianTimeMs = 3000L, // subtraction from round number
    ),
    CURRENCY_EXCHANGE(
        category = Category.WORLD,
        displayName = "Currency Exchange",
        description = "Convert between currencies",
        gradientColors = 0xFFE65100L to 0xFFFF9800L,
        availableDifficulties = listOf(Difficulty.NORMAL, Difficulty.PRACTICE),
        expectedMedianTimeMs = 5000L, // multiply by rate
    ),
    TIME_ZONES(
        category = Category.WORLD,
        displayName = "Time Zones",
        description = "Convert times across zones",
        gradientColors = 0xFF0D47A1L to 0xFF42A5F5L,
        availableDifficulties = listOf(Difficulty.NORMAL, Difficulty.PRACTICE),
        expectedMedianTimeMs = 4000L, // add/subtract hours, handle wrap
    ),
    LENGTH_CONVERSION(
        category = Category.CONVERSIONS,
        displayName = "Length",
        description = "Miles, feet & inches",
        gradientColors = 0xFF00838FL to 0xFF4DD0E1L,
        availableDifficulties = listOf(Difficulty.NORMAL, Difficulty.PRACTICE, Difficulty.LEARNING),
        expectedMedianTimeMs = 4000L,
    ),
    WEIGHT_CONVERSION(
        category = Category.CONVERSIONS,
        displayName = "Weight",
        description = "Pounds, ounces & kg",
        gradientColors = 0xFF4E342EL to 0xFF8D6E63L,
        availableDifficulties = listOf(Difficulty.NORMAL, Difficulty.PRACTICE, Difficulty.LEARNING),
        expectedMedianTimeMs = 4000L,
    ),
    TEMPERATURE_CONVERSION(
        category = Category.CONVERSIONS,
        displayName = "Temperature",
        description = "Fahrenheit & Celsius",
        gradientColors = 0xFFC62828L to 0xFFFF8F00L,
        availableDifficulties = listOf(Difficulty.NORMAL, Difficulty.PRACTICE, Difficulty.LEARNING),
        expectedMedianTimeMs = 5000L, // multiply + offset
    ),
    VOLUME_CONVERSION(
        category = Category.CONVERSIONS,
        displayName = "Volume",
        description = "Gallons, cups & liters",
        gradientColors = 0xFF1565C0L to 0xFF42A5F5L,
        availableDifficulties = listOf(Difficulty.NORMAL, Difficulty.PRACTICE, Difficulty.LEARNING),
        expectedMedianTimeMs = 4000L,
    ),
    SPEED_CONVERSION(
        category = Category.CONVERSIONS,
        displayName = "Speed",
        description = "mph & km/h",
        gradientColors = 0xFF37474FL to 0xFF78909CL,
        availableDifficulties = listOf(Difficulty.NORMAL, Difficulty.PRACTICE, Difficulty.LEARNING),
        expectedMedianTimeMs = 4000L,
    ),
    DOOMSDAY(
        category = Category.MATH_TRICKS,
        displayName = "Doomsday",
        description = "Find the weekday for any date",
        gradientColors = 0xFF4A148CL to 0xFFAB47BCL,
        availableDifficulties = listOf(Difficulty.NORMAL, Difficulty.PRACTICE, Difficulty.LEARNING),
        expectedMedianTimeMs = 20000L, // multi-step algorithm
    ),
    SQUARING(
        category = Category.GENERAL_MATH,
        displayName = "Squaring",
        description = "Square any number",
        gradientColors = 0xFF283593L to 0xFF5C6BC0L,
        availableDifficulties = listOf(Difficulty.NORMAL, Difficulty.PRACTICE, Difficulty.LEARNING),
        expectedMedianTimeMs = 6000L, // multi-step mental math
    ),
    MULTIPLICATION(
        category = Category.GENERAL_MATH,
        displayName = "Multiplication",
        description = "Multiply two numbers",
        gradientColors = 0xFF1A237EL to 0xFF3F51B5L,
        availableDifficulties = listOf(Difficulty.NORMAL, Difficulty.PRACTICE, Difficulty.LEARNING),
        expectedMedianTimeMs = 5000L, // multi-step mental math
    );

    val startColor: Color get() = Color(gradientColors.first)
    val endColor: Color get() = Color(gradientColors.second)

    companion object {
        fun forCategory(category: Category): List<ScenarioType> =
            entries.filter { it.category == category }

        /** Top-level scenarios (no subcategory) for a category. */
        fun topLevelForCategory(category: Category): List<ScenarioType> =
            entries.filter { it.category == category && it.subcategory == null }

        /** All distinct subcategory names within a category. */
        fun subcategoriesForCategory(category: Category): List<String> =
            entries.filter { it.category == category && it.subcategory != null }
                .mapNotNull { it.subcategory }
                .distinct()

        /** Scenarios belonging to a specific subcategory within a category. */
        fun forSubcategory(category: Category, subcategory: String): List<ScenarioType> =
            entries.filter { it.category == category && it.subcategory == subcategory }
    }
}
