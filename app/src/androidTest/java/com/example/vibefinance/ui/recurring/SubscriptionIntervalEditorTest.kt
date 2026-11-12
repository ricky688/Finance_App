package com.example.vibefinance.ui.recurring

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Recurring → Add → Daily/Repeat every/IME explored with ARTEMIS. Callbacks use memory only. */
class SubscriptionIntervalEditorTest {
    @get:Rule val rule = createComposeRule()
    private val bank = AccountEntity(id = 1, name = "Bank", type = AccountType.BANK, balance = 1000.0, icon = "bank")
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun customDailyIntervalValidatesRestoresAndSaves() {
        val restorer = StateRestorationTester(rule)
        val saved = mutableListOf<SubscriptionEntity>()
        restorer.setContent { MaterialTheme { AddEditSubscriptionSheet(null, null, listOf(bank), {},
            onSave = { saved += it }, onDelete = { error("New subscription cannot delete") }) } }
        rule.onNodeWithTag("SubscriptionAmount").performTextReplacement("60")
        rule.onNodeWithTag("SubscriptionName").performScrollTo().performTextReplacement("服務 🎵")
        rule.onNodeWithText(context.getString(R.string.subscription_daily)).performScrollTo().performClick()
        rule.onNodeWithTag("SubscriptionInterval").performScrollTo().performTextReplacement("0")
        rule.onNodeWithTag("SubscriptionSaveButton").assertIsNotEnabled()
        rule.onNodeWithTag("SubscriptionInterval").performTextReplacement("2")
        restorer.emulateSavedInstanceStateRestore()
        rule.onNodeWithTag("SubscriptionInterval").assertTextContains("2")
        rule.onNodeWithTag("SubscriptionName").assertTextContains("服務 🎵")
        rule.onNodeWithTag("SubscriptionSaveButton").assertIsEnabled().performClick()
        rule.runOnIdle {
            assertEquals(1, saved.size)
            assertEquals("Daily", saved.single().frequency)
            assertEquals(2, saved.single().interval)
            assertEquals(60.0, saved.single().amount, 0.0)
        }
    }

    @Test fun editingPreservesSixMonthIntervalAndIdentity() {
        val sub = SubscriptionEntity(id = 7, name = "Insurance", amount = 600.0, category = "Other",
            frequency = "Monthly", interval = 6, nextPaymentDate = System.currentTimeMillis() + 86400000, accountId = 1)
        val saved = mutableListOf<SubscriptionEntity>()
        rule.setContent { MaterialTheme { AddEditSubscriptionSheet(sub, null, listOf(bank), {},
            onSave = { saved += it }, onDelete = {}) } }
        rule.onNodeWithTag("SubscriptionInterval").performScrollTo().assertTextContains("6")
        rule.onNodeWithTag("SubscriptionSaveButton").assertIsEnabled().performClick()
        rule.runOnIdle {
            assertEquals(1, saved.size)
            assertEquals(7L, saved.single().id)
            assertEquals(6, saved.single().interval)
            assertEquals("Monthly", saved.single().frequency)
        }
    }
}
