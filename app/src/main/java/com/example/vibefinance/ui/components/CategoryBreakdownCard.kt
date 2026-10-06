package com.example.vibefinance.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.luminance
import com.example.vibefinance.theme.ChartColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.TransactionEntity
import java.util.Locale

enum class CategoryAnalyticsPeriodMode { MONTH, BUDGET_PERIOD, ALL_TIME }

@Composable
fun CategoryBreakdownCard(
    transactions: List<TransactionEntity>,
    onExportCsv: () -> Unit,
    modifier: Modifier = Modifier,
    selectedCategory: String? = null,
    onSelectCategory: ((String?) -> Unit)? = null,
    onImportData: (() -> Unit)? = null,
    periodMode: CategoryAnalyticsPeriodMode = CategoryAnalyticsPeriodMode.MONTH,
    periodLabel: String = "",
    hasBudgetPeriod: Boolean = false,
    onSelectPeriod: ((CategoryAnalyticsPeriodMode) -> Unit)? = null,
    onPreviousMonth: (() -> Unit)? = null,
    onNextMonth: (() -> Unit)? = null,
    canGoToNextMonth: Boolean = true,
    emptyStateMessage: String? = null,
    emptyStateHint: String? = null,
    onAddTransaction: (() -> Unit)? = null,
    onViewAllRecords: (() -> Unit)? = null
) {
    var actionsExpanded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val expenseTransactions = remember(transactions) {
        transactions.filter { it.toAccountId == null && !it.isBalanceAdjustment && it.amount > 0 }
    }
    val totalExpense = remember(expenseTransactions) { expenseTransactions.sumOf { it.amount } }
    val categoryTotals = remember(expenseTransactions) {
        expenseTransactions.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .entries.sortedByDescending { it.value }
    }

    val categoryCounts = remember(expenseTransactions) {
        expenseTransactions.groupBy { it.category }.mapValues { it.value.size }
    }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f || isSystemInDarkTheme()
    val primaryColor = MaterialTheme.colorScheme.primary

    val categoryColorMap = remember(categoryTotals, isDark, primaryColor) {
        ChartColors.buildCategoryColorMap(
            categories = categoryTotals.map { it.key },
            isDark = isDark,
            primaryColor = primaryColor
        )
    }

    val activeCategory = selectedCategory
    var lastVisibleCategory by remember { mutableStateOf<String?>(null) }
    SideEffect {
        if (activeCategory != null) lastVisibleCategory = activeCategory
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("CategoryAnalyticsCard")
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Secondary file actions occupy one touch target; title and hint wrap normally.
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(34.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PieChart, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.category_analytics_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth().testTag("CategoryAnalyticsTitle")
                    )
                    val localizedCategory = selectedCategory?.let {
                        com.example.vibefinance.ui.home.getCategoryDisplayName(it)
                    }
                    Text(
                        text = if (localizedCategory != null) stringResource(R.string.category_analytics_filtering, localizedCategory)
                            else stringResource(R.string.category_tap_to_filter),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (localizedCategory != null) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().testTag("CategoryAnalyticsHint")
                    )
                }
                if (selectedCategory != null) {
                    IconButton(
                        onClick = { onSelectCategory?.invoke(null) },
                        modifier = Modifier.testTag("CategoryAnalyticsReset")
                    ) {
                        Icon(Icons.Default.Close, stringResource(R.string.reset_filter), tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Box {
                    IconButton(
                        onClick = { actionsExpanded = true },
                        modifier = Modifier.size(48.dp).testTag("CategoryAnalyticsActions")
                    ) {
                        Icon(Icons.Default.MoreVert, stringResource(R.string.analytics_more_actions))
                    }
                    DropdownMenu(
                        expanded = actionsExpanded,
                        onDismissRequest = { actionsExpanded = false },
                        modifier = Modifier.testTag("CategoryAnalyticsActionsMenu")
                    ) {
                        if (onImportData != null) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.import_title)) },
                                leadingIcon = { Icon(Icons.Default.FileUpload, null) },
                                onClick = { actionsExpanded = false; onImportData() },
                                modifier = Modifier.testTag("CategoryAnalyticsImport")
                            )
                            HorizontalDivider()
                        }
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.analytics_export_csv)) },
                            leadingIcon = { Icon(Icons.Default.FileDownload, null) },
                            onClick = { actionsExpanded = false; onExportCsv() },
                            modifier = Modifier.testTag("CategoryAnalyticsExport")
                        )
                    }
                }
            }

            if (onSelectPeriod != null) {
                Spacer(modifier = Modifier.height(12.dp))
                val modes = CategoryAnalyticsPeriodMode.entries.filter {
                    hasBudgetPeriod || it != CategoryAnalyticsPeriodMode.BUDGET_PERIOD
                }
                ExpressiveConnectedButtonGroup(
                    items = modes,
                    selectedIndex = modes.indexOf(periodMode),
                    onItemSelected = { index -> onSelectPeriod(modes[index]) },
                    modifier = Modifier.fillMaxWidth().testTag("CategoryAnalyticsRange"),
                    lightModeFocusMotion = true,
                    compact = true,
                    labelMaxLines = 2,
                    focusBackdropColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        .compositeOver(MaterialTheme.colorScheme.background),
                    labelProvider = { mode ->
                        stringResource(
                            when (mode) {
                                CategoryAnalyticsPeriodMode.MONTH -> R.string.category_analytics_month
                                CategoryAnalyticsPeriodMode.BUDGET_PERIOD -> R.string.category_analytics_budget_period
                                CategoryAnalyticsPeriodMode.ALL_TIME -> R.string.category_analytics_all_time
                            }
                        )
                    }
                )
            }

            if (periodMode == CategoryAnalyticsPeriodMode.ALL_TIME) {
                Spacer(Modifier.height(12.dp))
                Column(Modifier.fillMaxWidth().testTag("CategoryAnalyticsAllTimeSummary"),
                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.analytics_total_spending),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(String.format(Locale.US, "HK$ %,.2f", totalExpense),
                        modifier = Modifier.fillMaxWidth().testTag("CategoryAnalyticsTotal"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(stringResource(R.string.category_transactions_count, expenseTransactions.size),
                        modifier = Modifier.testTag("CategoryAnalyticsRecordCount"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else if (periodLabel.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Surface(shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        if (periodMode == CategoryAnalyticsPeriodMode.MONTH && onPreviousMonth != null) {
                            IconButton(onClick = onPreviousMonth,
                                modifier = Modifier.size(48.dp).testTag("CategoryAnalyticsPreviousMonth")) {
                                Icon(Icons.Default.ChevronLeft,
                                    stringResource(R.string.category_analytics_previous_month))
                            }
                        }
                        AnimatedContent(targetState = periodLabel,
                            modifier = Modifier.weight(1f),
                            transitionSpec = {
                                (slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it / 2 } + fadeIn())
                                    .togetherWith(slideOutVertically(spring(stiffness = Spring.StiffnessMediumLow)) { -it / 2 } + fadeOut())
                            }, label = "categoryAnalyticsPeriod") { label ->
                            Text(label, Modifier.fillMaxWidth().padding(vertical = 8.dp).testTag("CategoryAnalyticsPeriodLabel"),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                        if (periodMode == CategoryAnalyticsPeriodMode.MONTH && onNextMonth != null) {
                            IconButton(onClick = onNextMonth, enabled = canGoToNextMonth,
                                modifier = Modifier.size(48.dp).testTag("CategoryAnalyticsNextMonth")) {
                                Icon(Icons.Default.ChevronRight,
                                    stringResource(R.string.category_analytics_next_month))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            if (categoryTotals.isEmpty() || totalExpense == 0.0) {
                HistoryEmptyState(
                    message = emptyStateMessage ?: stringResource(R.string.category_analytics_no_expenses_period),
                    hint = emptyStateHint,
                    onAddTransaction = onAddTransaction,
                    onViewAllRecords = onViewAllRecords
                )
            } else {
                // Each actual segment has a stable 48dp tap slot. Dragging never filters.
                Box(Modifier.fillMaxWidth().testTag("CategoryAnalyticsBar")) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        categoryTotals.forEachIndexed { index, entry ->
                            val targetFraction = (entry.value / totalExpense).toFloat().coerceIn(0.01f, 1f)
                            val animatedFraction by animateFloatAsState(
                                targetValue = targetFraction,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                label = "segment_spring_$index"
                            )
                            val color = categoryColorMap[entry.key] ?: ChartColors.getCategoryColor(entry.key, isDark, primaryColor)
                            val isSegmentSelected = activeCategory != null && entry.key.equals(activeCategory, ignoreCase = true)
                            val isSegmentDimmed = activeCategory != null && !isSegmentSelected

                            val segmentHeight by animateDpAsState(
                                targetValue = when {
                                    isSegmentSelected -> 24.dp
                                    isSegmentDimmed -> 10.dp
                                    else -> 14.dp
                                },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                label = "segment_height_spring_$index"
                            )

                            val segmentAlpha by animateFloatAsState(
                                targetValue = if (isSegmentDimmed) 0.35f else 1.0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                label = "segment_alpha_spring_$index"
                            )

                            val segmentScale by animateFloatAsState(
                                targetValue = if (isSegmentSelected) 1.05f else 1.0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "segment_scale_spring_$index"
                            )

                            val categoryName = com.example.vibefinance.ui.home.getCategoryDisplayName(entry.key)
                            Box(
                                modifier = Modifier.weight(animatedFraction).height(48.dp)
                                    .testTag("CategoryAnalyticsSegment_${entry.key}")
                                    .semantics {
                                        contentDescription = categoryName
                                        selected = isSegmentSelected
                                    }
                                    .categorySelectionTap(enabled = onSelectCategory != null) {
                                        onSelectCategory?.invoke(
                                            if (entry.key.equals(selectedCategory, ignoreCase = true)) null else entry.key
                                        )
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(Modifier.fillMaxWidth().height(segmentHeight)
                                    .graphicsLayer { scaleX = segmentScale; scaleY = segmentScale }
                                    .clip(CircleShape).background(color.copy(alpha = segmentAlpha)))
                            }
                        }
                    }
                }

                // Interactive Category Popover / Detail Card
                AnimatedVisibility(
                    visible = activeCategory != null,
                    enter = expandVertically(spring(stiffness = Spring.StiffnessMediumLow)) +
                        fadeIn(spring(stiffness = Spring.StiffnessMediumLow)),
                    exit = shrinkVertically(spring(stiffness = Spring.StiffnessMediumLow)) +
                        fadeOut(spring(stiffness = Spring.StiffnessMediumLow))
                ) {
                    (activeCategory ?: lastVisibleCategory)?.let { cat ->
                        val catTotal = categoryTotals.find { it.key == cat }?.value ?: 0.0
                        val catCount = categoryCounts[cat] ?: 0
                        val catPct = if (totalExpense > 0) (catTotal / totalExpense * 100) else 0.0
                        val catColor = categoryColorMap[cat] ?: ChartColors.getCategoryColor(cat, isDark, primaryColor)

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f),
                            border = BorderStroke(1.dp, catColor.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(enabled = activeCategory != null) {
                                    val newCat = if (cat.equals(selectedCategory, ignoreCase = true)) null else cat
                                    onSelectCategory?.invoke(newCat)
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(catColor)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = com.example.vibefinance.ui.home.getCategoryDisplayName(cat),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = stringResource(R.string.category_transactions_count, catCount),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = String.format(Locale.US, "HK$ %,.2f", catTotal),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = stringResource(R.string.category_percentage_total, catPct),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (selectedCategory != null && cat.equals(selectedCategory, ignoreCase = true)) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Detailed Interactive Legend List with Spring Highlight Physics
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    categoryTotals.take(5).forEachIndexed { index, entry ->
                        val color = categoryColorMap[entry.key] ?: ChartColors.getCategoryColor(entry.key, isDark, primaryColor)
                        val percentage = if (totalExpense > 0) (entry.value / totalExpense * 100) else 0.0
                        val isSelected = activeCategory != null && entry.key.equals(activeCategory, ignoreCase = true)
                        val isDimmed = activeCategory != null && !isSelected

                        val rowBgColor by animateColorAsState(
                            targetValue = if (isSelected) color.copy(alpha = 0.18f)
                            else Color.Transparent,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "row_bg_spring_$index"
                        )

                        val rowAlpha by animateFloatAsState(
                            targetValue = if (isDimmed) 0.35f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "row_alpha_spring_$index"
                        )

                        val dotSize by animateDpAsState(
                            targetValue = if (isSelected) 14.dp else 10.dp,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "dot_size_spring_$index"
                        )

                        val horizontalPadding by animateDpAsState(
                            targetValue = if (isSelected) 12.dp else 8.dp,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "row_h_padding_spring_$index"
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(rowBgColor)
                                .testTag("CategoryAnalyticsLegend_${entry.key}")
                                .clickable {
                                    val newCat = if (isSelected) null else entry.key
                                    onSelectCategory?.invoke(newCat)
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                }
                                .padding(horizontal = horizontalPadding, vertical = 6.dp)
                                .alpha(rowAlpha),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(dotSize)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = com.example.vibefinance.ui.home.getCategoryDisplayName(entry.key),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = String.format(Locale.US, "%.1f%%", percentage),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = String.format(Locale.US, "HK$ %,.0f", entry.value),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
