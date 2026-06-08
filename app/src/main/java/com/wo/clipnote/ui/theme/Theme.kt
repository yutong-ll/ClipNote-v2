package com.wo.clipnote.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = Green600,
    onPrimary = Gray0,
    primaryContainer = Green300.copy(alpha = 0.24f),
    onPrimaryContainer = Gray900,
    secondary = Gray600,
    onSecondary = Gray0,
    secondaryContainer = Gray100,
    onSecondaryContainer = Gray800,
    tertiary = Green400,
    onTertiary = Gray900,
    background = Gray50,
    onBackground = Gray900,
    surface = Gray0,
    onSurface = Gray900,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray600,
    surfaceTint = Green500,
    outline = Gray300,
    outlineVariant = Gray200,
    error = androidx.compose.ui.graphics.Color(0xFFB84D4D),
    onError = Gray0,
    errorContainer = androidx.compose.ui.graphics.Color(0xFFF5DDDA),
    onErrorContainer = Gray900
)

private val DarkColorScheme = darkColorScheme(
    primary = Green300,
    onPrimary = Gray900,
    primaryContainer = Green700,
    onPrimaryContainer = Gray50,
    secondary = Gray300,
    onSecondary = Gray900,
    secondaryContainer = Gray700,
    onSecondaryContainer = Gray50,
    tertiary = TagMorandiLavender,
    onTertiary = Gray900,
    background = Gray900,
    onBackground = Gray50,
    surface = Gray800,
    onSurface = Gray50,
    surfaceVariant = Gray700,
    onSurfaceVariant = Gray300,
    surfaceTint = Green300,
    outline = Gray600,
    outlineVariant = Gray700,
    error = androidx.compose.ui.graphics.Color(0xFFE19A93),
    onError = Gray900,
    errorContainer = androidx.compose.ui.graphics.Color(0xFF7D3C38),
    onErrorContainer = Gray50
)

private val ClipNoteShapes = androidx.compose.material3.Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun ClipNoteTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = ClipNoteShapes,
        content = content
    )
}
