@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.history

import com.example.vibefinance.ui.components.CompletePressButton
import com.example.vibefinance.ui.components.CompletePressTextButton
import com.example.vibefinance.ui.components.CompletePressToggleButton

import androidx.compose.ui.res.stringResource
import com.example.vibefinance.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.example.vibefinance.ui.components.ExpressiveSwitch
import com.example.vibefinance.ui.common.pressBounce
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.platform.LocalDensity
import com.example.vibefinance.ui.components.ExpressiveSwipeRow
import com.example.vibefinance.ui.home.CategoryIcon
import androidx.compose.ui.unit.min
import androidx.compose.runtime.derivedStateOf
import kotlin.math.abs
import kotlin.math.absoluteValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import android.widget.Toast
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.historyAmountFor
import com.example.vibefinance.data.entity.DefaultExpenseCategories
import com.example.vibefinance.data.entity.DefaultIncomeCategories
import com.example.vibefinance.theme.rememberIconShape
import com.example.vibefinance.ui.FinanceIntent
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.components.CategoryAnalyticsPeriodMode
import com.example.vibefinance.ui.components.rememberConnectedButtonColorMotion
import com.example.vibefinance.ui.components.ConnectedButtonRipple
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private fun AccountEntity.displayLabel(): String = nickname?.trim()?.takeIf { it.isNotEmpty() } ?: name

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    state: FinanceUiState,
    onIntent: (FinanceIntent) -> Unit,
    modifier: Modifier = Modifier,
    topContentPadding: Dp = 16.dp,
    accountFilterId: Long? = null,
    onClearAccountFilter: () -> Unit = {},
    categoryFilter: String? = null,
    onClearCategoryFilter: () -> Unit = {},
    onAddTransaction: (() -> Unit)? = null
) {
    var selectedCategoryFilter by remember(categoryFilter) { mutableStateOf<String?>(categoryFilter) }
    var periodFilterMode by remember { mutableStateOf(com.example.vibefinance.ui.components.PeriodFilterMode.ALL) }

    val strToday = stringResource(R.string.date_today)
    val strYesterday = stringResource(R.string.date_yesterday)
    val dateLocale = LocalConfiguration.current.locales[0]

    val budgetInfo = state.budgetInfo
    val context = LocalContext.current
    val filteredAccount = state.accounts.firstOrNull { it.id == accountFilterId }
    val zone = ZoneId.systemDefault()
    val activePeriodStart = budgetInfo?.startDate ?: 0L
    val activePeriodEnd = budgetInfo?.endDate ?: 0L
    val hasBudgetPeriod = activePeriodStart > 0L && activePeriodEnd >= activePeriodStart
    // A selected end date is stored at local midnight; include that entire calendar day.
    val activePeriodEndExclusive = if (hasBudgetPeriod) {
        Instant.ofEpochMilli(activePeriodEnd).atZone(zone).toLocalDate()
            .plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    } else 0L
    var analyticsPeriodMode by remember(activePeriodStart, activePeriodEnd) {
        mutableStateOf(
            if (categoryFilter != null) CategoryAnalyticsPeriodMode.ALL_TIME
            else if (hasBudgetPeriod) CategoryAnalyticsPeriodMode.BUDGET_PERIOD
            else CategoryAnalyticsPeriodMode.MONTH
        )
    }
    var analyticsMonth by remember { mutableStateOf(YearMonth.now(zone)) }
    val effectiveAnalyticsMode = if (!hasBudgetPeriod && analyticsPeriodMode == CategoryAnalyticsPeriodMode.BUDGET_PERIOD) {
        CategoryAnalyticsPeriodMode.MONTH
    } else analyticsPeriodMode
    val monthStart = analyticsMonth.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
    val nextMonthStart = analyticsMonth.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
    val analyticsStart = if (effectiveAnalyticsMode == CategoryAnalyticsPeriodMode.BUDGET_PERIOD) {
        activePeriodStart
    } else monthStart
    val analyticsEndExclusive = if (effectiveAnalyticsMode == CategoryAnalyticsPeriodMode.BUDGET_PERIOD) {
        activePeriodEndExclusive
    } else nextMonthStart
    val analyticsPeriodLabel = when (effectiveAnalyticsMode) {
        CategoryAnalyticsPeriodMode.ALL_TIME -> stringResource(R.string.category_analytics_all_time)
        CategoryAnalyticsPeriodMode.BUDGET_PERIOD -> {
            val datePattern = if (dateLocale.language.startsWith("zh")) "yyyy年M月d日" else "d MMM yyyy"
            val formatter = DateTimeFormatter.ofPattern(datePattern, dateLocale)
            val startDate = Instant.ofEpochMilli(activePeriodStart).atZone(zone).toLocalDate()
            val endDate = Instant.ofEpochMilli(activePeriodEnd).atZone(zone).toLocalDate()
            "${startDate.format(formatter)} – ${endDate.format(formatter)}"
        }
        CategoryAnalyticsPeriodMode.MONTH -> {
            val monthPattern = if (dateLocale.language.startsWith("zh")) "yyyy年M月" else "MMMM yyyy"
            analyticsMonth.atDay(1).format(DateTimeFormatter.ofPattern(monthPattern, dateLocale))
        }
    }

    LaunchedEffect(state.categoryMergeRules) {
        selectedCategoryFilter = selectedCategoryFilter?.let {
            state.categoryMergeRules.resolve(it, com.example.vibefinance.data.entity.CategoryKind.EXPENSE)
        }
    }
    LaunchedEffect(categoryFilter) {
        selectedCategoryFilter = categoryFilter?.let {
            state.categoryMergeRules.resolve(it, com.example.vibefinance.data.entity.CategoryKind.EXPENSE)
        }
        if (categoryFilter != null) analyticsPeriodMode = CategoryAnalyticsPeriodMode.ALL_TIME
    }
    LaunchedEffect(accountFilterId) {
        if (accountFilterId != null) {
            selectedCategoryFilter = null
            onClearCategoryFilter()
        }
        periodFilterMode = com.example.vibefinance.ui.components.PeriodFilterMode.ALL
    }
    LaunchedEffect(activePeriodStart, activePeriodEnd) {
        if (categoryFilter == null) {
            selectedCategoryFilter = null
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = com.example.vibefinance.util.FinancialDataImportEngine.getFileNameFromUri(context, uri)
            onIntent(FinanceIntent.AnalyzeImportFile(uri, fileName))
        }
    }

    val accountTransactions = remember(state.transactions, accountFilterId) {
        if (accountFilterId == null) state.transactions else state.transactions.filter {
            it.accountId == accountFilterId || it.toAccountId == accountFilterId
        }
    }

    val analyticsTransactions = remember(accountTransactions, effectiveAnalyticsMode, analyticsStart, analyticsEndExclusive) {
        if (effectiveAnalyticsMode == CategoryAnalyticsPeriodMode.ALL_TIME) accountTransactions
        else accountTransactions.filter { it.timestamp >= analyticsStart && it.timestamp < analyticsEndExclusive }
    }
    val accountExpenses = remember(accountTransactions) {
        accountTransactions.filter { it.amount > 0 && it.toAccountId == null && !it.isBalanceAdjustment }
    }
    val analyticsHasExpenses = remember(analyticsTransactions) {
        analyticsTransactions.any { it.amount > 0 && it.toAccountId == null && !it.isBalanceAdjustment }
    }
    val viewAllRecords: () -> Unit = {
        analyticsPeriodMode = CategoryAnalyticsPeriodMode.ALL_TIME
        periodFilterMode = com.example.vibefinance.ui.components.PeriodFilterMode.ALL
        selectedCategoryFilter = null
        onClearCategoryFilter()
    }

    val periodFilteredTransactions = remember(
        accountTransactions, periodFilterMode, activePeriodStart, activePeriodEndExclusive
    ) {
        if (hasBudgetPeriod) {
            when (periodFilterMode) {
                com.example.vibefinance.ui.components.PeriodFilterMode.ALL -> accountTransactions
                com.example.vibefinance.ui.components.PeriodFilterMode.ACTIVE_PERIOD -> accountTransactions.filter {
                    it.timestamp >= activePeriodStart && it.timestamp < activePeriodEndExclusive
                }
                com.example.vibefinance.ui.components.PeriodFilterMode.OTHER_PERIODS -> accountTransactions.filter {
                    it.timestamp < activePeriodStart || it.timestamp >= activePeriodEndExclusive
                }
            }
        } else {
            accountTransactions
        }
    }

    val filteredTransactions = remember(periodFilteredTransactions, analyticsTransactions, selectedCategoryFilter) {
        val catFilter = selectedCategoryFilter
        if (!catFilter.isNullOrBlank()) {
            // A selected chart category must retain its displayed scope, even when empty.
            analyticsTransactions.filter {
                it.toAccountId == null && !it.isBalanceAdjustment && it.amount > 0 &&
                    it.category.equals(catFilter, ignoreCase = true)
            }
        } else {
            periodFilteredTransactions
        }
    }

    val transactionsList = filteredTransactions

    // Group transactions by date: present & past transactions sorted descending (newest logged first at top), followed by future scheduled transactions
    val groupedTransactions = remember(transactionsList) {
        val now = System.currentTimeMillis()
        val presentAndPast = transactionsList.filter { it.timestamp <= now }.sortedByDescending { it.timestamp }
        val future = transactionsList.filter { it.timestamp > now }.sortedBy { it.timestamp }
        (presentAndPast + future).groupBy { tx ->
            LocalDateTime.ofInstant(Instant.ofEpochMilli(tx.timestamp), ZoneId.systemDefault()).toLocalDate()
        }
    }

    // Extract current period's spent vibe color dynamically based on budget spent ratio
    val ratioSpent = if (budgetInfo != null && budgetInfo.dailyAllowance > 0) {
        val spentToday = budgetInfo.dailyAllowance - budgetInfo.dailyRemaining
        spentToday / budgetInfo.dailyAllowance
    } else {
        0.0
    }

    val activeVibeColor = when {
        ratioSpent <= 0.5 -> MaterialTheme.colorScheme.primary
        ratioSpent <= 1.0 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.tertiary
    }

    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var viewingAdjustment by remember { mutableStateOf<TransactionEntity?>(null) }
    var editAmountText by remember { mutableStateOf("") }
    var editCategoryText by remember { mutableStateOf("") }
    var editDescriptionText by remember { mutableStateOf("") }
    var editCustomIcon by remember { mutableStateOf<String?>(null) }
    var editIsIncome by remember { mutableStateOf(false) }
    var editIsDailyBudget by remember { mutableStateOf(true) }

    // Predictive back for editing transaction modal
    androidx.activity.compose.PredictiveBackHandler(enabled = editingTransaction != null || viewingAdjustment != null) { progressFlow ->
        try {
            progressFlow.collect { }
            editingTransaction = null
            viewingAdjustment = null
        } catch (e: kotlinx.coroutines.CancellationException) {
        }
    }

    // Predictive back for active category filter
    androidx.activity.compose.PredictiveBackHandler(enabled = selectedCategoryFilter != null && editingTransaction == null && viewingAdjustment == null) { progressFlow ->
        try {
            progressFlow.collect { }
            selectedCategoryFilter = null
            onClearCategoryFilter()
        } catch (e: kotlinx.coroutines.CancellationException) {
        }
    }

    androidx.activity.compose.PredictiveBackHandler(enabled = accountFilterId != null && selectedCategoryFilter == null && editingTransaction == null && viewingAdjustment == null) { progressFlow ->
        try {
            progressFlow.collect { }
            onClearAccountFilter()
        } catch (e: kotlinx.coroutines.CancellationException) {
        }
    }


    AnimatedContent(
        targetState = state.isLoading,
        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
        label = "historySkeletonCrossfade"
    ) { isLoading ->
        if (isLoading) {
            com.example.vibefinance.ui.components.HistoryScreenSkeleton(modifier = modifier)
        } else {
            LazyColumn(
                modifier = modifier.fillMaxSize().testTag("HistoryList"),
                contentPadding = PaddingValues(top = topContentPadding),
                verticalArrangement = Arrangement.Top
            ) {
        if (accountFilterId != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(start = 16.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.account_history_filter, filteredAccount?.displayLabel() ?: "#${accountFilterId}"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        IconButton(onClick = onClearAccountFilter) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.show_all_history))
                        }
                    }
                }
            }
        }
        // --- 📅 ACTIVE BUDGET PERIOD INDICATOR & FILTER CARD ---
        if (accountFilterId == null) item {
            com.example.vibefinance.ui.components.BudgetPeriodIndicatorCard(
                budgetInfo = state.budgetInfo,
                selectedFilter = periodFilterMode,
                onSelectFilter = { mode -> periodFilterMode = mode },
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // --- 📊 CATEGORY BREAKDOWN & CSV EXPORT CARD ---
        item {
            val context = LocalContext.current
            com.example.vibefinance.ui.components.CategoryBreakdownCard(
                transactions = analyticsTransactions,
                onExportCsv = {
                    val csvContent = com.example.vibefinance.util.CsvExportEngine.generateCsvContent(
                        transactions = state.transactions,
                        accounts = state.accounts
                    )
                    com.example.vibefinance.util.CsvExportEngine.shareCsvFile(context, csvContent)
                },
                selectedCategory = selectedCategoryFilter,
                onSelectCategory = { cat ->
                    selectedCategoryFilter = cat
                    if (cat == null) onClearCategoryFilter()
                },
                periodMode = effectiveAnalyticsMode,
                periodLabel = analyticsPeriodLabel,
                hasBudgetPeriod = hasBudgetPeriod,
                onSelectPeriod = { mode ->
                    analyticsPeriodMode = mode
                    if (mode == CategoryAnalyticsPeriodMode.ALL_TIME) {
                        periodFilterMode = com.example.vibefinance.ui.components.PeriodFilterMode.ALL
                    }
                    selectedCategoryFilter = null
                    onClearCategoryFilter()
                },
                onPreviousMonth = {
                    analyticsMonth = analyticsMonth.minusMonths(1)
                    selectedCategoryFilter = null
                    onClearCategoryFilter()
                },
                onNextMonth = {
                    if (analyticsMonth.isBefore(YearMonth.now(zone))) {
                        analyticsMonth = analyticsMonth.plusMonths(1)
                        selectedCategoryFilter = null
                        onClearCategoryFilter()
                    }
                },
                canGoToNextMonth = analyticsMonth.isBefore(YearMonth.now(zone)),
                onImportData = {
                    filePickerLauncher.launch("*/*")
                },
                emptyStateMessage = if (accountTransactions.isEmpty()) stringResource(R.string.no_transactions) else null,
                emptyStateHint = if (accountTransactions.isEmpty()) stringResource(R.string.analytics_empty_hint) else null,
                onAddTransaction = onAddTransaction,
                onViewAllRecords = if (effectiveAnalyticsMode != CategoryAnalyticsPeriodMode.ALL_TIME &&
                    accountExpenses.isNotEmpty() && !analyticsHasExpenses) viewAllRecords else null,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }



        if (transactionsList.isEmpty() && analyticsHasExpenses) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().testTag("HistoryListEmptyCard"),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    com.example.vibefinance.ui.components.HistoryEmptyState(
                        message = stringResource(R.string.analytics_no_matching_records),
                        modifier = Modifier.padding(16.dp),
                        onAddTransaction = onAddTransaction,
                        onViewAllRecords = if (accountTransactions.isNotEmpty()) viewAllRecords else null
                    )
                }
            }
        } else {
            val groupedList = groupedTransactions.toList()
            groupedList.forEachIndexed { groupIndex, (localDate, txsForDate) ->
                // 1. Date Header Group (Muted, Clean Typography)
                val today = LocalDate.now()
                val isZh = dateLocale.language.startsWith("zh")
                val datePattern = if (isZh) "yyyy年M月d日" else "MMM dd, yyyy"
                val dateLabel = when (localDate) {
                    today -> strToday
                    today.minusDays(1) -> strYesterday
                    else -> localDate.format(DateTimeFormatter.ofPattern(datePattern, dateLocale))
                }
                val weekday = localDate.format(DateTimeFormatter.ofPattern("EEEE", dateLocale))
                val headerText = "$dateLabel · $weekday"

                item {
                    val bInfo = state.budgetInfo
                    val isInActivePeriod = hasBudgetPeriod && txsForDate.any { tx ->
                        tx.timestamp >= activePeriodStart && tx.timestamp < activePeriodEndExclusive
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = headerText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                            fontWeight = FontWeight.Bold
                        )

                        if (bInfo != null && bInfo.startDate > 0L && bInfo.endDate > 0L) {
                            Surface(
                                shape = CircleShape,
                                color = if (isInActivePeriod) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = if (isInActivePeriod) stringResource(R.string.badge_active_period) else stringResource(R.string.badge_past_period),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isInActivePeriod) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // 2. Transaction Item Flat list (M3 grouped list items)
                itemsIndexed(items = txsForDate, key = { _, tx -> tx.id }) { index, tx ->
                    val sourceAccount = state.accounts.find { it.id == tx.accountId }
                    val destAccount = tx.toAccountId?.let { toId -> state.accounts.find { it.id == toId } }
                    val isFuture = tx.timestamp > System.currentTimeMillis()
                    val isTransfer = tx.toAccountId != null
                    val isInstallment = tx.installmentNumber != null
                    val displayDelta = tx.historyAmountFor(accountFilterId)
                    val isIncome = displayDelta > 0

                    val date = LocalDateTime.ofInstant(Instant.ofEpochMilli(tx.timestamp), ZoneId.systemDefault())
                    val timeText = date.format(DateTimeFormatter.ofPattern("HH:mm", dateLocale))

                    val transitionState = remember { MutableTransitionState(true) }

                    LaunchedEffect(transitionState.currentState, transitionState.targetState) {
                        if (!transitionState.currentState && !transitionState.targetState) {
                            onIntent(FinanceIntent.DeleteTransaction(tx))
                        }
                    }

                    val isFirst = index == 0
                    val isLast = index == txsForDate.size - 1

                    AnimatedVisibility(
                        visibleState = transitionState,
                        exit = shrinkVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) + fadeOut(animationSpec = tween(150)),
                        modifier = Modifier.animateItem(
                            placementSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            val itemShape = RoundedCornerShape(
                                topStart = if (isFirst) 26.dp else 8.dp,
                                topEnd = if (isFirst) 26.dp else 8.dp,
                                bottomStart = if (isLast) 26.dp else 8.dp,
                                bottomEnd = if (isLast) 26.dp else 8.dp
                            )
                            ExpressiveSwipeRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("TransactionRow_${tx.id}"),
                                shape = itemShape,
                                onEdit = {
                                    if (tx.isBalanceAdjustment) {
                                        viewingAdjustment = tx
                                    } else {
                                        editingTransaction = tx
                                        editAmountText = String.format(Locale.US, "%.2f", Math.abs(tx.amount))
                                        editCategoryText = tx.category
                                        editDescriptionText = tx.description
                                        editCustomIcon = tx.customIcon
                                        editIsIncome = tx.amount < 0
                                        editIsDailyBudget = tx.amount < 0 || !tx.isExcludedFromDailyBudget
                                    }
                                },
                                onDelete = { transitionState.targetState = false }
                            ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        // 1. Leading Avatar Visual Container (M3 Expressive)
                                        Surface(
                                            modifier = Modifier.size(44.dp),
                                            shape = rememberIconShape("history.${tx.id}"),
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (tx.isBalanceAdjustment) {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                } else {
                                                    com.example.vibefinance.ui.components.ExpenseEventIcon(
                                                        customIcon = tx.customIcon.takeIf { tx.amount > 0 && tx.toAccountId == null },
                                                        category = tx.category,
                                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }
                                            }
                                        }

                                        // 2. Center Content: Headline (Title) & Supporting Metadata (Subtitles)
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            val titleText = when {
                                                tx.isBalanceAdjustment -> stringResource(R.string.balance_adjustment_title)
                                                tx.description.isNotBlank() -> tx.description
                                                else -> com.example.vibefinance.ui.home.getCategoryDisplayName(tx.category)
                                            }
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = titleText,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f, fill = false)
                                                )
                                                if (tx.isExcludedFromDailyBudget && !tx.isBalanceAdjustment && tx.toAccountId == null && !isIncome) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                                                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                                                    ) {
                                                        Text(
                                                            text = stringResource(R.string.non_daily_expense_badge),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp),
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    }
                                                }
                                            }

                                            val cardName = sourceAccount?.displayLabel() ?: stringResource(R.string.acc_type_cash)
                                            val categoryName = com.example.vibefinance.ui.home.getCategoryDisplayName(tx.category)
                                            val cardSubtitle = when {
                                                tx.isBalanceAdjustment -> cardName
                                                isTransfer -> "${sourceAccount?.displayLabel() ?: stringResource(R.string.ah_fallback_account)} ➔ ${destAccount?.displayLabel() ?: stringResource(R.string.ah_fallback_account)}"
                                                isInstallment -> "$categoryName • $cardName (${tx.installmentNumber}/${tx.totalInstallments})"
                                                else -> "$categoryName • $cardName"
                                            }
                                            Text(
                                                text = cardSubtitle,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                        }

                                        // 3. Trailing Content: Metric (Amount) & Secondary Metadata (Timestamp/Delete)
                                        Column(
                                            horizontalAlignment = Alignment.End,
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            val amtString = String.format(Locale.US, if (isIncome) "+HK$%,.2f" else "-HK$%,.2f", Math.abs(displayDelta))
                                            val amountColor = when {
                                                isTransfer -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                isIncome -> MaterialTheme.colorScheme.secondary
                                                isFuture -> activeVibeColor.copy(alpha = 0.5f)
                                                else -> activeVibeColor
                                            }

                                            Text(
                                                text = amtString,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = amountColor
                                            )

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = timeText,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                )
                                                IconButton(
                                                    onClick = { transitionState.targetState = false },
                                                    modifier = Modifier.size(20.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = stringResource(R.string.btn_delete),
                                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                            }
                            if (!isLast) {
                                Spacer(modifier = Modifier.height(5.dp))
                            }
                        }
                    }
                }

                // Transfers only move money between accounts, so they never contribute to
                // a day's income-minus-spending total. Balance corrections remain visible
                // in an account's own history, where they change that account's balance.
                val dailySummaryTransactions = txsForDate.filter { tx ->
                    tx.toAccountId == null && (accountFilterId != null || !tx.isBalanceAdjustment)
                }
                val dailyNet = dailySummaryTransactions.sumOf { tx ->
                    tx.historyAmountFor(accountFilterId)
                }
                if (dailySummaryTransactions.isNotEmpty()) item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = stringResource(
                                R.string.daily_net_format,
                                when {
                                    dailyNet > 0.0 -> "+"
                                    dailyNet < 0.0 -> "-"
                                    else -> ""
                                },
                                kotlin.math.abs(dailyNet)
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = activeVibeColor.copy(alpha = 0.8f),
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }
                }

                // 4. Day Divider (if this is not the last group/day)
                if (groupIndex < groupedList.size - 1) {
                    item {
                        androidx.compose.material3.HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            thickness = 1.dp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
    }
    }

    // Real Transaction Edit Dialog with Container Transform Morphing
    editingTransaction?.let { tx ->
        val canChangeType = tx.toAccountId == null && !tx.isBalanceAdjustment
        val editedMagnitude = editAmountText.toDoubleOrNull()?.takeIf { it.isFinite() && it > 0.0 }
        val dialogScale by animateFloatAsState(
            targetValue = 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "dialogContainerTransform"
        )

        AlertDialog(
            onDismissRequest = { editingTransaction = null },
            title = {
                Text(
                    text = stringResource(R.string.edit_transaction_title),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (canChangeType) {
                        Text(
                            text = stringResource(R.string.edit_transaction_type_label),
                            style = MaterialTheme.typography.labelLarge
                        )
                        HistoryTransactionTypeSelector(
                            isIncome = editIsIncome,
                            onTypeSelected = { editIsIncome = it }
                        )
                    }
                    OutlinedTextField(
                        value = editAmountText,
                        onValueChange = { editAmountText = it },
                        label = { Text(stringResource(R.string.edit_amount_label) + " (HK$)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("EditTransactionAmount")
                    )

                    if (canChangeType) {
                        HistoryCategoryDropdown(
                            category = editCategoryText,
                            isIncome = editIsIncome,
                            transactions = state.transactions,
                            categoryMergeRules = state.categoryMergeRules,
                            onCategorySelected = { editCategoryText = it }
                        )
                    } else {
                        OutlinedTextField(
                            value = com.example.vibefinance.ui.home.getCategoryDisplayName(editCategoryText),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.edit_category_label)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("EditTransactionCategory")
                        )
                    }

                    if (canChangeType && !editIsIncome) {
                        com.example.vibefinance.ui.components.ExpenseIconChoice(
                            value = editCustomIcon, category = editCategoryText, onValueChange = { editCustomIcon = it }
                        )
                    }

                    OutlinedTextField(
                        value = editDescriptionText,
                        onValueChange = { editDescriptionText = it },
                        label = { Text(stringResource(R.string.edit_description_label)) },
                        modifier = Modifier.fillMaxWidth().testTag("EditTransactionDescription")
                    )

                    // Daily Budget Toggle for Expenses
                    if (canChangeType && !editIsIncome) {
                        val editHaptic = LocalHapticFeedback.current
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    editIsDailyBudget = !editIsDailyBudget
                                    editHaptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.daily_budget_toggle_title),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (editIsDailyBudget) {
                                            stringResource(R.string.daily_budget_toggle_desc_on)
                                        } else {
                                            stringResource(R.string.daily_budget_toggle_desc_off)
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                ExpressiveSwitch(
                                    checked = editIsDailyBudget,
                                    onCheckedChange = { editIsDailyBudget = it },
                                    modifier = Modifier.testTag("EditTransactionDailyBudget")
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                val confirmInteraction = remember { MutableInteractionSource() }
                val haptic = LocalHapticFeedback.current
                CompletePressButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        val amount = editedMagnitude
                        if (amount != null) {
                            val newTx = tx.withHistoryEdit(
                                magnitude = amount,
                                isIncome = editIsIncome,
                                category = editCategoryText,
                                description = editDescriptionText,
                                countsTowardDailyBudget = editIsDailyBudget,
                                customIcon = editCustomIcon
                            )
                            onIntent(FinanceIntent.EditTransaction(tx, newTx))
                            editingTransaction = null
                        }
                    },
                    enabled = editedMagnitude != null,
                    shapes = ButtonDefaults.shapes(shape = CircleShape, pressedShape = RoundedCornerShape(percent = 32)),
                    interactionSource = confirmInteraction,
                    modifier = Modifier
                        .testTag("EditTransactionSave")
                        .pressBounce(interactionSource = confirmInteraction)
                ) {
                    Text(stringResource(R.string.btn_save))
                }
            },
            dismissButton = {
                val dismissInteraction = remember { MutableInteractionSource() }
                val haptic = LocalHapticFeedback.current
                CompletePressTextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        editingTransaction = null
                    },
                    shapes = ButtonDefaults.shapes(shape = CircleShape, pressedShape = RoundedCornerShape(percent = 32)),
                    interactionSource = dismissInteraction,
                    modifier = Modifier.pressBounce(interactionSource = dismissInteraction)
                ) {
                    Text(stringResource(R.string.btn_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier
                .graphicsLayer {
                    scaleX = dialogScale
                    scaleY = dialogScale
                }
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(32.dp)
                )
        )
    }

    viewingAdjustment?.let { tx ->
        val accountName = state.accounts.firstOrNull { it.id == tx.accountId }?.displayLabel() ?: "#${tx.accountId}"
        val delta = tx.balanceAdjustmentDelta ?: 0.0
        val signedAmount = String.format(Locale.US, "%sHK$%,.2f", if (delta >= 0) "+" else "-", abs(delta))
        AlertDialog(
            onDismissRequest = { viewingAdjustment = null },
            title = { Text(stringResource(R.string.balance_adjustment_title)) },
            text = { Text(stringResource(R.string.balance_adjustment_detail, accountName, signedAmount)) },
            confirmButton = {
                TextButton(onClick = { viewingAdjustment = null }) {
                    Text(stringResource(R.string.btn_close))
                }
            }
        )
    }
}

@Composable
private fun HistoryTransactionTypeSelector(
    isIncome: Boolean,
    onTypeSelected: (Boolean) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup()
            .testTag("EditTransactionTypeGroup"),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
    ) {
        listOf(false, true).forEachIndexed { index, incomeOption ->
            val isSelected = isIncome == incomeOption
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val colorMotion = rememberConnectedButtonColorMotion(
                isSelected = isSelected,
                isPressed = isPressed,
                backdropColor = colors.surfaceContainerHigh
            )
            ConnectedButtonRipple {
                CompletePressToggleButton(
                    checked = isSelected,
                    onCheckedChange = {
                        onTypeSelected(incomeOption)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    shapes = if (index == 0) {
                        ButtonGroupDefaults.connectedLeadingButtonShapes()
                    } else {
                        ButtonGroupDefaults.connectedTrailingButtonShapes()
                    },
                    interactionSource = interactionSource,
                    colors = ToggleButtonDefaults.toggleButtonColors(
                        containerColor = colorMotion.containerColor,
                        checkedContainerColor = colorMotion.containerColor,
                        contentColor = colorMotion.contentColor,
                        checkedContentColor = colorMotion.contentColor
                    ),
                    elevation = null,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag(if (incomeOption) "EditTransactionIncome" else "EditTransactionExpense")
                        .semantics {
                            selected = isSelected
                            role = Role.RadioButton
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .then(colorMotion.contentModifier)
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                            Text(
                                text = stringResource(if (incomeOption) R.string.filter_income else R.string.filter_expense),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryCategoryDropdown(
    category: String,
    isIncome: Boolean,
    transactions: List<TransactionEntity>,
    categoryMergeRules: com.example.vibefinance.data.entity.CategoryMergeRules,
    onCategorySelected: (String) -> Unit
) {
    var expanded by remember(isIncome) { mutableStateOf(false) }
    val kind = if (isIncome) com.example.vibefinance.data.entity.CategoryKind.INCOME else com.example.vibefinance.data.entity.CategoryKind.EXPENSE
    val defaultCategories = (if (isIncome) DefaultIncomeCategories else DefaultExpenseCategories)
        .map { categoryMergeRules.resolve(it, kind) }.distinct()
    val categoryFrequency = remember(transactions, isIncome) {
        transactions.filter {
            it.toAccountId == null && !it.isBalanceAdjustment && (it.amount < 0.0) == isIncome
        }.map { it.category }.filter { it.isNotBlank() }.groupingBy { it }.eachCount()
    }
    val defaults = remember(defaultCategories, categoryFrequency) {
        defaultCategories.sortedByDescending { categoryFrequency[it] ?: 0 }
    }
    val existing = remember(defaultCategories, categoryFrequency) {
        categoryFrequency.keys.filter { it !in defaultCategories }
            .sortedWith(compareByDescending<String> { categoryFrequency[it] ?: 0 }.thenBy { it })
    }
    val legacyCategory = category.takeIf { it.isNotBlank() && it !in defaults && it !in existing }
    val sections = buildList {
        legacyCategory?.let { add(R.string.edit_category_current to listOf(it)) }
        add(R.string.edit_category_defaults to defaults)
        if (existing.isNotEmpty()) add(R.string.edit_category_existing to existing)
    }
    val haptic = LocalHapticFeedback.current

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = com.example.vibefinance.ui.home.getCategoryDisplayName(category),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.edit_category_label)) },
            leadingIcon = { CategoryIcon(category, MaterialTheme.colorScheme.primary, Modifier.size(22.dp)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
                .testTag("EditTransactionCategory")
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 320.dp).testTag("EditTransactionCategoryMenu")
        ) {
            sections.forEachIndexed { sectionIndex, (title, categories) ->
                if (sectionIndex > 0) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }
                Text(
                    text = stringResource(title),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .semantics { heading() }
                )
                categories.forEach { choice ->
                    val isSelected = choice == category
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = com.example.vibefinance.ui.home.getCategoryDisplayName(choice),
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            CategoryIcon(
                                choice,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                Modifier.size(22.dp)
                            )
                        },
                        trailingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                        } else null,
                        onClick = {
                            onCategorySelected(choice)
                            expanded = false
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("EditTransactionCategoryOption_$choice")
                            .semantics { selected = isSelected }
                    )
                }
            }
        }
    }
}

/** Expense/income edits retain the magnitude and transfer metadata; adjustments are read only. */
internal fun TransactionEntity.withHistoryEdit(
    magnitude: Double,
    isIncome: Boolean,
    category: String,
    description: String,
    countsTowardDailyBudget: Boolean,
    customIcon: String? = this.customIcon
): TransactionEntity {
    require(magnitude.isFinite() && magnitude > 0.0) { "Amount must be positive and finite" }
    require(!isBalanceAdjustment) { "Balance adjustments are read only" }
    val canChangeType = toAccountId == null
    val targetIsIncome = if (canChangeType) isIncome else amount < 0.0
    return copy(
        amount = if (targetIsIncome) -magnitude else magnitude,
        category = category,
        description = description,
        customIcon = com.example.vibefinance.data.entity.ExpenseIcon.normalize(customIcon),
        isExcludedFromDailyBudget = if (canChangeType) isIncome || !countsTowardDailyBudget else isExcludedFromDailyBudget
    )
}

private fun combineColors(color1: Color, color2: Color, weight: Float): Color {
    return Color(
        red = color1.red * weight + color2.red * (1f - weight),
        green = color1.green * weight + color2.green * (1f - weight),
        blue = color1.blue * weight + color2.blue * (1f - weight),
        alpha = color1.alpha * weight + color2.alpha * (1f - weight)
    )
}
