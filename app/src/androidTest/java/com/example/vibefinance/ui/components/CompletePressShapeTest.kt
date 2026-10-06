@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.components

import android.graphics.Bitmap
import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vibefinance.theme.LocalIsDarkTheme
import com.example.vibefinance.ui.main.ExpressiveAddButton
import com.example.vibefinance.ui.main.ExpressiveSegmentedButtonGroup
import com.example.vibefinance.ui.recurring.ConnectedButtonGroup
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * ARTEMIS screenshots and ADB explored actual Assets connected buttons, expense categories
 * and the expense keyboard on Waydroid before authoring this suite. Real pointer gestures
 * drive production controls; semantic locators and frame waits verify painted corner coverage.
 * No financial database is used. The 600ms keyboard wait is for its actual long-click handler.
 */
@RunWith(AndroidJUnit4::class)
class CompletePressShapeTest {
    @get:Rule val rule = createComposeRule()
    private val clicks = AtomicInteger()
    private val longClicks = AtomicInteger()

    @Test fun filledButton_completesTap_andHoldsUntilRelease() = exercise(Kind.FILLED)
    @Test fun tonalButton_completesTap_andHoldsUntilRelease() = exercise(Kind.TONAL)
    @Test fun outlinedButton_completesTap_andHoldsUntilRelease() = exercise(Kind.OUTLINED)
    @Test fun textButton_completesTap_andHoldsUntilRelease() = exercise(Kind.TEXT)
    @Test fun nativeConnectedButton_completesTap_andHoldsUntilRelease() = exercise(Kind.NATIVE)
    @Test fun expenseConnectedButton_completesTap_andHoldsUntilRelease() = exercise(Kind.MAIN)
    @Test fun recurringConnectedButton_completesTap_andHoldsUntilRelease() = exercise(Kind.RECURRING)
    @Test fun keyboard_completesTap_andLongClickDoesNotResetHeldShape() = exercise(Kind.KEYBOARD)
    @Test fun appendedAddButton_completesTap_andHoldsUntilRelease() = exercise(Kind.ADD)

    private fun exercise(kind: Kind) {
        show(kind)
        val button = if (kind in listOf(Kind.NATIVE, Kind.MAIN, Kind.RECURRING)) {
            rule.onNodeWithText(LABELS.first())
        } else rule.onNodeWithTag(TARGET)
        val measuredSize = button.fetchSemanticsNode().size
        val baseline = capture(kind, "rest", button)
        val restingCorners = coverage(baseline)

        // Establish the full held outline, then hold much longer than an animation duration.
        button.performTouchInput { down(center) }
        advance(240)
        val heldCorners = coverage(capture(kind, "held", button))
        assertTrue("${kind.name}: press must visibly change corners ($restingCorners -> $heldCorners)",
            heldCorners > restingCorners + 8)
        advance(1_200)
        if (kind == Kind.KEYBOARD) {
            SystemClock.sleep(600)
            rule.waitUntil(5_000) { longClicks.get() == 1 }
        }
        val longHeld = coverage(capture(kind, "held-long", button))
        assertEquals("Still-held pointer must retain the full pressed outline", heldCorners, longHeld)
        assertEquals("Holding must not click", 0, clicks.get())
        assertEquals("Shape motion must retain its measured slot", measuredSize, button.fetchSemanticsNode().size)
        button.performTouchInput { up() }
        val expectedAfterHold = if (kind == Kind.KEYBOARD) 0 else 1
        rule.waitUntil(5_000) { clicks.get() == expectedAfterHold }
        advance(500)
        assertRestored(kind, button, restingCorners, "released")

        // Release after one frame. The inward morph must still reach the same full outline.
        button.performTouchInput { down(center) }
        advance(16)
        button.performTouchInput { up() }
        rule.waitUntil(5_000) { clicks.get() == expectedAfterHold + 1 }
        // Frame-aligned completion of the 180ms morph, before a return frame can advance.
        advance(176)
        val tapPeak = coverage(capture(kind, "short-tap-peak", button))
        assertTrue("A one-frame tap must complete the held outline: $tapPeak versus $heldCorners",
            abs(tapPeak - heldCorners) <= 5)
        advance(500)
        assertRestored(kind, button, restingCorners, "short-tap-restored")

        // Cancel a partial gesture without completing a click or leaving a stuck shape.
        val beforeCancel = clicks.get()
        button.performTouchInput { down(center) }
        advance(64)
        button.performTouchInput { cancel() }
        advance(500)
        assertRestored(kind, button, restingCorners, "cancelled")
        assertEquals("Cancel must not click", beforeCancel, clicks.get())

        // Rapid taps can interrupt restoration; every release still commits exactly once.
        repeat(3) {
            button.performTouchInput { down(center) }
            advance(16)
            button.performTouchInput { up() }
            advance(64)
        }
        advance(700)
        rule.waitUntil(5_000) { clicks.get() == beforeCancel + 3 }
        assertRestored(kind, button, restingCorners, "rapid-taps-restored")
        assertEquals(measuredSize, button.fetchSemanticsNode().size)
    }

