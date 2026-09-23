package it.bosler.numeracy.util

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * The time the screens read, in ms. The app's own clock unless a caller supplies another, which is
 * how a screen that changes with time is drawn the same way twice.
 */
val LocalClock = staticCompositionLocalOf<() -> Long> { ::currentTimeMillis }
