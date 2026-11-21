package com.example.vibefinance.ui.recurring

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.vibefinance.data.currency.ExchangeRateLoader
import com.example.vibefinance.data.entity.SubscriptionEntity
import com.example.vibefinance.ui.FinanceIntent
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.accounts.assetEditorFadeSpec
import com.example.vibefinance.ui.accounts.assetEditorScaleSpec
import com.example.vibefinance.ui.accounts.assetEditorSlideSpec
import java.util.UUID

@Stable
class SubscriptionEditorState {
    var isOpen by mutableStateOf(false)
        internal set

    var sessionId by mutableStateOf(UUID.randomUUID().toString())
        internal set

    var subscription by mutableStateOf<SubscriptionEntity?>(null)
        internal set

    var subscriptionId by mutableStateOf<Long?>(null)
        internal set

    var initialPreset by mutableStateOf<SubscriptionPreset?>(null)
        internal set

    var isTransitionActive by mutableStateOf(false)
        internal set

    fun open(
        sub: SubscriptionEntity? = null,
        preset: SubscriptionPreset? = null,
    ) {
        subscription = sub
        subscriptionId = sub?.id
        initialPreset = preset
        sessionId = UUID.randomUUID().toString()
        isOpen = true
    }

    fun close() {
        isOpen = false
    }

    companion object {
        val Saver =
            listSaver<SubscriptionEditorState, Any>(
                save = {
                    listOf(
                        it.isOpen,
                        it.sessionId,
                        it.subscriptionId ?: -1L,
                        it.initialPreset?.name.orEmpty(),
                    )
                },
                restore = { list ->
                    SubscriptionEditorState().apply {
                        isOpen = list[0] as Boolean
                        sessionId = list[1] as String
                        val subId = (list[2] as Number).toLong()
                        subscriptionId = subId.takeIf { it != -1L }
                        val presetName = list[3] as String
                        initialPreset =
                            presetName.takeIf { it.isNotEmpty() }?.let { name ->
                                POPULAR_SUBSCRIPTION_PRESETS.firstOrNull { it.name == name }
                            }
                    }
                },
            )
    }
}

@Composable
fun rememberSubscriptionEditorState(): SubscriptionEditorState =
    rememberSaveable(saver = SubscriptionEditorState.Saver) { SubscriptionEditorState() }

/** Right-edge slide-over host for Add/Edit Subscription & Installment Plans (full-screen or beside rail). */
@Composable
fun SubscriptionEntryHost(
    visible: Boolean,
    state: FinanceUiState,
    editorState: SubscriptionEditorState,
    onIntent: (FinanceIntent) -> Unit,
    modifier: Modifier = Modifier,
    exchangeRateLoader: ExchangeRateLoader? = null,
    onDismiss: () -> Unit = { editorState.close() },
    transition: Transition<Boolean> = updateTransition(visible, label = "Subscription editor"),
    isWideWorkspace: Boolean = false,
) {
    var pendingDeleteSub by remember { mutableStateOf<SubscriptionEntity?>(null) }
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = LocalFocusManager.current
    val keyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val dismiss = {
        keyboard?.hide()
        focus.clearFocus()
        onDismiss()
    }

    BackHandler(enabled = visible) {
        if (keyboardVisible) {
            keyboard?.hide()
            focus.clearFocus()
        } else {
            dismiss()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (isWideWorkspace) {
            val scrimAlpha by transition.animateFloat(
                transitionSpec = { tween(240) },
                label = "SubWorkspaceScrimAlpha",
            ) { open -> if (open) 0.16f else 0f }

            if (scrimAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = scrimAlpha))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = dismiss,
                        )
                        .testTag("SubscriptionWorkspaceScrim")
                )
            }
        }

        val workspaceScale = if (isWideWorkspace) 1.03f else 1.15f
        val workspaceShape = if (isWideWorkspace) RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp) else RectangleShape
        val workspaceBorder = if (isWideWorkspace) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        } else {
            null
        }

        transition.AnimatedVisibility(
            visible = { it },
            modifier = Modifier.fillMaxSize(),
            enter =
                slideInHorizontally(assetEditorSlideSpec()) { it } +
                    fadeIn(assetEditorFadeSpec()) +
                    scaleIn(assetEditorScaleSpec(), initialScale = workspaceScale),
            exit =
                slideOutHorizontally(assetEditorSlideSpec()) { it } +
                    fadeOut(assetEditorFadeSpec()) +
                    scaleOut(assetEditorScaleSpec(), targetScale = workspaceScale),
        ) {
            Surface(
                modifier = Modifier.fillMaxSize().testTag("SubscriptionEntryScreen"),
                shape = workspaceShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = if (isWideWorkspace) 6.dp else 0.dp,
                border = workspaceBorder,
            ) {
                Box(
                    modifier =
                        Modifier.fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .imePadding(),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    key(editorState.sessionId) {
                        val activeSub =
                            editorState.subscription
                                ?: editorState.subscriptionId?.let { id ->
                                    state.subscriptions.firstOrNull { it.id == id }
                                }
                        AddEditSubscriptionSheet(
                            subscription = activeSub,
                            initialPreset = editorState.initialPreset,
                            accounts = state.accounts,
                            transactions = state.transactions,
                            subscriptions = state.subscriptions,
                            categoryLimits = state.categoryLimits,
                            categoryMergeRules = state.categoryMergeRules,
                            exchangeRateLoader = exchangeRateLoader,
                            modifier = if (isWideWorkspace) Modifier.fillMaxSize() else Modifier.widthIn(max = 840.dp).fillMaxSize(),
                            onDismiss = dismiss,
                            onSave = { savedSub ->
                                onIntent(FinanceIntent.SaveSubscription(savedSub))
                                dismiss()
                            },
                            onDelete = { delSub ->
                                pendingDeleteSub = delSub
                            },
                            onSaveInstallment = { totalAmt, cat, accId, desc, count, firstDate ->
                                onIntent(
                                    FinanceIntent.AddInstallmentTransaction(
                                        amount = totalAmt,
                                        category = cat,
                                        accountId = accId,
                                        description = desc,
                                        installments = count,
                                        firstDueDate = firstDate,
                                    )
                                )
                                dismiss()
                            },
                            onAddNewAccount = { onIntent(FinanceIntent.SaveAccount(it)) },
                        )
                    }
                }
            }
        }
    }

    pendingDeleteSub?.let { sub ->
        DeleteSubscriptionConfirmDialog(
            subscription = sub,
            transactions = state.transactions,
            accounts = state.accounts,
            onDismiss = { pendingDeleteSub = null },
            onConfirm = { deletePastTransactions ->
                onIntent(FinanceIntent.DeleteSubscription(sub, deletePastTransactions))
                pendingDeleteSub = null
                onDismiss()
            },
        )
    }
}

