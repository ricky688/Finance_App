package com.example.vibefinance.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vibefinance.ui.preferences.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Repeated five-tap logo triggers explored with ARTEMIS on Waydroid before authoring. */
@RunWith(AndroidJUnit4::class)
class FlyingCashOverlayTest {
    @get:Rule val rule = createComposeRule()

    @Test fun fullMotionKeepsOlderFlockUntilItsOwnCompletion() = overlap(MotionLevel.FULL)
    @Test fun reducedMotionAlsoKeepsIndependentFlocks() = overlap(MotionLevel.REDUCED)

    private fun overlap(motion: MotionLevel) {
        var trigger by mutableLongStateOf(101L)
        var completions = 0
        var clicks = 0
        rule.mainClock.autoAdvance = false
        rule.setContent {
            CompositionLocalProvider(LocalExperience provides ExperiencePreferences(motion = motion)) {
                MaterialTheme {
                    Box {
                        Button(onClick = { clicks++ }) { Text("Underlying control") }
                        FlyingCashOverlay(triggerKey = trigger, onCompleted = { completions++; trigger = 0 })
                    }
                }
            }
        }
        fun advance(ms: Long) { rule.mainClock.advanceTimeBy(ms); rule.waitForIdle() }
        fun nodes() = rule.onAllNodesWithTag("FlyingCashOverlay").fetchSemanticsNodes()
        advance(100)
        assertEquals(1, nodes().size)
        val firstId = nodes().single().id
        advance(1000)
        rule.runOnIdle { trigger = 202L }
        advance(64)
        assertEquals("New trigger adds another flock", 2, nodes().size)
        assertEquals("Existing animation stays in composition", firstId, nodes().first().id)
        val secondId = nodes().last().id
        rule.onNodeWithText("Underlying control").performTouchInput { click() }
        rule.runOnIdle { assertEquals(1, clicks) }
        advance(2600)
        assertEquals("Only the older flock finishes", secondId, nodes().single().id)
        rule.runOnIdle { assertEquals("Older completion cannot clear the caller trigger", 0, completions) }
        advance(1400)
        assertTrue(nodes().isEmpty())
        rule.runOnIdle { assertEquals(1, completions); assertEquals(0L, trigger) }
    }

    @Test fun minimalMotionCompletesWithoutDrawingOrLeavingAnActiveOverlay() {
        var trigger by mutableLongStateOf(1L)
        var completions = 0
        rule.setContent {
            CompositionLocalProvider(LocalExperience provides ExperiencePreferences(motion = MotionLevel.MINIMAL)) {
                FlyingCashOverlay(triggerKey = trigger, onCompleted = { completions++; trigger = 0 })
            }
        }
        rule.waitUntil(5_000) { completions == 1 }
        rule.onNodeWithTag("FlyingCashOverlay").assertDoesNotExist()
        rule.runOnIdle { trigger = 2 }
        rule.waitUntil(5_000) { completions == 2 }
        rule.onNodeWithTag("FlyingCashOverlay").assertDoesNotExist()
    }
}
