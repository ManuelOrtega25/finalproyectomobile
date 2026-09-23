package com.example.proyectofinal.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AppColorScheme = darkColorScheme(
    primary = AccentYellow,
    onPrimary = DarkBackground,
    primaryContainer = AccentYellow,
    onPrimaryContainer = DarkBackground,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = CardBackground,
    onSurface = TextPrimary,
    surfaceVariant = SecondarySurface,
    onSurfaceVariant = TextSecondary,
    outline = BorderColor
)

@Composable
fun ProyectoFinalTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun Act4Theme(
    content: @Composable () -> Unit
) {
    ProyectoFinalTheme(content = content)
}
