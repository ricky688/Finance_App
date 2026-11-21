package com.example.vibefinance.ui.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vibefinance.data.entity.*
import com.example.vibefinance.ui.*
import com.example.vibefinance.ui.history.HistoryScreen
import com.example.vibefinance.ui.home.WholeBudgetCard
import java.time.LocalDate
import java.time.ZoneId
import org.junit.*
import org.junit.runner.RunWith

/** New search/filter, motion/blur, amount privacy and app-lock setup were explored with ARTEMIS first. */
@RunWith(AndroidJUnit4::class)
class ExperienceFeaturesTest {
    @get:Rule val rule = createComposeRule()
    private fun at(date: String) = LocalDate.parse(date).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    /** Types into the expanded M3E search view (focusing the pinned bar expands it), then collapses it. */
    private fun searchFor(text: String) {
        rule.onAllNodesWithTag("HistorySearch").onFirst().performClick()
        rule.waitUntil(5_000) {
            rule.onAllNodesWithTag("HistorySearchNavBack").fetchSemanticsNodes().isNotEmpty() &&
                rule.onAllNodes(hasTestTag("HistorySearch") and hasSetTextAction()).fetchSemanticsNodes().isNotEmpty()
        }
        rule.onAllNodes(hasTestTag("HistorySearch") and hasSetTextAction()).onLast().performTextReplacement(text)
        rule.waitForIdle()
        androidx.test.espresso.Espresso.closeSoftKeyboard()
        androidx.test.espresso.Espresso.pressBack()
        rule.waitUntil(5_000) {
            rule.onAllNodesWithTag("HistorySearchNavBack").fetchSemanticsNodes().isEmpty() &&
                rule.onAllNodesWithTag("HistorySearchExpandedPanel").fetchSemanticsNodes().isEmpty()
        }
    }
    @Test fun searchAccountCategoryAmountAndDateIntersectAndClearTogether() {
        val accounts = listOf(AccountEntity(id = 1, name = "Daily Visa", type = AccountType.CC, balance = 10.0, icon = "cc"), AccountEntity(id = 2, name = "Cash", type = AccountType.CASH, balance = 20.0, icon = "wallet"))
        val rows = listOf(TransactionEntity(1, 10.0, "Food", at("2026-09-01"), 1, description = "Coffee alpha"),
            TransactionEntity(2, 20.0, "Food", at("2026-09-02"), 2, description = "Coffee beta"),
            TransactionEntity(3, 30.0, "Transport", at("2026-09-01"), 1, description = "Train"))
        rule.setContent { MaterialTheme { HistoryScreen(FinanceUiState(isLoading = false, accounts = accounts, transactions = rows), {}) } }
        fun count(number: Int) {
            val tag = if (rule.onAllNodesWithTag("HistoryFiltersSheet").fetchSemanticsNodes().isNotEmpty()) "HistoryFilterResultsCount" else "HistoryResultsCount"
            rule.waitUntil(5_000) { rule.onAllNodesWithTag(tag).fetchSemanticsNodes().size == 1 }
            rule.onNodeWithTag(tag).assertTextContains("$number", substring = true)
        }
        // Redesign item 3: the M3E search bar is always present; amount/date filters live in "More".
        fun openSheet() {
            rule.onNodeWithTag("HistoryChip_More").performScrollTo().performClick()
            rule.waitUntil(5_000) { rule.onAllNodesWithTag("HistoryFiltersSheet").fetchSemanticsNodes().isNotEmpty() }
        }
        fun closeSheet() {
            rule.onNodeWithTag("HistoryFiltersClose").performClick()
            rule.waitUntil(5_000) { rule.onAllNodesWithTag("HistoryFiltersSheet").fetchSemanticsNodes().isEmpty() }
        }
        rule.onNodeWithTag("HistorySearchBar").assertExists()
        openSheet(); count(3); closeSheet()
        searchFor("coffee")
        openSheet(); count(2)
        rule.onNodeWithTag("HistoryAccountFilter").performScrollTo().performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("HistoryAccountFilter_1").fetchSemanticsNodes().size == 1 }
        rule.onNodeWithTag("HistoryAccountFilter_1").performClick(); count(1)
        rule.onNodeWithTag("HistoryCategoryFilter").performScrollTo().performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("HistoryCategoryFilter_1").fetchSemanticsNodes().size == 1 }
        rule.onNodeWithTag("HistoryCategoryFilter_1").performClick(); count(1)
        rule.onNodeWithTag("HistoryMinimum").performScrollTo().performTextInput("15"); count(0)
        rule.onNodeWithTag("HistoryMinimum").performTextReplacement("0"); count(1)
        rule.onNodeWithTag("HistoryFrom").performScrollTo().performTextInput("2026-09-02"); count(0)
        androidx.test.espresso.Espresso.closeSoftKeyboard()
        rule.onNodeWithTag("HistoryClearFilters").performScrollTo().performClick(); count(3)
        closeSheet()
        // Clearing also empties the pinned search field.
        rule.waitUntil(5_000) {
            rule.onAllNodes(hasTestTag("HistorySearch") and hasText("coffee", substring = true)).fetchSemanticsNodes().isEmpty()
        }
        rule.onNodeWithTag("HistorySearchBar").assertExists()
    }
    /** Run with -e historyWorkbook true after copying the supplied workbook to targetContext.externalCacheDir/history-test.xlsx.
     * Uses the production importer and History screen in memory; never imports into the saved ledger.
     */
    @Test fun suppliedWorkbookInlineSearchKeepsCombinedFiltersAcrossCloseAndReopen() {
        val instrumentation = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
        org.junit.Assume.assumeTrue("Opt in with -e historyWorkbook true", androidx.test.platform.app.InstrumentationRegistry.getArguments().getString("historyWorkbook") == "true")
        val file = java.io.File(requireNotNull(instrumentation.targetContext.externalCacheDir), "history-test.xlsx")
        Assert.assertTrue("Copy the workbook fixture before running", file.isFile)
        val imported = file.inputStream().use { com.example.vibefinance.util.FinancialDataImportEngine.parseStream(it, file.name) }
        Assert.assertNull(imported.error)
        Assert.assertEquals(868, imported.rawTransactions.size)
        Assert.assertEquals(14, imported.detectedAccounts.size)
        val accounts = imported.detectedAccounts.mapIndexed { index, raw -> AccountEntity(id = index + 1L,
            name = raw.name, type = raw.detectedType, balance = 0.0, icon = "wallet") }
        val ids = accounts.associate { it.name to it.id }
        val rows = imported.rawTransactions.mapIndexed { index, raw -> TransactionEntity(index + 1L, raw.amount,
            raw.category, raw.timestamp, ids.getValue(raw.sourceAccountName),
            if (raw.isTransfer) ids[raw.destinationAccountName] else null, description = raw.description) }
        Assert.assertEquals(52831.49, rows.filter { it.amount > 0 && it.toAccountId == null }.sumOf { it.amount }, .001)
        val categories = rows.map { it.category }.distinct().sorted()
        val target = rows.first { it.amount > 0 && it.toAccountId == null && it.description.isNotBlank() }
        val query = com.example.vibefinance.ui.history.HistoryQuery(text = target.description, account = target.accountId,
            category = target.category, minimum = target.amount.toString(), maximum = target.amount.toString())
        val expected = query.apply(rows, accounts).size
        rule.setContent { MaterialTheme { HistoryScreen(FinanceUiState(isLoading = false, accounts = accounts, transactions = rows), {}) } }
        val allTime = instrumentation.targetContext.getString(com.example.vibefinance.R.string.category_analytics_all_time)
        rule.onNode(hasText(allTime) and hasAnyAncestor(hasTestTag("CategoryAnalyticsRange"))).performClick()
        rule.onNodeWithTag("CategoryAnalyticsTotal").assertTextEquals("HK$ 52,831.49")
        fun count(number: Int) {
            val tag = if (rule.onAllNodesWithTag("HistoryFiltersSheet").fetchSemanticsNodes().isNotEmpty()) "HistoryFilterResultsCount" else "HistoryResultsCount"
            rule.waitUntil(5_000) { rule.onAllNodesWithTag(tag).fetchSemanticsNodes().size == 1 }
            rule.onNodeWithTag(tag).assertTextContains("$number", substring = true)
        }
        fun openSheet() {
            rule.onNodeWithTag("HistoryChip_More").performScrollTo().performClick()
            rule.waitUntil(5_000) { rule.onAllNodesWithTag("HistoryFiltersSheet").fetchSemanticsNodes().isNotEmpty() }
        }
        openSheet(); count(868)
        rule.onNodeWithTag("HistoryFiltersClose").performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("HistoryFiltersSheet").fetchSemanticsNodes().isEmpty() }
        searchFor(target.description)
        openSheet()
        rule.onNodeWithTag("HistoryAccountFilter").performScrollTo().performClick()
        val accountIndex = accounts.indexOfFirst { it.id == target.accountId } + 1
        rule.onNodeWithTag("HistoryAccountFilter_$accountIndex").performScrollTo().performClick()
        rule.onNodeWithTag("HistoryCategoryFilter").performScrollTo().performClick()
        rule.onNodeWithTag("HistoryCategoryFilter_${categories.indexOf(target.category) + 1}").performScrollTo().performClick()
        rule.onNodeWithTag("HistoryMinimum").performScrollTo().performTextReplacement(query.minimum)
        rule.onNodeWithTag("HistoryMaximum").performScrollTo().performTextReplacement(query.maximum)
        androidx.test.espresso.Espresso.closeSoftKeyboard()
        count(expected)
        rule.onNodeWithTag("HistoryFiltersClose").performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("HistoryFiltersSheet").fetchSemanticsNodes().isEmpty() }
        rule.onNodeWithTag("CategoryAnalyticsHint").assertTextContains("$expected", substring = true)
        rule.onAllNodesWithTag("HistorySearch").onFirst().assertTextContains(target.description)
        openSheet(); count(expected)
        rule.onNodeWithTag("HistoryClearFilters").performScrollTo().performClick(); count(868)
        rule.onNodeWithTag("HistoryFiltersClose").performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("HistoryFiltersSheet").fetchSemanticsNodes().isEmpty() }
        rule.onNodeWithTag("CategoryAnalyticsTotal").assertTextEquals("HK$ 52,831.49")
    }
    /** Redesign item 3: the M3E search bar is pinned above the cards, with filter chips under it. */
    @Test fun pinnedSearchBarAndChipsSitAboveTheBudgetCard() {
        val budget = com.example.vibefinance.data.repository.DailyBudgetInfo(100.0, 0.0, 100.0, 10.0, 10.0, 10,
            at("2026-09-01"), at("2026-09-30"))
        rule.setContent { MaterialTheme { HistoryScreen(FinanceUiState(isLoading = false, budgetInfo = budget), {}) } }
        rule.waitForIdle()
        val bar = rule.onNodeWithTag("HistorySearchBar").fetchSemanticsNode().boundsInRoot
        val chips = rule.onNodeWithTag("HistoryFilterChips").fetchSemanticsNode().boundsInRoot
        val card = rule.onNodeWithTag("HistoryBudgetPeriod").fetchSemanticsNode().boundsInRoot
        Assert.assertTrue("Chips sit under the search bar", chips.top >= bar.bottom)
        Assert.assertTrue("Search header sits above the period card", chips.bottom <= card.top)
    }
    @Test fun typeChipsFilterAndToggleBackUnderMinimalMotion() {
        val accounts = listOf(AccountEntity(id = 1, name = "Cash", type = AccountType.CASH, balance = 20.0, icon = "wallet"))
        val rows = listOf(TransactionEntity(1, 10.0, "Food", at("2026-09-01"), 1, description = "Lunch"),
            TransactionEntity(2, -50.0, "Salary", at("2026-09-02"), 1, description = "Pay"))
        rule.setContent { CompositionLocalProvider(LocalExperience provides ExperiencePreferences(motion = MotionLevel.MINIMAL)) {
            MaterialTheme { HistoryScreen(FinanceUiState(isLoading = false, accounts = accounts, transactions = rows), {}) }
        } }
        rule.waitForIdle()
        rule.onNodeWithTag("HistoryChip_ALL").assertIsSelected()
        rule.onNodeWithTag("HistoryChip_INCOME").performClick()
        rule.onNodeWithTag("HistoryChip_INCOME").assertIsSelected()
        rule.waitUntil(5_000) { rule.onAllNodes(hasText("Lunch")).fetchSemanticsNodes().isEmpty() }
        rule.onAllNodes(hasText("Pay")).onFirst().assertExists()
        // Tapping the selected type again returns to all records.
        rule.onNodeWithTag("HistoryChip_INCOME").performClick()
        rule.onNodeWithTag("HistoryChip_ALL").assertIsSelected()
        rule.waitUntil(5_000) { rule.onAllNodes(hasText("Lunch")).fetchSemanticsNodes().isNotEmpty() }
    }
    @Test fun hiddenAmountsDisappearFromTextSemanticsAndReturnOnReveal() {
        var hidden by mutableStateOf(false)
        rule.setContent { CompositionLocalProvider(LocalExperience provides ExperiencePreferences(hideAmounts = hidden)) {
            MaterialTheme { Column {
                WholeBudgetCard(modifier = Modifier.height(220.dp), budget = 12345.67, startDate = at("2026-09-01"), endDate = at("2026-09-30"))
                com.example.vibefinance.ui.components.RollingNumberText("HK$9.90", style = MaterialTheme.typography.bodyMedium)
                Button(onClick = { hidden = !hidden }) { androidx.compose.material3.Text("Toggle fixture privacy") }
            } }
        } }
        rule.onNodeWithText("$12,345.67").assertExists()
        rule.onNodeWithText("Toggle fixture privacy").assertIsDisplayed().performClick()
        rule.onNodeWithText("$12,345.67", useUnmergedTree = true).assertDoesNotExist()
        rule.onNodeWithText("9", useUnmergedTree = true).assertDoesNotExist()
        rule.onAllNodesWithText("••••", useUnmergedTree = true).assertCountEquals(2)
        rule.onNodeWithText("Toggle fixture privacy").assertIsDisplayed().performClick()
        rule.onNodeWithText("$12,345.67").assertExists()
    }
    @Test fun motionAndBlurControlsDispatchIndependentValuesAndUseSingleLineLabels() {
        var state by mutableStateOf(FinanceUiState(isLoading = false))
        rule.setContent { MaterialTheme { MotionBlurSettings(state) { intent ->
            state = when (intent) {
                is FinanceIntent.SetMotionLevel -> state.copy(motionLevel = intent.level)
                is FinanceIntent.SetBlurIntensity -> state.copy(blurIntensity = intent.intensity)
                else -> state
            }
        } } }
        rule.onNodeWithTag("Motion_MINIMAL").performClick().assertIsSelected()
        rule.onNodeWithTag("BlurIntensity").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.SetProgress) { it(.25f) }
        rule.runOnIdle { Assert.assertEquals(MotionLevel.MINIMAL, state.motionLevel); Assert.assertEquals(.25f, state.blurIntensity) }
        rule.onNodeWithTag("Motion_REDUCED").performClick().assertIsSelected()
        rule.onNodeWithTag("Motion_FULL").performClick().assertIsSelected()
    }
}