    private fun show(kind: Kind) {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            CompositionLocalProvider(LocalIsDarkTheme provides true) {
                MaterialTheme(colorScheme = darkColorScheme(
                    primary = GREEN, onPrimary = Color.White,
                    primaryContainer = GREEN, onPrimaryContainer = Color.White,
                    surfaceVariant = GREEN, onSurfaceVariant = Color.White,
                    surface = Color.Black, background = Color.Black
                )) {
                    Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                        val click = { clicks.incrementAndGet(); Unit }
                        val shapes = ButtonDefaults.shapes(CircleShape, RoundedCornerShape(10.dp))
                        val modifier = Modifier.size(180.dp, 64.dp).testTag(TARGET)
                        val colors = ButtonDefaults.buttonColors(containerColor = GREEN, contentColor = Color.White)
                        when (kind) {
                            Kind.FILLED -> CompletePressButton(click, shapes, modifier, colors = colors) { Text("I") }
                            Kind.TONAL -> CompletePressFilledTonalButton(click, shapes, modifier, colors = colors) { Text("I") }
                            Kind.OUTLINED -> CompletePressOutlinedButton(click, shapes, modifier, colors = colors) { Text("I") }
                            Kind.TEXT -> CompletePressTextButton(click, shapes, modifier, colors = colors) { Text("I") }
                            Kind.NATIVE -> ExpressiveConnectedButtonGroup(
                                LABELS, 2, { click() }, Modifier.width(320.dp), labelProvider = { it }
                            )
                            Kind.MAIN -> ExpressiveSegmentedButtonGroup(
                                LABELS, 2, { click() }, Modifier.width(320.dp), labelProvider = { it }
                            )
                            Kind.RECURRING -> ConnectedButtonGroup(
                                LABELS, 2, { click() }, Modifier.width(320.dp), labelProvider = { it }
                            )
                            Kind.ADD -> ExpressiveAddButton("I", click, modifier)
                            Kind.KEYBOARD -> Box(modifier) {
                                KeyboardButton(type = KeyboardButtonType.DEFAULT, text = "7",
                                    onClick = click, onLongClick = { longClicks.incrementAndGet() })
                            }
                        }
                    }
                }
            }
        }
        advance(700)
    }

    private fun assertRestored(kind: Kind, node: SemanticsNodeInteraction, expected: Int, stage: String) {
        val actual = coverage(capture(kind, stage, node))
        assertTrue("${kind.name}: restoration must match original corners ($actual versus $expected)",
            abs(actual - expected) <= 5)
    }

    /** Count the painted outline, including a neutral border, but exclude near-black AA edges.
     * Chroma cannot classify outlines: a gray ripple changes the tint of a neutral border.
     */
    private fun coverage(image: ImageBitmap): Int {
        val pixels = image.toPixelMap()
        var count = 0
        for (y in 0 until pixels.height) {
            if (y >= pixels.height / 4 && y < pixels.height * 3 / 4) continue
            for (x in 0 until pixels.width) {
                if (x >= pixels.width / 4 && x < pixels.width * 3 / 4) continue
                val color = pixels[x, y]
                if (maxOf(color.red, color.green, color.blue) > 0.10f) count++
            }
        }
        return count
    }

    private fun capture(kind: Kind, stage: String, node: SemanticsNodeInteraction): ImageBitmap {
        val image = node.captureToImage()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.getExternalFilesDir(null), "complete-press-shape-test-frames")
        check(directory.exists() || directory.mkdirs())
        File(directory, "${kind.name.lowercase()}-$stage.png").outputStream().use {
            check(image.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it))
        }
        return image
    }

    private fun advance(ms: Long) {
        rule.mainClock.advanceTimeBy(ms)
        rule.waitForIdle()
    }

    private enum class Kind { FILLED, TONAL, OUTLINED, TEXT, NATIVE, MAIN, RECURRING, KEYBOARD, ADD }
    private companion object {
        val LABELS = listOf("I", "II", "III")
        val GREEN = Color(0.04f, 0.72f, 0.30f)
        const val TARGET = "complete-press-target"
    }
}
