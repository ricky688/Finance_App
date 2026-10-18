@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)

package com.example.vibefinance.ui.recurring

import androidx.compose.material.icons.automirrored.filled.TrendingUp
import com.example.vibefinance.ui.common.ContentEntranceViewport
import com.example.vibefinance.ui.common.PageContentEntrance

import com.example.vibefinance.ui.components.rememberCompletePressProgress
import com.example.vibefinance.ui.components.completePressShape

import com.example.vibefinance.ui.common.horizontalFadingEdge
import com.example.vibefinance.theme.ChartColors
import com.example.vibefinance.theme.MotionTokens
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.luminance
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.filled.KeyboardArrowDown
import com.example.vibefinance.ui.common.pressBounce
import com.example.vibefinance.ui.components.CompletePressButton
import com.example.vibefinance.ui.main.ExpressiveSegmentedButtonGroup
import com.example.vibefinance.ui.main.ConnectedButtonCorners
import com.example.vibefinance.ui.components.AppModalBottomSheet
import com.example.vibefinance.ui.main.SelectedAccountCard
import com.example.vibefinance.ui.main.getAccountIcon
import com.example.vibefinance.ui.main.getCategoryDisplayNameFromResources
import com.example.vibefinance.ui.main.accountTypeLabel
import kotlinx.coroutines.launch
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.material.icons.filled.Clear
import com.example.vibefinance.ui.components.ExpressiveConnectedButtonGroup
import com.example.vibefinance.ui.components.ExpressiveSwipeRow
import com.example.vibefinance.ui.components.RollingNumberText
import com.example.vibefinance.ui.components.rememberConnectedButtonColorMotion
import com.example.vibefinance.ui.home.getCategoryDisplayName
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewTimeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.example.vibefinance.ui.components.AppModalBottomSheet as ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import com.example.vibefinance.ui.preferences.PrivacyText as Text
import androidx.compose.ui.res.stringResource
import com.example.vibefinance.R
import com.example.vibefinance.theme.rememberIconShape
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import com.example.vibefinance.util.rememberVibeHapticFeedback
import androidx.compose.ui.platform.LocalView
import android.view.SoundEffectConstants
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.vibefinance.data.currency.ExchangeRateLoader
import com.example.vibefinance.data.currency.ExchangeRateRepository
import com.example.vibefinance.data.currency.ExchangeRateResult
import com.example.vibefinance.ui.components.SwipeActions
import com.example.vibefinance.ui.components.SwipeActionsConfig
import com.example.vibefinance.ui.components.GlassmorphicCard
import com.example.vibefinance.ui.main.CurrencyRateStatus
import com.example.vibefinance.util.CurrencyEngine
import java.io.File
import kotlin.math.abs
import androidx.compose.material3.LinearProgressIndicator
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.data.entity.SubscriptionEntity
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.ui.FinanceIntent
import com.example.vibefinance.ui.FinanceUiState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.toMutableStateList
import com.example.vibefinance.data.entity.CategoryKind
import com.example.vibefinance.data.entity.CategoryMergeRules
import com.example.vibefinance.data.entity.categoryChoices
import com.example.vibefinance.data.entity.isSystemCategory
import com.example.vibefinance.ui.common.bouncyClickable
import com.example.vibefinance.ui.home.CategoryIcon
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

// Data class for popular 1-tap subscription presets
data class SubscriptionPreset(
    val name: String,
    val defaultAmount: Double,
    val category: String,
    val frequency: String,
    val brandColor: Color,
    val emoji: String
)

val POPULAR_SUBSCRIPTION_PRESETS = listOf(
    SubscriptionPreset("Netflix", 93.00, "Entertainment", "Monthly", Color(0xFFE50914), "🎬"),
    SubscriptionPreset("Spotify", 68.00, "Entertainment", "Monthly", Color(0xFF1DB954), "🎵"),
    SubscriptionPreset("YouTube Premium", 78.00, "Entertainment", "Monthly", Color(0xFFFF0000), "▶️"),
    SubscriptionPreset("ChatGPT Plus", 160.00, "Software / AI", "Monthly", Color(0xFF10A37F), "🤖"),
    SubscriptionPreset("iCloud+", 23.00, "Utilities", "Monthly", Color(0xFF3B82F6), "☁️"),
    SubscriptionPreset("Google One", 15.00, "Utilities", "Monthly", Color(0xFF4285F4), "📦"),
    SubscriptionPreset("Amazon Prime", 38.00, "Shopping", "Monthly", Color(0xFFFF9900), "🛒"),
    SubscriptionPreset("Fitness Club", 450.00, "Fitness", "Monthly", Color(0xFFF97316), "🏋️"),
    SubscriptionPreset("Phone & Broadband", 198.00, "Utilities", "Monthly", Color(0xFF8B5CF6), "📶")
)

enum class RecurringFilter(val label: String) {
    ALL("All"),
    DUE_SOON("⚡ Due Soon"),
    MONTHLY("Monthly"),
    INSTALLMENTS("Installments"),
    YEARLY("Yearly"),
    WEEKLY("Weekly")
}

data class InstallmentPlan(
    val groupId: String,
    val description: String,
    val category: String,
    val accountId: Long,
    val totalInstallments: Int,
    val paidInstallments: Int,
    val monthlyAmount: Double,
    val totalAmount: Double,
    val remainingAmount: Double,
    val nextDueDate: Long?,
    val isCompleted: Boolean,
    val transactions: List<TransactionEntity> = emptyList()
)

fun extractInstallmentPlans(transactions: List<TransactionEntity>): List<InstallmentPlan> {
    val grouped = transactions.filter { it.groupId != null && it.totalInstallments != null }
        .groupBy { it.groupId!! }

    val now = System.currentTimeMillis()
    return grouped.map { (groupId, txs) ->
        val sorted = txs.sortedBy { it.installmentNumber ?: 0 }
        val first = sorted.first()
        val totalCount = first.totalInstallments ?: sorted.size
        val paidTxs = sorted.filter { it.timestamp <= now }
        val unpaidTxs = sorted.filter { it.timestamp > now }
        val monthly = first.amount
        val total = monthly * totalCount
        val remaining = unpaidTxs.sumOf { it.amount }
        val nextDue = unpaidTxs.minByOrNull { it.timestamp }?.timestamp
        val isCompleted = unpaidTxs.isEmpty()

        InstallmentPlan(
            groupId = groupId,
            description = first.description.ifBlank { "Installment Plan" },
            category = first.category,
            accountId = first.accountId,
            totalInstallments = totalCount,
            paidInstallments = paidTxs.size,
            monthlyAmount = monthly,
            totalAmount = total,
            remainingAmount = remaining,
            nextDueDate = nextDue,
            isCompleted = isCompleted,
            transactions = sorted
        )
    }.sortedBy { it.nextDueDate ?: Long.MAX_VALUE }
}

data class RecurringCommitment(
    val monthly: Double,
    val annual: Double,
    val dailyImpact: Double
)

fun calculateRecurringCommitment(
    subscriptions: List<SubscriptionEntity>,
    installments: List<InstallmentPlan> = emptyList()
): RecurringCommitment {
    val subMonthly = subscriptions.sumOf { sub ->
        when (sub.frequency.lowercase(Locale.US)) {
            "weekly" -> sub.amount * 4.333
            "yearly", "annual" -> sub.amount / 12.0
            else -> sub.amount
        }
    }
    val instMonthly = installments.filter { !it.isCompleted }.sumOf { it.monthlyAmount }
    val totalMonthly = subMonthly + instMonthly
    return RecurringCommitment(
        monthly = totalMonthly,
        annual = totalMonthly * 12.0,
        dailyImpact = totalMonthly / 30.0
    )
}

fun filterSubscriptions(
    subscriptions: List<SubscriptionEntity>,
    filter: RecurringFilter,
    today: LocalDate
): List<SubscriptionEntity> {
    if (filter == RecurringFilter.INSTALLMENTS) return emptyList()
    return subscriptions.filter { sub ->
        val paymentLocalDate = Instant.ofEpochMilli(sub.nextPaymentDate)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val daysUntil = ChronoUnit.DAYS.between(today, paymentLocalDate).toInt()

        when (filter) {
            RecurringFilter.ALL -> true
            RecurringFilter.DUE_SOON -> daysUntil in -30..7
            RecurringFilter.MONTHLY -> sub.frequency.equals("Monthly", ignoreCase = true)
            RecurringFilter.YEARLY -> sub.frequency.equals("Yearly", ignoreCase = true) || sub.frequency.equals("Annual", ignoreCase = true)
            RecurringFilter.WEEKLY -> sub.frequency.equals("Weekly", ignoreCase = true)
            RecurringFilter.INSTALLMENTS -> false
        }
    }.sortedBy { it.nextPaymentDate }
}

