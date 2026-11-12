package com.example.vibefinance.ui.home

import com.example.vibefinance.ui.components.AppButtonDefaults

import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.res.stringResource
import com.example.vibefinance.R
import com.example.vibefinance.theme.MotionTokens
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.example.vibefinance.ui.common.bouncyClickable
import com.example.vibefinance.ui.common.rememberExpressiveCardPress
import com.example.vibefinance.ui.common.expressiveCardClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import com.example.vibefinance.theme.BentoCardShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalView
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.example.vibefinance.ui.preferences.PrivacyText as Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.data.repository.DailyBudgetInfo
import com.example.vibefinance.ui.FinanceIntent
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.components.GlassmorphicCard
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Constrains DaysLeftCard size between 120dp and 156dp for vertical stack mode.
 */
fun calculateStackedDaysSize(maxWidth: Dp): Dp {
    return minOf(maxWidth * 0.45f, 156.dp).coerceAtLeast(120.dp)
}

/**
 * Determines whether the daily bento layout should stack vertically:
 * - Ultra-narrow widths (< 340dp)
 * - Amount overflow at 12sp floor in side-by-side mode
 * - High font scale (> 1.25) where available width is constrained
 */
fun shouldStackBento(
    maxWidth: Dp,
    bentoSpacing: Dp,
    fontScale: Float,
    isUltraNarrowThreshold: Dp = 340.dp,
    remTextWidthAt12sp: Float = 0f,
    spentTextWidthAt12sp: Float = 0f,
    candidateAvailableWidthPx: Float = Float.MAX_VALUE,
): Boolean {
    if (maxWidth < isUltraNarrowThreshold) return true
    val maxTextWidth = maxOf(remTextWidthAt12sp, spentTextWidthAt12sp)
    if (maxTextWidth > candidateAvailableWidthPx) return true
    if (fontScale > 1.25f && maxTextWidth > candidateAvailableWidthPx * 0.85f) return true
    return false
}

@Composable
fun CategoryIcon(category: String, tint: Color, modifier: Modifier = Modifier) {
    val normalized = category.trim().replaceFirst(Regex("^[^\\p{L}\\p{N}]+"), "").lowercase(Locale.US).trim()
    val icon = when (normalized) {
        "food", "food & drink", "restaurant", "food & dining" -> Icons.Default.Restaurant
        "coffee", "cafe" -> Icons.Default.LocalCafe
        "transport", "transit", "commute", "bus", "taxi" -> Icons.Default.Commute
        "car", "driving", "fuel", "gas" -> Icons.Default.DirectionsCar
        "shopping", "clothing", "groceries", "supermarket" -> Icons.Default.ShoppingBag
        "bills" -> Icons.Default.ReceiptLong
        "utilities", "electricity", "water", "bills", "electronics", "gadgets" -> Icons.Default.ReceiptLong
        "transfer" -> Icons.Default.SwapHoriz
        "movies" -> Icons.Default.Movie
        "entertainment", "movie", "netflix", "premium", "subscription", "recurring" -> Icons.Default.Movie
        "game", "games", "gaming" -> Icons.Default.SportsEsports
        "education", "tuition", "school" -> Icons.Default.School
        "housing", "rent", "home" -> Icons.Default.Home
        "health", "medical", "health & medical" -> Icons.Default.LocalHospital
        "software / ai", "software", "ai" -> Icons.Default.Memory
        "fitness", "gym" -> Icons.Default.FitnessCenter
        "travel", "holiday", "flight" -> Icons.Default.Flight
        "salary", "paycheck", "wages", "income" -> Icons.Default.Savings
        "bonus", "reward" -> Icons.Default.Add
        "investment", "dividend", "stocks" -> Icons.AutoMirrored.Filled.TrendingUp
        "work" -> Icons.Default.Work
        "part-time", "freelance", "side hustle" -> Icons.Default.Work
        "gift", "redpocket", "allowance", "petty cash" -> Icons.Default.CardGiftcard
        "pets", "pet" -> Icons.Default.Pets
        "修改結餘" -> Icons.Default.Tune
        "借錢", "還錢" -> Icons.Default.SwapHoriz
        else -> Icons.Default.Category
    }
    Icon(icon, contentDescription = category, tint = tint, modifier = modifier)
}

/**
 * Resolves responsive Bento grid column and item spacing based on available screen width.
 */
fun resolveBentoSpacing(screenWidthDp: Int): Dp = if (screenWidthDp < 400) 12.dp else 16.dp

/**
 * Resolves responsive screen horizontal/vertical outer padding based on available screen width.
 */
fun resolveScreenPadding(screenWidthDp: Int): Dp = if (screenWidthDp < 400) 12.dp else 16.dp

/**
 * Resolves responsive top bar horizontal padding based on available screen width.
 */
fun resolveTopBarPadding(screenWidthDp: Int): Dp = if (screenWidthDp < 400) 12.dp else 20.dp

/**
 * Daily financial vibe health categories based on daily spending ratio.
 */
enum class DailyVibeCategory {
    SAVING_VIBE,
    STEADY_VIBE,
    OVERSPENT_ALERT
}

