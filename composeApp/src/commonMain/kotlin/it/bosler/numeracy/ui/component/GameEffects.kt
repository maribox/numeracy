package it.bosler.numeracy.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

// ─── Particle data ────────────────────────────────────────────────

private data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float,
    val rotation: Float,
    val rotationSpeed: Float,
    val shape: ParticleShape = ParticleShape.RECT,
)

private enum class ParticleShape { RECT, CIRCLE, DIAMOND, STAR }

// ─── Color palettes ───────────────────────────────────────────────

private val confettiColors = listOf(
    Color(0xFFFF1744), Color(0xFFFF9100), Color(0xFFFFEA00),
    Color(0xFF00E676), Color(0xFF2979FF), Color(0xFFD500F9),
    Color(0xFFFF4081), Color(0xFF00E5FF), Color(0xFF76FF03),
    Color(0xFFFF6D00), Color(0xFF448AFF), Color(0xFFE040FB),
)

private val flameColors = listOf(
    Color(0xFFFF6D00), Color(0xFFFF9100), Color(0xFFFFAB00),
    Color(0xFFFFD600), Color(0xFFFF3D00), Color(0xFFDD2C00),
)

private val sparkColors = listOf(
    Color(0xFFFFD600), Color(0xFFFFAB00), Color(0xFFFFFFFF),
    Color(0xFFFFF176), Color(0xFFFFE57F),
)

// ─── Confetti ─────────────────────────────────────────────────────

@Composable
fun ConfettiEffect(trigger: Int) {
    if (trigger == 0) return

    val progress = remember(trigger) { Animatable(0f) }
    val particles = remember(trigger) {
        val shapes = ParticleShape.entries
        List(120) {
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val speed = Random.nextFloat() * 1000f + 300f
            Particle(
                x = 0.5f + (Random.nextFloat() - 0.5f) * 0.2f,
                y = 0.35f + (Random.nextFloat() - 0.5f) * 0.1f,
                vx = cos(angle) * speed,
                vy = sin(angle) * speed - 500f,
                color = confettiColors.random(),
                size = Random.nextFloat() * 12f + 4f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = Random.nextFloat() * 1080f - 540f,
                shape = shapes.random(),
            )
        }
    }

    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(2000, easing = LinearEasing))
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val t = progress.value
        val gravity = 1400f

        particles.forEach { p ->
            val px = (p.x * size.width) + p.vx * t
            val py = (p.y * size.height) + p.vy * t + 0.5f * gravity * t * t
            // Fade out in the last 40%
            val alpha = ((1f - t) / 0.4f).coerceIn(0f, 1f)
            val rot = p.rotation + p.rotationSpeed * t

            if (py < size.height + 50f && py > -50f && alpha > 0f) {
                val c = p.color.copy(alpha = alpha)
                rotate(rot, pivot = Offset(px, py)) {
                    when (p.shape) {
                        ParticleShape.RECT -> drawRect(
                            color = c,
                            topLeft = Offset(px - p.size / 2f, py - p.size * 0.3f),
                            size = Size(p.size, p.size * 0.6f),
                        )
                        ParticleShape.CIRCLE -> drawCircle(
                            color = c, radius = p.size / 2f, center = Offset(px, py),
                        )
                        ParticleShape.DIAMOND -> {
                            val half = p.size / 2f
                            val path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(px, py - half)
                                lineTo(px + half * 0.6f, py)
                                lineTo(px, py + half)
                                lineTo(px - half * 0.6f, py)
                                close()
                            }
                            drawPath(path, c)
                        }
                        ParticleShape.STAR -> {
                            drawCircle(color = c, radius = p.size * 0.35f, center = Offset(px, py))
                            // Cross sparkle lines
                            val arm = p.size * 0.5f
                            drawLine(c, Offset(px - arm, py), Offset(px + arm, py), strokeWidth = 1.5f)
                            drawLine(c, Offset(px, py - arm), Offset(px, py + arm), strokeWidth = 1.5f)
                        }
                    }
                }
            }
        }
    }
}

// ─── Flame burst ──────────────────────────────────────────────────

