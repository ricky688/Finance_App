package com.example.vibefinance.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.google.android.material.color.utilities.Hct
import com.google.android.material.color.utilities.MaterialDynamicColors
import com.google.android.material.color.utilities.SchemeTonalSpot

/** Curated seeds, converted to complete Material tonal schemes at the selected contrast level. */
enum class AppearancePalette(val seedArgb: Int) {
    ORIGINAL(0xFF006C4C.toInt()),
    OCEAN(0xFF006590.toInt()),
    SUNSET(0xFFE64A19.toInt()),
    VIOLET(0xFF7047A3.toInt()),
    ROSE(0xFF9E2A5E.toInt()),
    AMBER(0xFFF57C00.toInt()),
    LIME(0xFF689F38.toInt())
}

fun paletteColorScheme(
    palette: AppearancePalette,
    darkTheme: Boolean,
    contrastLevel: Int
): ColorScheme {
    val scheme = SchemeTonalSpot(
        Hct.fromInt(palette.seedArgb),
        darkTheme,
        contrastLevel.coerceIn(-1, 1).toDouble()
    )
    val roles = MaterialDynamicColors()
    fun color(role: com.google.android.material.color.utilities.DynamicColor) = Color(role.getArgb(scheme))

    val base = if (darkTheme) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = color(roles.primary()),
        onPrimary = color(roles.onPrimary()),
        primaryContainer = color(roles.primaryContainer()),
        onPrimaryContainer = color(roles.onPrimaryContainer()),
        inversePrimary = color(roles.inversePrimary()),
        secondary = color(roles.secondary()),
        onSecondary = color(roles.onSecondary()),
        secondaryContainer = color(roles.secondaryContainer()),
        onSecondaryContainer = color(roles.onSecondaryContainer()),
        tertiary = color(roles.tertiary()),
        onTertiary = color(roles.onTertiary()),
        tertiaryContainer = color(roles.tertiaryContainer()),
        onTertiaryContainer = color(roles.onTertiaryContainer()),
        background = color(roles.background()),
        onBackground = color(roles.onBackground()),
        surface = color(roles.surface()),
        onSurface = color(roles.onSurface()),
        surfaceVariant = color(roles.surfaceVariant()),
        onSurfaceVariant = color(roles.onSurfaceVariant()),
        inverseSurface = color(roles.inverseSurface()),
        inverseOnSurface = color(roles.inverseOnSurface()),
        error = color(roles.error()),
        onError = color(roles.onError()),
        errorContainer = color(roles.errorContainer()),
        onErrorContainer = color(roles.onErrorContainer()),
        outline = color(roles.outline()),
        outlineVariant = color(roles.outlineVariant()),
        scrim = color(roles.scrim()),
        surfaceTint = color(roles.primary()),
        surfaceContainerLowest = color(roles.surfaceContainerLowest()),
        surfaceContainerLow = color(roles.surfaceContainerLow()),
        surfaceContainer = color(roles.surfaceContainer()),
        surfaceContainerHigh = color(roles.surfaceContainerHigh()),
        surfaceContainerHighest = color(roles.surfaceContainerHighest())
    )
}

/** Keep content and accent roles intact while making dark surfaces truly black. */
fun ColorScheme.withPureBlackSurfaces(): ColorScheme = copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color.Black,
    surfaceContainer = Color(0xFF0C0C0C),
    surfaceContainerHigh = Color(0xFF161616),
    surfaceContainerHighest = Color(0xFF202020),
    surfaceVariant = Color(0xFF242424)
)
