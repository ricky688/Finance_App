package com.example.vibefinance.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.res.stringResource
import com.example.vibefinance.R
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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Commute
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import com.example.vibefinance.theme.ThemeMode
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun CategoryIcon(category: String, tint: Color, modifier: Modifier = Modifier) {
    val icon = when (category.lowercase(Locale.US)) {
        "food", "food & drink", "restaurant" -> Icons.Default.Restaurant
        "transport", "transit", "commute", "bus", "taxi" -> Icons.Default.Commute
        "shopping", "clothing", "groceries" -> Icons.Default.ShoppingBag
        "utilities", "electricity", "water", "bills", "electronics", "gadgets" -> Icons.Default.ElectricBolt
        "transfer" -> Icons.Default.SwapHoriz
        "entertainment", "netflix", "premium", "subscription", "recurring" -> Icons.Default.AutoMode
        "salary", "paycheck", "wages" -> Icons.Default.Savings
        "bonus", "reward" -> Icons.Default.Add
        "investment", "dividend", "stocks" -> Icons.AutoMirrored.Filled.TrendingUp
        "part-time", "freelance", "side hustle" -> Icons.Default.AccountBalance
        "gift", "redpocket", "allowance" -> Icons.Default.Savings
        else -> Icons.Default.MoreHoriz
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
    return when (category.lowercase(Locale.US).trim()) {
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
        "others", "other" -> stringResource(R.string.cat_others)
        else -> category
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    state: FinanceUiState,
    onIntent: (FinanceIntent) -> Unit,
    modifier: Modifier = Modifier,
    topContentPadding: Dp = 16.dp,
    onViewAllClick: () -> Unit = {},
    onCategoryClick: (String) -> Unit = {},
    showAddDialog: Boolean = false,
    onDismissAddDialog: () -> Unit = {},
    onOpenBudgetDialog: () -> Unit = {},
    onOpenRecalcSheet: () -> Unit = {}
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

    val startDateText = remember(budgetInfo.startDate) {
        if (budgetInfo.startDate > 0) {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(budgetInfo.startDate), ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("dd MMM", Locale.US))
        } else {
            "N/A"
        }
    }
    val endDateText = remember(budgetInfo.endDate) {
        if (budgetInfo.endDate > 0) {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(budgetInfo.endDate), ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("dd MMM", Locale.US))
        } else {
            "N/A"
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

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = state.isLoading,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
            label = "homeSkeletonCrossfade"
        ) { isLoading ->
            if (isLoading) {
                com.example.vibefinance.ui.components.HomeScreenSkeleton()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = topContentPadding),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
            // ICONIC HERO DAILY REMAINING BUDGET CARD (Option 5)
            item {
                HeroDailyBudgetCard(
                    budgetInfo = budgetInfo,
                    onOpenRecalcSheet = onOpenRecalcSheet,
                    onOpenBudgetDialog = onOpenBudgetDialog
                )
            }

            // B. START BUDGET & PERIOD CARD (WholeBudgetCard)
            item {
                WholeBudgetCard(
                    modifier = Modifier
                        .clip(BentoCardShape)
                        .bouncyClickable(shape = BentoCardShape) { onOpenBudgetDialog() },
                    budget = budgetInfo.totalMonthlyBudget,
                    startDate = budgetInfo.startDate,
                    endDate = budgetInfo.endDate,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            // C. BENTO MATRIX SECTION
            item {
                val screenWidthDp = LocalConfiguration.current.screenWidthDp
                val bentoSpacing = resolveBentoSpacing(screenWidthDp)

                Column(
                    verticalArrangement = Arrangement.spacedBy(bentoSpacing),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Left: Remaining Budget, Right: Circular Days Left & Stats
                    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(bentoSpacing),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RestAndSpentBudgetCard(
                            modifier = Modifier
                                .weight(1.2f)
                                .height(160.dp),
                            remainingBudget = budgetInfo.monthlyRemaining,
                            totalBudget = budgetInfo.totalMonthlyBudget,
                            isDarkTheme = isDark
                        )
                        val totalPeriodDays = remember(budgetInfo.startDate, budgetInfo.endDate) {
                            calculatePeriodTotalDays(budgetInfo.startDate, budgetInfo.endDate)
                        }
                        DaysLeftCard(
                            modifier = Modifier
                                .weight(0.8f)
                                .height(160.dp),
                            daysLeft = budgetInfo.daysLeft,
                            totalDays = totalPeriodDays
                        )
                    }

                    // Row of Lowest & Highest Stats
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(bentoSpacing),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MinMaxSpentCard(
                            modifier = Modifier
                                .weight(1f)
                                .height(128.dp),
                            isMin = true,
                            spends = periodExpenses,
                            isDarkTheme = isDark
                        )
                        MinMaxSpentCard(
                            modifier = Modifier
                                .weight(1f)
                                .height(128.dp),
                            isMin = false,
                            spends = periodExpenses,
                            isDarkTheme = isDark
                        )
                    }
                }
            }

            item {
                TotalExpensesRow(state = state, onViewAllClick = onViewAllClick)
            }
            item {
                DailySpendingLineChart(state = state)
            }
            item {
                CategoryDonutChart(
                    state = state,
                    onCategoryClick = onCategoryClick
                )
            }
            item {
                SpendsCalendar(state = state)
            }

            // Bottom space
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
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

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = BentoCardShape,
        modifier = modifier
            .fillMaxWidth()
            .clip(BentoCardShape)
            .bouncyClickable(shape = BentoCardShape) { onViewAllClick() }
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
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
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
                        text = "Category Analytics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "View Category Details",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                AnimatedVisibility(
                    visible = selectedCategory != null,
                    enter = fadeIn(animationSpec = tween(200)) + expandVertically(),
                    exit = fadeOut(animationSpec = tween(150)) + shrinkVertically()
                ) {
                    Text(
                        text = "Reset Filter",
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
                                    text = "Total Spent",
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
                            text = "No category data available.",
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
                                            text = category,
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
                                                percent >= 100 -> MaterialTheme.colorScheme.error to "Over limit ($percent%)"
                                                percent >= 80 -> Color(0xFFE65100) to "Warning ($percent%)"
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) to "$percent% of limit"
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
