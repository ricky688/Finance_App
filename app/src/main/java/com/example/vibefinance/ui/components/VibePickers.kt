@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.vibefinance.theme.VibeMotion
import com.example.vibefinance.ui.components.datepicker.prettyWeekDay
import com.example.vibefinance.ui.preferences.LocalExperience
import com.example.vibefinance.ui.preferences.MotionLevel
import com.example.vibefinance.ui.preferences.PrivacyText as Text
import com.example.vibefinance.util.VibeHaptics
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val SundayFirstDays = listOf(
    DayOfWeek.SUNDAY,
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY,
    DayOfWeek.SATURDAY
)

/**
 * Shared M3 Expressive shape-morphing dialog container for date and time pickers.
 * Morphs corner radius (48dp -> 28dp) and scale (0.86 -> 1.0) along [VibeMotion.slowSpatial]
 * so focus/opening transitions are unhurried and clearly perceptible.
 */
@Composable
fun VibeMorphingPickerDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)? = null,
    leadingFooter: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val minimal = LocalExperience.current.motion == MotionLevel.MINIMAL
    val openProgress = remember { Animatable(if (minimal) 1f else 0f) }
    val alphaProgress = remember { Animatable(if (minimal) 1f else 0f) }
    val driftPx = with(LocalDensity.current) { 14.dp.toPx() }

    LaunchedEffect(Unit) {
        if (!minimal) {
            launch { openProgress.animateTo(1f, VibeMotion.slowSpatial()) }
            launch { alphaProgress.animateTo(1f, VibeMotion.defaultEffects()) }
        }
    }

    val p = openProgress.value
    val animatedCorner = lerp(48f, 28f, p).coerceIn(20f, 56f).dp
    val scale = lerp(0.88f, 1f, p)

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Surface(
            modifier = modifier
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .widthIn(min = 312.dp, max = 380.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationY = (1f - p) * driftPx
                    alpha = alphaProgress.value.coerceIn(0f, 1f)
                },
            shape = RoundedCornerShape(animatedCorner),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                content()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (leadingFooter != null) {
                        Box { leadingFooter() }
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (dismissButton != null) {
                            dismissButton()
                            Spacer(Modifier.width(8.dp))
                        }
                        confirmButton()
                    }
                }
            }
        }
    }
}

