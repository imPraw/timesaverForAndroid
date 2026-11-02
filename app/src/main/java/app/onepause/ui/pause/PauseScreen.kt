package app.onepause.ui.pause

import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.onepause.ui.components.AppIcon
import app.onepause.ui.components.PrimaryButton
import app.onepause.ui.theme.Coral
import app.onepause.ui.theme.PauseCream
import app.onepause.ui.theme.PauseForest
import app.onepause.ui.theme.PauseNight
import app.onepause.ui.theme.PausePeach
import app.onepause.ui.theme.PausePeachDeep
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
    var secondsLeft by remember(pauseSeconds) { mutableIntStateOf(pauseSeconds) }
    val breath = remember { Animatable(0f) }
    var phase by remember { mutableStateOf(BreathPhase.INHALE) }

    LaunchedEffect(pauseSeconds) {
        secondsLeft = pauseSeconds
        for (second in pauseSeconds downTo 1) {
            secondsLeft = second
            delay(1000)
        }
        secondsLeft = 0
    }

    LaunchedEffect(Unit) {
        while (true) {
            phase = BreathPhase.INHALE
            breath.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 4000, easing = FastOutSlowInEasing)
            )
            phase = BreathPhase.EXHALE
            breath.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 4000, easing = FastOutSlowInEasing)
            )
        }
    }

    LaunchedEffect(phase) {
        onPhaseChange(phase == BreathPhase.INHALE)
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
            .background(
                Brush.verticalGradient(
                    colors = listOf(PauseNight, PauseForest, Color(0xFF133429))
                )
            )
    ) {
        val compact = maxHeight < 690.dp
        PauseAtmosphere(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BrandGlyph(size = 26.dp)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "ONE PAUSE",
                    style = MaterialTheme.typography.labelSmall,
                    color = PauseCream.copy(alpha = 0.78f),
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.weight(1f))
                if (isPreview) {
                    Text(
                        text = "PRACTICE",
                        style = MaterialTheme.typography.labelSmall,
                        color = PausePeach,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.07f), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
                Spacer(Modifier.width(6.dp))
                IconButton(onClick = onAvoid, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close the pause",
                        tint = PauseCream.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

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
                        text = if (isPreview) "A practice pause" else "YOU REACHED FOR",
                        style = MaterialTheme.typography.labelSmall,
                        color = PauseCream.copy(alpha = 0.5f)
                    )
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = PauseCream.copy(alpha = 0.92f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(if (compact) 8.dp else 14.dp))

            BreathingOrb(
                breath = breath.value,
                phase = phase,
                size = if (compact) 218.dp else 270.dp,
                phaseLabel = when (phase) {
                    BreathPhase.INHALE -> "Breathe in"
                    BreathPhase.EXHALE -> "Breathe out"
                }
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
                        text = if (ready) "The choice is yours." else "Take one breath.",
                        style = if (compact) {
                            MaterialTheme.typography.headlineMedium
                        } else {
                            MaterialTheme.typography.headlineLarge
                        },
                        color = PauseCream,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = when {
                            isPreview && !ready -> "Nothing is blocked. Just feel the pause."
                            isPreview -> "You made a little room. Carry that with you."
                            !ready -> "The app can wait while you decide."
                            else -> "Open it on purpose, or leave it here."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = PauseCream.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }

            if (compact) {
                Spacer(Modifier.weight(1f))
            } else {
                Spacer(Modifier.weight(0.8f))
                QuietNote()
                Spacer(Modifier.weight(1f))
            }

            PrimaryButton(
                text = if (isPreview) "Back to One Pause" else "Not now",
                onClick = onAvoid,
                modifier = Modifier.fillMaxWidth(),
                containerColor = PauseCream,
                contentColor = PauseNight
            )

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = onContinue,
                enabled = canContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = CircleShape,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (canContinue) {
                        PauseCream.copy(alpha = 0.34f)
                    } else {
                        PauseCream.copy(alpha = 0.12f)
                    }
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (canContinue) {
                        PauseCream.copy(alpha = 0.88f)
                    } else {
                        PauseCream.copy(alpha = 0.38f)
                    },
                    disabledContentColor = PauseCream.copy(alpha = 0.38f)
                )
            ) {
                Text(
                    text = when {
                        isPreview && !canContinue -> "Practice continues in ${secondsLeft}s"
                        isPreview -> "Finish practice"
                        !canContinue -> "Open $displayName in ${secondsLeft}s"
                        else -> "Open $displayName anyway"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
                            if (canContinue) Coral else PauseCream.copy(alpha = 0.35f),
                            CircleShape
                        )
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    text = if (canContinue) {
                        "You're in control."
                    } else {
                        "${pauseSeconds}-second pause · set by you"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = PauseCream.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
private fun QuietNote() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(34.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            PauseCream.copy(alpha = 0.28f),
                            Color.Transparent
                        )
                    )
                )
        )
        Spacer(Modifier.height(11.dp))
        Text(
            text = "Nothing is required of you.",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
            color = PauseCream.copy(alpha = 0.42f)
        )
    }
}

