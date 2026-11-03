package app.onepause.ui.stats

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.onepause.data.AppStat
import app.onepause.data.DayStat
import app.onepause.data.EventSummary
import app.onepause.ui.components.AppIcon
import app.onepause.ui.components.BrandMark
import app.onepause.ui.components.Pill
import app.onepause.ui.components.PrimaryButton
import app.onepause.ui.components.ScreenHeader
import app.onepause.ui.components.SectionLabel
import app.onepause.ui.components.SoftCard
import app.onepause.ui.components.TextAction
import app.onepause.ui.components.rememberAppInfo
import app.onepause.util.formatLongMinutes
import app.onepause.util.formatPercent
import app.onepause.util.pluralize
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
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 132.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ScreenHeader(
                overline = "Your patterns",
                title = "Time, back.",
                subtitle = "A quiet record of the moments you chose something else."
            )
        }

        if (stats.totalPauses == 0) {
            item {
                SoftCard(color = colors.tertiaryContainer, borderColor = Color.Transparent) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(78.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(colors.tertiaryContainer, colors.tertiary),
                                        center = Offset(30f, 24f),
                                        radius = 110f
                                    ),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            BrandMark(size = 38.dp, accent = colors.inverseOnSurface)
                        }
                        Spacer(Modifier.height(18.dp))
                        Text(
                            text = "Your first pause starts the story.",
                            style = MaterialTheme.typography.headlineMedium,
                            color = colors.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "As soon as you pause or close a protected app, your time and patterns will show up here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(20.dp))
                        PrimaryButton(
                            text = "Choose your apps",
                            onClick = onManageApps,
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.AutoMirrored.Rounded.ArrowForward
                        )
                    }
                }
            }
            item { HowStatsWorkCard() }
            return@LazyColumn
        }

        item {
            TotalCard(stats = stats)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SmallStat(
                    label = "This week",
                    value = "${stats.weekPauses}",
                    detail = pluralize(stats.weekPauses, "pause"),
                    modifier = Modifier.weight(1f),
                    tint = colors.secondaryContainer
                )
                SmallStat(
                    label = "Closed instead",
                    value = formatPercent(stats.totalAvoided, stats.totalPauses),
                    detail = "${stats.totalAvoided} ${pluralize(stats.totalAvoided, "session")}",
                    modifier = Modifier.weight(1f),
                    tint = colors.tertiaryContainer
                )
            }
        }

        item {
            WeekChartCard(stats = stats)
        }

        item {
            WeeklyReflection(stats = stats)
        }

        if (stats.appStats.isNotEmpty()) {
            item {
                SectionLabel("By app", modifier = Modifier.padding(top = 8.dp))
            }
            item {
                SoftCard {
                    Column {
                        val maxPauses = stats.appStats.maxOf { it.pauses }
                        stats.appStats.forEachIndexed { index, appStat ->
                            AppStatRow(appStat = appStat, maxPauses = maxPauses)
                            if (index < stats.appStats.lastIndex) {
                                Box(
                                    Modifier
                                        .padding(horizontal = 16.dp)
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(colors.outlineVariant.copy(alpha = 0.65f))
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            SoftCard(color = colors.surfaceContainerLow) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "How this is counted",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = "“Time reclaimed” is an estimate: each app session you close after a pause counts as $reclaimedMinutesPerSkip minutes back. It isn't tracked screen time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    TextAction(
                        text = "Adjust the estimate",
                        onClick = onAdjustEstimate,
                        modifier = Modifier.padding(start = 0.dp),
                        color = colors.tertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun TotalCard(stats: EventSummary) {
    val colors = MaterialTheme.colorScheme
    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        color = colors.surfaceContainerLow
    ) {
        Box {
            Canvas(Modifier.matchParentSize()) {
                drawCircle(
                    color = colors.tertiary.copy(alpha = 0.06f),
                    radius = size.width * 0.42f,
                    center = Offset(size.width * 0.95f, size.height * 0.22f)
                )
                drawCircle(
                    color = colors.secondary.copy(alpha = 0.1f),
                    radius = size.width * 0.28f,
                    center = Offset(size.width * 0.86f, size.height * 0.96f)
                )
            }
            Column(modifier = Modifier.padding(23.dp)) {
                SectionLabel("Reclaimed all time", color = colors.onSurfaceVariant)
                Spacer(Modifier.height(9.dp))
                Text(
                    text = formatLongMinutes(stats.allTimeReclaimedMinutes),
                    style = MaterialTheme.typography.displayMedium,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "across ${stats.totalPauses} ${pluralize(stats.totalPauses, "intentional pause")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceContainerHigh, RoundedCornerShape(16.dp))
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TotalPart("${stats.totalPauses}", "pauses", Modifier.weight(1f))
                    TotalDivider()
                    TotalPart("${stats.totalAvoided}", "left for later", Modifier.weight(1f))
                    TotalDivider()
                    TotalPart("${stats.streakDays}d", "streak", Modifier.weight(1f))
                }
            }
        }
    }
}



@Composable
private fun TotalPart(value: String, label: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = colors.onSurface
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun TotalDivider() {
    val colors = MaterialTheme.colorScheme
    Box(
        Modifier
            .height(30.dp)
            .width(1.dp)
            .background(colors.outlineVariant)
    )
}

@Composable
private fun SmallStat(
    label: String,
    value: String,
    detail: String,
    modifier: Modifier = Modifier,
    tint: Color
) {
    val colors = MaterialTheme.colorScheme
    SoftCard(
        modifier = modifier,
        color = tint.copy(alpha = 0.62f),
        borderColor = Color.Transparent
    ) {
        Column(modifier = Modifier.padding(17.dp)) {
            SectionLabel(label, color = colors.onSurface.copy(alpha = 0.45f))
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = colors.onSurface
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun WeekChartCard(stats: EventSummary) {
    val colors = MaterialTheme.colorScheme
    val maxCount = maxOf(1, stats.lastSevenDays.maxOfOrNull { it.pauses } ?: 0)
    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        color = colors.surfaceContainerLow
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    SectionLabel("Last seven days")
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "${stats.lastSevenDays.sumOf { it.pauses }} ${pluralize(stats.lastSevenDays.sumOf { it.pauses }, "pause")}",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.onSurface
                    )
                }
                Pill(
                    text = "This week",
                    background = colors.secondaryContainer.copy(alpha = 0.55f),
                    borderColor = Color.Transparent
                )
            }
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                stats.lastSevenDays.forEachIndexed { index, day ->
                    DayBar(
                        day = day,
                        maxCount = maxCount,
                        isToday = index == stats.lastSevenDays.lastIndex,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DayBar(
    day: DayStat,
    maxCount: Int,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val fraction = (day.pauses.toFloat() / maxCount.toFloat()).coerceIn(0f, 1f)
    val barHeight by animateFloatAsState(
        targetValue = (if (day.pauses == 0) 0.035f else fraction.coerceAtLeast(0.11f)),
        animationSpec = tween(600),
        label = "barHeight"
    )
    val barColor = when {
        isToday -> colors.tertiary
        day.avoided > 0 -> colors.secondary
        else -> colors.onSurface.copy(alpha = 0.3f)
    }
    val dayLabel = day.date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault())

    Column(
        modifier = modifier.height(150.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (day.pauses > 0) day.pauses.toString() else "",
            style = MaterialTheme.typography.labelSmall,
            color = if (isToday) colors.onSurface else colors.onSurfaceVariant.copy(alpha = 0.55f)
        )
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .width(if (isToday) 24.dp else 20.dp)
                .height((100 * barHeight).dp)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                .background(barColor)
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(if (isToday) colors.onSurface else Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = dayLabel,
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp),
                color = if (isToday) colors.inverseOnSurface else colors.onSurfaceVariant.copy(alpha = 0.65f)
            )
        }
    }
}

@Composable
private fun WeeklyReflection(stats: EventSummary) {
    val colors = MaterialTheme.colorScheme
    val difference = stats.weekAvoided - stats.lastWeekAvoided
    SoftCard(
        color = if (difference >= 0) colors.tertiaryContainer else colors.tertiaryContainer.copy(alpha = 0.5f),
        borderColor = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color.White.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (difference >= 0) "+" else "",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.onSurface
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when {
                        stats.weekAvoided == 0 && stats.lastWeekAvoided == 0 ->
                            "Your first intentional week."
                        difference > 0 ->
                            "You left ${difference} more ${pluralize(difference, "session")} for later than last week."
                        difference < 0 ->
                            "You left ${-difference} fewer ${pluralize(-difference, "session")} for later than last week."
                        else -> "Holding steady from last week."
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.onSurface
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "${stats.weekAvoided} this week · ${stats.lastWeekAvoided} last week",
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
        targetValue = (appStat.pauses.toFloat() / maxPauses.toFloat()).coerceIn(0f, 1f),
        animationSpec = tween(600),
        label = "appProgress"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(packageName = appStat.packageName, icon = info.icon, size = 40.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row {
                Text(
                    text = info.label,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${appStat.pauses}",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(7.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(colors.outlineVariant.copy(alpha = 0.55f), CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(4.dp)
                        .background(if (appStat.avoided > 0) colors.tertiary else colors.secondary, CircleShape)
                )
            }
            Spacer(Modifier.height(5.dp))
            Text(
                text = "${appStat.avoided} of ${appStat.pauses} closed instead",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant.copy(alpha = 0.67f)
            )
        }
    }
}

@Composable
private fun HowStatsWorkCard() {
    val colors = MaterialTheme.colorScheme
    SoftCard(color = colors.surfaceContainerLow) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Your data stays here.",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = "Pause history lives only on this phone. No account, no analytics, and no internet permission.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
    }
}
