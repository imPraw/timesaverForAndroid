package app.onepause.ui.stats

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.TrendingFlat
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.onepause.data.AppStat
import app.onepause.data.DayStat
import app.onepause.data.EventSummary
import app.onepause.ui.components.AppIcon
import app.onepause.ui.components.PrimaryButton
import app.onepause.ui.components.ScreenHeader
import app.onepause.ui.components.SectionLabel
import app.onepause.ui.components.SoftCard
import app.onepause.ui.components.SurfaceDivider
import app.onepause.ui.components.TextAction
import app.onepause.ui.components.rememberAppInfo
import app.onepause.util.formatMinutes
import app.onepause.util.formatPercent
import app.onepause.util.pluralize
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun StatsScreen(
    stats: EventSummary,
    reclaimedMinutesPerSkip: Int,
    onManageApps: () -> Unit,
    onAdjustEstimate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    LazyColumn(
        modifier = modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 132.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "header") {
            ScreenHeader(
                overline = "Your patterns",
                title = "Insights",
                subtitle = "Small pauses. A little more perspective.",
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        if (stats.totalPauses == 0) {
            item(key = "empty") {
                SoftCard {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            Modifier.size(64.dp).background(colors.primaryContainer, RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.BarChart, null, tint = colors.onPrimaryContainer, modifier = Modifier.size(30.dp))
                        }
                        Spacer(Modifier.height(20.dp))
                        Text(
                            "It starts with one pause.",
                            style = MaterialTheme.typography.headlineSmall,
                            color = colors.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Choose an app to protect. After your first pause, your patterns will appear here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(24.dp))
                        PrimaryButton(
                            text = "Choose your apps",
                            onClick = onManageApps,
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.AutoMirrored.Rounded.ArrowForward
                        )
                    }
                }
            }
            item(key = "privacy") { HowStatsWorkCard() }
            return@LazyColumn
        }

        item(key = "total") { TotalCard(stats) }
        item(key = "metrics") {
            BoxWithConstraints {
                val stacked = maxWidth < 300.dp || LocalDensity.current.fontScale > 1.35f
                if (stacked) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SmallStat("Pauses this week", stats.weekPauses.toString(), "Since Monday", Modifier.fillMaxWidth())
                        SmallStat("Left for later", formatPercent(stats.totalAvoided, stats.totalPauses), "${stats.totalAvoided} sessions, all time", Modifier.fillMaxWidth())
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SmallStat("Pauses this week", stats.weekPauses.toString(), "Since Monday", Modifier.weight(1f))
                        SmallStat("Left for later", formatPercent(stats.totalAvoided, stats.totalPauses), "${stats.totalAvoided} sessions, all time", Modifier.weight(1f))
                    }
                }
            }
        }
        item(key = "week_chart") { WeekChartCard(stats) }
        item(key = "reflection") { WeeklyReflection(stats) }

        if (stats.appStats.isNotEmpty()) {
            item(key = "targets_header") {
                SectionLabel("Apps & websites", modifier = Modifier.padding(top = 8.dp))
            }
            item(key = "targets") {
                SoftCard {
                    Column {
                        val maxPauses = stats.appStats.maxOf { it.pauses }.coerceAtLeast(1)
                        stats.appStats.forEachIndexed { index, appStat ->
                            AppStatRow(appStat, maxPauses)
                            if (index < stats.appStats.lastIndex) SurfaceDivider(Modifier.padding(start = 74.dp))
                        }
                    }
                }
            }
        }
        item(key = "estimate") {
            SoftCard {
                Column(Modifier.padding(20.dp)) {
                    Text("About your estimate", style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Each session you leave for later counts as $reclaimedMinutesPerSkip minutes reclaimed. This is an estimate, not measured screen time.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    TextAction("Adjust estimate", onAdjustEstimate, color = colors.primary)
                }
            }
        }
        item(key = "privacy") { HowStatsWorkCard() }
    }
}

