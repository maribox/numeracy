package it.bosler.numeracy.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NumPad(
    value: String,
    onValueChange: (String) -> Unit,
    showDecimal: Boolean = false,
    enabled: Boolean = true,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val rows = if (showDecimal) {
        listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf(".", "0", "DEL"),
        )
    } else {
        listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "DEL"),
        )
    }

    val keyHeight = if (compact) 52.dp else 80.dp

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp),
    ) {
        for (row in rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp),
            ) {
                for (key in row) {
                    if (key.isEmpty()) {
                        Box(modifier = Modifier.weight(1f))
                    } else {
                        NumPadKey(
                            key = key,
                            keyHeight = keyHeight,
                            onClick = {
                                if (!enabled) return@NumPadKey
                                when (key) {
                                    "DEL" -> {
                                        if (value.isNotEmpty()) {
                                            onValueChange(value.dropLast(1))
                                        }
                                    }
                                    "." -> {
                                        if (!value.contains(".")) {
                                            onValueChange(if (value.isEmpty()) "0." else "$value.")
                                        }
                                    }
                                    else -> onValueChange(value + key)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = enabled,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NumPadKey(
    key: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    keyHeight: androidx.compose.ui.unit.Dp = 80.dp,
) {
    val bgColor = if (key == "DEL") {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    }
    val textColor = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    }

    Box(
        modifier = modifier
            .height(keyHeight)
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (key == "DEL") "\u232B" else key,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = if (key == "DEL") 32.sp else 30.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            color = textColor,
        )
    }
}
