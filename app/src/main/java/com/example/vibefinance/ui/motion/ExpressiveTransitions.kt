package com.example.vibefinance.ui.motion

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.example.vibefinance.theme.VibeMotion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Shared-element scope for M3 Expressive container transforms (list item -> detail page,
 * FAB -> add panel, settings entry -> settings page). Null outside [ExpressiveSharedTransitionRoot],
 * so callers fall back to their plain enter/exit motion.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ExpressiveSharedTransitionRoot(content: @Composable () -> Unit) {
    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) { content() }
    }
}

/** Live progress of a predictive back gesture on a full-screen page. */
@Stable
class PredictiveBackPageState internal constructor() {
    internal val progress = Animatable(0f)
    internal var swipeEdge by mutableIntStateOf(BackEventCompat.EDGE_LEFT)
    val fraction: Float get() = progress.value
}

/**
 * Drives [predictiveBackPage] from the system back gesture. Completing the gesture calls [onBack];
 * cancelling it (or an [onBack] that keeps the page, e.g. a discard prompt) springs the page back.
 */
@Composable
fun rememberPredictiveBackPage(enabled: Boolean = true, onBack: () -> Unit): PredictiveBackPageState {
    val state = remember { PredictiveBackPageState() }
    val scope = rememberCoroutineScope()
    val currentOnBack by rememberUpdatedState(onBack)
    PredictiveBackHandler(enabled = enabled) { events ->
        try {
            events.collect { event ->
                state.swipeEdge = event.swipeEdge
                state.progress.snapTo(event.progress)
            }
            currentOnBack()
        } catch (e: CancellationException) {
            // Gesture cancelled: fall through to the spring-back below.
        } finally {
            scope.launch { state.progress.animateTo(0f, VibeMotion.defaultSpatial()) }
        }
    }
    return state
}

/**
 * M3 predictive back for full-screen surfaces: the page shrinks to 90%, drifts up to 8dp away from
 * the swiped edge and rounds its corners to 28dp, all on the GPU layer (no recomposition).
 */
fun Modifier.predictiveBackPage(state: PredictiveBackPageState): Modifier = graphicsLayer {
    val p = state.fraction
    if (p > 0f) {
        val scale = lerp(1f, 0.9f, p)
        scaleX = scale
        scaleY = scale
        val direction = if (state.swipeEdge == BackEventCompat.EDGE_LEFT) 1f else -1f
        translationX = direction * 8.dp.toPx() * p
        shape = RoundedCornerShape(lerp(0f, 28f, p).dp)
        clip = true
    }
}

/**
 * The [AnimatedVisibilityScope] of the page currently being composed inside a shared-element
 * transition (for example each side of an `AnimatedContent`). Null everywhere else, which turns
 * [sharedTransitionBounds] and [sharedTransitionElement] into no-ops.
 */
val LocalSharedContentScope = staticCompositionLocalOf<AnimatedVisibilityScope?> { null }

/** True for the page that is (or is becoming) the visible one during a shared-element transition. */
val LocalIsSharedTargetPage = staticCompositionLocalOf { true }

/**
 * Shared element transition for an element that looks different on each page (text that becomes
 * an input field, a card whose chrome changes): its bounds move and resize along the slow spatial
 * spring while the two versions cross-fade slowly, so the element stays visible the whole way.
 * Content is only ever scaled down to fit the moving bounds (never blown up), anchored at the start,
 * or re-measured to the bounds with [remeasure] for containers.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedTransitionBounds(key: Any, remeasure: Boolean = false): Modifier {
    val shared = LocalSharedTransitionScope.current ?: return this
    val animated = LocalSharedContentScope.current ?: return this
    return with(shared) {
        this@sharedTransitionBounds.sharedBounds(
            sharedContentState = rememberSharedContentState(key),
            animatedVisibilityScope = animated,
            enter = fadeIn(VibeMotion.slowEffects()),
            exit = fadeOut(VibeMotion.slowEffects()),
            boundsTransform = { _, _ -> VibeMotion.slowSpatial() },
            resizeMode = if (remeasure) SharedTransitionScope.ResizeMode.RemeasureToBounds
                else SharedTransitionScope.ResizeMode.scaleToBounds(
                    androidx.compose.ui.layout.ContentScale.Inside,
                    androidx.compose.ui.Alignment.CenterStart
                )
        )
    }
}

/**
 * Shared element transition for the very same content on both pages (only the target page draws
 * it during the move), e.g. the transaction map handed over through movable content.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedTransitionElement(key: Any): Modifier {
    val shared = LocalSharedTransitionScope.current ?: return this
    val animated = LocalSharedContentScope.current ?: return this
    return with(shared) {
        this@sharedTransitionElement.sharedElement(
            sharedContentState = rememberSharedContentState(key),
            animatedVisibilityScope = animated,
            boundsTransform = { _, _ -> VibeMotion.slowSpatial() }
        )
    }
}
