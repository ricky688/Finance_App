package com.example.vibefinance.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import android.os.SystemClock
import android.view.MotionEvent
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.ui.FinanceIntent
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.R
import com.example.vibefinance.data.currency.ExchangeRateLoader
import com.example.vibefinance.data.currency.ExchangeRateQuote
import com.example.vibefinance.data.currency.ExchangeRateResult
import kotlinx.coroutines.CompletableDeferred
import com.example.vibefinance.data.repository.DailyBudgetInfo
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** ARTEMIS/ADB verified on Waydroid: toggle -> keypad overlay -> select/scroll -> collapse/Back.
 * Memory-only fixtures exercise the real form without modifying the installed financial dataset.
 */
@RunWith(AndroidJUnit4::class)
class EntrySelectorOverlayTest {
    @get:Rule val compose = createComposeRule()
    private val accounts = (1L..14L).map {
        AccountEntity(it, "Account $it", type = AccountType.BANK, balance = 100.0, icon = "bank")
    }
    private var submitted: FinanceIntent.AddTransaction? = null

    @Test fun emojiKeepTheirColorsInLightPanelsAndConnectedRows() = verifyEmojiColors(false)

    @Test fun emojiKeepTheirColorsInDarkPanelsAtLargeFontSize() = verifyEmojiColors(true)