@Composable
private fun TotalCard(stats: EventSummary) {
    val colors = MaterialTheme.colorScheme
    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        color = colors.primaryContainer,
        borderColor = colors.primaryContainer
    ) {
        Column(Modifier.padding(24.dp)) {
            Text("Time reclaimed", style = MaterialTheme.typography.titleMedium, color = colors.onPrimaryContainer)
            Spacer(Modifier.height(12.dp))
            Text(
                formatMinutes(stats.allTimeReclaimedMinutes),
                style = MaterialTheme.typography.displayLarge,
                color = colors.onPrimaryContainer
            )
            Spacer(Modifier.height(6.dp))
            Text("Estimated, all time", style = MaterialTheme.typography.bodyMedium, color = colors.onPrimaryContainer)
            Spacer(Modifier.height(24.dp))
            SurfaceDivider(color = colors.onPrimaryContainer.copy(alpha = 0.14f))
            Spacer(Modifier.height(18.dp))
            BoxWithConstraints {
                if (maxWidth < 270.dp || LocalDensity.current.fontScale > 1.35f) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        TotalPart(stats.totalPauses.toString(), "Total pauses", colors.onPrimaryContainer)
                        TotalPart(stats.totalAvoided.toString(), "Left for later", colors.onPrimaryContainer)
                        TotalPart("${stats.streakDays} ${pluralize(stats.streakDays, "day")}", "Current streak", colors.onPrimaryContainer)
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TotalPart(stats.totalPauses.toString(), "Pauses", colors.onPrimaryContainer, Modifier.weight(1f))
                        TotalPart(stats.totalAvoided.toString(), "Left for later", colors.onPrimaryContainer, Modifier.weight(1f))
                        TotalPart("${stats.streakDays}d", "Streak", colors.onPrimaryContainer, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun TotalPart(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = color)
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = color)
    }
}

@Composable
private fun SmallStat(label: String, value: String, detail: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    SoftCard(modifier = modifier) {
        Column(Modifier.padding(18.dp)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineLarge, color = colors.onSurface)
            Spacer(Modifier.height(4.dp))
            Text(detail, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun WeekChartCard(stats: EventSummary) {
    val colors = MaterialTheme.colorScheme
    val total = stats.lastSevenDays.sumOf { it.pauses }
    val maxCount = maxOf(1, stats.lastSevenDays.maxOfOrNull { it.pauses } ?: 0)
    SoftCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Text("Last 7 days", style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
            Spacer(Modifier.height(5.dp))
            Text("$total ${pluralize(total, "pause")}", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                stats.lastSevenDays.forEachIndexed { index, day ->
                    DayBar(day, maxCount, index == stats.lastSevenDays.lastIndex, Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Box(Modifier.size(7.dp).background(colors.primary, CircleShape))
                Text("Today", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun DayBar(day: DayStat, maxCount: Int, isToday: Boolean, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val fraction = (day.pauses.toFloat() / maxCount.toFloat()).coerceIn(0f, 1f)
    val barHeight by animateFloatAsState(
        targetValue = if (day.pauses == 0) 0.025f else fraction.coerceAtLeast(0.06f),
        animationSpec = tween(350),
        label = "dailyPauses"
    )
    val dateLabel = day.date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault()))
    val dayLabel = day.date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault())
    Column(
        modifier = modifier.clearAndSetSemantics {
            contentDescription = "$dateLabel: ${day.pauses} ${pluralize(day.pauses, "pause")}, ${day.avoided} left for later${if (isToday) ", today" else ""}"
        },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(day.pauses.toString(), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(104.dp), contentAlignment = Alignment.BottomCenter) {
            Box(
                Modifier.width(22.dp).height((100 * barHeight).dp)
                    .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                    .background(
                        when {
                            day.pauses == 0 -> colors.outlineVariant
                            isToday -> colors.primary
                            else -> colors.primary.copy(alpha = 0.28f)
                        }
                    )
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            dayLabel,
            style = MaterialTheme.typography.labelMedium,
            color = if (isToday) colors.primary else colors.onSurfaceVariant
        )
    }
}

@Composable
private fun WeeklyReflection(stats: EventSummary) {
    val colors = MaterialTheme.colorScheme
    val difference = stats.weekAvoided - stats.lastWeekAvoided
    SoftCard {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).background(colors.primaryContainer, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (difference > 0) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingFlat,
                    null,
                    tint = colors.onPrimaryContainer,
                    modifier = Modifier.size(21.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    when {
                        stats.weekAvoided == 0 && stats.lastWeekAvoided == 0 -> "A little room to begin."
                        difference > 0 -> "$difference more ${pluralize(difference, "session")} left for later."
                        difference < 0 -> "Every week has its own rhythm."
                        else -> "A steady practice."
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    "${stats.weekAvoided} so far this week · ${stats.lastWeekAvoided} last week",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AppStatRow(appStat: AppStat, maxPauses: Int) {
    val colors = MaterialTheme.colorScheme
    val info = rememberAppInfo(appStat.packageName)
    val progress by animateFloatAsState(
        targetValue = (appStat.pauses.toFloat() / maxPauses.coerceAtLeast(1)).coerceIn(0f, 1f),
        animationSpec = tween(350),
        label = "appPauses"
    )
    Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
        AppIcon(appStat.packageName, info.icon, size = 42.dp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    info.label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(appStat.pauses.toString(), style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(4.dp).background(colors.outlineVariant, CircleShape)) {
                Box(Modifier.fillMaxWidth(progress).height(4.dp).background(colors.primary, CircleShape))
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "${appStat.avoided} of ${appStat.pauses} left for later",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HowStatsWorkCard() {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(Icons.Rounded.Lock, null, tint = colors.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp).size(16.dp))
        Spacer(Modifier.width(9.dp))
        Text(
            "Your history stays on this phone. No account, no analytics, no internet permission.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
    }
}
