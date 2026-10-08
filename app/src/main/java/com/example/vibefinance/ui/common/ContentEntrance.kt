package com.example.vibefinance.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
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
internal class ContentEntranceState {
    val revealed = mutableStateMapOf<String, Boolean>()
}

/** Measures the unchanged slot; only its painted content fades and moves. */
@Composable
internal fun ContentEntrance(
    id: String,
    state: ContentEntranceState,
    viewport: Rect,
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    tagPrefix: String = "ContentArrival",
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
            // including children of grouped items, before consuming its entrance.
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
    Box(modifier = modifier.fillMaxWidth().then(tracking).testTag("$tagPrefix:$id")) {
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

/** The measured, unobscured part of the page. Nested scroll panes intersect this viewport. */
private data class ContentEntranceScope(
    val state: ContentEntranceState,
    val viewport: Rect,
    val tagPrefix: String,
)

private val LocalContentEntranceScope = staticCompositionLocalOf<ContentEntranceScope?> { null }

@Composable
internal fun ContentEntranceViewport(
    modifier: Modifier = Modifier,
    topVisibilityInset: Dp = 0.dp,
    bottomVisibilityInset: Dp = 0.dp,
    tagPrefix: String? = null,
    content: @Composable () -> Unit,
) {
    val parent = LocalContentEntranceScope.current
    val ownState = remember { ContentEntranceState() }
    var bounds by remember { mutableStateOf(Rect.Zero) }
    val viewport = with(LocalDensity.current) {
        val top = bounds.top + topVisibilityInset.toPx()
        Rect(bounds.left, top, bounds.right, maxOf(top, bounds.bottom - bottomVisibilityInset.toPx()))
    }.let { if (parent == null) it else it.intersect(parent.viewport) }
    Box(modifier.onGloballyPositioned { bounds = it.boundsInWindow() }) {
        CompositionLocalProvider(LocalContentEntranceScope provides ContentEntranceScope(
            parent?.state ?: ownState, viewport, tagPrefix ?: parent?.tagPrefix ?: "ContentArrival"
        )) {
            content()
        }
    }
}

/** Page-scoped IDs keep already seen content settled through filtering, updates and lazy disposal. */
@Composable
internal fun PageContentEntrance(
    id: String,
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    content: @Composable () -> Unit,
) {
    val scope = checkNotNull(LocalContentEntranceScope.current) { "An entrance needs its page viewport" }
    ContentEntrance(id, scope.state, scope.viewport, modifier, delayMillis, scope.tagPrefix) {
        // A lazy item can emit several siblings (for example a card and its bottom spacer).
        // Keep their original vertical layout rather than stacking them in the paint layer.
        Column(Modifier.fillMaxWidth()) { content() }
    }
}
