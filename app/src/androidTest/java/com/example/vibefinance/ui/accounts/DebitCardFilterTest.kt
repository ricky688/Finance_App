package com.example.vibefinance.ui.accounts

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.theme.LocalIsDarkTheme
import com.example.vibefinance.ui.FinanceUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Four-option Assets selection was explored with ARTEMIS and ADB before this regression. */
@RunWith(AndroidJUnit4::class)
class DebitCardFilterTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun debitOption_countsOnlyDebitCards_andSwitchesWithOtherFilters() {
        val accounts = listOf(
            AccountEntity(id = 1, name = "Bank fixture", type = AccountType.BANK, balance = 500.0, icon = "bank"),
            AccountEntity(id = 2, name = "First debit fixture", type = AccountType.DEBIT, balance = 100.0, icon = "card"),
            AccountEntity(id = 3, name = "Second debit fixture", type = AccountType.DEBIT, balance = 200.0, icon = "card"),
            AccountEntity(id = 4, name = "Credit fixture", type = AccountType.CC, balance = 50.0, icon = "cc")
        )
        rule.setContent {
            CompositionLocalProvider(LocalIsDarkTheme provides true) {
                MaterialTheme(colorScheme = darkColorScheme()) {
                    AccountsScreen(state = FinanceUiState(isLoading = false, accounts = accounts), onIntent = {})
                }
            }
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        fun option(resource: Int) = rule.onNode(
            hasText(context.getString(resource)) and
                SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton)
        )

        val all = option(R.string.asset_filter_all)
        val assets = option(R.string.asset_filter_cash_bank)
        val debit = option(R.string.asset_filter_debit_cards)
        val credit = option(R.string.asset_filter_credit_cards)
        debit.performScrollTo()
        rule.waitForIdle()
        all.assertIsOn().assertTextContains("4")
        assets.assertTextContains("3")
        debit.assertIsOff().assertTextContains("2").performClick()
        rule.waitForIdle()
        debit.assertIsOn()
        all.assertIsOff()
        credit.assertIsOff().assertTextContains("1").performClick()
        rule.waitForIdle()
        credit.assertIsOn()
        debit.assertIsOff()
        all.performClick()
        rule.waitForIdle()
        all.assertIsOn()
        debit.assertIsOff()
        credit.assertIsOff()
    }
}
