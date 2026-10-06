package com.example.vibefinance.ui.history

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.data.repository.DailyBudgetInfo
import com.example.vibefinance.theme.VibeFinanceTheme
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.components.CategoryBreakdownCard
import com.example.vibefinance.ui.components.CategoryAnalyticsPeriodMode
import androidx.test.espresso.Espresso
import java.util.concurrent.atomic.AtomicInteger
import java.time.YearMonth
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Real chart/History interactions explored with ARTEMIS on Waydroid before authoring.
 * All fixtures stay in Compose memory; no ledger or preference is modified.
 */
@RunWith(AndroidJUnit4::class)
class CategoryAnalyticsInteractionTest {
    @get:Rule val rule = createComposeRule()
    private val selected = mutableStateOf<String?>(null)
    private val zone = ZoneId.systemDefault()
    private val month = YearMonth.now(zone)
    private fun date(offset: Long, day: Int = 2) = month.plusMonths(offset).atDay(day)
        .atStartOfDay(zone).toInstant().toEpochMilli()
    private val records get() = listOf(
        TransactionEntity(901, 120.0, "Food", date(0), 1, description = "Current food"),
        TransactionEntity(902, 70.0, "Transport", date(0, 3), 1, description = "Current transport"),
        TransactionEntity(903, 40.0, "Food", date(-2), 1, description = "Older food"),
        TransactionEntity(904, 30.0, "Food", date(-1), 2, description = "Other wallet"),
        TransactionEntity(905, -500.0, "Food", date(0), 1, description = "Income excluded"),
        TransactionEntity(906, 25.0, "Food", date(0), 1, toAccountId = 2, description = "Transfer excluded"),
        TransactionEntity(907, 15.0, "Food", date(0), 1, isBalanceAdjustment = true, description = "Adjustment excluded")
    )

    @Test fun shortTapAndAccessibilityActionSelectAndToggleOnlyThatCategory() {
        showCard()
        segment("Food").performTouchInput { click() }
        awaitSelection("Food")
        segment("Food").assertIsSelected()
        segment("Transport").assertIsNotSelected()
        segment("Food").performTouchInput { click() }
        awaitSelection(null)
        segment("Transport").performClick()
        awaitSelection("Transport")
        rule.onNodeWithTag("CategoryAnalyticsReset").performClick()
        awaitSelection(null)
    }

