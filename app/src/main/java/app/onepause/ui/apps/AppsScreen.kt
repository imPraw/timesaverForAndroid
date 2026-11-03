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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.onepause.ui.components.AppIcon
import app.onepause.ui.components.ChoiceChip
import app.onepause.ui.components.OnePauseSwitch
import app.onepause.ui.components.Pill
import app.onepause.ui.components.PrimaryButton
import app.onepause.ui.components.ScreenHeader
import app.onepause.ui.components.SectionLabel
import app.onepause.ui.components.SoftCard
import app.onepause.util.InstalledApp
import app.onepause.util.loadLaunchableApps
import app.onepause.util.suggestedPackages
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    var apps by remember { mutableStateOf<List<InstalledApp>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var protectedOnly by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        apps = withContext(Dispatchers.IO) {
            runCatching { loadLaunchableApps(context) }.getOrDefault(emptyList())
        }
        loading = false
    }

    val filteredApps = remember(apps, searchQuery, protectedOnly, selectedPackages) {
        apps.filter { app ->
            (!protectedOnly || app.packageName in selectedPackages) &&
                (searchQuery.isBlank() ||
                    app.label.contains(searchQuery.trim(), ignoreCase = true))
        }
    }
    val popularSuggestions = remember(apps, selectedPackages) {
        apps.filter { it.packageName in suggestedPackages && it.packageName !in selectedPackages }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding(),
        contentPadding = PaddingValues(bottom = 132.dp)
    ) {
        item {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                ScreenHeader(
                    overline = "Protected apps",
                    title = "Choose your friction.",
                    subtitle = "Pick the apps that tend to pull you in. One Pause steps in before only these open."
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Pill(
                        text = "${selectedPackages.size} protected",
                        background = if (selectedPackages.isNotEmpty()) colors.secondaryContainer else colors.surfaceContainerLow,
                        borderColor = if (selectedPackages.isNotEmpty()) Color.Transparent else colors.outlineVariant
                    )
                    if (!serviceEnabled) {
                        Pill(
                            text = "protection off",
                            background = colors.tertiaryContainer.copy(alpha = 0.75f),
                            borderColor = Color.Transparent
                        )
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Search your apps",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Clear search",
                                    tint = colors.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else {
                        null
                    },
                    singleLine = true,
                    shape = CircleShape,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.surfaceContainerLow,
                        unfocusedContainerColor = colors.surfaceContainerLow,
                        focusedBorderColor = colors.onSurface.copy(alpha = 0.55f),
                        unfocusedBorderColor = colors.outlineVariant,
                        cursorColor = colors.tertiary,
                        focusedTextColor = colors.onSurface,
                        unfocusedTextColor = colors.onSurface
                    )
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChoiceChip(
                        text = "All apps",
                        selected = !protectedOnly,
                        onClick = { protectedOnly = false }
                    )
                    ChoiceChip(
                        text = if (selectedPackages.isEmpty()) {
                            "Protected"
                        } else {
                            "Protected · ${selectedPackages.size}"
                        },
                        selected = protectedOnly,
                        onClick = { protectedOnly = true }
                    )
                }
            }
        }

        if (!serviceEnabled) {
            item {
                Box(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    SoftCard(
                        color = colors.tertiaryContainer.copy(alpha = 0.7f),
                        borderColor = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(colors.tertiary.copy(alpha = 0.22f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = null,
                                    tint = colors.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.width(11.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Protection is off",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colors.onSurface
                                )
                                Text(
                                    text = "Your choices are saved, but pauses can't appear yet.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            IconButton(onClick = onEnableProtection) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                    contentDescription = "Turn on accessibility",
                                    tint = colors.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        if (popularSuggestions.isNotEmpty() && !protectedOnly && searchQuery.isBlank()) {
            item {
                Box(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    SoftCard(
                        color = colors.tertiaryContainer,
                        borderColor = Color.Transparent
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    SectionLabel("A place to start", color = colors.onSurface.copy(alpha = 0.48f))
                                    Spacer(Modifier.height(5.dp))
                                    Text(
                                        text = "The usual time thieves",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = colors.onSurface
                                    )
                                }
                                Row {
                                    popularSuggestions.take(3).forEachIndexed { index, app ->
                                        Box(
                                            modifier = Modifier
                                                .padding(start = if (index == 0) 0.dp else 4.dp)
                                                .size(32.dp)
                                                .background(Color.White.copy(alpha = 0.6f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AppIcon(
                                                packageName = app.packageName,
                                                icon = app.icon,
                                                size = 24.dp
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "${popularSuggestions.size} popular social and entertainment apps are installed on this phone.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurface.copy(alpha = 0.58f)
                            )
                            Spacer(Modifier.height(13.dp))
                            PrimaryButton(
                                text = "Add ${popularSuggestions.size} popular apps",
                                onClick = {
                                    onSelectionChange(selectedPackages + popularSuggestions.map { it.packageName })
                                },
                                modifier = Modifier.fillMaxWidth(),
                                containerColor = colors.onSurface,
                                contentColor = colors.inverseOnSurface
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionLabel(
                    text = when {
                        protectedOnly -> "Your protected apps"
                        searchQuery.isNotBlank() -> "Search results"
                        else -> "All apps"
                    }
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${filteredApps.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }

        if (loading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 54.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = colors.tertiary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(25.dp)
                    )
                }
            }
        } else if (filteredApps.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 42.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(colors.secondaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = colors.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.height(13.dp))
                        Text(
                            text = if (protectedOnly) "Nothing protected yet." else "No apps found.",
                            style = MaterialTheme.typography.headlineSmall,
                            color = colors.onSurface
                        )
                        Spacer(Modifier.height(5.dp))
                        Text(
                            text = if (protectedOnly) {
                                "Pick an app from the full list to add a pause."
                            } else {
                                "Try a different name."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            itemsIndexed(
                items = filteredApps,
                key = { _, app -> app.packageName }
            ) { index, app ->
                val selected = app.packageName in selectedPackages
                val isFirst = index == 0
                val isLast = index == filteredApps.lastIndex
                val shape = RoundedCornerShape(
                    topStart = if (isFirst) 24.dp else 0.dp,
                    topEnd = if (isFirst) 24.dp else 0.dp,
                    bottomStart = if (isLast) 24.dp else 0.dp,
                    bottomEnd = if (isLast) 24.dp else 0.dp
                )
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Surface(color = colors.surfaceContainerLow, shape = shape) {
                        Column {
                            AppRow(
                                app = app,
                                selected = selected,
                                isPopular = app.packageName in suggestedPackages,
                                onToggle = {
                                    onSelectionChange(
                                        if (selected) {
                                            selectedPackages - app.packageName
                                        } else {
                                            selectedPackages + app.packageName
                                        }
                                    )
                                }
                            )
                            if (!isLast) {
                                Box(
                                    Modifier
                                        .padding(start = 72.dp)
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
            Text(
                text = "One Pause only sees which app is in front. It never reads what's on your screen.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant.copy(alpha = 0.65f),
                modifier = Modifier.padding(horizontal = 26.dp, vertical = 22.dp)
            )
        }
    }
}

@Composable
private fun AppRow(
    app: InstalledApp,
    selected: Boolean,
    isPopular: Boolean,
    onToggle: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 15.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(
            packageName = app.packageName,
            icon = app.icon,
            size = 42.dp
        )
        Spacer(Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (isPopular) {
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = "POPULAR",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, letterSpacing = 0.9.sp),
                        color = colors.tertiary,
                        modifier = Modifier
                            .background(colors.tertiaryContainer.copy(alpha = 0.5f), CircleShape)
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = if (selected) "Pause before opening" else "No pause",
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) colors.onSurfaceVariant else colors.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
        Spacer(Modifier.width(10.dp))
        OnePauseSwitch(
            checked = selected,
            onCheckedChange = { onToggle() }
        )
    }
}
