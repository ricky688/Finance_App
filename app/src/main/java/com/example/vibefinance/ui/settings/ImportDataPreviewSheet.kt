@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.ui.common.pressBounce
import com.example.vibefinance.util.FinancialDataImportEngine
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportDataPreviewSheet(
    preview: FinancialDataImportEngine.ImportDataPreview,
    isImporting: Boolean,
    onConfirm: (replaceExisting: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var replaceExisting by remember { mutableStateOf(false) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.getDefault()) }
    val dateRangeText = remember(preview.startDateMillis, preview.endDateMillis) {
        val start = preview.startDateMillis?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).format(dateFormatter)
        }
        val end = preview.endDateMillis?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).format(dateFormatter)
        }
        if (start != null && end != null) {
            "$start — $end"
        } else ""
    }

    ModalBottomSheet(
        onDismissRequest = { if (!isImporting) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- HEADER ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.import_preview_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = preview.fileName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(36.dp)
                ) {
                    val closeInteraction = remember { MutableInteractionSource() }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .clickable(
                                enabled = !isImporting,
                                interactionSource = closeInteraction,
                                indication = null
                            ) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onDismiss()
                            }
                            .pressBounce(shape = CircleShape, interactionSource = closeInteraction)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Summary Subtitle Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                ) {
                    Text(
                        text = stringResource(R.string.import_transactions_total, preview.rawTransactions.size),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (dateRangeText.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = dateRangeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // --- 3-COLUMN BENTO METRICS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Expense
                MetricBentoCard(
                    title = stringResource(R.string.import_expenses_count, preview.totalExpensesCount),
                    amount = String.format(Locale.US, "$%,.0f", preview.totalExpensesSum),
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
                    contentColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )

                // Income
                MetricBentoCard(
                    title = stringResource(R.string.import_income_count, preview.totalIncomeCount),
                    amount = String.format(Locale.US, "$%,.0f", preview.totalIncomeSum),
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                // Transfers
                MetricBentoCard(
                    title = stringResource(R.string.import_transfers_count, preview.totalTransfersCount),
                    amount = String.format(Locale.US, "$%,.0f", preview.totalTransfersSum),
                    icon = Icons.Default.SwapHoriz,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
                    contentColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }

            // --- DETECTED ACCOUNTS SECTION ---
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.import_detected_accounts, preview.detectedAccounts.size),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    preview.detectedAccounts.forEachIndexed { index, account ->
                        if (index > 0) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Account Type Icon & Name
                            val typeIcon = when (account.detectedType) {
                                AccountType.CASH -> Icons.Default.Payments
                                AccountType.BANK -> Icons.Default.AccountBalance
                                AccountType.DEBIT -> Icons.Default.CreditCard
                                AccountType.CC -> Icons.Default.CreditCard
                            }
                            val typeColor = when (account.detectedType) {
                                AccountType.CASH -> MaterialTheme.colorScheme.tertiary
                                AccountType.BANK -> MaterialTheme.colorScheme.primary
                                AccountType.DEBIT -> MaterialTheme.colorScheme.secondary
                                AccountType.CC -> MaterialTheme.colorScheme.error
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = typeColor.copy(alpha = 0.15f),
                                modifier = Modifier.size(30.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = typeIcon,
                                        contentDescription = null,
                                        tint = typeColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = account.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${account.detectedType.name} · ${account.transactionCount} records",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Calculated net balance
                            Text(
                                text = String.format(Locale.US, "$%,.2f", account.calculatedNetBalance),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (account.calculatedNetBalance < 0) MaterialTheme.colorScheme.error
                                       else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // --- IMPORT STRATEGY (MERGE vs REPLACE) ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.import_mode_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Option 1: Merge
                val mergeInteraction = remember { MutableInteractionSource() }
                val mergeBorderColor by animateColorAsState(
                    targetValue = if (!replaceExisting) MaterialTheme.colorScheme.primary
                                  else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "mergeBorder"
                )
                val mergeContainerColor by animateColorAsState(
                    targetValue = if (!replaceExisting) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                  else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "mergeContainer"
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = mergeContainerColor,
                    border = BorderStroke(if (!replaceExisting) 2.dp else 1.dp, mergeBorderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(
                            enabled = !isImporting,
                            interactionSource = mergeInteraction,
                            indication = null
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            replaceExisting = false
                        }
                        .pressBounce(shape = RoundedCornerShape(16.dp), interactionSource = mergeInteraction)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !replaceExisting,
                            onClick = {
                                if (!isImporting) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    replaceExisting = false
                                }
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.import_mode_merge),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.import_mode_merge_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Option 2: Replace
                val replaceInteraction = remember { MutableInteractionSource() }
                val replaceBorderColor by animateColorAsState(
                    targetValue = if (replaceExisting) MaterialTheme.colorScheme.error
                                  else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "replaceBorder"
                )
                val replaceContainerColor by animateColorAsState(
                    targetValue = if (replaceExisting) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                                  else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "replaceContainer"
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = replaceContainerColor,
                    border = BorderStroke(if (replaceExisting) 2.dp else 1.dp, replaceBorderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(
                            enabled = !isImporting,
                            interactionSource = replaceInteraction,
                            indication = null
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            replaceExisting = true
                        }
                        .pressBounce(shape = RoundedCornerShape(16.dp), interactionSource = replaceInteraction)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = replaceExisting,
                            onClick = {
                                if (!isImporting) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    replaceExisting = true
                                }
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.error)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.import_mode_replace),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (replaceExisting) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = stringResource(R.string.import_mode_replace_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // --- ACTION BUTTONS ---
            val confirmInteraction = remember { MutableInteractionSource() }
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onConfirm(replaceExisting)
                },
                enabled = !isImporting && preview.rawTransactions.isNotEmpty(),
                shapes = ButtonDefaults.shapes(shape = CircleShape, pressedShape = RoundedCornerShape(percent = 32)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (replaceExisting) MaterialTheme.colorScheme.error
                                     else MaterialTheme.colorScheme.primary
                ),
                interactionSource = confirmInteraction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .pressBounce(interactionSource = confirmInteraction)
            ) {
                if (isImporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.import_btn_importing),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.import_btn_confirm),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            val cancelInteraction = remember { MutableInteractionSource() }
            TextButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onDismiss()
                },
                enabled = !isImporting,
                shapes = ButtonDefaults.shapes(shape = CircleShape, pressedShape = RoundedCornerShape(percent = 32)),
                interactionSource = cancelInteraction,
                modifier = Modifier
                    .fillMaxWidth()
                    .pressBounce(interactionSource = cancelInteraction)
            ) {
                Text(
                    text = stringResource(R.string.btn_cancel),
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MetricBentoCard(
    title: String,
    amount: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                fontWeight = FontWeight.Bold,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
