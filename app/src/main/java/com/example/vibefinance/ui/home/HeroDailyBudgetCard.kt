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
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.vibefinance.R
import com.example.vibefinance.data.repository.DailyBudgetInfo
import com.example.vibefinance.theme.HeroCardShape
import com.example.vibefinance.ui.common.bouncyClickable
import java.util.Locale

/**
 * State classification for the Hero Daily Budget Card according to Buckwheat rules.
 */
enum class HeroDailyBudgetState {
    NORMAL,
    OVERDRAFT,
    BUDGET_END,
    PERIOD_ENDED,
    NO_BUDGET
}

/**
 * Calculates the current hero daily budget financial state according to Buckwheat rules.
 */
fun calculateHeroDailyBudgetState(
    info: DailyBudgetInfo,
    currentTimeMillis: Long = System.currentTimeMillis()
): HeroDailyBudgetState {
    if (info.totalMonthlyBudget <= 0.0 || info.endDate <= 0L) return HeroDailyBudgetState.NO_BUDGET
    val isPeriodEnded = info.daysLeft <= 0 || (info.endDate > 0 && currentTimeMillis >= info.endDate)
    if (isPeriodEnded) return HeroDailyBudgetState.PERIOD_ENDED
    val isBudgetEnd = info.monthlyRemaining <= 0.0 || (info.dailyRemaining < 0.0 && info.newDailyBudget <= 0.0)
    if (isBudgetEnd) return HeroDailyBudgetState.BUDGET_END
    val isOverdraft = info.dailyRemaining < 0.0
    if (isOverdraft) return HeroDailyBudgetState.OVERDRAFT
    return HeroDailyBudgetState.NORMAL
}

/**
 * Calculates responsive typography font size for hero amount based on character length.
 */
fun calculateHeroAmountFontSize(formattedText: String): TextUnit = when {
    formattedText.length >= 13 -> 24.sp
    formattedText.length >= 10 -> 28.sp
    formattedText.length >= 8 -> 32.sp
    formattedText.length >= 6 -> 36.sp
    else -> 40.sp
}

/**
 * Formats hero target amount, retaining negative deficit sign when period has ended.
 */
