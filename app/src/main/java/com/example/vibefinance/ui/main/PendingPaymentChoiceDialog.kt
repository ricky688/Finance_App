package com.example.vibefinance.ui.main

import com.example.vibefinance.ui.components.CompletePressButton

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.service.PendingPayment
import com.example.vibefinance.service.PendingPaymentStore
import com.example.vibefinance.ui.common.bouncyClickable
import java.util.Locale

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun PendingPaymentChoiceDialog(
    payment: PendingPayment,
    accounts: List<AccountEntity>,
    saving: Boolean,
    onRecord: (accountId: Long, rememberChoice: Boolean) -> Unit,
    onLater: () -> Unit,
    onIgnore: () -> Unit
) {
    val context = LocalContext.current
    val appLabel = remember(payment.sourcePackage) {
        runCatching {
            val appInfo = context.packageManager.getApplicationInfo(payment.sourcePackage, 0)
            context.packageManager.getApplicationLabel(appInfo).toString()
        }.getOrDefault(payment.sourcePackage)
    }
    var currentPayment by remember(payment.id) { mutableStateOf(payment) }
    var showTaggingDialog by remember(payment.id) { mutableStateOf(false) }
    val suggestedId = remember(payment.id, currentPayment.cardLast4, accounts) {
        PendingPaymentStore.findMatchingAccount(currentPayment, accounts)?.id
    }
    var selectedId by remember(payment.id, suggestedId) { mutableStateOf(suggestedId) }
    var rememberChoice by remember(payment.id) { mutableStateOf(true) }
    val sortedAccounts = remember(accounts) {
        accounts.sortedWith(compareBy<AccountEntity>({
            when (it.type) {
                AccountType.CASH -> 0
                AccountType.BANK -> 1
                AccountType.DEBIT -> 2
                AccountType.CC -> 3
            }
        }, { it.name.lowercase(Locale.getDefault()) }))
    }
    val selectedAccount = sortedAccounts.firstOrNull { it.id == selectedId }
    val canRemember = selectedAccount?.let {
        PendingPaymentStore.canRememberChoice(currentPayment, it) ||
            !currentPayment.cardLast4.isNullOrBlank() ||
            (!currentPayment.assetHint.isBlank() && !PendingPaymentStore.isGenericHint(currentPayment.assetHint))
    } == true

    AlertDialog(
        onDismissRequest = { if (!saving) onLater() },
        shape = RoundedCornerShape(28.dp),
        icon = { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null) },
        title = {
            Text(
                text = stringResource(R.string.pending_payment_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.pending_payment_intro, appLabel),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        val isIncome = currentPayment.isTopUp || currentPayment.transactionType == "INCOME"
                        val amountPrefix = if (isIncome) "+HK$" else "HK$"
                        Text(
                            text = "$amountPrefix${String.format(Locale.getDefault(), "%,.2f", currentPayment.amount)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = currentPayment.merchant,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (currentPayment.assetHint.isNotBlank() &&
                            !currentPayment.assetHint.equals(currentPayment.merchant, ignoreCase = true)
                        ) {
                            Text(
                                text = stringResource(R.string.pending_payment_detected_hint, currentPayment.assetHint),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.75f)
                            )
                        }
                        val last4 = currentPayment.cardLast4
                        if (!last4.isNullOrBlank()) {
                            Text(
                                text = stringResource(R.string.ui_pending_card_last_four, last4),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        if (currentPayment.balanceRemaining != null) {
                            Text(
                                text = stringResource(
                                    R.string.pending_payment_balance_remaining,
                                    "HK$${String.format(Locale.getDefault(), "%,.2f", currentPayment.balanceRemaining)}"
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }

                // Button to open Visual Slot Tagging dialog
                androidx.compose.material3.FilledTonalButton(
                    onClick = { showTaggingDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.vst_open_button),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = stringResource(R.string.pending_payment_choose_account),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (sortedAccounts.isEmpty()) {
                    Text(
                        text = stringResource(R.string.pending_payment_no_accounts),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(sortedAccounts, key = { it.id }) { account ->
                            val selected = selectedId == account.id
                            val rowShape = RoundedCornerShape(18.dp)
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bouncyClickable(enabled = !saving, shape = rowShape) {
                                        selectedId = account.id
                                    },
                                shape = rowShape,
                                color = if (selected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceContainerLow
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = selected, onClick = null)
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = account.nickname?.takeIf { it.isNotBlank() } ?: account.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        if (!account.nickname.isNullOrBlank() && account.nickname != account.name) {
                                            Text(
                                                text = account.name,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        val typeLabel = when (account.type) {
                                            AccountType.CASH -> stringResource(R.string.pending_payment_account_cash)
                                            AccountType.BANK -> stringResource(R.string.pending_payment_account_bank)
                                            AccountType.DEBIT -> stringResource(R.string.pending_payment_account_debit)
                                            AccountType.CC -> stringResource(R.string.pending_payment_account_card)
                                        }
                                        val cardSuffix = account.cardLast4?.let { " •••• $it" } ?: ""
                                        val isMatchedCard = !payment.cardLast4.isNullOrBlank() && account.cardLast4 == payment.cardLast4
                                        Text(
                                            text = if (isMatchedCard) stringResource(R.string.ui_pending_matching_card, "$typeLabel$cardSuffix") else "$typeLabel$cardSuffix",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isMatchedCard) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isMatchedCard) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (canRemember) {
                        val displayHint = if (!payment.cardLast4.isNullOrBlank()) {
                            "${payment.assetHint} (•••• ${payment.cardLast4})"
                        } else {
                            payment.assetHint
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !saving) { rememberChoice = !rememberChoice },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = rememberChoice, onCheckedChange = null)
                            Text(
                                text = stringResource(
                                    R.string.pending_payment_remember_choice,
                                    appLabel,
                                    displayHint
                                ),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.pending_payment_remember_unavailable),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            CompletePressButton(
                enabled = selectedId != null && !saving,
                onClick = { selectedId?.let { onRecord(it, rememberChoice && canRemember) } },
                shapes = ButtonDefaults.shapes(
                    shape = RoundedCornerShape(26.dp),
                    pressedShape = RoundedCornerShape(16.dp)
                )
            ) {
                Text(stringResource(R.string.pending_payment_record))
            }
        },
        dismissButton = {
            Row {
                TextButton(enabled = !saving, onClick = onIgnore) {
                    Text(stringResource(R.string.pending_payment_ignore))
                }
                TextButton(enabled = !saving, onClick = onLater) {
                    Text(stringResource(R.string.pending_payment_later))
                }
            }
        }
    )

    if (showTaggingDialog) {
        VisualSlotTaggingDialog(
            payment = currentPayment,
            onDismiss = { showTaggingDialog = false },
            onSaveAndApply = { template, matchResult ->
                com.example.vibefinance.data.InMemoryDatabase.saveNotificationTemplate(template)
                val updated = currentPayment.copy(
                    amount = matchResult.amount,
                    merchant = matchResult.merchant,
                    cardLast4 = matchResult.cardLast4 ?: currentPayment.cardLast4,
                    transactionType = matchResult.transactionType.name
                )
                currentPayment = updated
                PendingPaymentStore.updatePayment(payment.id, updated)
                showTaggingDialog = false
            }
        )
    }
}
