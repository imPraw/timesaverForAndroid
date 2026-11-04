package app.onepause.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.onepause.BuildConfig
import app.onepause.data.AppPreferences
import app.onepause.ui.components.*
import app.onepause.util.pluralize

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    pauseSeconds: Int,
    reclaimedMinutesPerSkip: Int,
    hapticsEnabled: Boolean,
    serviceEnabled: Boolean,
    eventCount: Int,
    appearanceMode: String,
    onAppearanceModeChange: (String) -> Unit,
    onPauseSecondsChange: (Int) -> Unit,
    onReclaimedMinutesChange: (Int) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onEraseHistory: () -> Unit,
    onOpenLicenses: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    var showEraseDialog by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 132.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ScreenHeader(
                overline = "Your preferences",
                title = "Settings",
                subtitle = "Make the pause your own."
            )
        }
        item { SectionLabel("Protection", Modifier.padding(top = 12.dp, start = 4.dp)) }
        item { ProtectionStatusCard(serviceEnabled, onOpenAccessibilitySettings) }
        item { UninstallGuardCard(serviceEnabled, onOpenAccessibilitySettings) }

        item { SectionLabel("The experience", Modifier.padding(top = 16.dp, start = 4.dp)) }
        item {
            SettingsCard(
                title = "Appearance",
                description = "Choose a look that feels right."
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("system" to "System", "light" to "Light", "dark" to "Dark").forEach { (mode, label) ->
                        ChoiceChip(
                            text = label,
                            selected = appearanceMode == mode,
                            onClick = { onAppearanceModeChange(mode) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "System follows your device. Applies everywhere, including your breathing pause.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
        }
        item {
            SettingsCard(
                title = "Pause duration",
                description = "A little breathing room before you choose to continue."
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppPreferences.PAUSE_OPTIONS.forEach { seconds ->
                        ChoiceChip(
                            text = "${seconds}s",
                            selected = pauseSeconds == seconds,
                            onClick = { onPauseSecondsChange(seconds) }
                        )
                    }
                }
            }
        }
        item {
            SoftCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(value = hapticsEnabled, role = Role.Switch, onValueChange = onHapticsChange)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Breathing haptics", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "A gentle tap with each breath.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(checked = hapticsEnabled, onCheckedChange = null)
                }
            }
        }
        item {
            SettingsCard(
                title = "Time reclaimed",
                description = "Your estimate for each session you choose to skip."
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppPreferences.RECLAIMED_OPTIONS.forEach { minutes ->
                        ChoiceChip(
                            text = "$minutes min",
                            selected = reclaimedMinutesPerSkip == minutes,
                            onClick = { onReclaimedMinutesChange(minutes) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "This is an estimate, not measured screen time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
        }

        item { SectionLabel("Privacy & data", Modifier.padding(top = 16.dp, start = 4.dp)) }
        item {
            SoftCard {
                Column {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.Top) {
                        Box(
                            Modifier.size(36.dp).background(colors.primaryContainer, RoundedCornerShape(11.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Lock, null, tint = colors.onPrimaryContainer, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Only on this phone", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(5.dp))
                            Text(
                                "Your settings and pause history stay here. No account, tracking, or internet permission.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }
                    SurfaceDivider(Modifier.padding(start = 20.dp))
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable(enabled = eventCount > 0, role = Role.Button) { showEraseDialog = true }
                            .heightIn(min = 64.dp)
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.DeleteOutline,
                            null,
                            tint = if (eventCount > 0) colors.error else colors.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Erase pause history",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (eventCount > 0) colors.error else colors.onSurfaceVariant
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                if (eventCount == 0) "No history to erase" else "$eventCount ${pluralize(eventCount, "event")} stored on this phone",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant
                            )
                        }
                        if (eventCount > 0) {
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Rounded.ChevronRight, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        item { SectionLabel("About One Pause", Modifier.padding(top = 16.dp, start = 4.dp)) }
        item {
            SoftCard {
                Column(Modifier.padding(20.dp)) {
                    HowRow("1", "Choose what to protect", "Select the apps and websites that pull you in.")
                    Spacer(Modifier.height(18.dp))
                    HowRow("2", "Take a breath", "A short pause gives you room to notice the reflex.")
                    Spacer(Modifier.height(18.dp))
                    HowRow("3", "Choose what comes next", "Leave it for later, or continue on purpose.")
                }
            }
        }
        item {
            SoftCard {
                Row(
                    Modifier.fillMaxWidth()
                        .clickable(role = Role.Button, onClick = onOpenLicenses)
                        .heightIn(min = 64.dp)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Licenses & notices", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(3.dp))
                        Text("Typeface and open-source credits", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    }
                    Spacer(Modifier.width(12.dp))
                    Icon(Icons.Rounded.ChevronRight, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
            }
        }
        item {
            Column(
                Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BrandMark(size = 26.dp, accent = colors.primary)
                Spacer(Modifier.height(10.dp))
                Text("One Pause · ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                Spacer(Modifier.height(3.dp))
                Text("A little more room for you.", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
    }

    if (showEraseDialog) {
        AlertDialog(
            onDismissRequest = { showEraseDialog = false },
            title = { Text("Erase pause history?") },
            text = { Text("This permanently clears your pauses, reclaimed-time estimates, and insights. Your protected apps, websites, and settings stay the same.") },
            confirmButton = {
                TextButton(onClick = {
                    onEraseHistory()
                    showEraseDialog = false
                }) { Text("Erase history", color = colors.error) }
            },
            dismissButton = {
                TextButton(onClick = { showEraseDialog = false }) { Text("Cancel") }
            },
            containerColor = colors.surfaceContainerLow,
            shape = MaterialTheme.shapes.large
        )
    }
}

@Composable
private fun ProtectionStatusCard(serviceEnabled: Boolean, onOpenAccessibilitySettings: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    SoftCard {
        Row(
            Modifier.fillMaxWidth()
                .clickable(role = Role.Button, onClick = onOpenAccessibilitySettings)
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(40.dp).background(
                    if (serviceEnabled) colors.primaryContainer else colors.surfaceContainerHigh,
                    RoundedCornerShape(12.dp)
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (serviceEnabled) Icons.Rounded.Check else Icons.Rounded.Lock,
                    null,
                    tint = if (serviceEnabled) colors.onPrimaryContainer else colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(if (serviceEnabled) "Protection is on" else "Set up protection", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    if (serviceEnabled) "Manage Accessibility access in Android." else "Enable Accessibility access to begin.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(10.dp))
            Icon(Icons.Rounded.ChevronRight, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun SettingsCard(title: String, description: String, content: @Composable () -> Unit) {
    SoftCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(5.dp))
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun HowRow(number: String, title: String, body: String) {
    val colors = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.Top) {
        Box(
            Modifier.size(28.dp).background(colors.surfaceContainer, RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(number, style = MaterialTheme.typography.labelMedium, color = colors.primary)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(3.dp))
            Text(body, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
    }
}
