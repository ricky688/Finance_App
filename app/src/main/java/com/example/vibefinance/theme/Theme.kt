package com.example.vibefinance.theme

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import android.os.Build
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

/** Actual chosen mode, available before the animated palette has finished changing. */
internal val LocalIsDarkTheme = staticCompositionLocalOf<Boolean?> { null }

private val LightColorScheme = lightColorScheme(
    primary = GoogleLightPrimary,
    onPrimary = GoogleLightOnPrimary,
    primaryContainer = GoogleLightPrimaryContainer,
    onPrimaryContainer = GoogleLightOnPrimaryContainer,
    inversePrimary = GoogleLightInversePrimary,
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
    surfaceTint = GoogleLightSurfaceTint,
    inverseSurface = GoogleLightInverseSurface,
    inverseOnSurface = GoogleLightInverseOnSurface,
    error = GoogleLightError,
    onError = GoogleLightOnError,
    errorContainer = GoogleLightErrorContainer,
    onErrorContainer = GoogleLightOnErrorContainer,
    outline = GoogleLightOutline,
    outlineVariant = GoogleLightOutlineVariant,
    scrim = GoogleLightScrim,
    surfaceContainerLowest = GoogleLightSurfaceContainerLowest,
    surfaceContainerLow = GoogleLightSurfaceContainerLow,
    surfaceContainer = GoogleLightSurfaceContainer,
    surfaceContainerHigh = GoogleLightSurfaceContainerHigh,
    surfaceContainerHighest = GoogleLightSurfaceContainerHighest
)

private val DarkColorScheme = darkColorScheme(
    primary = GoogleDarkPrimary,
    onPrimary = GoogleDarkOnPrimary,
    primaryContainer = GoogleDarkPrimaryContainer,
    onPrimaryContainer = GoogleDarkOnPrimaryContainer,
    inversePrimary = GoogleDarkInversePrimary,
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
    surfaceTint = GoogleDarkSurfaceTint,
    inverseSurface = GoogleDarkInverseSurface,
    inverseOnSurface = GoogleDarkInverseOnSurface,
    error = GoogleDarkError,
    onError = GoogleDarkOnError,
    errorContainer = GoogleDarkErrorContainer,
    onErrorContainer = GoogleDarkOnErrorContainer,
    outline = GoogleDarkOutline,
    outlineVariant = GoogleDarkOutlineVariant,
    scrim = GoogleDarkScrim,
    surfaceContainerLowest = GoogleDarkSurfaceContainerLowest,
    surfaceContainerLow = GoogleDarkSurfaceContainerLow,
    surfaceContainer = GoogleDarkSurfaceContainer,
    surfaceContainerHigh = GoogleDarkSurfaceContainerHigh,
    surfaceContainerHighest = GoogleDarkSurfaceContainerHighest
)

@Composable
fun VibeFinanceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColorEnabled: Boolean = true,
    appearancePalette: AppearancePalette = AppearancePalette.ORIGINAL,
    appearanceContrast: Int = 0,
    pureBlackDarkMode: Boolean = false,
    iconShape: androidx.compose.ui.graphics.Shape = IconShapeMode.COOKIE_4.shape,
    randomIconShapes: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val dynamicColorAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S // Android 12+

    val selectedScheme = when {
        dynamicColorEnabled && dynamicColorAvailable && darkTheme -> dynamicDarkColorScheme(context)
        dynamicColorEnabled && dynamicColorAvailable && !darkTheme -> dynamicLightColorScheme(context)
        appearancePalette == AppearancePalette.ORIGINAL && appearanceContrast == 0 && darkTheme -> DarkColorScheme
        appearancePalette == AppearancePalette.ORIGINAL && appearanceContrast == 0 -> LightColorScheme
        else -> remember(appearancePalette, darkTheme, appearanceContrast) {
            paletteColorScheme(appearancePalette, darkTheme, appearanceContrast)
        }
    }
    val targetScheme = if (darkTheme && pureBlackDarkMode) {
        remember(selectedScheme) { selectedScheme.withPureBlackSurfaces() }
    } else selectedScheme

    // Material 3 Expressive smooth organic fluid transition for theme colors
    val animatedColorScheme = animateColorSchemeAsState(targetScheme)

    // Dynamically align status bar and navigation bar transparent styles to the theme mode
    SideEffect {
        var currentCtx = context
        while (currentCtx is android.content.ContextWrapper) {
            if (currentCtx is ComponentActivity) break
            currentCtx = currentCtx.baseContext
        }
        val activity = currentCtx as? ComponentActivity
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

    CompositionLocalProvider(
        LocalIconShape provides iconShape,
        LocalRandomIconShapes provides randomIconShapes,
        LocalIsDarkTheme provides darkTheme
    ) {
        MaterialTheme(
            colorScheme = animatedColorScheme,
            typography = Typography,
            shapes = ExpressiveShapes,
            content = content
        )
    }
}

/**
 * Animates all semantic tokens of a [ColorScheme] using Material 3 Expressive spring physics
 * (Spring.DampingRatioNoBouncy with Spring.StiffnessLow) for seamless color transitions.
 */
