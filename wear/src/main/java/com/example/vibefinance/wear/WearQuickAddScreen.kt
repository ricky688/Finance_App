package com.example.vibefinance.wear

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.material3.onehandedgesture.OneHandedGestureAction
import androidx.wear.compose.material3.onehandedgesture.OneHandedGesturePriority
import androidx.wear.compose.material3.onehandedgesture.oneHandedGesture
import androidx.wear.compose.material3.onehandedgesture.rememberOneHandedGestureConfiguration
import com.example.vibefinance.wear.contract.*
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

private enum class QuickAddStep { CATEGORY, AMOUNT, CONFIRM }

private sealed interface QuickAddDialogState {
    data object None : QuickAddDialogState
    data class Success(val text: String) : QuickAddDialogState
    data class Failure(val text: String) : QuickAddDialogState
}

@Composable
internal fun WearQuickAddScreen(
    categories: List<String>,
    accounts: List<WearAccountOption>,
    fullMotion: Boolean = true,
    onSendCommand: suspend (WearCommand) -> WearCommandResult?,
    onEnqueueOffline: (WearCommand) -> Unit,
    onClose: () -> Unit
) {
    val safeCategories = categories.ifEmpty {
        listOf("Food & Drink", "Transport", "Groceries", "Shopping", "Entertainment", "Utilities", "Other")
    }
    var step by remember { mutableStateOf(QuickAddStep.CATEGORY) }
    var selectedCategory by remember { mutableStateOf(safeCategories.first()) }
    var amountInput by remember { mutableStateOf("") }
    var selectedAccountIndex by remember { mutableIntStateOf(0) }
    var busy by remember { mutableStateOf(false) }
    var dialogState by remember { mutableStateOf<QuickAddDialogState>(QuickAddDialogState.None) }

    val scope = rememberCoroutineScope()
    val doneStr = stringResource(R.string.pending_done)
    val queuedStr = stringResource(R.string.quick_add_queued)
    val failedStr = stringResource(R.string.pending_failed)
    val saveLabel = stringResource(R.string.quick_add_save)
    val waitingStr = stringResource(R.string.pending_waiting_phone)

    val parsedAmount = amountInput.toDoubleOrNull()?.takeIf { it.isFinite() && it > 0.0 }
    val selectedAccount = accounts.getOrNull(selectedAccountIndex.coerceIn(0, (accounts.size - 1).coerceAtLeast(0)))

    val saveExpense: () -> Unit = {
        val amt = parsedAmount
        val acc = selectedAccount
        if (amt != null && acc != null && !busy) {
            val cmd = WearCommand(
                requestId = UUID.randomUUID().toString(),
                issuedAt = System.currentTimeMillis(),
                action = WearCommandAction.QUICK_ADD_EXPENSE,
                accountId = acc.id,
                category = selectedCategory,
                amount = amt,
                note = selectedCategory
            )
            scope.launch {
                busy = true
                val res = onSendCommand(cmd)
                busy = false
                dialogState = when (res?.status) {
                    WearCommandStatus.SUCCESS, WearCommandStatus.ALREADY_HANDLED ->
                        QuickAddDialogState.Success(doneStr)
                    null -> {
                        onEnqueueOffline(cmd)
                        QuickAddDialogState.Failure(queuedStr)
                    }
                    else -> QuickAddDialogState.Failure(res.message.ifBlank { failedStr })
                }
            }
        }
    }

    when (step) {
        QuickAddStep.CATEGORY -> {
            val listState = rememberTransformingLazyColumnState()
            val transformation = rememberTransformationSpec()
            AppScaffold {
                ScreenScaffold(
                    scrollState = listState,
                    scrollIndicator = { ScrollIndicator(listState) },
                    edgeButton = {
                        EdgeButton(
                            onClick = onClose,
                            colors = ButtonDefaults.filledTonalButtonColors()
                        ) {
                            Text(stringResource(R.string.quick_add_back), maxLines = 1)
                        }
                    }
                ) { padding ->
                    TransformingLazyColumn(
                        state = listState,
                        contentPadding = padding,
                        modifier = Modifier.fillMaxSize().testTag("QuickCategoryList")
                    ) {
                        item {
                            ListHeader(
                                modifier = Modifier.then(if (fullMotion) Modifier.transformedHeight(this, transformation) else Modifier),
                                transformation = if (fullMotion) SurfaceTransformation(transformation) else null
                            ) {
                                Text(stringResource(R.string.quick_add_category_title))
                            }
                        }
                        items(safeCategories.size) { i ->
                            val cat = safeCategories[i]
                            FilledTonalButton(
                                onClick = {
                                    selectedCategory = cat
                                    step = QuickAddStep.AMOUNT
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("QuickCat_$i")
                                    .then(if (fullMotion) Modifier.transformedHeight(this, transformation) else Modifier),
                                transformation = if (fullMotion) SurfaceTransformation(transformation) else null
                            ) {
                                Text(
                                    cat,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }

        QuickAddStep.AMOUNT -> {
            val listState = rememberTransformingLazyColumnState()
            val transformation = rememberTransformationSpec()
            val displayText = if (amountInput.isBlank()) "HK$ 0" else "HK$ $amountInput"
            fun appendChar(c: Char) {
                if (c == '.' && (amountInput.contains('.') || amountInput.isEmpty())) return
                val parts = amountInput.split('.')
                if (parts.size == 2 && parts[1].length >= 2) return
                if (amountInput.length >= 9) return
                amountInput += c
            }

            AppScaffold {
                ScreenScaffold(
                    scrollState = listState,
                    scrollIndicator = { ScrollIndicator(listState) },
                    edgeButton = {
                        EdgeButton(
                            onClick = { step = QuickAddStep.CONFIRM },
                            enabled = parsedAmount != null,
                            modifier = Modifier.testTag("QuickAmountNext")
                        ) {
                            Text(stringResource(R.string.quick_add_next), maxLines = 1)
                        }
                    }
                ) { padding ->
                    TransformingLazyColumn(
                        state = listState,
                        contentPadding = padding,
                        modifier = Modifier.fillMaxSize().testTag("QuickAmountList")
                    ) {
                        item { ListHeader { Text(stringResource(R.string.quick_add_amount_title)) } }
                        item {
                            FullAmount(
                                amount = displayText,
                                tag = "QuickAmountDisplay",
                                baseStyle = MaterialTheme.typography.numeralMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        // M3E spring-expanding ButtonGroup for one-tap HKD presets
                        item {
                            ButtonGroup(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(if (fullMotion) Modifier.transformedHeight(this, transformation) else Modifier),
                                transformation = if (fullMotion) SurfaceTransformation(transformation) else null
                            ) {
                                listOf("20", "50", "100").forEach { preset ->
                                    val interaction = remember { MutableInteractionSource() }
                                    FilledTonalButton(
                                        onClick = {
                                            amountInput = preset
                                            step = QuickAddStep.CONFIRM
                                        },
                                        interactionSource = interaction,
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .minWidth(36.dp)
                                            .animateWidth(interaction)
                                            .testTag("Preset_$preset")
                                    ) {
                                        Text("$$preset", maxLines = 1, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                                    }
                                }
                            }
                        }
                        // M3E spring-expanding ButtonGroup rows for numeric keypad
                        val rows = listOf(
                            listOf("1", "2", "3"),
                            listOf("4", "5", "6"),
                            listOf("7", "8", "9"),
                            listOf(".", "0", "⌫")
                        )
                        items(rows.size) { rowIdx ->
                            ButtonGroup(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(if (fullMotion) Modifier.transformedHeight(this, transformation) else Modifier),
                                transformation = if (fullMotion) SurfaceTransformation(transformation) else null
                            ) {
                                rows[rowIdx].forEach { key ->
                                    val interaction = remember { MutableInteractionSource() }
                                    CompactButton(
                                        onClick = {
                                            if (key == "⌫") {
                                                if (amountInput.isNotEmpty()) amountInput = amountInput.dropLast(1)
                                            } else {
                                                appendChar(key[0])
                                            }
                                        },
                                        interactionSource = interaction,
                                        modifier = Modifier
                                            .weight(1f)
                                            .minWidth(36.dp)
                                            .animateWidth(interaction)
                                            .testTag("Key_$key")
                                    ) {
                                        Text(key, maxLines = 1)
                                    }
                                }
                            }
                        }
                        item {
                            OutlinedButton(
                                onClick = { step = QuickAddStep.CATEGORY },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.quick_add_back), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            }
        }

        QuickAddStep.CONFIRM -> {
            val listState = rememberTransformingLazyColumnState()
            val transformation = rememberTransformationSpec()
            val formattedAmount = String.format(Locale.US, "HK$ %,.2f", parsedAmount ?: 0.0)
            val pinchConfig = rememberOneHandedGestureConfiguration(
                action = OneHandedGestureAction.Primary,
                priority = OneHandedGesturePriority.Clickable
            )
            AppScaffold {
                ScreenScaffold(
                    scrollState = listState,
                    scrollIndicator = { ScrollIndicator(listState) },
                    edgeButton = {
                        EdgeButton(
                            onClick = saveExpense,
                            enabled = !busy && parsedAmount != null && selectedAccount != null,
                            modifier = Modifier
                                .testTag("QuickSaveBtn")
                                .then(
                                    if (!busy && parsedAmount != null && selectedAccount != null) {
                                        Modifier.oneHandedGesture(
                                            gestureConfiguration = pinchConfig,
                                            onGestureLabel = saveLabel,
                                            onGesture = { saveExpense() }
                                        )
                                    } else Modifier
                                )
                        ) {
                            Text(if (busy) waitingStr else saveLabel, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                ) { padding ->
                    TransformingLazyColumn(
                        state = listState,
                        contentPadding = padding,
                        modifier = Modifier.fillMaxSize().testTag("QuickConfirmList")
                    ) {
                        item { ListHeader { Text(stringResource(R.string.quick_add_confirm_title)) } }
                        item {
                            TitleCard(
                                onClick = {},
                                enabled = false,
                                title = { Text(selectedCategory, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(if (fullMotion) Modifier.transformedHeight(this, transformation) else Modifier),
                                transformation = if (fullMotion) SurfaceTransformation(transformation) else null
                            ) {
                                Spacer(Modifier.height(4.dp))
                                FullAmount(
                                    amount = formattedAmount,
                                    tag = "QuickConfirmAmount",
                                    baseStyle = MaterialTheme.typography.numeralMedium
                                )
                            }
                        }
                        if (selectedAccount != null) {
                            item {
                                FilledTonalButton(
                                    onClick = {
                                        if (accounts.size > 1) {
                                            selectedAccountIndex = (selectedAccountIndex + 1) % accounts.size
                                        }
                                    },
                                    enabled = !busy && accounts.size > 1,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("QuickAccountCycle")
                                        .then(if (fullMotion) Modifier.transformedHeight(this, transformation) else Modifier),
                                    transformation = if (fullMotion) SurfaceTransformation(transformation) else null
                                ) {
                                    Text(
                                        text = stringResource(R.string.pending_account_label, selectedAccount.name),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                        item {
                            OutlinedButton(
                                onClick = { step = QuickAddStep.AMOUNT },
                                enabled = !busy,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.quick_add_back), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            }
        }
    }

    val curvedStyle = ConfirmationDialogDefaults.curvedTextStyle
    when (val dlg = dialogState) {
        is QuickAddDialogState.Success -> {
            SuccessConfirmationDialog(
                visible = true,
                onDismissRequest = {
                    dialogState = QuickAddDialogState.None
                    onClose()
                },
                curvedText = { confirmationDialogCurvedText(dlg.text, curvedStyle) }
            )
        }
        is QuickAddDialogState.Failure -> {
            FailureConfirmationDialog(
                visible = true,
                onDismissRequest = { dialogState = QuickAddDialogState.None },
                curvedText = { confirmationDialogCurvedText(dlg.text, curvedStyle) }
            )
        }
        QuickAddDialogState.None -> Unit
    }
}
