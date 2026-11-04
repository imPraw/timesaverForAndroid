package app.onepause.ui.websites

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.onepause.ui.components.EmptyState
import app.onepause.ui.components.PrimaryButton
import app.onepause.ui.components.ScreenHeader
import app.onepause.ui.components.SectionLabel
import app.onepause.ui.components.SoftCard
import app.onepause.util.WebsiteDomain

@Composable
fun WebsitesScreen(domains: Set<String>, serviceEnabled: Boolean, onChange: (Set<String>) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current
    var input by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val sortedDomains = remember(domains) { domains.sorted() }
    val addWebsite: () -> Unit = {
        val domain = WebsiteDomain.parse(input)
        when {
            domain == null -> error = "Enter a website such as instagram.com."
            domain in domains -> error = "$domain is already in your list."
            else -> {
                onChange(domains + domain)
                input = ""
                error = null
                focusManager.clearFocus()
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding().imePadding(),
        contentPadding = PaddingValues(bottom = 132.dp)
    ) {
        item(key = "header") {
            ScreenHeader(
                overline = "A more intentional internet",
                title = "Websites",
                subtitle = "Bring the same small pause to the sites you visit.",
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 18.dp, bottom = 24.dp)
            )
        }
        item(key = "add_website") {
            SoftCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Text("Add a website", style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                    Spacer(Modifier.height(6.dp))
                    Text("Enter a domain or paste a link.", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                    Spacer(Modifier.height(18.dp))
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it; error = null },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Website") },
                        placeholder = { Text("instagram.com") },
                        leadingIcon = { Icon(Icons.Rounded.Language, contentDescription = null, modifier = Modifier.size(21.dp)) },
                        singleLine = true,
                        isError = error != null,
                        shape = RoundedCornerShape(14.dp),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            autoCorrectEnabled = false,
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { addWebsite() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.outlineVariant,
                            cursorColor = colors.primary
                        )
                    )
                    if (error != null) {
                        Text(
                            text = error.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.error,
                            modifier = Modifier.padding(top = 8.dp).semantics { liveRegion = LiveRegionMode.Polite }
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    PrimaryButton(
                        text = "Add website",
                        onClick = addWebsite,
                        enabled = input.isNotBlank(),
                        icon = Icons.Rounded.Add,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        if (!serviceEnabled) {
            item(key = "protection_status") {
                SoftCard(
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp),
                    color = colors.primaryContainer,
                    borderColor = Color.Transparent
                ) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Shield, contentDescription = null, tint = colors.onPrimaryContainer, modifier = Modifier.size(23.dp))
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Protection is off", style = MaterialTheme.typography.titleSmall, color = colors.onPrimaryContainer)
                            Spacer(Modifier.height(3.dp))
                            Text("Your list is saved. Turn on accessibility protection in Settings to start pausing.", style = MaterialTheme.typography.bodySmall, color = colors.onPrimaryContainer)
                        }
                    }
                }
            }
        }
        item(key = "list_title") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 26.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionLabel("Your websites")
                Spacer(Modifier.weight(1f))
                Text("${domains.size}", style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
            }
        }
        if (domains.isEmpty()) {
            item(key = "empty") {
                SoftCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                    EmptyState(
                        title = "Make a little space",
                        body = "Add a site above. One Pause will give you a moment to decide before you continue."
                    )
                }
            }
        } else {
            itemsIndexed(sortedDomains, key = { _, domain -> domain }) { index, domain ->
                val isLast = index == sortedDomains.lastIndex
                Surface(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(
                        topStart = if (index == 0) 22.dp else 0.dp,
                        topEnd = if (index == 0) 22.dp else 0.dp,
                        bottomStart = if (isLast) 22.dp else 0.dp,
                        bottomEnd = if (isLast) 22.dp else 0.dp
                    ),
                    color = colors.surfaceContainerLow
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(42.dp).background(colors.primaryContainer, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Language, contentDescription = null, tint = colors.onPrimaryContainer, modifier = Modifier.size(22.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(domain, style = MaterialTheme.typography.titleMedium, color = colors.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Spacer(Modifier.height(3.dp))
                                Text("Includes subdomains", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                            }
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = { onChange(domains - domain) }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Remove $domain", tint = colors.onSurfaceVariant, modifier = Modifier.size(19.dp))
                            }
                        }
                        if (!isLast) {
                            Box(Modifier.padding(start = 72.dp).fillMaxWidth().height(0.5.dp).background(colors.outlineVariant))
                        }
                    }
                }
            }
        }
        item(key = "browser_note") {
            Column(Modifier.padding(horizontal = 26.dp, vertical = 20.dp)) {
                Text("A note on browsers", style = MaterialTheme.typography.labelLarge, color = colors.onSurface)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Works with supported browsers that expose their address bar. Some browsers, private tabs, and in-app browsers may not be detected. No browser extension is installed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}