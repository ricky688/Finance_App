package com.example.vibefinance.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
    val contentModifier: Modifier,
    val rippleColor: Color
)

private val DarkConnectedButtonRippleColor = Color(0xFFBDBDBD)

@Composable
private fun isConnectedButtonDarkTheme(): Boolean =
    LocalIsDarkTheme.current ?: (MaterialTheme.colorScheme.background.luminance() < 0.5f)

/** Keep native buttons' bounded ripple neutral without overriding other app components. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ConnectedButtonRipple(content: @Composable () -> Unit) {
    val configuration = if (isConnectedButtonDarkTheme()) {
        RippleConfiguration(color = DarkConnectedButtonRippleColor)
    } else {
        LocalRippleConfiguration.current
    }
    CompositionLocalProvider(LocalRippleConfiguration provides configuration, content = content)
}

/** Light press/selection focus motion; Dark selection crossfade with an independent press ripple. */
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
    val isDarkTheme = isConnectedButtonDarkTheme()
    val useFocusMotion = enabled && !isDarkTheme
    // A held Dark button must retain its current selection colors until the click commits.
    val isFocused = isSelected || (useFocusMotion && isPressed)
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
        ) else Modifier,
        rippleColor = if (isDarkTheme) DarkConnectedButtonRippleColor else Color.Unspecified
    )
}
