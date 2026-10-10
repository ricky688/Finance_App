@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Settings and Daily FAB sheet opening/Back dismissal explored with ARTEMIS/ADB first.
 * Sample the actual native SheetState with a deterministic frame clock; no device settings change.
 */
@RunWith(AndroidJUnit4::class)
class AppModalBottomSheetMotionTest {
    @get:Rule val rule = createComposeRule()
    private lateinit var state: SheetState
    private lateinit var scope: CoroutineScope
    private var childScheme: MotionScheme? = null
    private val parentScheme = MotionScheme.standard()

    private fun show() {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            MaterialTheme(motionScheme = parentScheme) {
                state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                scope = rememberCoroutineScope()
                AppModalBottomSheet(onDismissRequest = {}, sheetState = state) {
                    childScheme = MaterialTheme.motionScheme
                    Box(Modifier.fillMaxWidth().height(300.dp)) { Text("Sheet motion fixture") }
                }
            }
        }
        rule.mainClock.advanceTimeBy(1_000)
        rule.waitForIdle()
        assertEquals(SheetValue.Expanded, state.currentValue)
        assertSame("Sheet easing must not replace its content's motion scheme", parentScheme, childScheme)
    }

    private fun offset(): Float = rule.runOnIdle { state.requireOffset() }

    @Test fun openingAndClosingUseAnEasedBounded320msSlide() {
        show()
        val expanded = offset()
        rule.runOnIdle { scope.launch { state.hide() } }
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeBy(64)
        val early = offset()
        rule.mainClock.advanceTimeBy(64)
        val middle = offset()
        rule.mainClock.advanceTimeBy(128)
        val late = offset()
        rule.mainClock.advanceTimeBy(64)
        val hidden = offset()
        assertEquals(SheetValue.Hidden, state.currentValue)
        assertEasedProgress((early - expanded) / (hidden - expanded),
            (middle - expanded) / (hidden - expanded), (late - expanded) / (hidden - expanded))

        rule.runOnIdle { scope.launch { state.show() } }
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeBy(64)
        val openingEarly = offset()
        rule.mainClock.advanceTimeBy(64)
        val openingMiddle = offset()
        rule.mainClock.advanceTimeBy(128)
        val openingLate = offset()
        rule.mainClock.advanceTimeBy(64)
        assertEquals(SheetValue.Expanded, state.currentValue)
        assertEquals(expanded, offset(), 1f)
        assertEasedProgress((hidden - openingEarly) / (hidden - expanded),
            (hidden - openingMiddle) / (hidden - expanded), (hidden - openingLate) / (hidden - expanded))
    }

    private fun assertEasedProgress(early: Float, middle: Float, late: Float) {
        assertTrue("The slide must ease into movement: $early", early in 0f..0.3f)
        assertTrue("The middle must move faster: $middle", middle > early + 0.2f)
        assertTrue("The slide must slow near its destination: $late", late in 0.9f..1f)
        assertTrue(early < middle && middle < late)
    }

    @Test fun reducedAndDisabledDurationScalesAreRespected() {
        show()
        val reduced = object : MotionDurationScale { override val scaleFactor = 0.35f }
        rule.runOnIdle { scope.launch(reduced) { state.hide() } }
        rule.mainClock.advanceTimeBy(160)
        rule.waitForIdle()
        assertEquals(SheetValue.Hidden, state.currentValue)

        val disabled = object : MotionDurationScale { override val scaleFactor = 0f }
        rule.runOnIdle { scope.launch(disabled) { state.show() } }
        rule.mainClock.advanceTimeBy(32)
        rule.waitForIdle()
        assertEquals(SheetValue.Expanded, state.currentValue)
        assertFalse(state.isAnimationRunning)
    }
}
