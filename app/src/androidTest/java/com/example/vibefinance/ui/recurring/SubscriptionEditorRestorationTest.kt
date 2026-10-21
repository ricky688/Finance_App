package com.example.vibefinance.ui.recurring

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vibefinance.R
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Existing Recurring FAB/Add Subscription/Close paths explored with ARTEMIS and ADB. */
class SubscriptionEditorRestorationTest {
    @get:Rule val rule = createComposeRule()
    @Test fun subscriptionDraftSurvivesRestorationAndCloseWithoutSaving() {
        val restorer = StateRestorationTester(rule)
        lateinit var editor: SubscriptionEditorState
        restorer.setContent {
            MaterialTheme {
                editor = rememberSubscriptionEditorState()
                Box(Modifier.fillMaxSize()) {
                    Box(Modifier.align(androidx.compose.ui.Alignment.BottomEnd).padding(16.dp).size(56.dp)
                        .testTag("OpenSubscriptionFab")
                        .clickable { editor.open() })
                    SubscriptionEntryHost(editor.isOpen, FinanceUiState(isLoading = false,
                        accounts = listOf(AccountEntity(id = 1, name = "Bank", type = AccountType.BANK, balance = 1000.0, icon = "bank"))), editor,
                        onIntent = { error("Memory-only fixture must not persist") })
                }
            }
        }
        rule.onNodeWithTag("OpenSubscriptionFab").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("SubscriptionEntryScreen").assertExists()
        rule.onNodeWithTag("SubscriptionAmount").performTextReplacement("88.50")
        rule.onNodeWithTag("SubscriptionName").performScrollTo().performTextReplacement("測試服務 🎵")
        restorer.emulateSavedInstanceStateRestore()
        rule.waitForIdle()
        rule.onNodeWithTag("SubscriptionEntryScreen").assertExists()
        rule.onNodeWithTag("SubscriptionName").assertTextContains("測試服務 🎵")
        rule.onNodeWithTag("SubscriptionAmount").assertTextContains("88.50")
        val close = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.btn_close)
        rule.onNodeWithContentDescription(close).performClick()
        rule.waitUntil(5000) { rule.onAllNodesWithTag("SubscriptionEntryScreen").fetchSemanticsNodes().isEmpty() }
    }
}
