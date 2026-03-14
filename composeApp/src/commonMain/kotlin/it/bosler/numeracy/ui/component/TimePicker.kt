package it.bosler.numeracy.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TimeInput(
    hours: Int,
    minutes: Int,
    onHoursChange: (Int) -> Unit,
    onMinutesChange: (Int) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Hours
        TimeScroller(
            value = hours,
            range = 0..23,
            onValueChange = { if (enabled) onHoursChange(it) },
            enabled = enabled,
        )

        Text(
            text = ":",
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp),
        )

        // Minutes
        TimeScroller(
            value = minutes,
            range = 0..59,
            onValueChange = { if (enabled) onMinutesChange(it) },
            enabled = enabled,
        )
    }
}

@Composable
private fun TimeScroller(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    enabled: Boolean,
) {
    val textColor = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Up
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .then(
                    if (enabled) Modifier.clickable {
                        val next = if (value >= range.last) range.first else value + 1
                        onValueChange(next)
                    } else Modifier
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "\u25B2",
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
            )
        }

        // Value
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = value.toString().padStart(2, '0'),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }

        // Down
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .then(
                    if (enabled) Modifier.clickable {
                        val prev = if (value <= range.first) range.last else value - 1
                        onValueChange(prev)
                    } else Modifier
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "\u25BC",
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
            )
        }
    }
}
