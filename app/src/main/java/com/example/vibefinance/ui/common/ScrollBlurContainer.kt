package com.example.vibefinance.ui.common

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.*

/** Only overflowing edges fade/blur. At either scroll limit the corresponding edge is clear. */
@Composable
internal fun ScrollBlurContainer(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    vertical: Boolean = false,
    enabled: Boolean = true,
    tagPrefix: String? = null,
    blurActive: Boolean = true,
    content: @Composable (Modifier) -> Unit
) {
    if (!enabled) { content(modifier); return }
    val intensity = com.example.vibefinance.ui.preferences.LocalExperience.current.blur
    val haze = rememberHazeState()
    val density = LocalDensity.current
    val rtl = !vertical && LocalLayoutDirection.current == LayoutDirection.Rtl
    val edge = 16.dp
    val before = with(density) { scrollState.value.toDp().coerceIn(0.dp, edge) }
    val after = with(density) { (scrollState.maxValue - scrollState.value).toDp().coerceIn(0.dp, edge) }
    val start = if (rtl) after else before
    val end = if (rtl) before else after
    val canStart = if (rtl) scrollState.canScrollForward else scrollState.canScrollBackward
    val canEnd = if (rtl) scrollState.canScrollBackward else scrollState.canScrollForward
    val fade = if (vertical) Modifier.verticalFadingEdge(if (canStart) start else 0.dp, if (canEnd) end else 0.dp)
        else Modifier.horizontalFadingEdge(if (canStart) start else 0.dp, if (canEnd) end else 0.dp)
    Box(modifier.clipToBounds().then(fade)) {
        content(Modifier.hazeSource(haze))
        val surface = MaterialTheme.colorScheme.surfaceContainer
        val style = HazeStyle(backgroundColor = surface, blurRadius = (4 * intensity).dp,
            tint = HazeTint(surface.copy(alpha = 0.12f)), noiseFactor = 0f,
            fallbackTint = HazeTint(surface.copy(alpha = 0.20f)))
        Box(Modifier.matchParentSize()) {
            listOf(true, false).forEach { first ->
                if (if (first) canStart else canEnd) {
                    val extent = if (first) start else end
                    val colors = if (first) listOf(Color.Black, Color.Transparent) else listOf(Color.Transparent, Color.Black)
                    val position = if (vertical) {
                        if (first) Alignment.TopCenter else Alignment.BottomCenter
                    } else {
                        // Use physical edges after resolving scroll direction below.
                        if (first) Alignment.CenterStart else Alignment.CenterEnd
                    }
                    // Start/End alignment is logical; reverse it for a physical left/right edge in RTL.
                    val alignment = if (rtl) { if (first) Alignment.CenterEnd else Alignment.CenterStart } else position
                    var edgeModifier = Modifier.align(alignment).then(
                        if (vertical) Modifier.fillMaxWidth().height(extent) else Modifier.fillMaxHeight().width(extent))
                    if (tagPrefix != null) edgeModifier = edgeModifier.testTag("${tagPrefix}_${if (first) "start" else "end"}_fade")
                    Box(edgeModifier.hazeEffect(haze, style) {
                        blurEnabled = blurActive && intensity > 0f
                        mask = if (vertical) Brush.verticalGradient(colors) else Brush.horizontalGradient(colors)
                        progressive = if (vertical) HazeProgressive.verticalGradient(
                            startIntensity = if (first) 1f else 0f, endIntensity = if (first) 0f else 1f, preferPerformance = true)
                        else HazeProgressive.horizontalGradient(
                            startIntensity = if (first) 1f else 0f, endIntensity = if (first) 0f else 1f, preferPerformance = true)
                    })
                }
            }
        }
    }
}
