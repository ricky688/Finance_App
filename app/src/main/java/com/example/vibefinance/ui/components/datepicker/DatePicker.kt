package com.example.vibefinance.ui.components.datepicker

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import com.example.vibefinance.ui.preferences.PrivacyText as Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Period
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.*
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

// --- CALENDAR MODELS & TYPES ---

enum class CalendarSelectionMode {
    SINGLE,
    RANGE,
}

data class Week(
    val number: Int,
    val yearMonth: YearMonth
)

data class Month(
    val yearMonth: YearMonth,
    val weeks: List<Week>
)

data class CalendarUiState(
    val selectionMode: CalendarSelectionMode = CalendarSelectionMode.RANGE,
    val selectedStartDate: LocalDate? = null,
    val selectedEndDate: LocalDate? = null,
    val disabledBefore: LocalDate? = LocalDate.now(),
    val disabledAfter: LocalDate? = null,
) {
    val hasSelectedDates: Boolean
        get() = selectedEndDate != null || selectionMode == CalendarSelectionMode.SINGLE

    fun hasSelectedPeriodOverlap(start: LocalDate, end: LocalDate): Boolean {
        if (!hasSelectedDates) return false
        if (selectedStartDate == null && selectedEndDate == null) return false
        if (selectedStartDate == start || selectedStartDate == end) return true
        if (selectedEndDate == null) {
            return !selectedStartDate!!.isBefore(start) && !selectedStartDate.isAfter(end)
        }
        return !end.isBefore(selectedStartDate) && !start.isAfter(selectedEndDate)
    }

    fun isDateInSelectedPeriod(date: LocalDate): Boolean {
        if (selectedStartDate == null) return false
        if (selectedStartDate == date) return true
        if (selectedEndDate == null) return false
        if (date.isBefore(selectedStartDate) || date.isAfter(selectedEndDate)) return false
        return true
    }

    fun isCurrentDay(date: LocalDate): Boolean {
        return date == LocalDate.now()
    }

    fun isDisabledDay(date: LocalDate): Boolean {
        return (disabledBefore != null && date.isBefore(disabledBefore))
                || (disabledAfter != null && date.isAfter(disabledAfter))
    }

    fun getNumberSelectedDaysInWeek(currentWeekStartDate: LocalDate, month: YearMonth): Int {
        var countSelected = 0
        var currentDate = currentWeekStartDate
        for (i in 0 until CalendarState.DAYS_IN_WEEK) {
            if (isDateInSelectedPeriod(currentDate) && currentDate.month == month.month) {
                countSelected++
            }
            currentDate = currentDate.plusDays(1)
        }
        return countSelected
    }

    fun selectedStartOffset(currentWeekStartDate: LocalDate, yearMonth: YearMonth): Int {
        var startDate = currentWeekStartDate
        var startOffset = 0
        for (i in 0 until CalendarState.DAYS_IN_WEEK) {
            if (!isDateInSelectedPeriod(startDate) || startDate.month != yearMonth.month) {
                startOffset++
            } else {
                break
            }
            startDate = startDate.plusDays(1)
        }
        return startOffset
    }

    fun isLeftHighlighted(beginningWeek: LocalDate?, month: YearMonth): Boolean {
        return if (beginningWeek != null) {
            if (month.month.value != beginningWeek.month.value) {
                false
            } else {
                val beginningWeekSelected = isDateInSelectedPeriod(beginningWeek)
                val lastDayPreviousWeek = beginningWeek.minusDays(1)
                isDateInSelectedPeriod(lastDayPreviousWeek) && beginningWeekSelected
            }
        } else {
            false
        }
    }

    fun isRightHighlighted(beginningWeek: LocalDate?, month: YearMonth): Boolean {
        val lastDayOfTheWeek = beginningWeek?.plusDays(6)
        return if (lastDayOfTheWeek != null) {
            if (month.month.value != lastDayOfTheWeek.month.value) {
                false
            } else {
                val lastDayOfTheWeekSelected = isDateInSelectedPeriod(lastDayOfTheWeek)
                val firstDayNextWeek = lastDayOfTheWeek.plusDays(1)
                isDateInSelectedPeriod(firstDayNextWeek) && lastDayOfTheWeekSelected
            }
        } else {
            false
        }
    }

    fun dayDelay(currentWeekStartDate: LocalDate): Int {
        if (selectedStartDate == null && selectedEndDate == null) return 0
        val endWeek = currentWeekStartDate.plusDays(6)
        return if (selectedStartDate?.isBefore(currentWeekStartDate) == true ||
            selectedStartDate?.isAfter(endWeek) == true
        ) {
            abs(ChronoUnit.DAYS.between(currentWeekStartDate, selectedStartDate)).toInt()
        } else {
            0
        }
    }

    fun monthOverlapSelectionDelay(currentWeekStartDate: LocalDate, week: Week): Int {
        val isStartInADifferentMonth = currentWeekStartDate.month != week.yearMonth.month
        return if (isStartInADifferentMonth) {
            var currentDate = currentWeekStartDate
            var offset = 0
            for (i in 0 until CalendarState.DAYS_IN_WEEK) {
                if (currentDate.month.value != week.yearMonth.month.value &&
                    isDateInSelectedPeriod(currentDate)
                ) {
                    offset++
                }
                currentDate = currentDate.plusDays(1)
            }
            offset
        } else {
            0
        }
    }

    fun setDates(newFrom: LocalDate?, newTo: LocalDate?): CalendarUiState {
        return if (newTo == null) {
            copy(selectedStartDate = newFrom)
        } else {
            copy(selectedStartDate = newFrom, selectedEndDate = newTo)
        }
    }

    fun setDate(new: LocalDate?): CalendarUiState {
        return copy(selectedStartDate = new)
    }
}

