package com.example.vibefinance.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.launch
import kotlin.math.abs

/** The same two-direction swipe surface and spring motion used by recurring subscription rows. */
@Composable
fun ExpressiveSwipeRow(
    shape: RoundedCornerShape,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val screenWidthPx = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val detachmentThresholdPx = screenWidthPx * 0.40f
    val dragOffsetX = remember { Animatable(0f) }
    var rawDragX by remember { mutableFloatStateOf(0f) }
    var isPastThreshold by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    val currentOffset = dragOffsetX.value
    val absOffset = abs(currentOffset)
    val isDeleteAction = currentOffset < 0f
    val iconScale by animateFloatAsState(
        targetValue = if (isPastThreshold) 1.25f else (0.8f + 0.2f * (absOffset / detachmentThresholdPx)).coerceIn(0.8f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "SwipeIconScale"
    )
    val backdropScale by animateFloatAsState(
        targetValue = if (isPastThreshold) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "SwipeBackdropScale"
    )
    val targetBgColor = if (isDeleteAction) {
        if (isPastThreshold) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.errorContainer
    } else {
        if (isPastThreshold) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.tertiaryContainer
    }
    val animatedBgColor by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = tween(durationMillis = 200),
        label = "SwipeBgColor"
    )
    val targetIconColor = if (isDeleteAction) {
        if (isPastThreshold) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onErrorContainer
    } else {
        if (isPastThreshold) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onTertiaryContainer
    }
    val animatedIconTint by animateColorAsState(
        targetValue = targetIconColor,
        animationSpec = tween(durationMillis = 200),
        label = "SwipeIconTint"
    )
    val cardElevation by animateDpAsState(
        targetValue = if (absOffset > 2f) 4.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "SwipeCardElevation"
    )

    val draggableState = rememberDraggableState { delta ->
        rawDragX += delta
        val absRaw = abs(rawDragX)
        val effectiveOffset = if (absRaw <= screenWidthPx) {
            rawDragX
        } else {
            val sign = if (rawDragX > 0f) 1f else -1f
            sign * (screenWidthPx + (absRaw - screenWidthPx) * 0.3f)
        }
        coroutineScope.launch { dragOffsetX.snapTo(effectiveOffset) }

        val reachedThreshold = absRaw >= detachmentThresholdPx
        if (reachedThreshold && !isPastThreshold) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            isPastThreshold = true
        } else if (!reachedThreshold && isPastThreshold) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            isPastThreshold = false
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        if (absOffset > 1f) {
            val iconRevealAlpha = (absOffset / with(density) { 40.dp.toPx() }).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(shape)
                    .background(animatedBgColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .align(if (isDeleteAction) Alignment.CenterEnd else Alignment.CenterStart)
                        .padding(if (isDeleteAction) androidx.compose.foundation.layout.PaddingValues(end = 24.dp) else androidx.compose.foundation.layout.PaddingValues(start = 24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .scale(backdropScale)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.22f))
                    )
                    Icon(
                        imageVector = if (isDeleteAction) Icons.Default.Delete else Icons.Default.Edit,
                        contentDescription = if (isDeleteAction) "Delete" else "Edit",
                        tint = animatedIconTint.copy(alpha = iconRevealAlpha),
                        modifier = Modifier.size(24.dp).scale(iconScale)
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationX = currentOffset
                    shadowElevation = cardElevation.toPx()
                    this.shape = shape
                    clip = true
                }
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Horizontal,
                    onDragStarted = { rawDragX = 0f },
                    onDragStopped = { velocity ->
                        val pastThreshold = isPastThreshold ||
                            (abs(velocity) > 1200f && abs(rawDragX) >= detachmentThresholdPx * 0.45f)
                        val delete = rawDragX < 0f
                        coroutineScope.launch {
                            dragOffsetX.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                            rawDragX = 0f
                            isPastThreshold = false
                            if (pastThreshold) {
                                if (delete) onDelete() else onEdit()
                            }
                        }
                    }
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple()
                ) { onEdit() },
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
            content = content
        )
    }
}
