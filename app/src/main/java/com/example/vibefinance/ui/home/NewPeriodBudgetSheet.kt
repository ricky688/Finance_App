@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.home

import com.example.vibefinance.ui.components.CompletePressButton
import com.example.vibefinance.ui.components.CompletePressTextButton

import com.example.vibefinance.R
import androidx.compose.ui.res.stringResource
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.vibefinance.ui.common.pressBounce
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibefinance.data.repository.DailyBudgetInfo
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPeriodBudgetSheet(
    budgetInfo: DailyBudgetInfo?,
    onConfirmNewPeriod: (amount: Double, startDate: Long, endDate: Long) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f || isSystemInDarkTheme()

    // Pre-fill initial budget amount from previous period or default 1500
    val prevBudgetAmount = budgetInfo?.totalMonthlyBudget ?: 1500.0
    val leftoverAmount = budgetInfo?.monthlyRemaining ?: 0.0

    var amountInputText by remember {
        mutableStateOf(if (prevBudgetAmount > 0) String.format(Locale.US, "%.0f", prevBudgetAmount) else "1500")
    }

    // Default period: 30 Days starting today
    var selectedDaysPreset by remember { mutableStateOf(30) }
    var customEndDateMillis by remember { mutableStateOf<Long?>(null) }
    var isRolloverEnabled by remember { mutableStateOf(leftoverAmount > 0) }

    var showDatePickerModal by remember { mutableStateOf(false) }

    val todayLocal = remember { LocalDate.now() }
    val startDateMillis = remember { System.currentTimeMillis() }

    val calculatedEndDateMillis = remember(selectedDaysPreset, customEndDateMillis) {
        if (selectedDaysPreset == -1 && customEndDateMillis != null) {
            customEndDateMillis!!
        } else {
            val days = if (selectedDaysPreset > 0) selectedDaysPreset else 30
            val targetLocal = todayLocal.plusDays(days.toLong() - 1)
            targetLocal.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }
    }

    val endDateLocal = remember(calculatedEndDateMillis) {
        Instant.ofEpochMilli(calculatedEndDateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    val totalDays = remember(todayLocal, endDateLocal) {
        (java.time.temporal.ChronoUnit.DAYS.between(todayLocal, endDateLocal) + 1).coerceAtLeast(1).toInt()
    }

    val baseBudget = amountInputText.toDoubleOrNull() ?: 0.0
    val finalTotalBudget = if (isRolloverEnabled && leftoverAmount > 0) baseBudget + leftoverAmount else baseBudget
    val estDailyBudget = if (totalDays > 0) finalTotalBudget / totalDays else 0.0

    val sheetContainerColor = if (isDark) Color(0xFF16181D) else Color(0xFFFAFAFD)
    val cardContainerColor = if (isDark) Color(0xFF252830) else Color(0xFFF0F1F5)
    val cardBorder = if (isDark) BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)) else BorderStroke(1.dp, Color.Black.copy(alpha = 0.06f))
    val primaryTextColor = if (isDark) Color.White else Color(0xFF1A1C20)
    val secondaryTextColor = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF626673)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetContainerColor,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow))
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Header Banner
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = stringResource(R.string.loc_new_period_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = primaryTextColor,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.loc_new_period_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = secondaryTextColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // 2. Previous Period Leftover Summary Card
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (leftoverAmount >= 0) {
                            if (isDark) Color(0xFF0F3820) else Color(0xFFE8F5E9)
                        } else {
                            if (isDark) Color(0xFF381212) else Color(0xFFFFEBEE)
                        },
                        border = BorderStroke(
                            1.dp,
                            if (leftoverAmount >= 0) Color(0xFF4CAF50).copy(alpha = 0.4f) else Color(0xFFF44336).copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (leftoverAmount >= 0) Icons.Default.CheckCircle else Icons.Default.RestartAlt,
                                    contentDescription = null,
                                    tint = if (leftoverAmount >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (leftoverAmount >= 0) stringResource(R.string.loc_previous_leftover) else stringResource(R.string.loc_previous_overspent),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (leftoverAmount >= 0) {
                                            if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
                                        } else {
                                            if (isDark) Color(0xFFE57373) else Color(0xFFC62828)
                                        }
                                    )
                                    Text(
                                        text = String.format(Locale.US, "HK$ %,.2f", Math.abs(leftoverAmount)),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = primaryTextColor
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. New Base Budget Amount Input
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.loc_new_budget_amount),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = primaryTextColor
                        )

                        OutlinedTextField(
                            value = amountInputText,
                            onValueChange = { amountInputText = it },
                            placeholder = { Text("1500") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Paid,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                Text(
                                    text = "HKD",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = secondaryTextColor,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 4. Period Duration Preset Pills & Custom Picker
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.loc_period_duration),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = primaryTextColor
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val presets = listOf(
                                30 to stringResource(R.string.loc_duration_30),
                                14 to stringResource(R.string.loc_duration_14),
                                7 to stringResource(R.string.loc_duration_7)
                            )

                            presets.forEach { (days, label) ->
                                val isSelected = selectedDaysPreset == days
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selectedDaysPreset = days
                                        customEndDateMillis = null
                                    },
                                    label = { Text(label) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Custom Date Picker Button
                        OutlinedButton(
                            onClick = { showDatePickerModal = true },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            val startStr = todayLocal.format(DateTimeFormatter.ofPattern("dd MMM", androidx.compose.ui.platform.LocalConfiguration.current.locales[0]))
                            val endStr = endDateLocal.format(DateTimeFormatter.ofPattern("dd MMM yyyy", androidx.compose.ui.platform.LocalConfiguration.current.locales[0]))
                            Text(
                                text = stringResource(R.string.loc_period_dates, startStr, endStr, totalDays),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 5. Buckwheat Rollover Options
                if (leftoverAmount > 0) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = stringResource(R.string.loc_leftover_rollover),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = primaryTextColor
                            )

                            // Option A: Rollover
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = if (isRolloverEnabled) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else cardContainerColor,
                                border = if (isRolloverEnabled) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else cardBorder,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        isRolloverEnabled = true
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isRolloverEnabled,
                                        onClick = { isRolloverEnabled = true }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = stringResource(R.string.loc_carry_over),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = primaryTextColor
                                        )
                                        Text(
                                            text = stringResource(R.string.loc_rollover_total, finalTotalBudget, leftoverAmount),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = secondaryTextColor
                                        )
                                    }
                                }
                            }

                            // Option B: Fresh Start
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = if (!isRolloverEnabled) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else cardContainerColor,
                                border = if (!isRolloverEnabled) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else cardBorder,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        isRolloverEnabled = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = !isRolloverEnabled,
                                        onClick = { isRolloverEnabled = false }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = stringResource(R.string.loc_fresh_start),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = primaryTextColor
                                        )
                                        Text(
                                            text = stringResource(R.string.loc_budget_total, baseBudget),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = secondaryTextColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 6. Real-time Summary Card (Estimated Daily Allowance)
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.loc_est_daily_baseline),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = secondaryTextColor
                                )
                                Text(
                                    text = stringResource(R.string.loc_daily_budget_amount, estDailyBudget),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = stringResource(R.string.loc_total_days, totalDays),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = primaryTextColor
                            )
                        }
                    }
                }

                // 7. Confirm Button
                item {
                    val confirmInteraction = remember { MutableInteractionSource() }
                    CompletePressButton(
                        onClick = {
                            if (baseBudget > 0 && calculatedEndDateMillis > startDateMillis) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onConfirmNewPeriod(finalTotalBudget, startDateMillis, calculatedEndDateMillis)
                                onDismiss()
                            }
                        },
                        enabled = baseBudget > 0,
                        shapes = ButtonDefaults.shapes(shape = RoundedCornerShape(27.dp), pressedShape = RoundedCornerShape(16.dp)),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 1.dp),
                        interactionSource = confirmInteraction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .pressBounce(interactionSource = confirmInteraction)
                    ) {
                        Text(
                            text = stringResource(R.string.loc_start_new_period),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Material 3 Custom End Date Picker Modal
    if (showDatePickerModal) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = calculatedEndDateMillis
        )

        DatePickerDialog(
            onDismissRequest = { showDatePickerModal = false },
            confirmButton = {
                val dateConfirmInteraction = remember { MutableInteractionSource() }
                CompletePressTextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        datePickerState.selectedDateMillis?.let {
                            customEndDateMillis = it
                            selectedDaysPreset = -1
                        }
                        showDatePickerModal = false
                    },
                    shapes = ButtonDefaults.shapes(shape = CircleShape, pressedShape = RoundedCornerShape(percent = 32)),
                    interactionSource = dateConfirmInteraction,
                    modifier = Modifier.pressBounce(interactionSource = dateConfirmInteraction)
                ) {
                    Text(stringResource(R.string.btn_confirm))
                }
            },
            dismissButton = {
                val dateDismissInteraction = remember { MutableInteractionSource() }
                CompletePressTextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showDatePickerModal = false
                    },
                    shapes = ButtonDefaults.shapes(shape = CircleShape, pressedShape = RoundedCornerShape(percent = 32)),
                    interactionSource = dateDismissInteraction,
                    modifier = Modifier.pressBounce(interactionSource = dateDismissInteraction)
                ) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
