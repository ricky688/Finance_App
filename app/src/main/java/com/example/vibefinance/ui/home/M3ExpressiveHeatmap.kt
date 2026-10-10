package com.example.vibefinance.ui.home

import com.example.vibefinance.R
import androidx.compose.ui.res.stringResource
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import com.example.vibefinance.ui.preferences.PrivacyText as Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.theme.JetBrainsMonoFontFamily
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class HeatmapTileShape(val label: String) {
    SQUIRCLE("Squircle"),
    PEBBLE_PILL("Pill"),
    SMOOTH_GLOW("Glow")
}

enum class DaySpendingVibe {
    ZERO_SPEND,
    UNDER_BUDGET,
    ON_TARGET,
    OVER_BUDGET,
    FUTURE
}

data class HeatmapDayData(
    val date: LocalDate,
    val spent: Double,
    val vibe: DaySpendingVibe,
    val isToday: Boolean,
    val isFuture: Boolean,
    val transactionsCount: Int
)

data class HeatmapMatrix(
    val weeks: List<List<HeatmapDayData>>, // 16 columns of 7 days (Mon=0..Sun=6)
    val currentStreak: Int,
    val totalTransactions: Int,
    val dailyAverage: Double,
    val adherencePercent: Int
)

object HeatmapCalculator {
    fun calculate(
        transactions: List<TransactionEntity>,
        dailyBudget: Double,
        today: LocalDate = LocalDate.now(),
        weeksCount: Int = 16
    ): HeatmapMatrix {
        val validTxs = transactions.filter {
            it.toAccountId == null && !it.isExcludedFromDailyBudget && it.amount > 0
        }

        // Group spending & count by date
        val daySpentMap = mutableMapOf<LocalDate, Double>()
        val dayTxCountMap = mutableMapOf<LocalDate, Int>()
        validTxs.forEach { tx ->
            val date = Instant.ofEpochMilli(tx.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
            daySpentMap[date] = (daySpentMap[date] ?: 0.0) + tx.amount
            dayTxCountMap[date] = (dayTxCountMap[date] ?: 0) + 1
        }

        // 16-week date grid ending on current week's Sunday
        val endOfCurrentWeek = today.plusDays((7 - today.dayOfWeek.value).toLong())
        val matrixStartDate = endOfCurrentWeek.minusDays((weeksCount * 7 - 1).toLong())

        val weeks = mutableListOf<List<HeatmapDayData>>()
        for (w in 0 until weeksCount) {
            val weekDays = mutableListOf<HeatmapDayData>()
            for (d in 0 until 7) {
                val cellDate = matrixStartDate.plusDays((w * 7 + d).toLong())
                val isFuture = cellDate.isAfter(today)
                val isToday = cellDate == today
                val spent = daySpentMap[cellDate] ?: 0.0
                val txCount = dayTxCountMap[cellDate] ?: 0

                val vibe = when {
                    isFuture -> DaySpendingVibe.FUTURE
                    spent <= 0.001 -> DaySpendingVibe.ZERO_SPEND
                    dailyBudget > 0.0 && spent <= dailyBudget * 0.8 -> DaySpendingVibe.UNDER_BUDGET
                    dailyBudget > 0.0 && spent <= dailyBudget -> DaySpendingVibe.ON_TARGET
                    else -> DaySpendingVibe.OVER_BUDGET
                }

                weekDays.add(
                    HeatmapDayData(
                        date = cellDate,
                        spent = spent,
                        vibe = vibe,
                        isToday = isToday,
                        isFuture = isFuture,
                        transactionsCount = txCount
                    )
                )
            }
            weeks.add(weekDays)
        }

        // Current streak: consecutive days ending today/yesterday where spent <= dailyBudget
        var streak = 0
        if (validTxs.isNotEmpty() && dailyBudget > 0.0) {
            val oldestDate = validTxs.minOf {
                Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
            }
            val todaySpent = daySpentMap[today] ?: 0.0
            var checkDate = today
            if (todaySpent <= dailyBudget) {
                if (todaySpent > 0.0) {
                    streak++
                    checkDate = today.minusDays(1)
                } else {
                    checkDate = today.minusDays(1)
                }
                while (!checkDate.isBefore(oldestDate) && streak < 365) {
                    val spent = daySpentMap[checkDate] ?: 0.0
                    if (spent <= dailyBudget) {
                        streak++
                        checkDate = checkDate.minusDays(1)
                    } else {
                        break
                    }
                }
            }
        }

        // Stats over the 16-week window
        var totalPastDays = 0
        var adherenceDays = 0
        var windowTotalSpent = 0.0
        var windowTotalTx = 0
        for (w in 0 until weeksCount) {
            for (d in 0 until 7) {
                val cellDate = matrixStartDate.plusDays((w * 7 + d).toLong())
                if (!cellDate.isAfter(today)) {
                    totalPastDays++
                    val spent = daySpentMap[cellDate] ?: 0.0
                    windowTotalSpent += spent
                    windowTotalTx += dayTxCountMap[cellDate] ?: 0
                    if (dailyBudget > 0.0 && spent <= dailyBudget) {
                        adherenceDays++
                    }
                }
            }
        }

        val dailyAvg = if (totalPastDays > 0) windowTotalSpent / totalPastDays else 0.0
        val adherence = if (totalPastDays > 0) ((adherenceDays.toDouble() / totalPastDays) * 100).toInt() else 100

        return HeatmapMatrix(
            weeks = weeks,
            currentStreak = streak,
            totalTransactions = windowTotalTx,
            dailyAverage = dailyAvg,
            adherencePercent = adherence
        )
    }
}

@Composable
fun M3ExpressiveHeatmap(
    transactions: List<TransactionEntity>,
    dailyBudget: Double,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onViewTransactionsForDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val matrix = remember(transactions, dailyBudget, today) {
        HeatmapCalculator.calculate(
            transactions = transactions,
            dailyBudget = dailyBudget,
            today = today,
            weeksCount = 16
        )
    }

    var tileShapeMode by remember { mutableStateOf(HeatmapTileShape.SQUIRCLE) }
    val activeDate = selectedDate ?: today

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Streak & Summary Banner (Glassmorphic M3 Expressive Card)
        HeroStreakCard(
            matrix = matrix,
            isDark = isDark
        )

        // 2. 16-Week Heatmap Grid
        HeatmapGridSection(
            matrix = matrix,
            selectedDate = activeDate,
            tileShapeMode = tileShapeMode,
            isDark = isDark,
            onCellClick = onDateSelected
        )

        // 3. Tile Shape Selector Pills
        TileShapeSelectorRow(
            currentShape = tileShapeMode,
            onSelectShape = { tileShapeMode = it }
        )

        // 4. Inspection Callout Card for Selected Day
        InspectionCalloutCard(
            date = activeDate,
            dailyBudget = dailyBudget,
            transactions = transactions,
            isDark = isDark,
            onViewTransactions = { onViewTransactionsForDate(activeDate) }
        )

        // 5. Visual Legend Bar
        HeatmapLegendBar(isDark = isDark)
    }
}