    @Test fun longHoldDriftReturnCancelAndMultipleFingersPreservePreviousSelection() {
        selected.value = "Food"
        showCard()
        val target = segment("Transport")
        target.performTouchInput {
            down(center)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis + 50)
            up()
        }
        awaitSelection("Food")
        target.performTouchInput {
            down(center)
            moveBy(Offset(-viewConfiguration.touchSlop * 2, 0f))
            moveTo(center)
            up()
        }
        awaitSelection("Food")
        target.performTouchInput { down(center); cancel() }
        awaitSelection("Food")
        target.performTouchInput {
            down(0, center)
            down(1, center + Offset(10f, 0f))
            up(1)
            up(0)
        }
        awaitSelection("Food")
        target.assertIsNotSelected()
        // A rejected gesture cannot poison the next intentional tap.
        target.performTouchInput { click() }
        awaitSelection("Transport")
    }

    @Test fun draggingBetweenChartCategoriesDoesNotSelectEitherOne() {
        showCard()
        rule.onNodeWithTag("CategoryAnalyticsBar").performTouchInput {
            swipe(Offset(width * 0.2f, center.y), Offset(width * 0.9f, center.y), 150)
        }
        awaitSelection(null)
        segment("Food").assertIsNotSelected()
        segment("Transport").assertIsNotSelected()
    }

    @Test fun verticalScrollBeginningOnChartRemainsAvailableWithoutChangingSelection() {
        selected.value = "Food"
        lateinit var scroll: ScrollState
        rule.setContent {
            VibeFinanceTheme(darkTheme = false) {
                scroll = rememberScrollState()
                Column(Modifier.width(360.dp).verticalScroll(scroll)) {
                    CategoryBreakdownCard(records, {}, selectedCategory = selected.value,
                        onSelectCategory = { selected.value = it })
                    Spacer(Modifier.height(1800.dp))
                }
            }
        }
        rule.waitForIdle()
        val before = scroll.value
        rule.onNodeWithTag("CategoryAnalyticsBar").performTouchInput {
            down(center)
            moveBy(Offset(0f, -viewConfiguration.touchSlop * 2))
            moveBy(Offset(0f, -180f))
            up()
        }
        rule.waitUntil(5000) { scroll.value > before }
        awaitSelection("Food")
    }

    @Test fun allTimeWithoutBudgetShowsOlderCategoryRecordsAndExcludesNonExpenses() {
        showHistory()
        range(R.string.category_analytics_month).assertIsSelected()
        selectHistoryCategory("Food")
        row(901).assertIsDisplayed()
        row(903).assertDoesNotExist()
        returnToChart()
        range(R.string.category_analytics_all_time).performClick().assertIsSelected()
        selectHistoryCategory("Food")
        for (id in listOf(901L, 903L, 904L)) {
            rule.onNodeWithTag("HistoryList").performScrollToNode(hasTestTag("TransactionRow_$id"))
            row(id).assertIsDisplayed()
        }
        for (id in listOf(902L, 905L, 906L, 907L)) row(id).assertDoesNotExist()
    }

    @Test fun allTimeRetainsAccountScope() {
        showHistory(accountId = 1)
        range(R.string.category_analytics_all_time).performClick()
        selectHistoryCategory("Food")
        rule.onNodeWithTag("HistoryList").performScrollToNode(hasTestTag("TransactionRow_903"))
        row(903).assertIsDisplayed()
        row(904).assertDoesNotExist()
    }

    @Test fun anEmptySelectedMonthDoesNotFallBackToOlderCategoryRecords() {
        val transactions = mutableStateOf(records)
        rule.setContent {
            VibeFinanceTheme(darkTheme = false) {
                HistoryScreen(FinanceUiState(isLoading = false, transactions = transactions.value), {})
            }
        }
        rule.waitForIdle()
        selectHistoryCategory("Food")
        rule.runOnIdle { transactions.value = records.filter { it.timestamp < date(0, 1) } }
        rule.waitForIdle()
        row(903).assertDoesNotExist()
        row(904).assertDoesNotExist()
        returnToChart()
        range(R.string.category_analytics_month).assertIsSelected()
        rule.onNodeWithTag("CategoryAnalyticsReset").assertIsDisplayed()
    }

    @Test fun budgetPeriodIncludesFinalDayAndAllTimeAddsRecordsOutsideIt() {
        val last = TransactionEntity(908, 10.0, "Food",
            date(0, month.lengthOfMonth()) + 23 * 3600000L, 1, description = "Final day expense")
        val budget = DailyBudgetInfo(1000.0, 200.0, 800.0, 50.0, 50.0, 20,
            date(0, 1), date(0, month.lengthOfMonth()))
        showHistory(budget = budget, transactions = records + last)
        range(R.string.category_analytics_budget_period).assertIsSelected()
        selectHistoryCategory("Food")
        rule.onNodeWithTag("HistoryList").performScrollToNode(hasTestTag("TransactionRow_908"))
        row(908).assertIsDisplayed()
        row(903).assertDoesNotExist()
        returnToChart()
        range(R.string.category_analytics_all_time).performClick()
        selectHistoryCategory("Food")
        rule.onNodeWithTag("HistoryList").performScrollToNode(hasTestTag("TransactionRow_903"))
        row(903).assertIsDisplayed()
    }

    @Test fun categoryDeepLinkStartsWithAllTimeScope() {
        showHistory(category = "Food")
        range(R.string.category_analytics_all_time).assertIsSelected()
        rule.onNodeWithTag("HistoryList").performScrollToNode(hasTestTag("TransactionRow_903"))
        row(903).assertIsDisplayed()
        row(902).assertDoesNotExist()
    }

    @Test fun englishTitleAndFilterHintWrapCompletelyOnNarrowScreenWithLargeText() {
        assertWrappedHeader("en")
    }

    @Test fun traditionalChineseTitleAndFilterHintWrapCompletelyWithLargeText() {
        assertWrappedHeader("zh-HK")
    }

    // These compact layouts, popup / picker flow, Add entry, budget date and older-record
    // action were explored with ARTEMIS / ADB on Waydroid before adding these tests.
    @Test fun overflowMenuRoutesImportAndExportOnceAndCanBeDismissed() {
        val imported = AtomicInteger()
        val exported = AtomicInteger()
        rule.setContent {
            VibeFinanceTheme(darkTheme = false) {
                Column(Modifier.width(360.dp).verticalScroll(rememberScrollState())) {
                    CategoryBreakdownCard(records, { exported.incrementAndGet() },
                        onImportData = { imported.incrementAndGet() })
                }
            }
        }
        rule.onNodeWithTag("CategoryAnalyticsImport").assertDoesNotExist()
        rule.onNodeWithTag("CategoryAnalyticsExport").assertDoesNotExist()
        openActions()
        rule.onNodeWithTag("CategoryAnalyticsImport").assertIsDisplayed().performClick()
        rule.waitUntil(5000) { imported.get() == 1 && menuIsClosed() }
        assertEquals(0, exported.get())
        openActions()
        rule.onNodeWithTag("CategoryAnalyticsExport").assertIsDisplayed().performClick()
        rule.waitUntil(5000) { exported.get() == 1 && menuIsClosed() }
        openActions()
        Espresso.pressBack()
        rule.waitUntil(5000) { menuIsClosed() }
        assertEquals(1, imported.get())
        assertEquals(1, exported.get())
    }

    @Test fun allTimeSummaryCountsOnlyScopedExpensesAndReplacesDateNavigation() {
        showHistory(accountId = 1)
        rule.onNodeWithTag("CategoryAnalyticsPreviousMonth").assertHeightIsAtLeast(48.dp)
        rule.onNodeWithTag("CategoryAnalyticsNextMonth").assertHeightIsAtLeast(48.dp)
        range(R.string.category_analytics_all_time).performClick()
        rule.onNodeWithTag("CategoryAnalyticsTotal").assertTextEquals("HK$ 230.00")
        rule.onNodeWithTag("CategoryAnalyticsRecordCount").assertTextEquals(
            InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.category_transactions_count, 3))
        rule.onNodeWithTag("CategoryAnalyticsPeriodLabel").assertDoesNotExist()
        rule.onNodeWithTag("CategoryAnalyticsPreviousMonth").assertDoesNotExist()
        rule.onNodeWithTag("CategoryAnalyticsNextMonth").assertDoesNotExist()
        range(R.string.category_analytics_month).performClick()
        rule.onNodeWithTag("CategoryAnalyticsAllTimeSummary").assertDoesNotExist()
        rule.onNodeWithTag("CategoryAnalyticsPeriodLabel").assertIsDisplayed()
    }

    @Test fun emptyHistoryHasOneExplanationAndWorkingAddEntry() {
        val added = AtomicInteger()
        showHistory(transactions = emptyList(), onAdd = { added.incrementAndGet() })
        rule.onAllNodesWithTag("HistoryEmptyState").assertCountEquals(1)
        rule.onNodeWithTag("HistoryListEmptyCard").assertDoesNotExist()
        rule.onNodeWithTag("HistoryEmptyMessage").assertTextEquals(
            InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.no_transactions))
        rule.onNodeWithTag("HistoryEmptyAdd").assertIsDisplayed().assertHeightIsAtLeast(48.dp).performClick()
        rule.waitUntil(5000) { added.get() == 1 }
        range(R.string.category_analytics_all_time).performClick()
        rule.onAllNodesWithTag("HistoryEmptyState").assertCountEquals(1)
        rule.onNodeWithTag("CategoryAnalyticsTotal").assertTextEquals("HK$ 0.00")
        rule.onNodeWithTag("CategoryAnalyticsRecordCount").assertTextEquals(
            InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.category_transactions_count, 0))
    }

    @Test fun viewAllFromEmptyMonthRevealsOlderRecordsWithinTheSameAccount() {
        showHistory(accountId = 1, transactions = records.filter { it.timestamp < date(0, 1) })
        range(R.string.category_analytics_month).assertIsSelected()
        rule.onNodeWithTag("HistoryEmptyViewAll").assertIsDisplayed().performClick()
        range(R.string.category_analytics_all_time).assertIsSelected()
        rule.onNodeWithTag("HistoryEmptyState").assertDoesNotExist()
        rule.onNodeWithTag("CategoryAnalyticsTotal").assertTextEquals("HK$ 40.00")
        rule.onNodeWithTag("HistoryList").performScrollToNode(hasTestTag("TransactionRow_903"))
        row(903).assertIsDisplayed()
        row(904).assertDoesNotExist()
    }

    @Test fun budgetDateWrapsCompletelyAtNarrowWidthAndLargeText() {
        val label = "1 October 2026 – 31 October 2026"
        rule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 1.3f)) {
                VibeFinanceTheme(darkTheme = false) {
                    Column(Modifier.width(280.dp).verticalScroll(rememberScrollState())) {
                        CategoryBreakdownCard(emptyList(), {}, periodMode = CategoryAnalyticsPeriodMode.BUDGET_PERIOD,
                            periodLabel = label, hasBudgetPeriod = true, onSelectPeriod = {})
                    }
                }
            }
        }
        rule.onNodeWithTag("CategoryAnalyticsPeriodLabel").assertTextEquals(label)
        val layouts = mutableListOf<TextLayoutResult>()
        rule.onNodeWithTag("CategoryAnalyticsPeriodLabel").performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        assertFalse(layouts.single().hasVisualOverflow)
        assertTrue("A long date should wrap rather than truncate", layouts.single().lineCount > 1)
        rule.onNodeWithTag("CategoryAnalyticsPreviousMonth").assertDoesNotExist()
        rule.onNodeWithTag("CategoryAnalyticsNextMonth").assertDoesNotExist()
    }

    @Test fun emptyCategoryResultHasCompactSpacingAndViewAllClearsTheFilter() {
        showHistory(category = "Missing category")
        rule.onNodeWithTag("HistoryList").performScrollToNode(hasTestTag("HistoryListEmptyCard"))
        rule.onAllNodesWithTag("HistoryEmptyState").assertCountEquals(1)
        val card = rule.onNodeWithTag("CategoryAnalyticsCard").fetchSemanticsNode().boundsInRoot
        val empty = rule.onNodeWithTag("HistoryListEmptyCard").fetchSemanticsNode().boundsInRoot
        val density = InstrumentationRegistry.getInstrumentation().targetContext.resources.displayMetrics.density
        val gapDp = (empty.top - card.bottom) / density
        assertTrue("There should be one 12dp gap, not the former extra 40dp: $gapDp", gapDp in 11f..13f)
        rule.onNodeWithTag("HistoryEmptyViewAll").performClick()
        rule.waitUntil(5000) {
            rule.onAllNodesWithTag("HistoryListEmptyCard").fetchSemanticsNodes().isEmpty()
        }
        rule.onNodeWithTag("CategoryAnalyticsReset").assertDoesNotExist()
        rule.onNodeWithTag("HistoryList").performScrollToNode(hasTestTag("TransactionRow_901"))
        row(901).assertIsDisplayed()
    }

    private fun menuIsClosed() = rule.onAllNodesWithTag("CategoryAnalyticsActionsMenu").fetchSemanticsNodes().isEmpty()
    private fun openActions() {
        rule.onNodeWithTag("CategoryAnalyticsActions").assertHeightIsAtLeast(48.dp).performClick()
        rule.waitUntil(5000) { !menuIsClosed() }
    }

    private fun showCard() {
        rule.setContent {
            VibeFinanceTheme(darkTheme = false) {
                Column(Modifier.width(360.dp).verticalScroll(rememberScrollState())) {
                    CategoryBreakdownCard(records, {}, selectedCategory = selected.value,
                        onSelectCategory = { selected.value = it })
                }
            }
        }
        rule.waitForIdle()
    }
    private fun showHistory(accountId: Long? = null, budget: DailyBudgetInfo? = null,
        transactions: List<TransactionEntity> = records, category: String? = null, onAdd: (() -> Unit)? = null) {
        rule.setContent {
            VibeFinanceTheme(darkTheme = false) {
                HistoryScreen(FinanceUiState(isLoading = false, transactions = transactions, budgetInfo = budget),
                    {}, accountFilterId = accountId, categoryFilter = category, onAddTransaction = onAdd)
            }
        }
        rule.waitForIdle()
    }
    private fun range(resource: Int): SemanticsNodeInteraction {
        val text = InstrumentationRegistry.getInstrumentation().targetContext.getString(resource)
        return rule.onNode(hasText(text) and hasAnyAncestor(hasTestTag("CategoryAnalyticsRange")))
    }
    private fun selectHistoryCategory(category: String) {
        segment(category).performTouchInput { click() }
        segment(category).assertIsSelected()
        rule.onNodeWithTag("HistoryList").performScrollToNode(hasTestTag("TransactionRow_901"))
    }
    private fun returnToChart() {
        rule.onNodeWithTag("HistoryList").performScrollToNode(hasTestTag("CategoryAnalyticsTitle"))
    }
    private fun row(id: Long) = rule.onNodeWithTag("TransactionRow_$id")
    private fun segment(category: String) = rule.onNodeWithTag("CategoryAnalyticsSegment_$category")
    private fun awaitSelection(expected: String?) {
        rule.waitForIdle()
        rule.waitUntil(5000) { selected.value == expected }
    }
    private fun assertWrappedHeader(language: String) {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val config = Configuration(base.resources.configuration).apply { setLocale(Locale.forLanguageTag(language)) }
        val localized = base.createConfigurationContext(config)
        selected.value = "Transport"
        rule.setContent {
            CompositionLocalProvider(LocalContext provides localized, LocalConfiguration provides config,
                LocalDensity provides Density(LocalDensity.current.density, 1.3f)) {
                VibeFinanceTheme(darkTheme = false) {
                    Column(Modifier.width(280.dp).verticalScroll(rememberScrollState())) {
                        CategoryBreakdownCard(records, {}, selectedCategory = selected.value,
                            onSelectCategory = { selected.value = it }, onImportData = {},
                            hasBudgetPeriod = true, onSelectPeriod = {})
                    }
                }
            }
        }
        rule.waitForIdle()
        val bitmap = rule.onRoot().captureToImage()
        val file = java.io.File(base.getExternalFilesDir(null), "analytics-$language-narrow.png")
        java.io.FileOutputStream(file).use {
            bitmap.asAndroidBitmap().compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
        for (tag in listOf("CategoryAnalyticsTitle", "CategoryAnalyticsHint")) {
            val layouts = mutableListOf<TextLayoutResult>()
            rule.onNodeWithTag(tag).assertIsDisplayed().performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
            assertTrue("Text layout must be exposed: $tag", layouts.isNotEmpty())
            val layout = layouts.single()
            assertFalse("$language $tag clipped: text=${layout.layoutInput.text}, size=${layout.size}, " +
                "width=${layout.didOverflowWidth}, height=${layout.didOverflowHeight}, lines=${layout.lineCount}, " +
                "lastLineBottom=${layout.getLineBottom(layout.lineCount - 1)}", layout.hasVisualOverflow)
        }
        rule.onNodeWithTag("CategoryAnalyticsTitle").assertTextEquals(localized.getString(R.string.category_analytics_title))
    }
}
