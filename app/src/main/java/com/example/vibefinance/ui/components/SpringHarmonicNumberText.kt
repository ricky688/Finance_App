package com.example.vibefinance.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Row
import com.example.vibefinance.ui.preferences.PrivacyText as Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch

/**
 * A harmonic spring physics text component with independent PER-DIGIT APPEAR & DISAPPEAR animations.
 *
 * - Appear / Addition: Digit stretches vertically (1.25x), pops up, overshoots, and wobbles to rest.
 * - Disappear / Removal: Digit drops downward (+Y displacement), squashes horizontally (0.3x), and fades out smoothly with harmonic spring physics.
 * - Independent: Unchanged digits remain completely steady.
 */
@Composable
fun SpringHarmonicNumberText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight = FontWeight.Bold
) {
    val experience = com.example.vibefinance.ui.preferences.LocalExperience.current
    if (experience.hideAmounts || experience.motion != com.example.vibefinance.ui.preferences.MotionLevel.FULL) {
        Text(if (experience.hideAmounts) "••••" else text, modifier = modifier, style = style, color = color, fontWeight = fontWeight)
        return
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        text.forEachIndexed { index, char ->
            key(index) {
                AnimatedContent(
                    targetState = char,
                    transitionSpec = {
                        val enterSpringFloat = spring<Float>(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                        val enterSpringOffset = spring<IntOffset>(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        )

                        val exitSpringFloat = spring<Float>(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                        val exitSpringOffset = spring<IntOffset>(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )

                        (fadeIn(animationSpec = enterSpringFloat) +
                         scaleIn(initialScale = 0.4f, transformOrigin = TransformOrigin(0.5f, 0.85f), animationSpec = enterSpringFloat) +
                         slideInVertically(animationSpec = enterSpringOffset) { -it / 2 }
                        ).togetherWith(
                         fadeOut(animationSpec = exitSpringFloat) +
                         scaleOut(targetScale = 0.3f, transformOrigin = TransformOrigin(0.5f, 0.85f), animationSpec = exitSpringFloat) +
                         slideOutVertically(animationSpec = exitSpringOffset) { it / 2 }
                        )
                    },
                    label = "DigitSpringHarmonic_$index"
                ) { targetChar ->
                    val translationAnim = remember(targetChar) { Animatable(-12f) }
                    val stretchAnim = remember(targetChar) { Animatable(1.25f) }

                    LaunchedEffect(targetChar) {
                        launch {
                            translationAnim.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                        launch {
                            stretchAnim.animateTo(
                                targetValue = 1.0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                        }
                    }

                    Text(
                        text = targetChar.toString(),
                        style = style,
                        color = color,
                        fontWeight = fontWeight,
                        modifier = Modifier.graphicsLayer {
                            translationY = translationAnim.value
                            scaleY = stretchAnim.value
                            scaleX = (2.0f - stretchAnim.value).coerceIn(0.75f, 1.25f)
                            transformOrigin = TransformOrigin(0.5f, 0.85f)
                        }
                    )
                }
            }
        }
    }
}
