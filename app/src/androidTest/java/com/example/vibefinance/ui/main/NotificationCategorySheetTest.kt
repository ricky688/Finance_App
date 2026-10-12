package com.example.vibefinance.ui.main

import android.app.NotificationManager
import android.os.SystemClock
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.service.PaymentLoggingNotification
import com.example.vibefinance.ui.FinanceUiState
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

/** Paths explored using ARTEMIS on Waydroid, with a memory-only preview before test authoring. */
@RunWith(AndroidJUnit4::class)
class NotificationCategorySheetTest {
    @get:Rule val compose = createComposeRule()
    private val context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private val transaction =
        TransactionEntity(
            id = 19,
            accountId = 8,
            amount = 42.0,
            category = "Dining",
            description = "Preview Coffee",
            timestamp = 0,
        )

    @Test
    fun selectingCategoryThenSaveReturnsOnlyCategoryWithoutTouchingLedger() {
        var category: String? = null
        compose.setContent {
            MaterialTheme {
                NotificationCategorySheet(
                    transaction,
                    FinanceUiState(transactions = listOf(transaction)),
                    false,
                    false,
                    onSave = { category = it },
                    onDismiss = {},
                )
            }
        }
        compose.waitUntil(5_000) {
            compose.onAllNodesWithTag("NotificationCategorySheet").fetchSemanticsNodes().size == 1
        }
        compose.onNodeWithTag("EditTransactionCategory").performClick()
        compose.waitUntil(5_000) {
            compose
                .onAllNodesWithTag("EditTransactionCategoryOption_Transport")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        compose.onNodeWithTag("EditTransactionCategoryOption_Transport").performClick()
        compose.onNodeWithTag("NotificationCategorySave").performClick()
        compose.runOnIdle { assertEquals("Transport", category) }
    }

    @Test
    fun savingDisablesSubmissionAndFailureKeepsSelectionForRetry() {
        var saving by mutableStateOf(true)
        var error by mutableStateOf(false)
        var saved: String? = null
        compose.setContent {
            MaterialTheme {
                NotificationCategorySheet(
                    transaction,
                    FinanceUiState(transactions = listOf(transaction)),
                    saving,
                    error,
                    onSave = { saved = it },
                    onDismiss = {},
                )
            }
        }
        compose.onNodeWithTag("NotificationCategorySave").assertIsNotEnabled()
        compose.runOnIdle {
            saving = false
            error = true
        }
        compose
            .onNodeWithText(context.getString(R.string.nf_category_save_failed))
            .assertIsDisplayed()
        compose.onNodeWithTag("NotificationCategorySave").performClick()
        compose.runOnIdle { assertEquals("Dining", saved) }
    }

    @Test
    fun privateReceiverCancelsAStaleReceiptWithoutLaunchingUiOrChangingLedger() {
        val manager = context.getSystemService(NotificationManager::class.java)
        Assume.assumeTrue(manager.areNotificationsEnabled())
        com.example.vibefinance.data.InMemoryDatabase.ensureInitialized(context)
        val id = "d".repeat(64)
        val before = com.example.vibefinance.data.InMemoryDatabase.transactions.value
        Assume.assumeFalse(before.any { it.groupId == "notification:$id" })
        val accounts = com.example.vibefinance.data.InMemoryDatabase.accounts.value
        try {
            PaymentLoggingNotification.show(
                context,
                id,
                "Stale payment test",
                "Synthetic receipt",
                42.0,
            )
            val receipt = manager.activeNotifications.single { it.tag == id }
            receipt.notification.actions[0].actionIntent.send()
            compose.waitUntil(5_000) { manager.activeNotifications.none { it.tag == id } }
            assertEquals(before, com.example.vibefinance.data.InMemoryDatabase.transactions.value)
            assertEquals(accounts, com.example.vibefinance.data.InMemoryDatabase.accounts.value)
        } finally {
            PaymentLoggingNotification.cancel(context, id)
        }
    }

    @Test
    fun syntheticNativeReceiptHasTwoActionsAndExpiresAfterThirtySeconds() {
        val manager = context.getSystemService(NotificationManager::class.java)
        Assume.assumeTrue(
            "Notification permission must already be enabled",
            manager.areNotificationsEnabled(),
        )
        val id = "e".repeat(64)
        val before = com.example.vibefinance.data.InMemoryDatabase.transactions.value
        try {
            PaymentLoggingNotification.show(
                context,
                id,
                "Payment test",
                "Synthetic receipt, no ledger entry",
                42.0,
            )
            fun active() = manager.activeNotifications.firstOrNull { it.tag == id }
            val posted = active() ?: throw AssertionError("Receipt was not posted")
            assertEquals(30_000L, posted.notification.timeoutAfter)
            assertEquals(2, posted.notification.actions.size)
            assertTrue(posted.notification.actions.all { it.actionIntent.isImmutable })
            InstrumentationRegistry.getInstrumentation()
                .uiAutomation
                .performGlobalAction(
                    android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME
                )
            val deadline = SystemClock.elapsedRealtime() + 35_000L
            while (active() != null && SystemClock.elapsedRealtime() < deadline) SystemClock.sleep(
                100
            )
            assertNull("Android must cancel the receipt after its native timeout", active())
            assertEquals(before, com.example.vibefinance.data.InMemoryDatabase.transactions.value)
        } finally {
            PaymentLoggingNotification.cancel(context, id)
        }
    }
}
