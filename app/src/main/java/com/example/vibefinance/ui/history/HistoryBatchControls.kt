package com.example.vibefinance.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DeleteOutline
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

/**
 * Multi-select actions in an M3 Expressive floating toolbar at the bottom of the History list:
 * close, the selected count, select all / none, then category, account and delete as icon buttons
 * (with their names as tooltips and spoken labels). A loading indicator replaces the count while
 * a batch change is running.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun HistoryBatchToolbar(count: Int, allSelected: Boolean, busy: Boolean, canCategory: Boolean, canAccount: Boolean,
    onClose: () -> Unit, onSelectAll: () -> Unit, onAction: (String) -> Unit) {
    HorizontalFloatingToolbar(
        expanded = true,
        modifier = Modifier.testTag("HistoryBatchToolbar"),
        colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors()
    ) {
        IconButton(onClick = onClose, enabled = !busy, modifier = Modifier.testTag("HistoryBatchClose")) {
            Icon(Icons.Default.Close, stringResource(R.string.batch_close))
        }
        Box(Modifier.align(androidx.compose.ui.Alignment.CenterVertically).widthIn(min = 56.dp).padding(horizontal = 4.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center) {
            if (busy) LoadingIndicator(Modifier.size(28.dp))
            else Text(stringResource(R.string.batch_selected, count), style = MaterialTheme.typography.titleSmall, maxLines = 1)
        }
        BatchIconAction(
            if (allSelected) Icons.Default.Deselect else Icons.Default.SelectAll,
            stringResource(if (allSelected) R.string.batch_select_none else R.string.batch_select_all),
            enabled = !busy, tag = "HistoryBatchSelectAll", onClick = onSelectAll
        )
        BatchIconAction(Icons.Default.Category, stringResource(R.string.edit_category_label),
            enabled = !busy && canCategory, tag = "HistoryBatchCategory") { onAction("category") }
        BatchIconAction(Icons.Default.AccountBalanceWallet, stringResource(R.string.batch_account),
            enabled = !busy && canAccount, tag = "HistoryBatchAccount") { onAction("account") }
        BatchIconAction(Icons.Default.DeleteOutline, stringResource(R.string.batch_delete),
            enabled = !busy && count > 0, tag = "HistoryBatchDelete", tint = MaterialTheme.colorScheme.error) { onAction("delete") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BatchIconAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean,
    tag: String,
    tint: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified,
    onClick: () -> Unit,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = { PlainTooltip { Text(label) } },
        state = rememberTooltipState()
    ) {
        IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.testTag(tag)) {
            Icon(icon, label, tint = if (enabled && tint != androidx.compose.ui.graphics.Color.Unspecified) tint else LocalContentColor.current)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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
                if (busy) {
                    val minimalMotion = com.example.vibefinance.ui.preferences.LocalExperience.current.motion ==
                        com.example.vibefinance.ui.preferences.MotionLevel.MINIMAL
                    LinearWavyProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        amplitude = if (minimalMotion) 0f else 1f
                    )
                }
                if (kind == "account") state.accounts.forEach { account ->
                    com.example.vibefinance.ui.components.CompletePressTextButton(
                        onClick = { onConfirm(BatchTransactionOperation.Account(account.id)) },
                        enabled = !busy,
                        shapes = com.example.vibefinance.ui.components.AppButtonDefaults.shapes(),
                        modifier = Modifier.fillMaxWidth().testTag("HistoryBatchAccount_${account.id}")
                    ) {
                        Text(account.nickname?.takeIf { it.isNotBlank() } ?: account.name)
                    }
                } else {
                    val type = if (records.firstOrNull()?.amount?.let { it < 0 } == true) CategoryKind.INCOME else CategoryKind.EXPENSE
                    categoryChoices(type, state.transactions, state.subscriptions, state.categoryLimits, state.categoryMergeRules).forEach { category ->
                        com.example.vibefinance.ui.components.CompletePressTextButton(
                            onClick = { onConfirm(BatchTransactionOperation.Category(category)) },
                            enabled = !busy,
                            shapes = com.example.vibefinance.ui.components.AppButtonDefaults.shapes(),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(category) }
                    }
                }
                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }
}
