package com.example.vibefinance.ui.components

import android.view.MotionEvent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.*
import com.example.vibefinance.ui.preferences.PrivacyText as Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.sqrt

data class SwipeActionsConfig(
    val threshold: Float,
    val icon: Painter?,
    val iconTint: Color,
    val background: Color,
    val backgroundActive: Color,
    val stayDismissed: Boolean,
    val onDismiss: () -> Unit,
)

val DefaultSwipeActionsConfig = SwipeActionsConfig(
    threshold = 0.4f,
    icon = null,
    iconTint = Color.Transparent,
    background = Color.Transparent,
    backgroundActive = Color.Transparent,
    stayDismissed = false,
    onDismiss = {},
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SwipeActions(
    modifier: Modifier = Modifier,
    startActionsConfig: SwipeActionsConfig = DefaultSwipeActionsConfig,
    endActionsConfig: SwipeActionsConfig = DefaultSwipeActionsConfig,
    onTried: () -> Unit = {},
    showTutorial: Boolean = false,
    content: @Composable RowScope.(SwipeToDismissBoxState) -> Unit,
) = BoxWithConstraints(modifier) {
    val width = constraints.maxWidth.toFloat()

    var willDismissDirection: SwipeToDismissBoxValue? by remember {
        mutableStateOf(null)
    }

    val haptic = LocalHapticFeedback.current
    var hasDetachedHapticFired by remember { mutableStateOf(false) }

    val recoverySpringSpec = remember {
        spring<Float>(
            dampingRatio = 0.6f,
            stiffness = 400f
        )
    }

    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            onTried()
            if (willDismissDirection == SwipeToDismissBoxValue.StartToEnd
                && it == SwipeToDismissBoxValue.StartToEnd
            ) {
                startActionsConfig.onDismiss()
                startActionsConfig.stayDismissed
            } else if (willDismissDirection == SwipeToDismissBoxValue.EndToStart &&
                it == SwipeToDismissBoxValue.EndToStart
            ) {
                endActionsConfig.onDismiss()
                endActionsConfig.stayDismissed
            } else {
                false
            }
        }
    )

    LaunchedEffect(key1 = Unit, block = {
        snapshotFlow {
            runCatching { state.requireOffset() }.getOrDefault(0f)
        }
        .collect { offsetVal ->
            val absOffset = abs(offsetVal)
            val currentThreshold = if (offsetVal > 0) startActionsConfig.threshold else endActionsConfig.threshold
            val fullThresholdPx = width * currentThreshold
            val detachmentPx = fullThresholdPx * 0.10f

            // 4. Haptic & Detachment Threshold at 10%
            if (absOffset >= detachmentPx && !hasDetachedHapticFired) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                hasDetachedHapticFired = true
            } else if (absOffset < detachmentPx && hasDetachedHapticFired) {
                hasDetachedHapticFired = false
            }

            willDismissDirection = when {
                offsetVal > fullThresholdPx -> SwipeToDismissBoxValue.StartToEnd
                offsetVal < -fullThresholdPx -> SwipeToDismissBoxValue.EndToStart
                else -> null
            }
        }
    })

    val enableStart = startActionsConfig != DefaultSwipeActionsConfig
    val enableEnd = endActionsConfig != DefaultSwipeActionsConfig

    SwipeToDismissBox(
        state = state,
        modifier = Modifier,
        enableDismissFromStartToEnd = enableStart,
        enableDismissFromEndToStart = enableEnd,

        backgroundContent = {
            val offsetVal = runCatching { state.requireOffset() }.getOrDefault(0f)
            val direction = state.dismissDirection

            val isStart = direction == SwipeToDismissBoxValue.StartToEnd
            val isEnd = direction == SwipeToDismissBoxValue.EndToStart
            val config = if (isStart) startActionsConfig else if (isEnd) endActionsConfig else null

            val willDismiss = willDismissDirection == direction && direction != SwipeToDismissBoxValue.Settled

            if (config != null && config.icon != null) {
                val absOffset = abs(offsetVal)
                val progress = (absOffset / (width * config.threshold)).coerceIn(0f, 1.5f)

                // Android 16 Spring Recovery animation spec (dampingRatio = 0.6f, stiffness = 400f)
                val iconScale by animateFloatAsState(
                    targetValue = if (willDismiss) 1.25f else (0.85f + progress * 0.15f).coerceAtMost(1f),
                    animationSpec = spring(
                        dampingRatio = 0.6f,
                        stiffness = 400f
                    ),
                    label = "Android16IconScale"
                )


                // Playful Pill background color morphing (Gmail M3 style)
                val pillColor by animateColorAsState(
                    targetValue = if (willDismiss) config.backgroundActive else config.background,
                    animationSpec = tween(250),
                    label = "GmailPillColor"
                )

                val iconTintColor by animateColorAsState(
                    targetValue = if (willDismiss) config.iconTint else config.backgroundActive,
                    animationSpec = tween(200),
                    label = "GmailIconTintColor"
                )

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = if (isStart) Alignment.CenterStart else Alignment.CenterEnd
                ) {
                    // Playful Pill-Shaped Container (Gmail / M3 Expressive)
                    val pillWidthDp = with(LocalDensity.current) { absOffset.toDp() }

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(pillWidthDp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(pillColor)
                            .padding(horizontal = 16.dp),
                        contentAlignment = if (isStart) Alignment.CenterStart else Alignment.CenterEnd
                    ) {
                        // Playful Action Icon Capsule
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .scale(iconScale)
                                .clip(CircleShape)
                                .background(
                                    if (willDismiss) config.backgroundActive.copy(alpha = 0.25f) else Color.Transparent
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = config.icon,
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(iconTintColor),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        },
        content = {
            content(state)
        }
    )
}

