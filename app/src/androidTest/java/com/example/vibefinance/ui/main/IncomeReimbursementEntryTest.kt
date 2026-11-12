package com.example.vibefinance.ui.main

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.espresso.Espresso
import com.example.vibefinance.data.currency.*
import com.example.vibefinance.data.entity.*
import com.example.vibefinance.ui.*
import com.example.vibefinance.ui.preferences.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Inline Income switch, panel expansion and empty state explored with ARTEMIS on Waydroid. Memory only. */
class IncomeReimbursementEntryTest {
    @get:Rule val rule = createComposeRule()
    private val accounts = listOf(
        AccountEntity(1, "Bank", type = AccountType.BANK, balance = 1000.0, icon = "bank"),
        AccountEntity(2, "Card", type = AccountType.CC, balance = 100.0, icon = "card"))
    private val expense = TransactionEntity(id = 10, amount = 200.0, category = "Food", accountId = 1,
        timestamp = System.currentTimeMillis() - 10000, description = "Lunch")
    private val intents = mutableListOf<FinanceIntent>()
    private var result by mutableStateOf<BatchTransactionResult?>(null)
    private var dismissals = 0
    private val loader = ExchangeRateLoader { currency, _ ->
        ExchangeRateResult(ExchangeRateQuote(currency, 8.2, "2026-10-28", 0))
    }

    private fun content(motion: MotionLevel = MotionLevel.FULL, empty: Boolean = false): @Composable () -> Unit = {
        var visible by remember { mutableStateOf(true) }
        CompositionLocalProvider(LocalExperience provides ExperiencePreferences(motion = motion)) {
            MaterialTheme {
                TransactionEntryHost(visible, FinanceUiState(isLoading = false, accounts = accounts,
                    transactions = if (empty) emptyList() else listOf(expense,
                        expense.copy(id = 11, amount = -30.0, reimbursementOf = 10, timestamp = expense.timestamp + 1))),
                    onIntent = { intents += it }, onDismiss = { dismissals++; visible = false }, initialMode = TransactionMode.INCOME,
                    categoryFrequency = emptyMap(), accountFrequency = mapOf(2L to 10),
                    exchangeRateLoader = loader, reimbursementResult = result)
            }
        }
    }

    private fun fill(amount: String) {
        rule.onNodeWithTag("EntryAmount").performScrollTo().performTextReplacement(amount)
        rule.onNodeWithTag("EntryDescription").performScrollTo().performTextReplacement("午餐 ↩️")
        Espresso.closeSoftKeyboard()
    }
    private fun enableRepayment() {
        rule.onNodeWithTag("IncomeReimbursement").performScrollTo().assertIsOff().performClick()
        rule.onNodeWithTag("IncomeReimbursement").assertIsOn()
        rule.onNodeWithTag("IncomeReimbursementOptions").assertExists()
        rule.onNodeWithTag("ReimbursementSheet").assertDoesNotExist()
    }
    private fun selectExpense() {
        rule.onNodeWithTag("ReimbursementExpense").performScrollTo().performClick()
        rule.onNodeWithTag("ReimbursementSource_10").performClick()
    }

