package app.onepause.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
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
fun OnePauseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = OnePauseColorScheme,
        typography = OnePauseTypography,
        shapes = OnePauseShapes,
        content = content
    )
}