@Composable
fun FlameEffect(trigger: Int) {
    if (trigger == 0) return

    val progress = remember(trigger) { Animatable(0f) }
    val particles = remember(trigger) {
        List(60) {
            val angle = -PI.toFloat() / 2f + (Random.nextFloat() - 0.5f) * 1.4f
            val speed = Random.nextFloat() * 600f + 250f
            Particle(
                x = 0.5f + (Random.nextFloat() - 0.5f) * 0.4f,
                y = 0.9f,
                vx = cos(angle) * speed * 0.35f,
                vy = sin(angle) * speed,
                color = flameColors.random(),
                size = Random.nextFloat() * 18f + 6f,
                rotation = 0f,
                rotationSpeed = 0f,
            )
        }
    }

    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(1000, easing = LinearEasing))
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val t = progress.value

        particles.forEach { p ->
            val px = (p.x * size.width) + p.vx * t
            val py = (p.y * size.height) + p.vy * t
            val alpha = (1f - t * 1.1f).coerceIn(0f, 1f)
            val currentSize = p.size * (1f - t * 0.4f)

            if (alpha > 0f) {
                // Outer glow
                drawCircle(
                    color = p.color.copy(alpha = alpha * 0.3f),
                    radius = currentSize * 2f,
                    center = Offset(px, py),
                )
                // Core
                drawCircle(
                    color = p.color.copy(alpha = alpha * 0.85f),
                    radius = currentSize,
                    center = Offset(px, py),
                )
                // Hot center
                drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.4f),
                    radius = currentSize * 0.4f,
                    center = Offset(px, py),
                )
            }
        }
    }
}

// ─── Shockwave ring on correct answer ─────────────────────────────

@Composable
fun ShockwaveEffect(trigger: Int) {
    if (trigger == 0) return

    val progress = remember(trigger) { Animatable(0f) }

    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(500, easing = EaseOutCubic))
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val t = progress.value
        val maxRadius = size.minDimension * 0.6f
        val radius = maxRadius * t
        val alpha = (1f - t).coerceIn(0f, 1f) * 0.35f
        val strokeW = (8f * (1f - t)).coerceAtLeast(1f)

        if (alpha > 0f) {
            drawCircle(
                color = Color(0xFF4CAF50).copy(alpha = alpha),
                radius = radius,
                center = Offset(size.width / 2f, size.height * 0.4f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW),
            )
        }
    }
}

// ─── Screen flash (green correct / red wrong) ─────────────────────

@Composable
fun ScreenFlash(trigger: Int, color: Color) {
    if (trigger == 0) return

    val progress = remember(trigger) { Animatable(0f) }

    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(400, easing = EaseOutCubic))
    }

    val alpha = ((1f - progress.value) * 0.15f).coerceIn(0f, 1f)

    if (alpha > 0.001f) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = color.copy(alpha = alpha))
        }
    }
}

// ─── Combo text ("NICE!", "GREAT!", etc.) ─────────────────────────

private val comboMessages = listOf(
    3 to "Nice!",
    5 to "Great!",
    7 to "Amazing!",
    10 to "On Fire!",
    15 to "Unstoppable!",
    20 to "Legendary!",
    25 to "Godlike!",
)

@Composable
fun ComboText(streak: Int, trigger: Int) {
    if (trigger == 0 || streak < 3) return

    val message = comboMessages.lastOrNull { streak >= it.first }?.second ?: return

    val scaleAnim = remember(trigger) { Animatable(0f) }
    val alphaAnim = remember(trigger) { Animatable(0f) }
    val offsetAnim = remember(trigger) { Animatable(0f) }

    LaunchedEffect(trigger) {
        // Pop in
        scaleAnim.snapTo(0.3f)
        alphaAnim.snapTo(1f)
        offsetAnim.snapTo(0f)

        launch { scaleAnim.animateTo(1f, tween(300, easing = FastOutSlowInEasing)) }
        delay(600)
        launch { alphaAnim.animateTo(0f, tween(400)) }
        launch { offsetAnim.animateTo(-40f, tween(400)) }
    }

    if (alphaAnim.value > 0f) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp,
                ),
                color = when {
                    streak >= 15 -> Color(0xFFFF1744)
                    streak >= 10 -> Color(0xFFFF6D00)
                    streak >= 5 -> Color(0xFFFFAB00)
                    else -> Color(0xFF4CAF50)
                }.copy(alpha = alphaAnim.value),
                modifier = Modifier
                    .scale(scaleAnim.value)
                    .offset { IntOffset(0, offsetAnim.value.toInt() - 80) },
            )
        }
    }
}

// ─── Floating points popup ────────────────────────────────────────

