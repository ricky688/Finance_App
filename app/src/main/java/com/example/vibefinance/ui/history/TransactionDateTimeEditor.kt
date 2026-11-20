@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.history

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerDialogDefaults
import androidx.compose.material3.TimePickerDisplayMode
import androidx.compose.material3.TimeScroll
import androidx.compose.material3.VibrantTimePickerDialog
import androidx.compose.material3.isInputValid
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.vibefinance.R
import com.example.vibefinance.ui.components.CompletePressFilledTonalButton
import com.example.vibefinance.ui.components.ConnectedButtonRipple
import com.example.vibefinance.ui.preferences.LocalExperience
import com.example.vibefinance.ui.motion.sharedTransitionBounds
import com.example.vibefinance.ui.preferences.MotionLevel
import com.example.vibefinance.util.TransactionDateTime
import com.example.vibefinance.util.VibeHaptics
import java.time.format.DateTimeFormatter


/**
 * Date and time row for the History edit dialog.
 *
 * Date uses the Material 3 [DatePicker] dialog. Time uses the Material 3 Expressive
 * [VibrantTimePickerDialog] (material3 1.5.0, 18 Nov 2026) with the new [TimeScroll] wheel and a
 * keyboard [TimeInput] fallback, following the official `VibrantTimePickerScrollSample`.
 */
@Composable
internal fun TransactionDateTimeEditor(
    timestamp: Long,
    enabled: Boolean,
    onTimestampChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val locale = LocalConfiguration.current.locales[0]
    val is24Hour = DateFormat.is24HourFormat(context)
    val local = TransactionDateTime.localDateTime(timestamp)
    val dateText = remember(local.toLocalDate(), locale) {
        local.format(DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, "yMMMdEEE"), locale))
    }
    // Narrow dialogs drop the year so the weekday is never clipped; the spoken label keeps it.
    val shortDateText = remember(local.toLocalDate(), locale) {
        local.format(DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, "MMMdEEE"), locale))
    }
    val timeText = remember(local.hour, local.minute, locale, is24Hour) {
        local.format(DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, if (is24Hour) "Hm" else "hma"), locale))
    }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    val motion = LocalExperience.current.motion

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // The calendar and clock icons label the pair in the compact phone dialog.
        if (!LocalCompactTransactionForm.current) Text(stringResource(R.string.edit_datetime_label), style = MaterialTheme.typography.labelLarge)
        // Same connected-group geometry, spacing and complete-press morph as the type selector.
        androidx.compose.foundation.layout.BoxWithConstraints(
            Modifier.fillMaxWidth().sharedTransitionBounds(SharedTxKeys.DateTime)
        ) {
        val compact = maxWidth < 360.dp
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
            val changeDate = stringResource(R.string.edit_datetime_change_date, dateText)
            val changeTime = stringResource(R.string.edit_datetime_change_time, timeText)
            DateTimeButton(
                text = if (compact) shortDateText else dateText, icon = { Icon(Icons.Outlined.CalendarMonth, null, Modifier.size(18.dp)) },
                shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(), enabled = enabled,
                modifier = Modifier.weight(1.45f).semantics { contentDescription = changeDate }
                    .testTag("EditTransactionDate"),
                onClick = { VibeHaptics.selection(view); showDatePicker = true }
            )
            DateTimeButton(
                text = timeText, icon = { Icon(Icons.Outlined.Schedule, null, Modifier.size(18.dp)) },
                shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(), enabled = enabled,
                modifier = Modifier.weight(1f).semantics { contentDescription = changeTime }
                    .testTag("EditTransactionTime"),
                onClick = { VibeHaptics.selection(view); showTimePicker = true }
            )
        }
        }
        AnimatedVisibility(
            visible = timestamp > System.currentTimeMillis(),
            enter = if (motion == MotionLevel.MINIMAL) EnterTransition.None else
                fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                    expandVertically(com.example.vibefinance.theme.VibeMotion.defaultSpatial()),
            exit = if (motion == MotionLevel.MINIMAL) ExitTransition.None else
                fadeOut(spring(stiffness = Spring.StiffnessMedium)) + shrinkVertically(spring(stiffness = Spring.StiffnessMediumLow))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.testTag("EditTransactionFutureHint")) {
                Icon(Icons.Outlined.Update, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.tertiary)
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.edit_datetime_future_hint), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary)
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = TransactionDateTime.toDatePickerMillis(timestamp))
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    enabled = pickerState.selectedDateMillis != null,
                    onClick = {
                        pickerState.selectedDateMillis?.let { onTimestampChange(TransactionDateTime.withDate(timestamp, it)) }
                        VibeHaptics.selection(view)
                        showDatePicker = false
                    },
                    modifier = Modifier.testTag("EditTransactionDateConfirm")
                ) { Text(stringResource(R.string.btn_confirm)) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.btn_cancel)) } }
        ) {
            DatePicker(
                state = pickerState,
                title = {
                    Text(stringResource(R.string.edit_date_picker_title),
                        Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp))
                }
            )
        }
    }

    if (showTimePicker) {
        val timeState = rememberTimePickerState(initialHour = local.hour, initialMinute = local.minute, is24Hour = is24Hour)
        var isScrollMode by rememberSaveable { mutableStateOf(true) }
        val displayMode = if (isScrollMode) TimePickerDisplayMode.Scroll else TimePickerDisplayMode.Input
        VibrantTimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    enabled = timeState.isInputValid,
                    onClick = {
                        onTimestampChange(TransactionDateTime.withTime(timestamp, timeState.hour, timeState.minute))
                        VibeHaptics.selection(view)
                        showTimePicker = false
                    },
                    modifier = Modifier.testTag("EditTransactionTimeConfirm")
                ) { Text(stringResource(R.string.btn_confirm)) }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.btn_cancel)) } },
            modeToggleButton = {
                TimePickerDialogDefaults.ScrollDisplayModeToggle(
                    onDisplayModeChange = { VibeHaptics.selection(view); isScrollMode = !isScrollMode },
                    displayMode = displayMode,
                )
            },
            modifier = Modifier.testTag("EditTransactionTimeDialog")
        ) {
            Text(
                stringResource(R.string.edit_time_picker_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            if (displayMode == TimePickerDisplayMode.Input) {
                TimeInput(state = timeState, shapes = TimePickerDefaults.shapes())
            } else {
                TimeScroll(state = timeState, shapes = TimePickerDefaults.shapes())
            }
        }
    }
}

@Composable
private fun DateTimeButton(
    text: String,
    icon: @Composable () -> Unit,
    shapes: ToggleButtonShapes,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    ConnectedButtonRipple {
        CompletePressFilledTonalButton(
            onClick = onClick,
            enabled = enabled,
            shapes = ButtonShapes(shape = shapes.shape, pressedShape = shapes.pressedShape),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = colors.surfaceContainerHigh,
                contentColor = colors.onSurface
            ),
            elevation = null,
            contentPadding = PaddingValues(0.dp),
            modifier = modifier
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon()
                Text(text, maxLines = 1, softWrap = false, overflow = TextOverflow.Visible,
                    style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