@Composable
private fun PauseAtmosphere(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(PausePeach.copy(alpha = 0.11f), Color.Transparent),
                center = Offset(width * 0.78f, height * 0.22f),
                radius = width * 0.7f
            ),
            radius = width * 0.7f,
            center = Offset(width * 0.78f, height * 0.22f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Coral.copy(alpha = 0.07f), Color.Transparent),
                center = Offset(width * 0.1f, height * 0.8f),
                radius = width * 0.75f
            ),
            radius = width * 0.75f,
            center = Offset(width * 0.1f, height * 0.8f)
        )
        val specks = listOf(
            Offset(width * 0.12f, height * 0.17f),
            Offset(width * 0.81f, height * 0.41f),
            Offset(width * 0.19f, height * 0.58f),
            Offset(width * 0.72f, height * 0.69f),
            Offset(width * 0.9f, height * 0.88f)
        )
        specks.forEachIndexed { index, point ->
            drawCircle(
                color = PauseCream.copy(alpha = if (index % 2 == 0) 0.16f else 0.09f),
                radius = if (index % 3 == 0) 2.2f else 1.3f,
                center = point
            )
        }
    }
}

@Composable
private fun BreathingOrb(
    breath: Float,
    phase: BreathPhase,
    phaseLabel: String,
    size: Dp
) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = center
            val maxRadius = this.size.minDimension / 2f
            drawCircle(
                color = PauseCream.copy(alpha = 0.11f),
                radius = maxRadius * 0.93f,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = PauseCream.copy(alpha = 0.07f),
                radius = maxRadius * 0.78f,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = PausePeach.copy(alpha = 0.08f),
                radius = maxRadius * (0.68f + breath * 0.08f)
            )
            val orbRadius = maxRadius * (0.52f + breath * 0.105f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFF8C9AC),
                        PausePeach,
                        PausePeachDeep
                    ),
                    center = Offset(center.x - orbRadius * 0.28f, center.y - orbRadius * 0.3f),
                    radius = orbRadius * 1.45f
                ),
                radius = orbRadius
            )
            drawCircle(
                color = PauseCream.copy(alpha = 0.2f),
                radius = orbRadius,
                style = Stroke(width = 1.3.dp.toPx())
            )
            val arcRadius = orbRadius * 0.82f
            drawArc(
                color = PauseCream.copy(alpha = 0.22f),
                startAngle = 210f,
                sweepAngle = 70f,
                useCenter = false,
                topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                size = androidx.compose.ui.geometry.Size(arcRadius * 2, arcRadius * 2),
                style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
            )
            if (phase == BreathPhase.INHALE) {
                drawCircle(
                    color = PauseCream.copy(alpha = 0.5f),
                    radius = 2.4.dp.toPx(),
                    center = Offset(center.x + orbRadius * 0.73f, center.y + orbRadius * 0.31f)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedContent(
                targetState = phase,
                transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(340)) },
                label = "breathLabel"
            ) {
                Text(
                    text = when (it) {
                        BreathPhase.INHALE -> "Breathe in"
                        BreathPhase.EXHALE -> "Breathe out"
                    },
                    color = PauseNight,
                    fontFamily = BodySans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 19.sp,
                    letterSpacing = 0.1.sp
                )
            }
            Spacer(Modifier.height(5.dp))
            Text(
                text = "through the pause",
                color = PauseNight.copy(alpha = 0.52f),
                fontFamily = BodySans,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
private fun BrandGlyph(size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val d = this.size.minDimension
        drawCircle(
            color = PauseCream.copy(alpha = 0.8f),
            radius = d * 0.43f,
            style = Stroke(width = d * 0.05f)
        )
        drawCircle(color = Coral, radius = d * 0.14f)
        drawArc(
            color = Coral,
            startAngle = -80f,
            sweepAngle = 65f,
            useCenter = false,
            topLeft = Offset(d * 0.08f, d * 0.08f),
            size = androidx.compose.ui.geometry.Size(d * 0.84f, d * 0.84f),
            style = Stroke(width = d * 0.05f, cap = StrokeCap.Round)
        )
    }
}
