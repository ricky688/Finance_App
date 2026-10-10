package com.example.vibefinance.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import com.example.vibefinance.ui.preferences.PrivacyText as Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

@Composable
fun RollingNumberText(
    text: String,
    style: TextStyle,
    fontWeight: FontWeight = FontWeight.Bold,
    color: Color = Color.Unspecified,
    modifier: Modifier = Modifier
) {
    val experience = com.example.vibefinance.ui.preferences.LocalExperience.current
    val displayText = if (experience.hideAmounts) com.example.vibefinance.ui.preferences.redactAmounts(text) else text
    if (experience.motion != com.example.vibefinance.ui.preferences.MotionLevel.FULL || displayText != text) {
        Text(displayText, modifier = modifier, style = style, fontWeight = fontWeight, color = color)
        return
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        text.forEachIndexed { index, char ->
            if (char.isDigit()) {
                AnimatedContent(
                    targetState = char,
                    transitionSpec = {
                        val digitSpring = spring<androidx.compose.ui.unit.IntOffset>(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                        if (targetState >= initialState) {
                            (slideInVertically(animationSpec = digitSpring) { height -> height } + fadeIn())
                                .togetherWith(slideOutVertically(animationSpec = digitSpring) { height -> -height } + fadeOut())
                        } else {
                            (slideInVertically(animationSpec = digitSpring) { height -> -height } + fadeIn())
                                .togetherWith(slideOutVertically(animationSpec = digitSpring) { height -> height } + fadeOut())
                        }
                    },
                    label = "digit_$index"
                ) { animatedChar ->
                    Text(
                        text = animatedChar.toString(),
                        style = style,
                        fontWeight = fontWeight,
                        color = color
                    )
                }
            } else {
                Text(
                    text = char.toString(),
                    style = style,
                    fontWeight = fontWeight,
                    color = color
                )
            }
        }
    }
}

