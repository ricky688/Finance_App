package com.example.vibefinance.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import com.example.vibefinance.ui.preferences.PrivacyText as Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import android.view.SoundEffectConstants
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
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
    contentDescription: String? = text,
    enabled: Boolean = true,
    onClick: (() -> Unit) = {},
    feedback: (android.view.View) -> Unit = com.example.vibefinance.util.VibeHaptics::lightTick,
    onLongClick: (() -> Unit)? = null,
    cornerRadius: Dp? = null,
) {
    val localDensity = LocalDensity.current
    val view = LocalView.current
    var minSize by remember { mutableStateOf(MAX_VALUE.dp) }
    var minSizeFloat by remember { mutableStateOf(MAX_VALUE.toFloat()) }
    val interactionSource = remember { MutableInteractionSource() }
    // Match Assets' connected controls: 180ms inward morph, 220ms return, no spring tail.
    val shapeProgress = rememberCompletePressProgress(interactionSource, enabled)
    val currentFeedback by rememberUpdatedState(feedback)
    LaunchedEffect(interactionSource, enabled) {
        interactionSource.interactions.collect { interaction ->
            if (enabled && interaction is PressInteraction.Press) currentFeedback(view)
        }
    }
    val initialRadius = cornerRadius ?: if (minSize == MAX_VALUE.dp) 28.dp else minSize / 2

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

    // Shape feedback keeps the key, its label and its touch target at their full size.
    Box(
        modifier = modifier
            .semantics { if (!enabled) disabled() }
            .fillMaxSize()
            .onGloballyPositioned {
                minSize = with(localDensity) { min(it.size.height, it.size.width).toDp() }
                minSizeFloat = min(it.size.height, it.size.width).toFloat()
            }
    ) {
        Box(Modifier.fillMaxSize().combinedClickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        onClick.invoke()
                    },
                    hapticFeedbackEnabled = false,
                    onLongClick = onLongClick?.let { action -> {
                        com.example.vibefinance.util.VibeHaptics.heavyLongPress(view)
                        action()
                    } },
                )) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .graphicsLayer {
                        val progress = shapeProgress.value.coerceIn(0f, 1f)
                        shape = RoundedCornerShape(androidx.compose.ui.unit.lerp(initialRadius, 16.dp, progress))
                        clip = true
                    }
                    .background(color)
                    .indication(interactionSource, ripple(bounded = true, color = contentColor)),
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
                        contentDescription = contentDescription,
                    )
                }
            }
        }
    }
}
