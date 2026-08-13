package com.example.vibefinance.ui.home

import androidx.compose.animation.animateColorAsState
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
import com.example.vibefinance.ui.common.bouncyClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Commute
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
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
        "investment", "dividend", "stocks" -> Icons.Default.TrendingUp
        "part-time", "freelance", "side hustle" -> Icons.Default.AccountBalance
        "gift", "redpocket", "allowance" -> Icons.Default.Savings
        else -> Icons.Default.MoreHoriz
    }
    Icon(icon, contentDescription = category, tint = tint, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    state: FinanceUiState,
    onIntent: (FinanceIntent) -> Unit,
    modifier: Modifier = Modifier,
    onViewAllClick: () -> Unit = {},
    showAddDialog: Boolean = false,
    onDismissAddDialog: () -> Unit = {},
    onOpenBudgetDialog: () -> Unit = {},
    onOpenRecalcSheet: () -> Unit = {}
) {

    val budgetInfo = state.budgetInfo ?: DailyBudgetInfo(
        totalMonthlyBudget = 1500.0,
        totalSpentThisMonth = 0.0,
        monthlyRemaining = 1500.0,
        dailyAllowance = 50.0,
        dailyRemaining = 50.0,
        daysLeft = 30,
        startDate = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        endDate = LocalDate.now().plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    // Dynamic glow color based on Daily Spent ratio
    val spentToday = budgetInfo.dailyAllowance - budgetInfo.dailyRemaining
    val ratioSpent = if (budgetInfo.dailyAllowance > 0) spentToday / budgetInfo.dailyAllowance else 0.0

    val glowColorAnimated by animateColorAsState(
        targetValue = when {
            ratioSpent <= 0.5 -> MaterialTheme.colorScheme.primary // Saving Vibe
            ratioSpent <= 1.0 -> MaterialTheme.colorScheme.secondary // Steady Vibe
            else -> MaterialTheme.colorScheme.tertiary              // Overspent Alert
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
        if (budgetInfo.startDate > 0 && budgetInfo.endDate > 0) {
            val startLocal = Instant.ofEpochMilli(budgetInfo.startDate).atZone(ZoneId.systemDefault()).toLocalDate()
            val endLocal = Instant.ofEpochMilli(budgetInfo.endDate).atZone(ZoneId.systemDefault()).toLocalDate()
            (ChronoUnit.DAYS.between(startLocal, endLocal) + 1).toInt()
        } else {
            30
        }
    }

    // Dynamic extraction of lowest and highest expense values
    val periodExpenses = remember(state.transactions, budgetInfo.startDate, budgetInfo.endDate) {
        state.transactions.filter {
            it.toAccountId == null &&
            !it.isExcludedFromDailyBudget &&
            it.amount > 0 &&
            it.timestamp >= budgetInfo.startDate &&
            it.timestamp <= budgetInfo.endDate
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
            // A. TOP PROFILE APP BAR SPACER
            item {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(72.dp)
                )
            }
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
                        .clip(RoundedCornerShape(24.dp))
                        .bouncyClickable { onOpenBudgetDialog() },
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
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Left: Remaining Budget, Right: Circular Days Left & Stats
                    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
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
                        Spacer(modifier = Modifier.width(16.dp))
                        val totalDays = remember(budgetInfo.startDate, budgetInfo.endDate) {
                            if (budgetInfo.startDate > 0 && budgetInfo.endDate > 0) {
                                val startLocal = Instant.ofEpochMilli(budgetInfo.startDate).atZone(ZoneId.systemDefault()).toLocalDate()
                                val endLocal = Instant.ofEpochMilli(budgetInfo.endDate).atZone(ZoneId.systemDefault()).toLocalDate()
                                (ChronoUnit.DAYS.between(startLocal, endLocal) + 1).toFloat()
                            } else {
                                30f
                            }
                        }
                        DaysLeftCard(
                            modifier = Modifier
                                .weight(0.8f)
                                .height(160.dp),
                            daysLeft = budgetInfo.daysLeft,
                            totalDays = totalDays.toInt()
                        )
                    }

                    // Row of Lowest & Highest Stats
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
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
                CategoryDonutChart(state = state)
            }
            item {
                SpendsCalendar(state = state)
            }

            // Bottom space
            item {
                Spacer(modifier = Modifier.height(80.dp))
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
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onViewAllClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Total Expenses",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$totalCount transactions logged",
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
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "View Details",
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



@Composable
fun CategoryDonutChart(
    state: FinanceUiState,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.inversePrimary,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer
    )
    
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
    
    val categoryColors = remember(categorySpending, colors) {
        categorySpending.mapIndexed { index, pair ->
            pair.first to colors[index % colors.size]
        }.toMap()
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
                    text = "Category Analytics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
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
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Interactive Animated Donut Chart Canvas
                Box(
                    modifier = Modifier.size(136.dp),
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
