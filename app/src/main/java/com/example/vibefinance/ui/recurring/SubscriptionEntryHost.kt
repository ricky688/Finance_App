package com.example.vibefinance.ui.recurring

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

/** Full-screen right-edge slide-over host for Add/Edit Subscription & Installment Plans. */
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
) {
    var pendingDeleteSub by remember { mutableStateOf<SubscriptionEntity?>(null) }

    transition.AnimatedVisibility(
        visible = { it },
        modifier = modifier.fillMaxSize(),
        enter =
            slideInHorizontally(assetEditorSlideSpec()) { it } +
                fadeIn(assetEditorFadeSpec()) +
                scaleIn(assetEditorScaleSpec(), initialScale = 1.15f),
        exit =
            slideOutHorizontally(assetEditorSlideSpec()) { it } +
                fadeOut(assetEditorFadeSpec()) +
                scaleOut(assetEditorScaleSpec(), targetScale = 1.15f),
    ) {
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
        Surface(
            modifier = Modifier.fillMaxSize().testTag("SubscriptionEntryScreen"),
            color = MaterialTheme.colorScheme.surface,
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
                        modifier = Modifier.widthIn(max = 840.dp).fillMaxSize(),
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
                onDismiss()
            },
        )
    }
}