/**
 * Large-screen subscription editor that lives in the Recurring list-detail pane (unified add-page
 * pattern: on medium and expanded windows every "add" opens in the right pane). Same form, save and
 * delete flow as [SubscriptionEntryHost], without the full-screen overlay.
 */
@Composable
fun SubscriptionEditorPane(
    state: FinanceUiState,
    editorState: SubscriptionEditorState,
    onIntent: (FinanceIntent) -> Unit,
    modifier: Modifier = Modifier,
    exchangeRateLoader: ExchangeRateLoader? = null,
    onDismiss: () -> Unit = { editorState.close() },
) {
    var pendingDeleteSub by remember { mutableStateOf<SubscriptionEntity?>(null) }
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = LocalFocusManager.current
    // Lets the delete dialog (outside the pane) close through the staggered exit too.
    val exitPane = remember { arrayOfNulls<com.example.vibefinance.ui.motion.PaneEntranceState>(1) }
    // Staggered content entrance; closing plays the staggered exit before the page is removed.
    // A new session (another subscription tapped while the editor is open) replays the entrance.
    key(editorState.sessionId) {
    com.example.vibefinance.ui.motion.StaggeredPane(modifier.fillMaxSize()) { entrance ->
    exitPane[0] = entrance
    val dismiss = {
        keyboard?.hide()
        focus.clearFocus()
        entrance.close(onDismiss)
    }
    BackHandler(enabled = editorState.isOpen && !entrance.exiting) { dismiss() }
    Surface(
        modifier = Modifier.fillMaxSize().imePadding().testTag("SubscriptionEditorPane"),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
    ) {
        key(editorState.sessionId) {
            val activeSub = editorState.subscription
                ?: editorState.subscriptionId?.let { id -> state.subscriptions.firstOrNull { it.id == id } }
            AddEditSubscriptionSheet(
                subscription = activeSub,
                initialPreset = editorState.initialPreset,
                accounts = state.accounts,
                transactions = state.transactions,
                subscriptions = state.subscriptions,
                categoryLimits = state.categoryLimits,
                categoryMergeRules = state.categoryMergeRules,
                exchangeRateLoader = exchangeRateLoader,
                modifier = Modifier.fillMaxSize(),
                onDismiss = dismiss,
                onSave = { savedSub ->
                    onIntent(FinanceIntent.SaveSubscription(savedSub))
                    dismiss()
                },
                onDelete = { delSub -> pendingDeleteSub = delSub },
                onSaveInstallment = { totalAmt, cat, accId, desc, count, firstDate ->
                    onIntent(
                        FinanceIntent.AddInstallmentTransaction(
                            amount = totalAmt, category = cat, accountId = accId,
                            description = desc, installments = count, firstDueDate = firstDate,
                        )
                    )
                    dismiss()
                },
                onAddNewAccount = { onIntent(FinanceIntent.SaveAccount(it)) },
            )
        }
    }
    }
    }
    pendingDeleteSub?.let { sub ->
        DeleteSubscriptionConfirmDialog(
            subscription = sub,
            transactions = state.transactions,
            accounts = state.accounts,
            onDismiss = { pendingDeleteSub = null },
            onConfirm = { deletePastTransactions ->
                onIntent(FinanceIntent.DeleteSubscription(sub, deletePastTransactions))
                pendingDeleteSub = null
                exitPane[0]?.close(onDismiss) ?: onDismiss()
            },
        )
    }
}