    /** Gift and flag choices mirror emoji-prefixed categories observed on the imported device. */
    private fun verifyEmojiColors(dark: Boolean) {
        val labels = listOf("🎁 Gift", "🇭🇰 Hong Kong", "👩🏽‍💻 Work", "1️⃣ One")
        compose.setContent {
            val density = LocalDensity.current.density
            val overlay = remember { EntrySelectorOverlayState() }
            var selected by remember { mutableIntStateOf(1) }
            CompositionLocalProvider(LocalDensity provides Density(density, 1.6f), LocalEntrySelectorOverlay provides overlay) {
                MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                    Column {
                        ExpandableEntrySelector(labels, selected, { selected = it }, "Category", "Emoji", { it }, { it }, compactChoices = true)
                        Box(Modifier.fillMaxWidth().height(400.dp).onGloballyPositioned { overlay.keypadBounds = it.boundsInWindow() })
                    }
                }
            }
        }
        val gift = "🎁 Gift"
        open("Emoji")
        assertRedEmojiPixels("Emoji_grid_$gift")
        compose.mainClock.autoAdvance = false
        click("Emoji_grid_$gift")
        compose.mainClock.advanceTimeBy(80)
        assertRedEmojiPixels("Emoji_grid_$gift")
        compose.mainClock.advanceTimeBy(2_000)
        assertRedEmojiPixels("Emoji_grid_$gift")
        compose.onNodeWithTag("Emoji_grid_$gift").assertIsSelected()
        click("Emoji_collapse")
        compose.mainClock.advanceTimeBy(2_000)
        compose.mainClock.autoAdvance = true
        assertRowReveals("Emoji", gift)
        assertRedEmojiPixels("Emoji_row_$gift")
    }

    private fun assertRedEmojiPixels(tag: String) {
        val pixels = compose.onNodeWithTag(tag).captureToImage().toPixelMap()
        var redPixels = 0
        for (y in 0 until pixels.height) for (x in 0 until pixels.width) {
            val color = pixels[x, y]
            if (color.red > 0.65f && color.red > color.green * 1.4f && color.red > color.blue * 1.4f) redPixels++
        }
        assertTrue("$tag must retain the gift emoji's red pixels instead of a tinted silhouette", redPixels > 3)
    }

    private fun host(mode: TransactionMode, rateLoader: ExchangeRateLoader? = null, budget: DailyBudgetInfo? = null) {
        compose.setContent {
            MaterialTheme {
                AddExpenseSheetContent(FinanceUiState(isLoading = false, accounts = accounts, budgetInfo = budget),
                    onIntent = { if (it is FinanceIntent.AddTransaction) submitted = it },
                    onDismissAddDialog = {}, initialMode = mode,
                    categoryFrequency = emptyMap(), accountFrequency = emptyMap(), modifier = Modifier.fillMaxSize(),
                    exchangeRateLoader = rateLoader)
            }
        }
    }

    /** Dynamic tags first; device pointer fallback derives coordinates from the located bounds. */
    private fun click(tag: String) {
        val node = compose.onNodeWithTag(tag)
        try { node.performClick() } catch (failure: AssertionError) {
            val center = screenBounds(tag).center
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            val downTime = SystemClock.uptimeMillis()
            listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP).forEach { action ->
                val event = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), action, center.x, center.y, 0)
                try { instrumentation.sendPointerSync(event) } finally { event.recycle() }
            }
        }
        compose.waitForIdle()
    }

    private fun open(prefix: String) {
        val toggle = compose.onNodeWithTag("${prefix}_toggle")
        try { toggle.assertIsDisplayed() } catch (_: AssertionError) { toggle.performScrollTo() }
        click("${prefix}_toggle")
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("${prefix}_expanded").fetchSemanticsNodes().isNotEmpty() }
    }

    private fun screenBounds(tag: String): Rect {
        val coordinates = compose.onNodeWithTag(tag).fetchSemanticsNode().layoutInfo.coordinates
        return Rect(coordinates.localToScreen(Offset.Zero), coordinates.size.toSize())
    }

    /** Exercise Android window dispatch, not only Compose's semantic onClick action. */
    private fun physicalPress(tag: String, holdMillis: Long) {
        val center = screenBounds(tag).center
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val downTime = SystemClock.uptimeMillis()
        fun send(action: Int) {
            val event = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), action, center.x, center.y, 0)
            try { instrumentation.sendPointerSync(event) } finally { event.recycle() }
        }
        send(MotionEvent.ACTION_DOWN)
        try { SystemClock.sleep(holdMillis) } finally { send(MotionEvent.ACTION_UP) }
        compose.waitForIdle()
    }

    private fun verifyPhysicalCollapse(prefixes: List<String>) {
        for (prefix in prefixes) {
            for (holdMillis in listOf(0L, 800L)) {
                open(prefix)
                physicalPress("${prefix}_toggle", holdMillis)
                compose.waitUntil(5_000) {
                    compose.onAllNodesWithTag("${prefix}_expanded").fetchSemanticsNodes().isEmpty()
                }
                compose.onNodeWithTag("${prefix}_expanded").assertDoesNotExist()
            }
            open(prefix)
            physicalPress("${prefix}_collapse", 800L)
            compose.onNodeWithTag("${prefix}_expanded").assertDoesNotExist()
        }
    }

    @Test fun releasingOutsideToggleOrHeaderCollapseDoesNotReopenExpensePanels() {
        host(TransactionMode.EXPENSE)
        verifyPhysicalCollapse(listOf("EntryCategory", "EntryAccount"))
    }

    @Test fun releasingOutsideToggleOrHeaderCollapseDoesNotReopenTransferDestination() {
        host(TransactionMode.TRANSFER)
        verifyPhysicalCollapse(listOf("EntryDestination"))
    }

    private fun selectCurrency(code: String) {
        click("EntryCurrency")
        click("EntryCurrency_$code")
    }

    private fun waitForRate() {
        compose.waitUntil(5_000) {
            compose.onAllNodesWithTag("EntryConvertedAmount").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun submitControl() = compose.onNode(
        hasClickAction() and hasAnyAncestor(hasTestTag("EntrySubmit")), useUnmergedTree = true)

    @Test fun currencyFetchesAutomaticallyConvertsAndPreservesTheRateUsed() {
        val requested = mutableListOf<String>()
        host(TransactionMode.EXPENSE, ExchangeRateLoader { code, _ ->
            requested.add(code)
            ExchangeRateResult(ExchangeRateQuote(code, if (code == "USD") 7.1234 else 0.0523, "2026-10-08", 0))
        })
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        selectCurrency("USD")
        waitForRate()
        compose.onNodeWithText(context.getString(R.string.currency_rate_value, "USD", "7.1234")).assertExists()
        selectCurrency("JPY")
        waitForRate()
        compose.onNodeWithText(context.getString(R.string.currency_rate_value, "JPY", "0.0523")).assertExists()
        compose.onNodeWithText("¥").assertExists()
        selectCurrency("HKD")
        compose.onNodeWithTag("EntryExchangeRate").assertDoesNotExist()
        selectCurrency("USD")
        waitForRate()
        compose.onNodeWithText("7").performScrollTo().performClick()
        click("EntrySubmit")
        compose.runOnIdle {
            assertEquals(listOf("USD", "JPY", "USD"), requested)
            assertEquals(49.86, submitted!!.amount, 0.001)
            assertTrue(submitted!!.description.contains("1 USD = 7.1234 HKD"))
            assertTrue(submitted!!.description.contains("Frankfurter 2026-10-08"))
        }
    }

    @Test fun loadingAndNetworkFailureBlockSavingUntilRetryObtainsARate() {
        val pending = CompletableDeferred<ExchangeRateResult>()
        var attempts = 0
        host(TransactionMode.EXPENSE, ExchangeRateLoader { code, _ ->
            attempts++
            if (attempts == 1) pending.await()
            else ExchangeRateResult(ExchangeRateQuote(code, 8.001, "2026-10-08", 0))
        })
        selectCurrency("USD")
        compose.onNodeWithText("7").performScrollTo().performClick()
        submitControl().assertIsNotEnabled()
        click("EntrySubmit")
        compose.runOnIdle { assertNull(submitted) }
        pending.complete(ExchangeRateResult(null, offline = true))
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("EntryExchangeRate_error").fetchSemanticsNodes().isNotEmpty() }
        submitControl().assertIsNotEnabled()
        compose.onNodeWithTag("EntryExchangeRate_refresh").performScrollTo()
        click("EntryExchangeRate_refresh")
        waitForRate()
        compose.onNodeWithTag("EntrySubmit").performScrollTo()
        submitControl().assertIsEnabled()
        click("EntrySubmit")
        compose.runOnIdle {
            assertEquals(2, attempts)
            assertEquals(56.01, submitted!!.amount, 0.001)
        }
    }

    @Test fun switchingCurrencyDiscardsAnOlderPendingRequest() {
        val pendingUsd = CompletableDeferred<ExchangeRateResult>()
        host(TransactionMode.EXPENSE, ExchangeRateLoader { code, _ ->
            if (code == "USD") pendingUsd.await()
            else ExchangeRateResult(ExchangeRateQuote(code, 0.0523, "2026-10-08", 0))
        })
        selectCurrency("USD")
        selectCurrency("JPY")
        waitForRate()
        pendingUsd.complete(ExchangeRateResult(ExchangeRateQuote("USD", 8.1, "2026-10-08", 0)))
        compose.waitForIdle()
        compose.onNodeWithText("7").performScrollTo().performClick()
        click("EntrySubmit")
        compose.runOnIdle {
            assertEquals(0.37, submitted!!.amount, 0.001)
            assertTrue(submitted!!.description.contains("1 JPY = 0.0523 HKD"))
            assertFalse(submitted!!.description.contains("USD"))
        }
    }

    @Test fun offlineSavedRateIsClearlyMarkedAndUsedForConversion() {
        host(TransactionMode.EXPENSE, ExchangeRateLoader { code, _ ->
            ExchangeRateResult(ExchangeRateQuote(code, 8.2, "2026-10-07", 0), offline = true)
        })
        selectCurrency("USD")
        waitForRate()
        compose.onNodeWithTag("EntryExchangeRate_cached").assertIsDisplayed()
        compose.onNodeWithText("7").performScrollTo().performClick()
        click("EntrySubmit")
        compose.runOnIdle {
            assertEquals(57.4, submitted!!.amount, 0.001)
            assertTrue(submitted!!.description.contains("Frankfurter 2026-10-07"))
        }
    }

    @Test fun budgetPreviewUsesTheConvertedHkdAmount() {
        host(TransactionMode.EXPENSE, ExchangeRateLoader { code, _ ->
            ExchangeRateResult(ExchangeRateQuote(code, 8.2, "2026-10-08", 0))
        }, DailyBudgetInfo(1000.0, 200.0, 800.0, 50.0, 50.0, 20, 0, 0))
        selectCurrency("USD")
        waitForRate()
        compose.onNodeWithText("7").performScrollTo().performClick()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        compose.onNodeWithText(context.getString(R.string.ui_main_budget_preview_over, "7", "39"))
            .performScrollTo().assertIsDisplayed()
    }

    @Test fun foreignIncomeKeepsTheConvertedCreditAndRateMetadata() {
        host(TransactionMode.INCOME, ExchangeRateLoader { code, _ ->
            ExchangeRateResult(ExchangeRateQuote(code, 8.2, "2026-10-08", 0))
        })
        selectCurrency("USD")
        waitForRate()
        compose.onNodeWithText("7").performScrollTo().performClick()
        click("EntrySubmit")
        compose.runOnIdle {
            assertEquals(-57.4, submitted!!.amount, 0.001)
            assertTrue(submitted!!.description.contains("1 USD = 8.2 HKD"))
        }
    }

    @Test fun foreignTransferPreservesTheConvertedAmountAndRateMetadata() {
        host(TransactionMode.TRANSFER, ExchangeRateLoader { code, _ ->
            ExchangeRateResult(ExchangeRateQuote(code, 8.2, "2026-10-08", 0))
        })
        selectCurrency("USD")
        waitForRate()
        open("EntryDestination")
        compose.onNodeWithTag("EntryDestination_grid_2").performScrollTo()
        click("EntryDestination_grid_2")
        click("EntryDestination_collapse")
        compose.onNodeWithText("7").performScrollTo().performClick()
        click("EntrySubmit")
        compose.runOnIdle {
            assertEquals(57.4, submitted!!.amount, 0.001)
            assertEquals(2L, submitted!!.toAccountId)
            assertTrue(submitted!!.description.contains("1 USD = 8.2 HKD"))
            assertTrue(submitted!!.description.contains("Frankfurter 2026-10-08"))
        }
    }

    private fun assertRowReveals(prefix: String, key: String) {
        compose.waitUntil(5_000) {
            val viewport = screenBounds("${prefix}_row")
            val choice = screenBounds("${prefix}_row_$key")
            choice.left >= viewport.left - 1f && choice.right <= viewport.right + 1f
        }
        compose.onNodeWithTag("${prefix}_row_$key").assertIsDisplayed()
    }

    @Test fun expandedCategoryRevealsTheChoiceIncludingRepeatedSelection() {
        host(TransactionMode.EXPENSE)
        val row = screenBounds("EntryCategory_row")
        open("EntryCategory")
        compose.onNodeWithTag("EntryCategory_grid_Utilities").performScrollTo()
        click("EntryCategory_grid_Utilities")
        assertRowReveals("EntryCategory", "Utilities")
        assertEquals(row, screenBounds("EntryCategory_row"))
        click("EntryCategory_collapse")
        val rowScroll = compose.onNode(hasScrollAction() and hasAnyAncestor(hasTestTag("EntryCategory_row")))
        rowScroll.performSemanticsAction(SemanticsActions.ScrollBy) { it(-100_000f, 0f) }
        compose.waitForIdle()
        assertTrue("The selected choice must no longer be fully visible after scrolling away",
            screenBounds("EntryCategory_row_Utilities").right > screenBounds("EntryCategory_row").right + 1f)
        open("EntryCategory")
        compose.onNodeWithTag("EntryCategory_grid_Utilities").performScrollTo()
        click("EntryCategory_grid_Utilities")
        assertRowReveals("EntryCategory", "Utilities")
        click("EntryCategory_collapse")
    }

    @Test fun categoryOverlayCoversAssetsAndKeypadWithoutMovingTheForm() {
        host(TransactionMode.EXPENSE)
        val row = compose.onNodeWithTag("EntryCategory_row").fetchSemanticsNode().boundsInWindow
        val keypad = compose.onNodeWithTag("EntryKeypad").fetchSemanticsNode().boundsInWindow
        open("EntryCategory")
        assertEquals(row, compose.onNodeWithTag("EntryCategory_row").fetchSemanticsNode().boundsInWindow)
        assertEquals(keypad, compose.onNodeWithTag("EntryKeypad").fetchSemanticsNode().boundsInWindow)
        val menu = screenBounds("EntryCategory_expanded")
        val keypadScreen = screenBounds("EntryKeypad")
        assertEquals(keypadScreen.left, menu.left, 1f)
        assertTrue("Panel begins below its connected row", menu.top > screenBounds("EntryCategory_row").bottom)
        assertTrue("Panel covers the expense icon control as well as assets",
            menu.top < screenBounds("ExpenseIconChoose").top)
        assertEquals(keypadScreen.width, menu.width, 1f)
        assertEquals(keypadScreen.bottom, menu.bottom, 1f)
        click("EntryCategory_grid_Food")
        compose.onNodeWithTag("EntryCategory_grid_Food").assertIsSelected()
        click("EntryCategory_collapse")
        compose.onNodeWithTag("EntryCategory_expanded").assertDoesNotExist()
        compose.onNodeWithText("7").performScrollTo().performClick()
        click("EntrySubmit")
        compose.runOnIdle {
            assertEquals("Food", submitted?.category)
            assertEquals(7.0, submitted!!.amount, 0.001)
        }
    }

    @Test fun accountOverlayScrollsAndCommitsSelectedAccount() {
        host(TransactionMode.INCOME)
        open("EntryAccount")
        compose.onNodeWithTag("EntryAccount_grid_14").performScrollTo()
        click("EntryAccount_grid_14")
        compose.onNodeWithTag("EntryAccount_grid_14").assertIsSelected()
        assertRowReveals("EntryAccount", "14")
        click("EntryAccount_collapse")
        compose.onNodeWithText("7").performScrollTo().performClick()
        click("EntrySubmit")
        compose.runOnIdle {
            assertEquals(14L, submitted?.accountId)
            assertEquals(-7.0, submitted!!.amount, 0.001)
        }
    }

    @Test fun transferDestinationExcludesSourceAndSubmitRequiresDestination() {
        host(TransactionMode.TRANSFER)
        compose.onNodeWithText("7").performScrollTo().performClick()
        click("EntrySubmit")
        compose.runOnIdle { assertNull(submitted) }
        open("EntryDestination")
        compose.onNodeWithTag("EntryDestination_grid_1").assertDoesNotExist()
        click("EntryDestination_grid_2")
        click("EntryDestination_collapse")
        // Choosing the former destination as source clears the invalid destination.
        open("EntryAccount")
        click("EntryAccount_grid_2")
        click("EntryAccount_collapse")
        click("EntrySubmit")
        compose.runOnIdle { assertNull(submitted) }
        open("EntryDestination")
        compose.onNodeWithTag("EntryDestination_grid_14").performScrollTo()
        click("EntryDestination_grid_14")
        assertRowReveals("EntryDestination", "14")
        click("EntryDestination_collapse")
        click("EntrySubmit")
        compose.runOnIdle {
            assertEquals(2L, submitted?.accountId)
            assertEquals(14L, submitted?.toAccountId)
        }
    }

    @Test fun overflowBlurEdgesTrackHorizontalAndVerticalScrollLimits() {
        host(TransactionMode.INCOME)
        compose.onNodeWithTag("EntryAccount_row_start_fade").assertDoesNotExist()
        compose.onNodeWithTag("EntryAccount_row_end_fade").assertExists()
        val rowScroll = compose.onNode(hasScrollAction() and hasAnyAncestor(hasTestTag("EntryAccount_row")))
        rowScroll.performSemanticsAction(SemanticsActions.ScrollBy) { it(70f, 0f) }
        compose.waitForIdle()
        compose.onNodeWithTag("EntryAccount_row_start_fade").assertExists()
        compose.onNodeWithTag("EntryAccount_row_end_fade").assertExists()
        rowScroll.performSemanticsAction(SemanticsActions.ScrollBy) { it(100_000f, 0f) }
        compose.waitForIdle()
        compose.onNodeWithTag("EntryAccount_row_start_fade").assertExists()
        compose.onNodeWithTag("EntryAccount_row_end_fade").assertDoesNotExist()
        rowScroll.performSemanticsAction(SemanticsActions.ScrollBy) { it(-100_000f, 0f) }
        compose.waitForIdle()
        compose.onNodeWithTag("EntryAccount_row_start_fade").assertDoesNotExist()
        open("EntryAccount")
        compose.onNodeWithTag("EntryAccount_grid_start_fade").assertDoesNotExist()
        compose.onNodeWithTag("EntryAccount_grid_end_fade").assertExists()
        val grid = compose.onNodeWithTag("EntryAccount_grid")
        grid.performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, 50f) }
        compose.waitForIdle()
        compose.onNodeWithTag("EntryAccount_grid_start_fade").assertExists()
        compose.onNodeWithTag("EntryAccount_grid_end_fade").assertExists()
        grid.performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, 100_000f) }
        compose.waitForIdle()
        compose.onNodeWithTag("EntryAccount_grid_start_fade").assertExists()
        compose.onNodeWithTag("EntryAccount_grid_end_fade").assertDoesNotExist()
        grid.performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, -100_000f) }
        compose.waitForIdle()
        compose.onNodeWithTag("EntryAccount_grid_start_fade").assertDoesNotExist()
        click("EntryAccount_collapse")
    }

    @Test fun expansionAndCollapseAnimateWithinFixedOverlayBounds() {
        host(TransactionMode.EXPENSE)
        val row = screenBounds("EntryCategory_row")
        val availableHeight = screenBounds("EntryKeypad").bottom - row.bottom
        compose.mainClock.autoAdvance = false
        click("EntryCategory_toggle")
        compose.mainClock.advanceTimeBy(80)
        compose.waitForIdle()
        // A newly attached popup can still be waiting for its first platform layout frame.
        // Zero visible height is valid here; an immediate, fully expanded menu is not.
        val openingHeight = compose.onNodeWithTag("EntryCategory_expanded").fetchSemanticsNode().boundsInRoot.height
        assertTrue("Opening must not jump immediately to full height", openingHeight < availableHeight)
        compose.mainClock.advanceTimeBy(2_000)
        compose.waitForIdle()
        val openedMenu = screenBounds("EntryCategory_expanded")
        assertTrue("Panel height grows during opening", openingHeight < openedMenu.height)
        assertEquals(screenBounds("EntryKeypad").bottom, openedMenu.bottom, 1f)
        assertEquals(row, screenBounds("EntryCategory_row"))
        click("EntryCategory_collapse")
        compose.mainClock.advanceTimeBy(80)
        compose.waitForIdle()
        compose.onNodeWithTag("EntryCategory_expanded").assertExists()
        compose.mainClock.advanceTimeBy(2_000)
        compose.waitForIdle()
        compose.onNodeWithTag("EntryCategory_expanded").assertDoesNotExist()
        compose.mainClock.autoAdvance = true
    }

    @Test fun choicesThatFitHaveNoBlurFades() {
        compose.setContent {
            val overlay = remember { EntrySelectorOverlayState() }
            CompositionLocalProvider(LocalEntrySelectorOverlay provides overlay) {
                MaterialTheme {
                    Column {
                        ExpandableEntrySelector(listOf("Food"), 0, {}, "Category", "Short", { it }, { it })
                        Box(Modifier.fillMaxWidth().height(218.dp).onGloballyPositioned {
                            overlay.keypadBounds = it.boundsInWindow()
                        })
                    }
                }
            }
        }
        compose.onNodeWithTag("Short_row_start_fade").assertDoesNotExist()
        compose.onNodeWithTag("Short_row_end_fade").assertDoesNotExist()
        open("Short")
        compose.onNodeWithTag("Short_grid_start_fade").assertDoesNotExist()
        compose.onNodeWithTag("Short_grid_end_fade").assertDoesNotExist()
        click("Short_collapse")
    }

    private fun verifyOriginalHeight(fontScale: Float, dark: Boolean) {
        compose.setContent {
            val density = LocalDensity.current.density
            val overlay = remember { EntrySelectorOverlayState() }
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale), LocalEntrySelectorOverlay provides overlay) {
                MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                    Column {
                        val items = (1..14).map { "Choice $it" }
                        var selected by remember { mutableIntStateOf(0) }
                        ExpressiveSegmentedButtonGroup(items, selected, { selected = it },
                            modifier = Modifier.testTag("OriginalGroup"), isScrollable = true, labelProvider = { it })
                        ExpandableEntrySelector(items, selected, { selected = it }, "Category", "Fixture", { it }, { it })
                        Box(Modifier.fillMaxWidth().height(218.dp).testTag("FixtureKeypad").onGloballyPositioned {
                            overlay.keypadBounds = it.boundsInWindow()
                        })
                    }
                }
            }
        }
        val originalHeight = compose.onNodeWithTag("OriginalGroup").fetchSemanticsNode().boundsInWindow.height
        assertEquals(originalHeight, compose.onNodeWithTag("Fixture_row").fetchSemanticsNode().boundsInWindow.height, 0f)
        val row = compose.onNodeWithTag("Fixture_row").fetchSemanticsNode().boundsInWindow
        open("Fixture")
        assertEquals(row, compose.onNodeWithTag("Fixture_row").fetchSemanticsNode().boundsInWindow)
        compose.onNodeWithTag("Fixture_grid_Choice 14").performScrollTo()
        click("Fixture_grid_Choice 14")
        compose.onNodeWithTag("Fixture_grid_Choice 14").assertIsSelected()
        assertRowReveals("Fixture", "Choice 14")
        click("Fixture_collapse")
    }

    @Test fun defaultFontKeepsOriginalConnectedButtonHeight() = verifyOriginalHeight(1f, false)
    @Test fun largeFontDarkThemeKeepsOriginalHeightAndScrollableGrid() = verifyOriginalHeight(2f, true)
}
