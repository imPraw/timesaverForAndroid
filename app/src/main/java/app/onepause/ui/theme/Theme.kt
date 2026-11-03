package app.onepause.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.platform.LocalContext
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val OnePauseColorScheme = lightColorScheme(
    primary = Forest,
    onPrimary = PauseCream,
    primaryContainer = ForestMist,
    onPrimaryContainer = Ink,
    secondary = Coral,
    onSecondary = Ink,
    secondaryContainer = CoralSoft,
    onSecondaryContainer = Ink,
    tertiary = Butter,
    onTertiary = Ink,
    tertiaryContainer = ButterSoft,
    onTertiaryContainer = Ink,
    background = Paper,
    onBackground = Ink,
    surface = SurfaceCream,
    onSurface = Ink,
    surfaceVariant = PaperDeep,
    onSurfaceVariant = MutedInk,
    outline = Line,
    outlineVariant = Line,
    error = ErrorRed,
    onError = PauseCream
)

private val OnePauseShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

@Composable
fun OnePauseTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val context = LocalContext.current
    val colors = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> darkColorScheme(
            primary = Sage, onPrimary = Forest, primaryContainer = Forest,
            onPrimaryContainer = ForestMist, secondary = PausePeach,
            tertiary = Butter, background = PauseNight, surface = PauseForest,
            onBackground = PauseCream, onSurface = PauseCream
        )
        else -> OnePauseColorScheme
    }
    MaterialTheme(
        colorScheme = colors,
        typography = OnePauseTypography,
        shapes = OnePauseShapes,
        content = content
    )
}
