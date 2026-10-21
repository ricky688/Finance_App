package com.example.vibefinance.ui.common

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import com.example.vibefinance.ui.preferences.LocalExperience
import com.example.vibefinance.ui.preferences.MotionLevel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import com.example.vibefinance.ui.components.rememberCompletePressProgress
import com.example.vibefinance.util.VibeHaptics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp

/** Keeps the original asymmetric corners while rounding them inwards on press. */
private class ExpressivePressedShape(
    private val restingShape: Shape,
    private val pressProgress: Float
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val outline = restingShape.createOutline(size, layoutDirection, density)
        if (outline !is Outline.Rounded) return outline

        val amount = pressProgress.coerceIn(0f, 1f)
        if (amount == 0f) return outline

        val corners = outline.roundRect
        // In Material 3 Expressive, shape changing is changing the dp of corners, not the edges.
        // Outer rectangle edges remain fixed while corner radii morph smoothly under spring physics.
        // Morphs from 28.dp down to 16.dp (ratio: 16/28 = 0.5714 -> reduction = 0.4286)
        val cornerScale = 1f - 0.4286f * amount
        fun CornerRadius.pressed() = CornerRadius(x * cornerScale, y * cornerScale)
        return Outline.Rounded(
            RoundRect(
                left = corners.left,
                top = corners.top,
                right = corners.right,
                bottom = corners.bottom,
                topLeftCornerRadius = corners.topLeftCornerRadius.pressed(),
                topRightCornerRadius = corners.topRightCornerRadius.pressed(),
                bottomRightCornerRadius = corners.bottomRightCornerRadius.pressed(),
                bottomLeftCornerRadius = corners.bottomLeftCornerRadius.pressed()
            )
        )
    }
}

/**
 * State representing a Material 3 Expressive tactile card press:
 * Smoothly morphs the corner radius (28dp resting -> 16dp pressed) under lively spring kinetics
 * while preserving fixed bounding edges and bounding semantic ripple highlights.
 */
@Immutable
class ExpressiveCardPressState(
    val interactionSource: MutableInteractionSource,
    val cornerRadius: Dp,
    val shape: RoundedCornerShape,
    val pressProgress: Float,
    val isPressed: Boolean
)

@Composable
fun rememberExpressiveCardPress(
    restingCorner: Dp = 28.dp,
    pressedCorner: Dp = 16.dp,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    enabled: Boolean = true
): ExpressiveCardPressState {
    val motion = LocalExperience.current.motion
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressProgress by rememberCompletePressProgress(
        interactionSource = interactionSource,
        enabled = enabled,
        pressSpec = if (motion == MotionLevel.MINIMAL) snap() else tween(160, easing = FastOutSlowInEasing),
        releaseSpec = if (motion == MotionLevel.MINIMAL) snap() else spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    )
    val cornerRadius = lerp(restingCorner, pressedCorner, pressProgress.coerceIn(0f, 1f))
    val shape = remember(cornerRadius) { RoundedCornerShape(cornerRadius) }
    return remember(interactionSource, cornerRadius, shape, pressProgress, isPressed) {
        ExpressiveCardPressState(
            interactionSource = interactionSource,
            cornerRadius = cornerRadius,
            shape = shape,
            pressProgress = pressProgress,
            isPressed = isPressed
        )
    }
}

/**
 * Attaches tactile press interaction with bounded semantic ripple and corner clipping.
 */
fun Modifier.expressiveCardClickable(
    pressState: ExpressiveCardPressState,
    rippleColor: Color = Color.Unspecified,
    enabled: Boolean = true,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit = {}
): Modifier = composed {
    val view = LocalView.current
    val base = this
        .clip(pressState.shape)
        .expressiveHoverLayer(
            interactionSource = pressState.interactionSource,
            enabled = enabled,
            shape = pressState.shape,
            liftDp = 1.5.dp,
            highlightRim = true
        )

    if (onLongClick != null) {
        base.combinedClickable(
            interactionSource = pressState.interactionSource,
            indication = ripple(bounded = true, color = rippleColor),
            enabled = enabled,
            onLongClick = {
                VibeHaptics.heavyLongPress(view)
                onLongClick()
            },
            onClick = {
                VibeHaptics.selection(view)
                onClick()
            }
        )
    } else {
        base.clickable(
            interactionSource = pressState.interactionSource,
            indication = ripple(bounded = true, color = rippleColor),
            enabled = enabled
        ) {
            VibeHaptics.selection(view)
            onClick()
        }
    }
}

/**
 * Tactile press with a shape morph. Shaped controls keep their size while the same layer clips
 * the surface and bounded ripple to the pressed outline.
 */
fun Modifier.bouncyClickable(
    enabled: Boolean = true,
    shape: Shape? = null,
    rippleColor: Color = Color.Unspecified,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val motion = LocalExperience.current.motion
    val isPressed by interactionSource.collectIsPressedAsState()
    val view = LocalView.current

    val pressProgress by rememberCompletePressProgress(
        interactionSource = interactionSource,
        enabled = enabled,
        pressSpec = if (motion == MotionLevel.MINIMAL) snap() else tween(160, easing = FastOutSlowInEasing),
        releaseSpec = if (motion == MotionLevel.MINIMAL) snap() else spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    )

    val baseModifier = this.graphicsLayer {
        val scale = if (shape == null) 1f - 0.05f * pressProgress else 1f
        scaleX = scale
        scaleY = scale
        if (shape != null) {
            this.shape = ExpressivePressedShape(shape, pressProgress)
            clip = true
        }
    }

    if (onLongClick != null) {
        baseModifier.combinedClickable(
            interactionSource = interactionSource,
            indication = ripple(bounded = true, color = rippleColor),
            enabled = enabled,
            onLongClick = {
                VibeHaptics.heavyLongPress(view)
                onLongClick()
            },
            onClick = {
                VibeHaptics.selection(view)
                onClick()
            }
        )
    } else {
        baseModifier.clickable(
            interactionSource = interactionSource,
            indication = ripple(bounded = true, color = rippleColor),
            enabled = enabled
        ) {
            VibeHaptics.selection(view)
            onClick()
        }
    }
}

/**
 * Material buttons already clip their own indication. Pass a shape to morph and clip a custom
 * button surface; scaling is opt-in for controls that deliberately need it.
 */
fun Modifier.pressBounce(
    scaleDown: Float = 1f,
    shape: Shape? = null,
    interactionSource: MutableInteractionSource
): Modifier = composed {
    if (shape == null && scaleDown == 1f) return@composed this
    val motion = LocalExperience.current.motion
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressProgress by rememberCompletePressProgress(
        interactionSource = interactionSource,
        pressSpec = if (motion == MotionLevel.MINIMAL) snap() else tween(160, easing = FastOutSlowInEasing),
        releaseSpec = if (motion == MotionLevel.MINIMAL) snap() else spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    )
    this.graphicsLayer {
        val scale = 1f - (1f - scaleDown) * pressProgress
        scaleX = scale
        scaleY = scale
        if (shape != null) {
            this.shape = ExpressivePressedShape(shape, pressProgress)
            clip = true
        }
    }
}
