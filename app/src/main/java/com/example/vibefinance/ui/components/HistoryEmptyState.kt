package com.example.vibefinance.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.vibefinance.R

/** One compact explanation and a useful next action, shared by chart and list states. */
@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun HistoryEmptyState(
    message: String,
    modifier: Modifier = Modifier,
    hint: String? = null,
    onAddTransaction: (() -> Unit)? = null,
    onViewAllRecords: (() -> Unit)? = null
) {
    Column(modifier.fillMaxWidth().testTag("HistoryEmptyState"),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(message, Modifier.fillMaxWidth().testTag("HistoryEmptyMessage"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (hint != null) Text(hint, Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (onViewAllRecords != null || onAddTransaction != null) {
            val viewAll = onViewAllRecords != null
            CompletePressFilledTonalButton(
                onClick = { if (viewAll) onViewAllRecords?.invoke() else onAddTransaction?.invoke() },
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier.heightIn(min = 48.dp)
                    .testTag(if (viewAll) "HistoryEmptyViewAll" else "HistoryEmptyAdd")
            ) {
                Icon(if (viewAll) Icons.Default.History else Icons.Default.Add,
                    contentDescription = null, modifier = Modifier.size(18.dp))
                androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
                Text(stringResource(if (viewAll) R.string.analytics_view_all_records else R.string.analytics_add_transaction))
            }
        }
    }
}
