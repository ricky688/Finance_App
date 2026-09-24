package com.example.vibefinance.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vibefinance.data.repository.DailyBudgetInfo
import com.example.vibefinance.ui.main.ExpressiveSegmentedButtonGroup
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class PeriodFilterMode {
    ALL,
    ACTIVE_PERIOD,
    OTHER_PERIODS
}

@Composable
fun BudgetPeriodIndicatorCard(
    budgetInfo: DailyBudgetInfo?,
    selectedFilter: PeriodFilterMode,
    onSelectFilter: (PeriodFilterMode) -> Unit,
    modifier: Modifier = Modifier
) {
    if (budgetInfo == null || budgetInfo.startDate <= 0L || budgetInfo.endDate <= 0L) {
        return
    }

    val startFormatted = Instant.ofEpochMilli(budgetInfo.startDate)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US))

    val endFormatted = Instant.ofEpochMilli(budgetInfo.endDate)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US))

    val spentRatio = if (budgetInfo.totalMonthlyBudget > 0) {
        (budgetInfo.totalSpentThisMonth / budgetInfo.totalMonthlyBudget).coerceIn(0.0, 1.0).toFloat()
    } else {
        0.0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = spentRatio,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "budget_period_progress_spring"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // 1. Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = androidx.compose.ui.res.stringResource(com.example.vibefinance.R.string.budget_period_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = androidx.compose.ui.res.stringResource(com.example.vibefinance.R.string.badge_active),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    maxLines = 1
                                )
                            }
                        }
                        Text(
                            text = "$startFormatted – $endFormatted",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.example.vibefinance.R.string.days_left_format, budgetInfo.daysLeft),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Budget Progress Bar & Amount Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.example.vibefinance.R.string.history_spent_format, budgetInfo.totalSpentThisMonth),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = androidx.compose.ui.res.stringResource(com.example.vibefinance.R.string.history_budget_format, budgetInfo.totalMonthlyBudget),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { animatedProgress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (spentRatio >= 1.0f) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Expressive Connected Button Group (ExpressiveSegmentedButtonGroup)
            val periodModes = PeriodFilterMode.entries
            val selectedIndex = periodModes.indexOf(selectedFilter)
            val strAll = androidx.compose.ui.res.stringResource(com.example.vibefinance.R.string.period_filter_all)
            val strActive = androidx.compose.ui.res.stringResource(com.example.vibefinance.R.string.period_filter_active)
            val strOther = androidx.compose.ui.res.stringResource(com.example.vibefinance.R.string.period_filter_other)

            ExpressiveSegmentedButtonGroup(
                items = periodModes,
                selectedIndex = selectedIndex,
                onItemSelected = { index -> onSelectFilter(periodModes[index]) },
                isScrollable = false,
                labelProvider = { mode ->
                    when (mode) {
                        PeriodFilterMode.ALL -> strAll
                        PeriodFilterMode.ACTIVE_PERIOD -> strActive
                        PeriodFilterMode.OTHER_PERIODS -> strOther
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
