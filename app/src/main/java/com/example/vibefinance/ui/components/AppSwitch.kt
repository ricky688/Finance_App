package com.example.vibefinance.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp

/**
 * Unified Material 3 Expressive Switch strictly matching official design specs:
 *
 * 1. Track:
 *    - Dimensions: 52dp width x 32dp height pill container (CircleShape).
 *    - Unchecked: surfaceContainerHighest fill, 2dp outline border.
 *    - Checked: primary fill, no outline border.
 *
 * 2. Handle (Thumb) & Stretch Kinetics:
 *    - Resting Size: 24dp diameter circle (centered with 4dp margin from edges).
 *    - Pressed Size: Expands to 28dp circle (centered with 2dp margin from edges).
 *    - Unchecked: outline / dark charcoal circle.
 *    - Checked: onPrimary / white circle.
 *
 * 3. Icons:
 *    - Always present: 16dp Check (✓) when checked, 16dp Close (✕) when unchecked.
 *    - Checked icon color: primary.
 *    - Unchecked icon color: surfaceContainerHighest.
 *
 * 4. Bounded Ripple Highlight & State Layer:
 *    - 40dp circular bounded ripple highlight centered directly over the handle.
 *    - Checked ripple color: onPrimary (with lavender state layer when pressed).
 *    - Unchecked ripple color: onSurfaceVariant (with neutral state layer when pressed).
 *
 * 5. Accessibility:
 *    - Minimum 48dp x 48dp accessible touch target with Role.Switch semantics.
 */
@Composable
fun AppSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    thumbContent: (@Composable () -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Expressive physics specifications
    val dpSpring = spring<androidx.compose.ui.unit.Dp>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
    val colorSpring = spring<Color>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )

    // Animated track and border tokens (M3 Switch specification)
    val trackColor by animateColorAsState(
        targetValue = when {
            !enabled && checked -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            !enabled -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.38f)
            checked -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surfaceContainerHighest
        },
        animationSpec = colorSpring,
        label = "appSwitchTrackColor"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            !enabled && !checked -> MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
            checked -> Color.Transparent
            else -> MaterialTheme.colorScheme.outline
        },
        animationSpec = colorSpring,
        label = "appSwitchBorderColor"
    )

    // Handle (thumb) dimensions: 24dp resting, 28dp pressed
    val thumbSize by animateDpAsState(
        targetValue = if (isPressed) 28.dp else 24.dp,
        animationSpec = dpSpring,
        label = "appSwitchThumbSize"
    )

    // Margin inside 32dp track: 4dp resting, 2dp when pressed
    val edgeMargin = (32.dp - thumbSize) / 2
    val targetOffsetX = if (checked) (52.dp - thumbSize - edgeMargin) else edgeMargin

    val thumbOffsetX by animateDpAsState(
        targetValue = targetOffsetX,
        animationSpec = dpSpring,
        label = "appSwitchThumbOffsetX"
    )

    val thumbColor by animateColorAsState(
        targetValue = when {
            !enabled && checked -> MaterialTheme.colorScheme.surface.copy(alpha = 1.0f)
            !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            checked -> MaterialTheme.colorScheme.onPrimary
            else -> MaterialTheme.colorScheme.outline
        },
        animationSpec = colorSpring,
        label = "appSwitchThumbColor"
    )

    val iconColor by animateColorAsState(
        targetValue = when {
            !enabled && checked -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            !enabled -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.38f)
            checked -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surfaceContainerHighest
        },
        animationSpec = colorSpring,
        label = "appSwitchIconColor"
    )

    // Bounded ripple highlight on onPrimary when checked, onSurfaceVariant when unchecked
    val rippleColor = when {
        checked -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val stateLayerColor = when {
        checked -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    }

    // Accessible touch target (min 48dp x 48dp) with Role.Switch semantics
    Box(
        modifier = modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .semantics {
                role = Role.Switch
                toggleableState = if (checked) ToggleableState.On else ToggleableState.Off
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Suppress outer bleed; bounded ripple is centered on the handle
                enabled = enabled,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onCheckedChange?.invoke(!checked)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        // Track container: exactly 52dp x 32dp with Alignment.CenterStart
        Box(
            modifier = Modifier.size(width = 52.dp, height = 32.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            // 1. Track background & border
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(trackColor, CircleShape)
                    .border(if (checked) 0.dp else 2.dp, borderColor, CircleShape)
            )

            // 2. 40dp Bounded Ripple Highlight & State Layer centered on the handle
            // The 40dp circle is centered horizontally with the handle:
            // handle center X = thumbOffsetX + thumbSize / 2
            // 40dp circle X = thumbOffsetX + (thumbSize - 40.dp) / 2
            // Vertical centering is automatic via Alignment.CenterStart
            val stateLayerOffsetX = thumbOffsetX + (thumbSize - 40.dp) / 2
            Box(
                modifier = Modifier
                    .offset(x = stateLayerOffsetX)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isPressed) stateLayerColor else Color.Transparent,
                        CircleShape
                    )
                    .indication(
                        interactionSource = interactionSource,
                        indication = ripple(bounded = true, color = rippleColor)
                    )
            )

            // 3. Handle (Thumb): 24dp resting -> 28dp pressed, centered vertically via Alignment.CenterStart
            Box(
                modifier = Modifier
                    .offset(x = thumbOffsetX)
                    .size(thumbSize)
                    .clip(CircleShape)
                    .background(thumbColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (thumbContent != null) {
                    thumbContent()
                } else {
                    // 16dp Check (✓) vs Close (✕) icon with spring transition
                    AnimatedContent(
                        targetState = checked,
                        transitionSpec = {
                            (scaleIn(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ) + fadeIn(animationSpec = tween(140)))
                                .togetherWith(
                                    scaleOut(
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    ) + fadeOut(animationSpec = tween(100))
                                )
                        },
                        label = "appSwitchThumbIcon"
                    ) { isChecked ->
                        if (isChecked) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
