package com.example.vibefinance.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.example.vibefinance.theme.LocalIsDarkTheme

/** Apply [contentModifier] inside the button's animated shape, before content padding. */
@Immutable
internal data class ConnectedButtonColorMotion(
    val containerColor: Color,
    val contentColor: Color,
    val contentModifier: Modifier
)

/** Shared finite, reversible Light motion and uniform Dark crossfade for connected controls. */
@Composable
internal fun rememberConnectedButtonColorMotion(
    isSelected: Boolean,
    isPressed: Boolean,
    inactiveContainerColor: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
    inactiveContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    activeContainerColor: Color = MaterialTheme.colorScheme.primary,
    activeContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    backdropColor: Color = MaterialTheme.colorScheme.surface,
    enabled: Boolean = true
): ConnectedButtonColorMotion {
    val isDarkTheme = LocalIsDarkTheme.current ?: (MaterialTheme.colorScheme.background.luminance() < 0.5f)
    val useFocusMotion = enabled && !isDarkTheme
    val isFocused = isSelected || (enabled && isPressed)
    val progress = if (useFocusMotion) {
        animateFloatAsState(
            targetValue = if (isFocused) 1f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "connectedInsetFocus"
        )
    } else {
        rememberUpdatedState(if (isFocused) 1f else 0f)
    }
    val colorAnimationSpec = spring<Color>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )
    val containerColor by animateColorAsState(
        targetValue = if (isFocused) activeContainerColor else inactiveContainerColor,
        animationSpec = colorAnimationSpec,
        label = "connectedContainer"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isFocused) activeContentColor else inactiveContentColor,
        animationSpec = colorAnimationSpec,
        label = "connectedContent"
    )

    return ConnectedButtonColorMotion(
        containerColor = if (useFocusMotion) Color.Transparent else containerColor,
        contentColor = if (useFocusMotion) inactiveContentColor else contentColor,
        contentModifier = if (useFocusMotion) Modifier.insetFocusColorMotion(
            progress = progress,
            inactiveColor = inactiveContainerColor,
            primaryColor = activeContainerColor,
            inactiveContentColor = inactiveContentColor,
            primaryContentColor = activeContentColor,
            backdropColor = backdropColor
        ) else Modifier
    )
}
