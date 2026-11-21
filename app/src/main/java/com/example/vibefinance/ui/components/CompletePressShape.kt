package com.example.vibefinance.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.example.vibefinance.theme.VibeMotion
import com.example.vibefinance.ui.preferences.LocalExperience
import com.example.vibefinance.ui.preferences.MotionLevel
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

internal const val PressShapeDurationMillis = 180
internal const val ReleaseShapeDurationMillis = 220
private const val PostPressBounceRiseDurationMillis = 80
private const val PostPressBouncePeakScale = 1.20f
private const val PostPressBounceHopDp = -2.5f
private const val PostPressSelectedRestingScale = 1.08f

/** A tap completes its inward morph; a held pointer keeps it there until release.
 * Cancellation (for example scrolling away) restores immediately without committing a click.
 * Only geometry consumes this state: colors, ripples and callbacks use the real interactions.
 */
@Composable
internal fun rememberCompletePressProgress(
    interactionSource: InteractionSource,
    enabled: Boolean = true,
    pressSpec: AnimationSpec<Float> = tween(PressShapeDurationMillis, easing = FastOutSlowInEasing),
    releaseSpec: AnimationSpec<Float> = tween(ReleaseShapeDurationMillis, easing = FastOutSlowInEasing),
): State<Float> {
    val reducedMotion = LocalExperience.current.motion == MotionLevel.MINIMAL
    val effectivePressSpec = if (reducedMotion) snap() else pressSpec
    val effectiveReleaseSpec = if (reducedMotion) snap() else releaseSpec
    val progress = remember(interactionSource) { Animatable(0f) }
    LaunchedEffect(interactionSource, enabled, effectivePressSpec, effectiveReleaseSpec) {
        if (!enabled) progress.snapTo(0f)
        coroutineScope {
            val held = mutableSetOf<PressInteraction.Press>()
            var pressAnimation: Job? = null
            var restoration: Job? = null
            interactionSource.interactions.collect { interaction ->
                if (!enabled) return@collect
                when (interaction) {
                    is PressInteraction.Press -> {
                        held.add(interaction)
                        restoration?.cancel()
                        pressAnimation?.cancel()
                        pressAnimation = launch {
                            progress.animateTo(1f, effectivePressSpec)
                        }
                    }
                    is PressInteraction.Release -> {
                        if (held.remove(interaction.press) && held.isEmpty()) {
                            restoration?.cancel()
                            restoration = launch {
                                pressAnimation?.join()
                                if (held.isEmpty()) {
                                    progress.animateTo(0f, effectiveReleaseSpec)
                                }
                            }
                        }
                    }
                    is PressInteraction.Cancel -> {
                        if (held.remove(interaction.press) && held.isEmpty()) {
                            pressAnimation?.cancel()
                            restoration?.cancel()
                            restoration = launch {
                                progress.animateTo(0f, effectiveReleaseSpec)
                            }
                        }
                    }
                }
            }
        }
    }
    return progress.asState()
}

/** Corner sizes stay in their original dp/percentage units, including in RTL and on resize. */
internal fun interpolateRoundedShape(from: Shape, to: Shape, fraction: Float): RoundedCornerShape {
    require(from is RoundedCornerShape && to is RoundedCornerShape)
    val clamped = fraction.coerceIn(0f, 1f)
    val amount = when {
        clamped >= 0.998f -> 1f
        clamped <= 0.002f -> 0f
        else -> clamped
    }
    fun corner(a: CornerSize, b: CornerSize) = object : CornerSize {
        override fun toPx(shapeSize: Size, density: Density): Float =
            a.toPx(shapeSize, density) * (1f - amount) + b.toPx(shapeSize, density) * amount
    }
    return RoundedCornerShape(
        corner(from.topStart, to.topStart), corner(from.topEnd, to.topEnd),
        corner(from.bottomEnd, to.bottomEnd), corner(from.bottomStart, to.bottomStart)
    )
}

/** The shared clock already animates this outline; Material must not add a second spring. */
internal fun completePressShape(resting: Shape, pressed: Shape, progress: Float): Shape {
    val rounded = interpolateRoundedShape(resting, pressed, progress)
    return object : Shape {
        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline =
            rounded.createOutline(size, layoutDirection, density)
    }
}

/** Provides the button's post-press icon bounce modifier to leading/trailing icons inside it. */
val LocalPostPressIconBounce = staticCompositionLocalOf<Modifier> { Modifier }

