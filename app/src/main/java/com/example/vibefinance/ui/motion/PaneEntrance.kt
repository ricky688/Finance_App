package com.example.vibefinance.ui.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.vibefinance.theme.VibeMotion
import com.example.vibefinance.ui.preferences.LocalExperience
import com.example.vibefinance.ui.preferences.MotionLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val ENTER_STEP_MS = 45L
private const val EXIT_STEP_MS = 15L
private const val SECTION_EXIT_MS = 110L
private const val SURFACE_EXIT_MS = 70L

/**
 * Staggered content entrance and exit for an add/edit page shown on the right side of a large
 * screen. Sections ([paneEntrance]) rise 24dp and fade in one after another; closing plays the
 * reverse stagger (last section first), rising upward 24dp while fading, then the page surface fades.
 */
@Stable
class PaneEntranceState internal constructor(
    private val scope: CoroutineScope,
    private val instant: Boolean,
) {
    /** True once the page is closing: sections leave, then [close]'s callback removes the page. */
    var exiting by mutableStateOf(false)
        internal set

    /** Sections that already entered; leaving and re-entering composition does not replay them. */
    internal val played = mutableSetOf<Int>()
    internal var sectionCount = 0

    internal val sectionsExitMs: Long
        get() = (sectionCount - 1).coerceAtLeast(0) * EXIT_STEP_MS + SECTION_EXIT_MS

    val totalExitDurationMs: Long
        get() = sectionsExitMs + SURFACE_EXIT_MS

    /** Plays the staggered exit, then calls [onClosed] (immediately with minimal motion). */
    fun close(onClosed: () -> Unit) {
        if (instant) { onClosed(); return }
        if (exiting) return
        exiting = true
        scope.launch {
            delay(sectionsExitMs + SURFACE_EXIT_MS)
            onClosed()
        }
    }

    /** Reopened while still closing (for example a quick second tap): the sections come back. */
    internal fun cancelExit() { exiting = false }
}

/** Non-null only inside [StaggeredPane]; phones and other pages leave [paneEntrance] a no-op. */
val LocalPaneEntrance = staticCompositionLocalOf<PaneEntranceState?> { null }

/**
 * One section of a staggered pane: enters at `index x 45ms` rising upward from +24dp -> 0dp,
 * and on close leaves quickly in reverse order (last section first, 15ms apart), moving upward
 * 24dp (0dp -> -24dp) with fast effects while fading. Runs on the GPU layer only.
 */
@Composable
fun Modifier.paneEntrance(index: Int): Modifier {
    val state = LocalPaneEntrance.current ?: return this
    val minimal = LocalExperience.current.motion == MotionLevel.MINIMAL
    if (index + 1 > state.sectionCount) state.sectionCount = index + 1
    val alreadySettled = minimal || index in state.played
    val alphaAnim = remember { Animatable(if (alreadySettled) 1f else 0f) }
    val offsetFraction = remember { Animatable(if (alreadySettled) 0f else 1f) }
    val travel = with(LocalDensity.current) { 24.dp.toPx() }
    LaunchedEffect(state.exiting) {
        if (minimal) return@LaunchedEffect
        if (state.exiting) {
            delay((state.sectionCount - 1 - index).coerceAtLeast(0) * EXIT_STEP_MS)
            coroutineScope {
                launch { alphaAnim.animateTo(0f, VibeMotion.fastEffects()) }
                launch { offsetFraction.animateTo(-1f, VibeMotion.fastEffects()) }
            }
        } else if (alphaAnim.value < 1f || offsetFraction.value != 0f) {
            if (index !in state.played) delay(index * ENTER_STEP_MS)
            state.played += index
            coroutineScope {
                launch { alphaAnim.animateTo(1f, VibeMotion.defaultSpatial()) }
                launch { offsetFraction.animateTo(0f, VibeMotion.defaultSpatial()) }
            }
        }
    }
    return graphicsLayer {
        alpha = alphaAnim.value.coerceIn(0f, 1f)
        translationY = travel * offsetFraction.value
    }
}

/**
 * Hosts an add/edit page on the right side of a large screen with the staggered content entrance:
 * the page surface fades in quickly, its sections ([paneEntrance]) follow one after another. Use
 * [PaneEntranceState.close] (from [content]) to play the staggered exit before removing the page.
 */
@Composable
fun StaggeredPane(
    modifier: Modifier = Modifier,
    /** For a page whose owner closes it (e.g. the asset editor): non-null drives the exit directly. */
    exiting: Boolean? = null,
    /** False leaves the page as it is (no surface fade, no stagger state for the sections). */
    enabled: Boolean = true,
    content: @Composable (PaneEntranceState) -> Unit,
) {
    val minimal = LocalExperience.current.motion == MotionLevel.MINIMAL || !enabled
    val scope = rememberCoroutineScope()
    val state = remember { PaneEntranceState(scope, instant = minimal) }
    if (exiting != null && state.exiting != exiting) state.exiting = exiting
    val surface = remember { Animatable(if (minimal) 1f else 0f) }
    LaunchedEffect(state.exiting) {
        if (minimal) return@LaunchedEffect
        if (state.exiting) {
            delay(state.sectionsExitMs)
            surface.animateTo(0f, VibeMotion.fastEffects())
        } else {
            surface.animateTo(1f, VibeMotion.fastEffects())
        }
    }
    if (!enabled) {
        Box(modifier) { content(state) }
        return
    }
    Box(modifier.graphicsLayer { alpha = surface.value }) {
        CompositionLocalProvider(LocalPaneEntrance provides state) { content(state) }
    }
}
