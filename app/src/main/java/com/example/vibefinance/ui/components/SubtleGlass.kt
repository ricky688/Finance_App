package com.example.vibefinance.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

object SubtleGlass {
    val blurRadius = 8.dp
    val topTransition = 24.dp
    val bottomTransition = 32.dp

    fun style(surface: Color) = HazeStyle(
        backgroundColor = surface,
        tint = HazeTint(surface.copy(alpha = 0.70f)),
        blurRadius = blurRadius,
        noiseFactor = 0.02f,
        fallbackTint = HazeTint(surface.copy(alpha = 0.90f))
    )
}

/** Samples only page content. Its own content is empty, so labels and controls stay sharp. */
@Composable
fun SubtleGlassTransition(state: HazeState, top: Boolean, obscured: Boolean, modifier: Modifier = Modifier) {
    val colors = if (top) listOf(Color.Black, Color.Transparent) else listOf(Color.Transparent, Color.Black)
    Box(modifier.fillMaxWidth().height(if (top) SubtleGlass.topTransition else SubtleGlass.bottomTransition)
        .hazeEffect(state, SubtleGlass.style(MaterialTheme.colorScheme.surface)) {
            blurEnabled = !obscured
            mask = Brush.verticalGradient(colors)
            progressive = HazeProgressive.verticalGradient(
                startIntensity = if (top) 1f else 0f,
                endIntensity = if (top) 0f else 1f,
                preferPerformance = true
            )
        })
}
