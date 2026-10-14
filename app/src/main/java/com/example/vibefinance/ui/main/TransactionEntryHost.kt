@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.main

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.vibefinance.data.currency.ExchangeRateLoader
import com.example.vibefinance.ui.FinanceIntent
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.accounts.assetEditorFadeSpec
import com.example.vibefinance.ui.accounts.assetEditorScaleSpec
import com.example.vibefinance.ui.accounts.assetEditorSlideSpec

/** Same right-edge page motion as the Asset editor; retains drafts until exit completes. */
@Composable
internal fun TransactionEntryHost(
    visible: Boolean,
    state: FinanceUiState,
    onIntent: (FinanceIntent) -> Unit,
    onDismiss: () -> Unit,
    initialMode: TransactionMode,
    categoryFrequency: Map<String, Int>,
    accountFrequency: Map<Long, Int>,
    prefilledDestinationAccountId: Long? = null,
    modifier: Modifier = Modifier,
    exchangeRateLoader: ExchangeRateLoader? = null,
    transition: Transition<Boolean> = updateTransition(visible, label = "Transaction editor"),
) {
    transition.AnimatedVisibility(
        visible = { it },
        modifier = modifier.fillMaxSize(),
        enter = slideInHorizontally(assetEditorSlideSpec()) { it } +
            fadeIn(assetEditorFadeSpec()) + scaleIn(assetEditorScaleSpec(), initialScale = 1.15f),
        exit = slideOutHorizontally(assetEditorSlideSpec()) { it } +
            fadeOut(assetEditorFadeSpec()) + scaleOut(assetEditorScaleSpec(), targetScale = 1.15f),
    ) {
        val keyboard = LocalSoftwareKeyboardController.current
        val focus = LocalFocusManager.current
        val keyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
        val dismiss = {
            keyboard?.hide()
            focus.clearFocus()
            onDismiss()
        }
        // Nested selector popups and dialogs register after this handler and handle Back first.
        BackHandler(enabled = visible) {
            if (keyboardVisible) {
                keyboard?.hide()
                focus.clearFocus()
            } else dismiss()
        }
        Surface(Modifier.fillMaxSize().testTag("TransactionEntryScreen"), color = MaterialTheme.colorScheme.surface) {
            Box(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding(),
                contentAlignment = Alignment.TopCenter) {
                AddExpenseSheetContent(
                    state = state, onIntent = onIntent, onDismissAddDialog = dismiss,
                    initialMode = initialMode, categoryFrequency = categoryFrequency,
                    accountFrequency = accountFrequency,
                    prefilledDestinationAccountId = prefilledDestinationAccountId,
                    exchangeRateLoader = exchangeRateLoader,
                    selectorNavigationReady = transition.currentState && transition.targetState && !transition.isRunning,
                    modifier = Modifier.widthIn(max = 840.dp).fillMaxSize(),
                )
            }
        }
    }
}