@Composable
private fun HeroStreakCard(
    matrix: HeatmapMatrix,
    isDark: Boolean
) {
    val cardBg = if (isDark) {
        Color(0xFF141519)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    val glowColor = Color(0xFFF59E0B) // Amber Glow for Streaks

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = cardBg,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDark) Color(0x33F59E0B) else Color(0x22F59E0B)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Row: Streak Pill Badge & Live Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = glowColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, glowColor.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = glowColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.loc_budget_streak, matrix.currentStreak),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = glowColor
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Text(
                        text = stringResource(R.string.loc_live_sync),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Metric: Daily Avg & Supporting Tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.loc_avg_16_weeks),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = String.format(Locale.US, "$%,.2f", matrix.dailyAverage),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = JetBrainsMonoFontFamily,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Stacked Adherence & Logs Pills (Clean, unconstrained layout)
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = stringResource(R.string.loc_adherence, matrix.adherencePercent),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = stringResource(R.string.loc_logs, matrix.totalTransactions),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeatmapGridSection(
    matrix: HeatmapMatrix,
    selectedDate: LocalDate,
    tileShapeMode: HeatmapTileShape,
    isDark: Boolean,
    onCellClick: (LocalDate) -> Unit
) {
    val scrollState = rememberScrollState()

    val weekdayLabels = java.time.DayOfWeek.values().map { it.getDisplayName(java.time.format.TextStyle.NARROW, androidx.compose.ui.platform.LocalConfiguration.current.locales[0]) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.loc_habit_grid),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Text(
                    text = stringResource(R.string.loc_past_16_weeks),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Fixed Weekday Labels on Left
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                weekdayLabels.forEach { label ->
                    Box(
                        modifier = Modifier.size(18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // Horizontally Scrollable 16-Week Columns with edge-fade
            Box(
                modifier = Modifier
                    .weight(1f)
                    .drawWithContent {
                        drawContent()
                        if (scrollState.value > 0) {
                            // Left edge fade hint only when scrolled past start
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    0.0f to Color.Transparent,
                                    0.04f to Color.Black
                                ),
                                blendMode = BlendMode.DstIn
                            )
                        }
                    }
            ) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    matrix.weeks.forEach { week ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            week.forEach { dayData ->
                                HeatmapTile(
                                    dayData = dayData,
                                    isSelected = dayData.date == selectedDate,
                                    shapeMode = tileShapeMode,
                                    isDark = isDark,
                                    onClick = { onCellClick(dayData.date) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeatmapTile(
    dayData: HeatmapDayData,
    isSelected: Boolean,
    shapeMode: HeatmapTileShape,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else if (isSelected) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tileScale"
    )

    val shape: Shape = when (shapeMode) {
        HeatmapTileShape.SQUIRCLE -> RoundedCornerShape(5.dp)
        HeatmapTileShape.PEBBLE_PILL -> RoundedCornerShape(50)
        HeatmapTileShape.SMOOTH_GLOW -> RoundedCornerShape(4.dp)
    }

    val tileColor = when (dayData.vibe) {
        DaySpendingVibe.FUTURE -> Color.Transparent
        DaySpendingVibe.ZERO_SPEND -> if (isDark) Color(0xFF1E2025) else Color(0xFFE2E8F0)
        DaySpendingVibe.UNDER_BUDGET -> Color(0xFF10B981) // Healthy Emerald
        DaySpendingVibe.ON_TARGET -> Color(0xFF38BDF8)   // Electric Sky Blue
        DaySpendingVibe.OVER_BUDGET -> Color(0xFFF43F5E) // Vivid Coral Alert
    }

    val animatedColor by animateColorAsState(
        targetValue = tileColor,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "tileColor"
    )

    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.dp else if (dayData.isToday) 1.5.dp else if (dayData.isFuture) 1.dp else 0.dp,
        label = "tileBorderWidth"
    )

    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        dayData.isToday -> Color(0xFFF59E0B) // Amber border for today
        dayData.isFuture -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        shapeMode == HeatmapTileShape.SMOOTH_GLOW && dayData.vibe != DaySpendingVibe.ZERO_SPEND -> tileColor.copy(alpha = 0.5f)
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .size(18.dp)
            .scale(scale)
            .clip(shape)
            .background(color = animatedColor, shape = shape)
            .border(width = borderWidth, color = borderColor, shape = shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !dayData.isFuture
            ) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        if (dayData.isToday) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}

@Composable
private fun TileShapeSelectorRow(
    currentShape: HeatmapTileShape,
    onSelectShape: (HeatmapTileShape) -> Unit
) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.loc_tile_shape),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                letterSpacing = 0.5.sp
            )

            HeatmapTileShape.values().forEach { shape ->
                val isSelected = currentShape == shape
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.94f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
                    label = "shapeChipScale"
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                    ),
                    modifier = Modifier
                        .scale(scale)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onSelectShape(shape) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Text(
                            text = stringResource(when (shape) { HeatmapTileShape.SQUIRCLE -> R.string.loc_squircle; HeatmapTileShape.PEBBLE_PILL -> R.string.loc_pill; HeatmapTileShape.SMOOTH_GLOW -> R.string.loc_glow }),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
}

@Composable
private fun InspectionCalloutCard(
    date: LocalDate,
    dailyBudget: Double,
    transactions: List<TransactionEntity>,
    isDark: Boolean,
    onViewTransactions: () -> Unit
) {
    val daySpent = remember(transactions, date) {
        transactions.filter {
            it.toAccountId == null && !it.isExcludedFromDailyBudget && it.amount > 0 &&
                    Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate() == date
        }.sumOf { it.amount }
    }

    val txCount = remember(transactions, date) {
        transactions.count {
            it.toAccountId == null && !it.isExcludedFromDailyBudget && it.amount > 0 &&
                    Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate() == date
        }
    }

    val isUnder = dailyBudget > 0.0 && daySpent <= dailyBudget
    val delta = kotlin.math.abs(dailyBudget - daySpent)

    val badgeColor = when {
        daySpent <= 0.001 -> Color(0xFF64748B)
        daySpent <= dailyBudget * 0.8 -> Color(0xFF10B981)
        daySpent <= dailyBudget -> Color(0xFF38BDF8)
        else -> Color(0xFFF43F5E)
    }

    val badgeText = when {
        daySpent <= 0.001 -> stringResource(R.string.loc_zero_spend)
        daySpent <= dailyBudget * 0.8 -> stringResource(R.string.loc_under_budget)
        daySpent <= dailyBudget -> stringResource(R.string.loc_on_target)
        else -> stringResource(R.string.loc_over_budget)
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isDark) Color(0xFF16171B) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            badgeColor.copy(alpha = 0.35f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewTransactions() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(badgeColor)
                    )
                    Text(
                        text = date.format(DateTimeFormatter.ofPattern("EEEE, MMM d", androidx.compose.ui.platform.LocalConfiguration.current.locales[0])),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = badgeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                val subtext = when {
                    daySpent <= 0.001 -> stringResource(R.string.loc_no_day_expenses)
                    isUnder -> stringResource(R.string.loc_day_saved, daySpent, delta)
                    else -> stringResource(R.string.loc_day_over, daySpent, delta)
                }

                Text(
                    text = subtext,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                )
            }

            if (txCount > 0) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.category_transactions_count, txCount),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeatmapLegendBar(isDark: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isDark) Color(0xFF141519) else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(
            color = if (isDark) Color(0xFF1E2025) else Color(0xFFE2E8F0),
            label = stringResource(R.string.loc_zero_spend)
        )
        LegendItem(
            color = Color(0xFF10B981),
            label = stringResource(R.string.loc_under_budget)
        )
        LegendItem(
            color = Color(0xFF38BDF8),
            label = stringResource(R.string.loc_on_target)
        )
        LegendItem(
            color = Color(0xFFF43F5E),
            label = stringResource(R.string.loc_over_budget)
        )
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(RoundedCornerShape(2.5.dp))
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
        )
    }
}
