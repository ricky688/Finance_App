package com.example.vibefinance.ui.accounts

import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.ui.components.AppModalBottomSheet
import com.example.vibefinance.ui.components.rememberSheetDismissAction
import com.example.vibefinance.util.CreditCardCycleCalculator
import com.example.vibefinance.util.CurrencyEngine
import com.example.vibefinance.util.VibeHaptics
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun StatementCycleSheet(
    account: AccountEntity,
    transactions: List<TransactionEntity>,
    hideAmounts: Boolean,
    onDismiss: () -> Unit,
    onSetOffset: (TransactionEntity, Int?) -> Unit
) {
    var period by rememberSaveable(account.id) { mutableIntStateOf(0) }
    var editingId by rememberSaveable(account.id) { mutableStateOf<Long?>(null) }
    val now = rememberFinancialNow()
    val zone = ZoneId.systemDefault()
    val calculator = remember(now, zone) { CreditCardCycleCalculator(java.time.Clock.fixed(Instant.ofEpochMilli(now), zone)) }
    val cycle = remember(account, calculator, period) {
        calculator.shiftCycle(account, calculator.currentCycle(account), period)
    }
    val records = remember(account, transactions, cycle, calculator) {
        calculator.transactionsInCycle(account, transactions, cycle).sortedByDescending { it.timestamp }.toList()
    }
    val total = remember(records) { records.sumOf { it.amount } }
    val locale = LocalConfiguration.current.locales[0]
    val date = remember(locale) { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale) }
    val view = LocalView.current
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dismiss = rememberSheetDismissAction(sheet)
    val periods = listOf(R.string.statement_cycle_previous, R.string.statement_cycle_current, R.string.statement_cycle_next)

    AppModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheet) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.statement_cycle_title), style = MaterialTheme.typography.titleLarge)
            Text(account.nickname?.takeIf { it.isNotBlank() } ?: account.name, style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                periods.forEachIndexed { index, label ->
                    FilterChip(selected = period == index - 1, onClick = {
                        if (period != index - 1) {
                            VibeHaptics.selection(view)
                            period = index - 1
                        }
                    }, label = { Text(stringResource(label), maxLines = 1, softWrap = false) })
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { VibeHaptics.selection(view); period-- }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, stringResource(R.string.statement_cycle_previous))
                }
                Text(stringResource(R.string.statement_cycle_range, date.format(cycle.start), date.format(cycle.closing)),
                    modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                IconButton(onClick = { VibeHaptics.selection(view); period++ }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, stringResource(R.string.statement_cycle_next))
                }
            }
            Text(if (hideAmounts) "••••" else stringResource(R.string.cycle_spent, total),
                style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.statement_cycle_hint), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        key(period) {
            LazyColumn(Modifier.weight(1f, fill = false).fillMaxWidth(), contentPadding = PaddingValues(vertical = 8.dp)) {
                if (records.isEmpty()) item {
                    Text(stringResource(R.string.statement_cycle_empty), Modifier.padding(20.dp))
                }
                items(records, key = { it.id }) { transaction ->
                    val transactionDate = Instant.ofEpochMilli(transaction.timestamp).atZone(zone).toLocalDate()
                    Surface(onClick = {
                        VibeHaptics.selection(view)
                        editingId = transaction.id
                    }, modifier = Modifier.fillMaxWidth()) {
                        ListItem(
                            headlineContent = { Text(transaction.description.ifBlank { transaction.category }) },
                            supportingContent = {
                                Column {
                                    Text(date.format(transactionDate))
                                    if (transaction.statementCycleOffset != null) {
                                        Text(stringResource(R.string.statement_cycle_adjusted))
                                    }
                                }
                            },
                            trailingContent = {
                                Text(if (hideAmounts) "••••" else CurrencyEngine.formatSignedAmount(transaction.amount),
                                    maxLines = 1, softWrap = false)
                            }
                        )
                    }
                }
            }
        }
        TextButton(onClick = { dismiss(onDismiss) }, modifier = Modifier.align(Alignment.End).padding(end = 16.dp)) {
            Text(stringResource(R.string.btn_close))
        }
    }

    // Freeze the selected record while editing so the storage layer can detect concurrent changes.
    records.firstOrNull { it.id == editingId }?.let { transaction ->
        val expected = remember(transaction.id) { transaction }
        var selected by rememberSaveable(transaction.id) { mutableIntStateOf(expected.statementCycleOffset ?: 0) }
        val naturalCycle = remember(account, calculator, expected) { calculator.cycleForTransaction(account, expected) }
        AlertDialog(
            onDismissRequest = { editingId = null },
            title = { Text(stringResource(R.string.statement_cycle_adjust)) },
            text = {
                Column(Modifier.selectableGroup()) {
                    for (offset in -1..1) {
                        val destination = calculator.shiftCycle(account, naturalCycle, offset)
                        val label = if (offset == 0) R.string.statement_cycle_auto else periods[offset + 1]
                        Row(
                            Modifier.fillMaxWidth()
                                .selectable(selected == offset, role = Role.RadioButton, onClick = {
                                    VibeHaptics.selection(view)
                                    selected = offset
                                })
                                .heightIn(min = 48.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            RadioButton(selected == offset, onClick = null)
                            Column(Modifier.padding(vertical = 8.dp)) {
                                Text(stringResource(label))
                                Text(stringResource(R.string.statement_cycle_closing, date.format(destination.closing)),
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    editingId = null
                    onSetOffset(expected, selected.takeUnless { it == 0 })
                }, enabled = selected != (expected.statementCycleOffset ?: 0)) {
                    Text(stringResource(R.string.btn_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { editingId = null }) { Text(stringResource(R.string.btn_cancel)) }
            }
        )
    }
}
