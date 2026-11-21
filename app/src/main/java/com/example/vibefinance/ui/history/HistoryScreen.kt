@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.history

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.layout
import kotlin.math.roundToInt
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import com.example.vibefinance.ui.common.ContentEntranceViewport
import com.example.vibefinance.ui.common.PageContentEntrance

import com.example.vibefinance.ui.components.CompletePressButton
import com.example.vibefinance.ui.components.CompletePressTextButton
import com.example.vibefinance.ui.components.CompletePressToggleButton

import androidx.compose.ui.res.stringResource
import com.example.vibefinance.R
import com.example.vibefinance.ui.motion.sharedTransitionBounds
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.snap
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.material3.LoadingIndicator
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.selected
import com.example.vibefinance.ui.common.verticalFadingEdge
import com.example.vibefinance.ui.preferences.LocalExperience
import com.example.vibefinance.ui.preferences.MotionLevel
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
import com.example.vibefinance.util.rememberVibeHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.example.vibefinance.ui.components.AppSwitch
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
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.nonInteractiveScrollbar
import androidx.compose.foundation.gestures.Orientation
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
import com.example.vibefinance.ui.preferences.PrivacyText as Text
import androidx.compose.material3.Checkbox
import androidx.compose.foundation.layout.ime
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.foundation.selection.toggleable
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

