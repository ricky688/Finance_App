package com.example.vibefinance.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vibefinance.ui.preferences.ExperiencePreferences
import com.example.vibefinance.ui.preferences.LocalExperience
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Daily -> Habit and horizontal swipes observed with ARTEMIS/ADB on Waydroid before authoring. */
@RunWith(AndroidJUnit4::class)
class HeatmapEdgeThemeTest {
    @get:Rule val rule = createComposeRule()
    @Test fun fadesFollowThemeAndScrollLimits() = verifyEdges(1f)
    @Test fun translucentFallbackFollowsThemeWithBlurDisabled() = verifyEdges(0f)

    private fun verifyEdges(blur: Float) {
        var dark by mutableStateOf(false)
        rule.mainClock.autoAdvance = false
        rule.setContent {
            val background = if (dark) Color.Black else Color.White
            val colors = if (dark) darkColorScheme(background = background, surfaceContainer = background)
                else lightColorScheme(background = background, surfaceContainer = background)
            CompositionLocalProvider(LocalExperience provides ExperiencePreferences(blur = blur)) {
                MaterialTheme(colors) {
                    Box(Modifier.width(280.dp).background(background)) {
                        M3ExpressiveHeatmap(emptyList(), 100.0, null, {}, {})
                    }
                }
            }
        }
        settle()
        rule.onNodeWithTag("HabitHeatmap_start_fade").assertDoesNotExist()
        rule.onNodeWithTag("HabitHeatmap_end_fade").assertExists()
        assertEdge(dark = false, left = false)
        rule.onNodeWithTag("HabitHeatmapScroll").performTouchInput { swipeLeft(durationMillis = 500) }
        settle()
        rule.onNodeWithTag("HabitHeatmap_start_fade").assertExists()
        rule.onNodeWithTag("HabitHeatmap_end_fade").assertDoesNotExist()
        assertEdge(dark = false, left = true)
        rule.runOnIdle { dark = true }; settle()
        assertEdge(dark = true, left = true)
        rule.onNodeWithTag("HabitHeatmapScroll").performTouchInput { swipeRight(durationMillis = 500) }
        settle()
        rule.onNodeWithTag("HabitHeatmap_start_fade").assertDoesNotExist()
        rule.onNodeWithTag("HabitHeatmap_end_fade").assertExists()
        assertEdge(dark = true, left = false)
        rule.runOnIdle { dark = false }; settle()
        assertEdge(dark = false, left = false)
    }

    private fun settle() { rule.mainClock.advanceTimeBy(1_500); rule.waitForIdle() }
    private fun assertEdge(dark: Boolean, left: Boolean) {
        val bounds = rule.onNodeWithTag("HabitHeatmapViewport").fetchSemanticsNode().boundsInRoot
        val pixels = rule.onRoot().captureToImage().toPixelMap()
        val edgeX = if (left) bounds.left.toInt() + 1 else bounds.right.toInt() - 8
        var total = 0.0; var count = 0
        for (x in edgeX until edgeX + 7) {
            for (y in bounds.top.toInt() + 2 until bounds.bottom.toInt() - 2) {
                total += pixels[x, y].luminance(); count++
            }
        }
        val average = total / count
        assertTrue("Edge must follow ${if (dark) "dark" else "light"} theme, luminance=$average",
            if (dark) average < 0.35 else average > 0.65)
    }
}
