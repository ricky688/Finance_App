package com.example.vibefinance.ui.accounts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.data.entity.historyAmountFor
import com.example.vibefinance.util.CurrencyEngine

@Composable
internal fun AccountTransactionInspection(id: Long, transactions: List<TransactionEntity>, hideAmounts: Boolean) {
    val recent = remember(id, transactions) {
        transactions.filter { it.accountId == id || it.toAccountId == id }.sortedByDescending { it.timestamp }.take(20)
    }
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.adaptive_recent_transactions), style = MaterialTheme.typography.titleMedium)
            recent.forEach { tx ->
                Column {
                    Text(if (tx.description.isBlank()) com.example.vibefinance.ui.home.getCategoryDisplayName(tx.category) else tx.description)
                    BasicText(if (hideAmounts) "••••" else CurrencyEngine.formatSignedAmount(tx.historyAmountFor(id), prefix = "HK$"),
                        modifier = Modifier.fillMaxWidth(), maxLines = 1,
                        autoSize = TextAutoSize.StepBased(minFontSize = 12.sp, maxFontSize = 16.sp),
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface))
                }
                HorizontalDivider()
            }
        }
    }
}
