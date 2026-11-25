@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.history

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerDialogDefaults
import androidx.compose.material3.TimePickerDisplayMode
import androidx.compose.material3.TimeScroll
import androidx.compose.material3.isInputValid
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.ui.graphics.graphicsLayer
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
                active = showDatePicker,
                modifier = Modifier.weight(1.45f).semantics { contentDescription = changeDate }
                    .testTag("EditTransactionDate"),
                onClick = { VibeHaptics.selection(view); showDatePicker = true }
            )
            DateTimeButton(
                text = timeText, icon = { Icon(Icons.Outlined.Schedule, null, Modifier.size(18.dp)) },
                shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(), enabled = enabled,
                active = showTimePicker,
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
        var selectedUtcMillis by rememberSaveable(timestamp) {
            mutableStateOf<Long?>(TransactionDateTime.toDatePickerMillis(timestamp))
        }
        com.example.vibefinance.ui.components.VibeDatePickerDialog(
            selectedUtcMillis = selectedUtcMillis,
            onDateSelected = { selectedUtcMillis = it },
            onDismissRequest = { showDatePicker = false },
            title = {
                Text(
                    text = stringResource(R.string.edit_date_picker_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                com.example.vibefinance.ui.components.CompletePressTextButton(
                    enabled = selectedUtcMillis != null,
                    onClick = {
                        selectedUtcMillis?.let { onTimestampChange(TransactionDateTime.withDate(timestamp, it)) }
                        VibeHaptics.selection(view)
                        showDatePicker = false
                    },
                    shapes = com.example.vibefinance.ui.components.AppButtonDefaults.shapes(),
                    modifier = Modifier.testTag("EditTransactionDateConfirm")
                ) { Text(stringResource(R.string.btn_confirm)) }
            },
            dismissButton = {
                com.example.vibefinance.ui.components.CompletePressTextButton(
                    onClick = { showDatePicker = false },
                    shapes = com.example.vibefinance.ui.components.AppButtonDefaults.shapes(),
                ) { Text(stringResource(R.string.btn_cancel)) }
            },
            modifier = Modifier.testTag("EditTransactionDateDialog")
        )
    }

    if (showTimePicker) {
        val timeState = rememberTimePickerState(initialHour = local.hour, initialMinute = local.minute, is24Hour = is24Hour)
        var isScrollMode by rememberSaveable { mutableStateOf(true) }
        val displayMode = if (isScrollMode) TimePickerDisplayMode.Scroll else TimePickerDisplayMode.Input
        com.example.vibefinance.ui.components.VibeTimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            title = stringResource(R.string.edit_time_picker_title),
            confirmButton = {
                com.example.vibefinance.ui.components.CompletePressTextButton(
                    enabled = timeState.isInputValid,
                    onClick = {
                        onTimestampChange(TransactionDateTime.withTime(timestamp, timeState.hour, timeState.minute))
                        VibeHaptics.selection(view)
                        showTimePicker = false
                    },
                    shapes = com.example.vibefinance.ui.components.AppButtonDefaults.shapes(),
                    modifier = Modifier.testTag("EditTransactionTimeConfirm")
                ) { Text(stringResource(R.string.btn_confirm)) }
            },
            dismissButton = {
                com.example.vibefinance.ui.components.CompletePressTextButton(
                    onClick = { showTimePicker = false },
                    shapes = com.example.vibefinance.ui.components.AppButtonDefaults.shapes(),
                ) { Text(stringResource(R.string.btn_cancel)) }
            },
            modeToggleButton = {
                TimePickerDialogDefaults.ScrollDisplayModeToggle(
                    onDisplayModeChange = { VibeHaptics.selection(view); isScrollMode = !isScrollMode },
                    displayMode = displayMode,
                )
            },
            modifier = Modifier.testTag("EditTransactionTimeDialog")
        ) {
            AnimatedContent(
                targetState = displayMode,
                transitionSpec = {
                    if (motion == MotionLevel.MINIMAL) {
                        EnterTransition.None togetherWith ExitTransition.None
                    } else {
                        fadeIn(com.example.vibefinance.theme.VibeMotion.defaultEffects()) togetherWith
                            fadeOut(com.example.vibefinance.theme.VibeMotion.fastEffects()) using
                            SizeTransform(clip = false) { _, _ -> com.example.vibefinance.theme.VibeMotion.slowSpatial() }
                    }
                },
                label = "TimePickerModeMorph"
            ) { mode ->
                if (mode == TimePickerDisplayMode.Input) {
                    TimeInput(state = timeState, shapes = TimePickerDefaults.shapes())
                } else {
                    TimeScroll(state = timeState, shapes = TimePickerDefaults.shapes())
                }
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
    active: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val minimal = LocalExperience.current.motion == MotionLevel.MINIMAL
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()
    val emphasized = active || isFocused

    // Slower spatial focus & active pill morphing so focus feedback never feels rushed.
    val activeCorner by animateDpAsState(
        targetValue = when {
            isPressed -> 8.dp
            emphasized -> 24.dp
            else -> 12.dp
        },
        animationSpec = if (minimal) androidx.compose.animation.core.snap()
        else com.example.vibefinance.theme.VibeMotion.slowSpatial(),
        label = "DateTimeButtonCornerMorph"
    )
    val pressScale by animateFloatAsState(
        targetValue = when {
            minimal -> 1f
            isPressed -> 0.94f
            emphasized -> 1.02f
            else -> 1f
        },
        animationSpec = com.example.vibefinance.theme.VibeMotion.slowSpatial(),
        label = "DateTimeButtonFocusScale"
    )
    val containerColor by animateColorAsState(
        targetValue = if (emphasized) colors.primaryContainer else colors.surfaceContainerHigh,
        animationSpec = com.example.vibefinance.theme.VibeMotion.defaultEffects(),
        label = "DateTimeButtonContainer"
    )
    val contentColor by animateColorAsState(
        targetValue = if (emphasized) colors.onPrimaryContainer else colors.onSurface,
        animationSpec = com.example.vibefinance.theme.VibeMotion.defaultEffects(),
        label = "DateTimeButtonContent"
    )

    val effectiveShape = if (isPressed || emphasized) RoundedCornerShape(activeCorner) else shapes.shape
    ConnectedButtonRipple(isSelected = emphasized) {
        CompletePressFilledTonalButton(
            onClick = onClick,
            enabled = enabled,
            interactionSource = interactionSource,
            shapes = ButtonShapes(shape = effectiveShape, pressedShape = shapes.pressedShape),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
            elevation = null,
            contentPadding = PaddingValues(0.dp),
            modifier = modifier.graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(com.example.vibefinance.ui.components.LocalPostPressIconBounce.current) { icon() }
                Text(text, maxLines = 1, softWrap = false, overflow = TextOverflow.Visible,
                    style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
