package it.bosler.numeracy.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun ElapsedTimer(
    elapsedMillis: Long,
    modifier: Modifier = Modifier,
) {
    val totalSeconds = (elapsedMillis / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val tenths = ((elapsedMillis % 1000) / 100).toInt()

    val text = if (minutes > 0) {
        "$minutes:${seconds.toString().padStart(2, '0')}.$tenths"
    } else {
        "$seconds.$tenths"
    }

    Text(
        text = "${text}s",
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp,
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}
