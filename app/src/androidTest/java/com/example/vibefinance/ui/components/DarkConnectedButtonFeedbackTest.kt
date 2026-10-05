package com.example.vibefinance.ui.components

import android.graphics.Bitmap
import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vibefinance.theme.LocalIsDarkTheme
import com.example.vibefinance.ui.main.ExpressiveSegmentedButtonGroup
import com.example.vibefinance.ui.recurring.ConnectedButtonGroup
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Dark Assets, Recurring and expense-category hold/cancel paths were explored using ARTEMIS
 * and ADB on Waydroid 192.168.240.112:5555 before these regressions. Exploration frames are in
 * captures/dark-connected-feedback-2026-10-05. Tests retain real pointers across captures;
 * the Compose clock controls shapes and colors. Native Android ripple rendering and legacy
 * pulse delays additionally use explicit wall-clock waits.
 */
@RunWith(AndroidJUnit4::class)
class DarkConnectedButtonFeedbackTest {
    @get:Rule val rule = createComposeRule()

    private val selections = CopyOnWriteArrayList<Int>()
    private val selectedIndex = AtomicInteger(INITIAL_SELECTION)

    @Test
    fun nativeDarkGroup_allPositionsMorphWithBoundedGrayRipple_cancelRestores_releaseSelects() {
        exercisePositions(GroupKind.NATIVE)
    }

    @Test
    fun mainLegacyDarkGroup_allPositionsMorphWithBoundedGrayRipple_cancelRestores_releaseSelects() {
        exercisePositions(GroupKind.MAIN)
    }

    @Test
    fun recurringDarkGroup_allPositionsMorphWithBoundedGrayRipple_cancelRestores_releaseSelects() {
        exercisePositions(GroupKind.RECURRING)
    }

    private fun exercisePositions(kind: GroupKind) {
        showGroup(kind)
        assertSelection(kind, INITIAL_SELECTION)

        // Initially the trailing option is selected, so both middle and leading are unchecked.
        holdAndCancel(kind, index = 1, expectedSelection = INITIAL_SELECTION)
        holdAndCancel(kind, index = 0, expectedSelection = INITIAL_SELECTION)

        // A real release is the only action allowed to commit a callback or selection.
        holdAndRelease(kind, index = 0, expectedSelection = INITIAL_SELECTION)
        assertEquals(listOf(0), selections.toList())
        assertSelection(kind, 0)

        // The former trailing selection is now unchecked, covering the other connected end.
        holdAndCancel(kind, index = 2, expectedSelection = 0)
        assertEquals("Cancelled presses must not add callbacks", listOf(0), selections.toList())
    }

