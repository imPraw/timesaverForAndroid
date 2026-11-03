package app.onepause.ui.home

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.onepause.data.EventSummary
import app.onepause.ui.components.AppIconNameRow
import app.onepause.ui.components.BrandMark
import app.onepause.ui.components.Pill
import app.onepause.ui.components.SectionLabel
import app.onepause.ui.components.SoftCard
import app.onepause.ui.components.TextAction
import app.onepause.ui.components.rememberAppInfo
import app.onepause.util.formatLongMinutes
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
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val now = remember { LocalTime.now() }
    val greeting = when (now.hour) {
        in 5..11 -> "Good morning."
        in 12..16 -> "Good afternoon."
        in 17..21 -> "Good evening."
        else -> "A quiet night."
    }
    val dateLabel = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault()))
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 132.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BrandMark(size = 29.dp)
                    Spacer(Modifier.width(9.dp))
                    Text(
                        text = "ONE PAUSE",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = dateLabel.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                Spacer(Modifier.height(26.dp))
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.displaySmall,
                    color = colors.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "What deserves your attention today?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            }
        }

        item {
            ReclaimedHero(stats = stats, onOpenInsights = onOpenInsights)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiniMetricCard(
                    label = "Today",
                    value = stats.todayPauses.toString(),
                    supporting = pluralize(stats.todayPauses, "pause"),
                    modifier = Modifier.weight(1f),
                    tint = colors.tertiaryContainer
                )
                MiniMetricCard(
                    label = "Left for later",
                    value = stats.totalAvoided.toString(),
                    supporting = "all time",
                    modifier = Modifier.weight(1f),
                    tint = colors.secondaryContainer.copy(alpha = 0.65f)
                )
            }
        }

        item {
            DailyBloom(stats.todayPauses)
        }

        item {
            ProtectionCard(
                serviceEnabled = serviceEnabled,
                blockedCount = blockedCount,
                onManageApps = onManageApps,
                onEnableProtection = onEnableProtection
            )
        }

        if (stats.appStats.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionLabel("Most paused")
                    Spacer(Modifier.weight(1f))
                    TextAction(
                        text = "See insights",
                        onClick = onOpenInsights,
                        color = colors.onSurface,
                        modifier = Modifier.padding(end = 0.dp)
                    )
                }
            }
            item {
                SoftCard {
                    Column {
                        stats.appStats.take(3).forEachIndexed { index, appStat ->
                            val info = rememberAppInfo(appStat.packageName)
                            AppIconNameRow(
                                packageName = appStat.packageName,
                                label = info.label,
                                icon = info.icon,
                                supporting = "${appStat.pauses} ${pluralize(appStat.pauses, "pause")} · ${appStat.avoided} left for later",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp)
                            )
                            if (index < minOf(stats.appStats.size, 3) - 1) {
                                Box(
                                    Modifier
                                        .padding(start = 73.dp)
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(colors.outlineVariant.copy(alpha = 0.75f))
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            PracticeCard(pauseSeconds = pauseSeconds, onPreviewPause = onPreviewPause)
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "The scroll can wait. You don't have to.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant.copy(alpha = 0.58f)
                )
            }
        }
    }
}

