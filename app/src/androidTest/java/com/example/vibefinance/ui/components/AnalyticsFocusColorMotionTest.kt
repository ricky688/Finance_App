package com.example.vibefinance.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vibefinance.theme.LocalIsDarkTheme
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Category Analytics light/dark paths were explored with ARTEMIS and ADB before these tests. */
@RunWith(AndroidJUnit4::class)
class AnalyticsFocusColorMotionTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun lightMode_holdFillsPerimeterBeforeCenter_cancelRestoresAndReleaseSelects() {
        val selections = CopyOnWriteArrayList<Int>()
        showGroup(actualDarkTheme = false, selections = selections)
        val target = rule.onNodeWithText(SECOND)
        val inactive = target.captureToImage()

        target.performTouchInput { down(center) }
        try {
            advance(64)
            target.assertIsNotSelected()
            rule.onNodeWithText(FIRST).assertIsSelected()
            assertTrue("Holding must not commit the selection", selections.isEmpty())
            val focusing = target.captureToImage()
            saveFrame("light-held-64ms", focusing)
            assertInwardFocus(focusing)
        } finally {
            target.performTouchInput { cancel() }
        }
        settle()
        target.assertIsNotSelected()
        assertEquals("Cancelling must not invoke selection", emptyList<Int>(), selections.toList())
        assertSameFill(inactive, target.captureToImage())

