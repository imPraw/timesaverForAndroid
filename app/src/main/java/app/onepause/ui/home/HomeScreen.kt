package app.onepause.ui.home

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.onepause.data.EventSummary
import app.onepause.ui.components.AppIconNameRow
import app.onepause.ui.components.BrandMark
import app.onepause.ui.components.SectionLabel
import app.onepause.ui.components.SoftCard
import app.onepause.ui.components.SurfaceDivider
import app.onepause.ui.components.TextAction
import app.onepause.ui.components.rememberAppInfo
import app.onepause.util.formatMinutes
import app.onepause.util.pluralize
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    stats: EventSummary,
    blockedCount: Int,
    serviceEnabled: Boolean,
    pauseSeconds: Int,
    onManageApps: () -> Unit,
    onEnableProtection: () -> Unit,
    onOpenInsights: () -> Unit,
    onPreviewPause: () -> Unit,
    modifier: Modifier = Modifier,
    blockedWebsiteCount: Int = 0,
    onManageWebsites: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    val greeting = when (LocalTime.now().hour) {
        in 5..11 -> "Good morning."
        in 12..16 -> "Good afternoon."
        in 17..21 -> "Good evening."
        else -> "A quiet moment."
    }
    val dateLabel = LocalDate.now().format(
        DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())
    )
    val needsSetup = !serviceEnabled || blockedCount + blockedWebsiteCount == 0
    LazyColumn(
        modifier = modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 132.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "header") {
            Column(Modifier.padding(bottom = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(dateLabel, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                        Spacer(Modifier.height(6.dp))
                        Text(greeting, style = MaterialTheme.typography.headlineLarge, color = colors.onSurface)
                    }
                    Spacer(Modifier.width(16.dp))
                    BrandMark(size = 34.dp, accent = colors.primary)
                }
                Spacer(Modifier.height(8.dp))
                Text("A little space for what matters.", style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
            }
        }
        if (needsSetup) {
            item(key = "setup") {
                ProtectionCard(serviceEnabled, blockedCount, blockedWebsiteCount, onManageApps, onManageWebsites, onEnableProtection)
            }
        }
        item(key = "reclaimed") { ReclaimedHero(stats, onOpenInsights) }
        item(key = "today") {
            BoxWithConstraints {
                val stacked = maxWidth < 300.dp || LocalDensity.current.fontScale > 1.35f
                if (stacked) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        MiniMetricCard("Pauses today", stats.todayPauses.toString(), "Every moment counts", Modifier.fillMaxWidth())
                        MiniMetricCard("Left for later", stats.totalAvoided.toString(), "All time", Modifier.fillMaxWidth())
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MiniMetricCard("Pauses today", stats.todayPauses.toString(), "Every moment counts", Modifier.weight(1f))
                        MiniMetricCard("Left for later", stats.totalAvoided.toString(), "All time", Modifier.weight(1f))
                    }
                }
            }
        }
        item(key = "bloom") { DailyBloom(stats.todayPauses) }
        if (!needsSetup) {
            item(key = "protection") {
                ProtectionCard(serviceEnabled, blockedCount, blockedWebsiteCount, onManageApps, onManageWebsites, onEnableProtection)
            }
        }
        if (stats.appStats.isNotEmpty()) {
            item(key = "most_paused_header") {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    SectionLabel("Most paused", modifier = Modifier.weight(1f))
                    TextAction("View insights", onOpenInsights, color = colors.primary)
                }
            }
            item(key = "most_paused") {
                SoftCard {
                    Column {
                        stats.appStats.take(3).forEachIndexed { index, appStat ->
                            val info = rememberAppInfo(appStat.packageName)
                            AppIconNameRow(
                                packageName = appStat.packageName,
                                label = info.label,
                                icon = info.icon,
                                supporting = "${appStat.pauses} ${pluralize(appStat.pauses, "pause")} · ${appStat.avoided} left for later",
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)
                            )
                            if (index < minOf(stats.appStats.size, 3) - 1) SurfaceDivider(Modifier.padding(start = 75.dp))
                        }
                    }
                }
            }
        }
        item(key = "practice") { PracticeCard(pauseSeconds, onPreviewPause) }
        item(key = "footer") {
            Text(
                "One breath. Then, your choice.",
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ReclaimedHero(stats: EventSummary, onOpenInsights: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    SoftCard(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
            .clickable(role = Role.Button, onClickLabel = "View insights", onClick = onOpenInsights),
        color = colors.primaryContainer,
        borderColor = colors.primaryContainer
    ) {
        Column(Modifier.padding(24.dp)) {
            Text("Time reclaimed", style = MaterialTheme.typography.titleMedium, color = colors.onPrimaryContainer)
            Spacer(Modifier.height(12.dp))
            Text(formatMinutes(stats.weekReclaimedMinutes), style = MaterialTheme.typography.displayLarge, color = colors.onPrimaryContainer)
            Spacer(Modifier.height(6.dp))
            Text("Estimated this week", style = MaterialTheme.typography.bodyMedium, color = colors.onPrimaryContainer)
            Spacer(Modifier.height(24.dp))
            SurfaceDivider(color = colors.onPrimaryContainer.copy(alpha = 0.14f))
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        if (stats.weekPauses == 0) "Your next pause is a fresh start." else
                            "${stats.weekPauses} ${pluralize(stats.weekPauses, "pause")} · ${stats.weekAvoided} left for later",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onPrimaryContainer
                    )
                    if (stats.streakDays > 0) {
                        Spacer(Modifier.height(3.dp))
                        Text("${stats.streakDays}-day streak", style = MaterialTheme.typography.bodySmall, color = colors.onPrimaryContainer)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Icon(Icons.Rounded.ChevronRight, null, tint = colors.onPrimaryContainer, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun MiniMetricCard(label: String, value: String, supporting: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    SoftCard(modifier = modifier) {
        Column(Modifier.padding(18.dp)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineLarge, color = colors.onSurface)
            Spacer(Modifier.height(4.dp))
            Text(supporting, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun ProtectionCard(
    serviceEnabled: Boolean,
    blockedCount: Int,
    blockedWebsiteCount: Int,
    onManageApps: () -> Unit,
    onManageWebsites: () -> Unit,
    onEnableProtection: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val needsSetup = !serviceEnabled || blockedCount + blockedWebsiteCount == 0
    val action = when {
        !serviceEnabled -> onEnableProtection
        blockedCount == 0 && blockedWebsiteCount > 0 -> onManageWebsites
        else -> onManageApps
    }
    SoftCard {
        Column {
            Row(
                Modifier.fillMaxWidth()
                    .clickable(
                        role = Role.Button,
                        onClickLabel = if (!serviceEnabled) "Enable protection" else "Manage protection",
                        onClick = action
                    )
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(42.dp).background(
                        if (needsSetup) colors.tertiaryContainer else colors.primaryContainer,
                        RoundedCornerShape(13.dp)
                    ), contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (needsSetup) Icons.Rounded.Lock else Icons.Rounded.Check,
                        null,
                        tint = if (needsSetup) colors.onTertiaryContainer else colors.onPrimaryContainer,
                        modifier = Modifier.size(21.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        when {
                            !serviceEnabled -> "Turn on protection"
                            needsSetup -> "Choose your apps"
                            else -> "Protection is on"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        when {
                            !serviceEnabled -> "Enable One Pause in Accessibility settings."
                            needsSetup -> "Add a breath before the apps that pull you in."
                            blockedCount == 0 -> "$blockedWebsiteCount ${pluralize(blockedWebsiteCount, "website")} protected."
                            else -> "$blockedCount ${pluralize(blockedCount, "app")} protected with a pause."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(12.dp))
                Icon(Icons.Rounded.ChevronRight, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
            if (blockedWebsiteCount > 0 && blockedCount > 0) {
                SurfaceDivider(Modifier.padding(start = 74.dp))
                Row(
                    Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onManageWebsites).padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Language, null, tint = colors.primary, modifier = Modifier.padding(horizontal = 10.dp).size(22.dp))
                    Spacer(Modifier.width(14.dp))
                    Text(
                        "$blockedWebsiteCount ${pluralize(blockedWebsiteCount, "website")} selected",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurface
                    )
                    Icon(Icons.Rounded.ChevronRight, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun PracticeCard(pauseSeconds: Int, onPreviewPause: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    SoftCard(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
            .clickable(role = Role.Button, onClickLabel = "Try a practice pause", onClick = onPreviewPause)
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(42.dp).background(colors.primaryContainer, RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.SelfImprovement, null, tint = colors.onPrimaryContainer, modifier = Modifier.size(23.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Take a practice pause", style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                Spacer(Modifier.height(4.dp))
                Text("$pauseSeconds seconds, just for you.", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
            Spacer(Modifier.width(12.dp))
            Icon(Icons.Rounded.ChevronRight, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
    }
}
