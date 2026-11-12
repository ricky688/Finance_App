package com.example.vibefinance.ui.main

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.ImeAction
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.theme.MotionTokens
import com.example.vibefinance.ui.common.ScrollEdgeEffects
import com.example.vibefinance.ui.common.bouncyClickable
import com.example.vibefinance.ui.components.AppOutlinedButton
import com.example.vibefinance.ui.components.AppTextButton
import com.example.vibefinance.ui.preferences.LocalExperience
import com.example.vibefinance.ui.preferences.MotionLevel
import com.example.vibefinance.ui.preferences.PrivacyText as Text
import com.example.vibefinance.util.Reimbursements
import java.math.BigDecimal
import java.text.DateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Date

internal fun reimbursementExpensesForSearch(expenses: List<TransactionEntity>, search: String,
    selectedEpochDay: Long?, zone: ZoneId): List<TransactionEntity> {
    val query = search.trim()
    return expenses.filter {
        (it.category.contains(query, true) || it.description.contains(query, true)) &&
            (selectedEpochDay == null || Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate().toEpochDay() == selectedEpochDay)
    }
}

/** Shared by Add Income and History; date/text filters never change the repayment itself. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReimbursementExpenseSelector(records: List<TransactionEntity>, selectedId: Long, enabled: Boolean = true,
    latestExpenseTimestamp: Long? = null, onClear: (() -> Unit)? = null, onSelect: (TransactionEntity) -> Unit) {
    var showExpenses by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var search by rememberSaveable { mutableStateOf("") }
    var selectedEpochDay by rememberSaveable { mutableStateOf<Long?>(null) }
    val locale = LocalContext.current.resources.configuration.locales[0]
    val zone = ZoneId.systemDefault()
    val dateFormat = remember(locale, zone) { DateFormat.getDateInstance(DateFormat.MEDIUM, locale).apply {
        timeZone = java.util.TimeZone.getTimeZone(zone)
    } }
    val motion = LocalExperience.current.motion
    val creditsByExpense = remember(records) { records.filter { it.reimbursementOf != null }.groupBy { it.reimbursementOf!! } }
    val expenses = remember(records, creditsByExpense, latestExpenseTimestamp, zone) {
        val now = System.currentTimeMillis()
        records.filter { Reimbursements.isExpense(it) && it.timestamp <= now &&
            Reimbursements.expenseOnOrBeforeDay(it.timestamp, minOf(now, latestExpenseTimestamp ?: now), zone) }.sortedByDescending { it.timestamp }
    }
    val availableDays = remember(expenses, zone) {
        expenses.map { Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() }.toSet()
    }
    val filtered = remember(expenses, search, selectedEpochDay, zone) {
        reimbursementExpensesForSearch(expenses, search, selectedEpochDay, zone)
    }
    val original = expenses.firstOrNull { it.id == selectedId }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AppOutlinedButton(onClick = { showExpenses = true }, enabled = enabled && expenses.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("ReimbursementExpense")) {
            Text(original?.description?.takeIf { it.isNotBlank() } ?: original?.category ?: stringResource(R.string.reimbursement_choose_expense), maxLines = 1)
        }
        Text(stringResource(R.string.reimbursement_optional_hint), style = MaterialTheme.typography.bodySmall)
        if (selectedId != 0L && onClear != null) TextButton(onClick = onClear, enabled = enabled) {
            Text(stringResource(R.string.reimbursement_unlink))
        }
        if (expenses.isEmpty()) Text(stringResource(R.string.reimbursement_empty))
        original?.let { Text(stringResource(R.string.reimbursement_outstanding,
            Reimbursements.outstanding(it, creditsByExpense[it.id].orEmpty()).toDouble()), color = MaterialTheme.colorScheme.primary) }
    }
    if (showExpenses) {
        val list = rememberLazyListState()
        val density = LocalDensity.current
        val edges by remember(list, density) { derivedStateOf {
            val info = list.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()
            with(density) {
                val top = if (!list.canScrollBackward) 0.dp else if (list.firstVisibleItemIndex > 0) 16.dp
                    else list.firstVisibleItemScrollOffset.toDp().coerceIn(0.dp, 16.dp)
                val bottom = if (!list.canScrollForward || last == null) 0.dp else if (last.index < info.totalItemsCount - 1) 16.dp
                    else (last.offset + last.size - (info.viewportEndOffset - info.afterContentPadding)).toDp().coerceIn(0.dp, 16.dp)
                top to bottom
            }
        } }
        LaunchedEffect(search, selectedEpochDay) { list.scrollToItem(0) }
        BasicAlertDialog(onDismissRequest = { showExpenses = false; showDatePicker = false },
            modifier = Modifier.testTag("ReimbursementSearchDialog")) {
            val keyboard = LocalSoftwareKeyboardController.current
            val focus = LocalFocusManager.current
            Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp) {
                Box {
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(stringResource(R.string.reimbursement_choose_expense), style = MaterialTheme.typography.headlineSmall)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = search, onValueChange = { search = it }, singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { keyboard?.hide(); focus.clearFocus() }),
                                label = { Text(stringResource(R.string.reimbursement_search)) }, shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.fillMaxWidth().testTag("ReimbursementSearch"))
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AppOutlinedButton(onClick = { keyboard?.hide(); focus.clearFocus(); showDatePicker = true },
                                    modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("ReimbursementDateFilter")) {
                                    Icon(Icons.Default.CalendarMonth, null, Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(selectedEpochDay?.let { day -> dateFormat.format(Date(LocalDate.ofEpochDay(day)
                                        .atStartOfDay(zone).toInstant().toEpochMilli())) } ?: stringResource(R.string.reimbursement_all_dates),
                                        style = MaterialTheme.typography.labelLarge)
                                }
                                if (selectedEpochDay != null) IconButton(onClick = { selectedEpochDay = null },
                                    modifier = Modifier.testTag("ReimbursementDateClear")) {
                                    Icon(Icons.Default.Close, stringResource(R.string.reimbursement_clear_date))
                                }
                            }
                            AnimatedContent(filtered.size, label = "Repayment search count", transitionSpec = {
                                if (motion == MotionLevel.MINIMAL) EnterTransition.None togetherWith ExitTransition.None
                                else fadeIn(MotionTokens.emphasized(160)) togetherWith fadeOut(MotionTokens.emphasized(100))
                            }) { count -> Text(stringResource(R.string.history_results_count, count),
                                style = MaterialTheme.typography.bodySmall, modifier = Modifier.testTag("ReimbursementResultCount")) }
                            ScrollEdgeEffects(edges.first, edges.second, Modifier.fillMaxWidth().height(320.dp), "ReimbursementSearch") { source ->
                                LazyColumn(source.fillMaxSize().testTag("ReimbursementSearchResults"), state = list,
                                    verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 72.dp)) {
                                    items(filtered, key = { it.id }) { expense ->
                                        ListItem(headlineContent = { Text(expense.description.ifBlank { expense.category }) },
                                            supportingContent = { Text(expense.category + " · " + dateFormat.format(Date(expense.timestamp)) + "\n" +
                                                stringResource(R.string.reimbursement_outstanding, Reimbursements.outstanding(expense, creditsByExpense[expense.id].orEmpty()).toDouble())) },
                                            modifier = Modifier.animateItem(
                                                fadeInSpec = if (motion == MotionLevel.MINIMAL) null else MotionTokens.emphasized(160),
                                                placementSpec = if (motion != MotionLevel.FULL) null else spring(
                                                    dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
                                                fadeOutSpec = if (motion == MotionLevel.MINIMAL) null else MotionTokens.emphasized(120))
                                                .bouncyClickable(shape = RoundedCornerShape(20.dp)) { onSelect(expense); showExpenses = false }
                                                .testTag("ReimbursementSource_${expense.id}"))
                                    }
                                    if (filtered.isEmpty()) item("empty") {
                                        Text(stringResource(R.string.reimbursement_no_matches), Modifier.padding(16.dp).testTag("ReimbursementSearchEmpty"))
                                    }
                                }
                            }
                        }
                    }
                    SmallFloatingActionButton(onClick = { showExpenses = false; showDatePicker = false },
                        modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).testTag("ReimbursementSearchClose"),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer) {
                        Icon(Icons.Default.Close, stringResource(R.string.btn_close))
                    }
                }
            }
        }
    }
    if (showExpenses && showDatePicker) {
        val selectable = remember(availableDays) { object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) =
                Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate() in availableDays
            override fun isSelectableYear(year: Int) = availableDays.any { it.year == year }
        } }
        val initialDay = selectedEpochDay?.let(LocalDate::ofEpochDay)
        fun LocalDate.utcMillis() = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val picker = rememberDatePickerState(initialSelectedDateMillis = initialDay?.utcMillis(),
            initialDisplayedMonthMillis = (initialDay ?: availableDays.maxOrNull())?.utcMillis(), selectableDates = selectable)
        DatePickerDialog(onDismissRequest = { showDatePicker = false }, modifier = Modifier.testTag("ReimbursementDatePicker"),
            confirmButton = { AppTextButton(onClick = {
                picker.selectedDateMillis?.let { selectedEpochDay = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay() }
                showDatePicker = false
            }, enabled = picker.selectedDateMillis != null, modifier = Modifier.testTag("ReimbursementDateApply")) {
                Text(stringResource(R.string.btn_confirm))
            } }, dismissButton = { AppTextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.btn_cancel)) } }) {
            DatePicker(picker)
        }
    }
}
