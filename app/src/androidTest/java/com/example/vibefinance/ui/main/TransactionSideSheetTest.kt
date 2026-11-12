package com.example.vibefinance.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.ui.FinanceUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TransactionSideSheetTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun sideSheetCappedAt420dpAndLeavesLeftAreaUncovered() {
        var dismissed = false
        val dummyState = FinanceUiState(
            isLoading = false,
            accounts = listOf(AccountEntity(id = 1L, name = "Cash", type = AccountType.CASH, balance = 500.0, icon = "cash"))
        )

        rule.setContent {
            MaterialTheme {
                Box(Modifier.requiredSize(1000.dp, 700.dp).testTag("Window")) {
                    TransactionEntryHost(
                        visible = true,
                        state = dummyState,
                        onIntent = {},
                        onDismiss = { dismissed = true },
                        initialMode = TransactionMode.EXPENSE,
                        categoryFrequency = emptyMap(),
                        accountFrequency = emptyMap(),
                        useSideSheet = true
                    )
                }
            }
        }

        rule.waitForIdle()
        rule.onNodeWithTag("TransactionEntryScreen").assertIsDisplayed()
        rule.onNodeWithTag("TransactionSideSheetScrim").assertIsDisplayed()

        val windowBounds = rule.onNodeWithTag("Window").getUnclippedBoundsInRoot()
        val sheetBounds = rule.onNodeWithTag("TransactionEntryScreen").getUnclippedBoundsInRoot()

        // Side sheet is anchored to the right and is approx 420dp wide
        assertEquals(windowBounds.right.value, sheetBounds.right.value, 2f)
        val sheetWidth = (sheetBounds.right - sheetBounds.left).value
        assertTrue("Sheet must be around 420dp wide", sheetWidth in 410f..430f)
        assertTrue("Sheet leaves left area of window uncovered by surface", sheetBounds.left.value > windowBounds.left.value + 500f)

        // Click scrim to trigger dismiss
        rule.onNodeWithTag("TransactionSideSheetScrim").performClick()
        rule.waitForIdle()
        assertTrue("Clicking scrim must trigger onDismiss", dismissed)
    }

    @Test
    fun compactModeUsesFullScreen() {
        val dummyState = FinanceUiState(
            isLoading = false,
            accounts = listOf(AccountEntity(id = 1L, name = "Cash", type = AccountType.CASH, balance = 500.0, icon = "cash"))
        )

        rule.setContent {
            MaterialTheme {
                Box(Modifier.requiredSize(360.dp, 640.dp).testTag("Window")) {
                    TransactionEntryHost(
                        visible = true,
                        state = dummyState,
                        onIntent = {},
                        onDismiss = {},
                        initialMode = TransactionMode.EXPENSE,
                        categoryFrequency = emptyMap(),
                        accountFrequency = emptyMap(),
                        useSideSheet = false
                    )
                }
            }
        }

        rule.waitForIdle()
        rule.onNodeWithTag("TransactionEntryScreen").assertIsDisplayed()
        rule.onNodeWithTag("TransactionSideSheetScrim").assertDoesNotExist()

        val windowBounds = rule.onNodeWithTag("Window").getUnclippedBoundsInRoot()
        val screenBounds = rule.onNodeWithTag("TransactionEntryScreen").getUnclippedBoundsInRoot()

        // Compact mode fills full screen
        val windowWidth = (windowBounds.right - windowBounds.left).value
        val screenWidth = (screenBounds.right - screenBounds.left).value
        assertEquals(windowWidth, screenWidth, 2f)
    }
}