class CalendarState(
    context: Context,
    val selectionMode: CalendarSelectionMode = CalendarSelectionMode.SINGLE,
    selectDate: LocalDate? = null,
    disableBeforeDate: LocalDate? = null,
    disableAfterDate: LocalDate? = null,
) {
    val calendarUiState = mutableStateOf(
        CalendarUiState(
            selectionMode = selectionMode,
            disabledBefore = disableBeforeDate,
            disabledAfter = disableAfterDate,
        )
    )
    val listMonths: List<Month>

    private val calendarStartDate: LocalDate = LocalDate.now().withDayOfMonth(1)
    private val calendarEndDate: LocalDate = LocalDate.now().plusYears(2).withMonth(12).withDayOfMonth(31)

    private val periodBetweenCalendarStartEnd: Period = Period.between(
        disableBeforeDate?.withDayOfMonth(1) ?: calendarStartDate,
        disableAfterDate?.withDayOfMonth(28) ?: calendarEndDate
    )

    init {
        val tempListMonths = mutableListOf<Month>()
        var startYearMonth = YearMonth.from(disableBeforeDate?.withDayOfMonth(1) ?: calendarStartDate)

        for (numberMonth in 0..periodBetweenCalendarStartEnd.toTotalMonths()) {
            val numberWeeks = startYearMonth.getNumberWeeks(context)
            val listWeekItems = mutableListOf<Week>()
            for (week in 0 until numberWeeks) {
                listWeekItems.add(
                    Week(
                        number = week,
                        yearMonth = startYearMonth
                    )
                )
            }
            val month = Month(startYearMonth, listWeekItems)
            tempListMonths.add(month)
            startYearMonth = startYearMonth.plusMonths(1)
        }
        listMonths = tempListMonths.toList()

        if (selectDate != null) setSelectedDay(selectDate)
    }

    fun setSelectedDay(newDate: LocalDate) {
        if (calendarUiState.value.selectionMode == CalendarSelectionMode.RANGE) {
            val start = calendarUiState.value.disabledBefore ?: LocalDate.now()
            calendarUiState.value = calendarUiState.value.setDates(start, newDate)
        } else {
            calendarUiState.value = calendarUiState.value.setDate(newDate)
        }
    }

    companion object {
        const val DAYS_IN_WEEK = 7
    }
}

// --- UTILITY DATE EXTENSIONS ---

fun YearMonth.getNumberWeeks(context: Context): Int {
    val locale = context.resources.configuration.locales[0]
    val weekWithFixFirstWeekDay = WeekFields.of(WeekFields.of(locale).firstDayOfWeek, 1).weekOfMonth()
    val weekNumberFirst = this.atDay(1).get(weekWithFixFirstWeekDay)
    val weekNumberLast = this.atEndOfMonth().get(weekWithFixFirstWeekDay)
    return weekNumberLast - weekNumberFirst + 1
}

fun getWeek(locale: Locale): Array<DayOfWeek> {
    val firstDayOfWeek = WeekFields.of(locale).firstDayOfWeek
    return if (firstDayOfWeek == DayOfWeek.MONDAY) {
        arrayOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
    } else {
        arrayOf(DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)
    }
}

fun prettyWeekDay(dayOfWeek: DayOfWeek, locale: Locale): String {
    val dayOfWeekFormatter = if (locale.language == "ru") {
        DateTimeFormatter.ofPattern("ccc", locale)
    } else {
        DateTimeFormatter.ofPattern("ccccc", locale)
    }
    return dayOfWeekFormatter.format(dayOfWeek).uppercase(locale)
}

