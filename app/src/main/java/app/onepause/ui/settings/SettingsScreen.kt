package app.onepause.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.onepause.BuildConfig
import app.onepause.data.AppPreferences
import app.onepause.ui.components.BrandMark
import app.onepause.ui.components.ChoiceChip
import app.onepause.ui.components.OnePauseSwitch
import app.onepause.ui.components.ScreenHeader
import app.onepause.ui.components.SectionLabel
import app.onepause.ui.components.SoftCard
import app.onepause.util.pluralize

@Composable
fun SettingsScreen(
    pauseSeconds: Int,
    reclaimedMinutesPerSkip: Int,
    hapticsEnabled: Boolean,
    serviceEnabled: Boolean,
    eventCount: Int,
    onPauseSecondsChange: (Int) -> Unit,
    onReclaimedMinutesChange: (Int) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onEraseHistory: () -> Unit,
    onOpenLicenses: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    var showEraseDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 132.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenHeader(
                overline = "Preferences",
                title = "A pause that fits.",
                subtitle = "Small adjustments, made around your life."
            )
        }

        item {
            ProtectionStatusCard(
                serviceEnabled = serviceEnabled,
                onOpenAccessibilitySettings = onOpenAccessibilitySettings
            )
        }

        item {
            SettingsCard(
                title = "The pause",
                description = "How long the breathing screen holds you before you can open a protected app."
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppPreferences.PAUSE_OPTIONS.forEach { seconds ->
                        ChoiceChip(
                            text = "${seconds}s",
                            selected = pauseSeconds == seconds,
                            onClick = { onPauseSecondsChange(seconds) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Long enough to interrupt the reflex, short enough to live with.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
        }

        item {
            SettingsCard(
                title = "Time reclaimed",
                description = "When you close a protected app after a pause, we count a little time back. Choose a number that feels honest to you."
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppPreferences.RECLAIMED_OPTIONS.forEach { minutes ->
                        ChoiceChip(
                            text = "$minutes min",
                            selected = reclaimedMinutesPerSkip == minutes,
                            onClick = { onReclaimedMinutesChange(minutes) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Timer,
                        contentDescription = null,
                        tint = colors.tertiary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = "A new app session, returned to you.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        }

        item {
            SoftCard {
                Column {
                    SettingRow(
                        title = "Breathing haptics",
                        subtitle = "A gentle tap as each breath changes",
                        trailing = {
                            OnePauseSwitch(
                                checked = hapticsEnabled,
                                onCheckedChange = onHapticsChange
                            )
                        }
                    )
                }
            }
        }

        item {
            SectionLabel("Privacy & data", modifier = Modifier.padding(top = 8.dp))
        }

        item {
            SoftCard {
                Column {
                    Row(
                        modifier = Modifier.padding(17.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(colors.secondaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = colors.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Private by design",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.onSurface
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = "No account. No tracking. No internet permission. Your pause history never leaves this phone.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }
                    Box(
                        Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(colors.outlineVariant.copy(alpha = 0.7f))
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = eventCount > 0) { showEraseDialog = true }
                            .padding(horizontal = 17.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = null,
                            tint = if (eventCount > 0) colors.tertiary else colors.onSurfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(9.dp))
                        Text(
                            text = if (eventCount == 0) {
                                "No pause history yet"
                            } else {
                                "Erase pause history · $eventCount ${pluralize(eventCount, "event")}"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = if (eventCount > 0) colors.onSurface else colors.onSurfaceVariant.copy(alpha = 0.55f)
                        )
                        Spacer(Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = colors.onSurfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        item {
            SectionLabel("How it works", modifier = Modifier.padding(top = 8.dp))
        }

        item {
            SoftCard {
                Column(modifier = Modifier.padding(18.dp)) {
                    HowRow("01", "Choose your apps", "Only the apps you select are interrupted.")
                    Spacer(Modifier.height(13.dp))
                    HowRow("02", "Meet the pause", "A breathing screen appears when one opens.")
                    Spacer(Modifier.height(13.dp))
                    HowRow("03", "Choose on purpose", "Close it and move on, or open it anyway.")
                }
            }
        }

        item {
            SoftCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenLicenses)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 17.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Licenses & notices",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.onSurface
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Typeface and open-source credits",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = colors.onSurfaceVariant.copy(alpha = 0.55f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BrandMark(size = 29.dp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "One Pause · ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Made to protect your attention.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant.copy(alpha = 0.58f)
                )
            }
        }
    }

    if (showEraseDialog) {
        AlertDialog(
            onDismissRequest = { showEraseDialog = false },
            title = {
                Text(
                    text = "Erase pause history?",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.onSurface
                )
            },
            text = {
                Text(
                    text = "This clears your pauses, time reclaimed, and app insights. Your protected apps and settings stay the same.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEraseHistory()
                        showEraseDialog = false
                    }
                ) {
                    Text("Erase", color = colors.tertiary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEraseDialog = false }) {
                    Text("Keep it", color = colors.onSurface)
                }
            },
            containerColor = colors.surfaceContainerLow,
            shape = MaterialTheme.shapes.large
        )
    }
}

@Composable
private fun ProtectionStatusCard(
    serviceEnabled: Boolean,
    onOpenAccessibilitySettings: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    SoftCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenAccessibilitySettings),
        color = if (serviceEnabled) colors.secondaryContainer.copy(alpha = 0.65f) else colors.tertiaryContainer.copy(alpha = 0.65f),
        borderColor = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (serviceEnabled) colors.secondary.copy(alpha = 0.55f) else colors.tertiary.copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (serviceEnabled) Icons.Rounded.Check else Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = colors.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(13.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (serviceEnabled) "Protection is on" else "Protection is off",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (serviceEnabled) {
                        "One Pause can see when a protected app opens."
                    } else {
                        "Turn on Accessibility access to begin."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(10.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = "Open Android accessibility settings",
                tint = colors.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = colors.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.padding(horizontal = 17.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(12.dp))
        trailing()
    }
}

@Composable
private fun HowRow(number: String, title: String, body: String) {
    val colors = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = number,
            style = MaterialTheme.typography.labelSmall,
            color = colors.tertiary,
            modifier = Modifier.width(25.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
    }
}