    @Test fun repaymentExcessRetainsFailureAndPendingDraftThenClosesOnceWithoutIncome() {
        val restorer = StateRestorationTester(rule)
        restorer.setContent(content())
        fill("230")
        enableRepayment()
        selectExpense()
        rule.onNodeWithTag("EntryCategory").assertDoesNotExist()
        rule.onNodeWithTag("EntrySubmit").assertIsEnabled()
        rule.onNodeWithTag("EntryAmount").performScrollTo().performTextReplacement("35.50")
        rule.onNodeWithTag("EntryAmount").performImeAction()
        rule.onNodeWithTag("EntrySubmit").assertIsEnabled().performClick()
        rule.onNodeWithTag("EntrySubmit").assertIsNotEnabled()
        rule.runOnIdle {
            val repayment = intents.single() as FinanceIntent.RecordReimbursement
            assertEquals(35.50, repayment.amount, 0.0)
            assertEquals(2L, repayment.accountId)
            assertEquals(expense, repayment.original)
            assertEquals("午餐 ↩️", repayment.description)
            result = BatchTransactionResult(repayment.requestId, true)
        }
        restorer.emulateSavedInstanceStateRestore()
        rule.onNodeWithTag("EntrySubmit").assertIsNotEnabled()
        rule.onNodeWithTag("IncomeReimbursement").assertIsNotEnabled()
        Espresso.pressBack()
        rule.onNodeWithTag("TransactionEntryScreen").assertExists()
        rule.runOnIdle { result = BatchTransactionResult((intents.single() as FinanceIntent.RecordReimbursement).requestId,
            false, error = "Storage unavailable") }
        rule.onNodeWithTag("ReimbursementError").assertTextContains("Storage unavailable")
        rule.onNodeWithTag("EntryDescription").assertTextContains("午餐 ↩️")
        rule.onNodeWithTag("EntrySubmit").assertIsEnabled().performClick()
        rule.runOnIdle {
            assertEquals(2, intents.size)
            val first = intents.first() as FinanceIntent.RecordReimbursement
            val last = intents.last() as FinanceIntent.RecordReimbursement
            assertEquals(first.requestId, last.requestId)
            result = BatchTransactionResult(last.requestId, false, success = true)
        }
        rule.waitUntil(5000) { dismissals == 1 }
        rule.waitUntil(5000) { rule.onAllNodesWithTag("TransactionEntryScreen").fetchSemanticsNodes().isEmpty() }
        rule.runOnIdle { assertTrue(intents.none { it is FinanceIntent.AddTransaction }) }
    }

    @Test fun selectedExpenseCanBeClearedBeforeSaving() {
        rule.setContent(content())
        fill("230")
        enableRepayment()
        selectExpense()
        val label = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
            .targetContext.getString(com.example.vibefinance.R.string.reimbursement_unlink)
        rule.onNodeWithText(label).performScrollTo().performClick()
        rule.onNodeWithText(label).assertDoesNotExist()
        rule.onNodeWithTag("EntrySubmit").assertIsEnabled().performClick()
        rule.runOnIdle {
            assertNull((intents.single() as FinanceIntent.RecordReimbursement).original)
        }
    }

    @Test fun noOriginalExpenseIsRequired() {
        rule.setContent(content(empty = true))
        fill("230")
        enableRepayment()
        rule.onNodeWithTag("EntrySubmit").assertIsEnabled().performClick()
        rule.runOnIdle {
            val repayment = intents.single() as FinanceIntent.RecordReimbursement
            assertNull(repayment.original)
            assertEquals(230.0, repayment.amount, 0.0)
        }
    }

