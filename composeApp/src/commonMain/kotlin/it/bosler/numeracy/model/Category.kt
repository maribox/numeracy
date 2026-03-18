package it.bosler.numeracy.model

import androidx.compose.ui.graphics.Color

enum class Category(
    val displayName: String,
    val description: String,
    val gradientColors: Pair<Long, Long>,
) {
    GAMES(
        displayName = "Games",
        description = "Darts, cards & more",
        gradientColors = 0xFF1B5E20L to 0xFF4CAF50L,
    ),
    WORK(
        displayName = "Work",
        description = "Everyday calculations",
        gradientColors = 0xFF0D47A1L to 0xFF42A5F5L,
    ),
    WORLD(
        displayName = "World",
        description = "Currencies & time zones",
        gradientColors = 0xFFE65100L to 0xFFFF9800L,
    ),
    CONVERSIONS(
        displayName = "Conversions",
        description = "Imperial & Metric",
        gradientColors = 0xFF00695CL to 0xFF26A69AL,
    ),
    MATH_TRICKS(
        displayName = "Math Tricks",
        description = "Impress your friends",
        gradientColors = 0xFF4A148CL to 0xFFAB47BCL,
    ),
    GENERAL_MATH(
        displayName = "General Math",
        description = "Multiply, square & more",
        gradientColors = 0xFF1A237EL to 0xFF5C6BC0L,
    );

    val startColor: Color get() = Color(gradientColors.first)
    val endColor: Color get() = Color(gradientColors.second)
}
