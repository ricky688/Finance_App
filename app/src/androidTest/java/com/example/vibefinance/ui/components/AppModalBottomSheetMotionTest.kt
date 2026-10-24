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
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
    private var allowHide = true
    private lateinit var state: SheetState
    private lateinit var scope: CoroutineScope
    private lateinit var dismissSheet: (() -> Unit) -> Unit
    private var childScheme: MotionScheme? = null
    private val parentScheme = MotionScheme.standard()

    private fun show() {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            MaterialTheme(motionScheme = parentScheme) {
                state = rememberModalBottomSheetState(skipPartiallyExpanded = true,
                    confirmValueChange = { it != SheetValue.Hidden || allowHide })
                scope = rememberCoroutineScope()
                dismissSheet = rememberSheetDismissAction(state)
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

    @Test fun openingDeceleratesOver320ms_andClosingAcceleratesOver220ms() {
        show()
        val expanded = offset()
        rule.runOnIdle { scope.launch { state.hide() } }
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeBy(44)
        val early = offset()
        rule.mainClock.advanceTimeBy(66)
        val middle = offset()
        rule.mainClock.advanceTimeBy(88)
        val late = offset()
        rule.mainClock.advanceTimeBy(32)
        val hidden = offset()
        assertEquals(SheetValue.Hidden, state.currentValue)
        val closingEarly = (early - expanded) / (hidden - expanded)
        val closingMiddle = (middle - expanded) / (hidden - expanded)
        val closingLate = (late - expanded) / (hidden - expanded)
        assertTrue(closingEarly in 0f..0.3f)
        assertTrue(closingMiddle > closingEarly && closingMiddle < closingLate)
        assertTrue(closingLate in 0.75f..1f)

        rule.runOnIdle { scope.launch { state.show() } }
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeBy(64)
        val openingEarly = (hidden - offset()) / (hidden - expanded)
        rule.mainClock.advanceTimeBy(96)
        val openingMiddle = (hidden - offset()) / (hidden - expanded)
        rule.mainClock.advanceTimeBy(128)
        val openingLate = (hidden - offset()) / (hidden - expanded)
        rule.mainClock.advanceTimeBy(48)
        assertEquals(SheetValue.Expanded, state.currentValue)
        assertEquals(expanded, offset(), 1f)
        assertTrue("Opening should settle earlier than an accelerating exit", openingEarly > closingEarly)
        assertTrue(openingEarly < openingMiddle && openingMiddle < openingLate)
        assertTrue("Opening should decelerate near its destination", openingMiddle > 0.7f && openingLate in 0.95f..1f)
    }

    @Test fun buttonDismiss_keepsSheetMountedUntilHidden_andRunsOnlyOneCallback() {
        show()
        var callbacks = 0
        rule.runOnIdle {
            dismissSheet { callbacks++ }
            dismissSheet { callbacks++ }
        }
        rule.mainClock.advanceTimeBy(96)
        assertTrue(state.isVisible)
        assertEquals(0, callbacks)
        rule.mainClock.advanceTimeBy(240)
        rule.runOnIdle { assertEquals(1, callbacks) }
        assertEquals(SheetValue.Hidden, state.currentValue)
        rule.runOnIdle { dismissSheet { callbacks++ } }
        rule.mainClock.advanceTimeBy(300)
        assertEquals(1, callbacks)
    }

    @Test fun rejectedHide_doesNotCommit_andCanRetryAfterConfirmation() {
        show()
        var callbacks = 0
        rule.runOnIdle { allowHide = false; dismissSheet { callbacks++ } }
        rule.mainClock.advanceTimeBy(300)
        rule.runOnIdle { assertEquals(0, callbacks) }
        assertEquals(SheetValue.Expanded, state.currentValue)
        rule.runOnIdle { allowHide = true; dismissSheet { callbacks++ } }
        rule.mainClock.advanceTimeBy(300)
        rule.runOnIdle { assertEquals(1, callbacks) }
        assertEquals(SheetValue.Hidden, state.currentValue)
    }

    @Test fun mandatoryRecalculation_onlyCommitsSelectedModeAfterExit() {
        rule.mainClock.autoAdvance = false
        var selections = 0
        var dismissals = 0
        var selected: com.example.vibefinance.data.entity.RolloverMode? = null
        rule.setContent {
            MaterialTheme {
                com.example.vibefinance.ui.home.RecalcBudgetSheet(
                    budgetInfo = com.example.vibefinance.data.repository.DailyBudgetInfo(
                        2500.0, 60.5, 2439.5, 94.0, 94.0, 26, 1L, 30L * 86_400_000),
                    yesterdayLeftover = 20.0,
                    isMandatory = true,
                    onSelectMode = { mode, _ -> selected = mode; selections++ },
                    onDismiss = { dismissals++ },
                )
            }
        }
        rule.mainClock.advanceTimeBy(1_000)
        androidx.test.espresso.Espresso.pressBack()
        rule.mainClock.advanceTimeBy(300)
        assertEquals(0, dismissals)
        val context = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
        rule.onNodeWithText(context.getString(com.example.vibefinance.R.string.split_rest_days_title)).performClick()
        rule.mainClock.advanceTimeBy(64)
        assertEquals(0, selections)
        rule.mainClock.advanceTimeBy(300)
        rule.runOnIdle {
            assertEquals(1, selections)
            assertEquals(1, dismissals)
            assertEquals(com.example.vibefinance.data.entity.RolloverMode.DISTRIBUTE_EVENLY, selected)
        }
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
