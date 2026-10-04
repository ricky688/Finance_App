package com.example.vibefinance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vibefinance.theme.LocalIsDarkTheme
import com.example.vibefinance.ui.main.ExpressiveSegmentedButtonGroup
import com.example.vibefinance.ui.recurring.ConnectedButtonGroup
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Actual expense category/account rows, recurring filters/editor tabs and asset controls were
 * explored with ARTEMIS and ADB before these regressions. Locators use semantics and scrolling;
 * motion assertions use the deterministic Compose clock rather than exploration latency.
 */
@RunWith(AndroidJUnit4::class)
class ConnectedButtonMotionVariantsTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun scrollableLegacyGroup_selectsOffscreenItemWithIcon_andKeepsTrailingAddIndependent() {
        val selections = CopyOnWriteArrayList<Int>()
        val additions = CopyOnWriteArrayList<Unit>()
        val items = (1..10).map { "Wallet $it" }
        showContent(actualDarkTheme = false) {
            var selected by remember { mutableIntStateOf(0) }
            ExpressiveSegmentedButtonGroup(
                items = items,
                selectedIndex = selected,
                onItemSelected = { selected = it; selections.add(it) },
                modifier = Modifier.width(260.dp),
                isScrollable = true,
                iconProvider = { item, tint ->
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = "Icon for $item",
                        tint = tint,
                        modifier = Modifier.size(16.dp)
                    )
                },
                trailingContent = {
                    TextButton(onClick = { additions.add(Unit) }) { Text(ADD) }
                },
                labelProvider = { it }
            )
        }
        // Scroll semantics await animated scroll frames. This behavior test does not sample
        // transitional pixels, so allow those frames to advance while performScrollTo waits.
        rule.mainClock.autoAdvance = true

        rule.onNodeWithContentDescription("Icon for ${items.first()}", useUnmergedTree = true)
            .assertIsDisplayed()
        val last = rule.onNodeWithText(items.last())
        last.performScrollTo()
        settle()
        last.assertIsDisplayed().performClick()
        settle()
        rule.waitUntil(TIMEOUT_MILLIS) { selections.toList() == listOf(items.lastIndex) }
        rule.onNodeWithContentDescription("Icon for ${items.last()}", useUnmergedTree = true)
            .assertIsDisplayed()
        rule.onNodeWithText(ADD).performScrollTo()
        settle()
        rule.onNodeWithText(ADD).assertIsDisplayed().performClick()
        rule.waitUntil(TIMEOUT_MILLIS) { additions.size == 1 }
        assertEquals("Trailing Add must not change the selected wallet", listOf(items.lastIndex), selections.toList())

        rule.onNodeWithText(items.first()).performScrollTo()
        settle()
        rule.onNodeWithText(items.first()).assertIsDisplayed().performClick()
        settle()
        rule.waitUntil(TIMEOUT_MILLIS) { selections.toList() == listOf(items.lastIndex, 0) }
        rule.onNodeWithContentDescription("Icon for ${items.first()}", useUnmergedTree = true)
            .assertIsDisplayed()
        assertEquals(1, additions.size)
    }

    @Test
    fun recurringLightMode_heldFocusConverges_andPreviousSelectionDispersesAfterRelease() {
        val selections = CopyOnWriteArrayList<Int>()
        showRecurring(actualDarkTheme = false, selections = selections)
        val previous = rule.onNodeWithText(FIRST)
        val target = rule.onNodeWithText(SECOND)
        assertSolidPrimary(previous.captureToImage())

        target.performTouchInput { down(center) }
        try {
            advance(64)
            assertTrue("A held recurring option must not commit selection", selections.isEmpty())
            assertInwardFocus(target.captureToImage())
        } finally {
            target.performTouchInput { up() }
        }
        advance(64)
        rule.waitUntil(TIMEOUT_MILLIS) { selections.toList() == listOf(1) }
        val leaving = previous.captureToImage()
        assertInwardFocus(leaving)
        assertTrue(
            "The old primary wash must lose opacity as its center clears; ${pixelSummary(leaving)}",
            blueChroma(edgeColor(leaving)) < blueChroma(PRIMARY) - 0.03f
        )
        settle()
        assertSolidPrimary(target.captureToImage())
        assertTrue("The deselected recurring option must finish tonal", blueChroma(centerColor(previous.captureToImage())) < 0.05f)
    }

    @Test
    fun recurringActualDarkMode_usesUniformCrossfade_andInterruptedSelectionsSettle() {
        val selections = CopyOnWriteArrayList<Int>()
        // Deliberately retain a light palette: the actual theme flag must disable fluid motion
        // immediately, including while the app's animated palette is still switching to Dark.
        showRecurring(actualDarkTheme = true, selections = selections)
        val previous = rule.onNodeWithText(FIRST)
        val target = rule.onNodeWithText(SECOND)
        target.performClick()
        advance(96)
        val entering = target.captureToImage()
        assertUniformFill(entering)
        assertUniformFill(previous.captureToImage())
        assertTrue("The Dark fallback must crossfade rather than snap", blueChroma(centerColor(entering)) in 0.05f..0.80f)

        previous.performClick()
        advance(32)
        target.performClick()
        settle()
        rule.waitUntil(TIMEOUT_MILLIS) { selections.toList() == listOf(1, 0, 1) }
        assertSolidPrimary(target.captureToImage())
        assertUniformFill(previous.captureToImage())
        assertTrue("Rapid switches must leave the previous option tonal", blueChroma(centerColor(previous.captureToImage())) < 0.05f)
    }

    @Test
    fun nativeNonCompactDefault_enablesLightFocusMotionWithoutOptIn_andKeeps48DpControls() {
        val selections = CopyOnWriteArrayList<Int>()
        showContent(actualDarkTheme = false) {
            var selected by remember { mutableIntStateOf(0) }
            ExpressiveConnectedButtonGroup(
                items = listOf(FIRST, SECOND),
                selectedIndex = selected,
                onItemSelected = { selected = it; selections.add(it) },
                modifier = Modifier.width(320.dp),
                // Budget Period and other default callers supply neither motion nor compact flags.
                labelProvider = { it }
            )
        }
        val target = rule.onNodeWithText(SECOND).assertHeightIsAtLeast(48.dp)
        target.performTouchInput { down(center) }
        try {
            advance(64)
            target.assertIsNotSelected()
            assertTrue(selections.isEmpty())
            assertInwardFocus(target.captureToImage())
        } finally {
            target.performTouchInput { up() }
        }
        settle()
        rule.waitUntil(TIMEOUT_MILLIS) { selections.toList() == listOf(1) }
        target.assertIsSelected().assertHeightIsAtLeast(48.dp)
        rule.onNodeWithText(FIRST).assertIsNotSelected()
        assertSolidPrimary(target.captureToImage())
    }

    private fun showRecurring(actualDarkTheme: Boolean, selections: MutableList<Int>) {
        showContent(actualDarkTheme) {
            var selected by remember { mutableIntStateOf(0) }
            ConnectedButtonGroup(
                items = listOf(FIRST, SECOND),
                selectedIndex = selected,
                onItemSelected = { selected = it; selections.add(it) },
                modifier = Modifier.width(320.dp),
                labelProvider = { it }
            )
        }
    }

    private fun showContent(actualDarkTheme: Boolean, content: @Composable () -> Unit) {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            CompositionLocalProvider(LocalIsDarkTheme provides actualDarkTheme) {
                MaterialTheme(
                    colorScheme = lightColorScheme(
                        primary = PRIMARY,
                        onPrimary = Color.White,
                        primaryContainer = Color.Black,
                        onPrimaryContainer = Color.Black,
                        surfaceVariant = Color.Black,
                        onSurfaceVariant = Color.Black,
                        background = Color.White,
                        surface = Color.White
                    )
                ) {
                    Box(Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
                        content()
                    }
                }
            }
        }
        settle()
    }

    private fun advance(milliseconds: Long) {
        rule.mainClock.advanceTimeBy(milliseconds)
        rule.waitForIdle()
    }

    private fun settle() = advance(2_000)

    private fun assertInwardFocus(image: ImageBitmap) {
        assertTrue(
            "Primary must occupy the perimeter before the center; ${pixelSummary(image)}",
            blueChroma(edgeColor(image)) > blueChroma(centerColor(image)) + 0.08f
        )
        assertTrue("The inset center must remain tonal; ${pixelSummary(image)}", blueChroma(centerColor(image)) < 0.05f)
    }

    private fun assertSolidPrimary(image: ImageBitmap) {
        assertTrue("Selected center must settle to primary; ${pixelSummary(image)}", blueChroma(centerColor(image)) > 0.8f)
        assertTrue("Selected perimeter must settle to primary; ${pixelSummary(image)}", blueChroma(edgeColor(image)) > 0.8f)
    }

    private fun assertUniformFill(image: ImageBitmap) {
        assertTrue("Dark mode must crossfade uniformly; ${pixelSummary(image)}", colorDistance(edgeColor(image), centerColor(image)) < 0.06f)
    }

    // Medians reject narrow glyphs and GPU antialiasing; regions stay away from changing corners.
    private fun edgeColor(image: ImageBitmap) = medianColor(image, 0.40f, 0.60f, 0.14f, 0.18f)

    private fun centerColor(image: ImageBitmap) = medianColor(image, 0.40f, 0.60f, 0.44f, 0.56f)

    private fun medianColor(image: ImageBitmap, left: Float, right: Float, top: Float, bottom: Float): Color {
        val pixels = image.toPixelMap()
        val colors = mutableListOf<Color>()
        for (y in (image.height * top).toInt()..(image.height * bottom).toInt()) {
            for (x in (image.width * left).toInt()..(image.width * right).toInt()) colors.add(pixels[x, y])
        }
        fun median(channel: (Color) -> Float) = colors.map(channel).sorted()[colors.size / 2]
        return Color(median { it.red }, median { it.green }, median { it.blue })
    }

    private fun blueChroma(color: Color) = color.blue - color.red

    private fun colorDistance(first: Color, second: Color) = maxOf(
        abs(first.red - second.red), abs(first.green - second.green), abs(first.blue - second.blue)
    )

    private fun pixelSummary(image: ImageBitmap) = "image=${image.width}x${image.height}, edge=${edgeColor(image)}, center=${centerColor(image)}"

    private companion object {
        val PRIMARY = Color(0.02f, 0.12f, 0.98f)
        const val FIRST = "I"
        const val SECOND = "II"
        const val ADD = "Add wallet"
        const val TIMEOUT_MILLIS = 5_000L
    }
}
