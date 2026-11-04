package app.onepause.ui.components

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Language
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import app.onepause.ui.theme.BodySans
import app.onepause.util.LightAppInfo
import app.onepause.util.loadLightAppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun BrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    light: Boolean = false,
    accent: Color = MaterialTheme.colorScheme.tertiary
) {
    val colors = MaterialTheme.colorScheme
    Canvas(modifier = modifier.size(size)) {
        val diameter = this.size.minDimension
        val ringColor = if (light) colors.inverseOnSurface else colors.onSurface
        drawCircle(
            color = ringColor.copy(alpha = 0.86f),
            radius = diameter * 0.44f,
            style = Stroke(width = diameter * 0.045f)
        )
        drawCircle(
            color = ringColor.copy(alpha = 0.32f),
            radius = diameter * 0.31f,
            style = Stroke(width = diameter * 0.035f)
        )
        drawCircle(
            color = accent,
            radius = diameter * 0.13f,
            center = center
        )
        drawArc(
            color = accent,
            startAngle = -76f,
            sweepAngle = 82f,
            useCenter = false,
            topLeft = Offset(diameter * 0.08f, diameter * 0.08f),
            size = androidx.compose.ui.geometry.Size(diameter * 0.84f, diameter * 0.84f),
            style = Stroke(width = diameter * 0.045f, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun AppIcon(
    packageName: String,
    icon: Drawable?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val colors = MaterialTheme.colorScheme
    if (packageName.startsWith("website:")) {
        Box(modifier = modifier.size(size).clip(RoundedCornerShape(size * 0.27f))
            .background(colors.primaryContainer), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Language, contentDescription = null, tint = colors.onPrimaryContainer,
                modifier = Modifier.size(size * 0.52f))
        }
        return
    }
    val imageBitmap = remember(icon, packageName) {
        icon?.let { drawable ->
            runCatching {
                drawable.toBitmap(width = 144, height = 144, config = android.graphics.Bitmap.Config.ARGB_8888)
                    .asImageBitmap()
            }.getOrNull()
        }
    }
    val fallbackColor = remember(packageName, colors) {
        val palette = listOf(
            colors.secondaryContainer,
            colors.tertiaryContainer,
            colors.primaryContainer,
            colors.surfaceContainerHigh,
            colors.surfaceContainerHighest
        )
        palette[(packageName.hashCode() and Int.MAX_VALUE) % palette.size]
    }
    val shape = RoundedCornerShape(size * 0.29f)
    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap,
            contentDescription = null,
            modifier = modifier
                .size(size)
                .clip(shape)
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .clip(shape)
                .background(fallbackColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = packageName.substringAfterLast('.').take(1).uppercase(Locale.getDefault()),
                color = colors.onSurface,
                fontFamily = BodySans,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.4f).sp
            )
        }
    }
}

@Composable
fun rememberAppInfo(packageName: String): LightAppInfo {
    val context = LocalContext.current
    var info by remember(packageName) {
        mutableStateOf(LightAppInfo(packageName, packageName.removePrefix("website:"), null))
    }
    LaunchedEffect(packageName) {
        info = withContext(Dispatchers.IO) { loadLightAppInfo(context, packageName) }
    }
    return info
}

@Composable
fun ScreenHeader(
    overline: String,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = overline,
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = title,
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.headlineLarge,
                color = colors.onSurface
            )
            if (subtitle != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        }
    }
}

@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Text(
        text = text,
        modifier = modifier.semantics { heading() },
        style = MaterialTheme.typography.titleSmall,
        color = color
    )
}

@Composable
fun Pill(
    text: String,
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = background,
        contentColor = contentColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor
        )
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    val interactions = remember { MutableInteractionSource() }
    val pressed by interactions.collectIsPressedAsState()
    val scale = androidx.compose.animation.core.animateFloatAsState(
        if (pressed) 0.98f else 1f, animationSpec = tween(120), label = "buttonPress"
    )
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 54.dp).graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        },
        interactionSource = interactions,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.35f),
            disabledContentColor = contentColor.copy(alpha = 0.6f)
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(9.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun TextAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true,
    textAlign: TextAlign? = null
) {
    TextButton(onClick = onClick, enabled = enabled,
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = color)) {
        Text(text, style = MaterialTheme.typography.labelLarge, textAlign = textAlign)
    }
}

