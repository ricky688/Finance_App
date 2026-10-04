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
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.luminance
import com.example.vibefinance.theme.ChartColors
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.TransactionEntity
import java.util.Locale

enum class CategoryAnalyticsPeriodMode { MONTH, BUDGET_PERIOD }

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
    canGoToNextMonth: Boolean = true
) {
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

    // Interactive scrub state
    var scrubbedCategory by remember { mutableStateOf<String?>(null) }
    var isScrubbing by remember { mutableStateOf(false) }
    val activeCategory = scrubbedCategory ?: selectedCategory
    var lastVisibleCategory by remember { mutableStateOf<String?>(null) }
    SideEffect {
        if (activeCategory != null) lastVisibleCategory = activeCategory
    }

    // Calculate cumulative segment fraction thresholds for scrubbing
    var barWidthPx by remember { mutableFloatStateOf(0f) }

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
                .padding(18.dp)
        ) {
            // Header Row: Category Analytics & CSV Export Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.category_analytics_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            AnimatedVisibility(
                                visible = selectedCategory != null,
                                enter = expandHorizontally(spring(stiffness = Spring.StiffnessMediumLow)) +
                                    fadeIn(spring(stiffness = Spring.StiffnessMediumLow)),
                                exit = shrinkHorizontally(spring(stiffness = Spring.StiffnessMediumLow)) +
                                    fadeOut(spring(stiffness = Spring.StiffnessMediumLow))
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .clickable(enabled = selectedCategory != null) {
                                                onSelectCategory?.invoke(null)
                                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                            }
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = stringResource(R.string.reset_filter),
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        AnimatedContent(
                            targetState = selectedCategory,
                            transitionSpec = {
                                (slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it / 2 } +
                                    fadeIn(spring(stiffness = Spring.StiffnessMediumLow)))
                                    .togetherWith(
                                        slideOutVertically(spring(stiffness = Spring.StiffnessMediumLow)) { -it / 2 } +
                                            fadeOut(spring(stiffness = Spring.StiffnessMediumLow))
                                    )
                            },
                            label = "category_filter_hint"
                        ) { category ->
                            val localizedCategory = category?.let {
                                com.example.vibefinance.ui.home.getCategoryDisplayName(it)
                            }
                            Text(
                                text = if (localizedCategory != null) stringResource(R.string.category_analytics_filtering, localizedCategory)
                                       else stringResource(R.string.category_tap_to_filter),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = if (category != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onImportData != null) {
                        FilledTonalButton(
                            onClick = onImportData,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = stringResource(R.string.import_title),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.import_title),
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = onExportCsv,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = stringResource(R.string.loc_export_csv),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "CSV",
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (onSelectPeriod != null && hasBudgetPeriod) {
                Spacer(modifier = Modifier.height(12.dp))
                val modes = CategoryAnalyticsPeriodMode.entries
                ExpressiveConnectedButtonGroup(
                    items = modes,
                    selectedIndex = modes.indexOf(periodMode),
                    onItemSelected = { index -> onSelectPeriod(modes[index]) },
                    modifier = Modifier.widthIn(max = 320.dp).align(Alignment.CenterHorizontally),
                    lightModeFocusMotion = true,
                    compact = true,
                    focusBackdropColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        .compositeOver(MaterialTheme.colorScheme.background),
                    labelProvider = { mode ->
                        stringResource(
                            if (mode == CategoryAnalyticsPeriodMode.MONTH) {
                                R.string.category_analytics_month
                            } else {
                                R.string.category_analytics_budget_period
                            }
                        )
                    }
                )
            }

            if (periodLabel.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (periodMode == CategoryAnalyticsPeriodMode.MONTH && onPreviousMonth != null) {
                            IconButton(onClick = onPreviousMonth, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = stringResource(R.string.category_analytics_previous_month)
                                )
                            }
                        }
                        AnimatedContent(
                            targetState = periodLabel,
                            modifier = Modifier.weight(1f),
                            transitionSpec = {
                                (slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it / 2 } +
                                    fadeIn(spring(stiffness = Spring.StiffnessMediumLow)))
                                    .togetherWith(
                                        slideOutVertically(spring(stiffness = Spring.StiffnessMediumLow)) { -it / 2 } +
                                            fadeOut(spring(stiffness = Spring.StiffnessMediumLow))
                                    )
                            },
                            label = "categoryAnalyticsPeriod"
                        ) { label ->
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        if (periodMode == CategoryAnalyticsPeriodMode.MONTH && onNextMonth != null) {
                            IconButton(
                                onClick = onNextMonth,
                                enabled = canGoToNextMonth,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = stringResource(R.string.category_analytics_next_month)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (categoryTotals.isEmpty() || totalExpense == 0.0) {
                Text(
                    text = stringResource(R.string.category_analytics_no_expenses_period),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                // Interactive Stacked Multi-Segment Progress Bar with Touch & Drag Scrubbing
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { barWidthPx = it.size.width.toFloat() }
                        .pointerInput(categoryTotals, totalExpense) {
                            detectTapGestures(
                                onTap = { offset ->
                                    if (barWidthPx > 0 && totalExpense > 0) {
                                        val touchRatio = (offset.x / barWidthPx).coerceIn(0f, 1f)
                                        var cum = 0.0
                                        var hitCat: String? = null
                                        for (entry in categoryTotals) {
                                            cum += (entry.value / totalExpense)
                                            if (touchRatio <= cum) {
                                                hitCat = entry.key
                                                break
                                            }
                                        }
                                        if (hitCat != null) {
                                            val newCat = if (hitCat.equals(selectedCategory, ignoreCase = true)) null else hitCat
                                            onSelectCategory?.invoke(newCat)
                                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                        }
                                    }
                                }
                            )
                        }
                        .pointerInput(categoryTotals, totalExpense) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    isScrubbing = true
                                    if (barWidthPx > 0 && totalExpense > 0) {
                                        val touchRatio = (offset.x / barWidthPx).coerceIn(0f, 1f)
                                        var cum = 0.0
                                        for (entry in categoryTotals) {
                                            cum += (entry.value / totalExpense)
                                            if (touchRatio <= cum) {
                                                scrubbedCategory = entry.key
                                                break
                                            }
                                        }
                                    }
                                },
                                onDragEnd = {
                                    isScrubbing = false
                                    if (scrubbedCategory != null) {
                                        onSelectCategory?.invoke(scrubbedCategory)
                                    }
                                    scrubbedCategory = null
                                },
                                onDragCancel = {
                                    isScrubbing = false
                                    scrubbedCategory = null
                                },
                                onDrag = { change, _ ->
                                    if (barWidthPx > 0 && totalExpense > 0) {
                                        val touchRatio = (change.position.x / barWidthPx).coerceIn(0f, 1f)
                                        var cum = 0.0
                                        var hitCat: String? = null
                                        for (entry in categoryTotals) {
                                            cum += (entry.value / totalExpense)
                                            if (touchRatio <= cum) {
                                                hitCat = entry.key
                                                break
                                            }
                                        }
                                        if (hitCat != null && hitCat != scrubbedCategory) {
                                            scrubbedCategory = hitCat
                                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                        }
                                    }
                                }
                            )
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp),
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

                            Box(
                                modifier = Modifier
                                    .weight(animatedFraction)
                                    .height(segmentHeight)
                                    .graphicsLayer {
                                        scaleX = segmentScale
                                        scaleY = segmentScale
                                    }
                                    .clip(CircleShape)
                                    .background(color.copy(alpha = segmentAlpha))
                            )
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
                                .padding(top = 10.dp)
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
                                            text = String.format(Locale.US, "%.1f%% of total", catPct),
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

                Spacer(modifier = Modifier.height(14.dp))

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
                                .clickable {
                                    val newCat = if (isSelected && selectedCategory != null) null else entry.key
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
