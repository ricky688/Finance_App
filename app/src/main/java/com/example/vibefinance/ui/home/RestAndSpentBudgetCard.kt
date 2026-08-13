package com.example.vibefinance.ui.home

import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.vibefinance.ui.common.bouncyClickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    val displayLabel = if (showSpentCard) "已花費" else "總剩餘"
    
    val percentFormatted = String.format(Locale.US, "%.2f%%", ratio * 100)

    val infiniteTransition = rememberInfiniteTransition(label = "waveShift")
    val shift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveShift"
    )

    // Custom harmonized colors matching buckwheat screenshot
    val colors = remember(ratio, isDarkTheme) {
        if (ratio < 0.2f) {
            // Low budget -> Red/Pink
            if (isDarkTheme) {
                HarmonizedColorPalette(
                    main = Color(0xFFC62828),
                    onMain = Color(0xFFFFFFFF),
                    container = Color(0xFF2C1C1D),
                    onContainer = Color(0xFFFFCDD2)
                )
            } else {
                HarmonizedColorPalette(
                    main = Color(0xFFE57373),
                    onMain = Color(0xFFFFFFFF),
                    container = Color(0xFFFFEBEE),
                    onContainer = Color(0xFFC62828)
                )
            }
        } else if (ratio < 0.5f) {
            // Medium budget -> Orange/Yellow
            if (isDarkTheme) {
                HarmonizedColorPalette(
                    main = Color(0xFFE65100),
                    onMain = Color(0xFFFFFFFF),
                    container = Color(0xFF2D2319),
                    onContainer = Color(0xFFFFE0B2)
                )
            } else {
                HarmonizedColorPalette(
                    main = Color(0xFFFFB74D),
                    onMain = Color(0xFFFFFFFF),
                    container = Color(0xFFFFF3E0),
                    onContainer = Color(0xFFEF6C00)
                )
            }
        } else {
            // Good budget -> Green/Teal
            if (isDarkTheme) {
                HarmonizedColorPalette(
                    main = Color(0xFF2E7D32),
                    onMain = Color(0xFFFFFFFF),
                    container = Color(0xFF1B241C),
                    onContainer = Color(0xFFC8E6C9)
                )
            } else {
                HarmonizedColorPalette(
                    main = Color(0xFF81C784),
                    onMain = Color(0xFFFFFFFF),
                    container = Color(0xFFE8F5E9),
                    onContainer = Color(0xFF2E7D32)
                )
            }
        }
    }

    val localDensity = LocalDensity.current
    var heightDp by remember { mutableStateOf(0.dp) }
    var widthDp by remember { mutableStateOf(0.dp) }

    Box(
        modifier = modifier
            .clip(shape = RoundedCornerShape(24.dp))
            .bouncyClickable { showSpentCard = !showSpentCard }
            .onGloballyPositioned {
                heightDp = with(localDensity) { it.size.height.toDp() }
                widthDp = with(localDensity) { it.size.width.toDp() }
            }
    ) {
        val resolvedHeight = if (heightDp > 0.dp) heightDp else 160.dp

        Card(
            modifier = Modifier.fillMaxWidth().height(resolvedHeight),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = colors.container,
                contentColor = colors.onContainer,
            ),
        ) {
            Box(
                Modifier.fillMaxSize()
            ) {
                // Wave background
                Box(
                    modifier = Modifier
                        .background(
                            colors.main,
                            shape = WavyShape(
                                period = 40.dp,
                                amplitude = 2.dp * (1f - ((ratio.coerceIn(0.96f, 1f) - 0.96f) / (1f - 0.96f))),
                                shift = shift,
                            ),
                        )
                        .fillMaxHeight()
                        .fillMaxWidth(ratio),
                )

                val verticalPadding = if (heightDp > 0.dp) (heightDp * 0.1f).coerceIn(8.dp, 16.dp) else 16.dp
                val horizontalPadding = if (widthDp > 0.dp) (widthDp * 0.15f).coerceIn(12.dp, 24.dp) else 24.dp

                val amountFontSize = if (heightDp > 0.dp) (heightDp.value * 0.15f).coerceIn(16f, 26f).sp else 24.sp
                val labelFontSize = if (heightDp > 0.dp) (heightDp.value * 0.075f).coerceIn(10f, 14f).sp else 12.sp
                val percentFontSize = if (heightDp > 0.dp) (heightDp.value * 0.07f).coerceIn(9f, 12f).sp else 11.sp
                val spacerHeight = if (heightDp > 0.dp) (heightDp * 0.04f).coerceIn(2.dp, 8.dp) else 6.dp

                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(vertical = verticalPadding, horizontal = horizontalPadding),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    com.example.vibefinance.ui.components.RollingNumberText(
                        text = String.format(Locale.US, "$%,.2f", displayAmount),
                        style = androidx.compose.ui.text.TextStyle(fontSize = amountFontSize),
                        fontWeight = FontWeight.Bold,
                        color = colors.onContainer
                    )
                    Text(
                        text = displayLabel,
                        fontSize = labelFontSize,
                        color = colors.onContainer.copy(alpha = 0.6f),
                    )
                    Spacer(modifier = Modifier.height(spacerHeight))
                    Text(
                        text = "預算的 $percentFormatted",
                        fontSize = percentFontSize,
                        color = colors.onContainer.copy(alpha = 0.8f)
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
