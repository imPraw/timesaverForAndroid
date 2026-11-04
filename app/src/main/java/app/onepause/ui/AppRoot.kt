package app.onepause.ui

import android.content.Intent
import androidx.compose.animation.Crossfade
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.onepause.LicensesActivity
import app.onepause.PausePreviewActivity
import app.onepause.data.AppPreferences
import app.onepause.data.EventSummary
import app.onepause.data.PauseEventStore
import app.onepause.data.StatsCalculator
import app.onepause.ui.apps.AppsScreen
import app.onepause.ui.websites.WebsitesScreen
import app.onepause.ui.components.BackgroundWash
import app.onepause.ui.components.NavSurface
import app.onepause.ui.home.HomeScreen
import app.onepause.ui.settings.SettingsScreen
import app.onepause.ui.stats.StatsScreen
import app.onepause.util.isAccessibilityServiceEnabled
import app.onepause.util.openAccessibilitySettings

private enum class AppTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Rounded.Home),
    APPS("Apps", Icons.Rounded.GridView),
    WEBSITES("Websites", Icons.Rounded.Language),
    INSIGHTS("Insights", Icons.Rounded.BarChart),
    SETTINGS("Settings", Icons.Rounded.Settings)
}

@Composable
fun MainShell(
    preferences: AppPreferences,
    serviceEnabled: Boolean,
    resumeTick: Int,
    appearanceMode: String,
    onAppearanceModeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    var selectedTabName by rememberSaveable {
        mutableStateOf(
            if (preferences.blockedPackages.isEmpty() && preferences.blockedDomains.isEmpty()) AppTab.APPS.name else AppTab.HOME.name
        )
    }
    val selectedTab = AppTab.valueOf(selectedTabName)
    val tabStates = rememberSaveableStateHolder()
    val focusManager = LocalFocusManager.current
    BackHandler(enabled = selectedTab != AppTab.HOME) { selectedTabName = AppTab.HOME.name }
    LaunchedEffect(selectedTab) { focusManager.clearFocus() }

    var selectedPackages by remember { mutableStateOf(preferences.blockedPackages) }
    var selectedDomains by remember { mutableStateOf(preferences.blockedDomains) }
    var events by remember { mutableStateOf(PauseEventStore.all(context)) }
    var pauseSeconds by remember { mutableIntStateOf(preferences.pauseSeconds) }
    var reclaimedMinutes by remember { mutableIntStateOf(preferences.reclaimedMinutesPerSkip) }
    var hapticsEnabled by remember { mutableStateOf(preferences.hapticsEnabled) }

    LaunchedEffect(resumeTick) {
        selectedPackages = preferences.blockedPackages
        selectedDomains = preferences.blockedDomains
        events = PauseEventStore.all(context)
        pauseSeconds = preferences.pauseSeconds
        reclaimedMinutes = preferences.reclaimedMinutesPerSkip
        hapticsEnabled = preferences.hapticsEnabled
    }

    val stats: EventSummary = remember(events, reclaimedMinutes) {
        StatsCalculator.calculate(events, reclaimedMinutes)
    }

    val updateSelection: (Set<String>) -> Unit = { next ->
        selectedPackages = next
        preferences.blockedPackages = next
    }

    val openProtection: () -> Unit = {
        openAccessibilitySettings(context)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Crossfade(
            targetState = selectedTab,
            animationSpec = tween(220),
            label = "tab"
        ) { tab ->
            tabStates.SaveableStateProvider(tab.name) {
            when (tab) {
                AppTab.HOME -> HomeScreen(
                    stats = stats,
                    blockedCount = selectedPackages.size,
                    blockedWebsiteCount = selectedDomains.size,
                    onManageWebsites = { selectedTabName = AppTab.WEBSITES.name },
                    serviceEnabled = serviceEnabled,
                    pauseSeconds = pauseSeconds,
                    onManageApps = { selectedTabName = AppTab.APPS.name },
                    onEnableProtection = openProtection,
                    onOpenInsights = { selectedTabName = AppTab.INSIGHTS.name },
                    onPreviewPause = {
                        context.startActivity(Intent(context, PausePreviewActivity::class.java))
                    }
                )

                AppTab.APPS -> AppsScreen(
                    selectedPackages = selectedPackages,
                    serviceEnabled = serviceEnabled,
                    onSelectionChange = updateSelection,
                    onEnableProtection = openProtection
                )

                AppTab.WEBSITES -> WebsitesScreen(
                    domains = selectedDomains, serviceEnabled = serviceEnabled,
                    onChange = { selectedDomains = it; preferences.blockedDomains = it }
                )

                AppTab.INSIGHTS -> StatsScreen(
                    stats = stats,
                    reclaimedMinutesPerSkip = reclaimedMinutes,
                    onManageApps = { selectedTabName = AppTab.APPS.name },
                    onAdjustEstimate = { selectedTabName = AppTab.SETTINGS.name }
                )

                AppTab.SETTINGS -> SettingsScreen(
                    appearanceMode = appearanceMode,
                    onAppearanceModeChange = onAppearanceModeChange,
                    pauseSeconds = pauseSeconds,
                    reclaimedMinutesPerSkip = reclaimedMinutes,
                    hapticsEnabled = hapticsEnabled,
                    serviceEnabled = serviceEnabled,
                    eventCount = events.size,
                    onPauseSecondsChange = { seconds ->
                        pauseSeconds = seconds
                        preferences.pauseSeconds = seconds
                    },
                    onReclaimedMinutesChange = { minutes ->
                        reclaimedMinutes = minutes
                        preferences.reclaimedMinutesPerSkip = minutes
                    },
                    onHapticsChange = { enabled ->
                        hapticsEnabled = enabled
                        preferences.hapticsEnabled = enabled
                    },
                    onOpenAccessibilitySettings = {
                        openAccessibilitySettings(context)
                    },
                    onEraseHistory = {
                        PauseEventStore.clear(context)
                        events = emptyList()
                    },
                    onOpenLicenses = {
                        context.startActivity(Intent(context, LicensesActivity::class.java))
                    }
                )
            }
            }
        }

        if (WindowInsets.ime.getBottom(LocalDensity.current) == 0) BottomNavigation(
            selected = selectedTab,
            onSelect = { selectedTabName = it.name },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun BottomNavigation(
    selected: AppTab,
    onSelect: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        color = colors.surface.copy(alpha = 0.98f),
        shadowElevation = 6.dp
    ) {
        Column {
        app.onepause.ui.components.SurfaceDivider()
        Row(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp).selectableGroup(),
            verticalAlignment = Alignment.CenterVertically
        ) {
        AppTab.entries.forEach { tab ->
            val active = selected == tab
            val tint by androidx.compose.animation.animateColorAsState(
                if (active) colors.primary else colors.onSurfaceVariant,
                animationSpec = tween(160), label = "navigationTint"
            )
            Column(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp))
                    .selectable(selected = active, role = Role.Tab, onClick = { onSelect(tab) })
                    .heightIn(min = 60.dp).padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(tab.icon, contentDescription = null, tint = tint, modifier = Modifier.size(23.dp))
                Spacer(Modifier.height(4.dp))
                Text(tab.label, color = tint, style = MaterialTheme.typography.labelSmall,
                    maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    fontWeight = if (active) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium)
            }
        }
        }
        }
    }
}
