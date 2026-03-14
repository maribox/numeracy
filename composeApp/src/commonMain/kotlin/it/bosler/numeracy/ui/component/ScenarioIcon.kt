package it.bosler.numeracy.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import it.bosler.numeracy.model.Category
import it.bosler.numeracy.model.ScenarioType
import numeracy.composeapp.generated.resources.Res
import numeracy.composeapp.generated.resources.img_blackjack
import numeracy.composeapp.generated.resources.img_calendar
import numeracy.composeapp.generated.resources.img_clock
import numeracy.composeapp.generated.resources.img_coins
import numeracy.composeapp.generated.resources.img_currency
import numeracy.composeapp.generated.resources.img_darts
import numeracy.composeapp.generated.resources.img_dice
import numeracy.composeapp.generated.resources.img_globe
import numeracy.composeapp.generated.resources.img_lightbulb
import numeracy.composeapp.generated.resources.img_measuring_cup
import numeracy.composeapp.generated.resources.img_money
import numeracy.composeapp.generated.resources.img_poker
import numeracy.composeapp.generated.resources.img_ruler
import numeracy.composeapp.generated.resources.img_scale
import numeracy.composeapp.generated.resources.img_speedometer
import numeracy.composeapp.generated.resources.img_tape
import numeracy.composeapp.generated.resources.img_thermometer
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private val scenarioImages: Map<ScenarioType, DrawableResource> = mapOf(
    ScenarioType.DARTS to Res.drawable.img_darts,
    ScenarioType.BLACKJACK to Res.drawable.img_blackjack,
    ScenarioType.POT_ODDS to Res.drawable.img_poker,
    ScenarioType.OUTS_COUNTING to Res.drawable.img_poker,
    ScenarioType.EQUITY to Res.drawable.img_poker,
    ScenarioType.IMPLIED_ODDS to Res.drawable.img_poker,
    ScenarioType.MAKING_CHANGE to Res.drawable.img_coins,
    ScenarioType.CURRENCY_EXCHANGE to Res.drawable.img_currency,
    ScenarioType.TIME_ZONES to Res.drawable.img_clock,
    ScenarioType.LENGTH_CONVERSION to Res.drawable.img_ruler,
    ScenarioType.WEIGHT_CONVERSION to Res.drawable.img_scale,
    ScenarioType.TEMPERATURE_CONVERSION to Res.drawable.img_thermometer,
    ScenarioType.VOLUME_CONVERSION to Res.drawable.img_measuring_cup,
    ScenarioType.SPEED_CONVERSION to Res.drawable.img_speedometer,
    ScenarioType.DOOMSDAY to Res.drawable.img_calendar,
)

private val categoryImages: Map<Category, DrawableResource> = mapOf(
    Category.GAMES to Res.drawable.img_dice,
    Category.WORK to Res.drawable.img_money,
    Category.WORLD to Res.drawable.img_globe,
    Category.CONVERSIONS to Res.drawable.img_tape,
    Category.MATH_TRICKS to Res.drawable.img_lightbulb,
)

fun scenarioImageRes(scenario: ScenarioType): DrawableResource? = scenarioImages[scenario]
fun categoryImageRes(category: Category): DrawableResource? = categoryImages[category]

/**
 * Full-bleed photo background for cards. The image fills the entire card,
 * with a horizontal gradient fade from [gradientColor] on the left to
 * transparent on the right, letting the photo show through.
 * A subtle dark bottom fade ensures text readability.
 */
@Composable
fun CardBackgroundImage(
    imageRes: DrawableResource?,
    gradientColor: Color,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        if (imageRes != null) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        // Diagonal fade: solid at bottom-left, transparent at top-right
        // This gives more breathing room for text in the bottom-left corner
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        0.0f to gradientColor,
                        0.3f to gradientColor.copy(alpha = 0.9f),
                        0.5f to gradientColor.copy(alpha = 0.5f),
                        0.7f to gradientColor.copy(alpha = 0.15f),
                        1.0f to Color.Transparent,
                        start = Offset(0f, heightPx * 0.6f),
                        end = Offset(widthPx * 0.9f, 0f),
                    )
                ),
        )

        // Bottom dark fade for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Transparent,
                        0.55f to Color.Transparent,
                        1.0f to Color.Black.copy(alpha = 0.45f),
                    )
                ),
        )
    }
}
