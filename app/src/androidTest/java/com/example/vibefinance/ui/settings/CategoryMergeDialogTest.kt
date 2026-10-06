package com.example.vibefinance.ui.settings

import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vibefinance.data.entity.*
import com.example.vibefinance.theme.VibeFinanceTheme
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.history.HistoryScreen
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

/** ARTEMIS verified Settings -> source -> target -> review -> cancel on Waydroid.
 * Dynamic Compose tags/text replace coordinates. Fixtures remain entirely in memory. */
@RunWith(AndroidJUnit4::class)
class CategoryMergeDialogTest {
    @get:Rule val rule = createComposeRule()
    private var callbacks = 0
    private val state = mutableStateOf(FinanceUiState(
        transactions = listOf(
            TransactionEntity(901, 12.6, "Food", 1000, 1),
            TransactionEntity(902, 20.0, "Food", Long.MAX_VALUE, 1),
            TransactionEntity(903, 15.0, "Transport", 1000, 1),
            TransactionEntity(904, -300.0, "Food", 1000, 1),
            TransactionEntity(905, 10.0, "Food", 1000, 1, toAccountId = 2)),
        subscriptions = listOf(SubscriptionEntity(901, "Monthly", 10.0, "Food", "Monthly", 2000, 1)),
        categoryLimits = mapOf("Food" to 100.0, "Transport" to 50.0, "Other" to 20.0)))
    private fun show(locale: Locale = Locale.ENGLISH, dark: Boolean = false, font: Float = 1f) {
        rule.setContent {
            val original = LocalContext.current
            val configuration = Configuration(LocalConfiguration.current).apply { setLocales(LocaleList(locale)); fontScale = font }
            val localized = remember(locale, font) { original.createConfigurationContext(configuration) }
            CompositionLocalProvider(LocalContext provides localized, LocalConfiguration provides configuration,
                LocalResources provides localized.resources, LocalDensity provides Density(LocalDensity.current.density, font)) {
                VibeFinanceTheme(darkTheme = dark, dynamicColorEnabled = false) {
                    CategoryMergeDialog(state.value, onMerge = { kind, sources, target ->
                        callbacks++
                        val s = state.value
                        val result = planCategoryMerge(kind, sources, target, s.transactions, s.subscriptions, s.categoryLimits, s.categoryMergeRules)
                        state.value = s.copy(transactions = result.transactions, subscriptions = result.subscriptions,
                            categoryLimits = result.limits, categoryMergeRules = result.rules)
                    }, onDismiss = {})
                }
            }
        }
        rule.waitUntil(5000) { rule.onAllNodesWithTag("ChooseMergeTarget").fetchSemanticsNodes().isNotEmpty() }
    }
    private fun source(category: String) = rule.onNodeWithTag("MergeSource:$category")
    private fun select(category: String) { source(category).performScrollTo().performClick(); rule.waitForIdle() }
    private fun target(category: String) {
        rule.onNodeWithTag("ChooseMergeTarget").performClick()
        rule.waitUntil(5000) { rule.onAllNodesWithTag("MergeTarget:$category").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("MergeTarget:$category").performScrollTo().performClick(); rule.waitForIdle()
    }
    private fun review() {
        rule.onNodeWithTag("ReviewCategoryMerge").assertIsEnabled().performClick()
        rule.waitUntil(5000) { rule.onAllNodesWithTag("ConfirmCategoryMerge").fetchSemanticsNodes().isNotEmpty() }
    }
    @Test fun selectionRequiresDistinctTargetAndCancelDoesNotMutateRecords() {
        show(); val original = state.value
        rule.onNodeWithTag("ReviewCategoryMerge").assertIsNotEnabled()
        select("Food"); rule.onNodeWithTag("ReviewCategoryMerge").assertIsNotEnabled()
        target("Other"); source("Other").assertIsNotEnabled()
        review(); assertEquals(0, callbacks)
        rule.onNodeWithText("Cancel").performClick(); rule.waitForIdle()
        assertEquals(0, callbacks); assertEquals(original, state.value)
        source("Food").assertIsOn()
    }
    @Test fun confirmedMultiSourceMergeUpdatesImpactLimitsAndHidesRetiredChoices() {
        show(); select("Food"); select("Transport"); target("Other"); review()
        rule.onNodeWithText("Updates 3 transactions and 1 recurring items, including future installments.").assertExists()
        rule.onNodeWithText("Combined category budget: HK$170.00").assertExists()
        rule.onNodeWithTag("ConfirmCategoryMerge").performClick()
        rule.waitUntil(5000) { callbacks == 1 && state.value.categoryMergeRules.expense.size == 2 }
        rule.waitForIdle()
        source("Food").assertDoesNotExist(); source("Transport").assertDoesNotExist()
        rule.onNodeWithTag("ReviewCategoryMerge").assertIsNotEnabled()
        assertEquals(170.0, state.value.categoryLimits.getValue("Other"), 0.0)
        assertEquals("Food", state.value.transactions[3].category) // Income independent.
        assertEquals("Food", state.value.transactions[4].category) // Transfer untouched.
    }
    @Test fun selectedSourcesAreExcludedFromTargetMenu() {
        show(); select("Food"); select("Transport")
        rule.onNodeWithTag("ChooseMergeTarget").performClick(); rule.waitForIdle()
        rule.onNodeWithTag("MergeTarget:Food").assertDoesNotExist()
        rule.onNodeWithTag("MergeTarget:Transport").assertDoesNotExist()
        rule.onNodeWithTag("MergeTarget:Other").assertExists()
    }
    @Test fun switchingToIncomeClearsExpenseSelectionAndKeepsExpenseRecords() {
        show(); select("Food"); target("Other")
        rule.onNodeWithText("Income").performClick(); rule.waitForIdle()
        rule.onNodeWithTag("ReviewCategoryMerge").assertIsNotEnabled()
        select("Food"); target("Salary"); review()
        rule.onNodeWithTag("ConfirmCategoryMerge").performClick()
        rule.waitUntil(5000) { callbacks == 1 }
        assertEquals("Salary", state.value.transactions[3].category)
        assertEquals("Food", state.value.transactions[0].category)
        assertEquals(100.0, state.value.categoryLimits.getValue("Food"), 0.0)
    }
    @Test fun traditionalChineseSearchUsesLocalizedCategoryLabels() {
        show(Locale.forLanguageTag("zh-Hant-HK"))
        rule.onNodeWithText("合併分類").assertExists()
        rule.onNodeWithTag("CategoryMergeSearch").performTextInput("交通")
        rule.waitForIdle()
        source("Transport").assertExists(); source("Food").assertDoesNotExist()
        rule.onNodeWithText("交通出行").assertExists()
        rule.onNodeWithText("Transport").assertDoesNotExist()
    }
    @Test fun busyStateBlocksSelectionReviewAndClose() {
        state.value = state.value.copy(isMergingCategories = true)
        show(dark = true)
        source("Food").assertIsNotEnabled()
        rule.onNodeWithTag("ChooseMergeTarget").assertIsNotEnabled()
        rule.onNodeWithTag("ReviewCategoryMerge").assertIsNotEnabled()
        rule.onNodeWithContentDescription("Back").assertIsNotEnabled()
        assertEquals(0, callbacks)
    }
    private fun showHistory(alreadyMerged: Boolean) {
        val records = state.value.transactions.take(3).map { it.copy(timestamp = System.currentTimeMillis() - 1000) }
        val initial = state.value.copy(isLoading = false, transactions = records)
        state.value = if (alreadyMerged) {
            val merged = planCategoryMerge(CategoryKind.EXPENSE, setOf("Food"), "Other", records,
                initial.subscriptions, initial.categoryLimits, initial.categoryMergeRules)
            initial.copy(transactions = merged.transactions, categoryMergeRules = merged.rules,
                subscriptions = merged.subscriptions, categoryLimits = merged.limits)
        } else initial
        rule.setContent { VibeFinanceTheme(darkTheme = false) { HistoryScreen(state.value, {}, categoryFilter = "Food") } }
        rule.waitForIdle()
    }
    @Test fun historyRetiredCategoryLinkResolvesToMergedCategory() {
        showHistory(alreadyMerged = true)
        rule.onNodeWithTag("CategoryAnalyticsSegment_Other").assertIsSelected()
        rule.onNodeWithTag("HistoryList").performScrollToNode(hasTestTag("TransactionRow_901"))
        rule.onNodeWithTag("TransactionRow_901").assertIsDisplayed()
    }
    @Test fun openHistoryFilterFollowsMergeWithoutLosingItsRecords() {
        showHistory(alreadyMerged = false)
        rule.onNodeWithTag("CategoryAnalyticsSegment_Food").assertIsSelected()
        rule.runOnIdle {
            val s = state.value
            val merged = planCategoryMerge(CategoryKind.EXPENSE, setOf("Food"), "Other", s.transactions,
                s.subscriptions, s.categoryLimits, s.categoryMergeRules)
            state.value = s.copy(transactions = merged.transactions, subscriptions = merged.subscriptions,
                categoryLimits = merged.limits, categoryMergeRules = merged.rules)
        }
        rule.waitUntil(5000) { rule.onAllNodesWithTag("CategoryAnalyticsSegment_Other").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("CategoryAnalyticsSegment_Other").assertIsSelected()
        rule.onNodeWithTag("HistoryList").performScrollToNode(hasTestTag("TransactionRow_901"))
        rule.onNodeWithTag("TransactionRow_901").assertIsDisplayed()
    }
    @Test fun largeTextKeepsReviewAccessibleAndTargetsScrollable() {
        show(font = 2f)
        rule.onNodeWithTag("ReviewCategoryMerge").assertIsDisplayed()
        select("Food"); target("Other"); review()
        rule.onNodeWithTag("ConfirmCategoryMerge").assertIsDisplayed()
    }
}