/** Detail-pane key of the large-screen add-transaction editor (never a real transaction id). */
private const val ADD_TRANSACTION_PANE_ID = -1L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    state: FinanceUiState,
    onIntent: (FinanceIntent) -> Unit,
    modifier: Modifier = Modifier,
    topContentPadding: Dp = 16.dp,
    topVisibilityInset: Dp = 0.dp,
    bottomVisibilityInset: Dp = 0.dp,
    accountFilterId: Long? = null,
    onClearAccountFilter: () -> Unit = {},
    categoryFilter: String? = null,
    onClearCategoryFilter: () -> Unit = {},
    onAddTransaction: (() -> Unit)? = null,
    batchResult: com.example.vibefinance.ui.BatchTransactionResult? = null,
    listState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState(),
    bottomVisibilityInsetProvider: () -> Dp = { bottomVisibilityInset },
    editResult: (() -> com.example.vibefinance.ui.BatchTransactionResult?)? = null,
    /** Large screens: the add-transaction editor, shown in the detail pane while non-null. */
    addPane: (@Composable () -> Unit)? = null,
    onDismissAddPane: () -> Unit = {},
    horizontalContentPadding: Dp = 0.dp,
) {
    var multiSelect by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    val motion = LocalExperience.current.motion
    val effectsSpec = if (motion == MotionLevel.MINIMAL) snap<Float>() else MaterialTheme.motionScheme.fastEffectsSpec<Float>()
    val spatialSpec = MaterialTheme.motionScheme.fastSpatialSpec<androidx.compose.ui.unit.IntSize>()
    val selectionEnter = when (motion) {
        MotionLevel.FULL -> expandVertically(spatialSpec, expandFrom = Alignment.Top, clip = false) + fadeIn(effectsSpec)
        MotionLevel.REDUCED -> fadeIn(effectsSpec)
        MotionLevel.MINIMAL -> EnterTransition.None
    }
    val selectionExit = when (motion) {
        MotionLevel.FULL -> shrinkVertically(spatialSpec, shrinkTowards = Alignment.Top, clip = false) + fadeOut(effectsSpec)
        MotionLevel.REDUCED -> fadeOut(effectsSpec)
        MotionLevel.MINIMAL -> ExitTransition.None
    }
    var selectionArray by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(longArrayOf()) }
    val selectedIds = remember(selectionArray) { selectionArray.toSet() }
    var batchPicker by remember { mutableStateOf<String?>(null) }
    var batchSnapshot by remember { mutableStateOf<List<TransactionEntity>>(emptyList()) }
    var batchRequestId by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf<String?>(null) }
    val batchBusy = batchRequestId != null && (batchResult?.requestId != batchRequestId || batchResult?.processing == true)
    val batchError = batchResult?.takeIf { it.requestId == batchRequestId }?.error
    val view = androidx.compose.ui.platform.LocalView.current
    val toggleSelected: (Long) -> Unit = { id ->
        if (!batchBusy) {
            selectionArray = (if (id in selectedIds) selectedIds - id else selectedIds + id).toLongArray()
            com.example.vibefinance.util.VibeHaptics.selection(view)
        }
    }
    LaunchedEffect(batchResult) {
        if (batchRequestId != null && batchResult?.requestId == batchRequestId && batchResult?.success == true) {
            multiSelect = false; selectionArray = longArrayOf(); batchPicker = null; batchRequestId = null
        }
    }
    var showSearch by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    var showFilters by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    val searchVisibility = remember { MutableTransitionState(showSearch) }
    searchVisibility.targetState = showSearch
    val historyListState = listState
    val selectionEdge by animateDpAsState(
        if (multiSelect && historyListState.canScrollBackward) 16.dp else 0.dp,
        animationSpec = if (motion == MotionLevel.MINIMAL) snap() else MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "historySelectionEdge")
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val keyboard = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    LaunchedEffect(showSearch) {
        if (showSearch && historyListState.firstVisibleItemIndex > 0) historyListState.animateScrollToItem(0)
    }
    // Enter-always collapse for the pinned search header, applied in layout (no recomposition).
    val searchHeaderOffset = remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    val searchHeaderHeight = remember { IntArray(1) }
    val searchHeaderScroll = remember {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: androidx.compose.ui.input.nestedscroll.NestedScrollSource): Offset {
                val current = searchHeaderOffset.floatValue
                val next = (current + available.y).coerceIn(-searchHeaderHeight[0].toFloat(), 0f)
                searchHeaderOffset.floatValue = next
                return Offset(0f, next - current)
            }
        }
    }
    val toggleSearch: () -> Unit = {
        if (showSearch) { focusManager.clearFocus(); keyboard?.hide() }
        showSearch = !showSearch
    }
    var searchText by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }
    var searchAccount by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf<Long?>(null) }
    var searchCategory by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf<String?>(null) }
    var searchKind by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(HistoryKind.ALL) }
    var searchFrom by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }
    var searchThrough by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }
    var searchMinimum by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }
    var searchMaximum by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }
    val searchQuery = HistoryQuery(searchText, searchAccount, searchCategory, searchKind, searchFrom, searchThrough, searchMinimum, searchMaximum)
    var selectedCategoryFilter by remember(categoryFilter) { mutableStateOf<String?>(categoryFilter) }
    var periodFilterMode by remember { mutableStateOf(com.example.vibefinance.ui.components.PeriodFilterMode.ALL) }

    val strToday = stringResource(R.string.date_today)
    val strYesterday = stringResource(R.string.date_yesterday)
    val dateLocale = LocalConfiguration.current.locales[0]
    val isZh = dateLocale.language.startsWith("zh")
    val datePattern = if (isZh) "yyyy年M月d日" else "MMM dd, yyyy"
    val dateHeaderFormatter = remember(datePattern, dateLocale) { DateTimeFormatter.ofPattern(datePattern, dateLocale) }
    val weekdayFormatter = remember(dateLocale) { DateTimeFormatter.ofPattern("EEEE", dateLocale) }
    val timeFormatter = remember(dateLocale) { DateTimeFormatter.ofPattern("HH:mm", dateLocale) }
    val monthPattern = if (isZh) "yyyy年M月" else "MMMM yyyy"
    val monthFormatter = remember(monthPattern, dateLocale) { DateTimeFormatter.ofPattern(monthPattern, dateLocale) }
    val budgetPeriodDatePattern = if (isZh) "yyyy年M月d日" else "d MMM yyyy"
    val budgetPeriodFormatter = remember(budgetPeriodDatePattern, dateLocale) { DateTimeFormatter.ofPattern(budgetPeriodDatePattern, dateLocale) }
    val accountsMap = remember(state.accounts) { state.accounts.associateBy { it.id } }
    val strBalanceAdjustment = stringResource(R.string.balance_adjustment_title)
    val strNonDailyExpense = stringResource(R.string.non_daily_expense_badge)
    val strFallbackCash = stringResource(R.string.acc_type_cash)
    val strFallbackAccount = stringResource(R.string.ah_fallback_account)
    val strDelete = stringResource(R.string.btn_delete)

    val budgetInfo = state.budgetInfo
    val context = LocalContext.current
    val filteredAccount = state.accounts.firstOrNull { it.id == accountFilterId }
    val zone = remember { ZoneId.systemDefault() }
    val activePeriodStart = budgetInfo?.startDate ?: 0L
    val activePeriodEnd = budgetInfo?.endDate ?: 0L
    val hasBudgetPeriod = activePeriodStart > 0L && activePeriodEnd >= activePeriodStart
    // A selected end date is stored at local midnight; include that entire calendar day.
    val activePeriodEndExclusive = if (hasBudgetPeriod) {
        Instant.ofEpochMilli(activePeriodEnd).atZone(zone).toLocalDate()
            .plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    } else 0L
    var analyticsPeriodMode by androidx.compose.runtime.saveable.rememberSaveable(activePeriodStart, activePeriodEnd) {
        mutableStateOf(
            if (categoryFilter != null) CategoryAnalyticsPeriodMode.ALL_TIME
            else if (hasBudgetPeriod) CategoryAnalyticsPeriodMode.BUDGET_PERIOD
            else CategoryAnalyticsPeriodMode.MONTH
        )
    }
    var analyticsMonth by androidx.compose.runtime.saveable.rememberSaveable(
        stateSaver = androidx.compose.runtime.saveable.Saver<YearMonth, String>(
            save = { it.toString() }, restore = { YearMonth.parse(it) })
    ) { mutableStateOf(YearMonth.now(zone)) }
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
            val startDate = Instant.ofEpochMilli(activePeriodStart).atZone(zone).toLocalDate()
            val endDate = Instant.ofEpochMilli(activePeriodEnd).atZone(zone).toLocalDate()
            "${startDate.format(budgetPeriodFormatter)} – ${endDate.format(budgetPeriodFormatter)}"
        }
        CategoryAnalyticsPeriodMode.MONTH -> {
            analyticsMonth.atDay(1).format(monthFormatter)
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

    val accountTransactions = remember(state.transactions, state.accounts, accountFilterId, searchQuery) {
        val scoped = if (accountFilterId == null) state.transactions else state.transactions.filter {
            it.accountId == accountFilterId || it.toAccountId == accountFilterId
        }
        searchQuery.apply(scoped, state.accounts)
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
            val periodIds = periodFilteredTransactions.mapTo(hashSetOf()) { it.id }
            analyticsTransactions.filter {
                it.id in periodIds && it.toAccountId == null && !it.isBalanceAdjustment && it.amount > 0 &&
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
    var viewingDetailTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var viewingAdjustment by remember { mutableStateOf<TransactionEntity?>(null) }
    val optimisticTransactions = remember { mutableStateMapOf<Long, Pair<TransactionEntity, TransactionEntity>>() }
    LaunchedEffect(state.transactions) {
        val byId = state.transactions.associateBy { it.id }
        optimisticTransactions.keys.toList().forEach { id ->
            val entry = optimisticTransactions[id]
            val stored = byId[id]
            if (entry == null || stored == null || stored != entry.first) {
                optimisticTransactions.remove(id)
            }
        }
    }
    fun latestTransaction(tx: TransactionEntity): TransactionEntity =
        optimisticTransactions[tx.id]?.second ?: state.transactions.firstOrNull { it.id == tx.id } ?: tx
    fun latestTransactionById(id: Long): TransactionEntity? =
        optimisticTransactions[id]?.second ?: state.transactions.firstOrNull { it.id == id }
    var fullScreenMapTransactionId by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf<Long?>(null) }
    val saveRepositionedLocation: (TransactionEntity, Double, Double, String) -> Unit = { tx, lat, lng, label ->
        val baseTx = state.transactions.firstOrNull { it.id == tx.id } ?: tx
        val updated = baseTx.withRepositionedLocation(lat, lng, label)
        if (updated != baseTx) {
            optimisticTransactions[tx.id] = baseTx to updated
        }
        if (viewingDetailTransaction?.id == tx.id) viewingDetailTransaction = updated
        if (editingTransaction?.id == tx.id) {
            editingTransaction = editingTransaction!!.withRepositionedLocation(lat, lng, label)
        }
        onIntent(FinanceIntent.EditTransaction(baseTx, updated))
    }
    var editAmountText by remember { mutableStateOf("") }
    var editCategoryText by remember { mutableStateOf("") }
    var editDescriptionText by remember { mutableStateOf("") }
    var editCustomIcon by remember { mutableStateOf<String?>(null) }
    var editType by remember { mutableStateOf(HistoryTransactionType.Expense) }
    var editSourceAccountId by remember { mutableStateOf<Long?>(null) }
    var editDestinationAccountId by remember { mutableStateOf<Long?>(null) }
    var editIsDailyBudget by remember { mutableStateOf(true) }
    var editRepayment by remember { mutableStateOf(false) }
    var editRepaymentExpenseId by remember { mutableLongStateOf(0L) }
    var editTimestamp by remember { mutableLongStateOf(0L) }
    var editRequestId by remember { mutableStateOf("") }
    var editSubmitting by remember { mutableStateOf(false) }
    val editOperation = editResult?.invoke()?.takeIf { it.requestId == editRequestId }
    val editBusy = editSubmitting || editOperation?.processing == true
    LaunchedEffect(editOperation) {
        if (editOperation != null) {
            editSubmitting = editOperation.processing
            if (!editOperation.processing && editOperation.success) editingTransaction = null
        }
    }

    // Predictive back for editing transaction modal
    androidx.activity.compose.PredictiveBackHandler(enabled = fullScreenMapTransactionId == null && (editingTransaction != null || viewingAdjustment != null || viewingDetailTransaction != null)) { progressFlow ->
        try {
            progressFlow.collect { }
            if (!editBusy) editingTransaction = null
            viewingAdjustment = null
            viewingDetailTransaction = null
        } catch (e: kotlinx.coroutines.CancellationException) {
        }
    }

    // Predictive back for active category filter
    androidx.activity.compose.PredictiveBackHandler(enabled = !multiSelect && !showSearch && !showFilters && selectedCategoryFilter != null && editingTransaction == null && viewingAdjustment == null && viewingDetailTransaction == null) { progressFlow ->
        try {
            progressFlow.collect { }
            selectedCategoryFilter = null
            onClearCategoryFilter()
        } catch (e: kotlinx.coroutines.CancellationException) {
        }
    }

    androidx.activity.compose.PredictiveBackHandler(enabled = !multiSelect && !showSearch && !showFilters && accountFilterId != null && selectedCategoryFilter == null && editingTransaction == null && viewingAdjustment == null && viewingDetailTransaction == null) { progressFlow ->
        try {
            progressFlow.collect { }
            onClearAccountFilter()
        } catch (e: kotlinx.coroutines.CancellationException) {
        }
    }


    androidx.activity.compose.PredictiveBackHandler(enabled = !multiSelect && showSearch && !showFilters && editingTransaction == null && viewingAdjustment == null && viewingDetailTransaction == null) { progressFlow ->
        try { progressFlow.collect { }; focusManager.clearFocus(); keyboard?.hide(); showSearch = false }
        catch (_: kotlinx.coroutines.CancellationException) { }
    }

    val imeOpen = androidx.compose.foundation.layout.WindowInsets.ime.getBottom(androidx.compose.ui.platform.LocalDensity.current) > 0
    androidx.activity.compose.PredictiveBackHandler(enabled = multiSelect && batchPicker == null && editingTransaction == null && viewingAdjustment == null && viewingDetailTransaction == null && !showFilters && !imeOpen) { flow ->
        try { flow.collect { }; if (!batchBusy) { multiSelect = false; selectionArray = longArrayOf() } }
        catch (_: kotlinx.coroutines.CancellationException) { }
    }
    if (batchPicker != null) HistoryBatchPicker(batchPicker!!, batchSnapshot, state, batchBusy, batchError,
        onDismiss = { if (!batchBusy) { batchPicker = null; batchRequestId = null } },
        onConfirm = { operation ->
            // Read the live request state so two taps in one frame cannot submit twice.
            if (batchRequestId == null || (batchResult?.requestId == batchRequestId &&
                    batchResult?.processing == false && batchResult?.success != true)) {
                val request = java.util.UUID.randomUUID().toString()
                batchRequestId = request
                onIntent(FinanceIntent.BatchTransactions(request, batchSnapshot, operation))
            }
        })
    val visibleIds = remember(transactionsList) { transactionsList.map { it.id }.toSet() }
    LaunchedEffect(visibleIds, multiSelect) {
        if (multiSelect && !batchBusy) selectionArray = selectedIds.intersect(visibleIds).toLongArray()
    }
    val useInspector = (com.example.vibefinance.ui.main.LocalMainWindowWidth.current
        ?: LocalConfiguration.current.screenWidthDp.dp) >= 600.dp
    var inspectedTransactionId by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf<Long?>(null) }
    var inspectDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    LaunchedEffect(transactionsList, state.isLoading) {
        if (!state.isLoading && transactionsList.none { it.id == inspectedTransactionId }) inspectedTransactionId = null
    }
    fun beginTransactionEdit(tx: TransactionEntity) {
                                            if (tx.isBalanceAdjustment) {
                                                viewingAdjustment = tx
                                            } else {
                                                editingTransaction = tx
                                                editRequestId = java.util.UUID.randomUUID().toString()
                                                editSubmitting = false
                                                editRepayment = tx.isReimbursement
                                                editRepaymentExpenseId = tx.reimbursementOf ?: 0L
                                                editTimestamp = tx.timestamp
                                                editAmountText = String.format(Locale.US, "%.2f", Math.abs(tx.amount))
                                                editCategoryText = tx.category
                                                editDescriptionText = tx.description
                                                editCustomIcon = tx.customIcon
                                                editType = when {
                                                    tx.toAccountId != null -> HistoryTransactionType.Transfer
                                                    tx.amount < 0 -> HistoryTransactionType.Income
                                                    else -> HistoryTransactionType.Expense
                                                }
                                                editSourceAccountId = tx.accountId
                                                editDestinationAccountId = tx.toAccountId
                                                editIsDailyBudget = tx.amount < 0 || !tx.isExcludedFromDailyBudget
                                            }
    }
    val historyList: @Composable () -> Unit = {
    ContentEntranceViewport(
        modifier = Modifier.fillMaxSize(),
        topVisibilityInset = topVisibilityInset,
        bottomVisibilityInset = bottomVisibilityInset,
        bottomVisibilityInsetProvider = bottomVisibilityInsetProvider,
        tagPrefix = "HistoryArrival"
    ) {
        AnimatedContent(
            targetState = state.isLoading,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
            label = "historySkeletonCrossfade"
        ) { isLoading ->
            if (isLoading) {
                com.example.vibefinance.ui.components.HistoryScreenSkeleton(modifier = Modifier)
            } else {
                // Keep the list's leading inset and analytics height stable when selection changes.
                val searchHeaderTopPadding = if (useInspector) topContentPadding else (topContentPadding - 16.dp).coerceAtLeast(0.dp)
                Column(Modifier.fillMaxSize().padding(top = searchHeaderTopPadding)) {
                // M3E search bar + filter chips, pinned above the list; it slides away while
                // scrolling down and comes straight back on any scroll up (enter-always).
                PageContentEntrance("search-header") {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clipToBounds()
                        .layout { measurable, constraints ->
                            val placeable = measurable.measure(constraints)
                            searchHeaderHeight[0] = placeable.height
                            val offset = searchHeaderOffset.floatValue.coerceIn(-placeable.height.toFloat(), 0f).roundToInt()
                            layout(placeable.width, (placeable.height + offset).coerceAtLeast(0)) { placeable.place(0, offset) }
                        }
                ) {
                    HistorySearchHeader(
                        query = searchQuery,
                        onQueryChange = { query ->
                            searchText = query.text; searchAccount = query.account; searchCategory = query.category
                            searchKind = query.kind; searchFrom = query.from; searchThrough = query.through
                            searchMinimum = query.minimum; searchMaximum = query.maximum
                        },
                        accounts = state.accounts,
                        categories = remember(state.transactions) { state.transactions.map { it.category }.distinct().sorted() },
                        results = transactionsList,
                        wide = useInspector,
                        onOpenMore = { showFilters = true },
                        onOpenResult = { tx ->
                            when {
                                useInspector -> inspectedTransactionId = tx.id
                                tx.isBalanceAdjustment -> viewingAdjustment = tx
                                else -> viewingDetailTransaction = tx
                            }
                        },
                        onExpandedChange = { isSearchExpanded ->
                            if (isSearchExpanded) searchHeaderOffset.floatValue = 0f
                        }
                    )
                }
                }
                Box(Modifier.weight(1f).fillMaxWidth().nestedScroll(searchHeaderScroll).testTag("HistoryRecordViewport")
                    .verticalFadingEdge(topFadeHeight = selectionEdge, bottomFadeHeight = 0.dp)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                        .then(
                            historyListState.scrollIndicatorState?.let {
                                Modifier.nonInteractiveScrollbar(state = it, orientation = Orientation.Vertical)
                            } ?: Modifier
                        )
                        .testTag("HistoryList"),
                    state = historyListState,
                    contentPadding = PaddingValues(top = if (useInspector) 8.dp else topContentPadding.coerceIn(0.dp, 16.dp)),
                    verticalArrangement = Arrangement.Top
                ) {
            if (accountFilterId != null) {
                item {
                    PageContentEntrance("account-filter") {
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
            }
            // --- 📅 ACTIVE BUDGET PERIOD INDICATOR & FILTER CARD ---
            if (accountFilterId == null) item {
                PageContentEntrance("period") {
                    com.example.vibefinance.ui.components.BudgetPeriodIndicatorCard(
                        budgetInfo = state.budgetInfo,
                        selectedFilter = periodFilterMode,
                        onSelectFilter = { mode -> periodFilterMode = mode },
                        modifier = Modifier.padding(bottom = 12.dp).testTag("HistoryBudgetPeriod")
                    )
                }
            }

            // --- 📊 CATEGORY BREAKDOWN & CSV EXPORT CARD ---
            item {
                val context = LocalContext.current
                PageContentEntrance("analytics") {
                    com.example.vibefinance.ui.components.CategoryBreakdownCard(
                        transactions = analyticsTransactions,
                        onSearch = null,
                        onSelectTransactions = { multiSelect = true; com.example.vibefinance.util.VibeHaptics.heavyLongPress(view) },
                        searchExpanded = false,
                        searchActive = searchQuery != HistoryQuery(),
                        searchSummary = if (searchQuery != HistoryQuery()) stringResource(R.string.history_results_count, transactionsList.size) else null,
                        onExportCsv = {
                            val csvContent = com.example.vibefinance.util.CsvExportEngine.generateCsvContent(
                                transactions = filteredTransactions,
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
            }



            if (transactionsList.isEmpty() && (analyticsHasExpenses || state.transactions.isNotEmpty())) {
                item {
                    PageContentEntrance("empty") {
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
                }
            } else {
                val groupedList = groupedTransactions.toList()
                groupedList.forEachIndexed { groupIndex, (localDate, txsForDate) ->
                    // 1. Date Header Group (Muted, Clean Typography)
                    val today = LocalDate.now(zone)
                    val dateLabel = when (localDate) {
                        today -> strToday
                        today.minusDays(1) -> strYesterday
                        else -> localDate.format(dateHeaderFormatter)
                    }
                    val weekday = localDate.format(weekdayFormatter)
                    val headerText = "$dateLabel · $weekday"
                    // Transfers only move money between accounts, so they never contribute to
                    // a day's income-minus-spending total. Balance corrections remain visible
                    // in an account's own history, where they change that account's balance.
                    val dailySummaryTransactions = txsForDate.filter { tx ->
                        tx.toAccountId == null && (accountFilterId != null || !tx.isBalanceAdjustment)
                    }
                    val dailyNet = dailySummaryTransactions.sumOf { tx ->
                        tx.historyAmountFor(accountFilterId)
                    }

                    // Sticky day header: pins under the search bar while that day's records scroll,
                    // and carries the day's net total on the right.
                    stickyHeader(key = "history-day-$localDate", contentType = "history-day-header") {
                        val bInfo = state.budgetInfo
                        val isInActivePeriod = hasBudgetPeriod && txsForDate.any { tx ->
                            tx.timestamp >= activePeriodStart && tx.timestamp < activePeriodEndExclusive
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 4.dp)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
                                .padding(start = 14.dp, end = 10.dp, top = 6.dp, bottom = 6.dp)
                                .testTag("HistoryDayHeader"),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = headerText,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                                maxLines = 1
                            )
                            if (dailySummaryTransactions.isNotEmpty()) {
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
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = activeVibeColor,
                                    maxLines = 1,
                                    modifier = Modifier.testTag("HistoryDayNet")
                                )
                            }

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
                        val selectedColor = if (multiSelect) {
                            animateColorAsState(
                                if (tx.id in selectedIds) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                                animationSpec = if (motion == MotionLevel.MINIMAL) snap() else MaterialTheme.motionScheme.fastEffectsSpec(),
                                label = "historySelectedColor_${tx.id}"
                            ).value
                        } else {
                            Color.Transparent
                        }
                        val sourceAccount = accountsMap[tx.accountId]
                        val destAccount = tx.toAccountId?.let { toId -> accountsMap[toId] }
                        val isFuture = tx.timestamp > System.currentTimeMillis()
                        val isTransfer = tx.toAccountId != null
                        val isInstallment = tx.installmentNumber != null
                        val displayDelta = tx.historyAmountFor(accountFilterId)
                        val isIncome = displayDelta > 0

                        val date = LocalDateTime.ofInstant(Instant.ofEpochMilli(tx.timestamp), zone)
                        val timeText = date.format(timeFormatter)

                        val transitionState = remember { MutableTransitionState(true) }

                        LaunchedEffect(transitionState.currentState, transitionState.targetState) {
                            if (!transitionState.currentState && !transitionState.targetState) {
                                onIntent(FinanceIntent.DeleteTransaction(tx))
                            }
                        }

                        val isFirst = index == 0
                        val isLast = index == txsForDate.size - 1

                        androidx.compose.animation.AnimatedVisibility(
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
                            PageContentEntrance("transaction:${tx.id}") {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    val isRowSelected = if (multiSelect) {
                                        tx.id in selectedIds
                                    } else if (useInspector) {
                                        inspectedTransactionId == tx.id
                                    } else {
                                        viewingDetailTransaction?.id == tx.id || editingTransaction?.id == tx.id
                                    }
                                    val rowInteractionSource = remember { MutableInteractionSource() }
                                    val isRowPressed by rowInteractionSource.collectIsPressedAsState()
                                    val itemShape = com.example.vibefinance.ui.main.rememberConnectedListRowShape(
                                        isFirst = isFirst,
                                        isLast = isLast,
                                        isSelected = isRowSelected,
                                        isPressed = isRowPressed
                                    )
                                    ExpressiveSwipeRow(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("TransactionRow_${tx.id}")
                                            .semantics { if (multiSelect) selected = tx.id in selectedIds else if (useInspector) selected = inspectedTransactionId == tx.id }
                                            .background(if (useInspector && inspectedTransactionId == tx.id) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent, itemShape),
                                        shape = itemShape,
                                        selectionMode = multiSelect,
                                        interactionSource = rowInteractionSource,
                                        onLongClick = { if (!batchBusy) { multiSelect = true; selectionArray = (selectedIds + tx.id).toLongArray(); com.example.vibefinance.util.VibeHaptics.heavyLongPress(view) } },
                                        onEdit = edit@{
                                            if (multiSelect) { toggleSelected(tx.id); return@edit }
                                            com.example.vibefinance.util.VibeHaptics.selection(view)
                                            if (useInspector) inspectedTransactionId = tx.id else beginTransactionEdit(tx)
                                        },
                                        onClick = {
                                            if (multiSelect) { toggleSelected(tx.id); return@ExpressiveSwipeRow }
                                            com.example.vibefinance.util.VibeHaptics.selection(view)
                                            if (useInspector) {
                                                inspectedTransactionId = tx.id
                                            } else {
                                                if (tx.isBalanceAdjustment) {
                                                    viewingAdjustment = tx
                                                } else {
                                                    viewingDetailTransaction = tx
                                                }
                                            }
                                        },
                                        onDelete = { transitionState.targetState = false }
                                    ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(selectedColor)
                                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                                            ) {
                                                AnimatedVisibility(visible = multiSelect,
                                                    enter = when (motion) {
                                                        MotionLevel.FULL -> expandHorizontally(spatialSpec) + fadeIn(effectsSpec)
                                                        MotionLevel.REDUCED -> fadeIn(effectsSpec)
                                                        MotionLevel.MINIMAL -> EnterTransition.None
                                                    }, exit = when (motion) {
                                                        MotionLevel.FULL -> shrinkHorizontally(spatialSpec) + fadeOut(effectsSpec)
                                                        MotionLevel.REDUCED -> fadeOut(effectsSpec)
                                                        MotionLevel.MINIMAL -> ExitTransition.None
                                                    }) {
                                                    Checkbox(checked = tx.id in selectedIds, onCheckedChange = { toggleSelected(tx.id) }, enabled = !batchBusy,
                                                        modifier = Modifier.testTag("HistorySelected_${tx.id}"))
                                                }
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
                                                                customIcon = if (tx.isReimbursement) "icon:reimbursement" else tx.customIcon.takeIf { tx.amount > 0 && tx.toAccountId == null },
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
                                                        tx.isBalanceAdjustment -> strBalanceAdjustment
                                                        tx.description.isNotBlank() -> tx.description
                                                        tx.isReimbursement -> stringResource(R.string.reimbursement_title)
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
                                                                    text = strNonDailyExpense,
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp),
                                                                    fontWeight = FontWeight.SemiBold
                                                                )
                                                            }
                                                        }
                                                    }

                                                    val cardName = sourceAccount?.displayLabel() ?: strFallbackCash
                                                    val categoryName = (if (tx.isReimbursement) stringResource(R.string.reimbursement_title) + " · " else "") + com.example.vibefinance.ui.home.getCategoryDisplayName(tx.category)
                                                    val cardSubtitle = when {
                                                        tx.isBalanceAdjustment -> cardName
                                                        isTransfer -> "${sourceAccount?.displayLabel() ?: strFallbackAccount} ➔ ${destAccount?.displayLabel() ?: strFallbackAccount}"
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
                                                                contentDescription = strDelete,
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
                    }

                    // 4. Day Divider (if this is not the last group/day)
                    if (groupIndex < groupedList.size - 1) {
                        item {
                            androidx.compose.material3.HorizontalDivider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                color = MaterialTheme.colorScheme.outlineVariant,
                                thickness = 1.dp
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
            // Room for the floating batch toolbar so the last record can scroll above it.
                    item(key = "history-batch-toolbar-space") {
                        androidx.compose.animation.AnimatedVisibility(multiSelect) { Spacer(Modifier.height(88.dp)) }
                    }
                }
                // Multi-select: M3E floating toolbar at the bottom instead of a top dock.
                androidx.compose.animation.AnimatedVisibility(
                    visible = multiSelect,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = bottomVisibilityInsetProvider() + 12.dp, start = 8.dp, end = 8.dp)
                        .testTag("HistorySelectionDock"),
                    enter = slideInVertically(com.example.vibefinance.theme.VibeMotion.defaultSpatial()) { it } +
                        fadeIn(com.example.vibefinance.theme.VibeMotion.defaultEffects()),
                    exit = slideOutVertically(com.example.vibefinance.theme.VibeMotion.defaultSpatial()) { it } +
                        fadeOut(com.example.vibefinance.theme.VibeMotion.fastEffects())
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (batchPicker == null && batchError != null) Text(batchError, color = MaterialTheme.colorScheme.error)
                    val records = transactionsList.filter { it.id in selectedIds }
                    val ordinary = records.isNotEmpty() && records.all { it.toAccountId == null && !it.isBalanceAdjustment && !it.isReimbursement }
                    HistoryBatchToolbar(selectedIds.size, visibleIds.isNotEmpty() && selectedIds == visibleIds, batchBusy || !multiSelect,
                        canCategory = ordinary && (records.all { it.amount > 0 } || records.all { it.amount < 0 }), canAccount = ordinary,
                        onClose = { multiSelect = false; selectionArray = longArrayOf() },
                        onSelectAll = { selectionArray = if (selectedIds == visibleIds) longArrayOf() else visibleIds.toLongArray() },
                        onAction = { kind -> batchSnapshot = records.toList(); batchPicker = kind; batchRequestId = null
                            if (kind == "delete") com.example.vibefinance.util.VibeHaptics.warningAlert(view) })
                    }
                }
                }
                }
        }
        }
    }
    }
    // Shared edit form for the transaction page (phone full screen and the wide detail pane).
    val transactionEditUi: @Composable (TransactionEntity) -> TransactionEditUi = { tx ->
        val canChangeType = !tx.isBalanceAdjustment
        val editIsIncome = editType == HistoryTransactionType.Income
        val editIsTransfer = editType == HistoryTransactionType.Transfer
        val editIsRepayment = editIsIncome && editRepayment
        val repaymentRecords = remember(state.transactions, tx.id) { state.transactions.filterNot { it.id == tx.id } }
        val repaymentExpense = repaymentRecords.firstOrNull { it.id == editRepaymentExpenseId &&
            com.example.vibefinance.util.Reimbursements.isExpense(it) && it.timestamp <= System.currentTimeMillis() &&
            com.example.vibefinance.util.Reimbursements.expenseOnOrBeforeDay(it.timestamp, editTimestamp) }
        val repaymentAmount = editAmountText.toBigDecimalOrNull()?.stripTrailingZeros()
        val validRepayment = !editIsRepayment || (editTimestamp <= System.currentTimeMillis() && (editRepaymentExpenseId == 0L || repaymentExpense != null) &&
            repaymentAmount != null && repaymentAmount > java.math.BigDecimal.ZERO && repaymentAmount.scale() <= 2 &&
            state.accounts.any { it.id == (editSourceAccountId ?: tx.accountId) && it.type != com.example.vibefinance.data.entity.AccountType.INVESTMENT })
        val repaymentEnter = if (motion == MotionLevel.MINIMAL) EnterTransition.None else
            androidx.compose.animation.fadeIn(com.example.vibefinance.theme.MotionTokens.emphasized(160)) +
                expandVertically(animationSpec = com.example.vibefinance.theme.MotionTokens.emphasized(240))
        val repaymentExit = if (motion == MotionLevel.MINIMAL) ExitTransition.None else
            androidx.compose.animation.fadeOut(com.example.vibefinance.theme.MotionTokens.emphasized(100)) +
                androidx.compose.animation.shrinkVertically(animationSpec = com.example.vibefinance.theme.MotionTokens.emphasized(240))
        val validAccounts = if (editIsTransfer) {
            state.accounts.any { it.id == editSourceAccountId } &&
                state.accounts.any { it.id == editDestinationAccountId } &&
                editSourceAccountId != editDestinationAccountId
        } else {
            state.accounts.isEmpty() || editSourceAccountId == null ||
                state.accounts.any { it.id == editSourceAccountId }
        }
        val editedMagnitude = editAmountText.toDoubleOrNull()?.takeIf { it.isFinite() && it > 0.0 }

        TransactionEditUi(
            form = {
                // Phone dialog: tighter rhythm and a shorter map so the shared fields fit without scrolling.
                val compactForm = LocalCompactTransactionForm.current
                Column(
                    verticalArrangement = Arrangement.spacedBy(if (compactForm) 8.dp else 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Where the intercepted payment happened comes first, as context for the edit.
                    rememberTransactionLocation(latestTransaction(tx))?.let {
                        EditTransactionLocationCard(
                            it,
                            mapHeight = if (compactForm) 84.dp else 140.dp,
                            onOpenFullScreen = { fullScreenMapTransactionId = tx.id }
                        )
                    }
                    if (canChangeType) {
                        // The selector's own labels are enough in the compact phone dialog.
                        if (!compactForm) Text(
                            text = stringResource(R.string.edit_transaction_type_label),
                            style = MaterialTheme.typography.labelLarge
                        )
                        HistoryTransactionTypeSelector(
                            type = editType,
                            enabled = !editBusy,
                            onTypeSelected = { selectedType ->
                                if (editType == HistoryTransactionType.Transfer &&
                                    selectedType != HistoryTransactionType.Transfer && editCategoryText == "Transfer") {
                                    editCategoryText = if (selectedType == HistoryTransactionType.Income) "Salary" else "Food"
                                }
                                editType = selectedType
                            }
                        )
                    }
                    OutlinedTextField(
                        visualTransformation = if (com.example.vibefinance.ui.preferences.LocalExperience.current.hideAmounts)
                            androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,

                        value = editAmountText,
                        enabled = !editBusy,
                        onValueChange = { editAmountText = it },
                        label = { Text(stringResource(R.string.edit_amount_label) + " (HK$)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().sharedTransitionBounds(SharedTxKeys.Amount).testTag("EditTransactionAmount")
                    )

                    TransactionDateTimeEditor(
                        timestamp = editTimestamp,
                        enabled = !editBusy,
                        onTimestampChange = { editTimestamp = it }
                    )

                    if (editIsTransfer) {
                        HistoryAccountDropdown(
                            accountId = editSourceAccountId,
                            accounts = state.accounts,
                            enabled = !editBusy,
                            label = stringResource(R.string.transfer_from),
                            tag = "EditTransactionSource",
                            modifier = Modifier.sharedTransitionBounds(SharedTxKeys.Account),
                            onAccountSelected = {
                                editSourceAccountId = it
                                if (editDestinationAccountId == it) editDestinationAccountId = null
                            }
                        )
                        HistoryAccountDropdown(
                            accountId = editDestinationAccountId,
                            accounts = state.accounts.filter { it.id != editSourceAccountId },
                            enabled = !editBusy,
                            label = stringResource(R.string.transfer_to),
                            tag = "EditTransactionDestination",
                            onAccountSelected = { editDestinationAccountId = it }
                        )
                        if (state.accounts.size < 2) {
                            Text(
                                stringResource(R.string.edit_transfer_accounts_required),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        HistoryAccountDropdown(
                            accountId = editSourceAccountId,
                            accounts = state.accounts.filter { !editIsRepayment || it.type != com.example.vibefinance.data.entity.AccountType.INVESTMENT },
                            enabled = !editBusy,
                            label = stringResource(
                                if (editIsIncome) R.string.edit_account_income_label
                                else R.string.edit_account_expense_label
                            ),
                            tag = "EditTransactionAccount",
                            modifier = Modifier.sharedTransitionBounds(SharedTxKeys.Account),
                            onAccountSelected = { editSourceAccountId = it }
                        )
                        Column(Modifier.fillMaxWidth()) {
                            if (editIsIncome) {
                                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                                    .toggleable(value = editRepayment, enabled = !editBusy,
                                        role = androidx.compose.ui.semantics.Role.Switch,
                                        onValueChange = {
                                            editRepayment = it
                                            if (!it && tx.isReimbursement && editCategoryText == tx.category) editCategoryText = "Salary"
                                            com.example.vibefinance.util.VibeHaptics.selection(view)
                                        }).padding(12.dp).testTag("EditTransactionReimbursement"),
                                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(stringResource(R.string.reimbursement_title), style = MaterialTheme.typography.titleSmall)
                                        Text(stringResource(R.string.income_reimbursement_hint), style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    AppSwitch(checked = editRepayment, enabled = !editBusy,
                                        onCheckedChange = {
                                            editRepayment = it
                                            if (!it && tx.isReimbursement && editCategoryText == tx.category) editCategoryText = "Salary"
                                        }, modifier = Modifier.clearAndSetSemantics { })
                                }
                                AnimatedVisibility(visible = editIsRepayment, enter = repaymentEnter, exit = repaymentExit) {
                                    Column(Modifier.fillMaxWidth().padding(top = 12.dp).testTag("EditTransactionReimbursementOptions"),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        com.example.vibefinance.ui.main.ReimbursementExpenseSelector(repaymentRecords, editRepaymentExpenseId,
                                            enabled = !editBusy, latestExpenseTimestamp = editTimestamp,
                                            onClear = { editRepaymentExpenseId = 0L }, onSelect = { editRepaymentExpenseId = it.id })
                                        Text(stringResource(R.string.history_reimbursement_date_hint,
                                            java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM,
                                                LocalContext.current.resources.configuration.locales[0]).format(java.util.Date(editTimestamp))),
                                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        if (editTimestamp > System.currentTimeMillis()) Text(stringResource(R.string.reimbursement_not_received),
                                            color = MaterialTheme.colorScheme.error)
                                        if (repaymentExpense?.isExcludedFromDailyBudget == true) Text(stringResource(R.string.reimbursement_excluded_hint))
                                    }
                                }
                            }
                            AnimatedVisibility(visible = !editIsRepayment, enter = repaymentEnter, exit = repaymentExit) {
                                Column(Modifier.fillMaxWidth().padding(top = if (editIsIncome) 12.dp else 0.dp)) {
                                    if (canChangeType) {
                                        HistoryCategoryDropdown(
                                            category = editCategoryText, isIncome = editIsIncome,
                                            transactions = state.transactions, categoryMergeRules = state.categoryMergeRules,
                                            onCategorySelected = { if (!editBusy) editCategoryText = it },
                                            modifier = Modifier.sharedTransitionBounds(SharedTxKeys.Category)
                                        )
                                    } else {
                                        OutlinedTextField(value = com.example.vibefinance.ui.home.getCategoryDisplayName(editCategoryText),
                                            onValueChange = {}, readOnly = true, label = { Text(stringResource(R.string.edit_category_label)) },
                                            singleLine = true, modifier = Modifier.fillMaxWidth().testTag("EditTransactionCategory"))
                                    }
                                }
                            }
                        }
                    }

                    if (canChangeType && editType == HistoryTransactionType.Expense) {
                        com.example.vibefinance.ui.components.ExpenseIconChoice(
                            value = editCustomIcon, category = editCategoryText, onValueChange = { editCustomIcon = it }
                        )
                    }

                    OutlinedTextField(
                        value = editDescriptionText,
                        enabled = !editBusy,
                        onValueChange = { editDescriptionText = it },
                        label = { Text(stringResource(R.string.edit_description_label)) },
                        modifier = Modifier.fillMaxWidth().sharedTransitionBounds(SharedTxKeys.Description).testTag("EditTransactionDescription")
                    )

                    editOperation?.error?.let { Text(it, color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("EditTransactionError")) }

                    // Daily Budget Toggle for Expenses
                    if (canChangeType && editType == HistoryTransactionType.Expense) {
                        val editHaptic = rememberVibeHapticFeedback()
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !editBusy) {
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
                                AppSwitch(
                                    checked = editIsDailyBudget,
                                    enabled = !editBusy,
                                    onCheckedChange = { editIsDailyBudget = it },
                                    modifier = Modifier.testTag("EditTransactionDailyBudget")
                                )
                            }
                        }
                    }
                }
            },
            canSave = !editBusy && editedMagnitude != null && validAccounts && validRepayment,
            busy = editBusy,
            save = {
                        val amount = editedMagnitude
                        if (amount != null && validAccounts && validRepayment && !editBusy) {
                            val currentStoredTx = latestTransaction(tx)
                            val oldStoredTx = state.transactions.firstOrNull { it.id == tx.id } ?: currentStoredTx
                            // Apply the edited time first: reimbursement checks inside read it.
                            val newTx = currentStoredTx.copy(timestamp = editTimestamp).withHistoryEdit(
                                magnitude = amount,
                                isIncome = editIsIncome,
                                category = editCategoryText,
                                description = editDescriptionText,
                                countsTowardDailyBudget = editIsDailyBudget,
                                customIcon = editCustomIcon,
                                isTransfer = editIsTransfer,
                                sourceAccountId = if (editIsTransfer) {
                                    requireNotNull(editSourceAccountId)
                                } else {
                                    editSourceAccountId ?: currentStoredTx.accountId
                                },
                                destinationAccountId = if (editIsTransfer) editDestinationAccountId else null,
                                repaymentExpense = if (editIsRepayment) repaymentExpense else null,
                                unlinkedRepayment = editIsRepayment && repaymentExpense == null
                            )
                            editSubmitting = editResult != null
                            onIntent(FinanceIntent.EditTransaction(oldStoredTx, newTx, editRequestId,
                                if (editIsRepayment) repaymentExpense else null))
                            if (editResult == null) editingTransaction = null
                        }
            }
        )
    }
    if (useInspector) {
        // Opening the add page clears the selection, so tapping any record (even the one shown
        // before) closes the add page and opens that record's detail.
        val addOpen = addPane != null
        val inspectorMapHost = remember { SharedMapHost() }
        val inContentContainer = com.example.vibefinance.ui.main.LocalNavigationSuiteType.current ==
            androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType.WideNavigationRailCollapsed
        LaunchedEffect(addOpen) {
            if (addOpen) {
                inspectedTransactionId = null
                fullScreenMapTransactionId = null
            }
        }
        LaunchedEffect(inspectedTransactionId) {
            if (inspectedTransactionId != null && addPane != null) onDismissAddPane()
            if (fullScreenMapTransactionId != null && inspectedTransactionId != fullScreenMapTransactionId) {
                fullScreenMapTransactionId = null
            }
        }
        LaunchedEffect(state.transactions, state.isLoading) {
            if (!state.isLoading && fullScreenMapTransactionId != null &&
                state.transactions.none { it.id == fullScreenMapTransactionId }
            ) {
                fullScreenMapTransactionId = null
            }
        }
        var displayedTransactionId by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(inspectedTransactionId) }
        var detailExiting by remember { mutableStateOf(false) }
        var activeDetailEntrance by remember { mutableStateOf<com.example.vibefinance.ui.motion.PaneEntranceState?>(null) }
        val minimalInspectorMotion = com.example.vibefinance.ui.preferences.LocalExperience.current.motion ==
            com.example.vibefinance.ui.preferences.MotionLevel.MINIMAL
        LaunchedEffect(inspectedTransactionId, addPane != null, minimalInspectorMotion, state.transactions) {
            if (addPane != null) {
                detailExiting = false
                displayedTransactionId = null
                return@LaunchedEffect
            }
            val currentId = displayedTransactionId
            val targetId = inspectedTransactionId
            if (currentId == targetId) {
                detailExiting = false
                return@LaunchedEffect
            }
            val currentExists = currentId != null && latestTransactionById(currentId) != null
            if (!currentExists || minimalInspectorMotion) {
                detailExiting = false
                displayedTransactionId = targetId
            } else {
                detailExiting = true
                val waitMs = activeDetailEntrance?.totalExitDurationMs ?: 255L
                kotlinx.coroutines.delay(waitMs)
                detailExiting = false
                displayedTransactionId = inspectedTransactionId
            }
        }
        CompositionLocalProvider(LocalSharedMapHost provides inspectorMapHost) {
            AnimatedContent(
                targetState = fullScreenMapTransactionId,
                transitionSpec = {
                    fadeIn(com.example.vibefinance.theme.VibeMotion.slowEffects()) togetherWith
                        fadeOut(com.example.vibefinance.theme.VibeMotion.defaultEffects())
                },
                modifier = modifier.fillMaxSize(),
                label = "historyInspectorFullScreenMap"
            ) { activeMapTxId ->
                CompositionLocalProvider(
                    com.example.vibefinance.ui.motion.LocalSharedContentScope provides this,
                    com.example.vibefinance.ui.motion.LocalIsSharedTargetPage provides (activeMapTxId == fullScreenMapTransactionId)
                ) {
                    val mapTx = activeMapTxId?.let { targetId ->
                        latestTransactionById(targetId)
                    }
                    if (mapTx != null) {
                        TransactionFullScreenMapOverlay(
                            transaction = mapTx,
                            inContentContainer = inContentContainer,
                            onExit = { fullScreenMapTransactionId = null },
                            onSaveLocation = { lat, lng, label ->
                                saveRepositionedLocation(mapTx, lat, lng, label)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        com.example.vibefinance.ui.main.AdaptiveRecordScene(
                            if (addPane != null) ADD_TRANSACTION_PANE_ID else displayedTransactionId,
                            { if (addPane != null) onDismissAddPane() else inspectedTransactionId = null },
                            Modifier.fillMaxSize().padding(horizontal = horizontalContentPadding),
                            historyList,
                            instantDetail = addPane != null
                        ) { id ->
                            if (id == ADD_TRANSACTION_PANE_ID) {
                                Box(Modifier.fillMaxSize().padding(top = topContentPadding, bottom = 16.dp)) { addPane?.invoke() }
                                return@AdaptiveRecordScene
                            }
                            latestTransactionById(id)?.let { tx ->
                                val paneEditing = editingTransaction?.id == tx.id
                                val reimbursementAccountId = state.accounts.firstOrNull {
                                    it.id == tx.accountId && it.type != com.example.vibefinance.data.entity.AccountType.INVESTMENT
                                }?.id ?: state.accounts.firstOrNull {
                                    it.type != com.example.vibefinance.data.entity.AccountType.INVESTMENT
                                }?.id ?: tx.accountId
                                // Staggered content entrance and exit for each record shown in the detail pane.
                                androidx.compose.runtime.key(id) {
                                    com.example.vibefinance.ui.motion.StaggeredPane(
                                        modifier = Modifier.fillMaxSize(),
                                        exiting = detailExiting
                                    ) { paneState ->
                                        SideEffect { activeDetailEntrance = paneState }
                                        TransactionDetailPage(
                                            transaction = tx,
                                            accounts = state.accounts,
                                            embedded = true,
                                            editing = paneEditing,
                                            editUi = editingTransaction?.takeIf { paneEditing }?.let { transactionEditUi(it) },
                                            onClose = { inspectedTransactionId = null },
                                            onEdit = { beginTransactionEdit(it) },
                                            onCancelEdit = { if (!editBusy) editingTransaction = null },
                                            onDelete = { inspectDelete = it },
                                            onUpdateTransaction = { updated ->
                                                val baseTx = state.transactions.firstOrNull { it.id == tx.id } ?: tx
                                                if (updated != baseTx) {
                                                    optimisticTransactions[tx.id] = baseTx to updated
                                                }
                                                onIntent(FinanceIntent.EditTransaction(baseTx, updated))
                                            },
                                            onMoneyBack = { com.example.vibefinance.util.VibeHaptics.selection(view); beginTransactionEdit(tx); editType = HistoryTransactionType.Income; editRepayment = true },
                                            onUnlink = { com.example.vibefinance.util.VibeHaptics.selection(view); beginTransactionEdit(tx); editType = HistoryTransactionType.Income; editRepayment = true; editRepaymentExpenseId = 0L },
                                            onRecordReimbursement = { amount, desc ->
                                                onIntent(FinanceIntent.RecordReimbursement(java.util.UUID.randomUUID().toString(), tx, amount, reimbursementAccountId, desc))
                                            },
                                            onOpenFullScreenMap = { fullScreenMapTransactionId = tx.id },
                                            modifier = Modifier.padding(top = topContentPadding)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } else Box(modifier.padding(horizontal = horizontalContentPadding)) { historyList() }
    inspectDelete?.let { tx ->
        AlertDialog(onDismissRequest = { inspectDelete = null },
            title = { Text(stringResource(R.string.btn_delete)) },
            text = { Text(tx.description.ifBlank { tx.category }) },
            confirmButton = { TextButton(onClick = { onIntent(FinanceIntent.DeleteTransaction(tx)); inspectDelete = null }) { Text(stringResource(R.string.btn_delete)) } },
            dismissButton = { TextButton(onClick = { inspectDelete = null }) { Text(stringResource(R.string.btn_cancel)) } })
    }


    if (showFilters) {
        HistoryFilterSheet(searchQuery, state.accounts, state.transactions.map { it.category }.distinct().sorted(),
            transactionsList.size, onChange = { query ->
                searchText = query.text; searchAccount = query.account; searchCategory = query.category
                searchKind = query.kind; searchFrom = query.from; searchThrough = query.through
                searchMinimum = query.minimum; searchMaximum = query.maximum
            }, onClear = {
                searchText = ""; searchAccount = null; searchCategory = null; searchKind = HistoryKind.ALL
                searchFrom = ""; searchThrough = ""; searchMinimum = ""; searchMaximum = ""
                selectedCategoryFilter = null
                periodFilterMode = com.example.vibefinance.ui.components.PeriodFilterMode.ALL
                analyticsPeriodMode = CategoryAnalyticsPeriodMode.ALL_TIME
                onClearCategoryFilter(); onClearAccountFilter()
            }, onDismiss = { showFilters = false })
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
                CompletePressTextButton(onClick = { viewingAdjustment = null }) {
                    Text(stringResource(R.string.btn_close))
                }
            }
        )
    }

    // Phones keep the original dialogs (user preference) but in one window: pressing Edit morphs
    // the detail dialog into the edit dialog, and Cancel / Save morph back. Wide windows use
    // TransactionDetailPage instead.
    if (!useInspector) {
        var lastEditTx by remember { mutableStateOf<TransactionEntity?>(null) }
        editingTransaction?.let { lastEditTx = it }
        val detailTx = viewingDetailTransaction?.let { v -> latestTransaction(v) }
        LaunchedEffect(detailTx, editingTransaction) {
            if (detailTx == null && editingTransaction == null) {
                fullScreenMapTransactionId = null
            }
        }
        if (detailTx != null || editingTransaction != null) {
            val isEditing = editingTransaction != null
            val fullScreenMapTx = fullScreenMapTransactionId?.let { targetId ->
                latestTransactionById(targetId)
                    ?: detailTx?.takeIf { it.id == targetId }
                    ?: editingTransaction?.takeIf { it.id == targetId }
            }
            MorphingTransactionDialog(
                editing = isEditing,
                fullScreenMapOpen = fullScreenMapTx != null,
                onDismissRequest = {
                    if (isEditing) { if (!editBusy) editingTransaction = null } else viewingDetailTransaction = null
                },
                fullScreenMap = {
                    fullScreenMapTx?.let { mapTx ->
                        TransactionFullScreenMapOverlay(
                            transaction = mapTx,
                            inContentContainer = false,
                            onExit = { fullScreenMapTransactionId = null },
                            onSaveLocation = { lat, lng, label ->
                                saveRepositionedLocation(mapTx, lat, lng, label)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                },
                detail = {
                    detailTx?.let { tx ->
                        val reimbursementAccountId = state.accounts.firstOrNull {
                            it.id == tx.accountId && it.type != com.example.vibefinance.data.entity.AccountType.INVESTMENT
                        }?.id ?: state.accounts.firstOrNull {
                            it.type != com.example.vibefinance.data.entity.AccountType.INVESTMENT
                        }?.id ?: tx.accountId
                        TransactionDetailDialog(
                            transaction = tx,
                            accounts = state.accounts,
                            inline = true,
                            onDismissRequest = { viewingDetailTransaction = null },
                            onEdit = {
                                com.example.vibefinance.util.VibeHaptics.selection(view)
                                beginTransactionEdit(it)
                            },
                            onDelete = {
                                viewingDetailTransaction = null
                                onIntent(FinanceIntent.DeleteTransaction(it))
                            },
                            onUpdateTransaction = { updated ->
                                val baseTx = state.transactions.firstOrNull { it.id == tx.id } ?: tx
                                if (updated != baseTx) {
                                    optimisticTransactions[tx.id] = baseTx to updated
                                }
                                viewingDetailTransaction = updated
                                onIntent(FinanceIntent.EditTransaction(baseTx, updated))
                            },
                            onRecordReimbursement = { amount, desc ->
                                onIntent(FinanceIntent.RecordReimbursement(java.util.UUID.randomUUID().toString(), tx, amount, reimbursementAccountId, desc))
                            },
                            onOpenFullScreenMap = { fullScreenMapTransactionId = tx.id },
                        )
                    }
                },
                edit = {
                    (editingTransaction ?: lastEditTx)?.let { tx ->
                        val ui = transactionEditUi(tx)
                        val dialogScale = 1f
        TransactionDialogFrame(
            inline = true,
            onDismissRequest = { if (!editBusy) editingTransaction = null },
            title = {
                Text(
                    text = stringResource(R.string.edit_transaction_title),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                val editScroll = rememberScrollState()
                com.example.vibefinance.ui.common.ScrollBlurContainer(scrollState = editScroll, vertical = true) { blurModifier ->
                    Column(blurModifier.fillMaxWidth().verticalScroll(editScroll)) { ui.form() }
                }
            },
            confirmButton = {
                val haptic = rememberVibeHapticFeedback()
                CompletePressButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        ui.save()
                    },
                    enabled = ui.canSave,
                    shapes = com.example.vibefinance.ui.components.AppButtonDefaults.shapes(),
                    modifier = Modifier.testTag("EditTransactionSave")
                ) {
                    if (ui.busy) LoadingIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                    else Text(stringResource(R.string.btn_save))
                }
            },
            dismissButton = {
                val haptic = rememberVibeHapticFeedback()
                CompletePressTextButton(
                    enabled = !editBusy,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        editingTransaction = null
                    },
                    shapes = com.example.vibefinance.ui.components.AppButtonDefaults.shapes(),
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
                }
            )
        }
    }
}

private enum class HistoryTransactionType { Expense, Income, Transfer }

@Composable
private fun HistoryTransactionTypeSelector(
    type: HistoryTransactionType,
    onTypeSelected: (HistoryTransactionType) -> Unit,
    enabled: Boolean = true
) {
    val colors = MaterialTheme.colorScheme
    val haptic = rememberVibeHapticFeedback()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup()
            .testTag("EditTransactionTypeGroup"),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
    ) {
        HistoryTransactionType.entries.forEachIndexed { index, option ->
            val isSelected = type == option
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val colorMotion = rememberConnectedButtonColorMotion(
                isSelected = isSelected,
                isPressed = isPressed,
                backdropColor = colors.surfaceContainerHigh
            )
            ConnectedButtonRipple(isSelected = isSelected) {
                CompletePressToggleButton(
                    checked = isSelected,
                    enabled = enabled,
                    onCheckedChange = {
                        onTypeSelected(option)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    shapes = when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        2 -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
                    interactionSource = interactionSource,
                    colors = ToggleButtonDefaults.colors(
                        containerColor = colorMotion.containerColor,
                        checkedContainerColor = colorMotion.containerColor,
                        contentColor = colorMotion.contentColor,
                        checkedContentColor = colorMotion.contentColor
                    ),
                    elevation = null,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("EditTransaction${option.name}")
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
                            Text(
                                text = stringResource(when (option) {
                                    HistoryTransactionType.Expense -> R.string.filter_expense
                                    HistoryTransactionType.Income -> R.string.filter_income
                                    HistoryTransactionType.Transfer -> R.string.filter_transfer
                                }),
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
private fun HistoryAccountDropdown(
    accountId: Long?,
    accounts: List<AccountEntity>,
    label: String,
    tag: String,
    onAccountSelected: (Long) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled && accounts.isNotEmpty()) expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = accounts.firstOrNull { it.id == accountId }?.displayLabel().orEmpty(),
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth().testTag(tag)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 320.dp).testTag("${tag}Menu")
        ) {
            accounts.forEach { account ->
                DropdownMenuItem(
                    text = { Text(account.displayLabel()) },
                    onClick = { onAccountSelected(account.id); expanded = false },
                    modifier = Modifier.testTag("${tag}Option_${account.id}")
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HistoryCategoryDropdown(
    category: String,
    isIncome: Boolean,
    transactions: List<TransactionEntity>,
    categoryMergeRules: com.example.vibefinance.data.entity.CategoryMergeRules,
    modifier: Modifier = Modifier,
    onCategorySelected: (String) -> Unit
) {
    var expanded by remember(isIncome) { mutableStateOf(false) }
    val kind = if (isIncome) com.example.vibefinance.data.entity.CategoryKind.INCOME else com.example.vibefinance.data.entity.CategoryKind.EXPENSE
    val defaultCategories = (if (isIncome) DefaultIncomeCategories else DefaultExpenseCategories)
        .map { categoryMergeRules.resolve(it, kind) }.distinct()
    val categoryFrequency = remember(transactions, isIncome) {
        transactions.filter {
            it.toAccountId == null && !it.isBalanceAdjustment && !it.isReimbursement && (it.amount < 0.0) == isIncome
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
    val haptic = rememberVibeHapticFeedback()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
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
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
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

/** Change a record in place; transfer conversions detach this record from any installment plan. */
internal fun TransactionEntity.withHistoryEdit(
    magnitude: Double,
    isIncome: Boolean,
    category: String,
    description: String,
    countsTowardDailyBudget: Boolean,
    customIcon: String? = this.customIcon,
    isTransfer: Boolean = toAccountId != null,
    sourceAccountId: Long = accountId,
    destinationAccountId: Long? = toAccountId,
    repaymentExpense: TransactionEntity? = null,
    unlinkedRepayment: Boolean = false
): TransactionEntity {
    require(magnitude.isFinite() && magnitude > 0.0) { "Amount must be positive and finite" }
    require(!isBalanceAdjustment) { "Balance adjustments are read only" }
    require(repaymentExpense == null || (isIncome && !isTransfer && repaymentExpense.id != id &&
        com.example.vibefinance.util.Reimbursements.isExpense(repaymentExpense) &&
        com.example.vibefinance.util.Reimbursements.expenseOnOrBeforeDay(repaymentExpense.timestamp, timestamp))) {
        "Choose an original expense on or before the repayment day"
    }
    require(!isTransfer || (destinationAccountId != null && sourceAccountId != destinationAccountId)) {
        "A transfer needs two different accounts"
    }
    require(!unlinkedRepayment || (isIncome && !isTransfer && repaymentExpense == null))
    val changesTransferType = (toAccountId != null) != isTransfer
    val detachesInstallment = changesTransferType || ((repaymentExpense != null || unlinkedRepayment) && !isReimbursement)
    // Keep the signed direction of legacy transfers when only editing their fields.
    val negative = if (isTransfer) toAccountId != null && amount < 0.0 else isIncome
    return copy(
        amount = if (negative) -magnitude else magnitude,
        accountId = sourceAccountId,
        toAccountId = if (isTransfer) destinationAccountId else null,
        sourceWasCreditCard = sourceWasCreditCard.takeIf { sourceAccountId == accountId },
        destinationWasCreditCard = destinationWasCreditCard.takeIf { isTransfer && destinationAccountId == toAccountId },
        category = repaymentExpense?.category ?: if (isTransfer) "Transfer" else category,
        reimbursementOf = repaymentExpense?.id,
        isUnlinkedReimbursement = unlinkedRepayment,
        description = description,
        customIcon = com.example.vibefinance.data.entity.ExpenseIcon.normalize(customIcon),
        isExcludedFromDailyBudget = repaymentExpense?.isExcludedFromDailyBudget ?: if (unlinkedRepayment) false else (isTransfer || isIncome || !countsTowardDailyBudget),
        installmentNumber = installmentNumber.takeUnless { detachesInstallment },
        totalInstallments = totalInstallments.takeUnless { detachesInstallment },
        groupId = groupId.takeUnless { detachesInstallment }
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
