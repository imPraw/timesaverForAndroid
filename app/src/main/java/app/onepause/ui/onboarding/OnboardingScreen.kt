package app.onepause.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.onepause.ui.components.BrandMark
import app.onepause.ui.components.PrimaryButton
import app.onepause.ui.components.SoftCard
import app.onepause.ui.components.TextAction

@Composable
fun OnboardingScreen(
    serviceEnabled: Boolean,
    onOpenAccessibilitySettings: () -> Unit,
    onFinish: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var step by rememberSaveable { mutableIntStateOf(0) }
    BackHandler(enabled = step > 0) { step-- }

    Column(
        Modifier.fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 24.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 60.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (step > 0) {
                IconButton(onClick = { step-- }, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Previous step", tint = colors.onSurface, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(4.dp))
            } else {
                BrandMark(size = 28.dp, accent = colors.primary)
                Spacer(Modifier.width(10.dp))
            }
            Text("One Pause", style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
            Spacer(Modifier.weight(1f))
            Text(
                "${step + 1} of 3",
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
                modifier = Modifier.semantics { contentDescription = "Step ${step + 1} of 3" }
            )
        }

        AnimatedContent(
            targetState = step,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            transitionSpec = { fadeIn(tween(240, delayMillis = 40)) togetherWith fadeOut(tween(160)) },
            label = "onboardingStep"
        ) { current ->
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 20.dp, bottom = 24.dp)
            ) {
                when (current) {
                    0 -> WelcomeStep()
                    1 -> HowItWorksStep()
                    else -> PermissionStep(serviceEnabled)
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        PrimaryButton(
            text = when (step) {
                0 -> "Get started"
                1 -> "Set up protection"
                else -> if (serviceEnabled) "Choose my apps" else "Open Android settings"
            },
            onClick = {
                when (step) {
                    0 -> step = 1
                    1 -> step = 2
                    else -> if (serviceEnabled) onFinish() else onOpenAccessibilitySettings()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            icon = if (step == 0) Icons.AutoMirrored.Rounded.ArrowForward else null
        )
        if (step == 2 && !serviceEnabled) {
            TextAction(
                text = "Set up later",
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth(),
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        } else {
            Spacer(Modifier.height(20.dp))
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun WelcomeStep() {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(12.dp))
        WelcomeOrb()
        Spacer(Modifier.height(28.dp))
        Text(
            "A moment\nfor you.",
            style = MaterialTheme.typography.displaySmall,
            color = colors.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "One deliberate breath before the apps that pull you in. A little room to choose what comes next.",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Spacer(Modifier.height(28.dp))
        Text(
            "Your attention. Your choice.",
            style = MaterialTheme.typography.labelLarge,
            color = colors.primary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HowItWorksStep() {
    val colors = MaterialTheme.colorScheme
    Text("A breath between\nimpulse and action.", style = MaterialTheme.typography.headlineLarge, color = colors.onSurface)
    Spacer(Modifier.height(12.dp))
    Text(
        "Choose the apps and websites you want a little distance from. One Pause takes it from there.",
        style = MaterialTheme.typography.bodyLarge,
        color = colors.onSurfaceVariant
    )
    Spacer(Modifier.height(28.dp))
    SoftCard {
        Column(Modifier.padding(20.dp)) {
            StepRow("1", "You reach for an app", "One Pause notices when something you've protected opens.")
            Spacer(Modifier.height(24.dp))
            StepRow("2", "You take a breath", "A calm screen gives you a few seconds to pause.")
            Spacer(Modifier.height(24.dp))
            StepRow("3", "You choose", "Leave it for later, or continue on purpose. You're in control.")
        }
    }
    Spacer(Modifier.height(20.dp))
    Text(
        "You can adjust the pause duration anytime in Settings.",
        style = MaterialTheme.typography.bodySmall,
        color = colors.onSurfaceVariant
    )
}

@Composable
private fun PermissionStep(serviceEnabled: Boolean) {
    val colors = MaterialTheme.colorScheme
    Text(
        if (serviceEnabled) "You're ready\nto pause." else "A little access.\nA clear purpose.",
        style = MaterialTheme.typography.headlineLarge,
        color = colors.onSurface
    )
    Spacer(Modifier.height(12.dp))
    Text(
        "Accessibility access lets One Pause detect protected apps and check supported browser address bars for your chosen websites.",
        style = MaterialTheme.typography.bodyLarge,
        color = colors.onSurfaceVariant
    )
    Spacer(Modifier.height(24.dp))
    SoftCard {
        Column(Modifier.padding(20.dp)) {
            PrivacyRow(
                Icons.Rounded.Smartphone,
                "What One Pause checks",
                "The foreground app and supported browser address bars, to show your breathing pause."
            )
            Spacer(Modifier.height(20.dp))
            PrivacyRow(
                Icons.Rounded.VisibilityOff,
                "What it doesn't collect",
                "Messages, passwords, or keystrokes. Visited addresses are checked locally and aren't saved."
            )
            Spacer(Modifier.height(20.dp))
            PrivacyRow(
                Icons.Rounded.Lock,
                "Everything stays here",
                "No account, tracking, or internet permission. Your settings and history stay on this phone."
            )
        }
    }
    Spacer(Modifier.height(20.dp))
    if (serviceEnabled) {
        Row(
            Modifier.fillMaxWidth().background(colors.primaryContainer, RoundedCornerShape(16.dp)).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Check, null, tint = colors.onPrimaryContainer, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text("Protection is on", style = MaterialTheme.typography.labelLarge, color = colors.onPrimaryContainer)
        }
    } else {
        Text(
            "In Android Settings, find One Pause under Installed apps, then enable “One Pause app protection.” Return here when you're done.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
    }
}

@Composable
private fun WelcomeOrb() {
    val colors = MaterialTheme.colorScheme
    Box(Modifier.size(200.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f
            drawCircle(colors.outlineVariant, radius = radius * 0.94f, style = Stroke(width = 1.dp.toPx()))
            drawCircle(colors.primary.copy(alpha = 0.12f), radius = radius * 0.76f)
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(colors.primaryContainer, colors.primary.copy(alpha = 0.8f)),
                    center = Offset(center.x - radius * 0.2f, center.y - radius * 0.3f),
                    radius = radius * 1.1f
                ),
                radius = radius * 0.57f
            )
            drawCircle(colors.primary, radius = 4.dp.toPx(), center = Offset(center.x + radius * 0.86f, center.y + radius * 0.37f))
        }
        BrandMark(size = 46.dp, accent = colors.onPrimaryContainer)
    }
}

@Composable
private fun StepRow(number: String, title: String, body: String) {
    val colors = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.Top) {
        Box(
            Modifier.size(30.dp).background(colors.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(number, style = MaterialTheme.typography.labelMedium, color = colors.onPrimaryContainer)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(5.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun PrivacyRow(icon: ImageVector, title: String, body: String) {
    val colors = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, modifier = Modifier.padding(top = 2.dp).size(20.dp), tint = colors.primary)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(5.dp))
            Text(body, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
    }
}
