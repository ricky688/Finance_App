@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vibefinance.ui.FinanceUiState
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** ARTEMIS/ADB explored Settings opening, nested Color scheme/Back, scrim and swipe first.
 * Real production drawer host/session and picker, with memory-only callbacks/preferences.
 */
@RunWith(AndroidJUnit4::class)
class SettingsDrawerTest {
    @get:Rule val rule = createComposeRule()
    private lateinit var drawer: DrawerState
    private var dismissals = 0
    private var backgroundClicks = 0
    private val settledCallbacks = mutableListOf<Boolean>()
    private val parentMotion = MotionScheme.standard()
    private var contentMotion: MotionScheme? = null
    private var drawerContentDirection: LayoutDirection? = null
    private var pageDirection: LayoutDirection? = null

    private fun show() {
        rule.setContent {
            MaterialTheme(motionScheme = parentMotion) {
                drawer = rememberDrawerState(DrawerValue.Closed)
                var visible by remember { mutableStateOf(false) }
                SettingsDrawerHost(drawer, drawerContent = {
                    if (visible) {
                        val close = rememberSettingsDrawerClose(drawer) {
                            settledCallbacks += drawer.isClosed && !drawer.isAnimationRunning
                            dismissals++
                            visible = false
                        }
                        contentMotion = MaterialTheme.motionScheme
                        drawerContentDirection = LocalLayoutDirection.current
                        var palette by remember { mutableStateOf(false) }
                        Column {
                            Button(close, Modifier.testTag("DrawerCloseFixture")) { Text("Close Settings") }
                            Button({ palette = true }, Modifier.testTag("DrawerPaletteFixture")) { Text("Color scheme") }
                        }
                        if (palette) AppearancePickerSheet(FinanceUiState(isLoading = false), {}, { palette = false })
                    }
                }) {
                    pageDirection = LocalLayoutDirection.current
                    Column(Modifier.fillMaxSize()) {
                        Button({ visible = true }, Modifier.testTag("DrawerOpenFixture")) { Text("Settings") }
                        Button({ backgroundClicks++ }, Modifier.testTag("DrawerBackgroundFixture")) { Text("Background action") }
                    }
                }
            }
        }
    }

    private fun open() {
        rule.onNodeWithTag("DrawerOpenFixture").performClick()
        rule.waitUntil(5_000) { drawer.isOpen && !drawer.isAnimationRunning }
        rule.onNodeWithTag("SettingsDrawer").assertIsDisplayed()
        assertSame(parentMotion, contentMotion)
        assertEquals("Settings text and controls must retain their reading direction", LayoutDirection.Ltr, drawerContentDirection)
        assertEquals("The underlying page must retain its reading direction", LayoutDirection.Ltr, pageDirection)
        val pane = rule.onNodeWithTag("SettingsDrawer").fetchSemanticsNode().boundsInRoot
        val window = rule.onRoot().fetchSemanticsNode().boundsInRoot
        assertEquals("Settings must anchor to the physical right edge", window.right, pane.right, 1f)
        with(rule.density) {
            assertTrue("Keep at least 56dp of touchable scrim on the left", pane.left - window.left >= 56.dp.toPx() - 1f)
            assertTrue("Drawer must not exceed 360dp", pane.width <= 360.dp.toPx() + 1f)
        }
    }

    private fun closed(count: Int) {
        try {
            rule.waitUntil(5_000) { dismissals == count && drawer.isClosed && !drawer.isAnimationRunning }
        } catch (error: androidx.compose.ui.test.ComposeTimeoutException) {
            throw AssertionError("Expected $count callbacks, got $dismissals; state=${drawer.currentValue}, target=${drawer.targetValue}, offset=${drawer.currentOffset}, running=${drawer.isAnimationRunning}", error)
        }
        rule.onNodeWithTag("DrawerCloseFixture").assertDoesNotExist()
        assertTrue("Settings must remain composed until closing settles", settledCallbacks.all { it })
        assertEquals(0, backgroundClicks)
    }

