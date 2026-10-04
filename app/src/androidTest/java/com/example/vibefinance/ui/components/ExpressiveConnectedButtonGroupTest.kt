package com.example.vibefinance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.concurrent.CopyOnWriteArrayList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Both History selector paths were explored with ARTEMIS and ADB before these regressions. */
@RunWith(AndroidJUnit4::class)
class ExpressiveConnectedButtonGroupTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun unselectedMiddleButton_morphsDuringHoldThenSelectsOnReleaseAndRestoresOnCancel() {
        rule.mainClock.autoAdvance = false
        val labels = listOf("All records", "Active period", "Past periods")
        val selections = CopyOnWriteArrayList<Int>()
        showGroup(labels, initialSelection = 0, selections = selections)
        settle()
        assertRadioButtons(labels, selectedIndex = 0)

        val middle = rule.onNodeWithText(labels[1])
        val resting = middle.captureToImage()
        heldPress(labels[1], release = true) { button ->
            // A pressed unchecked button must morph before its checked state changes.
            button.assertIsNotSelected()
            rule.onNodeWithText(labels[0]).assertIsSelected()
            assertTrue("Holding must not invoke selection", selections.isEmpty())
            assertTrue(
                "The unselected middle button's clipped corners did not morph while held",
                changedCornerCoverage(resting, button.captureToImage()) > MIN_CHANGED_PIXELS
            )
        }
        rule.waitUntil(TIMEOUT_MILLIS) { selections.toList() == listOf(1) }
        assertRadioButtons(labels, selectedIndex = 1)

        rule.onNodeWithText(labels[0]).performClick()
        settle()
        rule.waitUntil(TIMEOUT_MILLIS) { selections.toList() == listOf(1, 0) }
        assertRadioButtons(labels, selectedIndex = 0)
        val restingBeforeCancel = middle.captureToImage()
        heldPress(labels[1], release = false) { button ->
            button.assertIsNotSelected()
            rule.onNodeWithText(labels[0]).assertIsSelected()
            assertTrue(
                "The second held press must still morph the unchecked middle button",
                changedCornerCoverage(restingBeforeCancel, button.captureToImage()) > MIN_CHANGED_PIXELS
            )
        }

        assertRadioButtons(labels, selectedIndex = 0)
        assertEquals("Pointer cancellation must not select a button", listOf(1, 0), selections.toList())
        assertEquals(
            "Cancelled press must restore the resting clipped silhouette",
            0,
            changedCornerCoverage(restingBeforeCancel, middle.captureToImage())
        )
    }

    @Test
    fun twoOptions_preserveRadioSemanticsAndMorphOnDeselectionAndCancelledPress() {
        rule.mainClock.autoAdvance = false
        val labels = listOf("Month", "Budget period")
        val selections = CopyOnWriteArrayList<Int>()
        showGroup(labels, initialSelection = 1, selections = selections)
        settle()
        assertRadioButtons(labels, selectedIndex = 1)

        val month = rule.onNodeWithText(labels[0])
        val budget = rule.onNodeWithText(labels[1])
        val monthResting = month.captureToImage()
        val budgetChecked = budget.captureToImage()
        heldPress(labels[0], release = true) { button ->
            button.assertIsNotSelected()
            budget.assertIsSelected()
            assertTrue("Holding must retain Budget period selection", selections.isEmpty())
            assertTrue(
                "An unchecked leading button must morph while held",
                changedCornerCoverage(monthResting, button.captureToImage()) > MIN_CHANGED_PIXELS
            )
        }

        rule.waitUntil(TIMEOUT_MILLIS) { selections.toList() == listOf(0) }
        assertRadioButtons(labels, selectedIndex = 0)
        val budgetUnchecked = budget.captureToImage()
        assertTrue(
            "Deselecting the trailing button must change its clipped silhouette",
            changedCornerCoverage(budgetChecked, budgetUnchecked) > MIN_CHANGED_PIXELS
        )
        heldPress(labels[1], release = false) { button ->
            button.assertIsNotSelected()
            month.assertIsSelected()
            assertTrue(
                "An unchecked trailing button must morph while held",
                changedCornerCoverage(budgetUnchecked, button.captureToImage()) > MIN_CHANGED_PIXELS
            )
        }

        assertRadioButtons(labels, selectedIndex = 0)
        assertEquals("Cancelling the trailing press must retain Month", listOf(0), selections.toList())
        assertEquals(
            "The trailing button must regain its unchecked silhouette after cancellation",
            0,
            changedCornerCoverage(budgetUnchecked, budget.captureToImage())
        )
    }

    private fun showGroup(labels: List<String>, initialSelection: Int, selections: MutableList<Int>) {
        rule.setContent {
            var selectedIndex by remember { mutableIntStateOf(initialSelection) }
            // Inactive fill is gray over white. Its black ripple only changes already-filled pixels.
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color.Black,
                    onPrimary = Color.White,
                    primaryContainer = Color.Black,
                    onPrimaryContainer = Color.Black,
                    background = Color.White,
                    surface = Color.White
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    ExpressiveConnectedButtonGroup(
                        items = labels,
                        selectedIndex = selectedIndex,
                        onItemSelected = {
                            selectedIndex = it
                            selections.add(it)
                        },
                        modifier = Modifier.width(320.dp),
                        labelProvider = { it }
                    )
                }
            }
        }
    }

    private fun assertRadioButtons(labels: List<String>, selectedIndex: Int) {
        labels.forEachIndexed { index, label ->
            val button = rule.onNodeWithText(label)
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton))
                .assertHeightIsAtLeast(48.dp)
            if (index == selectedIndex) button.assertIsSelected() else button.assertIsNotSelected()
        }
    }

    private fun heldPress(
        label: String,
        release: Boolean,
        assertions: (SemanticsNodeInteraction) -> Unit
    ) {
        val button = rule.onNodeWithText(label)
        // The framework retains this pointer across calls, allowing capture before up/cancel.
        button.performTouchInput { down(center) }
        try {
            rule.mainClock.advanceTimeBy(800)
            rule.waitForIdle()
            assertions(button)
        } finally {
            button.performTouchInput {
                if (release) up() else cancel()
            }
            settle()
        }
    }

    private fun settle() {
        rule.mainClock.advanceTimeBy(2_000)
        rule.waitForIdle()
    }

    /** Compare geometry, excluding central glyphs and color-only ripple/fade changes. */
    private fun changedCornerCoverage(before: ImageBitmap, after: ImageBitmap): Int {
        assertEquals(before.width, after.width)
        assertEquals(before.height, after.height)
        val first = before.toPixelMap()
        val second = after.toPixelMap()
        var changed = 0
        for (y in 0 until first.height) {
            val nearTopOrBottom = y < first.height / 4 || y >= first.height * 3 / 4
            if (!nearTopOrBottom) continue
            for (x in 0 until first.width) {
                val nearLeftOrRight = x < first.width / 4 || x >= first.width * 3 / 4
                if (!nearLeftOrRight) continue
                val a = first[x, y]
                val b = second[x, y]
                // The wide confidence gap rejects antialiasing and small GPU dithering changes.
                if ((isBackground(a) && isFill(b)) || (isFill(a) && isBackground(b))) changed++
            }
        }
        return changed
    }

    private fun isBackground(color: Color): Boolean =
        color.red > 0.97f && color.green > 0.97f && color.blue > 0.97f

    private fun isFill(color: Color): Boolean =
        color.red < 0.85f && color.green < 0.85f && color.blue < 0.85f

    private companion object {
        const val TIMEOUT_MILLIS = 5_000L
        const val MIN_CHANGED_PIXELS = 4
    }
}
