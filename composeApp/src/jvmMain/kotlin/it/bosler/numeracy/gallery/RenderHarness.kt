package it.bosler.numeracy.gallery

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import it.bosler.numeracy.ui.theme.NumeracyTheme
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import org.jetbrains.skia.SamplingMode
import org.jetbrains.skia.Surface as SkiaSurface
import java.io.File

/**
 * Draws a composable to a PNG with no display, so every screen of the app can be looked at as a
 * picture that came from the code the app runs. Runs on the JVM target.
 */
fun renderToPng(
    name: String,
    widthDp: Int,
    heightDp: Int,
    dark: Boolean,
    outDir: File,
    // Pixels per dp. A phone render is read at its own size and wants two; a wide one is only ever
    // looked at beside the phone, at half a window, and at two it costs four times the pixels for
    // no more to read.
    scale: Float,
    content: @Composable () -> Unit,
) {
    val density = Density(scale)
    val scene = ImageComposeScene(
        width = (widthDp * density.density).toInt(),
        height = (heightDp * density.density).toInt(),
        density = density,
    ) {
        NumeracyTheme(darkTheme = dark) {
            Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxSize()) { content() }
            }
        }
    }
    try {
        // A screen answers a tap with an animation and loads its photos off the composition, so the
        // first frame is a screen mid-blink: colours not yet arrived at, cards still grey. Frames
        // are drawn until those have settled, and the last one is the picture.
        var last: Image? = null
        var time = 0L
        repeat(SETTLING_FRAMES) {
            last = scene.render(time)
            time += FRAME_NANOS
            Thread.sleep(2)
        }
        val image = last!!
        outDir.mkdirs()
        File(outDir, "$name.png").writeBytes(image.encodeToData()!!.bytes)
        // The book asks for a small copy where it shows a picture small, and a card is the top of a
        // screen at a size that can be read. Without them every thumbnail on the index is a
        // quarter-megabyte photograph of a phone.
        writePart(image, File(outDir, "small"), name, SMALL_SCALE, 1f)
        writePart(image, File(outDir, "card"), name, CARD_SCALE, CARD_TOP)
    } finally {
        scene.close()
    }
}

/** A copy of a render: [scale] of its size, and the top [top] of its height. */
private fun writePart(image: Image, dir: File, name: String, scale: Float, top: Float) {
    val width = (image.width * scale).toInt().coerceAtLeast(1)
    val height = (image.height * top * scale).toInt().coerceAtLeast(1)
    val surface = SkiaSurface.makeRasterN32Premul(width, height)
    try {
        surface.canvas.drawImageRect(
            image,
            Rect.makeWH(image.width.toFloat(), image.height * top),
            Rect.makeWH(width.toFloat(), height.toFloat()),
            SamplingMode.LINEAR,
            null,
            true,
        )
        dir.mkdirs()
        File(dir, "$name.png").writeBytes(surface.makeImageSnapshot().encodeToData()!!.bytes)
    } finally {
        surface.close()
    }
}

private const val SMALL_SCALE = 0.5f
private const val CARD_SCALE = 0.55f
private const val CARD_TOP = 0.45f

private const val FRAME_NANOS = 16_000_000L
private val SETTLING_FRAMES = (System.getProperty("gallery.frames") ?: "45").toInt()
