package com.example.vibefinance.ui.main

import android.view.View
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.core.view.WindowInsetsCompat
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.ui.FinanceUiState
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** ARTEMIS/ADB verified full-window entry, modal bottom sheet Back dismissal, IME-first Back and return to Daily.
 * All inputs below are isolated in memory; no financial intents are persisted.
 */
@RunWith(AndroidJUnit4::class)
class TransactionEntryHostTest {
    @get:Rule val rule = createComposeRule()
    private var visible by mutableStateOf(false)
    private var backgroundClicks = 0
    private lateinit var view: View
    private var fixtureDensity = 1f

    private fun show(animated: Boolean = false, short: Boolean = false, large: Boolean = false) {
        rule.mainClock.autoAdvance = !animated
        rule.setContent {
            view = LocalView.current
            val density = LocalDensity.current
            fixtureDensity = density.density
            CompositionLocalProvider(LocalDensity provides Density(density.density, if (large) 1.7f else density.fontScale)) {
                MaterialTheme(colorScheme = if (large) darkColorScheme() else androidx.compose.material3.lightColorScheme()) {
                    Box((if (short) Modifier.fillMaxWidth().height(420.dp) else Modifier.fillMaxSize()).testTag("EntryFixtureWindow")) {
                        Box(Modifier.fillMaxSize().clickable { backgroundClicks++ }) { Text("Background") }
                        TransactionEntryHost(
                            visible = visible,
                            state = FinanceUiState(isLoading = false, accounts = listOf(
                                AccountEntity(1, "Bank", type = AccountType.BANK, balance = 1000.0, icon = "bank"))),
                            onIntent = { error("Read-only fixture must not submit") },
                            onDismiss = { visible = false }, initialMode = TransactionMode.EXPENSE,
                            categoryFrequency = emptyMap(), accountFrequency = emptyMap(),
                        )
                    }
                }
            }
        }
        rule.runOnIdle { visible = true }
        if (!animated) {
            rule.waitForIdle()
        }
    }

