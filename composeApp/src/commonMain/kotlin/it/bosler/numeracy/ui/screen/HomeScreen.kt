package it.bosler.numeracy.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.bosler.numeracy.model.Category
import it.bosler.numeracy.ui.component.CardBackgroundImage
import it.bosler.numeracy.ui.component.categoryImageRes

@Composable
fun HomeScreen(
    onCategorySelected: (Category) -> Unit,
    onSettingsSelected: () -> Unit = {},
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        val isLandscape = maxWidth > maxHeight

        if (isLandscape) {
            // Landscape: title on left, grid on right
            Row(
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(
                    modifier = Modifier
                        .weight(0.35f)
                        .fillMaxSize()
                        .padding(start = 24.dp, end = 16.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Numeracy",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 36.sp,
                                letterSpacing = (-1).sp,
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        GearButton(onClick = onSettingsSelected)
                    }
                    Text(
                        text = "Train your brain",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .weight(0.65f)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(Category.entries.toList()) { category ->
                        CategoryCard(
                            category = category,
                            onClick = { onCategorySelected(category) },
                            height = 120,
                        )
                    }
                }
            }
        } else {
            // Portrait: standard vertical layout
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Numeracy",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 36.sp,
                            letterSpacing = (-1).sp,
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    GearButton(onClick = onSettingsSelected)
                }
                Text(
                    text = "Train your brain",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(Category.entries.toList()) { category ->
                        CategoryCard(
                            category = category,
                            onClick = { onCategorySelected(category) },
                            height = 180,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: Category,
    onClick: () -> Unit,
    height: Int = 180,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(
                Brush.linearGradient(
                    colors = listOf(category.startColor, category.endColor),
                )
            )
            .clickable(onClick = onClick),
    ) {
        CardBackgroundImage(
            imageRes = categoryImageRes(category),
            gradientColor = category.startColor,
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(18.dp),
        ) {
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                ),
                color = Color.White,
            )
            Text(
                text = category.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
private fun GearButton(onClick: () -> Unit) {
    val iconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(22.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val outerR = size.width / 2f
            val innerR = outerR * 0.55f
            val toothR = outerR * 0.82f
            val strokeW = size.width * 0.1f

            // Center circle
            drawCircle(
                color = iconColor,
                radius = innerR,
                center = Offset(cx, cy),
                style = Stroke(width = strokeW),
            )

            // Gear teeth (6 lines radiating outward)
            val teeth = 6
            for (i in 0 until teeth) {
                val angle = Math.toRadians((i * 360.0 / teeth) - 90.0)
                val cos = kotlin.math.cos(angle).toFloat()
                val sin = kotlin.math.sin(angle).toFloat()
                drawLine(
                    color = iconColor,
                    start = Offset(cx + toothR * 0.65f * cos, cy + toothR * 0.65f * sin),
                    end = Offset(cx + outerR * cos, cy + outerR * sin),
                    strokeWidth = strokeW * 1.8f,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}
