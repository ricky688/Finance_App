package com.example.vibefinance.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
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
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        text.forEachIndexed { index, char ->
            if (char.isDigit()) {
                AnimatedContent(
                    targetState = char,
                    transitionSpec = {
                        if (targetState >= initialState) {
                            (slideInVertically(animationSpec = tween(220)) { height -> height } + fadeIn())
                                .togetherWith(slideOutVertically(animationSpec = tween(220)) { height -> -height } + fadeOut())
                        } else {
                            (slideInVertically(animationSpec = tween(220)) { height -> -height } + fadeIn())
                                .togetherWith(slideOutVertically(animationSpec = tween(220)) { height -> height } + fadeOut())
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
