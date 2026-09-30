package com.example.vibefinance.ui.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.ripple
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

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
        // The inner Surface can retain its resting outline. An inset guarantees that this
        // outer clip remains visible while also containing the ripple within the pressed shape.
        val inset = minOf(2f * density.density, size.minDimension * 0.04f) * amount
        val cornerScale = 1f - 0.30f * amount
        fun CornerRadius.pressed() = CornerRadius(x * cornerScale, y * cornerScale)
        return Outline.Rounded(
            RoundRect(
                left = corners.left + inset,
                top = corners.top + inset,
                right = corners.right - inset,
                bottom = corners.bottom - inset,
                topLeftCornerRadius = corners.topLeftCornerRadius.pressed(),
                topRightCornerRadius = corners.topRightCornerRadius.pressed(),
                bottomRightCornerRadius = corners.bottomRightCornerRadius.pressed(),
                bottomLeftCornerRadius = corners.bottomLeftCornerRadius.pressed()
            )
        )
    }
}

/**
 * Tactile press with a shape morph. Shaped controls keep their size while the same layer clips
 * the surface and bounded ripple to the pressed outline.
 */
fun Modifier.bouncyClickable(
    enabled: Boolean = true,
    shape: Shape? = null,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val pressProgress by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "bouncyPressProgress"
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
            indication = ripple(),
            enabled = enabled,
            onLongClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onLongClick()
            },
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
        )
    } else {
        baseModifier.clickable(
            interactionSource = interactionSource,
            indication = ripple(),
            enabled = enabled
        ) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressProgress by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pressBounceProgress"
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
