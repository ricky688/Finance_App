package com.example.vibefinance.wear

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.wear.compose.material3.MaterialTheme
import com.example.vibefinance.wear.contract.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Actual Wear components with in-memory display fixtures, not a paired-device E2E suite. */
@RunWith(AndroidJUnit4::class)
class BudgetScreenTest {
    @get:Rule val rule = createAndroidComposeRule<androidx.activity.ComponentActivity>()

    @Before fun keepOnlyTheFixtureActivityAwake() {
        rule.activityRule.scenario.onActivity {
            it.setTurnScreenOn(true)
            it.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    @Test fun waitingScreenCanScrollToTheRealRefreshButton() {
        var requests = 0
        rule.setContent { MaterialTheme { BudgetScreen(CachedBudget(), false, false) { requests++ } } }
        rule.onNodeWithTag("BudgetStatus").assertExists()
        rule.onNodeWithTag("RefreshBudget").performScrollTo().assertIsDisplayed().performClick()
        rule.runOnIdle { assertEquals(1, requests) }
    }

    @Test fun expiryContainsNoMonetaryNodes() {
        rule.setContent { MaterialTheme { BudgetScreen(CachedBudget(expired = true), false, false, {}) } }
        rule.onNodeWithTag("DailyAmount").assertDoesNotExist()
        rule.onNodeWithTag("PeriodAmount").assertDoesNotExist()
        rule.onNodeWithTag("RefreshBudget").performScrollTo().assertIsEnabled()
    }

    @Test fun fullAmountsStaySingleLineAtNarrowAndLargeFontConfigurations() {
        var fontScale by mutableFloatStateOf(1f)
        var width by mutableIntStateOf(168)
        val amount = "-HK$ 1,234,567"
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                MaterialTheme {
                    Box(Modifier.width(width.dp).testTag("AmountContainer")) { FullAmount(amount, "Amount") }
                }
            }
        }
        fun verify() {
            val results = mutableListOf<TextLayoutResult>()
            val node = rule.onNodeWithTag("Amount").assertTextEquals(amount)
            node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
            assertEquals(1, results.single().lineCount)
            assertFalse(results.single().isLineEllipsized(0))
            assertTrue(results.single().layoutInput.style.fontSize.value >= 14f)
            val actual = node.fetchSemanticsNode().boundsInRoot
            val container = rule.onNodeWithTag("AmountContainer").fetchSemanticsNode().boundsInRoot
            assertTrue(actual.left >= container.left && actual.right <= container.right + 1)
        }
        verify()
        rule.runOnIdle { fontScale = 1.5f; width = 128 }
        rule.waitForIdle()
        verify()
    }

    @Test fun readyScreenUsesFullProductionAmountComponents() {
        val snapshot = readySnapshot()
        rule.setContent { MaterialTheme { BudgetScreen(CachedBudget(snapshot), false, false, {}) } }
        rule.onNodeWithTag("DailyAmount").assertTextEquals("HK$ 120")
        rule.onNodeWithTag("PeriodAmount").assertTextEquals("HK$ 500.00")
    }

    @Test fun readyLayoutFitsNarrowLargeTextAndKeepsReadableHierarchy() {
        val snapshot = readySnapshot()
        var scale by mutableFloatStateOf(1f)
        var width by mutableIntStateOf(220)
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, scale)) {
                MaterialTheme {
                    Box(Modifier.width(width.dp).testTag("ScreenContainer")) {
                        BudgetScreen(CachedBudget(snapshot), false, false, {})
                    }
                }
            }
        }
        fun layout(tag: String): TextLayoutResult {
            val results = mutableListOf<TextLayoutResult>()
            val node = rule.onNodeWithTag(tag).performScrollTo().assertIsDisplayed()
            node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
            val text = results.single()
            assertEquals(1, text.lineCount)
            assertFalse(text.isLineEllipsized(0))
            assertTrue(text.layoutInput.style.fontSize.value >= 14f)
            val bounds = node.fetchSemanticsNode().boundsInRoot
            val container = rule.onNodeWithTag("ScreenContainer").fetchSemanticsNode().boundsInRoot
            assertTrue(bounds.left >= container.left && bounds.right <= container.right + 1)
            return text
        }
        rule.onRoot().captureToImage().asAndroidBitmap().let { image ->
            val context = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
            java.io.File(context.cacheDir, "wear-budget-fixture.png").outputStream().use {
                image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
            }
        }
        val daily = layout("DailyAmount")
        val period = layout("PeriodAmount")
        assertTrue(daily.layoutInput.style.fontSize > period.layoutInput.style.fontSize)
        rule.runOnIdle { scale = 1.5f; width = 168 }
        layout("DailyAmount")
        layout("PeriodAmount")
        rule.onNodeWithTag("BudgetList").performScrollToNode(hasTestTag("RefreshBudget"))
        rule.onNodeWithTag("RefreshBudget").assertIsDisplayed().assertHeightIsAtLeast(48.dp)
    }

    @Test fun offlineReadyBudgetRetainsAmountsAndAllowsRetry() {
        rule.setContent { MaterialTheme { BudgetScreen(CachedBudget(readySnapshot()), false, true, {}) } }
        rule.onNodeWithTag("DailyAmount").assertTextEquals("HK$ 120")
        rule.onNodeWithTag("BudgetList").performScrollToNode(hasTestTag("RefreshStatus"))
        rule.onNodeWithTag("RefreshStatus").assertIsDisplayed()
        rule.onNodeWithTag("RefreshBudget").performScrollTo().assertIsEnabled()
    }

    @Test fun refreshingDisablesRealButtonAndDoesNotRepeatRequest() {
        var requests = 0
        rule.setContent { MaterialTheme { BudgetScreen(CachedBudget(), true, false) { requests++ } } }
        rule.onNodeWithTag("RefreshBudget").performScrollTo().assertIsNotEnabled().performClick()
        rule.runOnIdle { assertEquals(0, requests) }
    }

    @Test fun negativeDailyAmountUsesSemanticWarningColor() {
        var expected = androidx.compose.ui.graphics.Color.Unspecified
        rule.setContent {
            MaterialTheme {
                expected = MaterialTheme.colorScheme.error
                BudgetScreen(CachedBudget(readySnapshot("-HK$ 120")), false, false, {})
            }
        }
        val layouts = mutableListOf<TextLayoutResult>()
        rule.onNodeWithTag("DailyAmount").assertTextEquals("-HK$ 120")
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        assertEquals(expected, layouts.single().layoutInput.style.color)
    }

    private fun readySnapshot(dailyAmount: String = "HK$ 120"): WearBudgetSnapshot {
        val now = System.currentTimeMillis()
        val today = java.time.Instant.ofEpochMilli(now).atZone(java.time.ZoneId.of("UTC")).toLocalDate()
        val expires = minOf(now + 60_000, today.plusDays(1).atStartOfDay(java.time.ZoneId.of("UTC")).toInstant().toEpochMilli())
        return WearBudgetSnapshot(1, BudgetStatus.READY, now, expires,
            today.toString(), "UTC", dailyAmount, "HK$ 500.00", 3, "MINIMAL").also { it.encode() }
    }

}
