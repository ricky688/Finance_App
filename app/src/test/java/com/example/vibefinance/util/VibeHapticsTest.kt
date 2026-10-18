package com.example.vibefinance.util

import android.view.View
import org.robolectric.RuntimeEnvironment
import com.example.vibefinance.ui.FinanceIntent
import com.example.vibefinance.ui.FinanceUiState
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class VibeHapticsTest {

    @Test
    fun defaultStateHasHapticFeedbackEnabled() {
        val state = FinanceUiState()
        assertTrue("Haptic feedback should be enabled by default", state.hapticFeedbackEnabled)
    }

    @Test
    fun vibeHapticsExecutesSafelyWithoutCrashing() {
        val context = RuntimeEnvironment.getApplication()
        val view = View(context)

        VibeHaptics.enabled = true
        VibeHaptics.lightTick(view)
        VibeHaptics.selection(view)
        VibeHaptics.toggle(view)
        VibeHaptics.impactMedium(view)
        VibeHaptics.heavyLongPress(view)
        VibeHaptics.successConfirm(view)
        VibeHaptics.warningAlert(view)

        // Null-view overloads
        VibeHaptics.lightTick(context)
        VibeHaptics.selection(context)
        VibeHaptics.toggle(context)
        VibeHaptics.impactMedium(context)
        VibeHaptics.heavyLongPress(context)
        VibeHaptics.successConfirm(context)
        VibeHaptics.warningAlert(context)
    }

    @Test
    fun vibeHapticsRespectsDisabledFlag() {
        val context = RuntimeEnvironment.getApplication()
        val view = View(context)

        VibeHaptics.enabled = false
        VibeHaptics.lightTick(view)
        assertFalse(VibeHaptics.enabled)

        VibeHaptics.enabled = true
        assertTrue(VibeHaptics.enabled)
    }

    @Test
    fun setHapticFeedbackEnabledIntentPreservesState() {
        val intentFalse = FinanceIntent.SetHapticFeedbackEnabled(false)
        assertFalse(intentFalse.enabled)
        val intentTrue = FinanceIntent.SetHapticFeedbackEnabled(true)
        assertTrue(intentTrue.enabled)
    }
}
