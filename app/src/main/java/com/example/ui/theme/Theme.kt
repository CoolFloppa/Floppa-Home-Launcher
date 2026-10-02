package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.model.FloppaThemeType

fun FloppaThemeType.toM3ColorScheme(): ColorScheme {
    return darkColorScheme(
        primary = primaryColor,
        onPrimary = backgroundColor,
        primaryContainer = cardColor,
        onPrimaryContainer = textColor,
        secondary = secondaryColor,
        onSecondary = backgroundColor,
        secondaryContainer = surfaceColor,
        onSecondaryContainer = textColor,
        tertiary = tertiaryColor,
        onTertiary = backgroundColor,
        background = backgroundColor,
        onBackground = textColor,
        surface = surfaceColor,
        onSurface = textColor,
        surfaceVariant = cardColor,
        onSurfaceVariant = textColor.copy(alpha = 0.8f)
    )
}

@Composable
fun FloppaLauncherTheme(
    theme: FloppaThemeType = FloppaThemeType.CLASSIC_GOSHA,
    content: @Composable () -> Unit
) {
    val colorScheme = theme.toM3ColorScheme()
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
