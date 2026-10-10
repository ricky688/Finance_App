package com.example.vibefinance.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.vibefinance.ui.components.completePressShape
import com.example.vibefinance.ui.components.rememberCompletePressProgress

/** One native click target drives both the subcard outline and its bounded ripple. */
@Composable
internal fun SettingsActionCard(
    onClick: () -> Unit,
    shape: RoundedCornerShape,
    color: Color,
    modifier: Modifier = Modifier,
    border: BorderStroke? = null,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val progress by rememberCompletePressProgress(source)
    Surface(
        onClick = onClick,
        interactionSource = source,
        shape = completePressShape(shape, RoundedCornerShape(10.dp), progress),
        color = color,
        modifier = modifier,
        border = border,
        content = content
    )
}
