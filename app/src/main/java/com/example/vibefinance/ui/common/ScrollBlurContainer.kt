package com.example.vibefinance.ui.common

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/** Only overflowing edges fade. At either scroll limit the corresponding edge is clear. */
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
    val density = LocalDensity.current
    val rtl = !vertical && LocalLayoutDirection.current == LayoutDirection.Rtl
    val edge = 16.dp
    val edges by remember(scrollState, density) { derivedStateOf {
        with(density) { scrollState.value.toDp().coerceIn(0.dp, edge) to
            (scrollState.maxValue - scrollState.value).toDp().coerceIn(0.dp, edge) }
    } }
    val (before, after) = edges
    val start = if (rtl) after else before
    val end = if (rtl) before else after
    val canStart = if (rtl) scrollState.canScrollForward else scrollState.canScrollBackward
    val canEnd = if (rtl) scrollState.canScrollBackward else scrollState.canScrollForward
    val fade = if (vertical) Modifier.verticalFadingEdge(if (canStart) start else 0.dp, if (canEnd) end else 0.dp)
        else Modifier.horizontalFadingEdge(if (canStart) start else 0.dp, if (canEnd) end else 0.dp)
    Box(modifier.clipToBounds().then(fade)) {
        content(Modifier)
        if (tagPrefix != null) {
            Box(Modifier.matchParentSize()) {
                listOf(true, false).forEach { first ->
                    if (if (first) canStart else canEnd) {
                        val extent = if (first) start else end
                        val position = if (vertical) {
                            if (first) Alignment.TopCenter else Alignment.BottomCenter
                        } else {
                            // Use physical edges after resolving scroll direction below.
                            if (first) Alignment.CenterStart else Alignment.CenterEnd
                        }
                        // Start/End alignment is logical; reverse it for a physical left/right edge in RTL.
                        val alignment = if (rtl) { if (first) Alignment.CenterEnd else Alignment.CenterStart } else position
                        val edgeModifier = Modifier.align(alignment)
                            .then(if (vertical) Modifier.fillMaxWidth().height(extent) else Modifier.fillMaxHeight().width(extent))
                            .testTag("${tagPrefix}_${if (first) "start" else "end"}_fade")
                        Box(edgeModifier)
                    }
                }
            }
        }
    }
}
