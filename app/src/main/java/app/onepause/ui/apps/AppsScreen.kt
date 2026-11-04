package app.onepause.ui.apps

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.onepause.ui.components.AppIcon
import app.onepause.ui.components.ChoiceChip
import app.onepause.ui.components.EmptyState
import app.onepause.ui.components.PrimaryButton
import app.onepause.ui.components.ScreenHeader
import app.onepause.ui.components.SectionLabel
import app.onepause.ui.components.SoftCard
import app.onepause.util.InstalledApp
import app.onepause.util.loadLaunchableApps
import app.onepause.util.suggestedPackages
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collect

@Composable
fun AppsScreen(
    selectedPackages: Set<String>,
    serviceEnabled: Boolean,
    onSelectionChange: (Set<String>) -> Unit,
    onEnableProtection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var apps by remember { mutableStateOf<List<InstalledApp>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var loadFailed by remember { mutableStateOf(false) }
    var loadAttempt by remember { mutableIntStateOf(0) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var protectedOnly by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    var savedScrollIndex by rememberSaveable { mutableIntStateOf(0) }
    var savedScrollOffset by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(loadAttempt) {
        loading = true
        loadFailed = false
        try {
            apps = loadLaunchableApps(context)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            loadFailed = true
        }
        loading = false
    }

    // Keep the last position while the catalog reloads after returning to this tab.
    // Restoring against the temporary loading row would otherwise clamp it to the top.
    LaunchedEffect(loading, loadFailed) {
        if (!loading && !loadFailed) {
            listState.scrollToItem(savedScrollIndex, savedScrollOffset)
            snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
                .collect { (index, offset) ->
                    savedScrollIndex = index
                    savedScrollOffset = offset
                }
        }
    }

    val filteredApps = remember(apps, searchQuery, protectedOnly, selectedPackages) {
        apps.filter { app ->
            (!protectedOnly || app.packageName in selectedPackages) &&
                (searchQuery.isBlank() || app.label.contains(searchQuery.trim(), ignoreCase = true))
        }
    }
    val popularSuggestions = remember(apps, selectedPackages) {
        apps.filter { it.packageName in suggestedPackages && it.packageName !in selectedPackages }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().statusBarsPadding().imePadding(),
        state = listState,
        contentPadding = PaddingValues(bottom = 132.dp)
    ) {
        item(key = "header") {
            ScreenHeader(
                overline = "Make room for what matters",
                title = "Apps",
                subtitle = "A moment to choose before the apps you reach for.",
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 18.dp, bottom = 24.dp)
            )
        }

        item(key = "search") {
            Column(Modifier.padding(horizontal = 20.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search apps") },
                    leadingIcon = {
                        Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(21.dp))
                    },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear search", modifier = Modifier.size(19.dp))
                            }
                        }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.surfaceContainerLow,
                        unfocusedContainerColor = colors.surfaceContainerLow,
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = colors.primary
                    )
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChoiceChip(
                        text = "All apps",
                        selected = !protectedOnly,
                        onClick = { protectedOnly = false },
                        modifier = Modifier.weight(1f)
                    )
                    ChoiceChip(
                        text = "Selected · ${selectedPackages.size}",
                        selected = protectedOnly,
                        onClick = { protectedOnly = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (!serviceEnabled) {
            item(key = "protection") {
                SoftCard(
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp),
                    color = colors.primaryContainer,
                    borderColor = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(role = Role.Button, onClick = onEnableProtection)
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Shield, contentDescription = null, tint = colors.onPrimaryContainer, modifier = Modifier.size(23.dp))
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Turn on protection", style = MaterialTheme.typography.titleSmall, color = colors.onPrimaryContainer)
                            Spacer(Modifier.height(3.dp))
                            Text("Your choices are saved. Enable accessibility to start pausing.", style = MaterialTheme.typography.bodySmall, color = colors.onPrimaryContainer)
                        }
                        Spacer(Modifier.width(12.dp))
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = colors.onPrimaryContainer, modifier = Modifier.size(19.dp))
                    }
                }
            }
        }

        if (popularSuggestions.isNotEmpty() && !protectedOnly && searchQuery.isBlank()) {
            item(key = "suggestions") {
                SoftCard(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        SectionLabel("A place to start")
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                popularSuggestions.take(3).forEach { app ->
                                    AppIcon(packageName = app.packageName, icon = app.icon, size = 32.dp)
                                }
                            }
                            Spacer(Modifier.width(14.dp))
                            Text("A little less autopilot.", style = MaterialTheme.typography.titleMedium, color = colors.onSurface, modifier = Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Start with ${popularSuggestions.size} social and entertainment ${if (popularSuggestions.size == 1) "app" else "apps"}. You can change your choices anytime.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant
                        )
                        Spacer(Modifier.height(18.dp))
                        PrimaryButton(
                            text = "Select ${popularSuggestions.size} suggested ${if (popularSuggestions.size == 1) "app" else "apps"}",
                            onClick = { onSelectionChange(selectedPackages + popularSuggestions.map { it.packageName }) },
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = colors.primaryContainer,
                            contentColor = colors.onPrimaryContainer
                        )
                    }
                }
            }
        }

        item(key = "list_title") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 26.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionLabel(
                    when {
                        searchQuery.isNotBlank() -> "Search results"
                        protectedOnly -> "Selected apps"
                        else -> "Your apps"
                    }
                )
                Spacer(Modifier.weight(1f))
                if (!loading && !loadFailed) {
                    Text("${filteredApps.size}", style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                }
            }
        }

        when {
            loading -> item(key = "loading") {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 44.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CircularProgressIndicator(color = colors.primary, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                    Text("Finding your apps…", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                }
            }
            loadFailed -> item(key = "load_error") {
                EmptyState(
                    title = "Couldn't load your apps",
                    body = "Your saved choices are safe. Try loading the list again.",
                    action = { PrimaryButton("Try again", onClick = { loadAttempt++ }) }
                )
            }
            filteredApps.isEmpty() -> item(key = "empty") {
                EmptyState(
                    title = when {
                        searchQuery.isNotBlank() -> "No matches"
                        protectedOnly -> "Start with one app"
                        else -> "No apps available"
                    },
                    body = when {
                        searchQuery.isNotBlank() -> "Try another app name or clear your search."
                        protectedOnly -> "Choose an app you often open without thinking. A small pause can help."
                        else -> "Launchable apps on this device will appear here."
                    },
                    action = when {
                        searchQuery.isNotBlank() -> ({ PrimaryButton("Clear search", onClick = { searchQuery = "" }) })
                        protectedOnly -> ({ PrimaryButton("Browse apps", onClick = { protectedOnly = false }) })
                        else -> null
                    }
                )
            }
            else -> itemsIndexed(filteredApps, key = { _, app -> app.packageName }) { index, app ->
                val selected = app.packageName in selectedPackages
                val isLast = index == filteredApps.lastIndex
                Surface(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = colors.surfaceContainerLow,
                    shape = RoundedCornerShape(
                        topStart = if (index == 0) 22.dp else 0.dp,
                        topEnd = if (index == 0) 22.dp else 0.dp,
                        bottomStart = if (isLast) 22.dp else 0.dp,
                        bottomEnd = if (isLast) 22.dp else 0.dp
                    )
                ) {
                    Column {
                        AppRow(
                            app = app,
                            selected = selected,
                            serviceEnabled = serviceEnabled,
                            onToggle = {
                                onSelectionChange(if (selected) selectedPackages - app.packageName else selectedPackages + app.packageName)
                            }
                        )
                        if (!isLast) {
                            Box(Modifier.padding(start = 74.dp).fillMaxWidth().height(0.5.dp).background(colors.outlineVariant))
                        }
                    }
                }
            }
        }

        item(key = "privacy") {
            Text(
                text = "Your selections stay on this device. Change them anytime.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 26.dp, vertical = 20.dp)
            )
        }
    }
}

@Composable
private fun AppRow(
    app: InstalledApp,
    selected: Boolean,
    serviceEnabled: Boolean,
    onToggle: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = selected, role = Role.Switch, onValueChange = { onToggle() })
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(packageName = app.packageName, icon = app.icon, size = 44.dp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(app.label, style = MaterialTheme.typography.titleMedium, color = colors.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(3.dp))
            Text(
                text = when {
                    selected && serviceEnabled -> "Pause before opening"
                    selected -> "Selected · protection off"
                    else -> "Opens as usual"
                },
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = selected, onCheckedChange = null)
    }
}