@Composable
fun OnePauseSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Switch(checked = checked, onCheckedChange = onCheckedChange, modifier = modifier)
}

@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = color,
        border = BorderStroke(0.5.dp, borderColor.copy(alpha = borderColor.alpha * 0.45f))
    ) {
        content()
    }
}

@Composable
fun ChoiceChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val background by animateColorAsState(
        if (selected) colors.primaryContainer else colors.surfaceContainer,
        animationSpec = tween(180),
        label = "chipBackground"
    )
    val contentColor by animateColorAsState(
        if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant,
        animationSpec = tween(180),
        label = "chipContent"
    )
    val borderColor by animateColorAsState(
        if (selected) colors.primary.copy(alpha = 0.3f) else Color.Transparent,
        animationSpec = tween(180),
        label = "chipBorder"
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(12.dp))
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(horizontal = 16.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor
        )
    }
}

@Composable
fun Dot(active: Boolean, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val size by animateDpAsState(if (active) 8.dp else 6.dp, label = "dotSize")
    val color by animateColorAsState(if (active) colors.inverseSurface else colors.secondary, label = "dotColor")
    Box(
        modifier = modifier
            .size(size)
            .background(color, CircleShape)
    )
}

@Composable
fun EmptyState(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(Modifier.size(54.dp)) {
            drawCircle(
                color = colors.secondary.copy(alpha = 0.4f),
                radius = size.minDimension * 0.46f,
                style = Stroke(width = 1.5.dp.toPx())
            )
            drawCircle(color = colors.tertiary, radius = size.minDimension * 0.12f)
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (action != null) {
            Spacer(Modifier.height(18.dp))
            action()
        }
    }
}

@Composable
fun DecorativeDots(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Canvas(modifier = modifier) {
        drawCircle(color = colors.tertiary.copy(alpha = 0.12f), radius = size.minDimension * 0.45f)
        drawCircle(color = colors.tertiary.copy(alpha = 0.18f), radius = size.minDimension * 0.2f)
        drawCircle(color = colors.secondary.copy(alpha = 0.14f), radius = size.minDimension * 0.3f)
    }
}

fun Drawable.toSquareImageBitmap(size: Int = 160): ImageBitmap? = runCatching {
    toBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888).asImageBitmap()
}.getOrNull()

@Composable
fun AppIconNameRow(
    packageName: String,
    label: String,
    icon: Drawable?,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(packageName = packageName, icon = icon, size = 44.dp)
        Spacer(Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (supporting != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        }
    }
}

@Composable
fun NavSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        shape = RoundedCornerShape(28.dp),
        color = colors.onSurface,
        shadowElevation = 12.dp,
        content = content
    )
}

@Composable
fun SurfaceDivider(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.outlineVariant) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(color.copy(alpha = color.alpha * 0.85f))
    )
}

@Composable
fun OverlineWithDot(text: String, modifier: Modifier = Modifier, dotColor: Color = MaterialTheme.colorScheme.tertiary) {
    val colors = MaterialTheme.colorScheme
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(6.dp).background(dotColor, CircleShape))
        Spacer(Modifier.width(7.dp))
        Text(
            text = text.uppercase(Locale.getDefault()),
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant
        )
    }
}

@Composable
fun BackgroundWash(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Canvas(modifier = modifier) {
        drawCircle(
            brush = Brush.radialGradient(
                listOf(colors.primary.copy(alpha = 0.025f), Color.Transparent),
                center = Offset(size.width * 0.92f, size.height * 0.08f),
                radius = size.width * 0.52f
            ),
            radius = size.width * 0.52f,
            center = Offset(size.width * 0.92f, size.height * 0.08f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                listOf(colors.primary.copy(alpha = 0.015f), Color.Transparent),
                center = Offset(size.width * 0.04f, size.height * 0.36f),
                radius = size.width * 0.6f
            ),
            radius = size.width * 0.6f,
            center = Offset(size.width * 0.04f, size.height * 0.36f)
        )
    }
}

fun Context.isProbablyInstalled(packageName: String): Boolean = runCatching {
    packageManager.getApplicationInfo(packageName, 0)
}.isSuccess

@Composable
fun ExplorePill(text: String, color: Color = MaterialTheme.colorScheme.tertiary) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = CircleShape,
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            color = colors.onSurface,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
