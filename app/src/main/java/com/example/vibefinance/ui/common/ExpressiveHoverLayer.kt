package com.example.vibefinance.ui.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.vibefinance.theme.LocalIsDarkTheme
import com.example.vibefinance.ui.preferences.LocalExperience
import com.example.vibefinance.ui.preferences.MotionLevel

/**
 * Material 3 Expressive Hover State Layer.
 * Provides a dynamic 8% State Layer overlay, 1.5dp spring lift, and subtle specular rim glow
 * when hovered by a pointer (mouse, trackpad, stylus hover / Samsung DeX), tiered by user motion preferences:
 * - MotionLevel.FULL: 8% tint + 1.5dp spring lift + specular rim glow.
 * - MotionLevel.REDUCED: 8% tint + specular rim glow (zero vertical translation).
 * - MotionLevel.MINIMAL: 8% static snap (zero translation, instant alpha).
 */
fun Modifier.expressiveHoverLayer(
    interactionSource: InteractionSource,
    enabled: Boolean = true,
    shape: Shape = RectangleShape,
    stateLayerColor: Color = Color.Unspecified,
    liftDp: Dp = 1.5.dp,
    highlightRim: Boolean = true,
): Modifier = composed {
    val isHovered by interactionSource.collectIsHoveredAsState()
    val motion = LocalExperience.current.motion
    val density = LocalDensity.current.density
    val isDark = LocalIsDarkTheme.current ?: (MaterialTheme.colorScheme.background.luminance() < 0.5f)

    val active = isHovered && enabled
    val targetAlpha = if (active) 0.08f else 0f
    val targetLift = if (active && motion == MotionLevel.FULL) -liftDp.value else 0f
    val targetRim = if (active && highlightRim) 1f else 0f

    val alpha = when (motion) {
        MotionLevel.MINIMAL -> rememberUpdatedState(targetAlpha)
        MotionLevel.REDUCED -> animateFloatAsState(targetAlpha, tween(120), label = "hoverAlpha")
        MotionLevel.FULL -> animateFloatAsState(targetAlpha, tween(150), label = "hoverAlpha")
    }

    val lift = when (motion) {
        MotionLevel.FULL -> animateFloatAsState(
            targetValue = targetLift,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "hoverLift"
        )
        else -> rememberUpdatedState(0f)
    }

    val rim = when (motion) {
        MotionLevel.MINIMAL -> rememberUpdatedState(targetRim)
        else -> animateFloatAsState(targetRim, tween(150), label = "hoverRim")
    }

    val resolvedTint = if (stateLayerColor != Color.Unspecified) {
        stateLayerColor
    } else {
        if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    }

    val rimColor = if (isDark) Color.White else MaterialTheme.colorScheme.primary

    this
        .graphicsLayer {
            val liftValue = lift.value
            if (liftValue != 0f) {
                translationY = liftValue * density
            }
        }
        .drawWithContent {
            drawContent()
            val a = alpha.value
            val r = rim.value
            if (a > 0f || (highlightRim && r > 0f)) {
                val drawOverlay: () -> Unit = {
                    if (a > 0f) {
                        drawRect(
                            color = resolvedTint.copy(alpha = a),
                            size = size
                        )
                    }
                    if (highlightRim && r > 0f) {
                        drawRect(
                            color = rimColor.copy(alpha = r * 0.12f),
                            topLeft = Offset.Zero,
                            size = Size(size.width, 1.5.dp.toPx())
                        )
                    }
                }

                if (shape == RectangleShape) {
                    drawOverlay()
                } else {
                    when (val outline = shape.createOutline(size, layoutDirection, this)) {
                        is Outline.Rectangle -> {
                            clipRect(
                                left = outline.rect.left,
                                top = outline.rect.top,
                                right = outline.rect.right,
                                bottom = outline.rect.bottom
                            ) {
                                drawOverlay()
                            }
                        }
                        is Outline.Rounded -> {
                            val path = Path().apply { addRoundRect(outline.roundRect) }
                            clipPath(path) {
                                drawOverlay()
                            }
                        }
                        is Outline.Generic -> {
                            clipPath(outline.path) {
                                drawOverlay()
                            }
                        }
                    }
                }
            }
        }
}
