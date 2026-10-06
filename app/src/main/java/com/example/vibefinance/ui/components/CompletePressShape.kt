package com.example.vibefinance.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

internal const val PressShapeDurationMillis = 180
internal const val ReleaseShapeDurationMillis = 220

/** A tap completes its inward morph; a held pointer keeps it there until release.
 * Cancellation (for example scrolling away) restores immediately without committing a click.
 * Only geometry consumes this state: colors, ripples and callbacks use the real interactions.
 */
@Composable
internal fun rememberCompletePressProgress(
    interactionSource: InteractionSource,
    enabled: Boolean = true,
): State<Float> {
    val progress = remember(interactionSource) { Animatable(0f) }
    LaunchedEffect(interactionSource, enabled) {
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
                            progress.animateTo(1f, tween(PressShapeDurationMillis, easing = FastOutSlowInEasing))
                        }
                    }
                    is PressInteraction.Release -> {
                        if (held.remove(interaction.press) && held.isEmpty()) {
                            restoration?.cancel()
                            restoration = launch {
                                pressAnimation?.join()
                                if (held.isEmpty()) {
                                    progress.animateTo(0f, tween(ReleaseShapeDurationMillis, easing = FastOutSlowInEasing))
                                }
                            }
                        }
                    }
                    is PressInteraction.Cancel -> {
                        if (held.remove(interaction.press) && held.isEmpty()) {
                            pressAnimation?.cancel()
                            restoration?.cancel()
                            restoration = launch {
                                progress.animateTo(0f, tween(ReleaseShapeDurationMillis, easing = FastOutSlowInEasing))
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
    val amount = fraction.coerceIn(0f, 1f)
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
