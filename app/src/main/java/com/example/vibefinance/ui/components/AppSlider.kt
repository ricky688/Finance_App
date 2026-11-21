@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.vibefinance.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
internal fun AppSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val coerced = value.coerceIn(valueRange.start, valueRange.endInclusive)
    val sliderState = remember(steps, valueRange) {
        SliderState(value = coerced, steps = steps, trackRange = valueRange)
    }
    if (sliderState.value != coerced) {
        sliderState.value = coerced
    }
    Slider(
        state = sliderState,
        onValueChange = { next ->
            sliderState.value = next
            onValueChange(next)
        },
        modifier = modifier,
        enabled = enabled,
        onValueChangeFinished = onValueChangeFinished,
        colors = colors,
        interactionSource = interactionSource
    )
}
