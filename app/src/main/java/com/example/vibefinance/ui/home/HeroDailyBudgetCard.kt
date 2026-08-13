package com.example.vibefinance.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibefinance.data.repository.DailyBudgetInfo
import com.example.vibefinance.ui.common.bouncyClickable
import java.util.Locale

@Composable
fun HeroDailyBudgetCard(
    budgetInfo: DailyBudgetInfo,
    onOpenRecalcSheet: () -> Unit,
    onOpenBudgetDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f || isSystemInDarkTheme()
    val dailyRem = budgetInfo.dailyRemaining
    val dailyAllowance = budgetInfo.dailyAllowance
    val isPos = dailyRem >= 0

    val ratio = if (dailyAllowance > 0) (dailyRem / dailyAllowance).coerceIn(0.0, 1.0).toFloat() else 0f

    // Animated wave movement
    val infiniteTransition = rememberInfiniteTransition(label = "heroWaveShift")
    val shift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "heroWaveShift"
    )

    // Option 2: Ambient Financial Health Mood Glow Color
    val ambientGlowColor = when {
        isPos && ratio >= 0.5f -> Color(0xFF00E676).copy(alpha = if (isDark) 0.18f else 0.25f) // Emerald Flow
        isPos -> Color(0xFFFFB74D).copy(alpha = if (isDark) 0.18f else 0.25f)                  // Sunset Amber
        else -> Color(0xFFFF5252).copy(alpha = if (isDark) 0.22f else 0.30f)                   // Crimson Alert
    }

    // Spring Physics animation specs
    val springSpec = spring<Color>(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)
    val floatSpringSpec = spring<Float>(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)

    // Animated ratio fill with Spring physics
    val animatedRatio by animateFloatAsState(
        targetValue = ratio.coerceAtLeast(0.08f),
        animationSpec = floatSpringSpec,
        label = "heroFillRatio"
    )

    // Animated amount number counter with Spring physics
    val animatedAmount by animateFloatAsState(
        targetValue = Math.abs(dailyRem).toFloat(),
        animationSpec = floatSpringSpec,
        label = "heroAmount"
    )

    // Animated daily allowance with Spring physics
    val animatedDailyAllowance by animateFloatAsState(
        targetValue = dailyAllowance.toFloat(),
        animationSpec = floatSpringSpec,
        label = "heroDailyAllowance"
    )

    // Animated monthly remaining with Spring physics
    val animatedMonthlyRemaining by animateFloatAsState(
        targetValue = budgetInfo.monthlyRemaining.toFloat(),
        animationSpec = floatSpringSpec,
        label = "heroMonthlyRemaining"
    )

    // Color tokens adaptation for Light (White Mode) & Dark Mode with Spring Transitions
    val targetContainerBg = when {
        !isDark && isPos -> Color(0xFFE8F5E9)  // Soft fresh mint in White Mode
        !isDark && !isPos -> Color(0xFFFFEBEE) // Soft light red in White Mode
        isDark && isPos -> Color(0xFF0F2618)   // Midnight forest green in Dark Mode
        else -> Color(0xFF2E1414)              // Midnight crimson in Dark Mode
    }
    val containerBg by animateColorAsState(targetContainerBg, springSpec, label = "containerBg")

    val targetWaveFillColor = when {
        !isDark && isPos -> Color(0xFFC8E6C9)
        !isDark && !isPos -> Color(0xFFFFCDD2)
        isDark && isPos -> Color(0xFF1B4D2E)
        else -> Color(0xFF5C1D1D)
    }
    val waveFillColor by animateColorAsState(targetWaveFillColor, springSpec, label = "waveFillColor")

    val targetCardBorderColor = when {
        !isDark && isPos -> Color(0xFF81C784)
        !isDark && !isPos -> Color(0xFFE57373)
        isDark && isPos -> Color(0xFF00E676).copy(alpha = 0.35f)
        else -> Color(0xFFFF5252).copy(alpha = 0.35f)
    }
    val cardBorderColor by animateColorAsState(targetCardBorderColor, springSpec, label = "cardBorderColor")

    val targetTitleTextColor = when {
        !isDark && isPos -> Color(0xFF1B5E20)
        !isDark && !isPos -> Color(0xFFB71C1C)
        isDark && isPos -> Color(0xFF81C784)
        else -> Color(0xFFFF8A80)
    }
    val titleTextColor by animateColorAsState(targetTitleTextColor, springSpec, label = "titleTextColor")

    val targetAmountTextColor = when {
        !isDark && isPos -> Color(0xFF1B5E20)
        !isDark && !isPos -> Color(0xFFC62828)
        isDark && isPos -> Color(0xFF00E676)
        else -> Color(0xFFFF5252)
    }
    val amountTextColor by animateColorAsState(targetAmountTextColor, springSpec, label = "amountTextColor")

    val subtitleTextColor = when {
        !isDark -> Color(0xFF2E7D32).copy(alpha = 0.85f)
        else -> Color.White.copy(alpha = 0.7f)
    }

    val targetActionButtonBg = when {
        !isDark && isPos -> Color(0xFF2E7D32)
        !isDark && !isPos -> Color(0xFFC62828)
        isDark && isPos -> Color(0xFF00E676)
        else -> Color(0xFFFF5252)
    }
    val actionButtonBg by animateColorAsState(targetActionButtonBg, springSpec, label = "actionButtonBg")

    val actionButtonTextColor = when {
        !isDark -> Color.White
        else -> Color(0xFF0A1F11)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
    ) {
        // Option 2: Ambient Financial Health Mood Glow Layer behind card
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(4.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(ambientGlowColor, Color.Transparent),
                        radius = 400f
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .blur(16.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = containerBg,
            border = BorderStroke(1.5.dp, cardBorderColor),
            shadowElevation = if (isDark) 8.dp else 4.dp
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Background Liquid Wavy Filler
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            waveFillColor,
                            shape = WavyShape(
                                period = 48.dp,
                                amplitude = 3.dp,
                                shift = shift
                            )
                        )
                        .fillMaxWidth(animatedRatio)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 20.dp)
                ) {
                    val isPeriodEnded = budgetInfo.daysLeft <= 0 || (budgetInfo.endDate > 0 && System.currentTimeMillis() >= budgetInfo.endDate)

                    // Header Status Badge & Title Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = titleTextColor.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, titleTextColor.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPeriodEnded) Icons.Default.Warning else if (isPos) Icons.Default.Bolt else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = titleTextColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (isPeriodEnded) "週期已結束" else if (isPos) "今日剩餘" else "今日超支",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = titleTextColor
                                    )
                                }
                            }
                        }

                        // Days Left Badge
                        Surface(
                            shape = CircleShape,
                            color = primaryTextColor(isDark).copy(alpha = 0.08f),
                            modifier = Modifier.bouncyClickable { onOpenBudgetDialog() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (isPeriodEnded) "0 天 (Ended)" else "剩餘 ${budgetInfo.daysLeft} 天",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryTextColor(isDark).copy(alpha = 0.8f)
                                )
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = primaryTextColor(isDark).copy(alpha = 0.6f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Giant Iconic Hero Amount Display: Today's Remaining Budget to Spend (e.g. HK$ 75)
                    com.example.vibefinance.ui.components.RollingNumberText(
                        text = String.format(Locale.US, "HK$ %,.0f", animatedAmount),
                        style = MaterialTheme.typography.displayMedium.copy(fontSize = 42.sp),
                        fontWeight = FontWeight.Black,
                        color = amountTextColor
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Subtitle Metrics Breakdown: Today Budget, Spent, & Next Days Daily Baseline
                    val startLocal = java.time.Instant.ofEpochMilli(budgetInfo.startDate).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                    val endLocal = java.time.Instant.ofEpochMilli(budgetInfo.endDate).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                    val totalDaysInPeriod = (java.time.temporal.ChronoUnit.DAYS.between(startLocal, endLocal) + 1).coerceAtLeast(1).toDouble()
                    val baseDaily = if (totalDaysInPeriod > 0) (budgetInfo.totalMonthlyBudget / totalDaysInPeriod).coerceAtLeast(0.0) else 0.0
                    val nextDaysAllowance = if (budgetInfo.rolloverMode == com.example.vibefinance.data.entity.RolloverMode.ADD_TO_NEXT_DAY) baseDaily else dailyAllowance
                    val todaySpent = (dailyAllowance - dailyRem).coerceAtLeast(0.0)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "今日預算 ",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = subtitleTextColor
                        )
                        com.example.vibefinance.ui.components.RollingNumberText(
                            text = String.format(Locale.US, "HK$%,.0f", animatedDailyAllowance),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = subtitleTextColor
                        )
                        Text(
                            text = "  •  已花 ",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = subtitleTextColor
                        )
                        com.example.vibefinance.ui.components.RollingNumberText(
                            text = String.format(Locale.US, "HK$%,.0f", todaySpent.toFloat()),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = subtitleTextColor
                        )
                        Text(
                            text = "  •  明起每天 ",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = subtitleTextColor
                        )
                        com.example.vibefinance.ui.components.RollingNumberText(
                            text = String.format(Locale.US, "HK$%,.0f", nextDaysAllowance.toFloat()),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = subtitleTextColor
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Action Buttons Row inside Hero Card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Option 1: Recalculate / New Period Button
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .bouncyClickable { onOpenRecalcSheet() },
                            shape = RoundedCornerShape(14.dp),
                            color = actionButtonBg,
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Autorenew,
                                    contentDescription = null,
                                    tint = actionButtonTextColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isPeriodEnded) "開啟新預算週期" else "重新計算預算",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = actionButtonTextColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun primaryTextColor(isDark: Boolean): Color {
    return if (isDark) Color.White else Color(0xFF1A1C20)
}