    @Test fun closeBackScrimAndSwipe_completeOnce_andAllowReopening() {
        show()
        open()
        rule.onNodeWithTag("DrawerCloseFixture").performClick()
        closed(1)
        open()
        pressBack()
        closed(2)
        open()
        try {
            // Native scrim semantics span the whole window, including the drawer-covered
            // center. Invoke its accessibility action rather than tapping that center.
            rule.onNodeWithContentDescription("Close navigation menu")
                .performSemanticsAction(SemanticsActions.OnClick) { it() }
        } catch (_: AssertionError) {
            // Right-edge drawer leaves the scrim on the left (verified on Waydroid).
            rule.onRoot().performTouchInput { click(Offset(width * 0.05f, height * 0.5f)) }
        }
        closed(3)
        open()
        rule.onNodeWithTag("SettingsDrawer").performTouchInput {
            swipe(Offset(24f, 80f), Offset(width - 24f, 80f), durationMillis = 350)
        }
        closed(4)
        open()
        rule.onNodeWithContentDescription("Close navigation menu").performTouchInput {
            click(Offset(width * 0.05f, height * 0.5f))
        }
        closed(5)
        rule.onNodeWithTag("DrawerBackgroundFixture").performClick()
        assertEquals(1, backgroundClicks)
    }

    @Test fun nestedColorPicker_backReturnsToDrawer_beforeClosingSettings() {
        show()
        open()
        rule.onNodeWithTag("DrawerPaletteFixture").performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("PaletteStyleOpen").fetchSemanticsNodes().size == 1 }
        pressBack()
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("PaletteStyleOpen").fetchSemanticsNodes().isEmpty() }
        rule.onNodeWithTag("SettingsDrawer").assertIsDisplayed()
        assertTrue(drawer.isOpen)
        assertEquals(0, dismissals)
        rule.onNodeWithTag("DrawerCloseFixture").performClick()
        closed(1)
    }

    @Test fun slideUsesEasedBoundedMotion_andKeepsContentUntilSettled() {
        rule.mainClock.autoAdvance = false
        show()
        rule.mainClock.advanceTimeBy(1_000)
        rule.waitForIdle()
        val closedOffset = drawer.currentOffset
        rule.onNodeWithTag("DrawerOpenFixture").performClick()
        rule.mainClock.advanceTimeByFrame()
        val opening = sampleProgress(closedOffset, 0f)
        assertEmphasizedDecelerate(opening)
        assertTrue(drawer.isOpen)
        rule.onNodeWithTag("DrawerCloseFixture").performClick()
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeBy(64)
        assertEquals("Do not unmount on press", 0, dismissals)
        rule.onNodeWithTag("DrawerCloseFixture").assertExists()
        val early = drawer.currentOffset / closedOffset
        rule.mainClock.advanceTimeBy(64)
        val middle = drawer.currentOffset / closedOffset
        rule.mainClock.advanceTimeBy(128)
        val late = drawer.currentOffset / closedOffset
        rule.mainClock.advanceTimeBy(128)
        rule.waitForIdle()
        assertEmphasizedDecelerate(listOf(early, middle, late))
        assertTrue(drawer.isClosed)
        assertEquals(1, dismissals)
        assertTrue(settledCallbacks.single())
    }

    private fun sampleProgress(start: Float, end: Float): List<Float> {
        val samples = mutableListOf<Float>()
        listOf(64L, 64L, 128L).forEach { elapsed ->
            rule.mainClock.advanceTimeBy(elapsed)
            samples += (drawer.currentOffset - start) / (end - start)
        }
        rule.mainClock.advanceTimeBy(128)
        rule.waitForIdle()
        return samples
    }

    private fun assertEmphasizedDecelerate(values: List<Float>) {
        assertTrue("Bounded, ordered slide: $values", values.all { it in 0f..1f } && values.zipWithNext().all { it.first < it.second })
        // Samples at 64/128/256ms: prompt initial travel, then a soft landing.
        assertTrue("Emphasized initial response: $values", values[0] > 0.6f && values[0] < 0.9f)
        assertTrue("Decelerating approach: $values", values[1] > 0.88f)
        assertTrue("Soft landing: $values", values[2] > 0.98f)
        assertTrue("Travel per millisecond must decrease: $values",
            values[0] / 64f > (values[1] - values[0]) / 64f &&
                (values[1] - values[0]) / 64f > (values[2] - values[1]) / 128f)
    }
}