fun prettyYearMonth(yearMonth: YearMonth, locale: Locale): String {
    val yearOnlyFormatter = DateTimeFormatter.ofPattern("yyyy", locale)
    val monthFormatter = DateTimeFormatter.ofPattern("LLLL", locale)
    val monthWithYearFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", locale)

    return if (yearMonth.year.toString() == yearOnlyFormatter.format(LocalDate.now())) {
        yearMonth.format(monthFormatter)
    } else {
        yearMonth.format(monthWithYearFormatter)
    }.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(locale) else it.toString()
    }
}

fun selectedDatesFormatted(state: CalendarState, locale: Locale): String {
    val uiState = state.calendarUiState.value
    if (uiState.selectedStartDate == null) return ""
    val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", locale)
    val startStr = uiState.selectedStartDate.format(formatter)
    if (uiState.selectionMode == CalendarSelectionMode.SINGLE) return startStr
    return if (uiState.selectedEndDate != null) {
        "$startStr — ${uiState.selectedEndDate.format(formatter)}"
    } else {
        "$startStr — ?"
    }
}

// --- COMPOSE COMPONENTS ---

val CELL_SIZE = 48.dp

@Composable
fun DatePicker(
    calendarState: CalendarState,
    onDayClicked: (date: LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val calendarUiState = calendarState.calendarUiState.value
    val dayWidth = remember { mutableStateOf(CELL_SIZE) }
    val localDensity = LocalDensity.current

    LazyColumn(
        modifier = modifier
            .onGloballyPositioned {
                dayWidth.value = with(localDensity) { it.size.width.toDp() / 7 }
            },
    ) {
        calendarState.listMonths.forEach { month ->
            itemsCalendarMonth(calendarUiState, onDayClicked, month, dayWidth.value)
        }
    }
}

private fun LazyListScope.itemsCalendarMonth(
    calendarUiState: CalendarUiState,
    onDayClicked: (LocalDate) -> Unit,
    month: Month,
    dayWidth: Dp,
) {
    item(month.yearMonth.month.name + month.yearMonth.year + "header") {
        MonthHeader(
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
            yearMonth = month.yearMonth
        )
    }

    val contentModifier = Modifier.fillMaxWidth()

    item(month.yearMonth.month.name + month.yearMonth.year + "daysOfWeek") {
        DaysOfWeek(modifier = contentModifier)
    }

    itemsIndexed(month.weeks, key = { index, _ ->
        month.yearMonth.year.toString() + "/" + month.yearMonth.month.value + "/" + (index + 1).toString()
    }) { _, week ->
        val locale = LocalConfiguration.current.locales[0]
        val beginningWeek = week.yearMonth.atDay(1).plusWeeks(week.number.toLong())
        val currentDay = beginningWeek.with(TemporalAdjusters.previousOrSame(getWeek(locale)[0]))

        if (calendarUiState.hasSelectedPeriodOverlap(currentDay, currentDay.plusDays(6))) {
            WeekSelectionPill(
                state = calendarUiState,
                currentWeekStart = currentDay,
                widthPerDay = dayWidth,
                heightPerDay = CELL_SIZE,
                week = week,
            )
        }
        Week(
            calendarUiState = calendarUiState,
            modifier = contentModifier,
            week = week,
            onDayClicked = onDayClicked
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
internal fun MonthHeader(modifier: Modifier = Modifier, yearMonth: YearMonth) {
    val locale = LocalConfiguration.current.locales[0]
    Row(modifier = modifier, verticalAlignment = Alignment.Bottom) {
        Text(
            modifier = Modifier
                .padding(start = 24.dp)
                .weight(1f),
            text = prettyYearMonth(yearMonth, locale),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
internal fun DaysOfWeek(modifier: Modifier = Modifier) {
    val locale = LocalConfiguration.current.locales[0]
    val week = getWeek(locale)

    Row(modifier = modifier) {
        for (day in week) {
            DayOfWeekHeading(
                day = prettyWeekDay(day, locale),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
internal fun DayOfWeekHeading(day: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(CELL_SIZE)
            .widthIn(min = CELL_SIZE)
            .fillMaxWidth()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentHeight(Alignment.CenterVertically),
            textAlign = TextAlign.Center,
            text = day,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5F),
        )
    }
}

@Composable
internal fun Week(
    calendarUiState: CalendarUiState,
    week: Week,
    onDayClicked: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val locale = LocalConfiguration.current.locales[0]
    val beginningWeek = week.yearMonth.atDay(1).plusWeeks(week.number.toLong())
    var currentDay = beginningWeek.with(TemporalAdjusters.previousOrSame(getWeek(locale)[0]))

    Box(Modifier.fillMaxWidth()) {
        Row(modifier = modifier) {
            for (i in 0..6) {
                if (currentDay.month == week.yearMonth.month) {
                    Day(
                        modifier = Modifier.weight(1f),
                        calendarState = calendarUiState,
                        day = currentDay,
                        onDayClicked = onDayClicked,
                    )
                } else {
                    Box(modifier = Modifier
                        .size(CELL_SIZE)
                        .weight(1f))
                }
                currentDay = currentDay.plusDays(1)
            }
        }
    }
}

@Composable
private fun DayContainer(
    modifier: Modifier = Modifier,
    current: Boolean = false,
    disabled: Boolean = false,
    onSelect: () -> Unit = { },
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .height(CELL_SIZE)
            .widthIn(min = CELL_SIZE)
            .fillMaxWidth()
            .background(Color.Transparent)
            .clickable(
                onClick = { onSelect() },
                enabled = !disabled,
                role = Role.Button,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(
                    bounded = false,
                    radius = CELL_SIZE / 2,
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (current) {
            Box(
                modifier = modifier
                    .height(CELL_SIZE - 8.dp)
                    .width(CELL_SIZE - 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                    )
            ) {
                content()
            }
        } else {
            content()
        }
    }
}

@Composable
internal fun Day(
    day: LocalDate,
    calendarState: CalendarUiState,
    onDayClicked: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val disabled = calendarState.isDisabledDay(day)
    val selected = calendarState.isDateInSelectedPeriod(day)
    val current = calendarState.isCurrentDay(day)

    DayContainer(
        modifier = modifier,
        current = current,
        disabled = disabled,
        onSelect = { onDayClicked(day) },
    ) {
        Text(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center),
            text = day.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = when (true) {
                current -> MaterialTheme.colorScheme.onPrimaryContainer
                selected -> MaterialTheme.colorScheme.onPrimary
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = if (disabled) 0.3F else 1F)
            },
        )
    }
}

@Composable
fun WeekSelectionPill(
    week: Week,
    currentWeekStart: LocalDate,
    state: CalendarUiState,
    modifier: Modifier = Modifier,
    widthPerDay: Dp = 48.dp,
    heightPerDay: Dp = 48.dp,
    pillColor: Color = MaterialTheme.colorScheme.primary
) {
    val widthPerDayPx = with(LocalDensity.current) { widthPerDay.toPx() }
    val heightPerDayPx = with(LocalDensity.current) { heightPerDay.toPx() }
    val cornerRadiusPx = with(LocalDensity.current) { 24.dp.toPx() }

    Canvas(
        modifier = modifier.fillMaxWidth(),
        onDraw = {
            val (offset, size) = getOffsetAndSize(
                this.size.width,
                state,
                currentWeekStart,
                week,
                widthPerDayPx,
                heightPerDayPx,
                cornerRadiusPx,
            )

            drawRoundRect(
                color = pillColor,
                topLeft = offset,
                size = Size(size, heightPerDayPx),
                cornerRadius = CornerRadius(cornerRadiusPx)
            )
        }
    )
}

private fun getOffsetAndSize(
    width: Float,
    state: CalendarUiState,
    currentWeekStart: LocalDate,
    week: Week,
    widthPerDayPx: Float,
    heightPerDayPx: Float,
    cornerRadiusPx: Float,
): Pair<Offset, Float> {
    val numberDaysSelected = state.getNumberSelectedDaysInWeek(currentWeekStart, week.yearMonth)
    val monthOverlapDelay = state.monthOverlapSelectionDelay(currentWeekStart, week)
    val dayDelay = state.dayDelay(currentWeekStart)
    val edgePadding = (width - widthPerDayPx * CalendarState.DAYS_IN_WEEK + ((widthPerDayPx - heightPerDayPx)) / 2F) + 1

    val sideSize = edgePadding + cornerRadiusPx

    val leftSize = if (state.isLeftHighlighted(currentWeekStart, week.yearMonth)) sideSize else 0f
    val rightSize = if (state.isRightHighlighted(currentWeekStart, week.yearMonth)) sideSize else 0f

    var totalSize = (numberDaysSelected * widthPerDayPx) + (leftSize + rightSize) - ((widthPerDayPx - heightPerDayPx))
    if (dayDelay + monthOverlapDelay == 0 && numberDaysSelected >= 1) {
        totalSize = totalSize.coerceAtLeast(heightPerDayPx)
    }

    totalSize = totalSize.coerceAtLeast(0F)

    val startOffset = state.selectedStartOffset(currentWeekStart, week.yearMonth) * widthPerDayPx

    val offset = Offset(startOffset + edgePadding - leftSize, 0f)

    return offset to totalSize
}
