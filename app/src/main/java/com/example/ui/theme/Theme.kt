package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryPurple,
    secondary = SecondaryLavender,
    tertiary = TertiaryIndigo,
    background = BackgroundLight,
    surface = SurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1D1B20),
    onSurface = Color(0xFF1D1B20),
    outline = OutlineLight
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryPurpleDark,
    secondary = SecondaryLavenderDark,
    tertiary = TertiaryIndigoDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = Color(0xFF21005D),
    onSecondary = Color(0xFF1C1B1F),
    onTertiary = Color(0xFF141218),
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
    outline = OutlineDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
