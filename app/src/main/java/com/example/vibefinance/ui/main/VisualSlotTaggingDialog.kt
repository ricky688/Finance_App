@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.vibefinance.ui.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.NotificationTemplate
import com.example.vibefinance.data.entity.ParsedTransactionType
import com.example.vibefinance.data.entity.SlotToken
import com.example.vibefinance.data.entity.SlotType
import com.example.vibefinance.data.entity.TemplateMatchResult
import com.example.vibefinance.service.PendingPayment
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.TextButton
import java.util.Locale

@Composable
fun VisualSlotTaggingDialog(
    payment: PendingPayment,
    onDismiss: () -> Unit,
    onSaveAndApply: (template: NotificationTemplate, matchResult: TemplateMatchResult) -> Unit
) {
    val fullRawText = remember(payment.rawTitle, payment.rawText, payment.merchant) {
        val raw = "${payment.rawTitle} ${payment.rawText}".trim()
        if (raw.isNotBlank()) raw else "${payment.assetHint} ${payment.merchant} ${payment.amount}"
    }

    // Split text into token pieces
    val initialTokens = remember(fullRawText) {
        val rawParts = fullRawText.split(Regex("(?<=[，,。：:\\s])|(?=[，,。：:\\s])"))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        rawParts.mapIndexed { index, part ->
            val slot = when {
                part.matches(Regex(".*\\d+(?:\\.\\d{1,2})?.*")) && (part.contains("$") || part.contains("HK") || part.toDoubleOrNull() != null) ->
                    SlotType.AMOUNT
                part.matches(Regex(".*\\d{4}.*")) && (part.contains("•") || part.contains("*") || part.contains("卡") || part.length == 4) ->
                    SlotType.CARD_LAST4
                else -> SlotType.NONE
            }
            SlotToken(index = index, text = part, slot = slot)
        }
    }

    val tokens = remember { mutableStateListOf<SlotToken>().apply { addAll(initialTokens) } }
    var selectedTokenIndex by remember { mutableStateOf<Int?>(null) }
    var transactionType by remember {
        mutableStateOf(
            runCatching { ParsedTransactionType.valueOf(payment.transactionType) }.getOrDefault(ParsedTransactionType.EXPENSE)
        )
    }

    // Real-time preview extraction
    val previewResult by remember {
        derivedStateOf {
            val amountToken = tokens.firstOrNull { it.slot == SlotType.AMOUNT }?.text
            val merchantTokens = tokens.filter { it.slot == SlotType.MERCHANT }.map { it.text }
            val cardToken = tokens.firstOrNull { it.slot == SlotType.CARD_LAST4 }?.text

            val extractedAmount = amountToken?.let {
                Regex("[0-9]+(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?").find(it)?.value?.replace(",", "")?.toDoubleOrNull()
            } ?: payment.amount

            val extractedMerchant = if (merchantTokens.isNotEmpty()) {
                merchantTokens.joinToString(" ")
            } else payment.merchant

            val extractedCard = cardToken?.let {
                Regex("\\d{4}").find(it)?.value
            } ?: payment.cardLast4

            TemplateMatchResult(
                amount = extractedAmount,
                merchant = extractedMerchant,
                cardLast4 = extractedCard,
                transactionType = transactionType
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        icon = {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = stringResource(R.string.vst_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.vst_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 1. Interactive Token Chips Container
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            text = stringResource(R.string.vst_tap_tokens_hint),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            tokens.forEach { token ->
                                val isSelected = selectedTokenIndex == token.index
                                val (bg, fg, label) = when (token.slot) {
                                    SlotType.AMOUNT -> Triple(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer, "💰")
                                    SlotType.MERCHANT -> Triple(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, "🏪")
                                    SlotType.CARD_LAST4 -> Triple(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer, "💳")
                                    SlotType.TYPE -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, "🔄")
                                    SlotType.NONE -> Triple(
                                        if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                                        MaterialTheme.colorScheme.onSurface,
                                        ""
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = bg,
                                    border = BorderStroke(
                                        if (isSelected) 2.dp else 1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier.clickable {
                                        selectedTokenIndex = if (selectedTokenIndex == token.index) null else token.index
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (label.isNotBlank()) {
                                            Text(text = label, fontSize = 11.sp)
                                            Spacer(Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = token.text,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (token.slot != SlotType.NONE) FontWeight.Bold else FontWeight.Normal,
                                            color = fg
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Slot Action Assigners
                if (selectedTokenIndex != null) {
                    val activeToken = tokens.getOrNull(selectedTokenIndex!!)
                    if (activeToken != null) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(
                                    text = stringResource(R.string.vst_assign_slot_to, activeToken.text),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    FilledTonalButton(
                                        onClick = {
                                            tokens[activeToken.index] = activeToken.copy(slot = SlotType.AMOUNT)
                                            selectedTokenIndex = null
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("💰 金額", style = MaterialTheme.typography.labelSmall)
                                    }
                                    FilledTonalButton(
                                        onClick = {
                                            tokens[activeToken.index] = activeToken.copy(slot = SlotType.MERCHANT)
                                            selectedTokenIndex = null
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("🏪 商戶", style = MaterialTheme.typography.labelSmall)
                                    }
                                    FilledTonalButton(
                                        onClick = {
                                            tokens[activeToken.index] = activeToken.copy(slot = SlotType.CARD_LAST4)
                                            selectedTokenIndex = null
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("💳 卡號", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                if (activeToken.slot != SlotType.NONE) {
                                    Spacer(Modifier.height(6.dp))
                                    TextButton(
                                        onClick = {
                                            tokens[activeToken.index] = activeToken.copy(slot = SlotType.NONE)
                                            selectedTokenIndex = null
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(stringResource(R.string.vst_clear_slot), style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Transaction Direction
                Text(
                    text = stringResource(R.string.vst_transaction_direction),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = transactionType == ParsedTransactionType.EXPENSE,
                        onClick = { transactionType = ParsedTransactionType.EXPENSE },
                        label = { Text("支出") },
                        leadingIcon = if (transactionType == ParsedTransactionType.EXPENSE) {
                            { Icon(Icons.Filled.Check, contentDescription = null, Modifier.size(16.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = transactionType == ParsedTransactionType.INCOME,
                        onClick = { transactionType = ParsedTransactionType.INCOME },
                        label = { Text("薪金 / 收入") },
                        leadingIcon = if (transactionType == ParsedTransactionType.INCOME) {
                            { Icon(Icons.Filled.Check, contentDescription = null, Modifier.size(16.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = transactionType == ParsedTransactionType.REPAYMENT,
                        onClick = { transactionType = ParsedTransactionType.REPAYMENT },
                        label = { Text("信用卡還款") },
                        leadingIcon = if (transactionType == ParsedTransactionType.REPAYMENT) {
                            { Icon(Icons.Filled.Check, contentDescription = null, Modifier.size(16.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                }

                // 4. Live Preview Card
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            text = stringResource(R.string.vst_live_preview),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(Modifier.height(4.dp))
                        val sign = if (transactionType == ParsedTransactionType.INCOME) "+HK$" else "HK$"
                        Text(
                            text = "$sign${String.format(Locale.getDefault(), "%,.2f", previewResult.amount)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = previewResult.merchant,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!previewResult.cardLast4.isNullOrBlank()) {
                            Text(
                                text = "卡號尾數：•••• ${previewResult.cardLast4}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val template = NotificationTemplate.compileFromTokens(
                        tokens = tokens,
                        originalText = fullRawText,
                        name = "${payment.assetHint.ifBlank { "自訂銀行" }} 模板",
                        sourcePackage = payment.sourcePackage,
                        transactionType = transactionType
                    )
                    onSaveAndApply(template, previewResult)
                }
            ) {
                Text(stringResource(R.string.vst_save_and_apply), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}
