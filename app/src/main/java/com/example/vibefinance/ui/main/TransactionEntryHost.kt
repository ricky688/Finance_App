@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.main

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

/**
 * Adaptive Transaction Entry Host.
 * On compact screens (phone), slides over full-screen.
 * On wide screens (tablet/landscape), operates as a Material 3 Modal Side Sheet (420dp width,
 * anchored to the right with a background Scrim), leaving the Navigation Rail completely visible.
 */
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
    reimbursementResult: com.example.vibefinance.ui.BatchTransactionResult? = null,
    useSideSheet: Boolean = false,
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

    if (useSideSheet) {
        Box(modifier = modifier.fillMaxSize()) {
            val scrimAlpha by transition.animateFloat(
                transitionSpec = { tween(240) },
                label = "SideSheetScrimAlpha"
            ) { if (it) 0.38f else 0f }

            if (scrimAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = scrimAlpha))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = dismiss
                        )
                        .testTag("TransactionSideSheetScrim")
                )
            }

            transition.AnimatedVisibility(
                visible = { it },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight(),
                enter = slideInHorizontally(assetEditorSlideSpec()) { it } +
                    fadeIn(assetEditorFadeSpec()) + scaleIn(assetEditorScaleSpec(), initialScale = 1.05f),
                exit = slideOutHorizontally(assetEditorSlideSpec()) { it } +
                    fadeOut(assetEditorFadeSpec()) + scaleOut(assetEditorScaleSpec(), targetScale = 1.05f),
            ) {
                Surface(
                    modifier = Modifier
                        .width(420.dp)
                        .fillMaxHeight()
                        .testTag("TransactionEntryScreen"),
                    shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ) {
                    Box(
                        Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        AddExpenseSheetContent(
                            state = state, onIntent = onIntent, onDismissAddDialog = dismiss,
                            initialMode = initialMode, categoryFrequency = categoryFrequency,
                            accountFrequency = accountFrequency,
                            prefilledDestinationAccountId = prefilledDestinationAccountId,
                            exchangeRateLoader = exchangeRateLoader,
                            selectorNavigationReady = transition.currentState && transition.targetState && !transition.isRunning,
                            modifier = Modifier.fillMaxSize(),
                            reimbursementResult = reimbursementResult,
                        )
                    }
                }
            }
        }
    } else {
        transition.AnimatedVisibility(
            visible = { it },
            modifier = modifier.fillMaxSize(),
            enter = slideInHorizontally(assetEditorSlideSpec()) { it } +
                fadeIn(assetEditorFadeSpec()) + scaleIn(assetEditorScaleSpec(), initialScale = 1.15f),
            exit = slideOutHorizontally(assetEditorSlideSpec()) { it } +
                fadeOut(assetEditorFadeSpec()) + scaleOut(assetEditorScaleSpec(), targetScale = 1.15f),
        ) {
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
                        reimbursementResult = reimbursementResult,
                    )
                }
            }
        }
    }
}
