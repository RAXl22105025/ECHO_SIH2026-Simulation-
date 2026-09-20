package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val TacticalColorScheme = darkColorScheme(
    primary = CyanTelemetry,
    onPrimary = TacticalDarkBg,
    primaryContainer = TacticalSurfaceElevated,
    onPrimaryContainer = CyanTelemetry,
    secondary = EmeraldTelemetry,
    onSecondary = TacticalDarkBg,
    secondaryContainer = TacticalSurfaceElevated,
    onSecondaryContainer = EmeraldTelemetry,
    tertiary = RoseAlert,
    onTertiary = TextPrimary,
    background = TacticalDarkBg,
    onBackground = TextPrimary,
    surface = TacticalSurface,
    onSurface = TextPrimary,
    surfaceVariant = TacticalSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = TacticalBorder,
    error = RoseAlert,
    onError = TextPrimary
)

@Composable
fun ProjectEchoTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TacticalColorScheme,
        typography = Typography,
        content = content
    )
}
