package app.onepause.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.onepause.ui.components.SoftCard
import kotlin.math.cos
import kotlin.math.sin

/** A gentle daily milestone, derived from actual pauses; no extra tracking. */
@Composable
internal fun DailyBloom(pauses: Int) {
    val colors = MaterialTheme.colorScheme
    val petals = pauses.coerceIn(0, 5)
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { revealed = true }
    val growth by animateFloatAsState(
        targetValue = if (revealed) petals / 5f else 0f,
        animationSpec = spring(dampingRatio = 0.7f), label = "bloomGrowth"
    )
    SoftCard(color = colors.tertiaryContainer, borderColor = colors.tertiaryContainer) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Canvas(Modifier.size(76.dp).semantics {
                contentDescription = "$petals of 5 daily bloom petals"
            }) {
                rotate(growth * 30f) {
                    repeat(5) { index ->
                        val angle = Math.toRadians(index * 72.0 - 90)
                        drawCircle(
                            color = if (index < petals) colors.primary else colors.onTertiaryContainer.copy(alpha = 0.12f),
                            radius = size.minDimension * (0.19f + growth * 0.025f),
                            center = center + Offset(cos(angle).toFloat(), sin(angle).toFloat()) * size.minDimension * 0.27f
                        )
                    }
                    drawCircle(colors.tertiaryContainer, size.minDimension * 0.16f)
                    drawCircle(colors.onTertiaryContainer, size.minDimension * 0.055f)
                }
            }
            Column(Modifier.weight(1f)) {
                Text(if (petals == 5) "Look at you bloom." else "Grow a little space.",
                    style = MaterialTheme.typography.titleLarge, color = colors.onTertiaryContainer)
                Spacer(Modifier.height(6.dp))
                Text(when (petals) {
                    0 -> "One pause, one petal. Your first is a breath away."
                    5 -> "Five mindful moments today. The rest is yours."
                    else -> "$petals of 5 petals today. Every pause counts."
                }, style = MaterialTheme.typography.bodyMedium, color = colors.onTertiaryContainer)
            }
        }
    }
}