/**
 * Categorizes current day's financial health vibe according to spending ratio.
 */
fun resolveDailyVibeCategory(dailyAllowance: Double, dailyRemaining: Double): DailyVibeCategory {
    val spentToday = dailyAllowance - dailyRemaining
    val ratioSpent = if (dailyAllowance > 0) spentToday / dailyAllowance else 0.0
    return when {
        ratioSpent <= 0.5 -> DailyVibeCategory.SAVING_VIBE
        ratioSpent <= 1.0 -> DailyVibeCategory.STEADY_VIBE
        else -> DailyVibeCategory.OVERSPENT_ALERT
    }
}

/**
 * Calculates total period duration in days between start and end epochs.
 */
fun calculatePeriodTotalDays(startDate: Long, endDate: Long): Int {
    return if (startDate > 0 && endDate > 0) {
        val startLocal = Instant.ofEpochMilli(startDate).atZone(ZoneId.systemDefault()).toLocalDate()
        val endLocal = Instant.ofEpochMilli(endDate).atZone(ZoneId.systemDefault()).toLocalDate()
        (ChronoUnit.DAYS.between(startLocal, endLocal) + 1).toInt()
    } else {
        30
    }
}

@Composable
fun getCategoryDisplayName(category: String): String {
    val normalized = category.trim().replaceFirst(Regex("^[^\\p{L}\\p{N}]+"), "").lowercase(Locale.US).trim()
    return when (normalized) {
        "food", "food & drink", "restaurant", "food & dining" -> stringResource(R.string.cat_food)
        "transport", "transit", "commute", "bus", "taxi" -> stringResource(R.string.cat_transport)
        "shopping", "clothing" -> stringResource(R.string.cat_shopping)
        "groceries", "supermarket" -> stringResource(R.string.cat_groceries)
        "electronics", "gadgets" -> stringResource(R.string.cat_electronics)
        "entertainment", "movie", "games", "gaming" -> stringResource(R.string.cat_entertainment)
        "utilities", "electricity", "water", "bills" -> stringResource(R.string.cat_utilities)
        "housing", "rent" -> stringResource(R.string.cat_housing)
        "health", "medical", "health & medical" -> stringResource(R.string.cat_health)
        "software / ai", "software", "ai", "subscription", "recurring" -> stringResource(R.string.cat_software_ai)
        "education", "tuition" -> stringResource(R.string.cat_education)
        "salary", "paycheck", "wages", "income" -> stringResource(R.string.cat_salary)
        "bonus", "reward" -> stringResource(R.string.cat_bonus)
        "investment", "dividend", "stocks" -> stringResource(R.string.cat_investment)
        "transfer" -> stringResource(R.string.filter_transfer)
        "part-time", "freelance", "side hustle" -> stringResource(R.string.loc_cat_part_time)
        "gift", "redpocket", "allowance" -> stringResource(R.string.loc_cat_gift)
        "other income" -> stringResource(R.string.loc_cat_other_income)
        "fitness", "gym" -> stringResource(R.string.loc_cat_fitness)
        "travel", "holiday" -> stringResource(R.string.loc_cat_travel)
        "others", "other" -> stringResource(R.string.cat_others)
        else -> category
    }
}

val LEFT_COLUMN_CARD_KEYS = setOf(
    "daily-hero",
    "daily-period",
    "daily-transactions",
    "daily-spending-chart"
)

val RIGHT_COLUMN_CARD_KEYS = setOf(
    "daily-bento",
    "daily-category-budgets",
    "daily-category-chart",
    "daily-calendar"
)

@Composable
fun resolveDailyCardTitle(cardKey: String): String = when (cardKey) {
    "daily-hero" -> stringResource(R.string.loc_daily_budget_hero)
    "daily-period" -> stringResource(R.string.loc_whole_budget_card)
    "daily-bento" -> stringResource(R.string.loc_bento_metrics)
    "daily-category-budgets" -> stringResource(R.string.loc_category_budgets_card)
    "daily-transactions" -> stringResource(R.string.loc_total_expenses)
    "daily-spending-chart" -> stringResource(R.string.loc_spending_trend)
    "daily-category-chart" -> stringResource(R.string.loc_category_analytics)
    "daily-calendar" -> stringResource(R.string.loc_financial_rhythm)
    else -> cardKey
}

