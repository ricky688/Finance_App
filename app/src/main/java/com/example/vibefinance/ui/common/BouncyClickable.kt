package com.example.vibefinance.ui.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.ripple
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Tactile Spring Micro-Interaction Modifier with bounded Shape Clipping and Material 3 Ripple.
 * Adds a 0.95x spring scale press animation, tactile haptic feedback, and clipped ripple feedback.
 */
fun Modifier.bouncyClickable(
    enabled: Boolean = true,
    shape: Shape? = null,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "bouncyScale"
    )

    val baseModifier = if (shape != null) this.clip(shape) else this

    baseModifier
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            if (shape != null) {
                this.shape = shape
                clip = true
            }
        }
        .clickable(
            interactionSource = interactionSource,
            indication = ripple(),
            enabled = enabled
        ) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        }
}