fun filterInstallments(
    installments: List<InstallmentPlan>,
    filter: RecurringFilter,
    today: LocalDate = LocalDate.now()
): List<InstallmentPlan> {
    return when (filter) {
        RecurringFilter.ALL -> installments.filter { !it.isCompleted }
        RecurringFilter.INSTALLMENTS -> installments
        RecurringFilter.DUE_SOON -> installments.filter { plan ->
            if (plan.isCompleted || plan.nextDueDate == null) false
            else {
                val dueDate = Instant.ofEpochMilli(plan.nextDueDate).atZone(ZoneId.systemDefault()).toLocalDate()
                ChronoUnit.DAYS.between(today, dueDate) in -30..7
            }
        }
        RecurringFilter.MONTHLY,
        RecurringFilter.YEARLY,
        RecurringFilter.WEEKLY -> emptyList()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringScreen(
    state: FinanceUiState,
    onIntent: (FinanceIntent) -> Unit,
    modifier: Modifier = Modifier,
    topContentPadding: Dp = 16.dp,
    topVisibilityInset: Dp = 0.dp,
    bottomVisibilityInset: Dp = 0.dp,
    showAddSheet: Boolean = false,
    onDismissAddSheet: () -> Unit = {},
    listState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState(),
    bottomVisibilityInsetProvider: () -> Dp = { bottomVisibilityInset },
    editorState: SubscriptionEditorState = rememberSubscriptionEditorState(),
    renderEditor: Boolean = true,
) {
    val haptic = rememberVibeHapticFeedback()
    var pendingDeleteSub by remember { mutableStateOf<SubscriptionEntity?>(null) }

    LaunchedEffect(showAddSheet) {
        if (showAddSheet) {
            editorState.open()
            onDismissAddSheet()
        }
    }

    var selectedFilter by remember { mutableStateOf(RecurringFilter.ALL) }
    var isTimelineView by remember { mutableStateOf(false) }

    var activeSwipeId by remember { mutableStateOf<Long?>(null) }
    var activeSwipeOffset by remember { mutableStateOf(0f) }

    var activeSwipeInstallmentId by remember { mutableStateOf<String?>(null) }
    var activeSwipeInstallmentOffset by remember { mutableStateOf(0f) }

    val today = remember { LocalDate.now() }

    // Extract installment plans from transactions
    val installmentPlans = remember(state.transactions) {
        extractInstallmentPlans(state.transactions)
    }
    val activeInstallments = remember(installmentPlans) {
        installmentPlans.filter { !it.isCompleted }
    }

    // Calculate unified monthly commitment (subscriptions + active installments)
    val commitment = remember(state.subscriptions, activeInstallments) {
        calculateRecurringCommitment(state.subscriptions, activeInstallments)
    }
    val totalMonthlyRecurring = commitment.monthly
    val totalAnnualRecurring = commitment.annual
    val dailyImpact = commitment.dailyImpact

    // Filtered subscriptions & installments
    val filteredSubscriptions = remember(state.subscriptions, selectedFilter, today) {
        filterSubscriptions(state.subscriptions, selectedFilter, today)
    }
    val filteredInstallments = remember(installmentPlans, selectedFilter, today) {
        filterInstallments(installmentPlans, selectedFilter, today)
    }

    // Earliest upcoming subscription for Alert Banner
    val earliestUpcoming = remember(state.subscriptions) {
        state.subscriptions.minByOrNull { it.nextPaymentDate }
    }

    ContentEntranceViewport(
        modifier = modifier.fillMaxSize(),
        topVisibilityInset = topVisibilityInset,
        bottomVisibilityInset = bottomVisibilityInset,
        bottomVisibilityInsetProvider = bottomVisibilityInsetProvider,
        tagPrefix = "RecurringArrival"
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWideScreen = maxWidth >= 600.dp

            if (!isWideScreen) {
                // Compact Single Column Layout (< 600dp)
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .testTag("RecurringList"),
                    contentPadding = PaddingValues(top = topContentPadding),
                    verticalArrangement = Arrangement.Top
                ) {
                    // 1. Bento Hero Summary Card
                    item {
                        PageContentEntrance("summary") {
                            RecurringHeroSummaryCard(
                                headerActions = { CalendarExportMenu(state) },
                                totalMonthly = totalMonthlyRecurring,
                                totalAnnual = totalAnnualRecurring,
                                dailyImpact = dailyImpact,
                                subscriptionCount = state.subscriptions.size + activeInstallments.size,
                                earliestUpcoming = earliestUpcoming,
                                subscriptions = state.subscriptions,
                                onAddClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    editorState.open()
                                },
                                modifier = Modifier.padding(bottom = 14.dp)
                            )
                        }
                    }

                    item(key = "income-estimate") {
                        PageContentEntrance("income-estimate") {
                            com.example.vibefinance.ui.components.PeriodIncomeEstimateCard(state,
                                Modifier.padding(bottom = 14.dp))
                        }
                    }

                    // 1.5 Recurring Category Donut Chart
                    item {
                        PageContentEntrance("category-chart") {
                            RecurringCategoryDonutChart(
                                subscriptions = state.subscriptions,
                                installments = activeInstallments,
                                modifier = Modifier.padding(bottom = 14.dp)
                            )
                        }
                    }

                    // 2. Filter & View Mode Controls
                    item {
                        PageContentEntrance("controls") {
                            FilterAndControlsRow(
                                selectedFilter = selectedFilter,
                                onFilterSelected = { selectedFilter = it },
                                isTimelineView = isTimelineView,
                                onToggleTimeline = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    isTimelineView = !isTimelineView
                                },
                                subscriptions = state.subscriptions,
                                installments = installmentPlans,
                                today = today,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }

                    // 4. Subscriptions & Installments List / Timeline View
                    subscriptionItemsSection(
                        filteredSubscriptions = filteredSubscriptions,
                        filteredInstallments = filteredInstallments,
                        accounts = state.accounts,
                        isTimelineView = isTimelineView,
                        today = today,
                        activeSwipeId = activeSwipeId,
                        activeSwipeOffset = activeSwipeOffset,
                        onSwipeChange = { id, off -> activeSwipeId = id; activeSwipeOffset = off },
                        activeSwipeGroupId = activeSwipeInstallmentId,
                        activeSwipeGroupOffset = activeSwipeInstallmentOffset,
                        onSwipeGroupChange = { id, off -> activeSwipeInstallmentId = id; activeSwipeInstallmentOffset = off },
                        onEdit = { sub ->
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            editorState.open(sub = sub)
                        },
                        onDelete = { sub ->
                            pendingDeleteSub = sub
                        },
                        onDeleteInstallment = { groupId, accountId ->
                            onIntent(FinanceIntent.DeleteInstallmentGroup(groupId, accountId))
                        },
                        onAddNew = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            editorState.open()
                        }
                    )

                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            } else {
                // Wide Dual-Pane Layout (>= 600dp)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 24.dp, end = 24.dp, top = topContentPadding)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Left Pane: Sticky / Scrollable Analytics Column (weight 0.45f)
                        ContentEntranceViewport(modifier = Modifier.weight(0.45f).fillMaxHeight()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(bottom = 100.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                PageContentEntrance("summary") {
                                    RecurringHeroSummaryCard(
                                        headerActions = { CalendarExportMenu(state) },
                                        totalMonthly = totalMonthlyRecurring,
                                        totalAnnual = totalAnnualRecurring,
                                        dailyImpact = dailyImpact,
                                        subscriptionCount = state.subscriptions.size + activeInstallments.size,
                                        earliestUpcoming = earliestUpcoming,
                                        subscriptions = state.subscriptions,
                                        onAddClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            editorState.open()
                                        }
                                    )
                                }

                                PageContentEntrance("income-estimate") {
                                    com.example.vibefinance.ui.components.PeriodIncomeEstimateCard(state)
                                }

                                PageContentEntrance("category-chart") {
                                    RecurringCategoryDonutChart(
                                        subscriptions = state.subscriptions,
                                        installments = activeInstallments
                                    )
                                }

                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        editorState.open()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.btn_add_subscription), fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Right Pane: Filter & Timeline/List (weight 0.55f)
                        Column(
                            modifier = Modifier
                                .weight(0.55f)
                                .fillMaxHeight()
                        ) {
                            PageContentEntrance("controls") {
                                FilterAndControlsRow(
                                    selectedFilter = selectedFilter,
                                    onFilterSelected = { selectedFilter = it },
                                    isTimelineView = isTimelineView,
                                    onToggleTimeline = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        isTimelineView = !isTimelineView
                                    },
                                    subscriptions = state.subscriptions,
                                    installments = installmentPlans,
                                    today = today,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            }

                            ContentEntranceViewport(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .testTag("RecurringList"),
                                    contentPadding = PaddingValues(bottom = 100.dp),
                                    verticalArrangement = Arrangement.Top
                                ) {
                                    subscriptionItemsSection(
                                        filteredSubscriptions = filteredSubscriptions,
                                        filteredInstallments = filteredInstallments,
                                        accounts = state.accounts,
                                        isTimelineView = isTimelineView,
                                        today = today,
                                        activeSwipeId = activeSwipeId,
                                        activeSwipeOffset = activeSwipeOffset,
                                        onSwipeChange = { id, off -> activeSwipeId = id; activeSwipeOffset = off },
                                        activeSwipeGroupId = activeSwipeInstallmentId,
                                        activeSwipeGroupOffset = activeSwipeInstallmentOffset,
                                        onSwipeGroupChange = { id, off -> activeSwipeInstallmentId = id; activeSwipeInstallmentOffset = off },
                                        onEdit = { sub ->
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            editorState.open(sub = sub)
                                        },
                                        onDelete = { sub ->
                                            pendingDeleteSub = sub
                                        },
                                        onDeleteInstallment = { groupId, accountId ->
                                            onIntent(FinanceIntent.DeleteInstallmentGroup(groupId, accountId))
                                        },
                                        onAddNew = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            editorState.open()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (renderEditor) {
        SubscriptionEntryHost(
            visible = editorState.isOpen,
            state = state,
            editorState = editorState,
            onIntent = onIntent,
            onDismiss = {
                editorState.close()
                onDismissAddSheet()
            }
        )
    }

    // Confirmation Dialog for Subscription Deletion
    pendingDeleteSub?.let { sub ->
        DeleteSubscriptionConfirmDialog(
            subscription = sub,
            transactions = state.transactions,
            accounts = state.accounts,
            onDismiss = { pendingDeleteSub = null },
            onConfirm = { deletePastTransactions ->
                onIntent(FinanceIntent.DeleteSubscription(sub, deletePastTransactions))
                pendingDeleteSub = null
            }
        )
    }
}


@Composable
internal fun recurringDateLabel(date: LocalDate, full: Boolean = false): String {
    val locale = LocalConfiguration.current.locales[0]
    val pattern = android.text.format.DateFormat.getBestDateTimePattern(locale, if (full) "yMMMMEEEEd" else "MMMd")
    return date.format(DateTimeFormatter.ofPattern(pattern, locale))
}

@Composable
private fun recurringFrequencySuffix(frequency: String): String = when (frequency.lowercase(Locale.ROOT)) {
    "monthly" -> stringResource(R.string.ui_recurring_per_month)
    "yearly", "annual" -> stringResource(R.string.ui_recurring_per_year)
    "weekly" -> stringResource(R.string.ui_recurring_per_week)
    else -> "/$frequency"
}

@Composable
private fun recurringFilterLabel(filter: RecurringFilter): String = stringResource(when (filter) {
    RecurringFilter.ALL -> R.string.filter_all
    RecurringFilter.DUE_SOON -> R.string.filter_due_soon
    RecurringFilter.MONTHLY -> R.string.filter_monthly
    RecurringFilter.INSTALLMENTS -> R.string.filter_installments
    RecurringFilter.YEARLY -> R.string.filter_yearly
    RecurringFilter.WEEKLY -> R.string.filter_weekly
})

@Composable
internal fun recurringPresetLabel(preset: SubscriptionPreset): String = when (preset.name) {
    "Fitness Club" -> stringResource(R.string.ui_recurring_preset_fitness)
    "Phone & Broadband" -> stringResource(R.string.ui_recurring_preset_phone)
    else -> preset.name
}

// -------------------------------------------------------------
// MODULAR RECURRING SUBCOMPONENTS
// -------------------------------------------------------------

@Composable
fun FilterAndControlsRow(
    selectedFilter: RecurringFilter,
    onFilterSelected: (RecurringFilter) -> Unit,
    isTimelineView: Boolean,
    onToggleTimeline: () -> Unit,
    subscriptions: List<SubscriptionEntity>,
    installments: List<InstallmentPlan> = emptyList(),
    today: LocalDate,
    modifier: Modifier = Modifier
) {
    val haptic = rememberVibeHapticFeedback()
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val connectedFilters = listOf(
            RecurringFilter.ALL,
            RecurringFilter.DUE_SOON,
            RecurringFilter.MONTHLY,
            RecurringFilter.INSTALLMENTS
        )
        val selectedIndex = connectedFilters.indexOf(selectedFilter).let { if (it >= 0) it else 0 }

        val strAll = stringResource(R.string.filter_all)
        val strDueSoon = stringResource(R.string.filter_due_soon)
        val strMonthly = stringResource(R.string.filter_monthly)
        val strInstallments = stringResource(R.string.filter_installments)

        ConnectedButtonGroup(
            items = connectedFilters,
            selectedIndex = selectedIndex,
            onItemSelected = { index ->
                onFilterSelected(connectedFilters[index])
            },
            modifier = Modifier.weight(1f),
            labelProvider = { filter ->
                val count = when (filter) {
                    RecurringFilter.ALL -> subscriptions.size + installments.count { !it.isCompleted }
                    RecurringFilter.DUE_SOON -> {
                        val subDue = subscriptions.count {
                            val due = Instant.ofEpochMilli(it.nextPaymentDate).atZone(ZoneId.systemDefault()).toLocalDate()
                            ChronoUnit.DAYS.between(today, due) in -30..7
                        }
                        val instDue = installments.count { plan ->
                            if (plan.isCompleted || plan.nextDueDate == null) false
                            else {
                                val due = Instant.ofEpochMilli(plan.nextDueDate).atZone(ZoneId.systemDefault()).toLocalDate()
                                ChronoUnit.DAYS.between(today, due) in -30..7
                            }
                        }
                        subDue + instDue
                    }
                    RecurringFilter.MONTHLY -> subscriptions.count { it.frequency.equals("Monthly", true) }
                    RecurringFilter.INSTALLMENTS -> installments.size
                    RecurringFilter.YEARLY -> subscriptions.count { it.frequency.equals("Yearly", true) || it.frequency.equals("Annual", true) }
                    RecurringFilter.WEEKLY -> subscriptions.count { it.frequency.equals("Weekly", true) }
                }
                when (filter) {
                    RecurringFilter.ALL -> "$strAll ($count)"
                    RecurringFilter.DUE_SOON -> "$strDueSoon ($count)"
                    RecurringFilter.MONTHLY -> "$strMonthly ($count)"
                    RecurringFilter.INSTALLMENTS -> "$strInstallments ($count)"
                    else -> "${recurringFilterLabel(filter)} ($count)"
                }
            }
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Timeline / List View Toggle
        Surface(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onToggleTimeline()
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.clip(RoundedCornerShape(12.dp)),
            color = if (isTimelineView) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            border = BorderStroke(
                1.dp,
                if (isTimelineView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = if (isTimelineView) Icons.Default.ViewAgenda else Icons.Default.CalendarMonth,
                    contentDescription = stringResource(R.string.ui_recurring_toggle_timeline),
                    tint = if (isTimelineView) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

fun LazyListScope.subscriptionItemsSection(
    filteredSubscriptions: List<SubscriptionEntity>,
    filteredInstallments: List<InstallmentPlan> = emptyList(),
    accounts: List<AccountEntity>,
    isTimelineView: Boolean,
    today: LocalDate,
    activeSwipeId: Long?,
    activeSwipeOffset: Float,
    onSwipeChange: (Long?, Float) -> Unit,
    activeSwipeGroupId: String? = null,
    activeSwipeGroupOffset: Float = 0f,
    onSwipeGroupChange: (String?, Float) -> Unit = { _, _ -> },
    onEdit: (SubscriptionEntity) -> Unit,
    onDelete: (SubscriptionEntity) -> Unit,
    onDeleteInstallment: (String, Long) -> Unit = { _, _ -> },
    onAddNew: () -> Unit
) {
    if (filteredSubscriptions.isEmpty() && filteredInstallments.isEmpty()) {
        item {
            Spacer(modifier = Modifier.height(20.dp))
            PageContentEntrance("empty") {
                EmptyRecurringCard(
                    onAddNew = onAddNew
                )
            }
        }
    } else if (isTimelineView) {
        // Timeline Schedule View Grouped
        val dueNext7DaysSubs = filteredSubscriptions.filter {
            val pDate = Instant.ofEpochMilli(it.nextPaymentDate).atZone(ZoneId.systemDefault()).toLocalDate()
            ChronoUnit.DAYS.between(today, pDate) in -30..7
        }
        val dueNext7DaysInst = filteredInstallments.filter {
            !it.isCompleted && it.nextDueDate != null && ChronoUnit.DAYS.between(today, Instant.ofEpochMilli(it.nextDueDate).atZone(ZoneId.systemDefault()).toLocalDate()) in -30..7
        }

        val dueLaterThisMonthSubs = filteredSubscriptions.filter {
            val pDate = Instant.ofEpochMilli(it.nextPaymentDate).atZone(ZoneId.systemDefault()).toLocalDate()
            val days = ChronoUnit.DAYS.between(today, pDate)
            days in 8..30
        }
        val dueLaterThisMonthInst = filteredInstallments.filter {
            !it.isCompleted && it.nextDueDate != null && ChronoUnit.DAYS.between(today, Instant.ofEpochMilli(it.nextDueDate).atZone(ZoneId.systemDefault()).toLocalDate()) in 8..30
        }

        val dueLaterSubs = filteredSubscriptions.filter {
            val pDate = Instant.ofEpochMilli(it.nextPaymentDate).atZone(ZoneId.systemDefault()).toLocalDate()
            ChronoUnit.DAYS.between(today, pDate) > 30
        }
        val dueLaterInst = filteredInstallments.filter {
            !it.isCompleted && it.nextDueDate != null && ChronoUnit.DAYS.between(today, Instant.ofEpochMilli(it.nextDueDate).atZone(ZoneId.systemDefault()).toLocalDate()) > 30
        }

        if (dueNext7DaysSubs.isNotEmpty() || dueNext7DaysInst.isNotEmpty()) {
            val total7 = dueNext7DaysSubs.sumOf { it.amount } + dueNext7DaysInst.sumOf { it.monthlyAmount }
            item {
                PageContentEntrance("heading:due-soon") {
                    SubscriptionGroupHeader(
                        title = stringResource(R.string.section_due_7_days),
                        badgeText = stringResource(R.string.ui_recurring_count_due, dueNext7DaysSubs.size + dueNext7DaysInst.size),
                        isInActivePeriod = true
                    )
                }
            }
            itemsIndexed(dueNext7DaysSubs, key = { _, sub -> "sub_${sub.id}" }) { idx, sub ->
                SubscriptionRowItem(
                    subscription = sub,
                    accounts = accounts,
                    index = idx,
                    sectionItems = dueNext7DaysSubs,
                    activeSwipeId = activeSwipeId,
                    activeSwipeOffset = activeSwipeOffset,
                    onSwipeChange = onSwipeChange,
                    onClick = { onEdit(sub) },
                    onDelete = { onDelete(sub) }
                )
            }
            itemsIndexed(dueNext7DaysInst, key = { _, plan -> "inst_${plan.groupId}" }) { idx, plan ->
                InstallmentRowItem(
                    plan = plan,
                    accounts = accounts,
                    index = idx,
                    sectionItems = dueNext7DaysInst,
                    activeSwipeGroupId = activeSwipeGroupId,
                    activeSwipeGroupOffset = activeSwipeGroupOffset,
                    onSwipeChange = onSwipeGroupChange,
                    onDelete = { onDeleteInstallment(plan.groupId, plan.accountId) }
                )
            }
            item {
                PageContentEntrance("total:due-soon") {
                    SubscriptionGroupFooter(total = total7)
                }
            }
        }

        if (dueLaterThisMonthSubs.isNotEmpty() || dueLaterThisMonthInst.isNotEmpty()) {
            val totalMonth = dueLaterThisMonthSubs.sumOf { it.amount } + dueLaterThisMonthInst.sumOf { it.monthlyAmount }
            item {
                PageContentEntrance("heading:this-month") {
                    SubscriptionGroupHeader(
                        title = stringResource(R.string.section_later_month),
                        badgeText = stringResource(R.string.ui_recurring_count_later, dueLaterThisMonthSubs.size + dueLaterThisMonthInst.size),
                        isInActivePeriod = false
                    )
                }
            }
            itemsIndexed(dueLaterThisMonthSubs, key = { _, sub -> "sub_${sub.id}" }) { idx, sub ->
                SubscriptionRowItem(
                    subscription = sub,
                    accounts = accounts,
                    index = idx,
                    sectionItems = dueLaterThisMonthSubs,
                    activeSwipeId = activeSwipeId,
                    activeSwipeOffset = activeSwipeOffset,
                    onSwipeChange = onSwipeChange,
                    onClick = { onEdit(sub) },
                    onDelete = { onDelete(sub) }
                )
            }
            itemsIndexed(dueLaterThisMonthInst, key = { _, plan -> "inst_${plan.groupId}" }) { idx, plan ->
                InstallmentRowItem(
                    plan = plan,
                    accounts = accounts,
                    index = idx,
                    sectionItems = dueLaterThisMonthInst,
                    activeSwipeGroupId = activeSwipeGroupId,
                    activeSwipeGroupOffset = activeSwipeGroupOffset,
                    onSwipeChange = onSwipeGroupChange,
                    onDelete = { onDeleteInstallment(plan.groupId, plan.accountId) }
                )
            }
            item {
                PageContentEntrance("total:this-month") {
                    SubscriptionGroupFooter(total = totalMonth)
                }
            }
        }

        if (dueLaterSubs.isNotEmpty() || dueLaterInst.isNotEmpty()) {
            val totalLater = dueLaterSubs.sumOf { it.amount } + dueLaterInst.sumOf { it.monthlyAmount }
            item {
                PageContentEntrance("heading:later") {
                    SubscriptionGroupHeader(
                        title = stringResource(R.string.section_next_month_beyond),
                        badgeText = stringResource(R.string.ui_recurring_count_upcoming, dueLaterSubs.size + dueLaterInst.size),
                        isInActivePeriod = false
                    )
                }
            }
            itemsIndexed(dueLaterSubs, key = { _, sub -> "sub_${sub.id}" }) { idx, sub ->
                SubscriptionRowItem(
                    subscription = sub,
                    accounts = accounts,
                    index = idx,
                    sectionItems = dueLaterSubs,
                    activeSwipeId = activeSwipeId,
                    activeSwipeOffset = activeSwipeOffset,
                    onSwipeChange = onSwipeChange,
                    onClick = { onEdit(sub) },
                    onDelete = { onDelete(sub) }
                )
            }
            itemsIndexed(dueLaterInst, key = { _, plan -> "inst_${plan.groupId}" }) { idx, plan ->
                InstallmentRowItem(
                    plan = plan,
                    accounts = accounts,
                    index = idx,
                    sectionItems = dueLaterInst,
                    activeSwipeGroupId = activeSwipeGroupId,
                    activeSwipeGroupOffset = activeSwipeGroupOffset,
                    onSwipeChange = onSwipeGroupChange,
                    onDelete = { onDeleteInstallment(plan.groupId, plan.accountId) }
                )
            }
            item {
                PageContentEntrance("total:later") {
                    SubscriptionGroupFooter(total = totalLater)
                }
            }
        }
    } else {
        // Standard List View
        if (filteredSubscriptions.isNotEmpty()) {
            val totalFiltered = filteredSubscriptions.sumOf { it.amount }
            item {
                PageContentEntrance("heading:subscriptions") {
                    SubscriptionGroupHeader(
                        title = stringResource(R.string.section_all_subscriptions),
                        badgeText = stringResource(R.string.active_count_format, filteredSubscriptions.size),
                        isInActivePeriod = true
                    )
                }
            }
            itemsIndexed(filteredSubscriptions, key = { _, sub -> "sub_${sub.id}" }) { idx, sub ->
                SubscriptionRowItem(
                    subscription = sub,
                    accounts = accounts,
                    index = idx,
                    sectionItems = filteredSubscriptions,
                    activeSwipeId = activeSwipeId,
                    activeSwipeOffset = activeSwipeOffset,
                    onSwipeChange = onSwipeChange,
                    onClick = { onEdit(sub) },
                    onDelete = { onDelete(sub) }
                )
            }
            item {
                PageContentEntrance("total:subscriptions") {
                    SubscriptionGroupFooter(total = totalFiltered)
                }
            }
        }

        if (filteredInstallments.isNotEmpty()) {
            val totalInstMonthly = filteredInstallments.filter { !it.isCompleted }.sumOf { it.monthlyAmount }
            item {
                PageContentEntrance("heading:installments") {
                    SubscriptionGroupHeader(
                        title = stringResource(R.string.section_installment_plans),
                        badgeText = stringResource(R.string.ui_recurring_count_plans, filteredInstallments.size),
                        isInActivePeriod = true
                    )
                }
            }
            itemsIndexed(filteredInstallments, key = { _, plan -> "inst_${plan.groupId}" }) { idx, plan ->
                InstallmentRowItem(
                    plan = plan,
                    accounts = accounts,
                    index = idx,
                    sectionItems = filteredInstallments,
                    activeSwipeGroupId = activeSwipeGroupId,
                    activeSwipeGroupOffset = activeSwipeGroupOffset,
                    onSwipeChange = onSwipeGroupChange,
                    onDelete = { onDeleteInstallment(plan.groupId, plan.accountId) }
                )
            }
            item {
                PageContentEntrance("total:installments") {
                    SubscriptionGroupFooter(total = totalInstMonthly)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// BENTO HERO SUMMARY COMPONENT
// -------------------------------------------------------------
@Composable
fun RecurringHeroSummaryCard(
    totalMonthly: Double,
    totalAnnual: Double,
    dailyImpact: Double,
    subscriptionCount: Int,
    earliestUpcoming: SubscriptionEntity?,
    subscriptions: List<SubscriptionEntity>,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    headerActions: @Composable () -> Unit = {}
) {
    val today = remember { LocalDate.now() }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Top Status / Next Bill Alert
            if (earliestUpcoming != null) {
                val pDate = Instant.ofEpochMilli(earliestUpcoming.nextPaymentDate).atZone(ZoneId.systemDefault()).toLocalDate()
                val daysUntil = ChronoUnit.DAYS.between(today, pDate).toInt()

                val (alertBg, alertFg, alertText) = when {
                    daysUntil < 0 -> Triple(Color(0xFFFFEBEE), Color(0xFFD32F2F), stringResource(R.string.ui_recurring_alert_overdue, earliestUpcoming.name, earliestUpcoming.amount))
                    daysUntil == 0 -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), stringResource(R.string.ui_recurring_alert_today, earliestUpcoming.name, earliestUpcoming.amount))
                    daysUntil == 1 -> Triple(Color(0xFFFFF8E1), Color(0xFFF57F17), stringResource(R.string.ui_recurring_alert_tomorrow, earliestUpcoming.name, earliestUpcoming.amount))
                    else -> Triple(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        MaterialTheme.colorScheme.onPrimaryContainer,
                        stringResource(R.string.ui_recurring_alert_in_days, daysUntil, earliestUpcoming.name, earliestUpcoming.amount)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = alertBg,
                    border = BorderStroke(1.dp, alertFg.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = alertText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = alertFg,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Monthly Recurring Header
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.est_monthly_commitment),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.weight(1f)
                )
                headerActions()
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RollingNumberText(
                    text = String.format(Locale.US, "HK$ %,.2f", totalMonthly),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Black
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.active_count_format, subscriptionCount),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-metrics (Yearly & Daily estimates)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.yearly_total_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    RollingNumberText(
                        text = stringResource(R.string.ui_recurring_annual_cost, totalAnnual),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                Column {
                    Text(
                        text = stringResource(R.string.daily_cost_impact),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    RollingNumberText(
                        text = stringResource(R.string.ui_recurring_daily_cost, dailyImpact),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Distinct semantic palette designed specifically for recurring category breakdown charts.
 * Spans wide-spectrum hues with high contrast in both dark and light modes, avoiding
 * adjacent or confusing shades of green/teal.
 */
val RECURRING_CHART_PALETTE = ChartColors.getFallbackPalette(isDark = true)

fun getSemanticCategoryChartColor(category: String, isDark: Boolean = true): Color? =
    ChartColors.getSemanticCategoryColor(category, isDark)

fun buildCategoryColorMap(
    categories: List<String>,
    isDark: Boolean = true,
    primaryColor: Color = Color(0xFF00E676)
): Map<String, Color> =
    ChartColors.buildCategoryColorMap(categories, isDark, primaryColor)

@Composable
fun RecurringCategoryDonutChart(
    subscriptions: List<SubscriptionEntity>,
    installments: List<InstallmentPlan> = emptyList(),
    modifier: Modifier = Modifier
) {
    val categorySpending = remember(subscriptions, installments) {
        val map = mutableMapOf<String, Double>()
        subscriptions.forEach { sub ->
            val monthlyAmt = when (sub.frequency.lowercase(Locale.US)) {
                "weekly" -> sub.amount * 4.333
                "yearly", "annual" -> sub.amount / 12.0
                else -> sub.amount
            }
            if (monthlyAmt > 0) {
                map[sub.category] = (map[sub.category] ?: 0.0) + monthlyAmt
            }
        }
        installments.filter { !it.isCompleted }.forEach { inst ->
            if (inst.monthlyAmount > 0) {
                map[inst.category] = (map[inst.category] ?: 0.0) + inst.monthlyAmount
            }
        }
        map.toList().sortedByDescending { it.second }
    }

    val totalSpending = remember(categorySpending) {
        categorySpending.sumOf { it.second }
    }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f || isSystemInDarkTheme()
    val primaryColor = MaterialTheme.colorScheme.primary

    val categoryColors = remember(categorySpending, isDark, primaryColor) {
        ChartColors.buildCategoryColorMap(
            categories = categorySpending.map { it.first },
            isDark = isDark,
            primaryColor = primaryColor
        )
    }

    var selectedCategory by remember { mutableStateOf<String?>(null) }

    // Animate stroke width and alpha for each category segment on focus / unfocus
    val isAnyFocused = selectedCategory != null
    val categoryAnimProps = categorySpending.associate { (cat, _) ->
        val isFocused = selectedCategory == cat
        val targetStroke = if (isFocused) 24.dp else if (isAnyFocused) 12.dp else 16.dp
        val targetAlpha = if (isFocused || !isAnyFocused) 1.0f else 0.22f

        val strokeState = animateDpAsState(
            targetValue = targetStroke,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
            label = "stroke_$cat"
        )
        val alphaState = animateFloatAsState(
            targetValue = targetAlpha,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            label = "alpha_$cat"
        )
        cat to (strokeState to alphaState)
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow))
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.category_analytics_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                AnimatedVisibility(
                    visible = selectedCategory != null,
                    enter = fadeIn(animationSpec = MotionTokens.standardEnter()) + expandVertically(animationSpec = MotionTokens.emphasizedEnter()),
                    exit = fadeOut(animationSpec = MotionTokens.standardExit()) + shrinkVertically(animationSpec = MotionTokens.emphasizedExit())
                ) {
                    Text(
                        text = stringResource(R.string.reset_filter),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedCategory = null }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Interactive Animated Donut Chart Canvas
                Box(
                    modifier = Modifier.size(118.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (totalSpending == 0.0) {
                            drawArc(
                                color = Color.Gray.copy(alpha = 0.15f),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                            )
                        } else {
                            val gapAngle = if (categorySpending.size > 1) 3.5f else 0f
                            var startAngle = -90f
                            categorySpending.forEach { (category, amount) ->
                                val rawSweep = ((amount / totalSpending) * 360f).toFloat()
                                val sweepAngle = (rawSweep - gapAngle).coerceAtLeast(1.5f)
                                val color = categoryColors[category] ?: Color.Gray

                                val (strokeState, alphaState) = categoryAnimProps[category]
                                    ?: (mutableStateOf(16.dp) to mutableStateOf(1f))

                                val animatedStrokePx = strokeState.value.toPx()
                                val animatedAlpha = alphaState.value

                                drawArc(
                                    color = color.copy(alpha = animatedAlpha),
                                    startAngle = startAngle + (gapAngle / 2f),
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    style = Stroke(width = animatedStrokePx, cap = StrokeCap.Round)
                                )
                                startAngle += rawSweep
                            }
                        }
                    }

                    // Animated Center Content transition on focus/unfocus
                    AnimatedContent(
                        targetState = selectedCategory,
                        transitionSpec = {
                            (fadeIn(animationSpec = MotionTokens.standardEnter(220)) + scaleIn(animationSpec = MotionTokens.emphasizedEnter(220), initialScale = 0.85f))
                                .togetherWith(fadeOut(animationSpec = MotionTokens.standardExit(180)) + scaleOut(animationSpec = MotionTokens.emphasizedExit(180), targetScale = 0.85f))
                        },
                        label = "centerDonutText"
                    ) { currentCategory ->
                        val selectedItem = categorySpending.find { it.first == currentCategory }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (selectedItem != null) {
                                val percent = if (totalSpending > 0) (selectedItem.second / totalSpending * 100).toInt() else 0
                                val itemColor = categoryColors[selectedItem.first] ?: MaterialTheme.colorScheme.primary
                                Text(
                                    text = getCategoryDisplayName(selectedItem.first),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = itemColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                RollingNumberText(
                                    text = String.format(Locale.US, "HK$ %.0f", selectedItem.second),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = itemColor.copy(alpha = 0.18f),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text = "$percent%",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        color = itemColor,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = stringResource(R.string.total_monthly_short),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                                RollingNumberText(
                                    text = String.format(Locale.US, "HK$ %.0f", totalSpending),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Legend with Animated Selection State
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (categorySpending.isEmpty()) {
                        Text(
                            text = stringResource(R.string.ui_recurring_no_category_data),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    } else {
                        categorySpending.take(5).forEach { (category, amount) ->
                            val color = categoryColors[category] ?: Color.Gray
                            val isFocused = selectedCategory == category

                            val rowBgColor by animateColorAsState(
                                targetValue = if (isFocused) color.copy(alpha = 0.18f) else Color.Transparent,
                                animationSpec = tween(250),
                                label = "rowBg_$category"
                            )
                            val dotSize by animateDpAsState(
                                targetValue = if (isFocused) 14.dp else 10.dp,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = "dotSize_$category"
                            )

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        selectedCategory = if (isFocused) null else category
                                    },
                                color = rowBgColor,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(dotSize)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                        Text(
                                            text = getCategoryDisplayName(category),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isFocused) FontWeight.ExtraBold else FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val percent = if (totalSpending > 0) (amount / totalSpending * 100).toInt() else 0
                                        val formattedAmount = if (amount % 1.0 == 0.0) {
                                            String.format(Locale.US, "$%,.0f", amount)
                                        } else {
                                            String.format(Locale.US, "$%,.2f", amount)
                                        }
                                        Text(
                                            text = formattedAmount,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isFocused) color.copy(alpha = 0.22f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
                                        ) {
                                            Text(
                                                text = "$percent%",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                color = if (isFocused) color else MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Modifier.fullBleed(horizontalPadding: androidx.compose.ui.unit.Dp = 16.dp): Modifier = this.layout { measurable, constraints ->
    val paddingPx = horizontalPadding.roundToPx()
    val placeable = measurable.measure(
        constraints.copy(
            minWidth = constraints.minWidth + paddingPx * 2,
            maxWidth = constraints.maxWidth + paddingPx * 2
        )
    )
    layout(constraints.maxWidth, placeable.height) {
        placeable.placeRelative(-paddingPx, 0)
    }
}

// -------------------------------------------------------------
// SUBSCRIPTION GROUP HEADER & FOOTER (UNIFIED WITH HISTORY)
// -------------------------------------------------------------
@Composable
fun SubscriptionGroupHeader(
    title: String,
    badgeText: String,
    isInActivePeriod: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.lowercase(Locale.US),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
            fontWeight = FontWeight.Bold
        )

        Surface(
            shape = CircleShape,
            color = if (isInActivePeriod) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Text(
                text = badgeText,
                style = MaterialTheme.typography.labelSmall,
                color = if (isInActivePeriod) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun SubscriptionGroupFooter(
    total: Double,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, top = 6.dp, bottom = 14.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Text(
            text = stringResource(R.string.group_total_format, total),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium
        )
    }
}

// -------------------------------------------------------------
// SUBSCRIPTION ROW ITEM COMPONENT (UNIFIED WITH HISTORY)
// -------------------------------------------------------------
@Composable
fun androidx.compose.foundation.lazy.LazyItemScope.SubscriptionRowItem(
    subscription: SubscriptionEntity,
    accounts: List<AccountEntity>,
    index: Int,
    sectionItems: List<SubscriptionEntity>,
    activeSwipeId: Long?,
    activeSwipeOffset: Float,
    onSwipeChange: (Long?, Float) -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    val paymentLocalDate = Instant.ofEpochMilli(subscription.nextPaymentDate)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    val daysRemaining = ChronoUnit.DAYS.between(today, paymentLocalDate).toInt()

    val associatedAccount = accounts.find { it.id == subscription.accountId }
    val cardName = associatedAccount?.name ?: stringResource(R.string.ui_recurring_linked_account)

    // Match preset brand emoji or default
    val brandEmoji = POPULAR_SUBSCRIPTION_PRESETS.find {
        subscription.name.contains(it.name, ignoreCase = true)
    }?.emoji

    // Dynamic countdown badge text & color
    val (badgeBg, badgeFg, countdownText) = when {
        daysRemaining < 0 -> Triple(Color(0xFFFFEBEE), Color(0xFFD32F2F), stringResource(R.string.badge_overdue))
        daysRemaining == 0 -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), stringResource(R.string.badge_renews_today))
        daysRemaining == 1 -> Triple(Color(0xFFFFF8E1), Color(0xFFF57F17), stringResource(R.string.badge_renews_tomorrow))
        daysRemaining in 2..7 -> Triple(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), MaterialTheme.colorScheme.primary, stringResource(R.string.badge_renews_in_days, daysRemaining))
        else -> Triple(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f), MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), "🗓️ " + recurringDateLabel(paymentLocalDate))
    }

    // Android 16 Stacking & Geometry specifications
    val isSingle = sectionItems.size <= 1
    val isFirst = index == 0
    val isLast = index == sectionItems.size - 1
    val baseOuterRadius = 26.dp // 24px - 28px spec
    val baseInnerRadius = 8.dp  // 8px spec

    val baseTopStart = if (isSingle || isFirst) baseOuterRadius else baseInnerRadius
    val baseTopEnd = if (isSingle || isFirst) baseOuterRadius else baseInnerRadius
    val baseBottomStart = if (isSingle || isLast) baseOuterRadius else baseInnerRadius
    val baseBottomEnd = if (isSingle || isLast) baseOuterRadius else baseInnerRadius

    // Stable grouped container shape (Gmail / M3 Expressive)
    val itemShape = RoundedCornerShape(
        topStart = baseTopStart,
        topEnd = baseTopEnd,
        bottomStart = baseBottomStart,
        bottomEnd = baseBottomEnd
    )

    val transitionState = remember { MutableTransitionState(true) }

    LaunchedEffect(transitionState.currentState, transitionState.targetState) {
        if (!transitionState.currentState && !transitionState.targetState) {
            onDelete()
        }
    }

    AnimatedVisibility(
        visibleState = transitionState,
        exit = shrinkVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) + fadeOut(animationSpec = MotionTokens.standardExit(150)),
        modifier = modifier.animateItem(
            placementSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    ) {
        PageContentEntrance("subscription:${subscription.id}") {
            Column(modifier = Modifier.fillMaxWidth()) {
                ExpressiveSwipeRow(
                    shape = itemShape,
                    onEdit = onClick,
                    onDelete = { transitionState.targetState = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("SubscriptionRow_${subscription.id}")
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
                                shape = rememberIconShape("subscription.${subscription.id}"),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (brandEmoji != null) {
                                        Text(text = brandEmoji, fontSize = 20.sp)
                                    } else {
                                        CategoryIcon(
                                            category = subscription.category,
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
                                Text(
                                    text = subscription.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = "${getCategoryDisplayName(subscription.category)} • $cardName",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                // Status Tag Pill
                                Box(
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .clip(RoundedCornerShape(99.dp))
                                        .background(badgeBg)
                                        .border(1.dp, badgeFg.copy(alpha = 0.3f), RoundedCornerShape(99.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = countdownText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = badgeFg,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // 3. Trailing Content: Metric (Amount) & Secondary Metadata
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = String.format(Locale.US, "HK$ %,.2f", subscription.amount),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = recurringFrequencySuffix(subscription.frequency),
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
}

// -------------------------------------------------------------
// INSTALLMENT ROW ITEM COMPONENT (M3 EXPRESSIVE STACKING & PEELING)
// -------------------------------------------------------------
@Composable
fun androidx.compose.foundation.lazy.LazyItemScope.InstallmentRowItem(
    plan: InstallmentPlan,
    accounts: List<AccountEntity>,
    index: Int,
    sectionItems: List<InstallmentPlan>,
    activeSwipeGroupId: String?,
    activeSwipeGroupOffset: Float,
    onSwipeChange: (String?, Float) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    val associatedAccount = accounts.find { it.id == plan.accountId }
    val cardName = associatedAccount?.name ?: stringResource(R.string.ui_recurring_linked_card)

    // Next due date text & status
    val nextDueLocalDate = plan.nextDueDate?.let {
        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
    }
    val daysRemaining = if (nextDueLocalDate != null) ChronoUnit.DAYS.between(today, nextDueLocalDate).toInt() else null

    val (badgeBg, badgeFg, statusText) = when {
        plan.isCompleted -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), stringResource(R.string.ui_recurring_completed))
        daysRemaining != null && daysRemaining < 0 -> Triple(Color(0xFFFFEBEE), Color(0xFFD32F2F), stringResource(R.string.badge_overdue))
        daysRemaining != null && daysRemaining == 0 -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), stringResource(R.string.ui_recurring_due_today))
        daysRemaining != null && daysRemaining == 1 -> Triple(Color(0xFFFFF8E1), Color(0xFFF57F17), stringResource(R.string.ui_recurring_due_tomorrow))
        daysRemaining != null && daysRemaining in 2..7 -> Triple(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), MaterialTheme.colorScheme.primary, stringResource(R.string.ui_recurring_due_in_days, daysRemaining))
        nextDueLocalDate != null -> Triple(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f), MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), "🗓️ " + recurringDateLabel(nextDueLocalDate))
        else -> Triple(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f), MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), stringResource(R.string.ui_recurring_plan_active))
    }

    // Android 16 Stacking & Geometry specifications
    val isSingle = sectionItems.size <= 1
    val isFirst = index == 0
    val isLast = index == sectionItems.size - 1
    val baseOuterRadius = 26.dp
    val baseInnerRadius = 8.dp

    val baseTopStart = if (isSingle || isFirst) baseOuterRadius else baseInnerRadius
    val baseTopEnd = if (isSingle || isFirst) baseOuterRadius else baseInnerRadius
    val baseBottomStart = if (isSingle || isLast) baseOuterRadius else baseInnerRadius
    val baseBottomEnd = if (isSingle || isLast) baseOuterRadius else baseInnerRadius

    // Stable grouped container shape (Gmail / M3 Expressive)
    val itemShape = RoundedCornerShape(
        topStart = baseTopStart,
        topEnd = baseTopEnd,
        bottomStart = baseBottomStart,
        bottomEnd = baseBottomEnd
    )

    val transitionState = remember { MutableTransitionState(true) }

    LaunchedEffect(transitionState.currentState, transitionState.targetState) {
        if (!transitionState.currentState && !transitionState.targetState) {
            onDelete()
        }
    }

    AnimatedVisibility(
        visibleState = transitionState,
        exit = shrinkVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) + fadeOut(animationSpec = MotionTokens.standardExit(150)),
        modifier = modifier.animateItem(
            placementSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    ) {
        PageContentEntrance("installment:${plan.groupId}") {
            Column(modifier = Modifier.fillMaxWidth()) {
                ExpressiveSwipeRow(
                    shape = itemShape,
                    onEdit = {},
                    onDelete = { transitionState.targetState = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("InstallmentRow_${plan.groupId}")
                ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // 1. Leading Container
                                Surface(
                                    modifier = Modifier.size(44.dp),
                                    shape = rememberIconShape("installment.${plan.groupId}"),
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        CategoryIcon(
                                            category = plan.category,
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                // 2. Center Content
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = if (plan.description == "Installment Plan") stringResource(R.string.tab_installment) else plan.description,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${getCategoryDisplayName(plan.category)} • $cardName",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // 3. Trailing Amount
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = String.format(Locale.US, "HK$ %,.2f", plan.monthlyAmount),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = stringResource(R.string.ui_recurring_per_month_short),
                                        style = MaterialTheme.typography.bodySmall,
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

                            // Linear Progress Indicator
                            val progress = if (plan.totalInstallments > 0) {
                                (plan.paidInstallments.toFloat() / plan.totalInstallments.toFloat()).coerceIn(0f, 1f)
                            } else 0f

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )

                            // Bottom Pills Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    // Progress pill
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(99.dp))
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(99.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.ui_recurring_months_progress, plan.paidInstallments, plan.totalInstallments),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    // Remaining Debt pill
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(99.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(99.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.ui_recurring_amount_left, plan.remainingAmount),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Due status pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(99.dp))
                                        .background(badgeBg)
                                        .border(1.dp, badgeFg.copy(alpha = 0.3f), RoundedCornerShape(99.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = badgeFg,
                                        fontWeight = FontWeight.Bold
                                    )
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

// -------------------------------------------------------------
// EMPTY STATE COMPONENT
// -------------------------------------------------------------
@Composable
fun EmptyRecurringCard(
    onAddNew: () -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 32.dp,
        borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.ui_recurring_empty_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.ui_recurring_empty_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )


            Spacer(modifier = Modifier.height(18.dp))
            Button(onClick = onAddNew, shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.btn_add_subscription))
            }

        }
    }
}

// -------------------------------------------------------------
// MODAL BOTTOM SHEET FOR ADD / EDIT SUBSCRIPTION
// -------------------------------------------------------------
@Composable
fun LiveSubscriptionPreviewCard(
    name: String,
    amount: Double,
    category: String,
    frequency: String,
    accountName: String,
    nextPaymentDate: Long,
    presetEmoji: String?,
    presetColor: Color?,
    originalAmountText: String? = null,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    val paymentDate = remember(nextPaymentDate) {
        Instant.ofEpochMilli(nextPaymentDate).atZone(ZoneId.systemDefault()).toLocalDate()
    }
    val daysUntil = remember(today, paymentDate) {
        ChronoUnit.DAYS.between(today, paymentDate).toInt()
    }

    val (badgeBg, badgeFg, countdownText) = when {
        daysUntil < 0 -> Triple(Color(0xFFFFEBEE), Color(0xFFD32F2F), stringResource(R.string.ui_recurring_overdue_plain))
        daysUntil == 0 -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), stringResource(R.string.ui_recurring_renews_today_plain))
        daysUntil == 1 -> Triple(Color(0xFFFFF8E1), Color(0xFFF57F17), stringResource(R.string.ui_recurring_renews_tomorrow_plain))
        daysUntil in 2..7 -> Triple(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), MaterialTheme.colorScheme.primary, stringResource(R.string.ui_recurring_in_days_plain, daysUntil))
        else -> Triple(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f), MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), recurringDateLabel(paymentDate))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = (presetColor ?: MaterialTheme.colorScheme.primaryContainer).copy(alpha = 0.12f)
        ),
        border = BorderStroke(1.dp, (presetColor ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.ui_recurring_live_preview),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = presetColor ?: MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )

                Surface(
                    shape = CircleShape,
                    color = badgeBg,
                    border = BorderStroke(1.dp, badgeFg.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = countdownText,
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeFg,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Leading Emoji / Category Icon Avatar
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = rememberIconShape("subscription.preview"),
                        color = (presetColor ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, (presetColor ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (presetEmoji != null) {
                                Text(text = presetEmoji, fontSize = 20.sp)
                            } else {
                                CategoryIcon(
                                    category = category,
                                    tint = presetColor ?: MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    // Center Details
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = if (name.isNotBlank()) name else stringResource(R.string.ui_recurring_name_placeholder),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${getCategoryDisplayName(category)} • $accountName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Trailing Amount & Frequency
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        RollingNumberText(
                            text = String.format(Locale.US, "HK$ %,.2f", amount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val freqSuffix = recurringFrequencySuffix(frequency)
                        Text(
                            text = if (originalAmountText != null) "$originalAmountText • $freqSuffix" else freqSuffix,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Live Material 3 Expressive Preview Card for Installment Plans.
 */
@Composable
fun LiveInstallmentPreviewCard(
    name: String,
    totalAmount: Double,
    installments: Int,
    category: String,
    accountName: String,
    firstDueDate: Long,
    originalTotalText: String? = null,
    modifier: Modifier = Modifier
) {
    val monthlyAmount = if (installments > 0) totalAmount / installments else 0.0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.22f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.ui_recurring_installment_preview),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.tertiary,
                    letterSpacing = 1.sp
                )

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = stringResource(R.string.ui_recurring_plan_months, installments),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ViewTimeline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = if (name.isNotBlank()) name else stringResource(R.string.ui_recurring_item_placeholder),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${getCategoryDisplayName(category)} • $accountName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        RollingNumberText(
                            text = String.format(Locale.US, "HK$ %,.2f", monthlyAmount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        val totalHkdText = stringResource(R.string.ui_recurring_monthly_total, String.format(Locale.US, "HK$ %,.0f", totalAmount))
                        Text(
                            text = if (originalTotalText != null) "$originalTotalText • $totalHkdText" else totalHkdText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Resolves unified expense category choices matching Daily page Add Expense flow,
 * preserving usage frequency ranking and merge rules.
 */
fun resolveRecurringExpenseCategories(
    transactions: List<TransactionEntity> = emptyList(),
    subscriptions: List<SubscriptionEntity> = emptyList(),
    categoryLimits: Map<String, Double> = emptyMap(),
    categoryMergeRules: CategoryMergeRules = CategoryMergeRules(),
    extraCategories: List<String> = emptyList()
): List<String> {
    val categoryFrequency = transactions.groupingBy { it.category }.eachCount()
    val baseChoices = categoryChoices(
        CategoryKind.EXPENSE,
        transactions,
        subscriptions,
        categoryLimits,
        categoryMergeRules
    )
    val all = buildList {
        addAll(baseChoices)
        addAll(extraCategories)
    }.filter { it.isNotBlank() && !isSystemCategory(it) }
     .map { categoryMergeRules.resolve(it, CategoryKind.EXPENSE) }
     .distinct()

    return all.sortedWith(
        compareByDescending<String> { categoryFrequency[it] ?: 0 }
            .thenBy { it }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSubscriptionSheet(
    subscription: SubscriptionEntity?,
    initialPreset: SubscriptionPreset?,
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onSave: (SubscriptionEntity) -> Unit,
    onDelete: (SubscriptionEntity) -> Unit,
    transactions: List<TransactionEntity> = emptyList(),
    subscriptions: List<SubscriptionEntity> = emptyList(),
    categoryLimits: Map<String, Double> = emptyMap(),
    categoryMergeRules: CategoryMergeRules = CategoryMergeRules(),
    exchangeRateLoader: ExchangeRateLoader? = null,
    modifier: Modifier = Modifier,
    onSaveInstallment: (totalAmount: Double, category: String, accountId: Long, description: String, installments: Int, firstDueDate: Long) -> Unit = { _, _, _, _, _, _ -> },
    onAddNewAccount: ((AccountEntity) -> Unit)? = null
) {
    val haptic = rememberVibeHapticFeedback()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hapticView = LocalView.current
    val resources = context.resources

    // Currency & Exchange Rate State
    var selectedCurrency by rememberSaveable { mutableStateOf("HKD") }
    val defaultRateLoader = remember(context.applicationContext) {
        ExchangeRateRepository(File(context.applicationContext.cacheDir, "exchange_rates"))
    }
    val rateLoader = exchangeRateLoader ?: defaultRateLoader
    var rateResult by remember { mutableStateOf<ExchangeRateResult?>(null) }
    var rateLoading by remember { mutableStateOf(false) }
    var rateRefreshRequest by remember { mutableIntStateOf(0) }
    var lastRequestedCurrency by remember { mutableStateOf<String?>(null) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { rateRefreshRequest++ }

    LaunchedEffect(selectedCurrency, rateRefreshRequest, rateLoader) {
        val forceRefresh = lastRequestedCurrency == selectedCurrency
        lastRequestedCurrency = selectedCurrency
        rateResult = null
        rateLoading = selectedCurrency != "HKD"
        if (selectedCurrency != "HKD") {
            rateResult = rateLoader.load(selectedCurrency, forceRefresh)
            rateLoading = false
        }
    }

    val rateQuote = rateResult?.quote?.takeIf { it.currencyCode == selectedCurrency }
    val currencyInfo = remember(selectedCurrency) {
        CurrencyEngine.supportedCurrencies.firstOrNull { it.code == selectedCurrency }
            ?: CurrencyEngine.supportedCurrencies.first()
    }
    val currencySymbol = if (selectedCurrency == "HKD") "HK$" else currencyInfo.symbol

    fun convertToHkd(amount: Double?): Double? = amount?.let {
        if (selectedCurrency == "HKD") it else rateQuote?.let { quote ->
            runCatching { quote.convert(it) }.getOrNull()
        }
    }

    // 0: Subscription, 1: Installment Plan (only switchable when adding new)
    var selectedSheetTab by remember { mutableIntStateOf(0) }

    // Unified Expense Categories matching Daily Page Add Expense
    val initialExpenseCategories = remember(transactions, subscriptions, categoryLimits, categoryMergeRules) {
        val extra = POPULAR_SUBSCRIPTION_PRESETS.map { it.category } +
            listOfNotNull(subscription?.category, initialPreset?.category)
        resolveRecurringExpenseCategories(
            transactions = transactions,
            subscriptions = subscriptions,
            categoryLimits = categoryLimits,
            categoryMergeRules = categoryMergeRules,
            extraCategories = extra
        )
    }
    val expenseCategories = remember(initialExpenseCategories) {
        initialExpenseCategories.toMutableStateList()
    }
    val defaultExpenseCategory = remember(expenseCategories) {
        expenseCategories.firstOrNull() ?: "Food"
    }

    // --- Subscription Form State ---
    var nameText by remember { mutableStateOf(subscription?.name ?: initialPreset?.name ?: "") }
    var amountText by remember {
        val initialAmount = subscription?.amount ?: initialPreset?.defaultAmount
        mutableStateOf(
            initialAmount?.let {
                if (it % 1.0 == 0.0) it.toLong().toString() else it.toString()
            } ?: ""
        )
    }
    val amountFocusRequester = remember { FocusRequester() }
    var categoryText by remember {
        mutableStateOf(subscription?.category ?: initialPreset?.category ?: defaultExpenseCategory)
    }
    var frequencyText by remember {
        mutableStateOf(subscription?.frequency ?: initialPreset?.frequency ?: "Monthly")
    }
    var accountIdText by remember {
        val defaultAcc = accounts.firstOrNull()?.id ?: 0L
        mutableStateOf(subscription?.accountId ?: defaultAcc)
    }
    var nextPaymentDateState by remember {
        mutableStateOf(subscription?.nextPaymentDate ?: (System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L)))
    }
    var showDatePicker by remember { mutableStateOf(false) }

    // --- Installment Form State ---
    var installmentNameText by remember { mutableStateOf("") }
    var installmentTotalAmountText by remember { mutableStateOf("") }
    val installmentAmountFocusRequester = remember { FocusRequester() }
    var installmentCategoryText by remember {
        mutableStateOf(
            expenseCategories.firstOrNull { it.equals("Shopping", ignoreCase = true) }
                ?: defaultExpenseCategory
        )
    }
    var installmentMonths by remember { mutableIntStateOf(12) }
    var installmentAccountId by remember {
        val defaultCc = accounts.firstOrNull { it.type == AccountType.CC }?.id ?: accounts.firstOrNull()?.id ?: 0L
        mutableLongStateOf(defaultCc)
    }
    var installmentFirstDueDate by remember {
        mutableLongStateOf(System.currentTimeMillis())
    }
    var showInstallmentDatePicker by remember { mutableStateOf(false) }

    var showInlineAddCategory by remember { mutableStateOf(false) }
    var newCatName by remember { mutableStateOf("") }
    var isCategoryExpanded by rememberSaveable { mutableStateOf(false) }
    var showAccountPickerSheet by remember { mutableStateOf(false) }
    var showInlineAddAccountInSheet by remember { mutableStateOf(false) }
    val deduplicatedCategories = remember(expenseCategories, resources) {
        expenseCategories.distinctBy { getCategoryDisplayNameFromResources(resources, it) }
    }
    val isZh = remember { Locale.getDefault().language.startsWith("zh") }

    fun onAddNewCategory(rawName: String) {
        val trimmed = rawName.trim()
        if (trimmed.isNotEmpty()) {
            val resolved = categoryMergeRules.resolve(trimmed, CategoryKind.EXPENSE)
            if (!expenseCategories.contains(resolved)) {
                expenseCategories.add(0, resolved)
            }
            if (selectedSheetTab == 0) {
                categoryText = resolved
            } else {
                installmentCategoryText = resolved
            }
            newCatName = ""
            showInlineAddCategory = false
        }
    }

    val frequenciesList = listOf("Monthly", "Yearly", "Weekly")
    val installmentMonthPresets = listOf(3, 6, 12, 24, 36)

    val currentPreset = remember(nameText) {
        POPULAR_SUBSCRIPTION_PRESETS.find {
            nameText.isNotBlank() && it.name.contains(nameText.trim(), ignoreCase = true)
        } ?: POPULAR_SUBSCRIPTION_PRESETS.find {
            nameText.isNotBlank() && nameText.contains(it.name, ignoreCase = true)
        }
    }

    val selectedAccount = remember(accountIdText, accounts) {
        accounts.find { it.id == accountIdText }
    }

    val selectedInstallmentAccount = remember(installmentAccountId, accounts) {
        accounts.find { it.id == installmentAccountId }
    }

    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
    val convertedSubscriptionAmount = convertToHkd(parsedAmount.takeIf { it > 0.0 })
    val isSubscriptionRateReady = selectedCurrency == "HKD" || rateQuote != null
    val canSaveSubscription = nameText.isNotBlank() && parsedAmount > 0.0 && accountIdText != 0L && isSubscriptionRateReady

    val parsedInstallmentTotal = installmentTotalAmountText.toDoubleOrNull() ?: 0.0
    val convertedInstallmentTotal = convertToHkd(parsedInstallmentTotal.takeIf { it > 0.0 })
    val isInstallmentRateReady = selectedCurrency == "HKD" || rateQuote != null
    val canSaveInstallment = installmentNameText.isNotBlank() && parsedInstallmentTotal > 0.0 && installmentMonths >= 2 && installmentAccountId != 0L && isInstallmentRateReady

    Surface(
        modifier = modifier.fillMaxSize().testTag("AddEditSubscriptionSheet"),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxSize()
            ) {
                // Top Page Title Bar (Fixed Header)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.btn_close)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        val sheetTitle = when {
                            subscription != null -> stringResource(R.string.btn_edit_subscription)
                            selectedSheetTab == 1 -> stringResource(R.string.tab_installment)
                            else -> stringResource(R.string.btn_add_subscription)
                        }
                        val sheetSubtitle = when {
                            subscription != null -> stringResource(R.string.ui_recurring_edit_subtitle)
                            selectedSheetTab == 1 -> stringResource(R.string.ui_recurring_installment_subtitle)
                            else -> stringResource(R.string.ui_recurring_add_subtitle)
                        }
                        Text(
                            text = sheetTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = sheetSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    Box {
                        var expanded by remember { mutableStateOf(false) }
                        Surface(
                            onClick = { expanded = !expanded },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.testTag("EntryCurrency"),
                        ) {
                            Text(
                                "${currencyInfo.flagEmoji} ${currencyInfo.code}",
                                Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                            CurrencyEngine.supportedCurrencies.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text("${option.flagEmoji} ${option.code} (${option.symbol})") },
                                    modifier = Modifier.testTag("EntryCurrency_${option.code}"),
                                    onClick = {
                                        selectedCurrency = option.code
                                        expanded = false
                                    },
                                )
                            }
                        }
                    }
                }

                // Scrollable Form Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Mode Switcher (Subscription vs Installment Plan) - only when adding new
                    if (subscription == null) {
                        val subTabStr = stringResource(R.string.tab_subscription)
                        val instTabStr = stringResource(R.string.tab_installment)
                        ConnectedButtonGroup(
                            items = listOf(subTabStr, instTabStr),
                            selectedIndex = selectedSheetTab,
                            onItemSelected = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedSheetTab = it
                            },
                            modifier = Modifier.fillMaxWidth(),
                            labelProvider = { it }
                        )
                    }

                    // FORM CONTENT: SUBSCRIPTION OR INSTALLMENT
                    AnimatedContent(
                        targetState = selectedSheetTab,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                        },
                        label = "SheetTabTransition"
                    ) { tab ->
                        if (tab == 0) {
                            // --- SUBSCRIPTION FORM ---
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Hero Amount Row (Parity with Daily Page Add Expense)
                                val activeBrandColor = currentPreset?.brandColor ?: MaterialTheme.colorScheme.primary
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            amountFocusRequester.requestFocus()
                                        },
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$currencySymbol ",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = activeBrandColor
                                        )
                                    )
                                    BasicTextField(
                                        value = if (amountText == "0") "" else amountText,
                                        onValueChange = { input ->
                                            if (input.isEmpty()) {
                                                amountText = ""
                                            } else if (input == ".") {
                                                amountText = "0."
                                            } else if (input.matches(Regex("""^\d*(?:\.\d{0,2})?$"""))) {
                                                amountText = input
                                            }
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        cursorBrush = SolidColor(activeBrandColor),
                                        textStyle = MaterialTheme.typography.displaySmall.copy(
                                            fontSize = 42.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Start
                                        ),
                                        visualTransformation = if (com.example.vibefinance.ui.preferences.LocalExperience.current.hideAmounts)
                                            androidx.compose.ui.text.input.PasswordVisualTransformation()
                                        else
                                            androidx.compose.ui.text.input.VisualTransformation.None,
                                        modifier = Modifier
                                            .width(IntrinsicSize.Min)
                                            .widthIn(min = 36.dp)
                                            .focusRequester(amountFocusRequester)
                                            .semantics { stateDescription = amountText },
                                        decorationBox = { innerTextField ->
                                            Box(contentAlignment = Alignment.CenterStart) {
                                                if (amountText.isEmpty() || amountText == "0") {
                                                    Text(
                                                        text = "0",
                                                        style = MaterialTheme.typography.displaySmall.copy(
                                                            fontSize = 42.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                                        )
                                                    )
                                                }
                                                innerTextField()
                                            }
                                        }
                                    )
                                    if (amountText.isNotEmpty() && amountText != "0") {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(
                                            onClick = {
                                                amountText = ""
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Clear,
                                                contentDescription = stringResource(R.string.ui_main_clear),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }

                                if (selectedCurrency != "HKD") {
                                    CurrencyRateStatus(
                                        currencyCode = selectedCurrency,
                                        result = rateResult,
                                        loading = rateLoading,
                                        convertedAmount = convertedSubscriptionAmount,
                                        onRefresh = { rateRefreshRequest++ }
                                    )
                                }

                                // ==========================================
                                // BENTO CARD 1: DETAILS & CYCLE (RoundedCornerShape(22.dp))
                                // ==========================================
                                Surface(
                                    shape = RoundedCornerShape(22.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.ui_recurring_identity),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            letterSpacing = 0.8.sp
                                        )

                                        // Subscription Name TextField with leading brand/category indicator
                                        OutlinedTextField(
                                            value = nameText,
                                            onValueChange = { nameText = it },
                                            label = { Text(stringResource(R.string.sub_name_label)) },
                                            placeholder = { Text(stringResource(R.string.ui_recurring_name_hint)) },
                                            leadingIcon = {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = (currentPreset?.brandColor ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.12f),
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        if (currentPreset?.emoji != null) {
                                                            Text(currentPreset.emoji, fontSize = 16.sp)
                                                        } else {
                                                            CategoryIcon(
                                                                category = categoryText,
                                                                tint = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            },
                                            singleLine = true,
                                            shape = RoundedCornerShape(14.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = currentPreset?.brandColor ?: MaterialTheme.colorScheme.primary,
                                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        // Frequency Selector
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                text = stringResource(R.string.ui_recurring_billing_frequency),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            val selectedFreqIndex = frequenciesList.indexOfFirst { it.equals(frequencyText, ignoreCase = true) }.coerceAtLeast(0)
                                            val monthlyStr = stringResource(R.string.filter_monthly)
                                            val yearlyStr = stringResource(R.string.filter_yearly)
                                            val weeklyStr = stringResource(R.string.filter_weekly)

                                            ConnectedButtonGroup(
                                                items = frequenciesList,
                                                selectedIndex = selectedFreqIndex,
                                                onItemSelected = { index ->
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    frequencyText = frequenciesList[index]
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                labelProvider = { freq ->
                                                    when (freq.lowercase(Locale.US)) {
                                                        "monthly" -> monthlyStr
                                                        "yearly", "annual" -> yearlyStr
                                                        "weekly" -> weeklyStr
                                                        else -> freq
                                                    }
                                                }
                                            )
                                        }

                                        // Next Payment Date
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                text = stringResource(R.string.sub_due_date_label),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            val formattedDate = recurringDateLabel(
                                                Instant.ofEpochMilli(nextPaymentDateState).atZone(ZoneId.systemDefault()).toLocalDate(),
                                                full = true
                                            )

                                            Surface(
                                                shape = RoundedCornerShape(14.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .bouncyClickable(shape = RoundedCornerShape(14.dp)) { showDatePicker = true }
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Default.CalendarMonth,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Text(
                                                            text = formattedDate,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                    Text(
                                                        text = stringResource(R.string.btn_change),
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // ==========================================
                                // BENTO CARD 2: CLASSIFICATION & PAYMENT ASSET (RoundedCornerShape(22.dp))
                                // ==========================================
                                Surface(
                                    shape = RoundedCornerShape(22.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        // Category Header Row with in-place expand/collapse toggle
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = stringResource(R.string.sub_category_label),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                            )

                                            val rotateDegrees by animateFloatAsState(
                                                targetValue = if (isCategoryExpanded) 180f else 0f,
                                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                                label = "categoryChevronRotation"
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable {
                                                        com.example.vibefinance.util.VibeHaptics.lightTick(hapticView)
                                                        isCategoryExpanded = !isCategoryExpanded
                                                    }
                                                    .testTag("RecurringCategoryExpandToggle")
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    val toggleText = if (isCategoryExpanded) {
                                                        if (isZh) "收合" else stringResource(R.string.settings_section_collapsed)
                                                    } else {
                                                        if (isZh) "展開全部" else stringResource(R.string.settings_section_expanded)
                                                    }
                                                    Text(
                                                        text = toggleText,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Icon(
                                                        imageVector = Icons.Default.KeyboardArrowDown,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier
                                                            .size(14.dp)
                                                            .graphicsLayer { rotationZ = rotateDegrees }
                                                    )
                                                }
                                            }
                                        }

                                        // Inline Add Category Form
                                        AnimatedVisibility(
                                            visible = showInlineAddCategory && selectedSheetTab == 0,
                                            enter = expandVertically() + fadeIn(),
                                            exit = shrinkVertically() + fadeOut()
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                                    .padding(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = stringResource(R.string.ui_main_add_category),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    OutlinedTextField(
                                                        value = newCatName,
                                                        onValueChange = { newCatName = it },
                                                        placeholder = { Text(stringResource(R.string.ui_main_category_hint)) },
                                                        singleLine = true,
                                                        modifier = Modifier.weight(1f),
                                                        textStyle = MaterialTheme.typography.bodyMedium
                                                    )
                                                    val addCatInteraction = remember { MutableInteractionSource() }
                                                    CompletePressButton(
                                                        onClick = {
                                                            com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                                            onAddNewCategory(newCatName)
                                                        },
                                                        shapes = ButtonDefaults.shapes(shape = CircleShape, pressedShape = RoundedCornerShape(percent = 32)),
                                                        interactionSource = addCatInteraction,
                                                        modifier = Modifier.pressBounce(interactionSource = addCatInteraction)
                                                    ) {
                                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(stringResource(R.string.btn_add))
                                                    }
                                                }
                                            }
                                        }

                                        // Category Tag Cloud / Single Row
                                        val subCategoryListState = rememberLazyListState()
                                        LaunchedEffect(categoryText, deduplicatedCategories.size) {
                                            val selectedIdx = deduplicatedCategories.indexOfFirst {
                                                getCategoryDisplayNameFromResources(resources, it).equals(getCategoryDisplayNameFromResources(resources, categoryText), ignoreCase = true)
                                            }
                                            if (selectedIdx >= 0) {
                                                subCategoryListState.animateScrollToItem((selectedIdx - 1).coerceAtLeast(0))
                                            }
                                        }

                                        AnimatedContent(
                                            targetState = isCategoryExpanded,
                                            transitionSpec = {
                                                if (targetState) {
                                                    (fadeIn(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)) +
                                                     expandVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow), expandFrom = Alignment.Top))
                                                     .togetherWith(fadeOut(animationSpec = tween(90, easing = LinearEasing)) +
                                                     shrinkVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow), shrinkTowards = Alignment.Top))
                                                } else {
                                                    (fadeIn(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)) +
                                                     expandVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow), expandFrom = Alignment.Top))
                                                     .togetherWith(fadeOut(animationSpec = tween(90, easing = LinearEasing)) +
                                                     shrinkVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow), shrinkTowards = Alignment.Top))
                                                }.using(SizeTransform(clip = false) { _, _ -> spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow) })
                                            },
                                            label = "SubscriptionCategoryExpansionTransition",
                                            modifier = Modifier.fillMaxWidth()
                                        ) { expanded ->
                                            if (!expanded) {
                                                LazyRow(
                                                    state = subCategoryListState,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .horizontalFadingEdge(subCategoryListState, 16.dp)
                                                        .testTag("RecurringCategory")
                                                ) {
                                                    items(deduplicatedCategories, key = { it }) { cat ->
                                                        val isSelected = getCategoryDisplayNameFromResources(resources, categoryText).equals(getCategoryDisplayNameFromResources(resources, cat), ignoreCase = true)
                                                        FilterChip(
                                                            selected = isSelected,
                                                            onClick = {
                                                                com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                                                categoryText = cat
                                                            },
                                                            leadingIcon = {
                                                                CategoryIcon(
                                                                    category = cat,
                                                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                                    modifier = Modifier.size(16.dp)
                                                                )
                                                            },
                                                            label = {
                                                                Text(
                                                                    getCategoryDisplayName(cat),
                                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                                    maxLines = 1,
                                                                    softWrap = false,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                            },
                                                            shape = RoundedCornerShape(12.dp),
                                                            modifier = Modifier.testTag("RecurringCategory_row_$cat")
                                                        )
                                                    }
                                                    item(key = "__add_category__") {
                                                        FilterChip(
                                                            selected = showInlineAddCategory && selectedSheetTab == 0,
                                                            onClick = {
                                                                com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                                                showInlineAddCategory = !showInlineAddCategory
                                                            },
                                                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                                            label = { Text(stringResource(R.string.btn_add)) },
                                                            shape = RoundedCornerShape(12.dp),
                                                            modifier = Modifier.testTag("RecurringCategory_row_add")
                                                        )
                                                    }
                                                }
                                            } else {
                                                FlowRow(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .testTag("RecurringCategory_cloud"),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    deduplicatedCategories.forEach { cat ->
                                                        val isSelected = getCategoryDisplayNameFromResources(resources, categoryText).equals(getCategoryDisplayNameFromResources(resources, cat), ignoreCase = true)
                                                        FilterChip(
                                                            selected = isSelected,
                                                            onClick = {
                                                                com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                                                categoryText = cat
                                                                isCategoryExpanded = false
                                                                scope.launch {
                                                                    val idx = deduplicatedCategories.indexOf(cat)
                                                                    if (idx >= 0) {
                                                                        subCategoryListState.animateScrollToItem((idx - 1).coerceAtLeast(0))
                                                                    }
                                                                }
                                                            },
                                                            leadingIcon = {
                                                                CategoryIcon(
                                                                    category = cat,
                                                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                                    modifier = Modifier.size(16.dp)
                                                                )
                                                            },
                                                            label = {
                                                                Text(
                                                                    getCategoryDisplayName(cat),
                                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                                    maxLines = 1,
                                                                    softWrap = false,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                            },
                                                            shape = RoundedCornerShape(12.dp),
                                                            modifier = Modifier.testTag("RecurringCategory_row_$cat")
                                                        )
                                                    }
                                                    FilterChip(
                                                        selected = showInlineAddCategory && selectedSheetTab == 0,
                                                        onClick = {
                                                            com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                                            showInlineAddCategory = !showInlineAddCategory
                                                        },
                                                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                                        label = { Text(stringResource(R.string.btn_add)) },
                                                        shape = RoundedCornerShape(12.dp),
                                                        modifier = Modifier.testTag("RecurringCategory_row_add")
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        // Payment Asset Card
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("RecurringAssetSection"),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = stringResource(R.string.sub_account_label),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                            )

                                            SelectedAccountCard(
                                                account = selectedAccount,
                                                isDestination = false,
                                                onClick = {
                                                    com.example.vibefinance.util.VibeHaptics.lightTick(hapticView)
                                                    showAccountPickerSheet = true
                                                },
                                                modifier = Modifier.testTag("RecurringAccount_card")
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // --- INSTALLMENT PLAN FORM ---
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Hero Total Amount Row (Parity with Daily Page Add Expense & Subscription)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            installmentAmountFocusRequester.requestFocus()
                                        },
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$currencySymbol ",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    )
                                    BasicTextField(
                                        value = if (installmentTotalAmountText == "0") "" else installmentTotalAmountText,
                                        onValueChange = { input ->
                                            if (input.isEmpty()) {
                                                installmentTotalAmountText = ""
                                            } else if (input == ".") {
                                                installmentTotalAmountText = "0."
                                            } else if (input.matches(Regex("""^\d*(?:\.\d{0,2})?$"""))) {
                                                installmentTotalAmountText = input
                                            }
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        cursorBrush = SolidColor(MaterialTheme.colorScheme.tertiary),
                                        textStyle = MaterialTheme.typography.displaySmall.copy(
                                            fontSize = 42.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Start
                                        ),
                                        visualTransformation = if (com.example.vibefinance.ui.preferences.LocalExperience.current.hideAmounts)
                                            androidx.compose.ui.text.input.PasswordVisualTransformation()
                                        else
                                            androidx.compose.ui.text.input.VisualTransformation.None,
                                        modifier = Modifier
                                            .width(IntrinsicSize.Min)
                                            .widthIn(min = 36.dp)
                                            .focusRequester(installmentAmountFocusRequester)
                                            .semantics { stateDescription = installmentTotalAmountText },
                                        decorationBox = { innerTextField ->
                                            Box(contentAlignment = Alignment.CenterStart) {
                                                if (installmentTotalAmountText.isEmpty() || installmentTotalAmountText == "0") {
                                                    Text(
                                                        text = "0",
                                                        style = MaterialTheme.typography.displaySmall.copy(
                                                            fontSize = 42.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                                        )
                                                    )
                                                }
                                                innerTextField()
                                            }
                                        }
                                    )
                                    if (installmentTotalAmountText.isNotEmpty() && installmentTotalAmountText != "0") {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(
                                            onClick = {
                                                installmentTotalAmountText = ""
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Clear,
                                                contentDescription = stringResource(R.string.ui_main_clear),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }

                                if (selectedCurrency != "HKD") {
                                    CurrencyRateStatus(
                                        currencyCode = selectedCurrency,
                                        result = rateResult,
                                        loading = rateLoading,
                                        convertedAmount = convertedInstallmentTotal,
                                        onRefresh = { rateRefreshRequest++ }
                                    )
                                }

                                // ==========================================
                                // BENTO CARD 1: PURCHASE DETAILS & DURATION (RoundedCornerShape(22.dp))
                                // ==========================================
                                Surface(
                                    shape = RoundedCornerShape(22.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.ui_recurring_purchase_details),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.tertiary,
                                            letterSpacing = 0.8.sp
                                        )

                                        // Purchase Name TextField
                                        OutlinedTextField(
                                            value = installmentNameText,
                                            onValueChange = { installmentNameText = it },
                                            label = { Text(stringResource(R.string.ui_recurring_purchase_name)) },
                                            placeholder = { Text(stringResource(R.string.ui_recurring_purchase_hint)) },
                                            leadingIcon = {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            Icons.Default.ViewTimeline,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.tertiary,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            },
                                            singleLine = true,
                                            shape = RoundedCornerShape(14.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        // Duration Presets & Stepper
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            val durationListState = rememberLazyListState()
                                            LazyRow(
                                                state = durationListState,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalFadingEdge(durationListState, 16.dp)
                                            ) {
                                                items(installmentMonthPresets) { months ->
                                                    val isSelected = installmentMonths == months
                                                    FilterChip(
                                                        selected = isSelected,
                                                        onClick = {
                                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                            installmentMonths = months
                                                        },
                                                        label = {
                                                            Text(
                                                                stringResource(R.string.ui_recurring_months_short, months),
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                                maxLines = 1,
                                                                softWrap = false,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        },
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                }
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(14.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    IconButton(
                                                        onClick = {
                                                            if (installmentMonths > 2) {
                                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                                installmentMonths--
                                                            }
                                                        },
                                                        enabled = installmentMonths > 2
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Remove,
                                                            contentDescription = stringResource(R.string.ui_recurring_decrease_months),
                                                            tint = if (installmentMonths > 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                                        )
                                                    }

                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text(
                                                            text = stringResource(R.string.ui_recurring_duration_months, installmentMonths),
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        if (parsedInstallmentTotal > 0.0) {
                                                            val effectiveTotal = if (selectedCurrency == "HKD") parsedInstallmentTotal else (convertedInstallmentTotal ?: 0.0)
                                                            val perMonth = effectiveTotal / installmentMonths
                                                            Text(
                                                                text = stringResource(R.string.ui_recurring_per_month_cost, perMonth),
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontWeight = FontWeight.SemiBold,
                                                                color = MaterialTheme.colorScheme.tertiary
                                                            )
                                                        }
                                                    }

                                                    IconButton(
                                                        onClick = {
                                                            if (installmentMonths < 60) {
                                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                                installmentMonths++
                                                            }
                                                        },
                                                        enabled = installmentMonths < 60
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Add,
                                                            contentDescription = stringResource(R.string.ui_recurring_increase_months),
                                                            tint = if (installmentMonths < 60) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // First Due Date
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                text = stringResource(R.string.ui_recurring_first_due_date),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            val formattedFirstDate = recurringDateLabel(
                                                Instant.ofEpochMilli(installmentFirstDueDate).atZone(ZoneId.systemDefault()).toLocalDate(),
                                                full = true
                                            )

                                            Surface(
                                                shape = RoundedCornerShape(14.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .bouncyClickable(shape = RoundedCornerShape(14.dp)) { showInstallmentDatePicker = true }
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Default.CalendarMonth,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.tertiary,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Text(
                                                            text = formattedFirstDate,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                    Text(
                                                        text = stringResource(R.string.btn_change),
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.tertiary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // ==========================================
                                // BENTO CARD 2: CLASSIFICATION & PAYMENT ASSET (RoundedCornerShape(22.dp))
                                // ==========================================
                                Surface(
                                    shape = RoundedCornerShape(22.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        // Category Header Row with in-place expand/collapse toggle
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = stringResource(R.string.sub_category_label),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                            )

                                            val rotateDegrees by animateFloatAsState(
                                                targetValue = if (isCategoryExpanded) 180f else 0f,
                                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                                label = "categoryChevronRotation_installment"
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable {
                                                        com.example.vibefinance.util.VibeHaptics.lightTick(hapticView)
                                                        isCategoryExpanded = !isCategoryExpanded
                                                    }
                                                    .testTag("RecurringInstallmentCategoryExpandToggle")
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    val toggleText = if (isCategoryExpanded) {
                                                        if (isZh) "收合" else stringResource(R.string.settings_section_collapsed)
                                                    } else {
                                                        if (isZh) "展開全部" else stringResource(R.string.settings_section_expanded)
                                                    }
                                                    Text(
                                                        text = toggleText,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.tertiary
                                                    )
                                                    Icon(
                                                        imageVector = Icons.Default.KeyboardArrowDown,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.tertiary,
                                                        modifier = Modifier
                                                            .size(14.dp)
                                                            .graphicsLayer { rotationZ = rotateDegrees }
                                                    )
                                                }
                                            }
                                        }

                                        // Inline Add Category Form
                                        AnimatedVisibility(
                                            visible = showInlineAddCategory && selectedSheetTab == 1,
                                            enter = expandVertically() + fadeIn(),
                                            exit = shrinkVertically() + fadeOut()
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                                    .padding(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = stringResource(R.string.ui_main_add_category),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = MaterialTheme.colorScheme.tertiary
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    OutlinedTextField(
                                                        value = newCatName,
                                                        onValueChange = { newCatName = it },
                                                        placeholder = { Text(stringResource(R.string.ui_main_category_hint)) },
                                                        singleLine = true,
                                                        modifier = Modifier.weight(1f),
                                                        textStyle = MaterialTheme.typography.bodyMedium
                                                    )
                                                    val addCatInteraction = remember { MutableInteractionSource() }
                                                    CompletePressButton(
                                                        onClick = {
                                                            com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                                            onAddNewCategory(newCatName)
                                                        },
                                                        shapes = ButtonDefaults.shapes(shape = CircleShape, pressedShape = RoundedCornerShape(percent = 32)),
                                                        interactionSource = addCatInteraction,
                                                        modifier = Modifier.pressBounce(interactionSource = addCatInteraction)
                                                    ) {
                                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(stringResource(R.string.btn_add))
                                                    }
                                                }
                                            }
                                        }

                                        // Category Tag Cloud / Single Row
                                        val instCategoryListState = rememberLazyListState()
                                        LaunchedEffect(installmentCategoryText, deduplicatedCategories.size) {
                                            val selectedIdx = deduplicatedCategories.indexOfFirst {
                                                getCategoryDisplayNameFromResources(resources, it).equals(getCategoryDisplayNameFromResources(resources, installmentCategoryText), ignoreCase = true)
                                            }
                                            if (selectedIdx >= 0) {
                                                instCategoryListState.animateScrollToItem((selectedIdx - 1).coerceAtLeast(0))
                                            }
                                        }

                                        AnimatedContent(
                                            targetState = isCategoryExpanded,
                                            transitionSpec = {
                                                if (targetState) {
                                                    (fadeIn(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)) +
                                                     expandVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow), expandFrom = Alignment.Top))
                                                     .togetherWith(fadeOut(animationSpec = tween(90, easing = LinearEasing)) +
                                                     shrinkVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow), shrinkTowards = Alignment.Top))
                                                } else {
                                                    (fadeIn(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)) +
                                                     expandVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow), expandFrom = Alignment.Top))
                                                     .togetherWith(fadeOut(animationSpec = tween(90, easing = LinearEasing)) +
                                                     shrinkVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow), shrinkTowards = Alignment.Top))
                                                }.using(SizeTransform(clip = false) { _, _ -> spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow) })
                                            },
                                            label = "InstallmentCategoryExpansionTransition",
                                            modifier = Modifier.fillMaxWidth()
                                        ) { expanded ->
                                            if (!expanded) {
                                                LazyRow(
                                                    state = instCategoryListState,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .horizontalFadingEdge(instCategoryListState, 16.dp)
                                                        .testTag("RecurringInstallmentCategory")
                                                ) {
                                                    items(deduplicatedCategories, key = { it }) { cat ->
                                                        val isSelected = getCategoryDisplayNameFromResources(resources, installmentCategoryText).equals(getCategoryDisplayNameFromResources(resources, cat), ignoreCase = true)
                                                        FilterChip(
                                                            selected = isSelected,
                                                            onClick = {
                                                                com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                                                installmentCategoryText = cat
                                                            },
                                                            leadingIcon = {
                                                                CategoryIcon(
                                                                    category = cat,
                                                                    tint = if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                                    modifier = Modifier.size(16.dp)
                                                                )
                                                            },
                                                            label = {
                                                                Text(
                                                                    getCategoryDisplayName(cat),
                                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                                    maxLines = 1,
                                                                    softWrap = false,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                            },
                                                            shape = RoundedCornerShape(12.dp),
                                                            modifier = Modifier.testTag("RecurringInstallmentCategory_row_$cat")
                                                        )
                                                    }
                                                    item(key = "__add_category_inst__") {
                                                        FilterChip(
                                                            selected = showInlineAddCategory && selectedSheetTab == 1,
                                                            onClick = {
                                                                com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                                                showInlineAddCategory = !showInlineAddCategory
                                                            },
                                                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                                            label = { Text(stringResource(R.string.btn_add)) },
                                                            shape = RoundedCornerShape(12.dp),
                                                            modifier = Modifier.testTag("RecurringInstallmentCategory_row_add")
                                                        )
                                                    }
                                                }
                                            } else {
                                                FlowRow(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .testTag("RecurringInstallmentCategory_cloud"),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    deduplicatedCategories.forEach { cat ->
                                                        val isSelected = getCategoryDisplayNameFromResources(resources, installmentCategoryText).equals(getCategoryDisplayNameFromResources(resources, cat), ignoreCase = true)
                                                        FilterChip(
                                                            selected = isSelected,
                                                            onClick = {
                                                                com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                                                installmentCategoryText = cat
                                                                isCategoryExpanded = false
                                                                scope.launch {
                                                                    val idx = deduplicatedCategories.indexOf(cat)
                                                                    if (idx >= 0) {
                                                                        instCategoryListState.animateScrollToItem((idx - 1).coerceAtLeast(0))
                                                                    }
                                                                }
                                                            },
                                                            leadingIcon = {
                                                                CategoryIcon(
                                                                    category = cat,
                                                                    tint = if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                                    modifier = Modifier.size(16.dp)
                                                                )
                                                            },
                                                            label = {
                                                                Text(
                                                                    getCategoryDisplayName(cat),
                                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                                    maxLines = 1,
                                                                    softWrap = false,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                            },
                                                            shape = RoundedCornerShape(12.dp),
                                                            modifier = Modifier.testTag("RecurringInstallmentCategory_row_$cat")
                                                        )
                                                    }
                                                    FilterChip(
                                                        selected = showInlineAddCategory && selectedSheetTab == 1,
                                                        onClick = {
                                                            com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                                            showInlineAddCategory = !showInlineAddCategory
                                                        },
                                                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                                        label = { Text(stringResource(R.string.btn_add)) },
                                                        shape = RoundedCornerShape(12.dp),
                                                        modifier = Modifier.testTag("RecurringInstallmentCategory_row_add")
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        // Payment Asset Card
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("RecurringInstallmentAssetSection"),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = stringResource(R.string.sub_account_label),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                            )

                                            SelectedAccountCard(
                                                account = selectedInstallmentAccount,
                                                isDestination = false,
                                                onClick = {
                                                    com.example.vibefinance.util.VibeHaptics.lightTick(hapticView)
                                                    showAccountPickerSheet = true
                                                },
                                                modifier = Modifier.testTag("RecurringInstallmentAccount_card")
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 5. STICKY ELEVATED BOTTOM ACTION BAR
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (subscription != null) {
                            OutlinedButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onDelete(subscription)
                                },
                                modifier = Modifier
                                    .weight(0.35f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.btn_delete),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (selectedSheetTab == 0) {
                            val saveInteraction = remember { MutableInteractionSource() }
                            CompletePressButton(
                                onClick = {
                                    if (canSaveSubscription) {
                                        val finalAmount = if (selectedCurrency == "HKD") {
                                            parsedAmount
                                        } else {
                                            convertedSubscriptionAmount ?: parsedAmount
                                        }
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        val sub = SubscriptionEntity(
                                            id = subscription?.id ?: 0L,
                                            name = nameText.trim(),
                                            amount = finalAmount,
                                            category = categoryText,
                                            frequency = frequencyText,
                                            nextPaymentDate = nextPaymentDateState,
                                            accountId = accountIdText
                                        )
                                        onSave(sub)
                                    }
                                },
                                enabled = canSaveSubscription,
                                shapes = ButtonDefaults.shapes(shape = RoundedCornerShape(16.dp), pressedShape = RoundedCornerShape(percent = 32)),
                                interactionSource = saveInteraction,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier
                                    .weight(if (subscription != null) 0.65f else 1f)
                                    .height(50.dp)
                                    .pressBounce(interactionSource = saveInteraction)
                                    .testTag("SubscriptionSaveButton")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (subscription == null) stringResource(R.string.btn_save_subscription) else stringResource(R.string.btn_save_changes),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            val saveInstallmentInteraction = remember { MutableInteractionSource() }
                            CompletePressButton(
                                onClick = {
                                    if (canSaveInstallment) {
                                        val finalTotal = if (selectedCurrency == "HKD") {
                                            parsedInstallmentTotal
                                        } else {
                                            convertedInstallmentTotal ?: parsedInstallmentTotal
                                        }
                                        val finalDescription = if (selectedCurrency != "HKD" && rateQuote != null) {
                                            val origStr = "[Orig: ${CurrencyEngine.formatOriginal(parsedInstallmentTotal, selectedCurrency)}; 1 ${rateQuote.currencyCode} = ${rateQuote.rateText()} HKD; ${rateQuote.source} ${rateQuote.rateDate}]"
                                            if (installmentNameText.isNotBlank()) "${installmentNameText.trim()} $origStr" else origStr
                                        } else {
                                            installmentNameText.trim()
                                        }
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onSaveInstallment(
                                            finalTotal,
                                            installmentCategoryText,
                                            installmentAccountId,
                                            finalDescription,
                                            installmentMonths,
                                            installmentFirstDueDate
                                        )
                                    }
                                },
                                enabled = canSaveInstallment,
                                shapes = ButtonDefaults.shapes(shape = RoundedCornerShape(16.dp), pressedShape = RoundedCornerShape(percent = 32)),
                                interactionSource = saveInstallmentInteraction,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                    contentColor = MaterialTheme.colorScheme.onTertiary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .pressBounce(interactionSource = saveInstallmentInteraction)
                                    .testTag("InstallmentSaveButton")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.btn_create_installment),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showAccountPickerSheet) {
            val sortedAccounts = remember(accounts) {
                accounts.sortedWith(
                    compareBy<AccountEntity> { acc ->
                        when (acc.type) {
                            AccountType.CASH -> 0
                            AccountType.BANK -> 1
                            AccountType.DEBIT -> 2
                            AccountType.CC -> 3
                            AccountType.INVESTMENT -> 4
                        }
                    }.thenBy { it.name }
                )
            }
            AppModalBottomSheet(
                onDismissRequest = {
                    showAccountPickerSheet = false
                    showInlineAddAccountInSheet = false
                },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .navigationBarsPadding()
                ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.ui_main_asset_selection),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = {
                            showAccountPickerSheet = false
                            showInlineAddAccountInSheet = false
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    // Account List (2 Columns Grid)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        items(sortedAccounts, key = { it.id }) { acc ->
                            val isSelected = acc.id == (if (selectedSheetTab == 0) accountIdText else installmentAccountId)
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                        if (selectedSheetTab == 0) {
                                            accountIdText = acc.id
                                        } else {
                                            installmentAccountId = acc.id
                                        }
                                        showAccountPickerSheet = false
                                        showInlineAddAccountInSheet = false
                                    }
                                    .testTag("RecurringAccount_row_${acc.id}")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = getAccountIcon(acc.type),
                                                    contentDescription = null,
                                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                                            ) {
                                                Text(
                                                    text = accountTypeLabel(acc.type),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = acc.nickname?.takeIf { it.isNotBlank() } ?: acc.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    val formattedBal = CurrencyEngine.formatSignedCompact(acc.balance, prefix = "HK$")
                                    val balanceLabel = if (acc.type == AccountType.CC)
                                        stringResource(R.string.ui_recurring_account_owing, formattedBal)
                                    else
                                        stringResource(R.string.ui_recurring_account_balance, formattedBal)
                                    Text(
                                        text = balanceLabel,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    if (onAddNewAccount != null) {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Inline "+ 新增帳戶" Toggle & Form
                        OutlinedButton(
                            onClick = {
                                showInlineAddAccountInSheet = !showInlineAddAccountInSheet
                                com.example.vibefinance.util.VibeHaptics.lightTick(hapticView)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("RecurringAccountPicker_AddAccountToggle"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = if (showInlineAddAccountInSheet) Icons.Default.Close else Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (showInlineAddAccountInSheet) stringResource(R.string.btn_cancel) else stringResource(R.string.ui_main_add_account))
                        }

                        AnimatedVisibility(
                            visible = showInlineAddAccountInSheet,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                var newAssetName by remember { mutableStateOf("") }
                                var selectedType by remember { mutableStateOf(AccountType.CASH) }
                                var initialBalanceText by remember { mutableStateOf("") }

                                Text(stringResource(R.string.ui_main_add_account), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)

                                OutlinedTextField(
                                    value = newAssetName,
                                    onValueChange = { newAssetName = it },
                                    placeholder = { Text(stringResource(R.string.ui_main_account_hint)) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(stringResource(R.string.ui_main_account_type), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.25f))
                                    val cashStr = stringResource(R.string.acc_type_cash)
                                    val bankStr = stringResource(R.string.ui_main_bank_short)
                                    val debitStr = stringResource(R.string.ui_main_debit_short)
                                    val creditStr = stringResource(R.string.ui_main_credit_short)
                                    val investStr = stringResource(R.string.assets_type_investment)
                                    ExpressiveSegmentedButtonGroup(
                                        corners = ConnectedButtonCorners.Entry,
                                        items = AccountType.entries,
                                        selectedIndex = AccountType.entries.indexOf(selectedType),
                                        onItemSelected = { selectedType = AccountType.entries[it] },
                                        modifier = Modifier.weight(1f),
                                        isScrollable = true,
                                        labelProvider = {
                                            when (it) {
                                                AccountType.CASH -> cashStr
                                                AccountType.BANK -> bankStr
                                                AccountType.DEBIT -> debitStr
                                                AccountType.CC -> creditStr
                                                AccountType.INVESTMENT -> investStr
                                            }
                                        }
                                    )
                                }

                                if (selectedType == AccountType.INVESTMENT) {
                                    Text(
                                        stringResource(R.string.assets_investment_notice),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        visualTransformation = if (com.example.vibefinance.ui.preferences.LocalExperience.current.hideAmounts)
                                            androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
                                        value = initialBalanceText,
                                        onValueChange = { initialBalanceText = it },
                                        placeholder = { Text(if (selectedType == AccountType.CC) stringResource(R.string.ui_main_credit_limit_hint) else stringResource(R.string.ui_main_initial_balance_hint)) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    val addAccountInteraction = remember { MutableInteractionSource() }
                                    CompletePressButton(
                                        onClick = {
                                            com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                            val name = newAssetName.trim()
                                            val bal = initialBalanceText.toDoubleOrNull() ?: 0.0
                                            if (name.isNotEmpty()) {
                                                val newAccount = AccountEntity(
                                                    name = name,
                                                    type = selectedType,
                                                    balance = if (selectedType == AccountType.CC) 0.0 else bal,
                                                    icon = when (selectedType) {
                                                        AccountType.CASH -> "wallet"
                                                        AccountType.BANK -> "bank"
                                                        AccountType.DEBIT -> "debit_card"
                                                        AccountType.CC -> "credit_card"
                                                        AccountType.INVESTMENT -> "investment"
                                                    },
                                                    creditLimit = if (selectedType == AccountType.CC) bal else null
                                                )
                                                onAddNewAccount(newAccount)
                                            }
                                            newAssetName = ""
                                            initialBalanceText = ""
                                            showInlineAddAccountInSheet = false
                                            showAccountPickerSheet = false
                                        },
                                        shapes = ButtonDefaults.shapes(shape = CircleShape, pressedShape = RoundedCornerShape(percent = 32)),
                                        interactionSource = addAccountInteraction,
                                        modifier = Modifier
                                            .height(48.dp)
                                            .pressBounce(interactionSource = addAccountInteraction)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(stringResource(R.string.btn_save))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Subscription Date Picker Modal Dialog
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = nextPaymentDateState
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let {
                                nextPaymentDateState = it
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text(stringResource(R.string.ui_recurring_select_date), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // Installment Date Picker Modal Dialog
        if (showInstallmentDatePicker) {
            val installmentDatePickerState = rememberDatePickerState(
                initialSelectedDateMillis = installmentFirstDueDate
            )
            DatePickerDialog(
                onDismissRequest = { showInstallmentDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            installmentDatePickerState.selectedDateMillis?.let {
                                installmentFirstDueDate = it
                            }
                            showInstallmentDatePicker = false
                        }
                    ) {
                        Text(stringResource(R.string.ui_recurring_select_date), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showInstallmentDatePicker = false }) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                }
            ) {
                DatePicker(state = installmentDatePickerState)
            }
        }
    }
}

/**
 * Material Design 3 Expressive Connected Button Group.
 * Standardized across screens via ExpressiveConnectedButtonGroup.
 */
@Composable
fun <T> ConnectedButtonGroup(
    items: List<T>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    labelProvider: @Composable (T) -> String
) {
    ExpressiveConnectedButtonGroup(
        items = items,
        selectedIndex = selectedIndex,
        onItemSelected = onItemSelected,
        modifier = modifier,
        compact = true,
        labelProvider = labelProvider
    )
}

// -------------------------------------------------------------
// DELETE SUBSCRIPTION CONFIRMATION DIALOG (M3 Expressive)
// -------------------------------------------------------------
fun findMatchingTransactionsForSubscription(
    subscription: SubscriptionEntity,
    transactions: List<TransactionEntity>
): List<TransactionEntity> {
    val subName = subscription.name.trim()
    if (subName.isBlank()) return emptyList()
    return transactions.filter { tx ->
        tx.toAccountId == null && (
            tx.description.contains(subName, ignoreCase = true) ||
            tx.description.contains("Auto-charge: $subName", ignoreCase = true)
        )
    }
}

@Composable
fun DeleteSubscriptionConfirmDialog(
    subscription: SubscriptionEntity,
    transactions: List<TransactionEntity>,
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (deletePastTransactions: Boolean) -> Unit
) {
    val matchingTransactions = remember(subscription, transactions) {
        findMatchingTransactionsForSubscription(subscription, transactions)
    }

    val pastCount = matchingTransactions.size
    val pastTotal = matchingTransactions.sumOf { it.amount }
    val formattedPastTotal = remember(pastTotal) {
        String.format(Locale.getDefault(), "HK$ %,.2f", pastTotal)
    }
    val formattedSubAmount = remember(subscription.amount) {
        String.format(Locale.getDefault(), "HK$ %,.2f", subscription.amount)
    }
    val accountName = accounts.find { it.id == subscription.accountId }?.name ?: stringResource(R.string.ui_recurring_linked_account)

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        if (pastCount > 0) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.errorContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (pastCount > 0) Icons.AutoMirrored.Filled.ReceiptLong else Icons.Default.Delete,
                    contentDescription = null,
                    tint = if (pastCount > 0) MaterialTheme.colorScheme.onPrimaryContainer
                           else MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Text(
                text = stringResource(R.string.delete_sub_dialog_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Subscription identity card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = subscription.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = formattedSubAmount,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = subscription.frequency,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = accountName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (pastCount > 0) {
                    // History summary banner
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(top = 2.dp)
                            )
                            Column {
                                Text(
                                    text = stringResource(
                                        R.string.delete_sub_records_found,
                                        pastCount,
                                        formattedPastTotal
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.delete_sub_ask_records),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }

                    // Choice 1: Keep Past Records (Recommended)
                    Surface(
                        onClick = { onConfirm(false) },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("BtnKeepPastRecords")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.delete_sub_keep_records_btn),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(R.string.delete_sub_keep_records_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    // Choice 2: Delete Records Too (Destructive)
                    Surface(
                        onClick = { onConfirm(true) },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.error.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("BtnDeleteRecordsToo")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.delete_sub_delete_all_btn),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(R.string.delete_sub_delete_all_desc, pastCount),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                } else {
                    // No past records
                    Text(
                        text = stringResource(R.string.delete_sub_no_records_msg),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            if (pastCount == 0) {
                Button(
                    onClick = { onConfirm(false) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.delete_sub_confirm_single))
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.btn_cancel))
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
}