@Composable
fun DualColumnPaneHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.3).sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            actions()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    state: FinanceUiState,
    onIntent: (FinanceIntent) -> Unit,
    modifier: Modifier = Modifier,
    topContentPadding: Dp = 16.dp,
    topVisibilityInset: Dp = 0.dp,
    bottomVisibilityInset: Dp = 0.dp,
    onViewAllClick: () -> Unit = {},
    onCategoryClick: (String) -> Unit = {},
    showAddDialog: Boolean = false,
    onDismissAddDialog: () -> Unit = {},
    onOpenBudgetDialog: () -> Unit = {},
    onOpenRecalcSheet: () -> Unit = {},
    onPayCard: (Long) -> Unit = {},
    listState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState(),
    bottomVisibilityInsetProvider: () -> Dp = { bottomVisibilityInset },
    useRail: Boolean = false
) {
    val budgetInfo = state.budgetInfo ?: DailyBudgetInfo(
        totalMonthlyBudget = 0.0,
        totalSpentThisMonth = 0.0,
        monthlyRemaining = 0.0,
        dailyAllowance = 0.0,
        dailyRemaining = 0.0,
        daysLeft = 0,
        startDate = 0L,
        endDate = 0L
    )

    // Dynamic glow color based on Daily Spent ratio
    val dailyVibe = resolveDailyVibeCategory(budgetInfo.dailyAllowance, budgetInfo.dailyRemaining)

    val glowColorAnimated by animateColorAsState(
        targetValue = when (dailyVibe) {
            DailyVibeCategory.SAVING_VIBE -> MaterialTheme.colorScheme.primary // Saving Vibe
            DailyVibeCategory.STEADY_VIBE -> MaterialTheme.colorScheme.secondary // Steady Vibe
            DailyVibeCategory.OVERSPENT_ALERT -> MaterialTheme.colorScheme.tertiary // Overspent Alert
        },
        animationSpec = tween(durationMillis = 600),
        label = "glowColor"
    )

    val dateLocale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
    val startDateText = remember(budgetInfo.startDate, dateLocale) {
        if (budgetInfo.startDate > 0) {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(budgetInfo.startDate), ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("dd MMM", dateLocale))
        } else {
            "—"
        }
    }
    val endDateText = remember(budgetInfo.endDate, dateLocale) {
        if (budgetInfo.endDate > 0) {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(budgetInfo.endDate), ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("dd MMM", dateLocale))
        } else {
            "—"
        }
    }
    val totalDays = remember(budgetInfo.startDate, budgetInfo.endDate) {
        calculatePeriodTotalDays(budgetInfo.startDate, budgetInfo.endDate)
    }

    // Dynamic extraction of lowest and highest expense values
    val periodExpenses = remember(state.transactions, budgetInfo.startDate, budgetInfo.endDate) {
        val endExclusive = Instant.ofEpochMilli(budgetInfo.endDate)
            .atZone(ZoneId.systemDefault()).toLocalDate().plusDays(1)
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        state.transactions.filter {
            it.toAccountId == null &&
            !it.isExcludedFromDailyBudget &&
            it.amount > 0 &&
            it.timestamp >= budgetInfo.startDate &&
            budgetInfo.endDate > 0L && it.timestamp < endExclusive
        }.sortedBy { it.timestamp }
    }
    val lowestExpense = remember(periodExpenses) {
        periodExpenses.minByOrNull { it.amount }
    }
    val highestExpense = remember(periodExpenses) {
        periodExpenses.maxByOrNull { it.amount }
    }

    val hapticView = LocalView.current
    var isEditMode by rememberSaveable { mutableStateOf(false) }

    val effectiveCardOrder = remember(state.dailyCardOrder) {
        val current = state.dailyCardOrder.filter { it in com.example.vibefinance.ui.DEFAULT_DAILY_CARD_ORDER }.toMutableList()
        com.example.vibefinance.ui.DEFAULT_DAILY_CARD_ORDER.forEach { if (!current.contains(it)) current.add(it) }
        current
    }

    com.example.vibefinance.ui.common.ContentEntranceViewport(
        modifier = modifier.fillMaxSize(), topVisibilityInset = topVisibilityInset,
        bottomVisibilityInsetProvider = bottomVisibilityInsetProvider, tagPrefix = "DailyArrival"
    ) {
        AnimatedContent(
            targetState = state.isLoading,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
            label = "homeSkeletonCrossfade"
        ) { isLoading ->
            if (isLoading) {
                com.example.vibefinance.ui.components.HomeScreenSkeleton()
            } else {
                val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val availableWidth = com.example.vibefinance.ui.main.LocalMainWindowWidth.current ?: maxWidth
                    val isDualColumn = availableWidth >= 600.dp

                    val leftKeys = remember(effectiveCardOrder) {
                        effectiveCardOrder.filter { it in LEFT_COLUMN_CARD_KEYS }
                    }
                    val rightKeys = remember(effectiveCardOrder) {
                        effectiveCardOrder.filter { it !in LEFT_COLUMN_CARD_KEYS }
                    }

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxHeight()
                                .widthIn(max = if (isDualColumn) 1240.dp else 680.dp)
                                .fillMaxWidth()
                                .testTag("DailyCardsList"),
                            contentPadding = PaddingValues(top = topContentPadding),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (isEditMode) {
                                item(key = "daily-edit-header") {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(20.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DashboardCustomize,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(
                                                    text = stringResource(R.string.loc_custom_bento_layout),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                TextButton(
                                                    onClick = {
                                                        com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                                        onIntent(com.example.vibefinance.ui.FinanceIntent.ResetDailyCardOrder)
                                                    }
                                                ) {
                                                    Text(
                                                        text = stringResource(R.string.loc_reset),
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                }
                                                FilledTonalButton(
                                                    onClick = {
                                                        com.example.vibefinance.util.VibeHaptics.successConfirm(hapticView)
                                                        isEditMode = false
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                                    modifier = Modifier.height(34.dp),
                                                    elevation = AppButtonDefaults.tonalElevation(),
                                                ) {
                                                    Text(
                                                        text = stringResource(R.string.loc_done),
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (isDualColumn) {
                                item(key = "daily-dual-column-body") {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("DailyDualColumn"),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        // Left Column: Primary Budget & Action Pane (50%)
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("DailyLeftColumn"),
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            if (useRail) {
                                                DualColumnPaneHeader(
                                                    title = stringResource(R.string.dual_column_left_title),
                                                    subtitle = stringResource(R.string.dual_column_left_subtitle),
                                                    modifier = Modifier
                                                        .padding(bottom = 2.dp)
                                                        .testTag("DualColumnLeftHeader")
                                                )
                                            }

                                            leftKeys.forEachIndexed { index, cardKey ->
                                                DailyBentoCardWrapper(
                                                    title = resolveDailyCardTitle(cardKey),
                                                    index = index,
                                                    totalCount = leftKeys.size,
                                                    isEditMode = isEditMode,
                                                    modifier = Modifier.testTag("DailyCard_$cardKey"),
                                                    onLongPress = { isEditMode = true },
                                                    onMoveUp = {
                                                        if (index > 0) {
                                                            com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                                            val newLeft = leftKeys.toMutableList()
                                                            java.util.Collections.swap(newLeft, index, index - 1)
                                                            val newOrder = newLeft + rightKeys
                                                            onIntent(com.example.vibefinance.ui.FinanceIntent.SetDailyCardOrder(newOrder))
                                                        }
                                                    },
                                                    onMoveDown = {
                                                        if (index < leftKeys.size - 1) {
                                                            com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                                            val newLeft = leftKeys.toMutableList()
                                                            java.util.Collections.swap(newLeft, index, index + 1)
                                                            val newOrder = newLeft + rightKeys
                                                            onIntent(com.example.vibefinance.ui.FinanceIntent.SetDailyCardOrder(newOrder))
                                                        }
                                                    }
                                                ) {
                                                    DailyCardContent(
                                                        cardKey = cardKey,
                                                        budgetInfo = budgetInfo,
                                                        state = state,
                                                        periodExpenses = periodExpenses,
                                                        isDark = isDark,
                                                        onOpenRecalcSheet = onOpenRecalcSheet,
                                                        onOpenBudgetDialog = onOpenBudgetDialog,
                                                        onViewAllClick = onViewAllClick,
                                                        onCategoryClick = onCategoryClick,
                                                        onPayCard = onPayCard
                                                    )
                                                }
                                            }
                                        }

                                        // Right Column: Analytics & Bento Pane (50%)
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("DailyRightColumn"),
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            if (useRail) {
                                                DualColumnPaneHeader(
                                                    title = stringResource(R.string.dual_column_right_title),
                                                    subtitle = stringResource(R.string.dual_column_right_subtitle),
                                                    modifier = Modifier
                                                        .padding(bottom = 2.dp)
                                                        .testTag("DualColumnRightHeader"),
                                                    actions = {
                                                        // Custom Bento Layout Toggle
                                                        FilledTonalIconButton(
                                                            onClick = {
                                                                com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                                                isEditMode = !isEditMode
                                                            },
                                                            modifier = Modifier.size(36.dp).testTag("DualColumnCustomLayoutBtn"),
                                                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                                                containerColor = if (isEditMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f),
                                                                contentColor = if (isEditMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        ) {
                                                            Icon(
                                                                imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.DashboardCustomize,
                                                                contentDescription = stringResource(R.string.loc_custom_bento_layout),
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }

                                                        // Recalculate Daily Budget
                                                        FilledTonalIconButton(
                                                            onClick = {
                                                                com.example.vibefinance.util.VibeHaptics.lightTick(hapticView)
                                                                onOpenRecalcSheet()
                                                            },
                                                            modifier = Modifier.size(36.dp).testTag("DualColumnRecalcBtn"),
                                                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f),
                                                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Refresh,
                                                                contentDescription = stringResource(R.string.btn_recalculate_budget),
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                    }
                                                )
                                            }

                                            rightKeys.forEachIndexed { index, cardKey ->
                                                DailyBentoCardWrapper(
                                                    title = resolveDailyCardTitle(cardKey),
                                                    index = index,
                                                    totalCount = rightKeys.size,
                                                    isEditMode = isEditMode,
                                                    modifier = Modifier.testTag("DailyCard_$cardKey"),
                                                    onLongPress = { isEditMode = true },
                                                    onMoveUp = {
                                                        if (index > 0) {
                                                            com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                                            val newRight = rightKeys.toMutableList()
                                                            java.util.Collections.swap(newRight, index, index - 1)
                                                            val newOrder = leftKeys + newRight
                                                            onIntent(com.example.vibefinance.ui.FinanceIntent.SetDailyCardOrder(newOrder))
                                                        }
                                                    },
                                                    onMoveDown = {
                                                        if (index < rightKeys.size - 1) {
                                                            com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                                            val newRight = rightKeys.toMutableList()
                                                            java.util.Collections.swap(newRight, index, index + 1)
                                                            val newOrder = leftKeys + newRight
                                                            onIntent(com.example.vibefinance.ui.FinanceIntent.SetDailyCardOrder(newOrder))
                                                        }
                                                    }
                                                ) {
                                                    DailyCardContent(
                                                        cardKey = cardKey,
                                                        budgetInfo = budgetInfo,
                                                        state = state,
                                                        periodExpenses = periodExpenses,
                                                        isDark = isDark,
                                                        onOpenRecalcSheet = onOpenRecalcSheet,
                                                        onOpenBudgetDialog = onOpenBudgetDialog,
                                                        onViewAllClick = onViewAllClick,
                                                        onCategoryClick = onCategoryClick,
                                                        onPayCard = onPayCard
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                if (useRail) {
                                    item(key = "daily-single-column-header") {
                                        DualColumnPaneHeader(
                                            title = stringResource(R.string.dual_column_left_title),
                                            subtitle = stringResource(R.string.dual_column_left_subtitle),
                                            modifier = Modifier
                                                .padding(horizontal = 4.dp, vertical = 4.dp)
                                                .testTag("DailySingleColumnHeader")
                                        )
                                    }
                                }
                                effectiveCardOrder.forEachIndexed { index, cardKey ->
                                    item(key = cardKey) {
                                        DailyBentoCardWrapper(
                                            title = resolveDailyCardTitle(cardKey),
                                            index = index,
                                            totalCount = effectiveCardOrder.size,
                                            isEditMode = isEditMode,
                                            onLongPress = { isEditMode = true },
                                            onMoveUp = {
                                                if (index > 0) {
                                                    com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                                    val newOrder = effectiveCardOrder.toMutableList()
                                                    java.util.Collections.swap(newOrder, index, index - 1)
                                                    onIntent(com.example.vibefinance.ui.FinanceIntent.SetDailyCardOrder(newOrder))
                                                }
                                            },
                                            onMoveDown = {
                                                if (index < effectiveCardOrder.size - 1) {
                                                    com.example.vibefinance.util.VibeHaptics.selection(hapticView)
                                                    val newOrder = effectiveCardOrder.toMutableList()
                                                    java.util.Collections.swap(newOrder, index, index + 1)
                                                    onIntent(com.example.vibefinance.ui.FinanceIntent.SetDailyCardOrder(newOrder))
                                                }
                                            },
                                            modifier = Modifier.animateItem().testTag("DailyCard_$cardKey")
                                        ) {
                                            DailyCardContent(
                                                cardKey = cardKey,
                                                budgetInfo = budgetInfo,
                                                state = state,
                                                periodExpenses = periodExpenses,
                                                isDark = isDark,
                                                onOpenRecalcSheet = onOpenRecalcSheet,
                                                onOpenBudgetDialog = onOpenBudgetDialog,
                                                onViewAllClick = onViewAllClick,
                                                onCategoryClick = onCategoryClick,
                                                onPayCard = onPayCard
                                            )
                                        }
                                    }
                                }
                            }

                            // Bottom space
                            item(key = "daily-bottom-space") {
                                Spacer(modifier = Modifier.height(100.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyCardContent(
    cardKey: String,
    budgetInfo: DailyBudgetInfo,
    state: FinanceUiState,
    periodExpenses: List<com.example.vibefinance.data.entity.TransactionEntity>,
    isDark: Boolean,
    onOpenRecalcSheet: () -> Unit,
    onOpenBudgetDialog: () -> Unit,
    onViewAllClick: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onPayCard: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    when (cardKey) {
        "daily-hero" -> {
            com.example.vibefinance.ui.common.PageContentEntrance("hero", modifier = modifier) {
                HeroDailyBudgetCard(
                    budgetInfo = budgetInfo,
                    onOpenRecalcSheet = onOpenRecalcSheet,
                    onOpenBudgetDialog = onOpenBudgetDialog
                )
            }
        }
        "daily-period" -> {
            com.example.vibefinance.ui.common.PageContentEntrance("period", delayMillis = 40, modifier = modifier) {
                WholeBudgetCard(
                    budget = budgetInfo.totalMonthlyBudget,
                    startDate = budgetInfo.startDate,
                    endDate = budgetInfo.endDate,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    onClick = onOpenBudgetDialog
                )
            }
        }
        "daily-category-budgets" -> {
            com.example.vibefinance.ui.common.PageContentEntrance("category-budgets", delayMillis = 60, modifier = modifier) {
                CategoryBudgetsCard(
                    budgetInfo = budgetInfo,
                    onOpenBudgetDialog = onOpenBudgetDialog
                )
            }
        }
        "daily-bento" -> {
            val screenWidthDp = LocalConfiguration.current.screenWidthDp
            val bentoSpacing = resolveBentoSpacing(screenWidthDp)

            Column(
                verticalArrangement = Arrangement.spacedBy(bentoSpacing),
                modifier = modifier.fillMaxWidth()
            ) {
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val bentoMaxWidth = maxWidth
                    val daysSide = minOf(((bentoMaxWidth - bentoSpacing) * 0.4f), 156.dp).coerceAtLeast(110.dp)
                    val candidateRestWidth = bentoMaxWidth - bentoSpacing - daysSide
                    val candidateRestPadding = (candidateRestWidth * 0.12f).coerceIn(12.dp, 18.dp) * 2
                    val candidateTextWidthPx = with(LocalDensity.current) { (candidateRestWidth - candidateRestPadding).toPx() }

                    val measurer = rememberTextMeasurer()
                    val measuringStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)

                    val remText = formatBudgetCardAmount(budgetInfo.monthlyRemaining)
                    val spentText = formatBudgetCardAmount((budgetInfo.totalMonthlyBudget - budgetInfo.monthlyRemaining).coerceAtLeast(0.0))

                    val remWidthAt12 = measurer.measure(
                        text = AnnotatedString(remText),
                        style = measuringStyle.copy(fontSize = 12.sp),
                        softWrap = false,
                        maxLines = 1,
                    ).size.width
                    val spentWidthAt12 = measurer.measure(
                        text = AnnotatedString(spentText),
                        style = measuringStyle.copy(fontSize = 12.sp),
                        softWrap = false,
                        maxLines = 1,
                    ).size.width

                    var childReportedOverflow by remember { mutableStateOf(false) }

                    val shouldStack = shouldStackBento(
                        maxWidth = bentoMaxWidth,
                        bentoSpacing = bentoSpacing,
                        fontScale = LocalDensity.current.fontScale,
                        remTextWidthAt12sp = remWidthAt12.toFloat(),
                        spentTextWidthAt12sp = spentWidthAt12.toFloat(),
                        candidateAvailableWidthPx = candidateTextWidthPx
                    ) || childReportedOverflow

                    val totalPeriodDays = remember(budgetInfo.startDate, budgetInfo.endDate) {
                        calculatePeriodTotalDays(budgetInfo.startDate, budgetInfo.endDate)
                    }

                    if (shouldStack) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(bentoSpacing),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            com.example.vibefinance.ui.common.PageContentEntrance(
                                "remaining", delayMillis = 80,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                RestAndSpentBudgetCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(144.dp),
                                    remainingBudget = budgetInfo.monthlyRemaining,
                                    totalBudget = budgetInfo.totalMonthlyBudget,
                                    isDarkTheme = isDark,
                                    onOverflowChanged = { childReportedOverflow = it }
                                )
                            }

                            val stackedDaysSize = calculateStackedDaysSize(bentoMaxWidth)
                            com.example.vibefinance.ui.common.PageContentEntrance(
                                "days", delayMillis = 110,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                DaysLeftCard(
                                    modifier = Modifier.size(stackedDaysSize),
                                    daysLeft = budgetInfo.daysLeft,
                                    totalDays = totalPeriodDays
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(daysSide),
                            horizontalArrangement = Arrangement.spacedBy(bentoSpacing),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            com.example.vibefinance.ui.common.PageContentEntrance(
                                "remaining", delayMillis = 80,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                RestAndSpentBudgetCard(
                                    modifier = Modifier.fillMaxSize(),
                                    remainingBudget = budgetInfo.monthlyRemaining,
                                    totalBudget = budgetInfo.totalMonthlyBudget,
                                    isDarkTheme = isDark,
                                    onOverflowChanged = { childReportedOverflow = it }
                                )
                            }
                            com.example.vibefinance.ui.common.PageContentEntrance(
                                "days", delayMillis = 110,
                                modifier = Modifier.size(daysSide)
                            ) {
                                DaysLeftCard(
                                    modifier = Modifier.fillMaxSize(),
                                    daysLeft = budgetInfo.daysLeft,
                                    totalDays = totalPeriodDays
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(bentoSpacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    com.example.vibefinance.ui.common.PageContentEntrance(
                        "lowest",
                        modifier = Modifier
                            .weight(1f)
                            .height(128.dp)
                    ) {
                        MinMaxSpentCard(
                            modifier = Modifier.fillMaxSize(), isMin = true,
                            spends = periodExpenses, isDarkTheme = isDark
                        )
                    }
                    com.example.vibefinance.ui.common.PageContentEntrance(
                        "highest", delayMillis = 40,
                        modifier = Modifier
                            .weight(1f)
                            .height(128.dp)
                    ) {
                        MinMaxSpentCard(
                            modifier = Modifier.fillMaxSize(), isMin = false,
                            spends = periodExpenses, isDarkTheme = isDark
                        )
                    }
                }
            }
        }
        "daily-transactions" -> {
            com.example.vibefinance.ui.common.PageContentEntrance("transactions", modifier = modifier) {
                TotalExpensesRow(state = state, onViewAllClick = onViewAllClick)
            }
        }
        "daily-spending-chart" -> {
            com.example.vibefinance.ui.common.PageContentEntrance("spending-chart", modifier = modifier) {
                DailySpendingLineChart(state = state)
            }
        }
        "daily-category-chart" -> {
            com.example.vibefinance.ui.common.PageContentEntrance("category-chart", modifier = modifier) {
                CategoryDonutChart(
                    state = state,
                    onCategoryClick = onCategoryClick
                )
            }
        }
        "daily-calendar" -> {
            com.example.vibefinance.ui.common.PageContentEntrance("calendar", modifier = modifier) {
                SpendsCalendar(state = state, onPayCard = onPayCard)
            }
        }
    }
}

@Composable
fun TotalExpensesRow(
    state: FinanceUiState,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCount = state.transactions.size
    val totalExpenses = state.transactions.filter { it.toAccountId == null && !it.isExcludedFromDailyBudget && it.amount > 0 }.sumOf { it.amount }
    
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val isCompact = screenWidthDp < 400

    val pressState = com.example.vibefinance.ui.common.rememberExpressiveCardPress(
        restingCorner = 28.dp,
        pressedCorner = 16.dp
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = pressState.shape,
        modifier = modifier
            .fillMaxWidth()
            .expressiveCardClickable(
                pressState = pressState,
                rippleColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) { onViewAllClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isCompact) 14.dp else 20.dp, vertical = if (isCompact) 14.dp else 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.total_expenses),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.transactions_logged_count, totalCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = String.format(Locale.US, "$%.2f", totalExpenses),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.view_details),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

private fun blendColors(colorA: Color, colorB: Color, angle: Float): Color {
    val a = angle.coerceIn(0f, 1f)
    return Color(
        red = colorA.red * (1f - a) + colorB.red * a,
        green = colorA.green * (1f - a) + colorB.green * a,
        blue = colorA.blue * (1f - a) + colorB.blue * a,
        alpha = colorA.alpha * (1f - a) + colorB.alpha * a
    )
}



/**
 * Expressive categorical color palette for the daily donut chart.
 * Uses unified ChartColors for 100% color scheme consistency across all 4 pages.
 */
fun getDailySemanticCategoryColor(category: String, isDark: Boolean): Color? =
    com.example.vibefinance.theme.ChartColors.getSemanticCategoryColor(category, isDark)

fun getDailyFallbackPalette(isDark: Boolean): List<Color> =
    com.example.vibefinance.theme.ChartColors.getFallbackPalette(isDark)

fun buildDailyCategoryColorMap(
    categories: List<String>,
    isDark: Boolean,
    primaryColor: Color
): Map<String, Color> =
    com.example.vibefinance.theme.ChartColors.buildCategoryColorMap(categories, isDark, primaryColor)

@Composable
fun CategoryDonutChart(
    state: FinanceUiState,
    modifier: Modifier = Modifier,
    onCategoryClick: (String) -> Unit = {}
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f || isSystemInDarkTheme()
    val primaryColor = MaterialTheme.colorScheme.primary
    
    val categorySpending = remember(state.transactions) {
        val map = mutableMapOf<String, Double>()
        state.transactions.forEach { tx ->
            if (tx.toAccountId == null && !tx.isExcludedFromDailyBudget && tx.amount > 0) {
                map[tx.category] = (map[tx.category] ?: 0.0) + tx.amount
            }
        }
        map.toList().sortedByDescending { it.second }
    }
    
    val totalSpending = remember(categorySpending) {
        categorySpending.sumOf { it.second }
    }
    
    val categoryColors = remember(categorySpending, isDark, primaryColor) {
        buildDailyCategoryColorMap(
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

    val pressState = com.example.vibefinance.ui.common.rememberExpressiveCardPress()
    val cardShape = pressState.shape

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = cardShape,
        modifier = modifier
            .fillMaxWidth()
            .testTag("DailyCategoryAnalyticsCard")
            .expressiveCardClickable(pressState, hoverEnabled = false) {}
            .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow))
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), cardShape)
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
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .testTag("DailyCategoryAnalyticsTitle")
                        .clickable(enabled = categorySpending.isNotEmpty()) {
                            val target = selectedCategory ?: categorySpending.firstOrNull()?.first
                            if (target != null) {
                                onCategoryClick(target)
                            }
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.category_analytics_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.loc_category_details),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
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
                    modifier = Modifier
                        .size(118.dp)
                        .pointerInput(categorySpending, totalSpending) {
                            detectTapGestures { offset ->
                                if (totalSpending > 0) {
                                    val cx = size.width / 2f
                                    val cy = size.height / 2f
                                    val dx = offset.x - cx
                                    val dy = offset.y - cy
                                    var angle = Math.toDegrees(kotlin.math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                    var relAngle = (angle - (-90f)) % 360f
                                    if (relAngle < 0f) relAngle += 360f

                                    var currentAngle = 0f
                                    for ((category, amount) in categorySpending) {
                                        val sweep = ((amount / totalSpending) * 360f).toFloat()
                                        if (relAngle >= currentAngle && relAngle < currentAngle + sweep) {
                                            selectedCategory = category
                                            onCategoryClick(category)
                                            break
                                        }
                                        currentAngle += sweep
                                    }
                                }
                            }
                        },
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
                            var startAngle = -90f
                            categorySpending.forEach { (category, amount) ->
                                val sweepAngle = ((amount / totalSpending) * 360f).toFloat()
                                val color = categoryColors[category] ?: Color.Gray
                                
                                val (strokeState, alphaState) = categoryAnimProps[category]
                                    ?: (mutableStateOf(16.dp) to mutableStateOf(1f))
                                
                                val animatedStrokePx = strokeState.value.toPx()
                                val animatedAlpha = alphaState.value

                                drawArc(
                                    color = color.copy(alpha = animatedAlpha),
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    style = Stroke(width = animatedStrokePx, cap = StrokeCap.Round)
                                )
                                startAngle += sweepAngle
                            }
                        }
                    }
                    
                    // Animated Center Content transition on focus/unfocus
                    AnimatedContent(
                        targetState = selectedCategory,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.85f))
                                .togetherWith(fadeOut(animationSpec = tween(180)) + scaleOut(targetScale = 0.85f))
                        },
                        label = "centerDonutText"
                    ) { currentCategory ->
                        val selectedItem = categorySpending.find { it.first == currentCategory }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (selectedItem != null) {
                                val percent = if (totalSpending > 0) (selectedItem.second / totalSpending * 100).toInt() else 0
                                Text(
                                    text = selectedItem.first,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = categoryColors[selectedItem.first] ?: MaterialTheme.colorScheme.primary,
                                    maxLines = 1
                                )
                                Text(
                                    text = String.format(Locale.US, "$%.0f", selectedItem.second),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$percent%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                    fontWeight = FontWeight.ExtraBold
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.total_spent),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = String.format(Locale.US, "$%.0f", totalSpending),
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
                            text = stringResource(R.string.loc_no_category_data),
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
                                    .testTag("DailyCategoryAnalyticsItem_$category")
                                    .clickable {
                                        selectedCategory = category
                                        onCategoryClick(category)
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
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = String.format(Locale.US, "$%.2f", amount),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        val limit = state.categoryLimits[category]
                                        if (limit != null && limit > 0) {
                                            val percent = (amount / limit * 100).toInt()
                                            val (textColor, textLabel) = when {
                                                percent >= 100 -> MaterialTheme.colorScheme.error to stringResource(R.string.loc_over_limit_percent, percent)
                                                percent >= 80 -> Color(0xFFE65100) to stringResource(R.string.loc_warning_percent, percent)
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) to stringResource(R.string.loc_percent_of_limit, percent)
                                            }
                                            Text(
                                                text = textLabel,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = textColor,
                                                fontWeight = FontWeight.Bold
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

data class VibeInsight(
    val title: String,
    val message: String,
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Preview(name = "Daily Bento Standard Side-by-Side Light (380dp)", showBackground = true, widthDp = 380)
@Composable
fun PreviewDailyBentoSideBySide() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Box(modifier = Modifier.padding(16.dp)) {
            val bentoSpacing = 16.dp
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                horizontalArrangement = Arrangement.spacedBy(bentoSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RestAndSpentBudgetCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    remainingBudget = 2439.5,
                    totalBudget = 2500.0,
                    isDarkTheme = false
                )
                DaysLeftCard(
                    modifier = Modifier.size(140.dp),
                    daysLeft = 18,
                    totalDays = 30
                )
            }
        }
    }
}

@Preview(name = "Daily Bento Narrow Stacked (320dp)", showBackground = true, widthDp = 320)
@Composable
fun PreviewDailyBentoNarrowStacked() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Box(modifier = Modifier.padding(12.dp)) {
            val bentoSpacing = 12.dp
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(bentoSpacing),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RestAndSpentBudgetCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(144.dp),
                    remainingBudget = -12345.67,
                    totalBudget = 3000.0,
                    isDarkTheme = false
                )
                DaysLeftCard(
                    modifier = Modifier.size(140.dp),
                    daysLeft = 5,
                    totalDays = 30
                )
            }
        }
    }
}

@Preview(name = "Daily Bento High Font Scale (1.5x) Stacked", showBackground = true, widthDp = 360, fontScale = 1.5f)
@Composable
fun PreviewDailyBentoHighFontScaleStacked() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Box(modifier = Modifier.padding(12.dp)) {
            val bentoSpacing = 12.dp
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(bentoSpacing),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RestAndSpentBudgetCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(144.dp),
                    remainingBudget = -1234567.89,
                    totalBudget = 5000.0,
                    isDarkTheme = false
                )
                DaysLeftCard(
                    modifier = Modifier.size(140.dp),
                    daysLeft = 12,
                    totalDays = 30
                )
            }
        }
    }
}

@Preview(name = "Daily Bento Dark Theme Side-by-Side", showBackground = true, widthDp = 380)
@Composable
fun PreviewDailyBentoDark() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Box(modifier = Modifier.padding(16.dp)) {
            val bentoSpacing = 16.dp
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                horizontalArrangement = Arrangement.spacedBy(bentoSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RestAndSpentBudgetCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    remainingBudget = 1800.0,
                    totalBudget = 2500.0,
                    isDarkTheme = true
                )
                DaysLeftCard(
                    modifier = Modifier.size(140.dp),
                    daysLeft = 20,
                    totalDays = 30
                )
            }
        }
    }
}
