package com.example.vibefinance.ui.common

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.vibefinance.ui.components.SubtleGlassTransition
import com.example.vibefinance.ui.preferences.LocalExperience
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

/** Shared bounded-viewport effects; headers and actions stay outside the sampled content. */
@Composable
internal fun ScrollEdgeEffects(top: Dp, bottom: Dp, modifier: Modifier, tagPrefix: String,
    content: @Composable (Modifier) -> Unit) {
    val haze = rememberHazeState()
    val blur = Build.VERSION.SDK_INT >= 31 && LocalExperience.current.blur > 0f &&
        LocalWindowInfo.current.isWindowFocused && (top > 0.dp || bottom > 0.dp)
    Box(modifier.clipToBounds().verticalFadingEdge(top, bottom).testTag("${tagPrefix}ScrollViewport")) {
        content(if (blur) Modifier.hazeSource(haze) else Modifier)
        if (top > 0.dp) {
            Box(Modifier.align(Alignment.TopCenter).fillMaxWidth().height(top).testTag("${tagPrefix}_start_fade"))
            if (blur) SubtleGlassTransition(haze, top = true, obscured = false,
                modifier = Modifier.align(Alignment.TopCenter).height(top).testTag("${tagPrefix}_start_blur"))
        }
        if (bottom > 0.dp) {
            Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(bottom).testTag("${tagPrefix}_end_fade"))
            if (blur) SubtleGlassTransition(haze, top = false, obscured = false,
                modifier = Modifier.align(Alignment.BottomCenter).height(bottom).testTag("${tagPrefix}_end_blur"))
        }
    }
}
