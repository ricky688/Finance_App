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

/** Automatic Category -> Asset and From -> Destination explored with ARTEMIS/ADB before authoring. */
@RunWith(AndroidJUnit4::class)
class GuidedEntrySelectorTest {
    @get:Rule val rule = createComposeRule()
    private val accounts = (1L..14L).map { AccountEntity(it, "Account $it", type = AccountType.BANK, balance = 1000.0, icon = "bank") }
    private var submitted: FinanceIntent.AddTransaction? = null
    private var submissions = 0
    private val prefixes = listOf("EntryCategory", "EntryAccount", "EntryDestination")
    private fun show(mode: TransactionMode = TransactionMode.EXPENSE, restoration: StateRestorationTester? = null,
        wideLarge: Boolean = false, empty: Boolean = false, prefill: Long? = null, single: Boolean = false) {
        val content: @Composable () -> Unit = {
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
        panel(if (mode == TransactionMode.TRANSFER) "EntryAccount" else "EntryCategory")
    }
    private fun panel(prefix: String) {
        rule.waitUntil(8_000) { rule.onAllNodesWithTag("${prefix}_expanded").fetchSemanticsNodes().size == 1 &&
            prefixes.filter { it != prefix }.all { rule.onAllNodesWithTag("${it}_expanded").fetchSemanticsNodes().isEmpty() } }
        rule.waitForIdle()
        rule.onNodeWithTag("${prefix}_expanded").assertIsDisplayed()
    }
    private fun noPanel() {
        rule.waitUntil(8_000) { prefixes.all { rule.onAllNodesWithTag("${it}_expanded").fetchSemanticsNodes().isEmpty() } }
    }
    private fun choose(prefix: String, key: String) {
        rule.onNodeWithTag("${prefix}_grid_$key").performScrollTo().performClick()
    }
    private fun bounds(tag: String): Rect {
        val coordinates = rule.onNodeWithTag(tag).fetchSemanticsNode().layoutInfo.coordinates
        return Rect(coordinates.localToScreen(Offset.Zero), coordinates.size.toSize())
    }
    private fun scrollValue() = rule.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange) and
        hasAnyAncestor(hasTestTag("EntryScrollContent")))
        .fetchSemanticsNode().config[SemanticsProperties.VerticalScrollAxisRange].value()
    private fun threeColumns(prefix: String) {
        val tiles = rule.onAllNodes(SemanticsMatcher("$prefix grid choices") {
            it.config.getOrNull(SemanticsProperties.TestTag)?.startsWith("${prefix}_grid_") == true &&
                it.config.getOrNull(SemanticsProperties.Selected) != null
        }, useUnmergedTree = true).fetchSemanticsNodes().map {
            val c = it.layoutInfo.coordinates
            Rect(c.localToScreen(Offset.Zero), c.size.toSize())
        }.sortedWith(compareBy({ it.top }, { it.left }))
        assertTrue(tiles.size >= 4)
        assertEquals(tiles[0].top, tiles[1].top, 1f); assertEquals(tiles[1].top, tiles[2].top, 1f)
        assertTrue(tiles[0].right < tiles[1].left && tiles[1].right < tiles[2].left)
        assertEquals(tiles[0].width, tiles[2].width, 1f)
        assertTrue("Fourth tile begins the next row", tiles[3].top >= tiles[0].bottom)
    }
    private fun apply() {
        noPanel()
        assertNull(submitted)
        rule.onNodeWithTag("EntryKey_7").performClick()
        rule.onNodeWithTag("EntrySubmit").performClick()
        assertEquals(1, submissions)
    }

    @Test fun expenseStartsWithCategory_thenScrollsToAsset_andReturnsToFixedKeypad() {
        show()
        val keypad = bounds("EntryKeypad")
        val rowHeight = bounds("EntryAccount_row").height
        val beforeScroll = scrollValue()
        threeColumns("EntryCategory")
        choose("EntryCategory", "Utilities"); panel("EntryAccount")
        threeColumns("EntryAccount")
        assertTrue(scrollValue() > beforeScroll)
        val viewport = bounds("EntryScrollContent")
        val row = bounds("EntryAccount_row")
        assertTrue(row.top >= viewport.top - 1 && row.bottom <= viewport.bottom + 1)
        assertEquals(rowHeight, row.height, 1f)
        assertEquals(keypad, bounds("EntryKeypad"))
        choose("EntryAccount", "14"); apply()
        assertEquals("Utilities", submitted!!.category); assertEquals(14L, submitted!!.accountId)
        assertEquals(7.0, submitted!!.amount, 0.0); assertEquals(keypad, bounds("EntryKeypad"))
    }
    @Test fun incomeUsesCategoryThenAsset_andPreservesTheCreditSign() {
        show(TransactionMode.INCOME)
        choose("EntryCategory", "Salary"); panel("EntryAccount")
        choose("EntryAccount", "3"); apply()
        assertEquals("Salary", submitted!!.category); assertEquals(3L, submitted!!.accountId)
        assertEquals(-7.0, submitted!!.amount, 0.0)
    }
    @Test fun transferStartsWithSource_thenDestination_excludesSource_andClearsInvalidPrefill() {
        show(TransactionMode.TRANSFER, prefill = 2)
        threeColumns("EntryAccount")
        choose("EntryAccount", "2"); panel("EntryDestination")
        threeColumns("EntryDestination")
        rule.onNodeWithTag("EntryDestination_grid_2").assertDoesNotExist()
        rule.onNodeWithTag("EntrySubmit").assertIsNotEnabled()
        choose("EntryDestination", "14"); apply()
        assertEquals(2L, submitted!!.accountId); assertEquals(14L, submitted!!.toAccountId)
        assertEquals(7.0, submitted!!.amount, 0.0)
    }
    @Test fun currentStageRestores_andBackCancellationStaysClosedAfterRestoration_andModesRestart() {
        val restoration = StateRestorationTester(rule)
        show(restoration = restoration)
        choose("EntryCategory", "Food"); panel("EntryAccount")
        restoration.emulateSavedInstanceStateRestore(); panel("EntryAccount")
        pressBack(); noPanel()
        restoration.emulateSavedInstanceStateRestore(); rule.waitForIdle(); noPanel()
        rule.onNodeWithTag("TransactionMode_INCOME").performScrollTo().performClick(); panel("EntryCategory")
        rule.onNodeWithTag("EntryCategory_grid_Salary").assertExists()
        pressBack(); noPanel()
        rule.onNodeWithTag("TransactionMode_TRANSFER").performScrollTo().performClick(); panel("EntryAccount")
        assertNull(submitted)
    }
    @Test fun wideDarkLargeFontPanelsStillHaveExactlyThreeColumns() {
        show(wideLarge = true)
        threeColumns("EntryCategory")
        choose("EntryCategory", "Food"); panel("EntryAccount")
        threeColumns("EntryAccount")
        choose("EntryAccount", "1"); noPanel()
        assertNull(submitted)
    }
    @Test fun emptyAccountsStillOfferAdd_afterTheCategoryStep() {
        show(empty = true)
        choose("EntryCategory", "Food"); panel("EntryAccount")
        rule.onNodeWithTag("EntryAccount_grid_add").assertIsDisplayed().performClick()
        noPanel()
        rule.onNodeWithTag("EntryAssetSection").assertExists()
        rule.onNodeWithTag("EntrySubmit").assertIsNotEnabled()
        assertNull(submitted)
    }
    @Test fun aSingleTransferAccountCanReachAndDismissTheEmptyDestinationPanel() {
        show(TransactionMode.TRANSFER, single = true)
        choose("EntryAccount", "1"); panel("EntryDestination")
        rule.onNodeWithTag("EntryDestination_grid_1").assertDoesNotExist()
        rule.onNodeWithTag("EntrySubmit").assertIsNotEnabled()
        pressBack(); noPanel()
        assertNull(submitted)
    }

}