/** Applies the enclosing button's post-press M3E icon bounce (1.20x + -2.5dp hop -> spatial settle). */
fun Modifier.postPressIconBounce(): Modifier = composed {
    this.then(LocalPostPressIconBounce.current)
}

/**
 * Matches AssetGroupSelector's post-press internal icon bounce:
 * - Outer button frame stays fixed (no scale shrinking).
 * - On trigger in [MotionLevel.FULL], icon hops up (-2.5dp) and scales to 1.20x in 80ms,
 *   then settles via [VibeMotion.defaultSpatial] to [restingSelectedScale] (1.08x) or 1.0x.
 * - Playback is decoupled from deselection so rapid taps let previous icons finish smoothly.
 */
@Composable
internal fun rememberPostPressIconBounce(
    selected: Boolean = false,
    clickTrigger: Int = 0,
    restingSelectedScale: Float = PostPressSelectedRestingScale,
    interactionSource: InteractionSource? = null,
): Modifier {
    val motion = LocalExperience.current.motion
    val density = LocalDensity.current.density
    val scope = rememberCoroutineScope()
    var activeJob by remember { mutableStateOf<Job?>(null) }
    val targetResting = if (selected) restingSelectedScale else 1f
    val currentTargetResting by rememberUpdatedState(targetResting)
    val iconScale = remember { Animatable(targetResting) }
    val iconOffsetY = remember { Animatable(0f) }
    var wasSelected by remember { mutableStateOf(selected) }
    var lastTrigger by remember { mutableIntStateOf(clickTrigger) }

    fun triggerBounce() {
        if (motion != MotionLevel.FULL) return
        activeJob?.cancel()
        activeJob = scope.launch {
            val riseSpec = tween<Float>(PostPressBounceRiseDurationMillis, easing = FastOutSlowInEasing)
            coroutineScope {
                launch {
                    iconScale.snapTo(1f)
                    iconScale.animateTo(PostPressBouncePeakScale, riseSpec)
                    iconScale.animateTo(currentTargetResting, VibeMotion.defaultSpatial())
                }
                launch {
                    iconOffsetY.snapTo(0f)
                    iconOffsetY.animateTo(PostPressBounceHopDp, riseSpec)
                    iconOffsetY.animateTo(0f, VibeMotion.defaultSpatial())
                }
            }
        }
    }

    LaunchedEffect(motion) {
        if (motion == MotionLevel.MINIMAL) {
            activeJob?.cancel()
            activeJob = null
            iconScale.snapTo(currentTargetResting)
            iconOffsetY.snapTo(0f)
        } else if (motion == MotionLevel.REDUCED) {
            activeJob?.cancel()
            activeJob = scope.launch {
                coroutineScope {
                    launch { iconScale.animateTo(currentTargetResting, tween(120)) }
                    launch { iconOffsetY.snapTo(0f) }
                }
            }
        }
    }

    if (interactionSource != null) {
        LaunchedEffect(interactionSource, motion) {
            interactionSource.interactions.collect { interaction ->
                if (interaction is PressInteraction.Release) {
                    triggerBounce()
                }
            }
        }
    }

    LaunchedEffect(selected, clickTrigger, motion) {
        val entering = selected && !wasSelected
        val triggerChanged = clickTrigger != lastTrigger && clickTrigger > 0
        wasSelected = selected
        lastTrigger = clickTrigger

        if ((entering || triggerChanged) && motion == MotionLevel.FULL) {
            triggerBounce()
        } else if (motion == MotionLevel.MINIMAL) {
            activeJob?.cancel()
            activeJob = null
            iconScale.snapTo(targetResting)
            iconOffsetY.snapTo(0f)
        } else if (motion == MotionLevel.REDUCED) {
            activeJob?.cancel()
            activeJob = scope.launch {
                iconScale.animateTo(targetResting, tween(120))
                iconOffsetY.snapTo(0f)
            }
        } else if (activeJob?.isActive != true) {
            activeJob = scope.launch {
                coroutineScope {
                    launch { iconScale.animateTo(targetResting, spring(stiffness = Spring.StiffnessMediumLow)) }
                    launch { iconOffsetY.snapTo(0f) }
                }
            }
        }
    }

    return remember(iconScale, iconOffsetY, density) {
        Modifier.graphicsLayer {
            scaleX = iconScale.value
            scaleY = iconScale.value
            translationY = iconOffsetY.value * density
        }
    }
}
