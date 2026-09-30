package com.example.vibefinance.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.interaction.PressInteraction
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.min
import java.lang.Integer.MAX_VALUE
import kotlin.math.min

enum class KeyboardButtonType { DEFAULT, PRIMARY, SECONDARY, TERTIARY, DELETE }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeyboardButton(
    modifier: Modifier = Modifier,
    type: KeyboardButtonType,
    text: String? = null,
    icon: Painter? = null,
    onClick: (() -> Unit) = {},
    onLongClick: (() -> Unit) = {},
) {
    val localDensity = LocalDensity.current
    val view = LocalView.current
    var minSize by remember { mutableStateOf(MAX_VALUE.dp) }
    var minSizeFloat by remember { mutableStateOf(MAX_VALUE.toFloat()) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()
    
    var isPulsing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var releaseJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    releaseJob?.cancel()
                    isPulsing = true
                }
                is PressInteraction.Release, is PressInteraction.Cancel -> {
                    releaseJob?.cancel()
                    releaseJob = coroutineScope.launch {
                        delay(140)
                        isPulsing = false
                    }
                }
            }
        }
    }

    val isShapeActive = isPressed.value || isPulsing
    val initialRadius = if (minSize == MAX_VALUE.dp) 28.dp else minSize / 2
    val pressedRadius = 10.dp
    val radius by animateDpAsState(
        targetValue = if (isShapeActive) pressedRadius else initialRadius,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = if (isShapeActive) Spring.StiffnessMedium else Spring.StiffnessMediumLow
        ),
        label = "buttonRadius"
    )

    val color = when (type) {
        KeyboardButtonType.DEFAULT -> MaterialTheme.colorScheme.surfaceVariant
        KeyboardButtonType.PRIMARY -> MaterialTheme.colorScheme.primary
        KeyboardButtonType.SECONDARY -> MaterialTheme.colorScheme.secondaryContainer
        KeyboardButtonType.TERTIARY -> MaterialTheme.colorScheme.tertiaryContainer
        KeyboardButtonType.DELETE -> MaterialTheme.colorScheme.errorContainer
    }

    val contentColor = when (type) {
        KeyboardButtonType.DEFAULT -> MaterialTheme.colorScheme.onSurface
        KeyboardButtonType.PRIMARY -> MaterialTheme.colorScheme.onPrimary
        KeyboardButtonType.SECONDARY -> MaterialTheme.colorScheme.onSecondaryContainer
        KeyboardButtonType.TERTIARY -> MaterialTheme.colorScheme.onTertiaryContainer
        KeyboardButtonType.DELETE -> MaterialTheme.colorScheme.onErrorContainer
    }

    Surface(
        tonalElevation = 6.dp,
        shape = RoundedCornerShape(radius),
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned {
                minSize = with(localDensity) { min(it.size.height, it.size.width).toDp() }
                minSizeFloat = min(it.size.height, it.size.width).toFloat()
            }
            .clip(RoundedCornerShape(radius))
    ) {
        Box(
            modifier = Modifier
                .background(color = color)
                .fillMaxSize()
                .clip(RoundedCornerShape(radius))
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = ripple(),
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        releaseJob?.cancel()
                        isPulsing = true
                        releaseJob = coroutineScope.launch {
                            delay(140)
                            isPulsing = false
                        }
                        onClick.invoke()
                    },
                    onLongClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        onLongClick.invoke()
                    },
                ),
            contentAlignment = Alignment.Center
        ) {
            if (text !== null) {
                val fontSize = with(localDensity) {
                    val computed = (minSizeFloat * 0.35f).toSp()
                    if (computed.value == 0f) 24.sp else if (computed.value < 36f) computed else 36.sp
                }

                Text(
                    text = text,
                    color = contentColor,
                    style = MaterialTheme.typography.displaySmall,
                    fontSize = fontSize,
                )
            }
            if (icon !== null) {
                Icon(
                    painter = icon,
                    tint = contentColor,
                    modifier = Modifier.size(min(minSize * 0.34f, 154.dp)),
                    contentDescription = null,
                )
            }
        }
    }
}
