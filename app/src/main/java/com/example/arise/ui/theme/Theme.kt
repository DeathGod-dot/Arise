package com.example.arise.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.luminance

val ColorScheme.isLight: Boolean
    get() = background.luminance() > 0.5f

private val AriseDarkColorScheme = darkColorScheme(
    primary = ArisePrimary,
    onPrimary = AriseOnPrimary,
    primaryContainer = ArisePrimaryContainer,
    onPrimaryContainer = AriseOnPrimaryContainer,
    secondary = AriseSecondary,
    onSecondary = AriseOnSecondary,
    secondaryContainer = AriseSecondaryContainer,
    onSecondaryContainer = AriseOnSecondaryContainer,
    tertiary = AriseTertiary,
    onTertiary = AriseOnTertiary,
    tertiaryContainer = AriseTertiaryContainer,
    onTertiaryContainer = AriseOnTertiaryContainer,
    error = AriseError,
    onError = AriseOnError,
    errorContainer = AriseErrorContainer,
    onErrorContainer = AriseOnErrorContainer,
    background = AriseBackground,
    onBackground = AriseOnSurface,
    surface = AriseSurfaceBase,
    onSurface = AriseOnSurface,
    onSurfaceVariant = AriseOnSurfaceVariant,
    surfaceContainer = AriseSurfaceContainer,
    surfaceContainerHigh = AriseSurfaceContainerHigh,
    surfaceContainerLowest = AriseSurfaceContainerLowest,
    outline = AriseOutline,
    outlineVariant = AriseOutlineVariant,
)

private val AriseLightColorScheme = lightColorScheme(
    primary = AriseLightPrimary,
    onPrimary = AriseLightOnPrimary,
    primaryContainer = AriseLightPrimaryContainer,
    onPrimaryContainer = AriseLightPrimary,
    secondary = AriseLightSecondary,
    onSecondary = AriseLightOnPrimary,
    secondaryContainer = AriseLightSecondary,
    onSecondaryContainer = AriseLightOnPrimary,
    tertiary = AriseLightTertiary,
    onTertiary = AriseLightOnPrimary,
    tertiaryContainer = AriseLightTertiary,
    onTertiaryContainer = AriseLightOnPrimary,
    error = AriseError,
    onError = AriseOnError,
    errorContainer = AriseErrorContainer,
    onErrorContainer = AriseOnErrorContainer,
    background = AriseLightBackground,
    onBackground = AriseLightOnSurface,
    surface = AriseLightSurfaceBase,
    onSurface = AriseLightOnSurface,
    onSurfaceVariant = AriseLightOnSurfaceVariant,
    surfaceContainer = AriseLightSurfaceContainer,
    surfaceContainerHigh = AriseLightSurfaceContainerHigh,
    surfaceContainerLowest = AriseLightSurfaceContainerLowest,
    outline = AriseLightOutline,
    outlineVariant = AriseLightOutlineVariant,
)

@Composable
fun AriseTheme(
    useLightTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (useLightTheme) AriseLightColorScheme else AriseDarkColorScheme
    val bgArgb = (if (useLightTheme) AriseLightBackground else AriseBackground).toArgb()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = bgArgb
            window.navigationBarColor = bgArgb
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = useLightTheme
                isAppearanceLightNavigationBars = useLightTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AriseTypography,
        shapes = AriseShapes,
        content = content,
    )
}
