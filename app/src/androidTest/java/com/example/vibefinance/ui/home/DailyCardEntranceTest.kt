package com.example.vibefinance.ui.home

import android.os.SystemClock
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.ui.FinanceUiState
import java.util.concurrent.atomic.AtomicInteger
import java.io.File
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Startup, bento rows and chart scrolling were explored with ARTEMIS + ADB on Waydroid first. */
@RunWith(AndroidJUnit4::class)
class DailyCardEntranceTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun initialAndScrolledCards_arriveWithoutLayoutJumps_andDoNotReplayOnUpdatesOrReturn() {
        rule.mainClock.autoAdvance = false
        var state by mutableStateOf(FinanceUiState(isLoading = false))
        val budgetClicks = AtomicInteger(0)
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme(surfaceVariant = Color(0xFFCCCCCC))) {
                // Short viewport: the first bento row is visible, but its second row is not.
                Box(Modifier.width(360.dp).height(440.dp).background(Color.Black)) {
                    HomeScreen(state = state, onIntent = {}, onOpenBudgetDialog = { budgetClicks.incrementAndGet() })
                }
            }
        }
        rule.mainClock.advanceTimeByFrame()
        awaitStagger()

        val period = rule.onNodeWithTag("DailyArrival:period")
        val periodSlot = period.fetchSemanticsNode().boundsInRoot
        val initial = brightness(capture("period-start", period))
        rule.mainClock.advanceTimeBy(112)
        val arriving = brightness(capture("period-mid", period))
        rule.mainClock.advanceTimeBy(1_000)
        val settled = brightness(capture("period-complete", period))
        assertTrue("Initially visible cards must fade in over multiple frames", arriving > initial + 5f)
        assertTrue("The entrance must finish at full opacity", settled > arriving + 5f)
        assertEquals("Arrival must not change the measured slot", periodSlot, period.fetchSemanticsNode().boundsInRoot)
        period.performClick()
        rule.waitUntil(5_000) { budgetClicks.get() == 1 }

        // The second row shares a composed bento item with the first: it must still wait
        // for its own visibility rather than finish its animation during lazy prefetch.
        val list = rule.onNodeWithTag("DailyCardsList")
        val lowest = rule.onNodeWithTag("DailyArrival:lowest")
        val viewportHeight = list.fetchSemanticsNode().boundsInRoot.height
        fun scrollBy(pixels: Float) {
            // High-level scroll searching waits for layout while the clock is paused.
            // Dispatch native semantic ScrollBy, then drive scrolling and entrance frames.
            list.performSemanticsAction(SemanticsActions.ScrollBy) { action ->
                assertTrue("Daily list must accept semantic scrolling", action(0f, pixels))
            }
            rule.mainClock.advanceTimeBy(48)
        }
        fun showLowest() {
            val distance = lowest.fetchSemanticsNode().positionInRoot.y -
                list.fetchSemanticsNode().positionInRoot.y - viewportHeight * 0.1f
            scrollBy(distance)
        }
        showLowest()
        val lowestSlot = lowest.fetchSemanticsNode().size
        val justVisible = brightness(capture("lowest-start", lowest))
        rule.mainClock.advanceTimeBy(112)
        val scrollingIn = brightness(capture("lowest-mid", lowest))
        rule.mainClock.advanceTimeBy(1_000)
        val fullyVisible = brightness(capture("lowest-complete", lowest))
        assertTrue("An offscreen bento child must start only when scrolled into view", scrollingIn > justVisible + 4f)
        assertTrue("The scrolled card must complete its fade", fullyVisible > scrollingIn + 4f)
        // Visible bounds change as scrolling uncovers the card; its full slot size must not.
        assertEquals(lowestSlot, lowest.fetchSemanticsNode().size)

        rule.runOnIdle {
            state = state.copy(transactions = listOf(TransactionEntity(
                id = 991, amount = 12.6, category = "Transport", accountId = 1,
                timestamp = System.currentTimeMillis(), description = "Entrance fixture"
            )))
        }
        rule.mainClock.advanceTimeByFrame()
        assertTrue("Financial updates must not restart an already visible card",
            abs(fullyVisible - brightness(capture("lowest-data-update", lowest))) < 6f)

        scrollBy(viewportHeight * 6f)
        rule.mainClock.advanceTimeBy(1_000)
        lowest.assertDoesNotExist()
        scrollBy(-viewportHeight * 8f)
        rule.mainClock.advanceTimeBy(1_000)
        showLowest()
        assertTrue("Scrolling back must retain the completed arrival",
            abs(fullyVisible - brightness(capture("lowest-revisited", lowest))) < 6f)
    }

    private fun awaitStagger() {
        // Coroutine delays use real time; the actual fade/translation use the controlled clock.
        val start = SystemClock.elapsedRealtime()
        rule.waitUntil(5_000) { SystemClock.elapsedRealtime() - start >= 140 }
    }

    private fun capture(name: String, node: SemanticsNodeInteraction): ImageBitmap {
        val image = node.captureToImage()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val folder = File(context.getExternalFilesDir(null) ?: context.filesDir, "daily-card-entrance-test-frames")
        check(folder.exists() || folder.mkdirs())
        File(folder, "$name.png").outputStream().use {
            check(image.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it))
        }
        return image
    }

    private fun brightness(image: ImageBitmap): Float {
        val pixels = image.toPixelMap()
        val values = ArrayList<Float>()
        for (y in image.height / 4 until image.height * 3 / 4) {
            for (x in image.width / 5 until image.width * 4 / 5) {
                val color = pixels[x, y]
                values.add((color.red + color.green + color.blue) * 255f / 3f)
            }
        }
        return values.sorted()[values.size / 2]
    }
}