@Composable
fun PointsPopup(
    points: Int,
    trigger: Int,
    streak: Int = 0,
    modifier: Modifier = Modifier,
) {
    if (trigger == 0 || points == 0) return

    val scaleAnim = remember(trigger) { Animatable(0f) }
    val alphaAnim = remember(trigger) { Animatable(0f) }
    val offsetAnim = remember(trigger) { Animatable(0f) }

    LaunchedEffect(trigger) {
        scaleAnim.snapTo(1.8f)
        alphaAnim.snapTo(1f)
        offsetAnim.snapTo(0f)

        launch { scaleAnim.animateTo(1f, tween(200, easing = FastOutSlowInEasing)) }
        delay(500)
        launch { offsetAnim.animateTo(-100f, tween(500, easing = EaseOutCubic)) }
        launch { alphaAnim.animateTo(0f, tween(500)) }
    }

    if (alphaAnim.value > 0f) {
        val bonusText = if (streak >= 3) " \u00D7${1.0 + (streak - 1) * 0.1}" else ""
        Text(
            text = "+$points$bonusText",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
            ),
            color = Color(0xFFFFD600).copy(alpha = alphaAnim.value),
            modifier = modifier
                .scale(scaleAnim.value)
                .offset { IntOffset(0, offsetAnim.value.toInt()) },
        )
    }
}

// ─── Ambient background sparkles (always on in game mode) ─────────

@Composable
fun AmbientSparkles(streak: Int) {
    val transition = rememberInfiniteTransition()
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )

    // More sparkles at higher streaks
    val count = (8 + streak.coerceAtMost(20) * 2).coerceAtMost(50)
    val sparkles = remember(count) {
        List(count) {
            Triple(
                Offset(Random.nextFloat(), Random.nextFloat()), // position
                Random.nextFloat() * 0.6f + 0.2f, // phase offset
                Random.nextFloat() * 3f + 1.5f, // size
            )
        }
    }

    // Streak glow color along bottom edge
    val streakAlpha by animateFloatAsState(
        targetValue = if (streak >= 3) (streak.coerceAtMost(15) / 15f) * 0.2f else 0f,
        animationSpec = tween(600),
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Bottom-edge streak glow
        if (streakAlpha > 0f) {
            val glowColor = when {
                streak >= 15 -> Color(0xFFFF1744)
                streak >= 10 -> Color(0xFFFF6D00)
                streak >= 5 -> Color(0xFFFFAB00)
                else -> Color(0xFFFF6D00)
            }
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, glowColor.copy(alpha = streakAlpha)),
                    startY = size.height * 0.7f,
                    endY = size.height,
                ),
            )
        }

        // Floating sparkles
        sparkles.forEach { (pos, phaseOffset, sparkSize) ->
            val t = (phase + phaseOffset) % 1f
            // Pulse: fade in, shine, fade out
            val sparkAlpha = if (t < 0.5f) t * 2f else (1f - t) * 2f
            val finalAlpha = sparkAlpha * 0.3f * (1f + streak.coerceAtMost(10) * 0.05f)

            if (finalAlpha > 0.01f) {
                val px = pos.x * size.width
                val py = pos.y * size.height
                // Glow
                drawCircle(
                    color = sparkColors.random().copy(alpha = finalAlpha * 0.5f),
                    radius = sparkSize * 3f,
                    center = Offset(px, py),
                )
                // Core
                drawCircle(
                    color = Color.White.copy(alpha = finalAlpha),
                    radius = sparkSize,
                    center = Offset(px, py),
                )
            }
        }
    }
}

// ─── Streak fire border glow ──────────────────────────────────────

@Composable
fun StreakGlow(streak: Int) {
    if (streak < 3) return

    val transition = rememberInfiniteTransition()
    val pulse by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    val intensity = (streak.coerceAtMost(20) - 2) / 18f
    val baseAlpha = 0.08f + intensity * 0.15f

    val glowColor = when {
        streak >= 15 -> Color(0xFFFF1744)
        streak >= 10 -> Color(0xFFFF6D00)
        streak >= 5 -> Color(0xFFFFAB00)
        else -> Color(0xFFFF9100)
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val alpha = baseAlpha * pulse

        // Top edge glow
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(glowColor.copy(alpha = alpha), Color.Transparent),
                startY = 0f,
                endY = size.height * 0.15f,
            ),
        )

        // Left edge
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(glowColor.copy(alpha = alpha * 0.7f), Color.Transparent),
                startX = 0f,
                endX = size.width * 0.08f,
            ),
        )

        // Right edge
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, glowColor.copy(alpha = alpha * 0.7f)),
                startX = size.width * 0.92f,
                endX = size.width,
            ),
        )
    }
}

// ─── Wrong answer screen shake effect ─────────────────────────────

@Composable
fun WrongFlashEffect(trigger: Int) {
    if (trigger == 0) return

    val progress = remember(trigger) { Animatable(0f) }

    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(350, easing = EaseOutCubic))
    }

    val alpha = ((1f - progress.value) * 0.12f).coerceIn(0f, 1f)

    if (alpha > 0.001f) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Vignette-style red flash from edges
            val edgeSize = size.minDimension * 0.4f
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, Color(0xFFF44336).copy(alpha = alpha)),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = size.maxDimension * 0.7f,
                ),
            )
        }
    }
}