    @Test fun inlineSelectionRestoresAndThumbSwitchOffPreservesConvertedIncomeDraft() {
        val restorer = StateRestorationTester(rule)
        restorer.setContent(content())
        fill("20")
        rule.onNodeWithTag("EntryCurrency").performClick()
        rule.onNodeWithTag("EntryCurrency_USD").performScrollTo().performClick()
        rule.waitUntil(5000) { rule.onAllNodesWithTag("EntryConvertedAmount").fetchSemanticsNodes().isNotEmpty() }
        enableRepayment()
        selectExpense()
        restorer.emulateSavedInstanceStateRestore()
        rule.onNodeWithTag("IncomeReimbursement").assertIsOn()
        rule.onNodeWithTag("ReimbursementExpense").assertTextContains("Lunch")
        rule.onNodeWithTag("EntryAmount").assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "20"))
        rule.onNodeWithTag("IncomeReimbursement").performScrollTo().performTouchInput {
            // Relative to the dynamically located row: tap the actual switch handle, not its label.
            click(Offset(width - 30f, height / 2f))
        }
        rule.onNodeWithTag("IncomeReimbursement").assertIsOff()
        rule.onNodeWithTag("IncomeReimbursementOptions").assertDoesNotExist()
        rule.onNodeWithTag("EntryCategory").assertExists()
        rule.onNodeWithTag("EntryDescription").assertTextContains("午餐 ↩️")
        rule.onNodeWithTag("EntrySubmit").assertIsEnabled().performClick()
        rule.runOnIdle {
            val income = intents.single() as FinanceIntent.AddTransaction
            assertEquals(-164.0, income.amount, 0.0)
            assertTrue(income.description.contains("午餐 ↩️"))
            assertEquals(1, dismissals)
        }
    }

    @Test fun inlineOptionsExpandAndCollapseThroughIntermediateHeights() {
        rule.setContent(content())
        fill("10")
        rule.onNodeWithTag("IncomeReimbursement").performScrollTo()
        rule.mainClock.autoAdvance = false
        rule.onNodeWithTag("IncomeReimbursement").performClick()
        rule.mainClock.advanceTimeBy(64)
        val entering = rule.onNodeWithTag("IncomeReimbursementExpansion").getUnclippedBoundsInRoot().let { it.bottom.value - it.top.value }
        rule.mainClock.advanceTimeBy(2000)
        val expanded = rule.onNodeWithTag("IncomeReimbursementExpansion").getUnclippedBoundsInRoot().let { it.bottom.value - it.top.value }
        assertTrue("Expansion must have an intermediate height", entering > 0f && entering < expanded)
        rule.onNodeWithTag("IncomeReimbursement").performClick()
        rule.mainClock.advanceTimeBy(64)
        val leaving = rule.onNodeWithTag("IncomeReimbursementExpansion").getUnclippedBoundsInRoot().let { it.bottom.value - it.top.value }
        assertTrue("Collapse must have an intermediate height", leaving > 0f && leaving < expanded)
        rule.mainClock.advanceTimeBy(2000)
        rule.onNodeWithTag("IncomeReimbursementOptions").assertDoesNotExist()
        rule.mainClock.autoAdvance = true
    }

    @Test fun repaymentAndCategoryTransitionsDoNotMoveAssetsAfterSettling() {
        rule.setContent(content())
        fill("10")
        rule.onNodeWithTag("IncomeReimbursement").performScrollTo()
        rule.mainClock.autoAdvance = false
        fun assetOffset(): Float {
            val asset = rule.onNodeWithTag("EntryAssetSection").getUnclippedBoundsInRoot()
            val panel = rule.onNodeWithTag("EntryModeClassification").getUnclippedBoundsInRoot()
            return asset.top.value - panel.top.value
        }
        fun verifySettled() {
            repeat(20) { rule.mainClock.advanceTimeBy(16); rule.waitForIdle() }
            val settled = assetOffset()
            val settledTop = rule.onNodeWithTag("EntryAssetSection").getUnclippedBoundsInRoot().top.value
            repeat(50) {
                rule.mainClock.advanceTimeBy(16); rule.waitForIdle()
                assertEquals("Assets must not jump after the transition ends", settled, assetOffset(), 0.5f)
                assertEquals("Assets must also keep their screen position", settledTop,
                    rule.onNodeWithTag("EntryAssetSection").getUnclippedBoundsInRoot().top.value, 0.5f)
            }
        }
        rule.onNodeWithTag("IncomeReimbursement").performClick()
        verifySettled()
        rule.onNodeWithTag("IncomeReimbursement").performClick()
        verifySettled()
        rule.onNodeWithTag("EntryCategory_toggle").performScrollTo().performClick()
        verifySettled()
        rule.onNodeWithTag("EntryCategory_toggle").performClick()
        verifySettled()
        rule.mainClock.autoAdvance = true
    }

    @Test fun minimalMotionAndEmptyLedgerAllowStandaloneRepayment() {
        rule.setContent(content(motion = MotionLevel.MINIMAL, empty = true))
        fill("10")
        enableRepayment()
        rule.onNodeWithTag("ReimbursementExpense").assertIsNotEnabled()
        rule.onNodeWithTag("EntrySubmit").assertIsEnabled()
        rule.runOnIdle { assertTrue(intents.isEmpty()) }
    }
}
