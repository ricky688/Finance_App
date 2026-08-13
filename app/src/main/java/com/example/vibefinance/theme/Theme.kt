package com.example.vibefinance.theme

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import android.os.Build
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme

private val LightColorScheme = lightColorScheme(
    primary = GoogleLightPrimary,
    onPrimary = GoogleLightOnPrimary,
    primaryContainer = GoogleLightPrimaryContainer,
    onPrimaryContainer = GoogleLightOnPrimaryContainer,
    secondary = GoogleLightSecondary,
    onSecondary = GoogleLightOnSecondary,
    secondaryContainer = GoogleLightSecondaryContainer,
    onSecondaryContainer = GoogleLightOnSecondaryContainer,
    tertiary = GoogleLightTertiary,
    onTertiary = GoogleLightOnTertiary,
    tertiaryContainer = GoogleLightTertiaryContainer,
    onTertiaryContainer = GoogleLightOnTertiaryContainer,
    background = GoogleLightBackground,
    onBackground = GoogleLightOnBackground,
    surface = GoogleLightSurface,
    onSurface = GoogleLightOnSurface,
    surfaceVariant = GoogleLightSurfaceVariant,
    onSurfaceVariant = GoogleLightOnSurfaceVariant,
    outline = GoogleLightOutline,
    outlineVariant = GoogleLightOutlineVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = GoogleDarkPrimary,
    onPrimary = GoogleDarkOnPrimary,
    primaryContainer = GoogleDarkPrimaryContainer,
    onPrimaryContainer = GoogleDarkOnPrimaryContainer,
    secondary = GoogleDarkSecondary,
    onSecondary = GoogleDarkOnSecondary,
    secondaryContainer = GoogleDarkSecondaryContainer,
    onSecondaryContainer = GoogleDarkOnSecondaryContainer,
    tertiary = GoogleDarkTertiary,
    onTertiary = GoogleDarkOnTertiary,
    tertiaryContainer = GoogleDarkTertiaryContainer,
    onTertiaryContainer = GoogleDarkOnTertiaryContainer,
    background = GoogleDarkBackground,
    onBackground = GoogleDarkOnBackground,
    surface = GoogleDarkSurface,
    onSurface = GoogleDarkOnSurface,
    surfaceVariant = GoogleDarkSurfaceVariant,
    onSurfaceVariant = GoogleDarkOnSurfaceVariant,
    outline = GoogleDarkOutline,
    outlineVariant = GoogleDarkOutlineVariant
)

@Composable
fun VibeFinanceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColorEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val dynamicColorAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S // Android 12+

    val colorScheme = when {
        dynamicColorEnabled && dynamicColorAvailable && darkTheme -> dynamicDarkColorScheme(context)
        dynamicColorEnabled && dynamicColorAvailable && !darkTheme -> dynamicLightColorScheme(context)
        darkTheme -> DarkColorScheme // Custom fallback
        else -> LightColorScheme      // Custom fallback
    }

    // Dynamically align status bar and navigation bar transparent styles to the theme mode
    SideEffect {
        val activity = context as? ComponentActivity
        activity?.enableEdgeToEdge(
            statusBarStyle = if (darkTheme) {
                SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
            } else {
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
            },
            navigationBarStyle = if (darkTheme) {
                SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
            } else {
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
            }
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}