    @Test fun receiptPickerCancellationReenablesScannerWithoutChangingDraftOrSaving() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val filter = android.content.IntentFilter(android.content.Intent.ACTION_GET_CONTENT).apply {
            addCategory(android.content.Intent.CATEGORY_OPENABLE)
            addDataType("image/*")
        }
        val monitor = instrumentation.addMonitor(filter,
            android.app.Instrumentation.ActivityResult(android.app.Activity.RESULT_CANCELED, null), true)
        try {
            show()
            rule.onNodeWithTag("EntryDescription").performTextReplacement("Retain receipt note")
            rule.onNodeWithTag("ReceiptScannerAction").performScrollTo().performClick()
            rule.waitUntil(8_000) { monitor.hits == 1 }
            rule.waitForIdle()
            rule.onNodeWithTag("ReceiptScannerAction").assertIsEnabled()
            rule.onNodeWithTag("EntryDescription").assertTextContains("Retain receipt note")
            rule.onNodeWithTag("ReceiptScannerAction").performClick()
            rule.waitUntil(8_000) { monitor.hits == 2 }
            rule.onNodeWithTag("TransactionEntryScreen").assertExists()
        } finally {
            instrumentation.removeMonitor(monitor)
        }
    }

    private fun waitClosed() {
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("TransactionEntryScreen").fetchSemanticsNodes().isEmpty() }
    }

    private fun assertKeypadDocked() {
        val bounds = rule.onNodeWithTag("EntryKeypad").getUnclippedBoundsInRoot()
        val form = rule.onNodeWithTag("EntryForm").getUnclippedBoundsInRoot()
        assertTrue("Keypad bottom should be within form bottom", bounds.bottom <= form.bottom + 2.dp)
        rule.onNodeWithTag("EntrySubmit").assertIsDisplayed()
    }

    private fun waitKeyboard(visible: Boolean) {
        rule.waitUntil(8_000) {
            WindowInsetsCompat.toWindowInsetsCompat(view.rootWindowInsets).isVisible(WindowInsetsCompat.Type.ime()) == visible
        }
    }

    @Test fun entryAutomaticallyOpensNumericKeyboardAndAllModesRetainIt() {
        show()
        waitKeyboard(true)
        rule.onNodeWithTag("EntryAmount").assertIsFocused().performTextReplacement("12.50")
        for (mode in listOf(TransactionMode.INCOME, TransactionMode.TRANSFER, TransactionMode.EXPENSE)) {
            rule.onNodeWithTag("TransactionMode_${mode.name}").performScrollTo().performClick().assertIsSelected()
            waitKeyboard(true)
            rule.onNodeWithTag("EntryAmount").assertIsFocused().assertTextEquals("12.50")
        }
        assertEquals(0, backgroundClicks)
    }

    @Test fun manuallyHiddenKeyboardIsNotReopenedByModeSwitching() {
        show()
        waitKeyboard(true)
        pressBack()
        waitKeyboard(false)
        for (mode in listOf(TransactionMode.INCOME, TransactionMode.TRANSFER, TransactionMode.EXPENSE)) {
            rule.onNodeWithTag("TransactionMode_${mode.name}").performScrollTo().performClick().assertIsSelected()
            rule.waitForIdle()
            waitKeyboard(false)
        }
        rule.onNodeWithTag("TransactionEntryScreen").assertExists()
    }

    @Test fun modeCardsResizeWithoutOvershootOrSettledPositionJumps() {
        show()
        waitKeyboard(true)
        pressBack()
        waitKeyboard(false)
        rule.mainClock.autoAdvance = false
        val tags = listOf("EntryModePreview", "EntryModeClassification")
        fun heights(): List<Float> = tags.map {
            val bounds = rule.onNodeWithTag(it).getUnclippedBoundsInRoot()
            (bounds.bottom - bounds.top).value
        }
        for (mode in listOf(TransactionMode.INCOME, TransactionMode.TRANSFER, TransactionMode.EXPENSE,
            TransactionMode.TRANSFER, TransactionMode.INCOME, TransactionMode.EXPENSE)) {
            val samples = mutableListOf(heights())
            rule.onNodeWithTag("TransactionMode_${mode.name}").performClick()
            repeat(24) {
                rule.mainClock.advanceTimeBy(16)
                rule.waitForIdle()
                samples += heights()
            }
            for (index in tags.indices) {
                val start = samples.first()[index]; val end = samples.last()[index]
                for (value in samples.map { it[index] }) {
                    assertTrue("${tags[index]} must stay within its start/end heights", value >= minOf(start, end) - 1f && value <= maxOf(start, end) + 1f)
                }
                samples.zipWithNext().forEach { (before, after) ->
                    assertTrue("${tags[index]} must not reverse during resizing",
                        if (end >= start) after[index] >= before[index] - 1f else after[index] <= before[index] + 1f)
                }
            }
            val settled = tags.map { rule.onNodeWithTag(it).getUnclippedBoundsInRoot() }
            rule.mainClock.advanceTimeBy(800)
            rule.waitForIdle()
            assertEquals(settled, tags.map { rule.onNodeWithTag(it).getUnclippedBoundsInRoot() })
        }
        val expenseHeights = heights()
        for (mode in listOf(TransactionMode.INCOME, TransactionMode.TRANSFER, TransactionMode.EXPENSE)) {
            rule.onNodeWithTag("TransactionMode_${mode.name}").performClick()
            rule.mainClock.advanceTimeBy(64)
            rule.waitForIdle()
        }
        rule.mainClock.advanceTimeBy(800)
        rule.waitForIdle()
        assertEquals("Interrupted switches must settle back to the original layout", expenseHeights, heights())
    }

    @Test fun coversWindowAndBlocksBackgroundTouches() {
        show()
        val window = rule.onNodeWithTag("EntryFixtureWindow").getUnclippedBoundsInRoot()
        val page = rule.onNodeWithTag("TransactionEntryScreen").getUnclippedBoundsInRoot()
        assertEquals(window, page)
        // Blank trailing page space must consume touch instead of reaching the covered page.
        rule.onNodeWithTag("TransactionEntryScreen").performTouchInput { click(Offset(width / 2f, height - 2f)) }
        assertEquals(0, backgroundClicks)
        rule.onNodeWithTag("TransactionModeSwitcher").assertIsDisplayed()
        waitKeyboard(true)
        pressBack()
        waitKeyboard(false)
        rule.onNodeWithTag("TransactionEntryScreen").assertExists()
        pressBack()
        waitClosed()
    }

    @Test fun selectorBackClosesPopupBeforePage() {
        show()
        // Tap the Asset Card to open the modal bottom sheet
        rule.onNodeWithTag("EntryAccount_card").performScrollTo().performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("EntryAccount_row_1").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("EntryAccount_row_1").assertIsDisplayed()
        // First back press dismisses the modal bottom sheet
        pressBack()
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("EntryAccount_row_1").fetchSemanticsNodes().isEmpty() }
        rule.onNodeWithTag("TransactionEntryScreen").assertExists()
        // Second back press dismisses the page
        pressBack()
        waitClosed()
    }

    @Test fun keyboardBackPreservesDraftBeforeLeavingPage() {
        show()
        rule.onNodeWithTag("EntryDescription").performScrollTo().performClick().performTextReplacement("午餐 🍜")
        rule.waitUntil(8_000) {
            WindowInsetsCompat.toWindowInsetsCompat(view.rootWindowInsets).isVisible(WindowInsetsCompat.Type.ime())
        }
        pressBack()
        rule.waitUntil(8_000) {
            !WindowInsetsCompat.toWindowInsetsCompat(view.rootWindowInsets).isVisible(WindowInsetsCompat.Type.ime())
        }
        rule.onNodeWithTag("TransactionEntryScreen").assertExists()
        rule.onNodeWithTag("EntryDescription").assertTextContains("午餐 🍜")
        pressBack()
        waitClosed()
    }

    @Test fun slidesFromRightAndReturnsRightBeforeRemoval() {
        show(animated = true)
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeBy(96)
        assertTrue(rule.onNodeWithTag("TransactionEntryScreen").getUnclippedBoundsInRoot().left.value > 0f)
        rule.mainClock.advanceTimeBy(600)
        rule.waitForIdle()
        val window = rule.onNodeWithTag("EntryFixtureWindow").getUnclippedBoundsInRoot()
        assertEquals(window, rule.onNodeWithTag("TransactionEntryScreen").getUnclippedBoundsInRoot())
        rule.runOnIdle { visible = false }
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeBy(96)
        assertTrue(rule.onNodeWithTag("TransactionEntryScreen").getUnclippedBoundsInRoot().left.value > 0f)
        rule.mainClock.advanceTimeBy(600)
        rule.waitForIdle()
        rule.onNodeWithTag("TransactionEntryScreen").assertDoesNotExist()
    }

    @Test fun keypadStaysDockedAcrossModesAndFormScrolling() {
        show()
        val keypad = rule.onNodeWithTag("EntryKeypad").getUnclippedBoundsInRoot()
        assertKeypadDocked()
        TransactionMode.entries.forEach { mode ->
            rule.onNodeWithTag("TransactionMode_${mode.name}").performScrollTo().performClick()
            rule.waitForIdle()
            rule.onNodeWithTag("EntryScrollContent").performTouchInput { swipeUp() }
            rule.waitForIdle()
            assertEquals(keypad, rule.onNodeWithTag("EntryKeypad").getUnclippedBoundsInRoot())
            rule.onNodeWithTag("EntrySubmit").assertIsDisplayed()
        }
    }

    @Test fun heroPreviewAndModeSpecificHintsRetainExpression() {
        show()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        rule.onNodeWithTag("EntryAmount").performTextReplacement("7.50")
        val switcher = rule.onNodeWithTag("TransactionModeSwitcher").getUnclippedBoundsInRoot()
        val amount = rule.onNodeWithTag("EntryAmount").getUnclippedBoundsInRoot()
        assertTrue(amount.top >= switcher.bottom)
        listOf(TransactionMode.EXPENSE to R.string.ui_main_note_hint_expense,
            TransactionMode.INCOME to R.string.ui_main_note_hint_income,
            TransactionMode.TRANSFER to R.string.ui_main_note_hint_transfer).forEach { (mode, hint) ->
            rule.onNodeWithTag("TransactionMode_${mode.name}").performScrollTo().performClick()
            rule.onNodeWithTag("EntryDescription").performScrollTo().assertTextContains(context.getString(hint))
            rule.onNodeWithTag("EntryAmount").assert(SemanticsMatcher.expectValue(
                androidx.compose.ui.semantics.SemanticsProperties.StateDescription, "7.50"))
        }
    }

    @Test fun shortViewportKeepsFieldsReachableAboveDockedKeypad() {
        show(short = true)
        rule.onNodeWithTag("EntryDescription").performScrollTo().assertIsDisplayed().performTextReplacement("Short viewport")
        rule.onNodeWithTag("EntryAmount").performScrollTo().assertIsDisplayed().performTextReplacement("7")
        assertKeypadDocked()
        rule.onNodeWithTag("EntryDescription").assertTextContains("Short viewport")
    }

    @Test fun largeFontDarkFixtureKeepsFormAndKeypadUsable() {
        show(large = true)
        rule.onNodeWithTag("TransactionMode_INCOME").performScrollTo().assertIsDisplayed().performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("EntryDescription").performScrollTo().assertIsDisplayed().performTextReplacement("Large font")
        rule.onNodeWithTag("EntryAmount").performScrollTo().assertIsDisplayed().performTextReplacement("7")
        rule.onNodeWithTag("EntryCurrency").assertIsDisplayed()
        rule.onNodeWithTag("EntryDescription").assertTextContains("Large font")
    }
}
