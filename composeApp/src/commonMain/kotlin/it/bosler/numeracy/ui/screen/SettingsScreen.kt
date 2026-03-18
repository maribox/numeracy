package it.bosler.numeracy.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import it.bosler.numeracy.BuildConfig
import it.bosler.numeracy.persistence.AppContext
import it.bosler.numeracy.util.PlatformBackHandler
import it.bosler.numeracy.util.showBackButton

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
) {
    PlatformBackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showBackButton) {
                TextButton(onClick = onBack) {
                    Text("Back", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = if (showBackButton) 0.dp else 16.dp),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            // Game Mode section
            SectionHeader("Game Mode")
            Spacer(modifier = Modifier.height(8.dp))

            SettingsCard {
                var gameModeEnabled by remember {
                    mutableStateOf(AppContext.runRepository.isGameModeEnabled())
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Game Mode",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Timer, points, streaks, and animations",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = gameModeEnabled,
                        onCheckedChange = {
                            gameModeEnabled = it
                            AppContext.runRepository.setGameModeEnabled(it)
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Help & About section
            SectionHeader("Help & About")
            Spacer(modifier = Modifier.height(8.dp))

            SettingsCard {
                SettingsRow(
                    label = "Version",
                    value = BuildConfig.VERSION_NAME,
                )
                SettingsDivider()
                SettingsRow(
                    label = "Build",
                    value = "#${BuildConfig.BUILD_NUMBER} (${BuildConfig.GIT_HASH})",
                )
                SettingsDivider()
                SettingsRow(
                    label = "Built",
                    value = BuildConfig.BUILD_TIMESTAMP,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // How to use
            SectionHeader("How to Use")
            Spacer(modifier = Modifier.height(8.dp))

            SettingsCard {
                HelpItem(
                    title = "Practice a scenario",
                    description = "Pick a category from the home screen, then tap a scenario to start practicing. Answer questions as fast as you can.",
                )
                SettingsDivider()
                HelpItem(
                    title = "Difficulty modes",
                    description = "Use the segmented bar at the top of the practice screen to switch difficulty. Each mode changes the visual helpers shown:\n\n" +
                        "\u2022 Hard: Score hidden (Darts only). Pure mental math.\n" +
                        "\u2022 Normal: Standard play. No helpers.\n" +
                        "\u2022 Practice: Intermediate values shown as badges to help you focus on one step at a time.\n" +
                        "\u2022 Learning: Full step-by-step visual breakdown of the calculation.",
                )
                SettingsDivider()
                HelpItem(
                    title = "Info sheets",
                    description = "Tap the (i) button during practice to see a detailed guide explaining the math techniques for that scenario.",
                )
                SettingsDivider()
                HelpItem(
                    title = "Statistics",
                    description = "Tap the bar chart icon on any scenario card to see your accuracy, average speed, and streak history.",
                )
                SettingsDivider()
                HelpItem(
                    title = "Keyboard input",
                    description = "Physical keyboards are supported. Type numbers directly and press Enter to submit (for time-based inputs).",
                )
                SettingsDivider()
                HelpItem(
                    title = "Landscape mode",
                    description = "Rotate your device for a side-by-side layout with the question on the left and input on the right.",
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Footer
            Text(
                text = "Numeracy - Mental Math Trainer",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.padding(start = 4.dp),
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        content()
    }
}

@Composable
private fun SettingsRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 2.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
    )
}

@Composable
private fun HelpItem(title: String, description: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp,
        )
    }
}
