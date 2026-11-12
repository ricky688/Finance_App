package com.example.vibefinance.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.data.entity.historyAmountFor
import com.example.vibefinance.ui.components.ExpenseEventIcon
import com.example.vibefinance.util.CurrencyEngine

@Composable
internal fun TransactionInspection(tx: TransactionEntity, hideAmounts: Boolean, top: Dp,
    onEdit: () -> Unit, onMoneyBack: () -> Unit, onUnlink: () -> Unit, onDelete: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        .padding(top = top, bottom = 96.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(shape = RoundedCornerShape(28.dp)) {
            Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                ExpenseEventIcon(customIcon = if (tx.isReimbursement) "icon:reimbursement" else tx.customIcon,
                    category = tx.category, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                BasicText(if (hideAmounts) "••••" else CurrencyEngine.formatSignedAmount(tx.historyAmountFor(), prefix = "HK$"),
                    modifier = Modifier.fillMaxWidth(), maxLines = 1,
                    autoSize = TextAutoSize.StepBased(minFontSize = 12.sp, maxFontSize = 28.sp),
                    style = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.onSurface))
                Text(com.example.vibefinance.ui.home.getCategoryDisplayName(tx.category), style = MaterialTheme.typography.titleLarge)
                Text(java.text.DateFormat.getDateTimeInstance().format(java.util.Date(tx.timestamp)))
                if (tx.description.isNotBlank()) Text(tx.description)
                if (tx.isReimbursement) Text(stringResource(R.string.reimbursement_title))
                if (tx.amount < 0 && tx.toAccountId == null && !tx.isBalanceAdjustment) {
                    TextButton(onClick = onMoneyBack) { Text(stringResource(R.string.reimbursement_choose_expense)) }
                    if (tx.reimbursementOf != null) TextButton(onClick = onUnlink) {
                        Text(stringResource(R.string.reimbursement_unlink))
                    }
                }
                // Reuse the complete editor for link/unlink, account selection and atomic validation.
                FilledTonalButton(onClick = onEdit) { Text(stringResource(R.string.edit_transaction_title)) }
                TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text(stringResource(R.string.btn_delete))
                }
            }
        }
    }
}
