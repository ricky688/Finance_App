package com.example.vibefinance.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vibefinance.ui.main.ExpressiveSegmentedButtonGroup
import com.example.vibefinance.ui.main.ConnectedButtonCorners
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** ARTEMIS screenshots + held ADB pointers verified the actual entry path on Waydroid.
 * Isolated component fixtures and the Compose clock sample motion without writing app data.
 */
@RunWith(AndroidJUnit4::class)
class FastSpatialMotionTest {
    @get:Rule val rule = createComposeRule()
    private var density = 1f
    private fun advance(ms: Long) { rule.mainClock.advanceTimeBy(ms); rule.waitForIdle() }
    private fun label(text: String) = rule.onNodeWithText(text, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

    private fun neighborCheck(direction: LayoutDirection) {
        rule.mainClock.autoAdvance = false
        var clicks = 0
        rule.setContent {
            density = LocalDensity.current.density
            CompositionLocalProvider(LocalLayoutDirection provides direction) {
                MaterialTheme {
                    ExpressiveSegmentedButtonGroup(listOf("Expense", "Income", "Transfer"), 1,
                        { clicks++ }, Modifier.width(360.dp).testTag("Group"),
                        itemModifierProvider = { Modifier.testTag("Mode_$it") }, labelProvider = { it })
                }
            }
        }
        advance(1_000)
        val before = listOf("Expense", "Income", "Transfer").map(::label)
        val pressedBounds = rule.onNodeWithTag("Mode_1").fetchSemanticsNode().boundsInRoot
        val group = rule.onNodeWithTag("Group").fetchSemanticsNode().boundsInRoot
        rule.onNodeWithTag("Mode_1").performTouchInput { down(center) }
        advance(1_500)
        val sign = if (direction == LayoutDirection.Ltr) 1 else -1
        assertEquals(-3f * sign * density, label("Expense").center.x - before[0].center.x, 0.15f)
        assertEquals(3f * sign * density, label("Transfer").center.x - before[2].center.x, 0.15f)
        // The checkmark offsets the label from the button center; scaling pulls it inward.
        val expectedLabelCenter = pressedBounds.center.x + (before[1].center.x - pressedBounds.center.x) * 0.95f
        assertEquals(expectedLabelCenter, label("Income").center.x, 0.15f)
        val heldBounds = rule.onNodeWithTag("Mode_1").fetchSemanticsNode().boundsInRoot
        assertEquals(pressedBounds.center.x, heldBounds.center.x, 0.1f)
        assertEquals(pressedBounds.center.y, heldBounds.center.y, 0.1f)
        assertTrue(label("Income").width < before[1].width)
        assertEquals(group, rule.onNodeWithTag("Group").fetchSemanticsNode().boundsInRoot)
        rule.onNodeWithTag("Mode_1").performTouchInput { cancel() }
        advance(2_000)
        listOf("Expense", "Income", "Transfer").forEachIndexed { index, text ->
            assertEquals(before[index].center.x, label(text).center.x, 0.15f)
        }
        assertEquals(0, clicks)
        rule.onNodeWithTag("Mode_1").performTouchInput { click() }
        advance(2_000)
        assertEquals(1, clicks)
    }

    @Test fun neighborsMoveOutwardAndCancellationRestoresThem() = neighborCheck(LayoutDirection.Ltr)
    @Test fun neighborsMoveOutwardInRtlReadingOrder() = neighborCheck(LayoutDirection.Rtl)

    @Test fun entryModeButtonsKeepLabelsAndNeighborsAtFullSize() = entryShapeOnlyCheck(false)
    @Test fun entrySelectorButtonsKeepLabelsAndNeighborsAtFullSize() = entryShapeOnlyCheck(true)

    private fun entryShapeOnlyCheck(scrollable: Boolean) {
        rule.mainClock.autoAdvance = false
        var clicks = 0
        val labels = listOf("Expense", "Income", "Transfer")
        rule.setContent {
            MaterialTheme {
                ExpressiveSegmentedButtonGroup(labels, 0, { clicks++ },
                    Modifier.width(360.dp).testTag("EntryGroup"), isScrollable = scrollable,
                    corners = ConnectedButtonCorners.Entry, spatialMotionEnabled = false,
                    itemModifierProvider = { Modifier.heightIn(min = 48.dp).testTag("Entry_$it") },
                    labelProvider = { it })
            }
        }
        advance(1_000)
        val before = labels.map(::label)
        val slots = labels.indices.map { rule.onNodeWithTag("Entry_$it").fetchSemanticsNode().boundsInRoot }
        val group = rule.onNodeWithTag("EntryGroup").fetchSemanticsNode().boundsInRoot
        rule.onNodeWithTag("Entry_1").performTouchInput { down(center) }
        advance(1_500)
        labels.forEachIndexed { index, text ->
            assertEquals("Press must not shrink or move $text", before[index], label(text))
            assertEquals(slots[index], rule.onNodeWithTag("Entry_$index").fetchSemanticsNode().boundsInRoot)
        }
        assertEquals(group, rule.onNodeWithTag("EntryGroup").fetchSemanticsNode().boundsInRoot)
        rule.onNodeWithTag("Entry_1").performTouchInput { cancel() }
        advance(700)
        labels.forEachIndexed { index, text -> assertEquals(before[index], label(text)) }
        assertEquals(0, clicks)
        rule.onNodeWithTag("Entry_1").performTouchInput { click() }
        advance(700)
        assertEquals(1, clicks)
    }

    @Test fun keypadCentersStayFixedAndFeedbackOccursOnlyOnTouchDown() {
        rule.mainClock.autoAdvance = false
        var ticks = 0
        var clicks = 0
        var enabled by mutableStateOf(true)
        rule.setContent {
            MaterialTheme {
                Row(Modifier.width(360.dp).height(54.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (7..9).forEach { key ->
                        KeyboardButton(Modifier.weight(1f).testTag("Key_$key"), KeyboardButtonType.DEFAULT,
                            text = key.toString(), enabled = enabled, cornerRadius = 24.dp,
                            feedback = { ticks++ }, onClick = { clicks++ })
                    }
                }
            }
        }
        advance(500)
        val before = (7..9).map { label(it.toString()) }
        val hitBounds = (7..9).map { rule.onNodeWithTag("Key_$it").fetchSemanticsNode().boundsInRoot }
        rule.onNodeWithTag("Key_8").performTouchInput { down(center) }
        advance(1_500)
        assertEquals(1, ticks)
        assertEquals(0, clicks)
        (7..9).forEachIndexed { index, key ->
            assertEquals(before[index].center.x, label(key.toString()).center.x, 0.15f)
            assertEquals(before[index].center.y, label(key.toString()).center.y, 0.15f)
            assertEquals(hitBounds[index], rule.onNodeWithTag("Key_$key").fetchSemanticsNode().boundsInRoot)
        }
        assertEquals("Held keys must not shrink their labels", before[1], label("8"))
        assertEquals(before[0], label("7"))
        assertEquals(before[2], label("9"))
        rule.onNodeWithTag("Key_8").performTouchInput { up() }
        advance(2_000)
        assertEquals(1, ticks)
        assertEquals(1, clicks)
        assertEquals(before[1].width, label("8").width, 0.15f)
        rule.runOnIdle { enabled = false }
        advance(32) // Recompose the disabled clickable before injecting its next pointer.
        rule.onNodeWithTag("Key_8").performTouchInput { click() }
        advance(500)
        assertEquals(1, ticks)
        assertEquals(1, clicks)
    }

    @Test fun selectedChipPushesSiblingsContinuouslyWithoutChangingHeight() {
        rule.mainClock.autoAdvance = false
        var selected by mutableIntStateOf(-1)
        rule.setContent {
            MaterialTheme {
                ExpressiveSegmentedButtonGroup(listOf("Salary", "Bonus", "Investment"), selected,
                    { selected = it }, Modifier.width(360.dp).testTag("Chips"), isScrollable = true,
                    itemModifierProvider = { Modifier.testTag("Chip_$it") }, labelProvider = { it })
            }
        }
        advance(1_000)
        val before = label("Investment").center.x
        val height = rule.onNodeWithTag("Chips").fetchSemanticsNode().boundsInRoot.height
        rule.onNodeWithTag("Chip_0").performTouchInput { click() }
        val samples = (1..10).map { advance(32); label("Investment").center.x }
        advance(2_000)
        val after = label("Investment").center.x
        assertTrue("Selection should push following chips", after > before + 2f)
        assertTrue("Chip width should move through intermediate frames: $before -> $samples -> $after",
            samples.any { it > before && it < after })
        assertEquals(height, rule.onNodeWithTag("Chips").fetchSemanticsNode().boundsInRoot.height, 0.1f)
    }
}
