@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.settings

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.DrawerValue
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal object SettingsDrawerTokens {
    val MaximumWidth = 360.dp
    val MinimumWidth = 280.dp
    val ScrimTouchWidth = 56.dp
    // Physical corners: the host is RTL for right-edge navigation, but its left edge is free.
    val ContainerShape = AbsoluteRoundedCornerShape(
        topLeft = 28.dp, bottomLeft = 28.dp, topRight = 0.dp, bottomRight = 0.dp
    )
    const val MotionDurationMillis = 320
    val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
}

internal fun settingsDrawerWidth(maxWidth: Dp): Dp {
    val availableWidth = (maxWidth - SettingsDrawerTokens.ScrimTouchWidth).coerceAtLeast(0.dp)
    // Prefer 280–360dp; below 336dp, preserving the scrim takes priority over minimum width.
    return minOf(SettingsDrawerTokens.MaximumWidth, availableWidth.coerceAtLeast(SettingsDrawerTokens.MinimumWidth))
        .coerceAtMost(availableWidth)
}

private class DrawerMotionScheme(parent: MotionScheme) : MotionScheme by parent {
    override fun <T> defaultSpatialSpec(): FiniteAnimationSpec<T> =
        tween(SettingsDrawerTokens.MotionDurationMillis, easing = SettingsDrawerTokens.EmphasizedDecelerate)
    override fun <T> fastEffectsSpec(): FiniteAnimationSpec<T> =
        tween(SettingsDrawerTokens.MotionDurationMillis, easing = SettingsDrawerTokens.EmphasizedDecelerate)
}

/** One native opening/closing session; notify the owner only after closing has settled. */
@Composable
internal fun rememberSettingsDrawerClose(drawerState: DrawerState, onDismiss: () -> Unit): () -> Unit {
    val onDismissLatest by rememberUpdatedState(onDismiss)
    val scope = rememberCoroutineScope()
    LaunchedEffect(drawerState) {
        launch { drawerState.open() }
        snapshotFlow { drawerState.targetValue }.first { it == DrawerValue.Open }
        snapshotFlow { drawerState.isClosed && !drawerState.isAnimationRunning && drawerState.targetValue == DrawerValue.Closed }
            .first { it }
        onDismissLatest()
    }
    return { scope.launch { drawerState.close() } }
}

/** Keep the actual page inside the native drawer host, including its scrim and semantics. */
@Composable
internal fun SettingsDrawerHost(
    drawerState: DrawerState,
    drawerContent: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val parentMotion = MaterialTheme.motionScheme
    val contentDirection = LocalLayoutDirection.current
    val drawerMotion = remember(parentMotion) { DrawerMotionScheme(parentMotion) }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val drawerWidth = settingsDrawerWidth(maxWidth)
        MaterialTheme(motionScheme = drawerMotion) {
            // Native drawers open at the layout's start edge. Scope RTL to the host so
            // Settings enters from the physical right, matching the toolbar Settings icon.
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    gesturesEnabled = drawerState.isOpen || drawerState.targetValue == DrawerValue.Open,
                    drawerContent = {
                        ModalDrawerSheet(
                            drawerState = drawerState,
                            // Draw a continuous toolbar surface behind the status bar.
                            // Keep native drawer insets so controls clear system icons/cutouts.
                            drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            drawerShape = SettingsDrawerTokens.ContainerShape,
                            modifier = Modifier.width(drawerWidth).fillMaxHeight().testTag("SettingsDrawer")
                        ) {
                            CompositionLocalProvider(LocalLayoutDirection provides contentDirection) {
                                MaterialTheme(motionScheme = parentMotion) { drawerContent() }
                            }
                        }
                    }
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides contentDirection) {
                        MaterialTheme(motionScheme = parentMotion) { content() }
                    }
                }
            }
        }
    }
}
