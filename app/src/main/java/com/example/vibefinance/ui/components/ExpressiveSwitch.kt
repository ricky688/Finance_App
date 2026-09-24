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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
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
 * Material 3 Expressive Switch strictly adhering to the Material 3 Switch specification:
 * - When ON (Selected): Filled primary track, 24dp onPrimary thumb with primary Check icon (✓).
 * - When OFF (Unselected): Solid 2dp outline border, surfaceContainerHighest track fill,
 *   24dp outline thumb with surfaceContainerHighest Close icon (✕).
 * - Kinetic elevation: 28dp horizontal stretch on press with bouncy spring kinetics.
 */
@Composable
fun ExpressiveSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
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
        label = "expressiveSwitchTrackColor"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            !enabled && !checked -> MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
            checked -> Color.Transparent
            else -> MaterialTheme.colorScheme.outline
        },
        animationSpec = colorSpring,
        label = "expressiveSwitchBorderColor"
    )

    // M3 Expressive thumb: 24dp resting size with stretch to 28dp when pressed
    val thumbWidth by animateDpAsState(
        targetValue = if (isPressed) 28.dp else 24.dp,
        animationSpec = dpSpring,
        label = "expressiveSwitchThumbWidth"
    )
    val thumbHeight = 24.dp

    // Offset: 4dp when unselected; (52dp - thumbWidth - 4dp) when selected
    val targetOffset = if (checked) (52.dp - thumbWidth - 4.dp) else 4.dp
    val thumbOffset by animateDpAsState(
        targetValue = targetOffset,
        animationSpec = dpSpring,
        label = "expressiveSwitchThumbOffset"
    )

    val thumbColor by animateColorAsState(
        targetValue = when {
            !enabled && checked -> MaterialTheme.colorScheme.surface.copy(alpha = 1.0f)
            !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            checked -> MaterialTheme.colorScheme.onPrimary
            else -> MaterialTheme.colorScheme.outline
        },
        animationSpec = colorSpring,
        label = "expressiveSwitchThumbColor"
    )

    val iconColor by animateColorAsState(
        targetValue = when {
            !enabled && checked -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            !enabled -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.38f)
            checked -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surfaceContainerHighest
        },
        animationSpec = colorSpring,
        label = "expressiveSwitchIconColor"
    )

    // 48dp minimum accessible touch target with Switch semantics
    Box(
        modifier = modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .semantics {
                role = Role.Switch
                toggleableState = if (checked) ToggleableState.On else ToggleableState.Off
            }
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = false, radius = 24.dp),
                enabled = enabled,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCheckedChange?.invoke(!checked)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        // Track: 52dp x 32dp pill container with solid 2dp outline when unselected
        Box(
            modifier = Modifier
                .width(52.dp)
                .height(32.dp)
                .background(trackColor, CircleShape)
                .border(2.dp, borderColor, CircleShape),
            contentAlignment = Alignment.CenterStart
        ) {
            // Morphing thumb pill with bouncy spring translation and press-stretch kinetics
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset)
                    .size(width = thumbWidth, height = thumbHeight)
                    .background(thumbColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // M3 Expressive active (Check) vs inactive (Cross) icon with spring scale/fade transition
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
                    label = "switchThumbIcon"
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
