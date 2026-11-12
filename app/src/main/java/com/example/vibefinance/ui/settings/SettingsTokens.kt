package com.example.vibefinance.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Dedicated Material 3 Expressive tokens reused across the Settings component family.
 * Centralizes surface role colors, restrained borders, and semantic gaps without
 * imposing premature abstractions outside settings.
 */
internal object SettingsTokens {
    // Shapes categorized by component role
    val ActionCardCorner: Dp = 16.dp
    val ActionCardCornerPressed: Dp = 10.dp

    val OptionCardCornerUnselected: Dp = 18.dp
    val OptionCardCornerSelected: Dp = 24.dp

    val OptionRowCorner: Dp = 14.dp

    // Heights & Touch Targets (Guarantees >= 48dp accessible touch target)
    val MinTouchTarget: Dp = 48.dp

    // Spacing & Gaps (tailored by component role)
    val VerticalIconToLabelGap: Dp = 6.dp // Vertical icon above title in ThemeOptionCard
    val HorizontalIconToTextGap: Dp = 12.dp // Horizontal icon/radio next to title
    val LabelToSublabelGap: Dp = 4.dp // Subtitle above title in LanguageOptionCard
    val TitleToDescriptionGap: Dp = 2.dp // Title to description in row items
    val CardPaddingHorizontal: Dp = 14.dp
    val CardPaddingVertical: Dp = 12.dp

    // Semantic Surface Colors
    val cardSurface: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.surfaceContainer

    val cardSurfaceSelected: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.primaryContainer

    // Restrained Borders
    @Composable
    @ReadOnlyComposable
    fun borderUnselected(alpha: Float = 0.35f): BorderStroke =
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = alpha))

    @Composable
    @ReadOnlyComposable
    fun borderSelected(): BorderStroke =
        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
}
