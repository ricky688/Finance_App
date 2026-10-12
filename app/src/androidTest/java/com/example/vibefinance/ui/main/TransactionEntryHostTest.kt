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
import kotlin.math.roundToInt

/** ARTEMIS/ADB verified full-window entry, selector-first Back, IME-first Back and return to Daily.
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
        if (!animated) rule.waitForIdle()
    }

    private fun waitClosed() {
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("TransactionEntryScreen").fetchSemanticsNodes().isEmpty() }
    }

    private fun assertKeypadDocked() {
        val bounds = rule.onNodeWithTag("EntryKeypad").getUnclippedBoundsInRoot()
        val form = rule.onNodeWithTag("EntryForm").getUnclippedBoundsInRoot()
        // Compose rounds each row, gap and padding to device pixels independently.
        val expectedPx = 4 * (54 * fixtureDensity).roundToInt() + 3 * (6 * fixtureDensity).roundToInt()
        assertEquals(expectedPx / fixtureDensity, (bounds.bottom - bounds.top).value, 0.1f)
        // Only the specified 6dp padding separates the keys from the consumed system inset.
        assertEquals((6 * fixtureDensity).roundToInt() / fixtureDensity, (form.bottom - bounds.bottom).value, 0.1f)
    }

    @Test fun coversWindowAndBlocksBackgroundTouches() {
        show()
        val window = rule.onNodeWithTag("EntryFixtureWindow").getUnclippedBoundsInRoot()
        val page = rule.onNodeWithTag("TransactionEntryScreen").getUnclippedBoundsInRoot()
        assertEquals(window, page)
        // Blank trailing page space must consume touch instead of reaching the covered page.
        rule.onNodeWithTag("TransactionEntryScreen").performTouchInput { click(Offset(width / 2f, height - 2f)) }
        assertEquals(0, backgroundClicks)
        rule.onNodeWithTag("EntryModeTitle").assertIsDisplayed()
        pressBack()
        waitClosed()
    }

    @Test fun selectorBackClosesPopupBeforePage() {
        show()
        rule.onNodeWithTag("EntryCategory_toggle").performScrollTo().performClick()
        rule.onNodeWithTag("EntryCategory_expanded").assertIsDisplayed()
        pressBack()
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("EntryCategory_expanded").fetchSemanticsNodes().isEmpty() }
        rule.onNodeWithTag("TransactionEntryScreen").assertExists()
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
        rule.onNodeWithTag("EntryKey_7").assertHeightIsEqualTo(54.dp)
        TransactionMode.entries.forEach { mode ->
            rule.onNodeWithTag("TransactionMode_${mode.name}").performScrollTo().performClick()
            rule.waitForIdle()
            rule.onNodeWithTag("EntryScrollContent").performTouchInput { swipeUp() }
            rule.waitForIdle()
            assertEquals(keypad, rule.onNodeWithTag("EntryKeypad").getUnclippedBoundsInRoot())
            rule.onNodeWithTag("EntryKey_7").assertIsDisplayed()
            rule.onNodeWithTag("EntrySubmit").assertIsDisplayed()
        }
    }

    @Test fun heroPreviewAndModeSpecificHintsRetainExpression() {
        show()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        rule.onNodeWithTag("EntryKey_7").performClick()
        rule.onNodeWithTag("EntryKey_+").performClick()
        val title = rule.onNodeWithTag("EntryModeTitle").getUnclippedBoundsInRoot()
        val amount = rule.onNodeWithTag("EntryAmount").getUnclippedBoundsInRoot()
        val modes = rule.onNodeWithTag("TransactionModeSwitcher").getUnclippedBoundsInRoot()
        assertTrue(amount.top >= title.bottom)
        assertTrue(amount.bottom <= modes.top)
        listOf(TransactionMode.EXPENSE to R.string.ui_main_note_hint_expense,
            TransactionMode.INCOME to R.string.ui_main_note_hint_income,
            TransactionMode.TRANSFER to R.string.ui_main_note_hint_transfer).forEach { (mode, hint) ->
            rule.onNodeWithTag("TransactionMode_${mode.name}").performScrollTo().performClick()
            rule.onNodeWithTag("EntryDescription").performScrollTo().assertTextContains(context.getString(hint))
            rule.onNodeWithTag("EntryAmount").assert(SemanticsMatcher.expectValue(
                androidx.compose.ui.semantics.SemanticsProperties.StateDescription, "7+"))
            rule.onNodeWithTag("EntryCalculatorResult").assertTextEquals("= 7.00")
        }
    }

    @Test fun shortViewportKeepsFieldsReachableAboveDockedKeypad() {
        show(short = true)
        rule.onNodeWithTag("EntryDescription").performScrollTo().assertIsDisplayed().performTextReplacement("Short viewport")
        rule.onNodeWithTag("EntryKey_7").assertIsDisplayed().performClick()
        assertKeypadDocked()
        rule.onNodeWithTag("EntryDescription").assertTextContains("Short viewport")
    }

    @Test fun largeFontDarkFixtureKeepsFormAndKeypadUsable() {
        show(large = true)
        rule.onNodeWithTag("TransactionMode_INCOME").performScrollTo().assertIsDisplayed().performClick()
        rule.onNodeWithTag("EntryDescription").performScrollTo().assertIsDisplayed().performTextReplacement("Large font")
        rule.onNodeWithTag("EntryKey_7").assertHeightIsEqualTo(54.dp).assertIsDisplayed().performClick()
        rule.onNodeWithTag("EntryCurrency").assertIsDisplayed()
        rule.onNodeWithTag("EntryDescription").assertTextContains("Large font")
    }
}
