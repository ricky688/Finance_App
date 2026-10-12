package com.example.vibefinance.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/** Draw-only transforms: measured sizes and the connected group's layout remain unchanged. */
@Composable
internal fun connectedSpatialMotion(index: Int, pressedIndex: Int?, isPressed: Boolean): Modifier {
    val direction = if (LocalLayoutDirection.current == LayoutDirection.Ltr) 1 else -1
    val offset = animateDpAsState(
        targetValue = when {
            pressedIndex == null -> 0.dp
            index == pressedIndex - 1 -> (-3 * direction).dp
            index == pressedIndex + 1 -> (3 * direction).dp
            else -> 0.dp
        },
        animationSpec = spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMediumLow),
        label = "neighborPush_$index"
    )
    val scale = animateFloatAsState(
        if (isPressed) 0.95f else 1f,
        spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMediumLow),
        label = "pressScale_$index"
    )
    // Read State only in the layer lambda; animation frames do not recompose the button.
    return Modifier.graphicsLayer {
        translationX = offset.value.toPx()
        scaleX = scale.value
        scaleY = scale.value
    }
}
