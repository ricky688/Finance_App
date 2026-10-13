package com.example.vibefinance.ui.accounts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.*
import com.example.vibefinance.ui.components.MoneySeparationChart
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Actual Assets/add/type/reminder/discard path explored with ARTEMIS/ADB. No persisted mutations. */
@RunWith(AndroidJUnit4::class)
class InvestmentDistributionTest {
    @get:Rule val rule = createComposeRule()

    @Test fun investmentHasSeparateBarAndNotice_withoutInflatingOrdinaryAssets() {
        val bank = AccountEntity(id = 1, name = "Bank", type = AccountType.BANK, balance = 500.0, icon = "bank")
        val investment = bank.copy(id = 2, type = AccountType.INVESTMENT, balance = 1000.25)
        var accounts by mutableStateOf(listOf(bank, investment))
        rule.setContent {
            MaterialTheme {
                Column(Modifier.width(360.dp).verticalScroll(rememberScrollState())) {
                    MoneySeparationChart(accounts)
                }
            }
        }
        rule.onNodeWithTag("InvestmentDistribution").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("InvestmentDistributionAmount").assertTextEquals("$1,000.25")
        rule.onNodeWithTag("InvestmentDistributionBar").assertExists()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        rule.onNodeWithText(context.getString(R.string.assets_investment_notice)).assertIsDisplayed()
        rule.onAllNodesWithText("$1,500.25").assertCountEquals(0)
        assertEquals(500.0, calculateNetWorth(accounts).netWorth, 0.0)
        rule.runOnIdle { accounts = listOf(bank, investment.copy(balance = 0.0)) }
        rule.onNodeWithTag("InvestmentDistributionAmount").assertTextEquals("$0.00")
        rule.onNodeWithTag("InvestmentDistributionBar").assertExists()
        rule.runOnIdle { accounts = listOf(bank) }
        rule.onNodeWithTag("InvestmentDistribution").assertDoesNotExist()
    }
}
