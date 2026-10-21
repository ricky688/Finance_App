@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.main

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.toSize
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vibefinance.data.entity.*
import com.example.vibefinance.data.repository.DailyBudgetInfo
import com.example.vibefinance.ui.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** ARTEMIS/ADB verified Bento 2-Card layout, Category tag cloud, and Full-Width Asset Card bottom sheet picker. */
@RunWith(AndroidJUnit4::class)
class GuidedEntrySelectorTest {
    @get:Rule val rule = createComposeRule()
    private val accounts = (1L..14L).map { AccountEntity(it, "Account $it", type = AccountType.BANK, balance = 1000.0, icon = "bank") }
    private var submitted: FinanceIntent.AddTransaction? = null
    private var submissions = 0
    private lateinit var view: android.view.View

    private fun show(mode: TransactionMode = TransactionMode.EXPENSE, restoration: StateRestorationTester? = null,
        wideLarge: Boolean = false, empty: Boolean = false, prefill: Long? = null, single: Boolean = false) {
        val content: @Composable () -> Unit = {
            view = androidx.compose.ui.platform.LocalView.current
            val original = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides if (wideLarge) Density(original.density * .5f, 1.7f) else original) {
                MaterialTheme(colorScheme = if (wideLarge) darkColorScheme() else lightColorScheme(), motionScheme = MotionScheme.expressive()) {
                    TransactionEntryHost(true, FinanceUiState(isLoading = false, accounts = if (empty) emptyList() else if (single) accounts.take(1) else accounts,
                        budgetInfo = DailyBudgetInfo(2500.0, 60.5, 2439.5, 94.0, 94.0, 26, 0, 0)),
                        { if (it is FinanceIntent.AddTransaction) { submitted = it; submissions++ } }, {}, mode,
                        emptyMap(), emptyMap(), prefilledDestinationAccountId = prefill)
                }
            }
        }
        if (restoration == null) rule.setContent(content) else restoration.setContent(content)
        noSheet()
    }

    private fun noSheet() {
        rule.waitForIdle()
        rule.onAllNodesWithTag("EntryAccount_row_1").assertCountEquals(0)
        rule.onAllNodesWithTag("EntryDestination_row_1").assertCountEquals(0)
    }

    private fun chooseCategory(key: String) {
        val row = rule.onNodeWithTag("EntryCategory")
        try { row.performScrollTo() } catch (_: AssertionError) {}
        while (rule.onAllNodesWithTag("EntryCategory_row_$key").fetchSemanticsNodes().isEmpty()) {
            row.performTouchInput { swipeLeft() }
            rule.waitForIdle()
        }
        rule.onNodeWithTag("EntryCategory_row_$key").performClick()
    }

    private fun openAccountSheet() {
        rule.onNodeWithTag("EntryAccount_card").performScrollTo().performClick()
        rule.waitUntil(5_000) {
            rule.onAllNodesWithTag("EntryAccount_row_1").fetchSemanticsNodes().isNotEmpty() ||
            rule.onAllNodesWithTag("AccountPicker_AddAccountToggle").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun openDestinationSheet() {
        rule.onNodeWithTag("EntryDestination_card").performScrollTo().performClick()
        rule.waitUntil(5_000) {
            rule.onAllNodes(hasTestTag("AccountPicker_AddAccountToggle") or hasTestTag("EntryDestination_row_1") or hasTestTag("EntryDestination_row_2") or hasTestTag("EntryDestination_row_3")).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun chooseAccount(id: Long) {
        while (rule.onAllNodesWithTag("EntryAccount_row_$id").fetchSemanticsNodes().isEmpty()) {
            rule.onAllNodes(hasScrollAction()).onLast().performTouchInput { swipeUp() }
            rule.waitForIdle()
        }
        rule.onNodeWithTag("EntryAccount_row_$id").performClick()
        noSheet()
    }

    private fun chooseDestination(id: Long) {
        while (rule.onAllNodesWithTag("EntryDestination_row_$id").fetchSemanticsNodes().isEmpty()) {
            rule.onAllNodes(hasScrollAction()).onLast().performTouchInput { swipeUp() }
            rule.waitForIdle()
        }
        rule.onNodeWithTag("EntryDestination_row_$id").performClick()
        noSheet()
    }

    private fun bounds(tag: String): Rect {
        val coordinates = rule.onNodeWithTag(tag).fetchSemanticsNode().layoutInfo.coordinates
        return Rect(coordinates.localToScreen(Offset.Zero), coordinates.size.toSize())
    }

    private fun apply(amountVal: String = "7") {
        noSheet()
        assertNull(submitted)
        try { rule.onNodeWithTag("EntryAmount").performScrollTo() } catch (_: AssertionError) {}
        rule.onNodeWithTag("EntryAmount").performTextReplacement(amountVal)
        rule.onNodeWithTag("EntrySubmit").assertIsDisplayed().performClick()
        assertEquals(1, submissions)
    }

    private fun finishAmount(value: String = "12.50") {
        rule.onNodeWithTag("EntryAmount").performScrollTo().performClick().performTextReplacement(value)
        rule.waitUntil(8_000) {
            androidx.core.view.ViewCompat.getRootWindowInsets(view)
                ?.isVisible(androidx.core.view.WindowInsetsCompat.Type.ime()) == true
        }
        rule.onNodeWithTag("EntryAmount").performImeAction()
        if (value != "0") {
            rule.waitUntil(8_000) {
                androidx.core.view.ViewCompat.getRootWindowInsets(view)
                    ?.isVisible(androidx.core.view.WindowInsetsCompat.Type.ime()) == false
            }
        }
    }

    private fun guidedCategoryThenAccount(category: String) {
        finishAmount()
        rule.onNodeWithTag("EntryCategory_cloud").assertIsDisplayed()
        rule.waitUntil(5_000) {
            val cloud = rule.onNodeWithTag("EntryCategory_cloud").getUnclippedBoundsInRoot()
            val viewport = rule.onNodeWithTag("EntryScrollContent").getUnclippedBoundsInRoot()
            val scrollRange = rule.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange) and hasAnyAncestor(hasTestTag("EntryScrollContent")))
                .fetchSemanticsNode().config[SemanticsProperties.VerticalScrollAxisRange]
            cloud.top >= viewport.top && cloud.bottom <= viewport.bottom &&
                (scrollRange.value() > 0f || scrollRange.maxValue() == 0f)
        }
        rule.onNodeWithTag("EntryCategory_row_$category").assertIsDisplayed().performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("EntryAccount_row_1").fetchSemanticsNodes().isNotEmpty() }
        chooseAccount(1)
        rule.onNodeWithTag("EntrySubmit").assertIsEnabled()
        assertNull("Guidance must never save automatically", submitted)
        rule.onNodeWithTag("EntryAmount").assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "12.50"))
    }

    @Test fun expenseKeyboardDone_revealsCategories_thenAutomaticallyOpensAssets() {
        show()
        guidedCategoryThenAccount("Utilities")
    }

    @Test fun incomeKeyboardDone_revealsCategories_thenAutomaticallyOpensAssets() {
        show(TransactionMode.INCOME)
        guidedCategoryThenAccount("Salary")
    }

    @Test fun transferKeyboardDone_opensSource_thenDestination_withoutSaving() {
        show(TransactionMode.TRANSFER, prefill = 2)
        finishAmount()
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("EntryAccount_row_1").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("EntryAccount_row_2").performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("EntryDestination_row_1").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("EntryDestination_row_2").assertDoesNotExist()
        chooseDestination(1)
        rule.onNodeWithTag("EntrySubmit").assertIsEnabled()
        assertNull(submitted)
    }

    @Test fun zeroAmountDoesNotAdvance_andCancelledGuidanceDoesNotReopenOnModeChange() {
        show()
        finishAmount("0")
        rule.onNodeWithTag("EntryAmount").assertIsFocused()
        rule.onNodeWithTag("EntryCategory_cloud").assertDoesNotExist()
        guidedCategoryThenAccount("Food")
        rule.onNodeWithTag("TransactionMode_INCOME").performScrollTo().performClick()
        noSheet()
        rule.onNodeWithTag("EntryCategory_cloud").assertDoesNotExist()
        finishAmount()
        rule.onNodeWithTag("EntryCategory_row_Salary").performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("EntryAccount_row_1").fetchSemanticsNodes().isNotEmpty() }
        pressBack()
        noSheet()
        rule.onNodeWithTag("TransactionMode_TRANSFER").performScrollTo().performClick()
        noSheet()
        assertNull(submitted)
    }

    @Test fun expenseStartsWithCategory_thenScrollsToAsset_andReturnsToFixedKeypad() {
        show()
        val keypad = bounds("EntryKeypad")
        rule.onNodeWithTag("EntryAccount_card").assertIsDisplayed()
        chooseCategory("Utilities")
        openAccountSheet()
        chooseAccount(14)
        apply("7")
        assertEquals("Utilities", submitted!!.category)
        assertEquals(14L, submitted!!.accountId)
        assertEquals(7.0, submitted!!.amount, 0.0)
        assertEquals(keypad, bounds("EntryKeypad"))
    }

    @Test fun incomeUsesCategoryThenAsset_andPreservesTheCreditSign() {
        show(TransactionMode.INCOME)
        chooseCategory("Salary")
        openAccountSheet()
        chooseAccount(3)
        apply("7")
        assertEquals("Salary", submitted!!.category)
        assertEquals(3L, submitted!!.accountId)
        assertEquals(-7.0, submitted!!.amount, 0.0)
    }

    @Test fun transferStartsWithSource_thenDestination_excludesSource_andClearsInvalidPrefill() {
        show(TransactionMode.TRANSFER, prefill = 2)
        rule.onNodeWithTag("EntryAccount_card").assertIsDisplayed()
        rule.onNodeWithTag("EntryDestination_card").assertIsDisplayed()
        openAccountSheet()
        chooseAccount(2)
        openDestinationSheet()
        // Account 2 is excluded from destination options
        rule.onNodeWithTag("EntryDestination_row_2").assertDoesNotExist()
        rule.onNodeWithTag("EntrySubmit").assertIsNotEnabled()
        chooseDestination(14)
        apply("7")
        assertEquals(2L, submitted!!.accountId)
        assertEquals(14L, submitted!!.toAccountId)
        assertEquals(7.0, submitted!!.amount, 0.0)
    }

    @Test fun currentStageRestores_andBackCancellationStaysClosedAfterRestoration_andModesRestart() {
        val restoration = StateRestorationTester(rule)
        show(restoration = restoration)
        chooseCategory("Food")
        openAccountSheet()
        pressBack()
        noSheet()
        restoration.emulateSavedInstanceStateRestore()
        rule.waitForIdle()
        noSheet()
        rule.onNodeWithTag("TransactionMode_INCOME").performScrollTo().performClick()
        noSheet()
        chooseCategory("Salary")
        rule.onNodeWithTag("EntryCategory_row_Salary").assertExists()
        rule.onNodeWithTag("TransactionMode_TRANSFER").performScrollTo().performClick()
        noSheet()
        rule.onNodeWithTag("EntryAccount_card").assertIsDisplayed()
        rule.onNodeWithTag("EntryDestination_card").assertIsDisplayed()
        assertNull(submitted)
    }

    @Test fun wideDarkLargeFontPanelsStillHaveAccountSheetUsable() {
        show(wideLarge = true)
        chooseCategory("Food")
        openAccountSheet()
        chooseAccount(1)
        noSheet()
        assertNull(submitted)
    }

    @Test fun emptyAccountsStillOfferAdd_afterTheCategoryStep() {
        show(empty = true)
        chooseCategory("Food")
        openAccountSheet()
        rule.onNodeWithTag("AccountPicker_AddAccountToggle").assertIsDisplayed().performClick()
        pressBack()
        rule.onNodeWithTag("EntryAssetSection").assertExists()
        rule.onNodeWithTag("EntrySubmit").assertIsNotEnabled()
        assertNull(submitted)
    }

    @Test fun aSingleTransferAccountCanReachAndDismissTheEmptyDestinationPanel() {
        show(TransactionMode.TRANSFER, single = true)
        openDestinationSheet()
        rule.onNodeWithTag("EntryDestination_row_1").assertDoesNotExist()
        rule.onNodeWithTag("EntrySubmit").assertIsNotEnabled()
        pressBack()
        noSheet()
        assertNull(submitted)
    }
}
