package com.example.vibefinance.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.vibefinance.R
import com.example.vibefinance.data.BatchTransactionOperation
import com.example.vibefinance.data.entity.*
import com.example.vibefinance.ui.FinanceUiState

@Composable
internal fun HistoryBatchToolbar(count: Int, allSelected: Boolean, busy: Boolean, canCategory: Boolean, canAccount: Boolean,
    onClose: () -> Unit, onSelectAll: () -> Unit, onAction: (String) -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        tonalElevation = 6.dp, shadowElevation = 3.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .35f)),
        modifier = Modifier.fillMaxWidth().testTag("HistoryBatchToolbar")) {
        BoxWithConstraints(Modifier.padding(4.dp)) {
            val narrow = maxWidth < 500.dp || androidx.compose.ui.platform.LocalDensity.current.fontScale > 1.3f
            val actions: @Composable () -> Unit = {
                TextButton(onClick = { onAction("category") }, enabled = !busy && canCategory, modifier = Modifier.testTag("HistoryBatchCategory")) { Text(stringResource(R.string.edit_category_label), maxLines = 1) }
                TextButton(onClick = { onAction("account") }, enabled = !busy && canAccount, modifier = Modifier.testTag("HistoryBatchAccount")) { Text(stringResource(R.string.batch_account), maxLines = 1) }
                TextButton(onClick = { onAction("delete") }, enabled = !busy && count > 0, modifier = Modifier.testTag("HistoryBatchDelete")) { Text(stringResource(R.string.batch_delete), maxLines = 1, color = MaterialTheme.colorScheme.error) }
            }
            Column {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    IconButton(onClick = onClose, enabled = !busy, modifier = Modifier.testTag("HistoryBatchClose")) { Icon(Icons.Default.Close, stringResource(R.string.batch_close)) }
                    Text(stringResource(R.string.batch_selected, count), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    TextButton(onClick = onSelectAll, enabled = !busy, modifier = Modifier.testTag("HistoryBatchSelectAll")) { Text(stringResource(if (allSelected) R.string.batch_select_none else R.string.batch_select_all), maxLines = 1) }
                    if (!narrow) actions()
                }
                if (narrow) FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { actions() }
                if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HistoryBatchPicker(kind: String, records: List<TransactionEntity>, state: FinanceUiState, busy: Boolean, error: String?,
    onDismiss: () -> Unit, onConfirm: (BatchTransactionOperation) -> Unit) {
    if (kind == "delete") {
        AlertDialog(onDismissRequest = { if (!busy) onDismiss() }, title = { Text(stringResource(R.string.batch_delete)) },
            text = { Column { Text(stringResource(R.string.batch_delete_description, records.size)); error?.let { Text(it, color = MaterialTheme.colorScheme.error) } } },
            dismissButton = { TextButton(onClick = onDismiss, enabled = !busy) { Text(stringResource(R.string.btn_cancel)) } },
            confirmButton = { TextButton(onClick = { onConfirm(BatchTransactionOperation.Delete) }, enabled = !busy, modifier = Modifier.testTag("HistoryBatchConfirmDelete")) { Text(stringResource(R.string.batch_delete)) } })
    } else {
        val latestBusy by rememberUpdatedState(busy)
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true, confirmValueChange = { !latestBusy || it != SheetValue.Hidden })
        com.example.vibefinance.ui.components.AppModalBottomSheet(onDismissRequest = { if (!busy) onDismiss() }, sheetState = sheetState) {
            Column(Modifier.fillMaxWidth().heightIn(max = 480.dp).verticalScroll(rememberScrollState()).padding(16.dp)) {
                Text(stringResource(if (kind == "category") R.string.edit_category_label else R.string.batch_account), style = MaterialTheme.typography.titleLarge)
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                if (kind == "account") state.accounts.forEach { account ->
                    TextButton(onClick = { onConfirm(BatchTransactionOperation.Account(account.id)) }, enabled = !busy, modifier = Modifier.fillMaxWidth().testTag("HistoryBatchAccount_${account.id}")) {
                        Text(account.nickname?.takeIf { it.isNotBlank() } ?: account.name)
                    }
                } else {
                    val type = if (records.firstOrNull()?.amount?.let { it < 0 } == true) CategoryKind.INCOME else CategoryKind.EXPENSE
                    categoryChoices(type, state.transactions, state.subscriptions, state.categoryLimits, state.categoryMergeRules).forEach { category ->
                        TextButton(onClick = { onConfirm(BatchTransactionOperation.Category(category)) }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text(category) }
                    }
                }
                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }
}