        target.performTouchInput { down(center) }
        advance(64)
        assertInwardFocus(target.captureToImage())
        target.performTouchInput { up() }
        settle()
        rule.waitUntil(TIMEOUT_MILLIS) { selections.toList() == listOf(1) }
        target.assertIsSelected()
        assertSolidPrimary(target.captureToImage())
    }

    @Test
    fun lightMode_deselectionClearsCenterBeforePerimeter_andInterruptedSwitchesSettle() {
        val selections = CopyOnWriteArrayList<Int>()
        showGroup(actualDarkTheme = false, selections = selections)
        val previous = rule.onNodeWithText(FIRST)
        assertSolidPrimary(previous.captureToImage())

        rule.onNodeWithText(SECOND).performClick()
        advance(64)
        previous.assertIsNotSelected()
        val defocusing = previous.captureToImage()
        saveFrame("light-deselected-64ms", defocusing)
        val edge = edgeColor(defocusing)
        val center = centerColor(defocusing)
        assertTrue("The center must clear before the remaining perimeter wash; ${pixelSummary(defocusing)}", blueChroma(edge) > blueChroma(center) + 0.08f)
        assertTrue("The released perimeter must fade below the fully active primary; ${pixelSummary(defocusing)}", blueChroma(edge) < blueChroma(PRIMARY) - 0.03f)

        rule.onNodeWithText(FIRST).performClick()
        advance(32)
        rule.onNodeWithText(SECOND).performClick()
        advance(32)
        rule.onNodeWithText(FIRST).performClick()
        settle()
        rule.waitUntil(TIMEOUT_MILLIS) { selections.toList() == listOf(1, 0, 1, 0) }
        previous.assertIsSelected()
        rule.onNodeWithText(SECOND).assertIsNotSelected()
        assertSolidPrimary(previous.captureToImage())
        assertTrue("Interrupted motion must leave the final inactive button tonal", blueChroma(centerColor(rule.onNodeWithText(SECOND).captureToImage())) < 0.05f)
    }

    @Test
    fun actualDarkMode_usesUniformCrossfadeEvenBeforeTheLightPaletteChanges() {
        val selections = CopyOnWriteArrayList<Int>()
        // Actual theme state deliberately disagrees with this light palette, as during a theme switch.
        showGroup(actualDarkTheme = true, selections = selections)
        val previous = rule.onNodeWithText(FIRST)
        val target = rule.onNodeWithText(SECOND)
        target.performClick()
        advance(96)
        val entering = target.captureToImage()
        val leaving = previous.captureToImage()
        assertUniformFill(entering)
        assertUniformFill(leaving)
        assertTrue("Dark fallback must animate rather than snap to solid primary", blueChroma(centerColor(entering)) in 0.05f..0.80f)
        settle()
        target.assertIsSelected()
        previous.assertIsNotSelected()
        assertSolidPrimary(target.captureToImage())
    }

    @Test
    fun compactButtons_haveSmallerPaintedHeightAndFull48DpTouchTargets() {
        val selections = CopyOnWriteArrayList<Int>()
        showGroup(actualDarkTheme = false, selections = selections)
        val group = rule.onNodeWithTag(GROUP).assertHeightIsAtLeast(48.dp)
        rule.onNodeWithText(FIRST).assertIsDisplayed()
        rule.onNodeWithText(SECOND).assertIsDisplayed()
        val image = group.captureToImage()
        val pixels = image.toPixelMap()
        // This column is away from both the native rounded ends and the centered glyphs.
        val x = (image.width * 0.31f).toInt()
        val paintedRows = (0 until image.height).filter { blueChroma(pixels[x, it]) > 0.5f }
        assertTrue("A selected button must render a primary fill", paintedRows.isNotEmpty())
        val paintedHeight = paintedRows.last() - paintedRows.first() + 1
        assertTrue("Compact 40dp visuals must be smaller than the 48dp row", paintedHeight < image.height - 2)
        assertTrue("The compact fill must keep its intended 40dp height", abs(paintedHeight - image.height * (40f / 48f)) <= 3f)

        // y=1px lies in the extra touch allocation above the centered 40dp painted surface.
        group.performTouchInput { click(Offset(width * 0.75f, 1f)) }
        settle()
        rule.waitUntil(TIMEOUT_MILLIS) { selections.toList() == listOf(1) }
        rule.onNodeWithText(SECOND).assertIsSelected()
    }

    private fun showGroup(actualDarkTheme: Boolean, selections: MutableList<Int>) {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            var selection by remember { mutableIntStateOf(0) }
            CompositionLocalProvider(LocalIsDarkTheme provides actualDarkTheme) {
                MaterialTheme(
                    colorScheme = lightColorScheme(
                        primary = PRIMARY,
                        onPrimary = Color.White,
                        primaryContainer = Color.Black,
                        onPrimaryContainer = Color.Black,
                        background = Color.White,
                        surface = Color.White
                    )
                ) {
                    Box(Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
                        ExpressiveConnectedButtonGroup(
                            items = listOf(FIRST, SECOND),
                            selectedIndex = selection,
                            onItemSelected = { selection = it; selections.add(it) },
                            modifier = Modifier.width(320.dp).testTag(GROUP),
                            lightModeFocusMotion = true,
                            compact = true,
                            labelProvider = { it }
                        )
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
        val edge = edgeColor(image)
        val center = centerColor(image)
        assertTrue("Primary must reach the perimeter before the center; ${pixelSummary(image)}", blueChroma(edge) > blueChroma(center) + 0.08f)
        assertTrue("The center must retain its tonal fill during the inset; ${pixelSummary(image)}", blueChroma(center) < 0.05f)
    }

    private fun assertSolidPrimary(image: ImageBitmap) {
        assertTrue("The active center must settle to primary", blueChroma(centerColor(image)) > 0.8f)
        assertTrue("The active perimeter must settle to primary", blueChroma(edgeColor(image)) > 0.8f)
    }

    private fun assertUniformFill(image: ImageBitmap) {
        val edge = edgeColor(image)
        val center = centerColor(image)
        assertTrue("Dark mode must not retain a perimeter-versus-center color mask", colorDistance(edge, center) < 0.06f)
    }

    private fun assertSameFill(before: ImageBitmap, after: ImageBitmap) {
        assertTrue("Cancelling must restore the inactive center", colorDistance(centerColor(before), centerColor(after)) < 0.03f)
        assertTrue("Cancelling must restore the inactive perimeter", colorDistance(edgeColor(before), edgeColor(after)) < 0.03f)
    }

    // Use medians over small interior regions: glyphs, ripple dithering and antialiasing cannot
    // turn a single sampled pixel into a false positive. No clipped-corner geometry is assumed.
    private fun edgeColor(image: ImageBitmap): Color = medianColor(image, 0.40f, 0.60f, 0.14f, 0.18f)

    private fun centerColor(image: ImageBitmap): Color = medianColor(image, 0.40f, 0.60f, 0.44f, 0.56f)

    private fun medianColor(image: ImageBitmap, left: Float, right: Float, top: Float, bottom: Float): Color {
        val pixels = image.toPixelMap()
        val colors = mutableListOf<Color>()
        for (y in (image.height * top).toInt()..(image.height * bottom).toInt()) {
            for (x in (image.width * left).toInt()..(image.width * right).toInt()) colors.add(pixels[x, y])
        }
        fun median(channel: (Color) -> Float): Float = colors.map(channel).sorted()[colors.size / 2]
        return Color(median { it.red }, median { it.green }, median { it.blue })
    }

    private fun blueChroma(color: Color) = color.blue - color.red

    private fun colorDistance(first: Color, second: Color) = maxOf(
        abs(first.red - second.red), abs(first.green - second.green), abs(first.blue - second.blue)
    )

    private fun pixelSummary(image: ImageBitmap): String {
        val edge = edgeColor(image)
        val center = centerColor(image)
        return "image=${image.width}x${image.height}, edge=$edge (blueChroma=${blueChroma(edge)}), " +
            "center=$center (blueChroma=${blueChroma(center)})"
    }

    private fun saveFrame(name: String, image: ImageBitmap) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.getExternalFilesDir(null), "analytics-motion-test-frames")
        check(directory.exists() || directory.mkdirs())
        File(directory, "$name.png").outputStream().use {
            check(image.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it))
        }
    }

    private companion object {
        val PRIMARY = Color(0.02f, 0.12f, 0.98f)
        const val FIRST = "I"
        const val SECOND = "II"
        const val GROUP = "analyticsFocusGroup"
        const val TIMEOUT_MILLIS = 5_000L
    }
}