@Composable
fun animateColorSchemeAsState(
    targetColorScheme: ColorScheme,
    animationSpec: AnimationSpec<Color> = spring(
        stiffness = Spring.StiffnessLow,
        dampingRatio = Spring.DampingRatioNoBouncy
    )
): ColorScheme {
    val primary by animateColorAsState(targetColorScheme.primary, animationSpec, label = "theme_primary")
    val onPrimary by animateColorAsState(targetColorScheme.onPrimary, animationSpec, label = "theme_onPrimary")
    val primaryContainer by animateColorAsState(targetColorScheme.primaryContainer, animationSpec, label = "theme_primaryContainer")
    val onPrimaryContainer by animateColorAsState(targetColorScheme.onPrimaryContainer, animationSpec, label = "theme_onPrimaryContainer")
    val inversePrimary by animateColorAsState(targetColorScheme.inversePrimary, animationSpec, label = "theme_inversePrimary")
    val secondary by animateColorAsState(targetColorScheme.secondary, animationSpec, label = "theme_secondary")
    val onSecondary by animateColorAsState(targetColorScheme.onSecondary, animationSpec, label = "theme_onSecondary")
    val secondaryContainer by animateColorAsState(targetColorScheme.secondaryContainer, animationSpec, label = "theme_secondaryContainer")
    val onSecondaryContainer by animateColorAsState(targetColorScheme.onSecondaryContainer, animationSpec, label = "theme_onSecondaryContainer")
    val tertiary by animateColorAsState(targetColorScheme.tertiary, animationSpec, label = "theme_tertiary")
    val onTertiary by animateColorAsState(targetColorScheme.onTertiary, animationSpec, label = "theme_onTertiary")
    val tertiaryContainer by animateColorAsState(targetColorScheme.tertiaryContainer, animationSpec, label = "theme_tertiaryContainer")
    val onTertiaryContainer by animateColorAsState(targetColorScheme.onTertiaryContainer, animationSpec, label = "theme_onTertiaryContainer")
    val background by animateColorAsState(targetColorScheme.background, animationSpec, label = "theme_background")
    val onBackground by animateColorAsState(targetColorScheme.onBackground, animationSpec, label = "theme_onBackground")
    val surface by animateColorAsState(targetColorScheme.surface, animationSpec, label = "theme_surface")
    val onSurface by animateColorAsState(targetColorScheme.onSurface, animationSpec, label = "theme_onSurface")
    val surfaceVariant by animateColorAsState(targetColorScheme.surfaceVariant, animationSpec, label = "theme_surfaceVariant")
    val onSurfaceVariant by animateColorAsState(targetColorScheme.onSurfaceVariant, animationSpec, label = "theme_onSurfaceVariant")
    val surfaceTint by animateColorAsState(targetColorScheme.surfaceTint, animationSpec, label = "theme_surfaceTint")
    val inverseSurface by animateColorAsState(targetColorScheme.inverseSurface, animationSpec, label = "theme_inverseSurface")
    val inverseOnSurface by animateColorAsState(targetColorScheme.inverseOnSurface, animationSpec, label = "theme_inverseOnSurface")
    val error by animateColorAsState(targetColorScheme.error, animationSpec, label = "theme_error")
    val onError by animateColorAsState(targetColorScheme.onError, animationSpec, label = "theme_onError")
    val errorContainer by animateColorAsState(targetColorScheme.errorContainer, animationSpec, label = "theme_errorContainer")
    val onErrorContainer by animateColorAsState(targetColorScheme.onErrorContainer, animationSpec, label = "theme_onErrorContainer")
    val outline by animateColorAsState(targetColorScheme.outline, animationSpec, label = "theme_outline")
    val outlineVariant by animateColorAsState(targetColorScheme.outlineVariant, animationSpec, label = "theme_outlineVariant")
    val scrim by animateColorAsState(targetColorScheme.scrim, animationSpec, label = "theme_scrim")
    val surfaceContainerLowest by animateColorAsState(targetColorScheme.surfaceContainerLowest, animationSpec, label = "theme_surfaceContainerLowest")
    val surfaceContainerLow by animateColorAsState(targetColorScheme.surfaceContainerLow, animationSpec, label = "theme_surfaceContainerLow")
    val surfaceContainer by animateColorAsState(targetColorScheme.surfaceContainer, animationSpec, label = "theme_surfaceContainer")
    val surfaceContainerHigh by animateColorAsState(targetColorScheme.surfaceContainerHigh, animationSpec, label = "theme_surfaceContainerHigh")
    val surfaceContainerHighest by animateColorAsState(targetColorScheme.surfaceContainerHighest, animationSpec, label = "theme_surfaceContainerHighest")

    return remember(
        primary, onPrimary, primaryContainer, onPrimaryContainer, inversePrimary,
        secondary, onSecondary, secondaryContainer, onSecondaryContainer,
        tertiary, onTertiary, tertiaryContainer, onTertiaryContainer,
        background, onBackground, surface, onSurface, surfaceVariant, onSurfaceVariant,
        surfaceTint, inverseSurface, inverseOnSurface, error, onError, errorContainer,
        onErrorContainer, outline, outlineVariant, scrim,
        surfaceContainerLowest, surfaceContainerLow, surfaceContainer,
        surfaceContainerHigh, surfaceContainerHighest
    ) {
        targetColorScheme.copy(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            inversePrimary = inversePrimary,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceTint = surfaceTint,
            inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,
            error = error,
            onError = onError,
            errorContainer = errorContainer,
            onErrorContainer = onErrorContainer,
            outline = outline,
            outlineVariant = outlineVariant,
            scrim = scrim,
            surfaceContainerLowest = surfaceContainerLowest,
            surfaceContainerLow = surfaceContainerLow,
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh,
            surfaceContainerHighest = surfaceContainerHighest
        )
    }
}

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}
