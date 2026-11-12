package com.example.vibefinance.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import com.example.vibefinance.data.entity.*
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.preferences.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Native amount focus/typing/Done and guided selection explored with ARTEMIS on Waydroid. Memory only. */
class AmountTypographyTest {
    @get:Rule val rule = createComposeRule()
    private var amount by mutableStateOf("8888")

    private fun show(motion: MotionLevel = MotionLevel.FULL) {
        rule.setContent {
            val focus = LocalFocusManager.current
            val keyboard = LocalSoftwareKeyboardController.current
            val requester = remember { FocusRequester() }
            CompositionLocalProvider(LocalExperience provides ExperiencePreferences(motion = motion)) {
                MaterialTheme(lightColorScheme(onSurface = Color.Black, surface = Color.White, primary = Color.Magenta)) {
                    Column(Modifier.fillMaxSize().background(Color.White).padding(16.dp)) {
                        Row(Modifier.fillMaxWidth()) {
                            Text("HK$", Modifier.testTag("AmountCurrency"))
                            TransactionEntryAmount(amount, { amount = it }, { focus.clearFocus(); keyboard?.hide() },
                                requester, Modifier.weight(1f, fill = false))
                        }
                        Box(Modifier.fillMaxWidth().height(24.dp).background(Color.Blue).testTag("BelowAmount"))
                    }
                }
            }
        }
        rule.waitForIdle()
    }
    private fun inkHeight(image: ImageBitmap): Int {
        val pixels = image.toPixelMap()
        var top = image.height; var bottom = -1
        for (y in 0 until image.height) for (x in 0 until image.width) {
            val color = pixels[x, y]
            // The caret is magenta, so only the actual black figure outlines are measured.
            if (color.alpha > 0.5f && color.red < 0.3f && color.green < 0.3f && color.blue < 0.3f) {
                top = minOf(top, y); bottom = maxOf(bottom, y)
            }
        }
        assertTrue("Roboto figures must render", bottom >= top)
        return bottom - top + 1
    }

    @Test fun focusedFiguresGrowAndReturnWithoutMovingTheFieldOrFollowingContent() {
        show()
        val resting = rule.onNodeWithTag("EntryAmount").captureToImage()
        val field = rule.onNodeWithTag("EntryAmount").getUnclippedBoundsInRoot()
        val following = rule.onNodeWithTag("BelowAmount").getUnclippedBoundsInRoot()
        val currency = rule.onNodeWithTag("AmountCurrency").getUnclippedBoundsInRoot()
        rule.mainClock.autoAdvance = false
        rule.onNodeWithTag("EntryAmount").performClick()
        rule.mainClock.advanceTimeBy(64)
        val middle = rule.onNodeWithTag("EntryAmount").captureToImage()
        rule.mainClock.advanceTimeBy(240)
        val focused = rule.onNodeWithTag("EntryAmount").captureToImage()
        assertTrue("Real font outlines must get taller", inkHeight(focused) >= inkHeight(resting) + 2)
        assertTrue("Focus transition must interpolate", inkHeight(middle) in (inkHeight(resting) + 1) until inkHeight(focused))
        assertEquals(field, rule.onNodeWithTag("EntryAmount").getUnclippedBoundsInRoot())
        assertEquals(following, rule.onNodeWithTag("BelowAmount").getUnclippedBoundsInRoot())
        assertEquals(currency, rule.onNodeWithTag("AmountCurrency").getUnclippedBoundsInRoot())
        rule.onNodeWithTag("EntryAmount").performImeAction()
        rule.mainClock.advanceTimeBy(400)
        assertEquals(inkHeight(resting), inkHeight(rule.onNodeWithTag("EntryAmount").captureToImage()))
        rule.onNodeWithTag("EntryAmount").assertIsNotFocused()
        assertEquals(field, rule.onNodeWithTag("EntryAmount").getUnclippedBoundsInRoot())
        assertEquals(following, rule.onNodeWithTag("BelowAmount").getUnclippedBoundsInRoot())
        rule.mainClock.autoAdvance = true
    }

    @Test fun buckwheatSizedFiguresShrinkForLongValuesWithoutMovingFollowingContent() {
        show()
        val shortInk = inkHeight(rule.onNodeWithTag("EntryAmount").captureToImage())
        assertTrue("Short amounts should have an expressive large figure size",
            shortInk > with(rule.density) { 40.sp.toPx() })
        val following = rule.onNodeWithTag("BelowAmount").getUnclippedBoundsInRoot()
        rule.runOnIdle { amount = "12345678901234567890.12" }
        val longInk = inkHeight(rule.onNodeWithTag("EntryAmount").captureToImage())
        assertTrue("Long amounts must adapt instead of keeping the 80sp size", longInk < shortInk)
        assertAmountFits()
        assertEquals(following, rule.onNodeWithTag("BelowAmount").getUnclippedBoundsInRoot())
    }

