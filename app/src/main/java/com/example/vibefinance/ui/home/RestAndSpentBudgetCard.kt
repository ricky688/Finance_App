@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import com.example.vibefinance.ui.common.bouncyClickable
import androidx.compose.material3.*
import com.example.vibefinance.ui.preferences.PrivacyText as Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibefinance.theme.BentoCardShape
import com.example.vibefinance.ui.preferences.LocalExperience
import com.example.vibefinance.ui.preferences.MotionLevel
import java.util.Locale

data class HarmonizedColorPalette(
    val main: Color,
    val onMain: Color,
    val container: Color,
    val onContainer: Color,
)

@Composable
fun RestAndSpentBudgetCard(
    modifier: Modifier = Modifier,
    remainingBudget: Double,
    totalBudget: Double,
    isDarkTheme: Boolean = false,
) {
    var showSpentCard by remember { mutableStateOf(false) }
    val motion = LocalExperience.current.motion
    val effectsSpec = if (motion == MotionLevel.MINIMAL) snap<Float>()
        else MaterialTheme.motionScheme.fastEffectsSpec<Float>()
    val spatialSpec = MaterialTheme.motionScheme.fastSpatialSpec<androidx.compose.ui.unit.IntOffset>()
    val modeTransition = updateTransition(showSpentCard, label = "budgetMode")
    val remainingDotAlpha by modeTransition.animateFloat(
        transitionSpec = { effectsSpec }, label = "remainingDotAlpha"
    ) { if (it) 0.3f else 1f }

    val ratio = if (totalBudget > 0) (remainingBudget / totalBudget).coerceIn(0.0, 1.0).toFloat() else 0f
    val isFullBudget = ratio >= 1.0f

    // When remaining budget is 100% (full), amplitude is 0.dp to show a clean full style.
    // Otherwise (< 100%), maintain the exact original wave effect (2.dp amplitude).
    val animatedAmplitude by animateDpAsState(
        targetValue = if (isFullBudget) 0.dp else 2.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "restWaveAmplitude"
    )

    val labelSpent = androidx.compose.ui.res.stringResource(com.example.vibefinance.R.string.total_spent)
    val labelRemaining = androidx.compose.ui.res.stringResource(com.example.vibefinance.R.string.total_remaining)
    
    val percentFormatted = String.format(Locale.US, "%.2f%%", ratio * 100)

    val shiftState = rememberAmbientWavePhase(enabled = ratio > 0f && !isFullBudget)

    val colorScheme = MaterialTheme.colorScheme

    // Semantic tokens replacing hardcoded hex colors
    val colors = remember(ratio, colorScheme) {
        when {
            ratio < 0.2f -> HarmonizedColorPalette(
                main = colorScheme.error.copy(alpha = 0.30f),
                onMain = colorScheme.onError,
                container = colorScheme.errorContainer,
                onContainer = colorScheme.onErrorContainer
            )
            ratio < 0.5f -> HarmonizedColorPalette(
                main = colorScheme.tertiary.copy(alpha = 0.30f),
                onMain = colorScheme.onTertiary,
                container = colorScheme.tertiaryContainer,
                onContainer = colorScheme.onTertiaryContainer
            )
            else -> HarmonizedColorPalette(
                main = colorScheme.primary.copy(alpha = 0.30f),
                onMain = colorScheme.onPrimary,
                container = colorScheme.primaryContainer,
                onContainer = colorScheme.onPrimaryContainer
            )
        }
    }

    val localDensity = LocalDensity.current
    var heightDp by remember { mutableStateOf(0.dp) }
    var widthDp by remember { mutableStateOf(0.dp) }

    Box(
        modifier = modifier
            .testTag("RemainingBudgetCard")
            .clip(shape = BentoCardShape)
            .bouncyClickable(shape = BentoCardShape) { showSpentCard = !showSpentCard }
            .onGloballyPositioned {
                heightDp = with(localDensity) { it.size.height.toDp() }
                widthDp = with(localDensity) { it.size.width.toDp() }
            }
    ) {
        val resolvedHeight = if (heightDp > 0.dp) heightDp else 160.dp

        Card(
            modifier = Modifier.fillMaxWidth().height(resolvedHeight),
            shape = BentoCardShape,
            colors = CardDefaults.cardColors(
                containerColor = colors.container,
                contentColor = colors.onContainer,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkTheme) 2.dp else 0.dp)
        ) {
            Box(
                Modifier.fillMaxSize()
            ) {
                // Wave background
                if (ratio > 0.005f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(ratio)
                            .graphicsLayer {
                                shape = WavyShape(
                                    period = 40.dp,
                                    amplitude = animatedAmplitude,
                                    shift = shiftState.value,
                                )
                                clip = true
                            }
                            .background(colors.main),
                    )
                }

                val verticalPadding = if (heightDp > 0.dp) (heightDp * 0.1f).coerceIn(8.dp, 16.dp) else 14.dp
                val horizontalPadding = if (widthDp > 0.dp) (widthDp * 0.12f).coerceIn(12.dp, 18.dp) else 14.dp

                val baseAmountFontSize = if (heightDp > 0.dp) (heightDp.value * 0.15f).coerceIn(16f, 26f).sp else 24.sp
                val labelFontSize = if (heightDp > 0.dp) (heightDp.value * 0.075f).coerceIn(10f, 14f).sp else 12.sp
                val percentFontSize = if (heightDp > 0.dp) (heightDp.value * 0.07f).coerceIn(9f, 12f).sp else 11.sp
                val spacerHeight = if (heightDp > 0.dp) (heightDp * 0.04f).coerceIn(2.dp, 8.dp) else 4.dp

                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(vertical = verticalPadding, horizontal = horizontalPadding),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    modeTransition.AnimatedContent(
                        modifier = Modifier.fillMaxWidth(),
                        transitionSpec = {
                            when (motion) {
                                MotionLevel.MINIMAL -> EnterTransition.None togetherWith ExitTransition.None
                                MotionLevel.REDUCED -> fadeIn(effectsSpec) togetherWith fadeOut(effectsSpec)
                                MotionLevel.FULL -> {
                                    val direction = if (targetState) 1 else -1
                                    (slideInVertically(spatialSpec) { direction * it / 4 } + fadeIn(effectsSpec))
                                        .togetherWith(slideOutVertically(spatialSpec) { -direction * it / 4 } + fadeOut(effectsSpec))
                                }
                            }.using(null)
                        },
                        contentAlignment = Alignment.CenterStart,
                    ) { spent ->
                        val amount = if (spent) (totalBudget - remainingBudget).coerceAtLeast(0.0) else remainingBudget
                        val formattedAmount = String.format(Locale.US, "$%,.2f", amount)
                        val amountFontSize = when {
                            formattedAmount.length >= 12 -> (baseAmountFontSize.value * 0.75f).sp
                            formattedAmount.length >= 10 -> (baseAmountFontSize.value * 0.85f).sp
                            else -> baseAmountFontSize
                        }
                        // Keep each exact amount paired with its label throughout the transition.
                        Column(Modifier.fillMaxWidth()
                            .testTag(if (spent) "BudgetMode_SPENT" else "BudgetMode_REMAINING")
                            .then(if (spent != showSpentCard) Modifier.clearAndSetSemantics {} else Modifier)) {
                            Text(
                                text = formattedAmount,
                                fontSize = amountFontSize,
                                fontWeight = FontWeight.Bold,
                                color = colors.onContainer,
                                maxLines = 1,
                            )
                            Text(
                                text = if (spent) labelSpent else labelRemaining,
                                fontSize = labelFontSize,
                                color = colors.onContainer.copy(alpha = 0.7f),
                                maxLines = 1,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(spacerHeight))
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.example.vibefinance.R.string.percent_of_budget, percentFormatted),
                        fontSize = percentFontSize,
                        color = colors.onContainer.copy(alpha = 0.85f),
                        maxLines = 1
                    )
                }
            }
        }

        // Indicator dots at top right
        Row(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd)
        ) {
            Box(
                Modifier
                    .size(4.dp)
                    .background(
                        color = colors.onContainer.copy(alpha = remainingDotAlpha.coerceIn(0.3f, 1f)),
                        shape = CircleShape,
                    )
            )
            Spacer(Modifier.width(4.dp))
            Box(
                Modifier
                    .size(4.dp)
                    .background(
                        color = colors.onContainer.copy(alpha = (1.3f - remainingDotAlpha).coerceIn(0.3f, 1f)),
                        shape = CircleShape,
                    )
            )
        }
    }
}
