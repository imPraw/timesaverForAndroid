package app.onepause.ui.pause

import android.graphics.drawable.Drawable
import android.animation.ValueAnimator
import android.os.SystemClock
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.onepause.ui.components.AppIcon
import app.onepause.ui.components.PrimaryButton
import app.onepause.ui.theme.BodySans
import kotlinx.coroutines.delay
import java.util.Locale

private enum class BreathPhase {
    INHALE,
    EXHALE
}

@Composable
fun PauseScreen(
    appLabel: String?,
    appPackage: String?,
    appIcon: Drawable?,
    pauseSeconds: Int,
    isPreview: Boolean,
    onAvoid: () -> Unit,
    onContinue: () -> Unit,
    onPhaseChange: (Boolean) -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    val dark = colors.background.luminance() < 0.3f
    val atmosphere = if (dark) listOf(Color(0xFF122D33), Color(0xFF35595E), Color(0xFF18373C))
        else listOf(Color(0xFFE3EFEC), Color(0xFFC7DEDA), Color(0xFFF0F5F1))
    val motionEnabled = remember { ValueAnimator.areAnimatorsEnabled() }
    val entrance = remember { Animatable(if (motionEnabled) 0f else 1f) }
    var entered by remember { mutableStateOf(!motionEnabled) }
    var secondsLeft by remember(pauseSeconds) { mutableIntStateOf(pauseSeconds) }
    val breath = remember { Animatable(0f) }
    var phase by remember { mutableStateOf(BreathPhase.INHALE) }
    val phaseCallback by rememberUpdatedState(onPhaseChange)

    LaunchedEffect(Unit) {
        if (motionEnabled) entrance.animateTo(1f, tween(320, easing = FastOutSlowInEasing))
        entered = true
    }

    // Start the timer with the first breath, using elapsed time rather than accumulated delays.
    LaunchedEffect(entered, pauseSeconds) {
        if (!entered) return@LaunchedEffect
        val deadline = SystemClock.elapsedRealtime() + pauseSeconds * 1000L
        do {
            val remaining = deadline - SystemClock.elapsedRealtime()
            secondsLeft = ((remaining.coerceAtLeast(0L) + 999L) / 1000L).toInt()
            if (remaining > 0) delay(minOf(remaining, 100L))
        } while (remaining > 0)
    }

    LaunchedEffect(entered) {
        if (!entered) return@LaunchedEffect
        val breathingEase = Easing { fraction ->
            ((1.0 - kotlin.math.cos(Math.PI * fraction)) / 2.0).toFloat()
        }
        while (true) {
            phase = BreathPhase.INHALE
            phaseCallback(true)
            val inhaleStart = SystemClock.elapsedRealtime()
            if (motionEnabled) breath.animateTo(1f, tween(4000, easing = breathingEase))
            delay((4000L - (SystemClock.elapsedRealtime() - inhaleStart)).coerceAtLeast(16L))
            phase = BreathPhase.EXHALE
            phaseCallback(false)
            val exhaleStart = SystemClock.elapsedRealtime()
            if (motionEnabled) breath.animateTo(0f, tween(4000, easing = breathingEase))
            delay((4000L - (SystemClock.elapsedRealtime() - exhaleStart)).coerceAtLeast(16L))
        }
    }

    val displayName = when {
        isPreview -> "this moment"
        !appLabel.isNullOrBlank() -> appLabel
        else -> "this app"
    }
    val canContinue = secondsLeft == 0

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = entrance.value }
            .background(Brush.verticalGradient(atmosphere))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Brush.radialGradient(listOf(Color(0xFFB9DFD5).copy(alpha = if (dark) .22f else .5f), Color.Transparent),
                center = Offset(size.width * .95f, size.height * .12f), radius = size.height * .65f))
            drawRect(Brush.radialGradient(listOf(Color(0xFFA9D6CC).copy(alpha = .18f), Color.Transparent),
                center = Offset(-size.width * .2f, size.height * .8f), radius = size.width * 1.2f))
        }
        val compact = maxHeight < 690.dp
        val orbSize = minOf(maxWidth - 52.dp, if (compact) 260.dp else 320.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 26.dp)
                .graphicsLayer {
                    val progress = entrance.value
                    translationY = (1f - progress) * 12.dp.toPx()
                    scaleX = 0.985f + 0.015f * progress
                    scaleY = scaleX
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BrandGlyph(size = 22.dp)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "ONE PAUSE",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 2.sp,
                    color = colors.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (isPreview) {
                    Text(
                        text = "Practice",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.primary,
                        modifier = Modifier
                            .background(colors.primaryContainer, CircleShape)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
                Spacer(Modifier.width(6.dp))
                IconButton(onClick = onAvoid, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close the pause",
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // The decision buttons stay in view while the breathing content can scroll.
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(Modifier.height(if (compact) 12.dp else 22.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (!isPreview && appPackage != null) {
                        AppIcon(
                            packageName = appPackage,
                            icon = appIcon,
                            size = 36.dp
                        )
                        Spacer(Modifier.width(10.dp))
                    }
                    Column {
                        Text(
                            text = if (isPreview) "A moment for yourself" else "Before you open",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(Modifier.height(if (compact) 8.dp else 14.dp))

                BreathingOrb(
                    breath = { breath.value },
                    phase = phase,
                    size = orbSize,
                )

                Spacer(Modifier.height(if (compact) 14.dp else 24.dp))

                AnimatedContent(
                    targetState = canContinue,
                    transitionSpec = {
                        fadeIn(tween(420)) togetherWith fadeOut(tween(220))
                    },
                    label = "headline"
                ) { ready ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (ready) "A little clearer." else "Let everything wait.",
                            style = if (compact) {
                                MaterialTheme.typography.headlineMedium
                            } else {
                                MaterialTheme.typography.headlineLarge
                            },
                            color = colors.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = when {
                                isPreview && !ready -> "Nothing to do. Just follow your breath."
                                isPreview -> "Take this quiet with you."
                                !ready -> "One breath between impulse and intention."
                                else -> "Continue on purpose, or leave it for later."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            Spacer(Modifier.height(12.dp))

            PrimaryButton(
                text = if (isPreview) "Back to One Pause" else "Not now",
                onClick = onAvoid,
                modifier = Modifier.fillMaxWidth(),
                containerColor = colors.onSurface, contentColor = colors.background
            )

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = onContinue,
                enabled = canContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (canContinue) {
                        colors.outline
                    } else {
                        colors.outlineVariant
                    }
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = colors.surface.copy(alpha = .12f),
                    disabledContainerColor = colors.surface.copy(alpha = .06f),
                    contentColor = colors.onSurface,
                    disabledContentColor = colors.onSurface.copy(alpha = 0.38f)
                )
            ) {
                Text(
                    text = when {
                        isPreview && !canContinue -> "${secondsLeft}s remaining"
                        isPreview -> "Finish practice"
                        !canContinue -> "Continue in ${secondsLeft}s"
                        else -> "Continue anyway"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(if (compact) 10.dp else 16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(
                            if (canContinue) colors.primary else colors.onSurfaceVariant,
                            CircleShape
                        )
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    text = if (canContinue) {
                        "You're in control."
                    } else {
                        "Your ${pauseSeconds}-second pause"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BreathingOrb(
    breath: () -> Float,
    phase: BreathPhase,
    size: Dp
) {
    val colors = MaterialTheme.colorScheme
    val dark = colors.background.luminance() < .3f
    val rim = if (dark) Color(0xFFD7F0EB) else Color(0xFF426F6B)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedContent(
            targetState = phase,
            transitionSpec = { fadeIn(tween(650)) togetherWith fadeOut(tween(450)) },
            label = "breathInstruction"
        ) { current ->
            Text(if (current == BreathPhase.INHALE) "Breathe in" else "Breathe out",
                color = colors.onSurface, style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Normal, letterSpacing = 0.5.sp,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
        }
        Canvas(Modifier.size(size)) {
            val progress = breath()
            val r = this.size.minDimension * (.24f + .14f * progress)
            val c = center
            // Soft transmitted light, followed by the transparent body and layered glass rim.
            drawCircle(Brush.radialGradient(listOf(rim.copy(alpha = .09f), Color.Transparent),
                center = c, radius = r * 1.6f), radius = r * 1.6f)
            drawCircle(Brush.radialGradient(listOf(Color.Transparent, rim.copy(alpha = .025f), rim.copy(alpha = .2f)),
                center = Offset(c.x - r * .18f, c.y - r * .22f), radius = r * 1.3f), radius = r)
            drawCircle(Brush.linearGradient(listOf(rim.copy(alpha = .85f), rim.copy(alpha = .12f), rim.copy(alpha = .5f)),
                start = Offset(c.x-r,c.y-r), end = Offset(c.x+r,c.y+r)), radius = r,
                style = Stroke(width = 1.7.dp.toPx()))
            drawCircle(rim.copy(alpha = .12f), radius = r * .965f, style = Stroke(width = 4.dp.toPx()))
            drawCircle(rim.copy(alpha = .18f), radius = r * .92f, style = Stroke(width = .7.dp.toPx()))
            fun reflection(start: Float, sweep: Float, radius: Float, alpha: Float, width: Float) {
                drawArc(rim.copy(alpha = alpha), start, sweep, false,
                    topLeft = Offset(c.x-radius,c.y-radius), size = Size(radius*2,radius*2),
                    style = Stroke(width = width.dp.toPx(), cap = StrokeCap.Round))
            }
            reflection(218f, 63f, r * .96f, .55f, 3f)
            reflection(282f, 29f, r * .955f, .8f, 2.2f)
            reflection(36f, 75f, r * .955f, .24f, 3.5f)
            reflection(64f, 53f, r * .89f, .2f, 1.4f)
            reflection(157f, 30f, r * .94f, .24f, 2f)
        }
    }
}

@Composable
private fun BrandGlyph(size: Dp) {
    val colors = MaterialTheme.colorScheme
    Canvas(modifier = Modifier.size(size)) {
        val d = this.size.minDimension
        drawCircle(
            color = colors.onSurface.copy(alpha = 0.8f),
            radius = d * 0.43f,
            style = Stroke(width = d * 0.05f)
        )
        drawCircle(color = colors.primary, radius = d * 0.14f)
        drawArc(
            color = colors.primary,
            startAngle = -80f,
            sweepAngle = 65f,
            useCenter = false,
            topLeft = Offset(d * 0.08f, d * 0.08f),
            size = androidx.compose.ui.geometry.Size(d * 0.84f, d * 0.84f),
            style = Stroke(width = d * 0.05f, cap = StrokeCap.Round)
        )
    }
}
