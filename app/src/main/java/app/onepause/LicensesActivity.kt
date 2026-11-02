package app.onepause

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.onepause.ui.components.BackgroundWash
import app.onepause.ui.components.BrandMark
import app.onepause.ui.components.ScreenHeader
import app.onepause.ui.components.SoftCard
import app.onepause.ui.components.TextAction
import app.onepause.ui.theme.Coral
import app.onepause.ui.theme.ForestMist
import app.onepause.ui.theme.Ink
import app.onepause.ui.theme.Line
import app.onepause.ui.theme.MutedInk
import app.onepause.ui.theme.OnePauseTheme
import app.onepause.ui.theme.Paper
import app.onepause.ui.theme.PauseCream
import app.onepause.ui.theme.SurfaceCream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LicensesActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        setContent {
            OnePauseTheme {
                LicensesScreen(onBack = { finish() })
            }
        }
    }
}

private enum class LicenseSection {
    FONT,
    APACHE
}

@Composable
private fun LicensesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var fontLicense by remember { mutableStateOf("") }
    var apacheLicense by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf<LicenseSection?>(null) }

    LaunchedEffect(Unit) {
        val (font, apache) = withContext(Dispatchers.IO) {
            val fontText = runCatching {
                context.assets.open("licenses/DM-Serif-Display-OFL.txt")
                    .bufferedReader()
                    .use { it.readText() }
            }.getOrDefault("")
            val apacheText = runCatching {
                context.assets.open("licenses/Apache-2.0.txt")
                    .bufferedReader()
                    .use { it.readText() }
            }.getOrDefault("")
            fontText to apacheText
        }
        fontLicense = font
        apacheLicense = apache
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
    ) {
        BackgroundWash(Modifier.matchParentSize())

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(38.dp)
                            .background(ForestMist.copy(alpha = 0.7f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Ink,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Spacer(Modifier.width(11.dp))
                    BrandMark(size = 27.dp)
                    Spacer(Modifier.width(9.dp))
                    Text(
                        text = "ONE PAUSE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedInk,
                        letterSpacing = 2.sp
                    )
                }
            }

            item {
                ScreenHeader(
                    overline = "Notices",
                    title = "Open by design.",
                    subtitle = "One Pause is made with open-source tools and a typeface shared under an open license."
                )
            }

            item {
                AttributionCard(
                    title = "One Pause",
                    body = "The app, its visual design, and its copy are an independent project. One Pause is not affiliated with One Sec or with any of the apps it helps you pause."
                )
            }

            item {
                AttributionCard(
                    title = "DM Serif Display",
                    body = "Copyright 2014–2018 Adobe, with Reserved Font Name “Source”. Copyright 2019 Google LLC. Licensed under the SIL Open Font License, Version 1.1.",
                    action = if (expanded == LicenseSection.FONT) "Hide license" else "Read the license",
                    onAction = {
                        expanded = if (expanded == LicenseSection.FONT) null else LicenseSection.FONT
                    }
                )
            }

            if (expanded == LicenseSection.FONT) {
                item { LicenseText(fontLicense.ifBlank { "Loading license…" }) }
            }

            item {
                AttributionCard(
                    title = "AndroidX · Compose · Kotlin",
                    body = "One Pause is built with Jetpack Compose, AndroidX, Kotlin, and kotlinx.coroutines. These projects are licensed under the Apache License, Version 2.0.",
                    action = if (expanded == LicenseSection.APACHE) "Hide license" else "Read the license",
                    onAction = {
                        expanded = if (expanded == LicenseSection.APACHE) null else LicenseSection.APACHE
                    }
                )
            }

            if (expanded == LicenseSection.APACHE) {
                item { LicenseText(apacheLicense.ifBlank { "Loading license…" }) }
            }

            item {
                Text(
                    text = "Android is a trademark of Google LLC. All other names are the property of their respective owners.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedInk.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun AttributionCard(
    title: String,
    body: String,
    action: String? = null,
    onAction: (() -> Unit)? = null
) {
    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceCream,
        borderColor = Line
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Ink
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MutedInk
            )
            if (action != null && onAction != null) {
                Spacer(Modifier.height(6.dp))
                TextAction(
                    text = action,
                    onClick = onAction,
                    color = Coral,
                    modifier = Modifier.padding(start = 0.dp)
                )
            }
        }
    }
}

@Composable
private fun LicenseText(text: String) {
    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        color = PauseCream.copy(alpha = 0.7f),
        borderColor = Line
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MutedInk,
            modifier = Modifier.padding(18.dp)
        )
    }
}