@Composable
private fun ReclaimedHero(stats: EventSummary, onOpenInsights: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val minutes = stats.weekReclaimedMinutes
    val mainValue: String
    val suffix: String
    when {
        minutes < 60 -> {
            mainValue = minutes.toString()
            suffix = "min"
        }
        minutes % 60 == 0 -> {
            mainValue = (minutes / 60).toString()
            suffix = if (minutes == 60) "hour" else "hours"
        }
        else -> {
            mainValue = formatMinutes(minutes)
            suffix = ""
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(colors.primaryContainer, colors.primaryContainer)
                )
            )
            .clickable(onClick = onOpenInsights)
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(
                color = colors.onPrimaryContainer.copy(alpha = 0.09f),
                radius = size.width * 0.42f,
                center = Offset(size.width * 0.9f, -size.height * 0.06f)
            )
            drawCircle(
                color = colors.onPrimaryContainer.copy(alpha = 0.04f),
                radius = size.width * 0.25f,
                center = Offset(size.width * 0.84f, size.height * 0.92f)
            )
            drawArc(
                color = colors.onPrimaryContainer.copy(alpha = 0.8f),
                startAngle = 112f,
                sweepAngle = 112f,
                useCenter = false,
                topLeft = Offset(size.width * 0.69f, size.height * 0.16f),
                size = androidx.compose.ui.geometry.Size(size.width * 0.34f, size.width * 0.34f),
                style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
            )
            drawCircle(
                color = colors.tertiary,
                radius = 3.2.dp.toPx(),
                center = Offset(size.width * 0.86f, size.height * 0.17f)
            )
        }

        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionLabel(
                    text = "Time reclaimed this week",
                    color = colors.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Spacer(Modifier.weight(1f))
                Pill(
                    text = if (stats.streakDays > 0) {
                        "${stats.streakDays} day streak"
                    } else {
                        "A fresh start"
                    },
                    background = Color.White.copy(alpha = 0.06f),
                    contentColor = colors.onPrimaryContainer.copy(alpha = 0.82f),
                    borderColor = Color.White.copy(alpha = 0.11f)
                )
            }
            Spacer(Modifier.height(22.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = mainValue,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp, lineHeight = 64.sp),
                    color = colors.onPrimaryContainer
                )
                if (suffix.isNotEmpty()) {
                    Spacer(Modifier.width(9.dp))
                    Text(
                        text = suffix,
                        style = MaterialTheme.typography.headlineMedium,
                        color = colors.onPrimaryContainer.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 7.dp)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = if (stats.weekPauses == 0) {
                    "A little pause. A little more room for you."
                } else {
                    "${stats.weekPauses} ${pluralize(stats.weekPauses, "intentional pause")} · ${stats.weekAvoided} closed instead"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(24.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.09f))
            )
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Timer,
                    contentDescription = null,
                    tint = colors.onPrimaryContainer,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    text = "An estimate, based on your settings",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = "Open insights",
                    tint = colors.onPrimaryContainer.copy(alpha = 0.45f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun MiniMetricCard(
    label: String,
    value: String,
    supporting: String,
    modifier: Modifier = Modifier,
    tint: Color
) {
    val colors = MaterialTheme.colorScheme
    SoftCard(modifier = modifier, color = tint, borderColor = Color.Transparent) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            Text(
                text = label.uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.onSurface
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ProtectionCard(
    serviceEnabled: Boolean,
    blockedCount: Int,
    onManageApps: () -> Unit,
    onEnableProtection: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val needsSetup = !serviceEnabled || blockedCount == 0
    val background = when {
        !serviceEnabled -> colors.tertiaryContainer.copy(alpha = 0.7f)
        blockedCount == 0 -> colors.tertiaryContainer
        else -> colors.surfaceContainerLow
    }
    val onClick = if (!serviceEnabled) onEnableProtection else onManageApps
    SoftCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = background,
        borderColor = if (needsSetup) Color.Transparent else colors.outlineVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 17.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        when {
                            !serviceEnabled -> colors.tertiary.copy(alpha = 0.2f)
                            blockedCount == 0 -> colors.tertiary.copy(alpha = 0.55f)
                            else -> colors.secondaryContainer
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (!serviceEnabled) Icons.Rounded.Lock else Icons.Rounded.Check,
                    contentDescription = null,
                    tint = colors.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(13.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when {
                        !serviceEnabled -> "Protection is off"
                        blockedCount == 0 -> "No apps protected yet"
                        else -> "Protection is on"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = when {
                        !serviceEnabled -> "Turn on Accessibility so the pause can appear."
                        blockedCount == 0 -> "Choose the apps that tend to pull you in."
                        else -> "One breath before ${blockedCount} ${pluralize(blockedCount, "app")} ${if (blockedCount == 1) "opens" else "open"}."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(10.dp))
            if (needsSetup) {
                Row(
                    modifier = Modifier
                        .background(if (!serviceEnabled) colors.tertiary else colors.onSurface, CircleShape)
                        .padding(horizontal = 13.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (!serviceEnabled) "Turn on" else "Choose",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (!serviceEnabled) colors.onTertiary else colors.inverseOnSurface
                    )
                    Spacer(Modifier.width(5.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = if (!serviceEnabled) colors.onTertiary else colors.inverseOnSurface,
                        modifier = Modifier.size(13.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = "Manage protected apps",
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun PracticeCard(pauseSeconds: Int, onPreviewPause: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        color = colors.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                SectionLabel("Your reset button")
                Spacer(Modifier.height(7.dp))
                Text(
                    text = "Take a mini breather.",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "A ${pauseSeconds}-second breath, just for practice.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                TextAction(
                    text = "Try a pause",
                    onClick = onPreviewPause,
                    color = colors.tertiary,
                    modifier = Modifier.padding(start = 0.dp, end = 0.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(colors.tertiaryContainer, colors.tertiary),
                            center = Offset(28f, 24f),
                            radius = 90f
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                BrandMark(size = 36.dp, accent = colors.inverseOnSurface)
            }
        }
    }
}
