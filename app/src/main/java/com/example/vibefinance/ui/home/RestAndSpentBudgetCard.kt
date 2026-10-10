package com.example.vibefinance.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibefinance.theme.BentoCardShape
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

    val ratio = if (totalBudget > 0) (remainingBudget / totalBudget).coerceIn(0.0, 1.0).toFloat() else 0f
    val displayAmount = if (showSpentCard) {
        (totalBudget - remainingBudget).coerceAtLeast(0.0)
    } else {
        remainingBudget
    }

    val labelSpent = androidx.compose.ui.res.stringResource(com.example.vibefinance.R.string.total_spent)
    val labelRemaining = androidx.compose.ui.res.stringResource(com.example.vibefinance.R.string.total_remaining)
    val displayLabel = if (showSpentCard) labelSpent else labelRemaining
    
    val percentFormatted = String.format(Locale.US, "%.2f%%", ratio * 100)

    val shiftState = rememberAmbientWavePhase(enabled = ratio > 0f)

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
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(ratio)
                        .graphicsLayer {
                            shape = WavyShape(
                                period = 40.dp,
                                amplitude = 2.dp,
                                shift = shiftState.value,
                            )
                            clip = true
                        }
                        .background(colors.main),
                )

                val verticalPadding = if (heightDp > 0.dp) (heightDp * 0.1f).coerceIn(8.dp, 16.dp) else 14.dp
                val horizontalPadding = if (widthDp > 0.dp) (widthDp * 0.12f).coerceIn(12.dp, 18.dp) else 14.dp

                val baseAmountFontSize = if (heightDp > 0.dp) (heightDp.value * 0.15f).coerceIn(16f, 26f).sp else 24.sp
                val formattedAmount = String.format(Locale.US, "$%,.2f", displayAmount)
                val amountFontSize = when {
                    formattedAmount.length >= 12 -> (baseAmountFontSize.value * 0.75f).sp
                    formattedAmount.length >= 10 -> (baseAmountFontSize.value * 0.85f).sp
                    else -> baseAmountFontSize
                }
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
                    com.example.vibefinance.ui.components.RollingNumberText(
                        text = formattedAmount,
                        style = androidx.compose.ui.text.TextStyle(fontSize = amountFontSize),
                        fontWeight = FontWeight.Bold,
                        color = colors.onContainer
                    )
                    Text(
                        text = displayLabel,
                        fontSize = labelFontSize,
                        color = colors.onContainer.copy(alpha = 0.7f),
                        maxLines = 1
                    )
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
                        color = colors.onContainer.copy(alpha = if (showSpentCard) 0.3f else 1f),
                        shape = CircleShape,
                    )
            )
            Spacer(Modifier.width(4.dp))
            Box(
                Modifier
                    .size(4.dp)
                    .background(
                        color = colors.onContainer.copy(alpha = if (!showSpentCard) 0.3f else 1f),
                        shape = CircleShape,
                    )
            )
        }
    }
}
