package it.bosler.numeracy.util

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS uses its own swipe-back gesture, no intercept needed
}
