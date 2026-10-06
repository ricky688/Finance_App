package com.example.vibefinance.ui.home

import com.example.vibefinance.ui.components.rememberCompletePressProgress
import com.example.vibefinance.ui.components.completePressShape

import com.example.vibefinance.R
import androidx.compose.ui.res.stringResource
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.components.rememberConnectedButtonColorMotion
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.pow

enum class CalendarDisplayMode {
    HEATMAP,
    CALENDAR
}

@Composable
fun ExpressiveDualViewSwitcher(
    currentMode: CalendarDisplayMode,
    onModeSelected: (CalendarDisplayMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = remember { listOf(CalendarDisplayMode.HEATMAP, CalendarDisplayMode.CALENDAR) }
    val selectedIndex = modes.indexOf(currentMode).coerceAtLeast(0)
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val colors = MaterialTheme.colorScheme
    val trackColor = colors.surfaceContainerHighest.copy(alpha = 0.35f)
    val trackBackdrop = trackColor.compositeOver(blendColors(colors.surface, colors.surfaceVariant, 0.3f))

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = trackColor,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            modes.forEachIndexed { index, mode ->
                val isSelected = index == selectedIndex
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed = interactionSource.collectIsPressedAsState()
                val shapeProgress by rememberCompletePressProgress(interactionSource)

                // Material 3 Expressive Connected Button Geometry:
                // Outer corners retain 18.dp rounding; inner adjacent corners are 4.dp when unselected.
                // When selected, inner corners smoothly expand to 18.dp for a distinct expressive pill!
                // When pressed / pulsing, all 4 corners morph to 6.dp with snappy non-bouncy spring physics.
                val restingTopStart = if (isSelected) 18.dp else (if (index == 0) 18.dp else 4.dp)
                val restingBottomStart = if (isSelected) 18.dp else (if (index == 0) 18.dp else 4.dp)
                val restingTopEnd = if (isSelected) 18.dp else (if (index == modes.size - 1) 18.dp else 4.dp)
                val restingBottomEnd = if (isSelected) 18.dp else (if (index == modes.size - 1) 18.dp else 4.dp)

                val pressedCorner = 6.dp

                val topStart by animateDpAsState(
                    targetValue = restingTopStart,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "switcherTopStart_$index"
                )
                val bottomStart by animateDpAsState(
                    targetValue = restingBottomStart,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "switcherBottomStart_$index"
                )
                val topEnd by animateDpAsState(
                    targetValue = restingTopEnd,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "switcherTopEnd_$index"
                )
                val bottomEnd by animateDpAsState(
                    targetValue = restingBottomEnd,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "switcherBottomEnd_$index"
                )

                val buttonShape = completePressShape(
                    RoundedCornerShape(
                        topStart = topStart, bottomStart = bottomStart,
                        topEnd = topEnd, bottomEnd = bottomEnd
                    ),
                    RoundedCornerShape(pressedCorner),
                    shapeProgress
                )

                val scale = 1f - 0.06f * shapeProgress

                val colorMotion = rememberConnectedButtonColorMotion(
                    isSelected = isSelected,
                    isPressed = isPressed.value,
                    inactiveContainerColor = Color.Transparent,
                    inactiveContentColor = colors.onSurfaceVariant,
                    backdropColor = trackBackdrop
                )

                Surface(
                    color = colorMotion.containerColor,
                    contentColor = colorMotion.contentColor,
                    shape = buttonShape,
                    modifier = Modifier
                        .scale(scale)
                        .clip(buttonShape)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = ripple(bounded = true, color = colorMotion.rippleColor),
                            onClick = {
                                if (!isSelected) {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onModeSelected(mode)
                                }
                            }
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp).then(colorMotion.contentModifier),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        AnimatedVisibility(
                            visible = isSelected,
                            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                                    expandHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)),
                            exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                                    shrinkHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = colorMotion.contentColor
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                            }
                        }

                        Icon(
                            imageVector = if (mode == CalendarDisplayMode.HEATMAP) Icons.Default.GridView else Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = colorMotion.contentColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (mode == CalendarDisplayMode.HEATMAP) stringResource(R.string.loc_habit) else stringResource(R.string.loc_month),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = colorMotion.contentColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

private val CELL_SIZE = 48.dp

private fun blendColors(colorA: Color, colorB: Color, angle: Float = 0.5f): Color {
    val colorAPart = (1f - angle) * 2f
    val colorBPart = angle * 2f
    return Color(
        red = ((colorA.red * colorAPart + colorB.red * colorBPart) / 2f).coerceIn(0f, 1f),
        green = ((colorA.green * colorAPart + colorB.green * colorBPart) / 2f).coerceIn(0f, 1f),
        blue = ((colorA.blue * colorAPart + colorB.blue * colorBPart) / 2f).coerceIn(0f, 1f),
    )
}

private data class CalendarHarmonizedColorPalette(
    val main: Color,
    val onMain: Color,
    val container: Color,
    val onContainer: Color
)

private fun combineColorsList(colors: List<Color>, angle: Float = 0.5f): Color {
    val approximateIndex = (colors.size - 1) * angle
    val floorIdx = kotlin.math.floor(approximateIndex).toInt()
    val ceilIdx = kotlin.math.ceil(approximateIndex).toInt()
    val colorA = colors[floorIdx]
    val colorB = colors[ceilIdx]
    return blendColors(colorA, colorB, approximateIndex - floorIdx)
}

private fun getHarmonizedPalette(
    color: Color,
    primaryColor: Color,
    isDarkTheme: Boolean
): CalendarHarmonizedColorPalette {
    val harmonized = blendColors(color, primaryColor, 0.15f)
    return if (isDarkTheme) {
        CalendarHarmonizedColorPalette(
            main = harmonized,
            onMain = blendColors(harmonized, Color.White, 0.8f),
            container = blendColors(harmonized, Color.Black, 0.7f),
            onContainer = blendColors(harmonized, Color.White, 0.75f)
        )
    } else {
        CalendarHarmonizedColorPalette(
            main = harmonized,
            onMain = blendColors(harmonized, Color.Black, 0.6f),
            container = blendColors(harmonized, Color.White, 0.85f),
            onContainer = blendColors(harmonized, Color.Black, 0.55f)
        )
    }
}

private fun verticalGridMeasurePolicy(columns: Int) = MeasurePolicy { measurables, constraints ->
    val cellWidth = constraints.maxWidth / columns

    val cells = mutableListOf<Int>()
    var cellsCount = 0

    val placeables = measurables.mapIndexed { index, it ->
        val span = if (it.layoutId == "fullWidth") columns else 1
        cells.add(span)
        cellsCount += span

        it.measure(
            constraints.copy(
                minWidth = 0,
                maxWidth = cellWidth * span,
                minHeight = 0,
                maxHeight = constraints.maxHeight
            )
        )
    }

    layout(
        constraints.maxWidth,
        (cellsCount + columns - 1) / columns * CELL_SIZE.roundToPx(),
    ) {
        var cellsOffset = 0

        placeables.forEachIndexed { index, it ->
            val cellIndex = cellsOffset
            val span = cells[index]
            cellsOffset += span
            it.place(
                cellWidth * (cellIndex % columns),
                CELL_SIZE.roundToPx() * (cellIndex / columns),
                0f
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpendsCalendar(
    state: FinanceUiState,
    modifier: Modifier = Modifier
) {
    val budgetInfo = state.budgetInfo ?: return
    val startLocal = remember(budgetInfo.startDate) {
        Instant.ofEpochMilli(budgetInfo.startDate).atZone(ZoneId.systemDefault()).toLocalDate()
    }
    val endLocal = remember(budgetInfo.endDate) {
        Instant.ofEpochMilli(budgetInfo.endDate).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    val months = remember(startLocal, endLocal) {
        val list = mutableListOf<YearMonth>()
        var curr = YearMonth.from(startLocal)
        val endLimit = YearMonth.from(endLocal)
        while (!curr.isAfter(endLimit)) {
            list.add(curr)
            curr = curr.plusMonths(1)
        }
        list
    }

    val dailyBudget = budgetInfo.dailyAllowance

    val daySpending = remember(state.transactions, startLocal, endLocal) {
        val map = mutableMapOf<LocalDate, Double>()
        state.transactions.forEach { tx ->
            if (tx.toAccountId == null && !tx.isExcludedFromDailyBudget && tx.amount > 0) {
                val date = Instant.ofEpochMilli(tx.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
                if (!date.isBefore(startLocal) && !date.isAfter(endLocal)) {
                    map[date] = (map[date] ?: 0.0) + tx.amount
                }
            }
        }
        map
    }

    var displayMode by remember { mutableStateOf(CalendarDisplayMode.HEATMAP) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDetailSheet by remember { mutableStateOf(false) }
    val today = remember { LocalDate.now() }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val cardBg = blendColors(
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.surfaceVariant,
        0.3f
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = cardBg
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Title & Expressive Dual-View Switcher
            val screenWidthDp = LocalConfiguration.current.screenWidthDp
            val titleFontSize = if (screenWidthDp < 380) 13.sp else 14.5.sp

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (displayMode == CalendarDisplayMode.HEATMAP) Icons.Default.GridView else Icons.Default.CalendarToday,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (displayMode == CalendarDisplayMode.HEATMAP) stringResource(R.string.loc_financial_rhythm) else stringResource(R.string.loc_spending_calendar),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = titleFontSize),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Expressive Dual-View Switcher Connected Button Group
                ExpressiveDualViewSwitcher(
                    currentMode = displayMode,
                    onModeSelected = { displayMode = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(
                targetState = displayMode,
                transitionSpec = {
                    if (targetState == CalendarDisplayMode.CALENDAR) {
                        (slideInHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy)) { width -> width / 3 } + fadeIn())
                            .togetherWith(slideOutHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy)) { width -> -width / 3 } + fadeOut())
                    } else {
                        (slideInHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy)) { width -> -width / 3 } + fadeIn())
                            .togetherWith(slideOutHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy)) { width -> width / 3 } + fadeOut())
                    }
                },
                label = "dualViewModeTransition"
            ) { mode ->
                when (mode) {
                    CalendarDisplayMode.HEATMAP -> {
                        M3ExpressiveHeatmap(
                            transactions = state.transactions,
                            dailyBudget = dailyBudget,
                            selectedDate = selectedDate,
                            onDateSelected = { selectedDate = it },
                            onViewTransactionsForDate = { date ->
                                selectedDate = date
                                showDetailSheet = true
                            }
                        )
                    }
                    CalendarDisplayMode.CALENDAR -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Visual Heatmap Legend Key Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LegendChip(color = Color(0xFF40AC02), label = stringResource(R.string.loc_healthy))
                                LegendChip(color = Color(0xFFFABC20), label = stringResource(R.string.loc_moderate))
                                LegendChip(color = Color(0xFFC70909), label = stringResource(R.string.loc_over_limit))
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Layout(
                                modifier = Modifier.fillMaxWidth(),
                                measurePolicy = verticalGridMeasurePolicy(7),
                                content = {
                                    months.forEach { month ->
                                        // 1. Month Header
                                        Box(
                                            modifier = Modifier
                                                .layoutId("fullWidth")
                                                .height(CELL_SIZE),
                                            contentAlignment = Alignment.BottomStart
                                        ) {
                                            val calendarLocale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
                                            val monthStr = remember(month, calendarLocale) {
                                                month.format(DateTimeFormatter.ofPattern("MMMM yyyy", calendarLocale))
                                            }
                                            Text(
                                                text = monthStr,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(start = 8.dp)
                                            )
                                        }

                                        // 2. Weekday Headers
                                        val headers = listOf(java.time.DayOfWeek.SUNDAY) + java.time.DayOfWeek.values().take(6)
                                        val localizedHeaders = headers.map { it.getDisplayName(java.time.format.TextStyle.SHORT, androidx.compose.ui.platform.LocalConfiguration.current.locales[0]) }
                                        localizedHeaders.forEach { h ->
                                            Box(
                                                modifier = Modifier
                                                    .height(CELL_SIZE)
                                                    .fillMaxWidth(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = h,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }

                                        // 3. Days Grid
                                        val daysInMonth = month.lengthOfMonth()
                                        val firstDayOfWeek = month.atDay(1).dayOfWeek.value // Mon = 1, ..., Sun = 7
                                        val blanksCount = firstDayOfWeek % 7

                                        // Blanks at start
                                        repeat(blanksCount) {
                                            Box(modifier = Modifier.size(CELL_SIZE))
                                        }

                                        // Month Days
                                        for (dayNum in 1..daysInMonth) {
                                            val cellDay = month.atDay(dayNum)
                                            val spent = daySpending[cellDay] ?: 0.0
                                            val percent = if (dailyBudget > 0.0) (spent / dailyBudget).toFloat() else 0f
                                            val zIndexVal = if (spent > 0.0) -percent + 1000f else 0f
                                            val isSelected = selectedDate == cellDay
                                            val isToday = cellDay == today
                                            val isInPeriod = !cellDay.isBefore(startLocal) && !cellDay.isAfter(endLocal)

                                            Box(
                                                modifier = Modifier
                                                    .height(CELL_SIZE)
                                                    .fillMaxWidth()
                                                    .zIndex(zIndexVal)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .clickable {
                                                        selectedDate = cellDay
                                                        showDetailSheet = true
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                DayCell(
                                                    day = cellDay,
                                                    spent = spent,
                                                    dailyBudget = dailyBudget,
                                                    isDarkTheme = isDark,
                                                    percent = percent,
                                                    isSelected = isSelected,
                                                    isToday = isToday,
                                                    isInPeriod = isInPeriod
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet for Day Expense Details
    if (showDetailSheet && selectedDate != null) {
        val date = selectedDate!!
        val dayTxList = remember(state.transactions, date) {
            state.transactions.filter { tx ->
                val txDate = Instant.ofEpochMilli(tx.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
                txDate == date && tx.toAccountId == null && !tx.isExcludedFromDailyBudget && tx.amount > 0
            }
        }
        val dateSpent = remember(dayTxList) { dayTxList.sumOf { it.amount } }
        val sheetState = rememberModalBottomSheetState()

        ModalBottomSheet(
            onDismissRequest = { showDetailSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // Sheet Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = date.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", androidx.compose.ui.platform.LocalConfiguration.current.locales[0])),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (date == today) stringResource(R.string.loc_today_expenses) else stringResource(R.string.loc_historical_spending),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    IconButton(onClick = { showDetailSheet = false }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(R.string.btn_close))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Day Budget Comparison Card
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.loc_day_spent),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text(
                                text = String.format(Locale.US, "$%,.2f", dateSpent),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (dateSpent > dailyBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = stringResource(R.string.loc_daily_limit),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text(
                                text = String.format(Locale.US, "$%,.2f", dailyBudget),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.loc_transactions_with_count, dayTxList.size),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (dayTxList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.loc_no_day_expenses),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                    ) {
                        items(dayTxList) { tx ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Box(modifier = Modifier.padding(6.dp)) {
                                                Icon(
                                                    imageVector = Icons.Default.ReceiptLong,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Column {
                                            Text(
                                                text = tx.description.ifBlank { getCategoryDisplayName(tx.category) },
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = getCategoryDisplayName(tx.category),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                    Text(
                                        text = String.format(Locale.US, "$%.2f", tx.amount),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun LegendChip(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
            fontSize = 10.sp
        )
    }
}

@Composable
private fun DayCell(
    day: LocalDate,
    spent: Double,
    dailyBudget: Double,
    isDarkTheme: Boolean,
    percent: Float,
    isSelected: Boolean,
    isToday: Boolean,
    isInPeriod: Boolean
) {
    val hasSpending = isInPeriod && spent > 0.0

    val cellBgColor = if (isInPeriod) {
        blendColors(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceVariant,
            angle = 0.3f
        ).copy(alpha = 0.8f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val palette = if (hasSpending) {
        val rawColor = if (spent <= dailyBudget) {
            combineColorsList(listOf(Color(0xFFFABC20), Color(0xFF40AC02)), 1f - percent.coerceIn(0f, 1f))
        } else {
            Color(0xFFC70909)
        }
        getHarmonizedPalette(rawColor, primaryColor, isDarkTheme)
    } else {
        null
    }

    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.secondary
        hasSpending && palette != null -> palette.container.copy(alpha = if (percent < 1f) 0.4f else 1f)
        else -> Color.Transparent
    }

    val borderThickness by animateDpAsState(
        targetValue = if (isSelected) 2.5.dp else if (isToday) 1.5.dp else 1.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cellBorderThickness"
    )

    Box(
        modifier = Modifier
            .size(CELL_SIZE)
            .padding(2.dp)
            .drawBehind {
                if (hasSpending && palette != null) {
                    val glowColor = palette.main.copy(alpha = 0.15f)
                    val glowRadius = (CELL_SIZE * percent.coerceIn(0.6f, 2.5f) * 0.7f).toPx()
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(glowColor, Color.Transparent),
                            radius = glowRadius
                        ),
                        radius = glowRadius
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(CELL_SIZE - 2.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    color = cellBgColor,
                    shape = RoundedCornerShape(10.dp)
                )
                .border(
                    width = borderThickness,
                    color = borderColor,
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (hasSpending && palette != null) {
                val innerAlpha = if (percent > 1f) {
                    (1f - (percent.coerceIn(1f, 3f) - 1f) / 2f) * 0.3f + 0.2f
                } else {
                    0.5f
                }
                val innerShapeRadius = 10.dp * percent.coerceAtLeast(0.7f).toDouble().pow(1.8).toFloat()
                Box(
                    modifier = Modifier
                        .requiredSize(CELL_SIZE * percent.coerceIn(0.3f, 1f))
                        .clip(RoundedCornerShape(innerShapeRadius))
                        .background(
                            color = palette.container.copy(alpha = innerAlpha),
                            shape = RoundedCornerShape(innerShapeRadius)
                        )
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = day.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected || isToday) FontWeight.ExtraBold else FontWeight.Bold,
                    color = when {
                        isToday -> MaterialTheme.colorScheme.primary
                        hasSpending && palette != null -> palette.onContainer
                        isInPeriod -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                    }
                )
                if (isToday) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}
