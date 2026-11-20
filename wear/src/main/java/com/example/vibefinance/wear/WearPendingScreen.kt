package com.example.vibefinance.wear

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import java.util.UUID

private sealed interface PendingDialogState {
    data object None : PendingDialogState
    data class Success(val text: String, val closeAfter: Boolean) : PendingDialogState
    data class Failure(val text: String) : PendingDialogState
    data object OpenOnPhone : PendingDialogState
}

@Composable
internal fun WearPendingScreen(
    items: List<WearPendingItem>,
    fullMotion: Boolean = true,
    onSendCommand: suspend (WearCommand) -> WearCommandResult?,
    onOpenOnPhone: suspend (String) -> Boolean,
    onClose: () -> Unit
) {
    val listState = rememberTransformingLazyColumnState()
    val transformation = rememberTransformationSpec()
    val scope = rememberCoroutineScope()
    var index by remember(items.size) { mutableIntStateOf(0) }
    val item = items.getOrNull(index.coerceIn(0, (items.size - 1).coerceAtLeast(0)))

    if (item == null) {
        LaunchedEffect(Unit) { onClose() }
        return
    }

    var selectedAccIndex by remember(item.id) {
        val defaultIdx = item.candidateAccounts.indexOfFirst { it.id == item.suggestedAccountId }.coerceAtLeast(0)
        mutableIntStateOf(defaultIdx)
    }
    val selectedAccount = item.candidateAccounts.getOrNull(selectedAccIndex)

    var busy by remember(item.id) { mutableStateOf(false) }
    var dialogState by remember(item.id) { mutableStateOf<PendingDialogState>(PendingDialogState.None) }

    val typeLabel = when (item.transactionType) {
        "INCOME" -> stringResource(R.string.pending_type_income)
        "TRANSFER" -> stringResource(R.string.pending_type_transfer)
        "TOP_UP" -> stringResource(R.string.pending_type_topup)
        else -> stringResource(R.string.pending_type_expense)
    }

    val doneText = stringResource(R.string.pending_done)
    val alreadyText = stringResource(R.string.pending_already_handled)
    val failedText = stringResource(R.string.pending_failed)
    val openPhoneCurved = stringResource(R.string.opened_on_phone_curved)
    val primaryLabel = stringResource(
        if (item.canConfirmIncome) R.string.pending_confirm_income else R.string.pending_confirm
    )

    fun submit(command: WearCommand) {
        if (busy) return
        scope.launch {
            busy = true
            val result = onSendCommand(command)
            busy = false
            dialogState = when (result?.status) {
                WearCommandStatus.SUCCESS -> PendingDialogState.Success(doneText, closeAfter = items.size <= 1)
                WearCommandStatus.ALREADY_HANDLED -> PendingDialogState.Success(alreadyText, closeAfter = items.size <= 1)
                null -> PendingDialogState.Failure(failedText)
                else -> PendingDialogState.Failure(result.message.ifBlank { failedText })
            }
        }
    }

    val confirmPrimary: () -> Unit = {
        val acc = selectedAccount
        if (acc != null) {
            submit(
                WearCommand(
                    requestId = UUID.randomUUID().toString(),
                    issuedAt = System.currentTimeMillis(),
                    action = WearCommandAction.ACCEPT_PENDING,
                    paymentId = item.id,
                    accountId = acc.id,
                    rememberChoice = true,
                    overrideType = if (item.canConfirmIncome) "INCOME" else ""
                )
            )
        }
    }

    val pinchConfig = rememberOneHandedGestureConfiguration(
        action = OneHandedGestureAction.Primary,
        priority = OneHandedGesturePriority.Clickable
    )

    AppScaffold {
        ScreenScaffold(
            scrollState = listState,
            scrollIndicator = { ScrollIndicator(listState) },
            edgeButton = {
                if (selectedAccount != null) {
                    EdgeButton(
                        onClick = confirmPrimary,
                        enabled = !busy,
                        modifier = Modifier
                            .testTag(if (item.canConfirmIncome) "ConfirmIncomeBtn" else "ConfirmExpenseBtn")
                            .then(
                                if (!busy) {
                                    Modifier.oneHandedGesture(
                                        gestureConfiguration = pinchConfig,
                                        onGestureLabel = primaryLabel,
                                        onGesture = { confirmPrimary() }
                                    )
                                } else Modifier
                            )
                    ) {
                        Text(
                            text = if (busy) stringResource(R.string.pending_waiting_phone) else primaryLabel,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        ) { padding ->
            TransformingLazyColumn(
                state = listState,
                contentPadding = padding,
                modifier = Modifier.fillMaxSize().testTag("PendingList")
            ) {
                item {
                    ListHeader(
                        modifier = Modifier.then(if (fullMotion) Modifier.transformedHeight(this, transformation) else Modifier),
                        transformation = if (fullMotion) SurfaceTransformation(transformation) else null
                    ) {
                        Text(stringResource(R.string.pending_screen_title, items.size))
                    }
                }

                item {
                    TitleCard(
                        onClick = {},
                        enabled = false,
                        title = {
                            Text(
                                text = item.merchant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        subtitle = {
                            Text(
                                text = "$typeLabel · ${item.sourceLabel}",
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (fullMotion) Modifier.transformedHeight(this, transformation) else Modifier),
                        transformation = if (fullMotion) SurfaceTransformation(transformation) else null
                    ) {
                        Spacer(Modifier.height(4.dp))
                        FullAmount(
                            amount = item.amountText,
                            tag = "PendingAmount",
                            baseStyle = MaterialTheme.typography.numeralMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (selectedAccount != null) {
                    item {
                        FilledTonalButton(
                            onClick = {
                                if (item.candidateAccounts.size > 1) {
                                    selectedAccIndex = (selectedAccIndex + 1) % item.candidateAccounts.size
                                }
                            },
                            enabled = !busy && item.candidateAccounts.size > 1,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("PendingAccountCycle")
                                .then(if (fullMotion) Modifier.transformedHeight(this, transformation) else Modifier),
                            transformation = if (fullMotion) SurfaceTransformation(transformation) else null
                        ) {
                            val accLabel = if (selectedAccount.maskedLast4.isNotBlank()) {
                                "${selectedAccount.name} ••${selectedAccount.maskedLast4}"
                            } else selectedAccount.name
                            Text(
                                text = stringResource(R.string.pending_account_label, accLabel),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    if (item.canConfirmTransfer) {
                        item {
                            FilledTonalButton(
                                onClick = {
                                    submit(
                                        WearCommand(
                                            requestId = UUID.randomUUID().toString(),
                                            issuedAt = System.currentTimeMillis(),
                                            action = WearCommandAction.ACCEPT_PENDING,
                                            paymentId = item.id,
                                            accountId = selectedAccount.id,
                                            rememberChoice = true,
                                            overrideType = "TRANSFER"
                                        )
                                    )
                                },
                                enabled = !busy,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ConfirmTransferBtn")
                                    .then(if (fullMotion) Modifier.transformedHeight(this, transformation) else Modifier),
                                transformation = if (fullMotion) SurfaceTransformation(transformation) else null
                            ) {
                                Text(
                                    stringResource(R.string.pending_confirm_transfer),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // M3E spring-expanding ButtonGroup for Ignore & Open on Phone
                item {
                    val ignoreInteraction = remember { MutableInteractionSource() }
                    val phoneInteraction = remember { MutableInteractionSource() }
                    ButtonGroup(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (fullMotion) Modifier.transformedHeight(this, transformation) else Modifier),
                        transformation = if (fullMotion) SurfaceTransformation(transformation) else null
                    ) {
                        OutlinedButton(
                            onClick = {
                                submit(
                                    WearCommand(
                                        requestId = UUID.randomUUID().toString(),
                                        issuedAt = System.currentTimeMillis(),
                                        action = WearCommandAction.IGNORE_PENDING,
                                        paymentId = item.id
                                    )
                                )
                            },
                            enabled = !busy,
                            interactionSource = ignoreInteraction,
                            modifier = Modifier
                                .weight(1f)
                                .animateWidth(ignoreInteraction)
                                .testTag("IgnorePendingBtn")
                        ) {
                            Text(
                                text = stringResource(R.string.pending_ignore),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        FilledTonalButton(
                            onClick = {
                                scope.launch {
                                    dialogState = PendingDialogState.OpenOnPhone
                                    onOpenOnPhone("home")
                                }
                            },
                            enabled = !busy,
                            interactionSource = phoneInteraction,
                            modifier = Modifier
                                .weight(1f)
                                .animateWidth(phoneInteraction)
                                .testTag("PendingOpenPhoneBtn")
                        ) {
                            Text(
                                text = stringResource(R.string.pending_open_phone),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                item {
                    FilledTonalButton(
                        onClick = onClose,
                        enabled = !busy,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("PendingLaterBtn")
                            .then(if (fullMotion) Modifier.transformedHeight(this, transformation) else Modifier),
                        transformation = if (fullMotion) SurfaceTransformation(transformation) else null
                    ) {
                        Text(
                            stringResource(R.string.pending_later),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    val curvedStyle = ConfirmationDialogDefaults.curvedTextStyle
    val phoneCurvedStyle = OpenOnPhoneDialogDefaults.curvedTextStyle

    when (val dlg = dialogState) {
        is PendingDialogState.Success -> {
            SuccessConfirmationDialog(
                visible = true,
                onDismissRequest = {
                    dialogState = PendingDialogState.None
                    if (dlg.closeAfter) onClose()
                },
                curvedText = { confirmationDialogCurvedText(dlg.text, curvedStyle) }
            )
        }
        is PendingDialogState.Failure -> {
            FailureConfirmationDialog(
                visible = true,
                onDismissRequest = { dialogState = PendingDialogState.None },
                curvedText = { confirmationDialogCurvedText(dlg.text, curvedStyle) }
            )
        }
        PendingDialogState.OpenOnPhone -> {
            OpenOnPhoneDialog(
                visible = true,
                onDismissRequest = { dialogState = PendingDialogState.None },
                curvedText = { openOnPhoneDialogCurvedText(openPhoneCurved, phoneCurvedStyle) }
            )
        }
        PendingDialogState.None -> Unit
    }
}