    private fun showGroup(kind: GroupKind) {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            var selected by remember { mutableIntStateOf(INITIAL_SELECTION) }
            CompositionLocalProvider(LocalIsDarkTheme provides true) {
                // Neutral inactive fills distinguish gray ripple from an erroneous blue press fill.
                MaterialTheme(
                    colorScheme = darkColorScheme(
                        primary = PRIMARY,
                        onPrimary = Color.White,
                        primaryContainer = Color(0.42f, 0.42f, 0.42f),
                        onPrimaryContainer = Color.White,
                        surfaceVariant = Color(0.36f, 0.36f, 0.36f),
                        onSurfaceVariant = Color.White,
                        background = BACKGROUND,
                        surface = BACKGROUND
                    )
                ) {
                    Box(Modifier.fillMaxSize().background(BACKGROUND), contentAlignment = Alignment.Center) {
                        // The surrounding background makes an unbounded halo observable.
                        Box(
                            Modifier.width(360.dp)
                                .testTag(BOARD)
                                .background(BACKGROUND)
                                .padding(20.dp)
                        ) {
                            val onSelected: (Int) -> Unit = {
                                selected = it
                                selectedIndex.set(it)
                                selections.add(it)
                            }
                            when (kind) {
                                GroupKind.NATIVE -> ExpressiveConnectedButtonGroup(
                                    items = LABELS,
                                    selectedIndex = selected,
                                    onItemSelected = onSelected,
                                    modifier = Modifier.fillMaxWidth(),
                                    labelProvider = { it }
                                )
                                GroupKind.MAIN -> ExpressiveSegmentedButtonGroup(
                                    items = LABELS,
                                    selectedIndex = selected,
                                    onItemSelected = onSelected,
                                    modifier = Modifier.fillMaxWidth(),
                                    isScrollable = false,
                                    labelProvider = { it }
                                )
                                GroupKind.RECURRING -> ConnectedButtonGroup(
                                    items = LABELS,
                                    selectedIndex = selected,
                                    onItemSelected = onSelected,
                                    modifier = Modifier.fillMaxWidth(),
                                    labelProvider = { it }
                                )
                            }
                        }
                    }
                }
            }
        }
        settle()
    }

    private fun holdAndCancel(kind: GroupKind, index: Int, expectedSelection: Int) {
        val button = rule.onNodeWithText(LABELS[index])
        val before = capture(button)
        val callbacksBefore = selections.toList()
        button.performTouchInput { down(center) }
        try {
            advance(HOLD_MILLIS)
            awaitRenderThread()
            assertHeldFeedback(kind, index, expectedSelection, callbacksBefore, before, button)
        } finally {
            button.performTouchInput { cancel() }
        }

        awaitRestoration(kind, button, before.button)
        assertSelection(kind, expectedSelection)
        assertEquals("Cancel must not invoke selection", callbacksBefore, selections.toList())
        val restored = capture(button)
        assertEquals("Cancel must restore clipped corners", 0, changedCornerCoverage(before.button, restored.button))
        assertTrue("Cancel must remove the gray press overlay", sameInteriorFill(before.button, restored.button))
        assertNoHalo(before, restored)
    }

    private fun holdAndRelease(kind: GroupKind, index: Int, expectedSelection: Int) {
        val button = rule.onNodeWithText(LABELS[index])
        val before = capture(button)
        val callbacksBefore = selections.toList()
        var feedbackVerified = false
        button.performTouchInput { down(center) }
        try {
            advance(HOLD_MILLIS)
            awaitRenderThread()
            assertHeldFeedback(kind, index, expectedSelection, callbacksBefore, before, button)
            feedbackVerified = true
        } finally {
            // Failed assertions still clear the pointer without accidentally committing a click.
            button.performTouchInput { if (feedbackVerified) up() else cancel() }
        }

        rule.waitUntil(TIMEOUT_MILLIS) { selections.toList() == callbacksBefore + index }
        awaitPulseDelay(kind)
        settle()
        awaitRenderThread()
        assertSelection(kind, index)
        assertTrue(
            "Only release should fill the newly selected option with primary",
            blueChroma(interiorColor(button.captureToImage())) > 0.8f
        )
        assertTrue(
            "The previous selection must settle back to its neutral inactive fill",
            abs(blueChroma(interiorColor(rule.onNodeWithText(LABELS[expectedSelection]).captureToImage()))) < 0.02f
        )
    }

    private fun assertHeldFeedback(
        kind: GroupKind,
        index: Int,
        expectedSelection: Int,
        callbacksBefore: List<Int>,
        before: Frame,
        button: SemanticsNodeInteraction
    ) {
        val held = capture(button)
        try {
            assertSelection(kind, expectedSelection)
            if (kind == GroupKind.NATIVE) button.assertIsNotSelected()
            assertEquals("Holding ${LABELS[index]} must not commit selection", callbacksBefore, selections.toList())
            assertNeutralRipple(before.button, held.button)
            assertTrue(
                "The unchecked ${kind.name} ${LABELS[index]} corners must morph during hold",
                changedCornerCoverage(before.button, held.button) >= MIN_CHANGED_CORNER_PIXELS
            )
            assertNoHalo(before, held)
        } catch (failure: AssertionError) {
            val prefix = "${kind.name.lowercase()}-$index-callbacks-${callbacksBefore.size}"
            runCatching {
                saveFrame("$prefix-before-button", before.button)
                saveFrame("$prefix-held-button", held.button)
                saveFrame("$prefix-before-board", before.board)
                saveFrame("$prefix-held-board", held.board)
            }.exceptionOrNull()?.let(failure::addSuppressed)
            throw failure
        }
    }

    private fun assertSelection(kind: GroupKind, expected: Int) {
        assertEquals("Selection must remain unchanged until release", expected, selectedIndex.get())
        if (kind == GroupKind.NATIVE) {
            LABELS.forEachIndexed { index, label ->
                val node = rule.onNodeWithText(label)
                if (index == expected) node.assertIsSelected() else node.assertIsNotSelected()
            }
        }
    }

    private fun awaitRestoration(kind: GroupKind, button: SemanticsNodeInteraction, resting: ImageBitmap) {
        awaitPulseDelay(kind)
        // Custom shape springs start after their actual 140ms coroutine pulse has ended.
        rule.waitUntil(TIMEOUT_MILLIS) {
            advance(64)
            val current = button.captureToImage()
            changedCornerCoverage(resting, current) == 0 && sameInteriorFill(resting, current)
        }
        // Clear any last ripple fade or subpixel spring movement before the next baseline.
        settle()
        awaitRenderThread()
    }

    private fun awaitPulseDelay(kind: GroupKind) {
        rule.waitForIdle()
        if (kind != GroupKind.NATIVE) {
            val started = SystemClock.elapsedRealtime()
            rule.waitUntil(TIMEOUT_MILLIS) { SystemClock.elapsedRealtime() - started >= PULSE_MILLIS }
        }
    }

    private fun awaitRenderThread() {
        // Android's native hardware ripple runs on real time, independently of mainClock.
        // Keep the pointer held while it reaches the interior sample, and wait after release
        // or cancellation so the next baseline cannot retain a fading native ripple.
        val started = SystemClock.elapsedRealtime()
        rule.waitUntil(TIMEOUT_MILLIS) {
            SystemClock.elapsedRealtime() - started >= RIPPLE_RENDER_MILLIS
        }
        rule.waitForIdle()
    }

    private fun capture(button: SemanticsNodeInteraction): Frame {
        val board = rule.onNodeWithTag(BOARD)
        val boardBounds = board.fetchSemanticsNode().boundsInRoot
        val buttonBounds = button.fetchSemanticsNode().boundsInRoot
        return Frame(
            button = button.captureToImage(),
            board = board.captureToImage(),
            buttonBounds = Rect(
                buttonBounds.left - boardBounds.left,
                buttonBounds.top - boardBounds.top,
                buttonBounds.right - boardBounds.left,
                buttonBounds.bottom - boardBounds.top
            )
        )
    }

    private fun assertNeutralRipple(before: ImageBitmap, held: ImageBitmap) {
        val base = interiorColor(before)
        val pressed = interiorColor(held)
        val deltas = listOf(pressed.red - base.red, pressed.green - base.green, pressed.blue - base.blue)
        assertTrue("A held button must show a visible gray ripple: base=$base, held=$pressed", deltas.minOrNull()!! > 0.002f)
        assertTrue("Press feedback must be neutral gray: delta=$deltas", deltas.maxOrNull()!! - deltas.minOrNull()!! < 0.02f)
        assertTrue("Hold must not fill an unchecked Dark button with primary: $pressed", abs(blueChroma(pressed)) < 0.02f)
    }

    private fun assertNoHalo(before: Frame, after: Frame) {
        assertEquals(before.board.width, after.board.width)
        assertEquals(before.board.height, after.board.height)
        val a = before.board.toPixelMap()
        val b = after.board.toPixelMap()
        val bounds = after.buttonBounds
        for (y in 0 until a.height) {
            for (x in 0 until a.width) {
                // A one-pixel confidence margin rejects boundary antialiasing, not visible halos.
                if (x + 0.5f >= bounds.left - 1f && x + 0.5f <= bounds.right + 1f &&
                    y + 0.5f >= bounds.top - 1f && y + 0.5f <= bounds.bottom + 1f
                ) continue
                assertTrue("Press feedback escaped button bounds at ($x,$y)", colorDistance(a[x, y], b[x, y]) < 0.01f)
            }
        }
    }

    /** Classify corner coverage independently of gray ripple and central glyphs. */
    private fun changedCornerCoverage(before: ImageBitmap, after: ImageBitmap): Int {
        assertEquals(before.width, after.width)
        assertEquals(before.height, after.height)
        val a = before.toPixelMap()
        val b = after.toPixelMap()
        var changed = 0
        for (y in 0 until a.height) {
            if (y >= a.height / 4 && y < a.height * 3 / 4) continue
            for (x in 0 until a.width) {
                if (x >= a.width / 4 && x < a.width * 3 / 4) continue
                val first = colorDistance(a[x, y], BACKGROUND)
                val second = colorDistance(b[x, y], BACKGROUND)
                if ((first < 0.03f && second > 0.07f) || (second < 0.03f && first > 0.07f)) changed++
            }
        }
        return changed
    }

    private fun sameInteriorFill(before: ImageBitmap, after: ImageBitmap): Boolean =
        colorDistance(interiorColor(before), interiorColor(after)) < 0.01f

    /** Sample away from both clipped corners and the narrow, centered I/II/III glyphs. */
    private fun interiorColor(image: ImageBitmap): Color {
        val pixels = image.toPixelMap()
        val colors = mutableListOf<Color>()
        for (y in (image.height * 0.40f).toInt()..(image.height * 0.60f).toInt()) {
            for (x in (image.width * 0.22f).toInt()..(image.width * 0.36f).toInt()) colors.add(pixels[x, y])
        }
        fun median(channel: (Color) -> Float) = colors.map(channel).sorted()[colors.size / 2]
        return Color(median { it.red }, median { it.green }, median { it.blue })
    }

    private fun colorDistance(a: Color, b: Color) = maxOf(abs(a.red - b.red), abs(a.green - b.green), abs(a.blue - b.blue))

    private fun blueChroma(color: Color) = color.blue - color.red

    private fun saveFrame(name: String, image: ImageBitmap) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.getExternalFilesDir(null) ?: context.filesDir, "dark-connected-feedback-test-frames")
        check(directory.exists() || directory.mkdirs())
        File(directory, "$name.png").outputStream().use {
            check(image.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it))
        }
    }

    private fun advance(milliseconds: Long) {
        rule.mainClock.advanceTimeBy(milliseconds)
        rule.waitForIdle()
    }

    private fun settle() = advance(2_000)

    private data class Frame(val button: ImageBitmap, val board: ImageBitmap, val buttonBounds: Rect)

    private enum class GroupKind { NATIVE, MAIN, RECURRING }

    private companion object {
        val PRIMARY = Color(0.02f, 0.12f, 0.98f)
        val BACKGROUND = Color(0.025f, 0.025f, 0.025f)
        val LABELS = listOf("I", "II", "III")
        const val BOARD = "dark-connected-feedback-board"
        const val INITIAL_SELECTION = 2
        const val HOLD_MILLIS = 800L
        const val PULSE_MILLIS = 140L
        const val RIPPLE_RENDER_MILLIS = 400L
        const val TIMEOUT_MILLIS = 5_000L
        const val MIN_CHANGED_CORNER_PIXELS = 4
    }
}
