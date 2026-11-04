package app.onepause.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Color(0xFF25674F), onPrimary = Color.White,
    primaryContainer = Color(0xFFE0EEE6), onPrimaryContainer = Color(0xFF184B38),
    secondary = Color(0xFF617269), onSecondary = Color.White,
    secondaryContainer = Color(0xFFEBF0EC), onSecondaryContainer = Color(0xFF354A3F),
    tertiary = Color(0xFF8C6B35), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF4EBD9), onTertiaryContainer = Color(0xFF604A26),
    background = Color(0xFFF5F5F7), onBackground = Color(0xFF1D1D1F),
    surface = Color.White, onSurface = Color(0xFF1D1D1F),
    surfaceVariant = Color(0xFFEEEEF0), onSurfaceVariant = Color(0xFF66666D),
    surfaceDim = Color(0xFFE4E4E7), surfaceBright = Color.White,
    surfaceContainerLowest = Color.White, surfaceContainerLow = Color.White,
    surfaceContainer = Color(0xFFF0F0F2), surfaceContainerHigh = Color(0xFFEAEAED),
    surfaceContainerHighest = Color(0xFFE2E2E6),
    outline = Color(0xFF8A8A91), outlineVariant = Color(0xFFE3E3E8),
    inverseSurface = Color(0xFF252528), inverseOnSurface = Color(0xFFF5F5F7),
    inversePrimary = Color(0xFFA7D9BD), surfaceTint = Color(0xFF25674F),
    error = Color(0xFFB53B37), onError = Color.White,
    errorContainer = Color(0xFFFCE9E7), onErrorContainer = Color(0xFF832521)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA7D9BD), onPrimary = Color(0xFF123D2B),
    primaryContainer = Color(0xFF253E32), onPrimaryContainer = Color(0xFFC1E7D0),
    secondary = Color(0xFFB7C5BB), onSecondary = Color(0xFF253A2E),
    secondaryContainer = Color(0xFF2B3430), onSecondaryContainer = Color(0xFFD4DFD7),
    tertiary = Color(0xFFE1C38D), onTertiary = Color(0xFF413216),
    tertiaryContainer = Color(0xFF3E3525), onTertiaryContainer = Color(0xFFF0DBB4),
    background = Color(0xFF111113), onBackground = Color(0xFFF5F5F7),
    surface = Color(0xFF1D1D20), onSurface = Color(0xFFF5F5F7),
    surfaceVariant = Color(0xFF303034), onSurfaceVariant = Color(0xFFACACB3),
    surfaceDim = Color(0xFF111113), surfaceBright = Color(0xFF38383D),
    surfaceContainerLowest = Color(0xFF0C0C0E), surfaceContainerLow = Color(0xFF1D1D20),
    surfaceContainer = Color(0xFF242427), surfaceContainerHigh = Color(0xFF2D2D31),
    surfaceContainerHighest = Color(0xFF37373C),
    outline = Color(0xFF7E7E87), outlineVariant = Color(0xFF333338),
    inverseSurface = Color(0xFFE9E9ED), inverseOnSurface = Color(0xFF202023),
    inversePrimary = Color(0xFF25674F), surfaceTint = Color(0xFFA7D9BD),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    errorContainer = Color(0xFF492623), onErrorContainer = Color(0xFFFFDAD5)
)

private val OnePauseShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun isDarkAppearance(mode: String): Boolean = when (mode) {
    "light" -> false
    "dark" -> true
    else -> isSystemInDarkTheme()
}

@Composable
fun OnePauseTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = OnePauseTypography,
        shapes = OnePauseShapes,
        content = content
    )
}
