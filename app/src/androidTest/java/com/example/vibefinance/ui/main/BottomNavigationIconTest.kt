@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.vibefinance.ui.main

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.outlined.AutoMode
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vibefinance.ui.preferences.*
import java.util.concurrent.atomic.AtomicInteger
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import kotlin.math.abs

/** ARTEMIS explored all four live bottom-navigation destinations before authoring these memory-only checks. */
@RunWith(AndroidJUnit4::class)
class BottomNavigationIconTest {
    @get:Rule val rule = createComposeRule()
    private var selected by mutableStateOf<TabItem?>(null)
    private var motion by mutableStateOf(MotionLevel.FULL)
    private val tabTriggers = mutableStateMapOf<TabItem, Int>()
    private val compositions = AtomicInteger()

    private fun show() {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            // Native ripple uses a wall clock; isolate the icon's deterministic frame-clock animation.
            CompositionLocalProvider(LocalExperience provides ExperiencePreferences(motion = motion),
                LocalRippleConfiguration provides null) {
                MaterialTheme {
                    NavigationBar(Modifier.fillMaxWidth()) {
                        TabItem.entries.forEach { tab ->
                            NavigationBarItem(selected = selected == tab, onClick = {
                                selected = tab
                                tabTriggers[tab] = (tabTriggers[tab] ?: 0) + 1
                            },
                                modifier = Modifier.testTag("Item_$tab"),
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onSurface,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                                    indicatorColor = androidx.compose.ui.graphics.Color.Transparent),
                                label = { Text(tab.name, Modifier.testTag("Label_$tab")) },
                                icon = {
                                    val active = selected == tab
                                    val vector = when (tab) {
                                        TabItem.HOME -> Icons.Filled.Home
                                        TabItem.ACCOUNTS -> Icons.Filled.AccountBalance
                                        TabItem.RECURRING -> Icons.Filled.AutoMode
                                        TabItem.HISTORY -> Icons.AutoMirrored.Filled.List
                                    }
                                    SideEffect { compositions.incrementAndGet() }
                                    // Equal vectors isolate motion from the outlined/filled crossfade.
                                    BottomNavigationIcon(tab, active, tab.name, vector, vector, tabTriggers[tab] ?: 0)
                                })
                        }
                    }
                }
            }
        }
        advance(1000)
    }
    private fun advance(ms: Long) { rule.mainClock.advanceTimeBy(ms); rule.waitForIdle() }
    private fun slot(tab: TabItem) = rule.onNodeWithTag("BottomNavIcon_${tab.name}", useUnmergedTree = true)
    private fun image(tab: TabItem) = slot(tab).captureToImage()
    private fun changed(a: ImageBitmap, b: ImageBitmap): Int {
        assertEquals(a.width, b.width); assertEquals(a.height, b.height)
        val p = a.toPixelMap(); val q = b.toPixelMap()
        var count = 0
        for (y in 0 until a.height) for (x in 0 until a.width) {
            if (abs(p[x,y].red - q[x,y].red) + abs(p[x,y].green - q[x,y].green) + abs(p[x,y].blue - q[x,y].blue) > .03f) count++
        }
        return count
    }

    @Test fun originalAutoModePathsArePreservedAndOnlyArrowIsAnimated() {
        for (original in listOf(Icons.Filled.AutoMode, Icons.Outlined.AutoMode)) {
            val fixed = autoModeLayer(original, arrows = false)
            val arrows = autoModeLayer(original, arrows = true)
            assertEquals(6, original.root.size)
            assertEquals(5, fixed.root.size)
            assertEquals(1, arrows.root.size)
            for (index in 0 until original.root.size) {
                val path = original.root[index] as VectorPath
                val copy = (if (index == 5) arrows.root[0] else fixed.root[index]) as VectorPath
                assertEquals(path.pathData, copy.pathData)
                assertEquals(path.fill, copy.fill)
                assertEquals(path.pathFillType, copy.pathFillType)
            }
            assertEquals(PathNode.MoveTo(7f, 12f), (fixed.root[4] as VectorPath).pathData.first())
            assertEquals(PathNode.MoveTo(12f, 21f), (arrows.root[0] as VectorPath).pathData.first())
        }
    }

    @Test fun eachIconMovesAndSettlesWhileSlotsLabelsAndParentCompositionStayStable() {
        show()
        for (tab in TabItem.entries) {
            rule.runOnIdle { selected = null }; advance(1000)
            val bounds = slot(tab).getUnclippedBoundsInRoot()
            val itemBounds = rule.onNodeWithTag("Item_$tab").getUnclippedBoundsInRoot()
            val labelBounds = rule.onNodeWithTag("Label_$tab", true).getUnclippedBoundsInRoot()
            rule.onNodeWithTag("Item_$tab").performClick()
            advance(128)
            val moving = image(tab)
            val count = compositions.get()
            advance(96)
            assertEquals("Animated values must not recompose icon parents", count, compositions.get())
            assertEquals(bounds, slot(tab).getUnclippedBoundsInRoot())
            assertEquals(itemBounds, rule.onNodeWithTag("Item_$tab").getUnclippedBoundsInRoot())
            assertEquals(labelBounds, rule.onNodeWithTag("Label_$tab", true).getUnclippedBoundsInRoot())
            advance(1800)
            assertTrue("$tab should have a visible one-shot motion", changed(moving, image(tab)) > 0)
            val settled = image(tab)
            advance(1000)
            assertEquals("$tab must sleep after settling", 0, changed(settled, image(tab)))
        }
    }

    @Test fun historyRowsSwipeRightThenReturnFromLeftInTopToBottomOrder() {
        show()
        val baseline = image(TabItem.HISTORY)
        fun rowChanges(frame: ImageBitmap, row: Int): Int {
            val original = baseline.toPixelMap(); val current = frame.toPixelMap()
            var changes = 0
            val top = (row * 4 + 6) * frame.height / 24
            val bottom = (row * 4 + 10) * frame.height / 24
            for (y in top until bottom) for (x in 0 until frame.width) {
                if (abs(original[x,y].red - current[x,y].red) > .03f) changes++
            }
            return changes
        }
        fun inkCenter(frame: ImageBitmap): Float {
            val pixels = frame.toPixelMap()
            val background = pixels[0, 0]
            var total = 0f; var weighted = 0f
            for (y in 6 * frame.height / 24 until 10 * frame.height / 24) {
                for (x in 0 until frame.width) {
                    val ink = abs(pixels[x,y].red - background.red)
                    total += ink; weighted += ink * x
                }
            }
            assertTrue("Top row must be visible", total > 0f)
            return weighted / total
        }
        rule.onNodeWithTag("Item_HISTORY").performClick(); advance(64)
        val first = image(TabItem.HISTORY)
        assertTrue(rowChanges(first, 0) > 0)
        assertEquals(0, rowChanges(first, 1)); assertEquals(0, rowChanges(first, 2))
        assertTrue("First row exits to the physical right", inkCenter(first) > inkCenter(baseline))
        advance(96)
        val second = image(TabItem.HISTORY)
        assertTrue(rowChanges(second, 1) > 0); assertEquals(0, rowChanges(second, 2))
        advance(96)
        val third = image(TabItem.HISTORY)
        assertTrue(rowChanges(third, 2) > 0)
        advance(80) // Sample after the returning path has crossed the left clip edge.
        assertTrue("First row returns from the physical left", inkCenter(image(TabItem.HISTORY)) < inkCenter(baseline))
        advance(2000)
        assertEquals("Original list is restored exactly", 0, changed(baseline, image(TabItem.HISTORY)))
    }

    @Test fun assetRoofColumnsAndBaseLiftIndependentlyAndReturnToOriginal() {
        show()
        val baseline = image(TabItem.ACCOUNTS)
        fun changes(frame: ImageBitmap, left: Int, top: Int, right: Int, bottom: Int): Int {
            val original = baseline.toPixelMap(); val current = frame.toPixelMap()
            var count = 0
            for (y in top * frame.height / 24 until bottom * frame.height / 24) {
                for (x in left * frame.width / 24 until right * frame.width / 24) {
                    if (abs(original[x,y].red - current[x,y].red) > .03f) count++
                }
            }
            return count
        }
        rule.onNodeWithTag("Item_ACCOUNTS").performClick(); advance(64)
        val first = image(TabItem.ACCOUNTS)
        assertTrue(changes(first, 0, 9, 8, 18) > 0)
        assertEquals(0, changes(first, 8, 9, 16, 18))
        assertEquals(0, changes(first, 16, 9, 24, 18))
        advance(96)
        val second = image(TabItem.ACCOUNTS)
        assertTrue(changes(second, 8, 9, 16, 18) > 0)
        assertEquals(0, changes(second, 16, 9, 24, 18))
        advance(96)
        val third = image(TabItem.ACCOUNTS)
        assertTrue(changes(third, 16, 9, 24, 18) > 0)
        assertTrue("Roof joins the opening lift", changes(first, 0, 0, 24, 8) > 0)
        assertEquals("Base waits for the column wave", 0, changes(first, 0, 18, 24, 24))
        advance(128)
        assertTrue("Base follows with its own lift", changes(image(TabItem.ACCOUNTS), 0, 18, 24, 24) > 0)
        advance(2000)
        assertEquals("Original bank returns exactly", 0, changed(baseline, image(TabItem.ACCOUNTS)))
    }

    @Test fun reducedAndMinimalMotionSwitchWithoutTransforms() {
        show()
        for (level in listOf(MotionLevel.REDUCED, MotionLevel.MINIMAL)) {
            rule.runOnIdle { motion = level }
            for (tab in TabItem.entries) {
                rule.onNodeWithTag("Item_$tab").performClick(); advance(64)
                val first = image(tab)
                advance(1200)
                assertEquals("$level must not animate $tab geometry", 0, changed(first, image(tab)))
            }
        }
    }

    @Test fun rapidDeselectionAndMotionChangeCancelThePreviousAnimation() {
        show()
        rule.onNodeWithTag("Item_RECURRING").performClick(); advance(128)
        rule.onNodeWithTag("Item_HOME").performClick(); advance(128)
        rule.onNodeWithTag("Item_ACCOUNTS").performClick(); advance(128)
        rule.onNodeWithTag("Item_HISTORY").performClick(); advance(128)
        rule.runOnIdle { motion = MotionLevel.MINIMAL }; advance(64)
        val reset = TabItem.entries.map(::image)
        advance(1800)
        TabItem.entries.forEachIndexed { i, tab -> assertEquals(0, changed(reset[i], image(tab))) }
        rule.onNodeWithTag("Item_HISTORY").assertIsSelected()
    }

    @Test fun reselectingAlreadySelectedTabReplaysAnimation() {
        show()
        // Select recurring and wait to settle
        rule.onNodeWithTag("Item_RECURRING").performClick()
        advance(2000)
        val settled = image(TabItem.RECURRING)

        // Reselect already selected recurring tab
        rule.onNodeWithTag("Item_RECURRING").performClick()
        advance(128)
        val moving = image(TabItem.RECURRING)
        assertTrue("Reselecting already selected tab must replay motion", changed(settled, moving) > 0)

        // Wait to settle again
        advance(2000)
        val settledAgain = image(TabItem.RECURRING)
        assertEquals("Must return to settled state after replay", 0, changed(settled, settledAgain))
    }
}
