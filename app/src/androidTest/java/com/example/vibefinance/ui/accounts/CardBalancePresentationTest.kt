@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.accounts

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.*
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.preferences.LocalExperience
import com.example.vibefinance.ui.preferences.MotionLevel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

/** Assets, balance dialog and History paths explored with ARTEMIS/ADB before authoring. */
@RunWith(AndroidJUnit4::class)
class CardBalancePresentationTest {
    @get:Rule val rule = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val go = AccountEntity(id = 1, name = "Go UnionPay", type = AccountType.CC, balance = -182.80, icon = "card")
    private fun text(resource: Int) = context.getString(resource)
    private fun balanceText(value: String) = rule.onNode(hasText(value) and hasAnyAncestor(hasTestTag("AccountBalance_1")), useUnmergedTree = true)
    private fun balanceLabel(resource: Int) = rule.onNode(hasText(text(resource)) and hasAnyAncestor(hasTestTag("AccountBalance_1")), useUnmergedTree = true)

    @Test fun receivingPaymentShowsIncreasingCardCreditAndDebtHasItsOwnLabel() {
        var account by mutableStateOf(go)
        rule.setContent {
            MaterialTheme(motionScheme = MotionScheme.expressive()) {
                Column(Modifier.width(360.dp).verticalScroll(rememberScrollState())) {
                    ExpressiveAccountListItem(account, onEditClick = {}, onBalanceClick = {}, onHistoryClick = {})
                }
            }
        }
        balanceLabel(R.string.ah_card_credit).assertIsDisplayed()
        balanceText("$182.80").assertIsDisplayed()
        rule.onNodeWithText("$-182.80").assertDoesNotExist()
        rule.runOnIdle { account = go.copy(balance = -281.80) }
        balanceText("$281.80").assertIsDisplayed()
        balanceLabel(R.string.ah_card_credit).assertExists()
        rule.runOnIdle { account = go.copy(balance = 99.0) }
        balanceText("$99.00").assertIsDisplayed()
        balanceLabel(R.string.ah_outstanding_debt).assertExists()
        rule.runOnIdle { account = go.copy(balance = 0.0) }
        balanceText("$0.00").assertIsDisplayed()
        balanceLabel(R.string.ah_outstanding_debt).assertExists()
        rule.runOnIdle { account = go.copy(type = AccountType.DEBIT, balance = -25.0) }
        balanceLabel(R.string.ah_current_balance).assertExists()
        balanceText("$-25.00").assertIsDisplayed()
    }

    @Test fun compactDarkLargeTextKeepsCreditLabelAndRawDebtEditorExplainsSigns() {
        // Route display preference writes to the test APK; the real app's settings stay intact.
        val isolated = object : ContextWrapper(context) {
            override fun getSharedPreferences(name: String, mode: Int) =
                InstrumentationRegistry.getInstrumentation().context.getSharedPreferences("credit_fixture_$name", Context.MODE_PRIVATE)
        }
        isolated.getSharedPreferences("assets_display", Context.MODE_PRIVATE).edit().clear().commit()
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalContext provides isolated, LocalDensity provides Density(density.density, 1.3f)) {
                MaterialTheme(colorScheme = darkColorScheme(), motionScheme = MotionScheme.expressive()) {
                    AccountsScreen(FinanceUiState(isLoading = false, accounts = listOf(go.copy(balance = -281.80))), {})
                }
            }
        }
        rule.onNodeWithTag("AssetsList").performScrollToNode(hasText(text(R.string.assets_compact_mode)))
        rule.onNodeWithText(text(R.string.assets_compact_mode)).performClick()
        rule.onNodeWithTag("AssetsList").performScrollToNode(hasText("Go UnionPay"))
        balanceLabel(R.string.ah_card_credit).assertIsDisplayed()
        balanceText("$281.80").assertIsDisplayed()
        rule.onNodeWithContentDescription(text(R.string.assets_edit_balance)).performClick()
        rule.onNodeWithText(text(R.string.ah_credit_balance_help)).assertIsDisplayed()
        rule.onNode(hasSetTextAction()).assertTextContains("-281.80")
        rule.onNodeWithText(context.getString(R.string.btn_cancel)).performClick()
        balanceText("$281.80").assertExists()
    }

    @Test fun cardPreviewNamesCreditAndUsesPositiveMagnitude() {
        rule.setContent {
            CompositionLocalProvider(LocalExperience provides LocalExperience.current.copy(motion = MotionLevel.MINIMAL)) {
                MaterialTheme { AssetCardItem(go.copy(balance = -281.80), {}) }
            }
        }
        rule.onNodeWithText(text(R.string.ah_card_credit)).assertIsDisplayed()
        rule.onNodeWithText("$281.80").assertIsDisplayed()
        rule.onNodeWithText("$-281.80").assertDoesNotExist()
    }

    @Test fun traditionalChineseNamesCardCreditAndPrivacyHidesItsAmount() {
        val localized = context.createConfigurationContext(Configuration(context.resources.configuration).apply {
            setLocale(Locale.forLanguageTag("zh-HK"))
        })
        var hidden by mutableStateOf(false)
        rule.setContent {
            CompositionLocalProvider(LocalContext provides localized,
                LocalConfiguration provides localized.resources.configuration,
                LocalExperience provides LocalExperience.current.copy(hideAmounts = hidden)) {
                MaterialTheme {
                    ExpressiveAccountListItem(go.copy(balance = -281.80), onEditClick = {}, onBalanceClick = {}, onHistoryClick = {})
                }
            }
        }
        rule.onNodeWithText(localized.getString(R.string.ah_card_credit), useUnmergedTree = true).assertIsDisplayed()
        balanceText("$281.80").assertIsDisplayed()
        rule.runOnIdle { hidden = true }
        balanceText("$281.80").assertDoesNotExist()
        balanceText("••••").assertIsDisplayed()
    }
}
