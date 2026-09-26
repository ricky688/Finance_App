package com.example.vibefinance.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibefinance.theme.BentoCardShape
import com.example.vibefinance.ui.common.bouncyClickable

enum class IndicatorShape {
    FLAT,
    WAVY
}

/**
 * Remaining budget-period days, shown with a determinate Material 3 circular indicator.
 * Tapping the card springs between the flat and wavy shapes.
 */
fun calculateDaysLeftProgress(daysLeft: Int, totalDays: Int): Float =
    if (totalDays > 0) (daysLeft.toFloat() / totalDays).coerceIn(0f, 1f) else 0f

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DaysLeftCard(
    modifier: Modifier = Modifier,
    daysLeft: Int,
    totalDays: Int,
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    var indicatorShape by remember { mutableStateOf(IndicatorShape.WAVY) }

    // Percentage = Days Left / Total Period Days
    val progressTarget = calculateDaysLeftProgress(daysLeft, totalDays)

    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "daysLeftProgress"
    )

    val activeColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
    val ringStroke = Stroke(
        width = with(LocalDensity.current) { 5.dp.toPx() },
        cap = StrokeCap.Round
    )

    Card(
        modifier = modifier
            .fillMaxHeight()
            .clip(BentoCardShape)
            .bouncyClickable(shape = BentoCardShape) {
                // Toggle between Flat and Wavy M3 Expressive shapes on tap
                indicatorShape = if (indicatorShape == IndicatorShape.FLAT) IndicatorShape.WAVY else IndicatorShape.FLAT
            },
        shape = BentoCardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 2.dp else 0.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            val circleDiameter = (minOf(maxWidth, maxHeight) - 8.dp).coerceAtMost(116.dp).coerceAtLeast(72.dp)

            Box(
                modifier = Modifier.size(circleDiameter),
                contentAlignment = Alignment.Center
            ) {
                CircularWavyProgressIndicator(
                    progress = { animatedProgress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxSize(),
                    color = activeColor,
                    trackColor = trackColor,
                    stroke = ringStroke,
                    trackStroke = ringStroke,
                    amplitude = { if (indicatorShape == IndicatorShape.WAVY) 1f else 0f },
                    waveSpeed = 0.dp
                )

                // Center Text: Days Left, Label & M3 Shape Mode Pill
                Column(
                    modifier = Modifier.offset(y = (-8).dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val circleVal = circleDiameter.value
                    Surface(
                        shape = CircleShape,
                        color = activeColor.copy(alpha = 0.12f)
                    ) {
                        val shapeLabel = if (indicatorShape == IndicatorShape.WAVY) {
                            androidx.compose.ui.res.stringResource(com.example.vibefinance.R.string.indicator_shape_wavy)
                        } else {
                            androidx.compose.ui.res.stringResource(com.example.vibefinance.R.string.indicator_shape_flat)
                        }
                        Text(
                            text = shapeLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = (circleVal * 0.085f).coerceIn(9f, 11f).sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = activeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = daysLeft.toString(),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = (circleVal * 0.28f).coerceIn(18f, 38f).sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.example.vibefinance.R.string.days_left_label),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = (circleVal * 0.095f).coerceIn(9f, 13f).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
