package app.onepause.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.onepause.ui.components.BrandMark
import app.onepause.ui.components.PrimaryButton
import app.onepause.ui.components.TextAction
import app.onepause.ui.theme.ForestMist
import app.onepause.ui.theme.SurfaceCream

@Composable
fun OnboardingScreen(
    serviceEnabled: Boolean,
    onOpenAccessibilitySettings: () -> Unit,
    onFinish: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var step by remember { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()

    LaunchedEffect(step) {
        scrollState.scrollTo(0)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(colors.background, colors.surfaceContainer, colors.surfaceContainerHigh)
                )
            )
    ) {
        OnboardingAtmosphere(Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 0) {
                    IconButton(
                        onClick = { step-- },
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color.White.copy(alpha = 0.07f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.onSurface.copy(alpha = 0.9f),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                }
                BrandMark(size = 27.dp)
                Spacer(Modifier.width(9.dp))
                Text(
                    text = "ONE PAUSE",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurface.copy(alpha = 0.8f),
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "0${step + 1} / 03",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurface.copy(alpha = 0.4f)
                )
            }

            Spacer(Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "onboardingStep"
                ) { current ->
                    when (current) {
                        0 -> WelcomeStep(scrollState)
                        1 -> HowItWorksStep(scrollState)
                        else -> PermissionStep(scrollState, serviceEnabled)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .width(if (index == step) 22.dp else 7.dp)
                            .height(7.dp)
                            .background(
                                if (index == step) colors.tertiary else colors.onSurface.copy(alpha = 0.18f),
                                CircleShape
                            )
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            PrimaryButton(
                text = when (step) {
                    0 -> "Show me how"
                    1 -> "Set up protection"
                    else -> if (serviceEnabled) "Choose my apps" else "Turn on protection"
                },
                onClick = {
                    when (step) {
                        0 -> step = 1
                        1 -> step = 2
                        else -> if (serviceEnabled) onFinish() else onOpenAccessibilitySettings()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                containerColor = colors.onSurface,
                contentColor = colors.background,
                icon = when {
                    step == 0 -> Icons.AutoMirrored.Rounded.ArrowForward
                    step == 2 && !serviceEnabled -> Icons.Rounded.Lock
                    else -> null
                }
            )

            if (step == 2 && !serviceEnabled) {
                TextAction(
                    text = "I'll do this later",
                    onClick = onFinish,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    color = colors.onSurface.copy(alpha = 0.58f),
                    textAlign = TextAlign.Center
                )
            } else {
                Spacer(Modifier.height(14.dp))
            }

            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun WelcomeStep(scrollState: androidx.compose.foundation.ScrollState) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(22.dp))
        WelcomeOrb()
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Make room\nfor now.",
            style = MaterialTheme.typography.displayLarge,
            color = colors.onSurface,
            textAlign = TextAlign.Center,
            lineHeight = 54.sp
        )
        Spacer(Modifier.height(18.dp))
        Text(
            text = "A small, deliberate pause before the apps that pull you in. Not a lock. A chance to choose.",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface.copy(alpha = 0.62f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(26.dp))
    }
}

@Composable
private fun HowItWorksStep(scrollState: androidx.compose.foundation.ScrollState) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Spacer(Modifier.height(22.dp))
        Text(
            text = "Built for the\nspace between.",
            style = MaterialTheme.typography.displaySmall,
            color = colors.onSurface,
            lineHeight = 41.sp
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "You choose the apps. One Pause adds a single breath before they open.",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(26.dp))
        DiagramCard()
        Spacer(Modifier.height(22.dp))
        StepRow(
            number = "01",
            title = "You reach for an app",
            body = "It's a reflex, not a decision. One Pause notices the moment."
        )
        StepRow(
            number = "02",
            title = "One breath gets in the way",
            body = "A calm screen holds the space for a few seconds."
        )
        StepRow(
            number = "03",
            title = "Then, you choose",
            body = "Close it and move on, or open the app on purpose."
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun PermissionStep(
    scrollState: androidx.compose.foundation.ScrollState,
    serviceEnabled: Boolean
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Spacer(Modifier.height(22.dp))
        Text(
            text = "One permission.\nNo surveillance.",
            style = MaterialTheme.typography.displaySmall,
            color = colors.onSurface,
            lineHeight = 41.sp
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Android needs Accessibility access so One Pause can notice when a protected app comes to the front.",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface.copy(alpha = 0.62f)
        )
        Spacer(Modifier.height(22.dp))
        PermissionCard()
        Spacer(Modifier.height(14.dp))
        PrivacyRow(
            icon = Check,
            title = "What it can do",
            body = "See which app is in front and show the pause screen."
        )
        PrivacyRow(
            icon = VisibilityOff,
            title = "What it can't do",
            body = "Read your screen, messages, passwords, or taps."
        )
        PrivacyRow(
            icon = Icons.Rounded.Lock,
            title = "Where data goes",
            body = "Nowhere. There is no account and no internet permission."
        )
        Spacer(Modifier.height(16.dp))
        if (serviceEnabled) {
            Row(
                modifier = Modifier
                    .background(ForestMist.copy(alpha = 0.14f), CircleShape)
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(colors.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = colors.background,
                        modifier = Modifier.size(12.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Protection is on",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurface
                )
            }
        } else {
            Text(
                text = "After tapping the button, find One Pause under Installed apps, then turn on “One Pause app protection.”",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface.copy(alpha = 0.42f)
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun WelcomeOrb() {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier.size(214.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = center
            val radius = size.minDimension / 2f
            drawCircle(
                color = colors.onSurface.copy(alpha = 0.1f),
                radius = radius * 0.95f,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = colors.onSurface.copy(alpha = 0.07f),
                radius = radius * 0.79f,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(colors.primaryContainer, colors.primary, colors.primaryContainer),
                    center = Offset(center.x - radius * 0.2f, center.y - radius * 0.28f),
                    radius = radius * 0.9f
                ),
                radius = radius * 0.57f
            )
            drawArc(
                color = colors.onSurface.copy(alpha = 0.32f),
                startAngle = 205f,
                sweepAngle = 70f,
                useCenter = false,
                topLeft = Offset(radius * 0.24f, radius * 0.24f),
                size = androidx.compose.ui.geometry.Size(radius * 1.1f, radius * 1.1f),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
            drawCircle(
                color = colors.tertiary.copy(alpha = 0.9f),
                radius = 3.dp.toPx(),
                center = Offset(center.x + radius * 0.67f, center.y + radius * 0.22f)
            )
        }
        BrandMark(size = 54.dp, accent = colors.onSurface)
    }
}

@Composable
private fun DiagramCard() {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(25.dp))
            .padding(horizontal = 18.dp, vertical = 19.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DiagramNode(
                icon = Icons.Rounded.Smartphone,
                label = "An app",
                tint = colors.tertiary
            )
            DiagramArrow()
            DiagramNode(
                icon = null,
                label = "A breath",
                tint = colors.primary,
                custom = {
                    BrandMark(size = 21.dp, accent = colors.primary)
                }
            )
            DiagramArrow()
            DiagramNode(
                icon = Icons.Rounded.Check,
                label = "You choose",
                tint = colors.secondary
            )
        }
        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.onSurface.copy(alpha = 0.1f))
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = "A little friction, only where you ask for it.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurface.copy(alpha = 0.48f),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DiagramNode(
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    label: String,
    tint: Color,
    custom: (@Composable () -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(74.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(tint.copy(alpha = 0.14f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (custom != null) {
                custom()
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(Modifier.height(7.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurface.copy(alpha = 0.74f)
        )
    }
}

@Composable
private fun DiagramArrow() {
    val colors = MaterialTheme.colorScheme
    Icon(
        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
        contentDescription = null,
        tint = colors.onSurface.copy(alpha = 0.28f),
        modifier = Modifier.size(16.dp)
    )
}

@Composable
private fun StepRow(number: String, title: String, body: String) {
    val colors = MaterialTheme.colorScheme
    Row(modifier = Modifier.padding(bottom = 18.dp)) {
        Text(
            text = number,
            style = MaterialTheme.typography.labelSmall,
            color = colors.primary,
            modifier = Modifier
                .padding(top = 4.dp)
                .width(28.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface.copy(alpha = 0.48f)
            )
        }
    }
}

@Composable
private fun PermissionCard() {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.onSurface, RoundedCornerShape(24.dp))
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(colors.secondary.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Smartphone,
                    contentDescription = null,
                    tint = colors.surface,
                    modifier = Modifier.size(19.dp)
                )
            }
            Spacer(Modifier.width(11.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Accessibility access",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.surface
                )
                Text(
                    text = "Required for app protection",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.surface.copy(alpha = 0.5f)
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = "Android labels this as Accessibility. One Pause uses it only to notice the app that's on screen.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.surface.copy(alpha = 0.64f)
        )
    }
}

@Composable
private fun PrivacyRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.padding(bottom = 14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(28.dp)
                .background(Color.White.copy(alpha = 0.06f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = colors.onSurface.copy(alpha = 0.85f)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface.copy(alpha = 0.43f)
            )
        }
    }
}

@Composable
private fun OnboardingAtmosphere(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Canvas(modifier = modifier) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(colors.primary.copy(alpha = 0.14f), Color.Transparent),
                center = Offset(size.width * 0.8f, size.height * 0.18f),
                radius = size.width * 0.8f
            ),
            radius = size.width * 0.8f,
            center = Offset(size.width * 0.8f, size.height * 0.18f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(colors.tertiary.copy(alpha = 0.08f), Color.Transparent),
                center = Offset(size.width * 0.05f, size.height * 0.72f),
                radius = size.width * 0.85f
            ),
            radius = size.width * 0.85f,
            center = Offset(size.width * 0.05f, size.height * 0.72f)
        )
    }
}

private val Check = Icons.Rounded.Check
private val VisibilityOff = Icons.Rounded.VisibilityOff