fun formatHeroTargetAmount(
    targetAmount: Float,
    isPeriodEnded: Boolean,
    monthlyRemaining: Double
): String {
    val absTarget = Math.abs(targetAmount)
    return if (isPeriodEnded && monthlyRemaining < 0) {
        String.format(Locale.US, "-HK$ %,.0f", absTarget)
    } else {
        String.format(Locale.US, "HK$ %,.0f", absTarget)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeroDailyBudgetCard(
    budgetInfo: DailyBudgetInfo,
    onOpenRecalcSheet: () -> Unit,
    onOpenBudgetDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // Buckwheat State Machine
    val budgetState = calculateHeroDailyBudgetState(budgetInfo)
    val isNoBudget = budgetState == HeroDailyBudgetState.NO_BUDGET
    val isPeriodEnded = budgetState == HeroDailyBudgetState.PERIOD_ENDED
    val isBudgetEnd = budgetState == HeroDailyBudgetState.BUDGET_END
    val isOverdraft = budgetState == HeroDailyBudgetState.OVERDRAFT
    val isNormal = budgetState == HeroDailyBudgetState.NORMAL

    var showNewDayBudgetInfoSheet by remember { mutableStateOf(false) }
    var showBudgetEndInfoSheet by remember { mutableStateOf(false) }

    val dailyRem = budgetInfo.dailyRemaining
    val dailyAllowance = budgetInfo.dailyAllowance
    val todaySpent = (dailyAllowance - dailyRem).coerceAtLeast(0.0)

    val ratio = if (isNormal && dailyAllowance > 0) (dailyRem / dailyAllowance).coerceIn(0.0, 1.0).toFloat() else 0f

    // Let the ambient wave settle after its entrance instead of redrawing forever.
    val shiftState = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        shiftState.animateTo(1f, tween(durationMillis = 5000, easing = LinearEasing))
    }

    // Ambient Financial Health Mood Glow Color (active in dark mode only)
    val ambientGlowColor = when {
        !isDark -> Color.Transparent
        isNormal && ratio >= 0.5f -> MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) // Emerald Flow
        isNormal -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)                  // Sunset Amber
        isNoBudget -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        else -> MaterialTheme.colorScheme.error.copy(alpha = 0.22f)                   // Crimson Alert
    }

    // Spring Physics animation specs
    val colorSpringSpec = spring<Color>(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioNoBouncy)
    val ratioSpringSpec = spring<Float>(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)

    // Animated ratio fill with Spring physics
    val targetRatio = if (isNormal && dailyAllowance > 0) (dailyRem / dailyAllowance).coerceIn(0.0, 1.0).toFloat() else 0f
    val animatedRatio by animateFloatAsState(
        targetValue = targetRatio,
        animationSpec = ratioSpringSpec,
        label = "heroFillRatio"
    )

    // Target amount to display in giant rolling numbers
    val targetAmount = when {
        isNoBudget -> 0f
        isPeriodEnded -> budgetInfo.monthlyRemaining.toFloat()
        isBudgetEnd -> 0f
        isOverdraft -> budgetInfo.newDailyBudget.toFloat()
        else -> dailyRem.toFloat()
    }

    val targetThirdMetric = if (isOverdraft) budgetInfo.newDailyBudget.toFloat() else budgetInfo.tomorrowAllowance.toFloat()

    // Semantic Color tokens adaptation for Light & Dark Mode
    val targetContainerBg = when {
        isNormal || isNoBudget -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDark) 0.35f else 0.45f)
        else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = if (isDark) 0.35f else 0.45f)
    }
    val containerBg by animateColorAsState(targetContainerBg, colorSpringSpec, label = "containerBg")

    val targetWaveFillColor = when {
        isNormal || isNoBudget -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDark) 0.60f else 0.70f)
        else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = if (isDark) 0.60f else 0.70f)
    }
    val waveFillColor by animateColorAsState(targetWaveFillColor, colorSpringSpec, label = "waveFillColor")

    val targetCardBorderColor = when {
        isNormal || isNoBudget -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.error.copy(alpha = 0.35f)
    }
    val cardBorderColor by animateColorAsState(targetCardBorderColor, colorSpringSpec, label = "cardBorderColor")

    val targetTitleTextColor = when {
        isNormal || isNoBudget -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onErrorContainer
    }
    val titleTextColor by animateColorAsState(targetTitleTextColor, colorSpringSpec, label = "titleTextColor")

    val targetAmountTextColor = when {
        isNormal || isNoBudget -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.error
    }
    val amountTextColor by animateColorAsState(targetAmountTextColor, colorSpringSpec, label = "amountTextColor")

    val subtitleTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    val targetActionButtonBg = when {
        isNormal || isNoBudget -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.error
    }
    val actionButtonBg by animateColorAsState(targetActionButtonBg, colorSpringSpec, label = "actionButtonBg")

    val actionButtonTextColor = when {
        isNormal || isNoBudget -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onError
    }

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        // Ambient Mood Glow Layer (Dark Mode only)
        if (isDark && ambientGlowColor != Color.Transparent) {
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
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = HeroCardShape,
            color = containerBg,
            border = if (isDark) BorderStroke(1.dp, cardBorderColor) else null,
            shadowElevation = if (isDark) 4.dp else 0.dp
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Background Liquid Wavy Filler with Spring Motion
                if (animatedRatio > 0.005f) {
                    Box(
                        modifier = Modifier.matchParentSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animatedRatio.coerceIn(0.001f, 1.0f))
                                .graphicsLayer {
                                    shape = WavyShape(
                                        period = 36.dp,
                                        amplitude = 4.dp,
                                        shift = shiftState.value
                                    )
                                    clip = true
                                }
                                .background(waveFillColor)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 20.dp)
                ) {
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
                                border = BorderStroke(1.dp, titleTextColor.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .bouncyClickable(shape = CircleShape) {
                                        when {
                                            isNoBudget -> onOpenBudgetDialog()
                                            isOverdraft -> showNewDayBudgetInfoSheet = true
                                            isBudgetEnd -> showBudgetEndInfoSheet = true
                                            else -> onOpenBudgetDialog()
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = when {
                                            isNoBudget -> Icons.Default.Bolt
                                            isPeriodEnded -> Icons.Default.EventBusy
                                            isBudgetEnd -> Icons.Default.Warning
                                            isOverdraft -> Icons.Default.Info
                                            else -> Icons.Default.Bolt
                                        },
                                        contentDescription = null,
                                        tint = titleTextColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = when {
                                            isNoBudget -> stringResource(R.string.no_budget_set)
                                            isPeriodEnded -> stringResource(R.string.period_ended)
                                            isBudgetEnd -> stringResource(R.string.budget_end)
                                            isOverdraft -> stringResource(R.string.new_daily_budget_short)
                                            else -> stringResource(R.string.rest_budget_for_today)
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = titleTextColor
                                    )
                                    if (isOverdraft || isBudgetEnd) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "Info",
                                            tint = titleTextColor.copy(alpha = 0.7f),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Days Left Badge
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            modifier = Modifier
                                .clip(CircleShape)
                                .bouncyClickable(shape = CircleShape) { onOpenBudgetDialog() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = when {
                                        isNoBudget -> "—"
                                        isPeriodEnded -> "0 " + stringResource(R.string.period_ended)
                                        else -> stringResource(R.string.days_left_format, budgetInfo.daysLeft)
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Giant Iconic Hero Amount Display with Auto-scaling and Deficit Support
                    val formattedTarget = formatHeroTargetAmount(
                        targetAmount = targetAmount,
                        isPeriodEnded = isPeriodEnded,
                        monthlyRemaining = budgetInfo.monthlyRemaining
                    )
                    val amountFontSize = calculateHeroAmountFontSize(formattedTarget)

                    com.example.vibefinance.ui.components.RollingNumberText(
                        text = formattedTarget,
                        style = MaterialTheme.typography.displayMedium.copy(fontSize = amountFontSize),
                        fontWeight = FontWeight.Black,
                        color = amountTextColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Subtitle Metrics Breakdown (Responsive 3-column Bento Pills)
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val columnWidth = maxWidth / 3
                        val isCompactPill = columnWidth < 105.dp
                        val pillPaddingHorizontal = if (isCompactPill) 2.dp else 4.dp
                        val pillSpacing = if (isCompactPill) 6.dp else 8.dp
                        val labelFontSize = if (isCompactPill) 10.sp else 11.sp

                        fun autoMetricFontSize(text: String): androidx.compose.ui.unit.TextUnit = when {
                            text.length >= 13 -> 9.5.sp
                            text.length >= 10 -> 11.sp
                            isCompactPill -> 12.sp
                            else -> 13.sp
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(pillSpacing),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Column 1: Daily Target / Allowance
                            val dailyTargetText = String.format(Locale.US, "HK$ %,.0f", dailyAllowance)
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.45f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = pillPaddingHorizontal, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.hero_daily_target).trim(),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = labelFontSize),
                                        fontWeight = FontWeight.Medium,
                                        color = subtitleTextColor.copy(alpha = 0.85f),
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    com.example.vibefinance.ui.components.RollingNumberText(
                                        text = dailyTargetText,
                                        style = MaterialTheme.typography.labelMedium.copy(fontSize = autoMetricFontSize(dailyTargetText)),
                                        fontWeight = FontWeight.ExtraBold,
                                        color = subtitleTextColor
                                    )
                                }
                            }

                            // Column 2: Spent Today
                            val spentVal = if (todaySpent < 0.5) 0.0 else todaySpent
                            val spentTodayText = String.format(Locale.US, "HK$ %,.0f", spentVal)
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.45f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = pillPaddingHorizontal, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.hero_spent_today).trim(),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = labelFontSize),
                                        fontWeight = FontWeight.Medium,
                                        color = subtitleTextColor.copy(alpha = 0.85f),
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    com.example.vibefinance.ui.components.RollingNumberText(
                                        text = spentTodayText,
                                        style = MaterialTheme.typography.labelMedium.copy(fontSize = autoMetricFontSize(spentTodayText)),
                                        fontWeight = FontWeight.ExtraBold,
                                        color = subtitleTextColor
                                    )
                                }
                            }

                            // Column 3: Tomorrow's Allowance / New Daily Budget
                            val thirdVal = if (targetThirdMetric < 0.5f) 0.0 else targetThirdMetric.toDouble()
                            val thirdMetricText = String.format(Locale.US, "HK$ %,.0f", thirdVal)
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.45f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = pillPaddingHorizontal, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = if (isOverdraft) stringResource(R.string.new_daily_budget_short).trim() else stringResource(R.string.tomorrow_daily_format).trim(),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = labelFontSize),
                                        fontWeight = FontWeight.Medium,
                                        color = subtitleTextColor.copy(alpha = 0.85f),
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    com.example.vibefinance.ui.components.RollingNumberText(
                                        text = thirdMetricText,
                                        style = MaterialTheme.typography.labelMedium.copy(fontSize = autoMetricFontSize(thirdMetricText)),
                                        fontWeight = FontWeight.ExtraBold,
                                        color = subtitleTextColor
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .bouncyClickable(shape = RoundedCornerShape(16.dp)) {
                                    if (isNoBudget || isPeriodEnded || isBudgetEnd) {
                                        onOpenBudgetDialog()
                                    } else {
                                        onOpenRecalcSheet()
                                    }
                                },
                            shape = RoundedCornerShape(16.dp),
                            color = actionButtonBg,
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isNoBudget) Icons.Default.Bolt else Icons.Default.Autorenew,
                                    contentDescription = null,
                                    tint = actionButtonTextColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when {
                                        isNoBudget -> stringResource(R.string.btn_set_budget)
                                        isPeriodEnded || isBudgetEnd -> stringResource(R.string.btn_new_period)
                                        else -> stringResource(R.string.btn_recalculate_budget)
                                    },
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

    // Modal Sheet 1: Buckwheat New Daily Budget Description
    if (showNewDayBudgetInfoSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNewDayBudgetInfoSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.new_daily_budget),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = String.format(Locale.US, "HK$ %,.0f/day", budgetInfo.newDailyBudget),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.new_daily_budget_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { showNewDayBudgetInfoSheet = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(text = "OK", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Modal Sheet 2: Buckwheat Budget Is Over Description
    if (showBudgetEndInfoSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBudgetEndInfoSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.budget_end),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.budget_end_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        showBudgetEndInfoSheet = false
                        onOpenBudgetDialog()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(text = stringResource(R.string.btn_new_period), fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