/**
 * M3 Expressive calendar date picker dialog fixing Android's Chinese narrow weekday truncation
 * (`星期一` -> `一`, never `星`) across every calendar, with shape-morphing day/month/year cells.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VibeDatePickerDialog(
    selectedUtcMillis: Long?,
    onDateSelected: (Long?) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    initialDisplayedMonthMillis: Long? = selectedUtcMillis,
    yearRange: IntRange = 1900..2100,
    selectableDates: SelectableDates? = null,
    title: (@Composable () -> Unit)? = null,
    confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)? = null,
) {
    VibeCalendarDialogCore(
        isRangeMode = false,
        selectedStartUtcMillis = selectedUtcMillis,
        selectedEndUtcMillis = null,
        onSingleDateSelected = onDateSelected,
        onRangeDateSelected = { _, _ -> },
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        initialDisplayedMonthMillis = initialDisplayedMonthMillis,
        yearRange = yearRange,
        selectableDates = selectableDates,
        title = title,
        confirmButton = confirmButton,
        dismissButton = dismissButton
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VibeDateRangePickerDialog(
    startUtcMillis: Long?,
    endUtcMillis: Long?,
    onRangeSelected: (Long?, Long?) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    yearRange: IntRange = 1900..2100,
    selectableDates: SelectableDates? = null,
    title: (@Composable () -> Unit)? = null,
    confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)? = null,
) {
    VibeCalendarDialogCore(
        isRangeMode = true,
        selectedStartUtcMillis = startUtcMillis,
        selectedEndUtcMillis = endUtcMillis,
        onSingleDateSelected = {},
        onRangeDateSelected = onRangeSelected,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        initialDisplayedMonthMillis = startUtcMillis ?: endUtcMillis,
        yearRange = yearRange,
        selectableDates = selectableDates,
        title = title,
        confirmButton = confirmButton,
        dismissButton = dismissButton
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VibeCalendarDialogCore(
    isRangeMode: Boolean,
    selectedStartUtcMillis: Long?,
    selectedEndUtcMillis: Long?,
    onSingleDateSelected: (Long?) -> Unit,
    onRangeDateSelected: (Long?, Long?) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    initialDisplayedMonthMillis: Long?,
    yearRange: IntRange,
    selectableDates: SelectableDates?,
    title: (@Composable () -> Unit)?,
    confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)?,
) {
    val view = LocalView.current
    val locale = LocalConfiguration.current.locales[0]
    val motion = LocalExperience.current.motion
    val today = remember { LocalDate.now() }

    val startDate = remember(selectedStartUtcMillis) { selectedStartUtcMillis?.utcMillisToLocalDate() }
    val endDate = remember(selectedEndUtcMillis) { selectedEndUtcMillis?.utcMillisToLocalDate() }
    val initialMonth = remember(initialDisplayedMonthMillis, startDate) {
        val anchor = (initialDisplayedMonthMillis?.utcMillisToLocalDate()) ?: startDate ?: today
        YearMonth.of(anchor.year.coerceIn(yearRange.first, yearRange.last), anchor.month)
    }

    var displayedMonth by remember { mutableStateOf(initialMonth) }
    var showYearSelector by remember { mutableStateOf(false) }

    val monthHeaderText = remember(displayedMonth, locale) {
        val pattern = android.text.format.DateFormat.getBestDateTimePattern(locale, "yMMMM")
        displayedMonth.atDay(1).format(DateTimeFormatter.ofPattern(pattern, locale))
    }

    val selectedHeadline = remember(startDate, endDate, isRangeMode, locale) {
        val fmt = DateTimeFormatter.ofPattern(
            android.text.format.DateFormat.getBestDateTimePattern(locale, "yMMMdEEE"),
            locale
        )
        when {
            !isRangeMode && startDate != null -> startDate.format(fmt)
            isRangeMode && startDate != null && endDate != null -> "${startDate.format(fmt)} – ${endDate.format(fmt)}"
            isRangeMode && startDate != null -> startDate.format(fmt)
            else -> "—"
        }
    }

    VibeMorphingPickerDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        confirmButton = confirmButton,
        dismissButton = dismissButton
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (title != null) {
                Box(Modifier.padding(horizontal = 4.dp)) { title() }
            }
            Text(
                text = selectedHeadline,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        // Month & year selector row with shape-morphing controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val yearPillShape by animateDpAsState(
                targetValue = if (showYearSelector) 22.dp else 12.dp,
                animationSpec = VibeMotion.slowSpatial(),
                label = "CalendarYearPillShape"
            )
            CompletePressFilledTonalButton(
                onClick = {
                    VibeHaptics.lightTick(view)
                    showYearSelector = !showYearSelector
                },
                shapes = ButtonShapes(
                    shape = RoundedCornerShape(yearPillShape),
                    pressedShape = RoundedCornerShape(8.dp)
                ),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (showYearSelector) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = if (showYearSelector) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.heightIn(min = 40.dp)
            ) {
                Text(
                    text = monthHeaderText,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }

            if (!showYearSelector) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val canPrev = displayedMonth.minusMonths(1).year >= yearRange.first
                    val canNext = displayedMonth.plusMonths(1).year <= yearRange.last
                    androidx.compose.material3.IconButton(
                        enabled = canPrev,
                        onClick = {
                            VibeHaptics.lightTick(view)
                            displayedMonth = displayedMonth.minusMonths(1)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = null
                        )
                    }
                    androidx.compose.material3.IconButton(
                        enabled = canNext,
                        onClick = {
                            VibeHaptics.lightTick(view)
                            displayedMonth = displayedMonth.plusMonths(1)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null
                        )
                    }
                }
            }
        }

        AnimatedContent(
            targetState = showYearSelector,
            transitionSpec = {
                if (motion == MotionLevel.MINIMAL) {
                    EnterTransition.None togetherWith ExitTransition.None
                } else {
                    (fadeIn(VibeMotion.defaultEffects())) togetherWith
                        (fadeOut(VibeMotion.fastEffects())) using
                        SizeTransform(clip = false) { _, _ -> VibeMotion.defaultSpatial() }
                }
            },
            label = "CalendarYearOrMonthContent"
        ) { selectingYear ->
            if (selectingYear) {
                val years = remember(yearRange) { yearRange.toList() }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(268.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(years, key = { it }) { year ->
                        val isCurrentYear = year == displayedMonth.year
                        val allowed = selectableDates?.isSelectableYear(year) ?: true
                        val corner by animateDpAsState(
                            targetValue = if (isCurrentYear) 22.dp else 10.dp,
                            animationSpec = VibeMotion.slowSpatial(),
                            label = "YearCellCorner"
                        )
                        val bg by animateColorAsState(
                            targetValue = if (isCurrentYear) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceContainerLow,
                            animationSpec = VibeMotion.defaultEffects(),
                            label = "YearCellBg"
                        )
                        val fg by animateColorAsState(
                            targetValue = if (isCurrentYear) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface,
                            animationSpec = VibeMotion.defaultEffects(),
                            label = "YearCellFg"
                        )
                        Box(
                            modifier = Modifier
                                .height(42.dp)
                                .clip(RoundedCornerShape(corner))
                                .background(bg)
                                .clickable(enabled = allowed) {
                                    VibeHaptics.selection(view)
                                    displayedMonth = YearMonth.of(year, displayedMonth.month)
                                    showYearSelector = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = year.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isCurrentYear) FontWeight.Bold else FontWeight.Medium,
                                color = if (allowed) fg else fg.copy(alpha = 0.38f)
                            )
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Fixed Chinese single-char weekday labels: 日 一 二 三 四 五 六 (never 星)
                    Row(modifier = Modifier.fillMaxWidth()) {
                        SundayFirstDays.forEach { dayOfWeek ->
                            val label = remember(dayOfWeek, locale) {
                                prettyWeekDay(dayOfWeek, locale)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(28.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    val firstDayOffset = displayedMonth.atDay(1).dayOfWeek.value % 7
                    val daysInMonth = displayedMonth.lengthOfMonth()
                    val totalCells = ((firstDayOffset + daysInMonth + 6) / 7) * 7

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        var cellIndex = 0
                        while (cellIndex < totalCells) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                for (col in 0 until 7) {
                                    val dayNum = cellIndex - firstDayOffset + 1
                                    if (dayNum in 1..daysInMonth) {
                                        val date = displayedMonth.atDay(dayNum)
                                        val utcMillis = date.localDateToUtcMillis()
                                        val enabled = selectableDates?.isSelectableDate(utcMillis) ?: true
                                        val isStart = date == startDate
                                        val isEnd = date == endDate
                                        val isInRange = isRangeMode && startDate != null && endDate != null &&
                                            date.isAfter(startDate) && date.isBefore(endDate)
                                        val isSelected = isStart || isEnd
                                        val isToday = date == today

                                        VibeCalendarDayCell(
                                            dayNumber = dayNum,
                                            enabled = enabled,
                                            isSelected = isSelected,
                                            isInRange = isInRange,
                                            isToday = isToday,
                                            onClick = {
                                                VibeHaptics.selection(view)
                                                if (!isRangeMode) {
                                                    onSingleDateSelected(utcMillis)
                                                } else {
                                                    val s = startDate
                                                    val e = endDate
                                                    when {
                                                        s == null || (s != null && e != null) || date.isBefore(s) ->
                                                            onRangeDateSelected(utcMillis, null)
                                                        date == s ->
                                                            onRangeDateSelected(utcMillis, utcMillis)
                                                        else ->
                                                            onRangeDateSelected(s.localDateToUtcMillis(), utcMillis)
                                                    }
                                                }
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    } else {
                                        Spacer(Modifier.weight(1f).aspectRatio(1f))
                                    }
                                    cellIndex++
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VibeCalendarDayCell(
    dayNumber: Int,
    enabled: Boolean,
    isSelected: Boolean,
    isInRange: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val minimal = LocalExperience.current.motion == MotionLevel.MINIMAL
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()

    val targetCorner = when {
        isPressed -> 8.dp
        isSelected -> 22.dp
        isInRange -> 10.dp
        isFocused -> 16.dp
        else -> 12.dp
    }
    val corner by animateDpAsState(
        targetValue = targetCorner,
        animationSpec = if (minimal) androidx.compose.animation.core.snap() else VibeMotion.slowSpatial(),
        label = "DayCellCornerMorph"
    )
    val scale by animateFloatAsState(
        targetValue = when {
            minimal -> 1f
            isPressed -> 0.88f
            isSelected -> 1.04f
            else -> 1f
        },
        animationSpec = VibeMotion.slowSpatial(),
        label = "DayCellScaleMorph"
    )
    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary
            isInRange -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            isFocused -> MaterialTheme.colorScheme.secondaryContainer
            isToday -> MaterialTheme.colorScheme.surfaceContainerHighest
            else -> Color.Transparent
        },
        animationSpec = VibeMotion.defaultEffects(),
        label = "DayCellBg"
    )
    val fgColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.34f)
            isSelected -> MaterialTheme.colorScheme.onPrimary
            isInRange -> MaterialTheme.colorScheme.onPrimaryContainer
            isToday -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurface
        },
        animationSpec = VibeMotion.defaultEffects(),
        label = "DayCellFg"
    )

    val shape = RoundedCornerShape(corner)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(1.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(bgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(bounded = true),
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = dayNumber.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
            color = fgColor
        )
    }
}

private fun Long.utcMillisToLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

private fun LocalDate.localDateToUtcMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VibeTimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    modeToggleButton: (@Composable () -> Unit)? = null,
    title: String,
    content: @Composable () -> Unit,
) {
    VibeMorphingPickerDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        leadingFooter = modeToggleButton
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
