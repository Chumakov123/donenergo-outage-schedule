// file: presentation/theme/Theme.kt
package com.chumakov123.outageschedule.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    secondary = SecondaryBlue,

    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,

    onPrimary = TextPrimary,
    onSecondary = TextPrimary,

    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,

    error = ErrorRed
)

@Composable
fun OutageScheduleTheme(
    content: @Composable () -> Unit
) {
    SystemBarsController()

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}