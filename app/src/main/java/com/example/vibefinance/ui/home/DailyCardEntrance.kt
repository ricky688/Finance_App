package com.example.vibefinance.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

/** Kept by the page so lazy disposal and financial-data updates cannot replay an arrival. */
internal class DailyCardEntranceState {
    val revealed = mutableStateMapOf<String, Boolean>()
}

/** Measures the unchanged slot; only its painted content fades and moves. */
@Composable
internal fun DailyCardEntrance(
    id: String,
    state: DailyCardEntranceState,
    viewport: Rect,
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    content: @Composable () -> Unit,
) {
    val wasRevealed = state.revealed[id] == true
    val progress = remember(id, state) { Animatable(if (wasRevealed) 1f else 0f) }
    var bounds by remember(id) { mutableStateOf(Rect.Zero) }
    val currentViewport by rememberUpdatedState(viewport)
    val minimumVisibleHeight = with(LocalDensity.current) { 32.dp.toPx() }
    val travel = with(LocalDensity.current) { 20.dp.toPx() }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(id, state, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            if (state.revealed[id] == true) {
                progress.snapTo(1f)
                return@repeatOnLifecycle
            }
            // Prefetch composes content before it is visible. Wait for actual window overlap,
            // including each child of the two-row bento item, before consuming its entrance.
            snapshotFlow {
                val overlap = bounds.intersect(currentViewport)
                bounds.width > 0f && bounds.height > 0f &&
                    currentViewport.width > 0f && currentViewport.height > 0f &&
                    overlap.width > 0f &&
                    overlap.height >= minOf(bounds.height * 0.15f, minimumVisibleHeight)
            }.first { it }
            state.revealed[id] = true
            val durationScale = coroutineContext[MotionDurationScale]?.scaleFactor ?: 1f
            if (durationScale <= 0f) {
                progress.snapTo(1f)
            } else {
                delay((delayMillis.coerceIn(0, 110) * durationScale).toLong())
                progress.animateTo(1f, tween(durationMillis = 320, easing = LinearOutSlowInEasing))
            }
        }
    }

    val tracking = if (wasRevealed) Modifier else Modifier.onGloballyPositioned {
        bounds = it.boundsInWindow(clipBounds = false)
    }
    Box(modifier = modifier.fillMaxWidth().then(tracking).testTag("DailyArrival:$id")) {
        Box(modifier = Modifier.fillMaxWidth().graphicsLayer {
            alpha = progress.value
            translationY = travel * (1f - progress.value)
            scaleX = 0.985f + 0.015f * progress.value
            scaleY = scaleX
        }) {
            content()
        }
    }
}
