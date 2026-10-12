package com.example.vibefinance.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.components.AppModalBottomSheet
import com.example.vibefinance.ui.history.HistoryCategoryDropdown

/** Only the category is edited; amount, account, transfer, metadata and balances stay intact. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NotificationCategorySheet(
    transaction: TransactionEntity,
    state: FinanceUiState,
    saving: Boolean,
    error: Boolean,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var category by rememberSaveable(transaction.id) { mutableStateOf(transaction.category) }
    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { !saving || it != SheetValue.Hidden },
        )
    AppModalBottomSheet(sheetState = sheetState, onDismissRequest = { if (!saving) onDismiss() }) {
        Column(
            Modifier.fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .testTag("NotificationCategorySheet"),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                stringResource(R.string.nf_action_change_category),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                transaction.description,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            HistoryCategoryDropdown(
                category,
                transaction.amount < 0.0,
                state.transactions,
                state.categoryMergeRules,
            ) {
                category = it
            }
            if (error)
                Text(
                    stringResource(R.string.nf_category_save_failed),
                    color = MaterialTheme.colorScheme.error,
                )
            Button(
                onClick = { onSave(category) },
                enabled = !saving,
                modifier =
                    Modifier.fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("NotificationCategorySave"),
            ) {
                if (saving) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text(stringResource(R.string.btn_save))
            }
        }
    }
}
