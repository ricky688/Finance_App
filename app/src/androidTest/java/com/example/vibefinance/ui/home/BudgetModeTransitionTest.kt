@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.vibefinance.ui.home

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vibefinance.ui.preferences.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

/** Remaining -> Spent -> Remaining explored with ARTEMIS/ADB on Waydroid before authoring. */
@RunWith(AndroidJUnit4::class)
class BudgetModeTransitionTest {
    @get:Rule val rule = createComposeRule()

    private fun show(motion: MotionLevel, dark: Boolean = false) {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            CompositionLocalProvider(LocalExperience provides ExperiencePreferences(motion = motion),
                LocalAmbientMotionEnabled provides false) {
                MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme(),
                    motionScheme = MotionScheme.expressive()) {
                    // Native ripple runs outside the paused Compose clock; isolate the mode transition.
                    CompositionLocalProvider(LocalRippleConfiguration provides null) {
                        RestAndSpentBudgetCard(remainingBudget = 2439.5, totalBudget = 2500.0,
                            isDarkTheme = dark, modifier = Modifier.width(240.dp).height(160.dp))
                    }
                }
            }
        }
        advance(1500)
    }
    private fun advance(ms: Long) { rule.mainClock.advanceTimeBy(ms); rule.waitForIdle() }
    private fun card() = rule.onNodeWithTag("RemainingBudgetCard")
    private fun image() = card().captureToImage()
    private fun assertAmount(spent: Boolean) {
        rule.onNodeWithTag(if (spent) "BudgetMode_SPENT" else "BudgetMode_REMAINING", true).assertExists()
        rule.onNodeWithText(if (spent) "$60.50" else "$2,439.50", useUnmergedTree = true).assertExists()
    }

    @Test fun fullMotion_hasIntermediateFrames_fixedBounds_andRapidReversalKeepsExactAmounts() {
        show(MotionLevel.FULL)
        val bounds = card().fetchSemanticsNode().boundsInRoot
        val remaining = image()
        assertAmount(false)
        card().performClick(); advance(64)
        val intermediate = image()
        assertEquals(bounds, card().fetchSemanticsNode().boundsInRoot)
        advance(1500)
        assertAmount(true)
        val spent = image()
        assertTrue(changedPixels(remaining, intermediate) > 20)
        assertTrue(changedPixels(intermediate, spent) > 20)
        card().performClick(); advance(64)
        card().performClick(); advance(1500)
        assertAmount(true)
        assertTrue("Rapid reversal must converge on Spent", changedPixels(spent, image()) < 10)
        card().performClick(); advance(1500)
        assertAmount(false)
        assertEquals(bounds, card().fetchSemanticsNode().boundsInRoot)
        assertTrue(changedPixels(remaining, image()) < 10)
    }

    @Test fun reducedMotion_fadesInDarkTheme_thenSettles() {
        show(MotionLevel.REDUCED, dark = true)
        card().performClick(); advance(32)
        val intermediate = image()
        advance(1000)
        assertAmount(true)
        val settled = image()
        assertTrue(changedPixels(intermediate, settled) > 20)
        advance(500)
        assertEquals(0, changedPixels(settled, image()))
    }

    @Test fun minimalMotion_switchesImmediately_withNoLingeringContent() {
        show(MotionLevel.MINIMAL)
        card().performClick(); advance(32)
        assertAmount(true)
        rule.onNodeWithTag("BudgetMode_REMAINING", true).assertDoesNotExist()
        val spent = image()
        advance(1500)
        assertEquals(0, changedPixels(spent, image()))
        card().performClick(); advance(32)
        assertAmount(false)
        rule.onNodeWithTag("BudgetMode_SPENT", true).assertDoesNotExist()
    }

    @Test fun returningToCardRetainsAmountBoundsThroughoutSettling() {
        // Daily card interactions were explored with ARTEMIS on Waydroid; this fixture never writes finances.
        var visible by androidx.compose.runtime.mutableStateOf(true)
        rule.mainClock.autoAdvance = false
        rule.setContent {
            CompositionLocalProvider(LocalAmbientMotionEnabled provides false) {
                MaterialTheme {
                    if (visible) RestAndSpentBudgetCard(remainingBudget = 2439.5, totalBudget = 2500.0,
                        modifier = Modifier.width(240.dp).height(160.dp))
                }
            }
        }
        val amount = rule.onNodeWithText("$2,439.50", useUnmergedTree = true)
        val expected = amount.fetchSemanticsNode().boundsInRoot
        fun checkFrames() {
            repeat(12) {
                rule.mainClock.advanceTimeByFrame()
                rule.waitForIdle()
                assertEquals("Amount must not jump while the card settles", expected, amount.fetchSemanticsNode().boundsInRoot)
            }
        }
        checkFrames()
        rule.runOnIdle { visible = false }
        advance(32)
        rule.onNodeWithTag("RemainingBudgetCard").assertDoesNotExist()
        rule.runOnIdle { visible = true }
        checkFrames()
    }

    private fun changedPixels(a: ImageBitmap, b: ImageBitmap): Int {
        assertEquals(a.width, b.width); assertEquals(a.height, b.height)
        val p = a.toPixelMap(); val q = b.toPixelMap()
        var changed = 0
        for (y in 0 until a.height) for (x in 0 until a.width) {
            if (abs(p[x, y].red - q[x, y].red) + abs(p[x, y].green - q[x, y].green) +
                abs(p[x, y].blue - q[x, y].blue) > .04f) changed++
        }
        return changed
    }
}