    @Test fun narrowLargeFontAmountRemainsFullyVisible() {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.6f)) {
                MaterialTheme {
                    Box(Modifier.width(240.dp)) {
                        TransactionEntryAmount("1234567890.12", {}, {}, remember { FocusRequester() })
                    }
                }
            }
        }
        rule.onNodeWithTag("EntryAmount").assertIsDisplayed()
        assertAmountFits()
    }

    private fun assertAmountFits() {
        val layouts = mutableListOf<TextLayoutResult>()
        rule.onNodeWithTag("EntryAmount").performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        assertTrue("Native editor must expose its rendered text", layouts.isNotEmpty())
        val layout = layouts.last()
        val bounds = rule.onNodeWithTag("EntryAmount").getUnclippedBoundsInRoot()
        val width = with(rule.density) { (bounds.right - bounds.left).toPx() }
        assertEquals(1, layout.lineCount)
        assertTrue("Entire amount must fit: line=${layout.getLineRight(0)}, field=$width, font=${layout.layoutInput.style.fontSize}, density=${layout.layoutInput.density}", layout.getLineRight(0) <= width + 1f)
    }

    @Test fun nativeSelectionAndReplacementSurviveFontMorphing() {
        amount = "12345678.90"
        show()
        rule.mainClock.autoAdvance = false
        rule.onNodeWithTag("EntryAmount").performClick()
        rule.onNodeWithTag("EntryAmount").performTextInputSelection(TextRange(2, 6))
        repeat(16) {
            rule.mainClock.advanceTimeBy(16)
            rule.onNodeWithTag("EntryAmount").assert(SemanticsMatcher.expectValue(
                SemanticsProperties.TextSelectionRange, TextRange(2, 6)))
        }
        rule.onNodeWithTag("EntryAmount").performTextInput("99")
        rule.mainClock.advanceTimeByFrame()
        rule.onNodeWithTag("EntryAmount").assert(SemanticsMatcher.expectValue(
            SemanticsProperties.StateDescription, "129978.90"))
        rule.mainClock.autoAdvance = true
    }

    @Test fun minimalMotionUsesTheFocusedFontImmediatelyWithFixedBounds() {
        show(MotionLevel.MINIMAL)
        val resting = inkHeight(rule.onNodeWithTag("EntryAmount").captureToImage())
        val bounds = rule.onNodeWithTag("EntryAmount").getUnclippedBoundsInRoot()
        rule.mainClock.autoAdvance = false
        rule.onNodeWithTag("EntryAmount").performClick()
        rule.mainClock.advanceTimeByFrame()
        val immediate = inkHeight(rule.onNodeWithTag("EntryAmount").captureToImage())
        rule.mainClock.advanceTimeBy(400)
        assertTrue(immediate > resting)
        assertEquals(immediate, inkHeight(rule.onNodeWithTag("EntryAmount").captureToImage()))
        assertEquals(bounds, rule.onNodeWithTag("EntryAmount").getUnclippedBoundsInRoot())
        rule.mainClock.autoAdvance = true
    }

    @Test fun longAmountKeepsCurrencyAndClearButtonInsideTheEditor() {
        rule.setContent {
            MaterialTheme {
                TransactionEntryHost(true, FinanceUiState(isLoading = false, accounts = listOf(
                    AccountEntity(id = 1, name = "Bank", type = AccountType.BANK, balance = 1000.0, icon = "bank"))),
                    onIntent = { error("Fixture must not persist") }, onDismiss = {}, initialMode = TransactionMode.EXPENSE,
                    categoryFrequency = emptyMap(), accountFrequency = emptyMap())
            }
        }
        val longAmount = "12345678901234567890.12"
        rule.onNodeWithTag("EntryAmount").performScrollTo().performTextReplacement(longAmount)
        rule.onNodeWithTag("EntryAmount").assertIsDisplayed().assert(SemanticsMatcher.expectValue(
            SemanticsProperties.StateDescription, longAmount))
        rule.onNodeWithContentDescription("Clear").assertIsDisplayed()
        val window = rule.onNodeWithTag("TransactionEntryScreen").getUnclippedBoundsInRoot()
        val clear = rule.onNodeWithContentDescription("Clear").getUnclippedBoundsInRoot()
        assertTrue(clear.right <= window.right)
        rule.onNodeWithText("HK$ ").assertIsDisplayed()
        rule.onNodeWithContentDescription("Clear").performClick()
        rule.onNodeWithTag("EntryAmount").assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "0"))
    }
}
