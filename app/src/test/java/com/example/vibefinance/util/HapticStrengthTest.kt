package com.example.vibefinance.util

import android.content.Context
import android.os.Vibrator
import android.provider.Settings
import android.view.View
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowVibrator
import org.robolectric.util.ReflectionHelpers

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class HapticStrengthTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private val vibrator get() = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    private val shadow get() = shadowOf(vibrator)

    @Before fun setup() {
        ShadowVibrator.reset()
        shadow.setHasVibrator(true)
        shadow.setHasAmplitudeControl(true)
        Settings.System.putInt(context.contentResolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 1)
        VibeHaptics.enabled = true
        VibeHaptics.strength = HapticStrength.STANDARD
    }
    @After fun cleanup() {
        VibeHaptics.enabled = true
        VibeHaptics.strength = HapticStrength.STANDARD
    }
    private fun amplitudes(): List<Float> = ReflectionHelpers.getStaticField<List<Any>>(
        ShadowVibrator::class.java, "vibrationEffectSegments").map {
        ReflectionHelpers.callInstanceMethod<Float>(it, "getAmplitude")
    }
    @Test @Config(sdk = [31]) fun strengthChangesAmplitudeAndClampsStrongEffects() {
        VibeHaptics.strength = HapticStrength.GENTLE
        VibeHaptics.selection(context)
        assertEquals(55 / 255f, amplitudes().single(), .001f)
        VibeHaptics.strength = HapticStrength.STRONG
        VibeHaptics.selection(context)
        assertEquals(165 / 255f, amplitudes().single(), .001f)
        VibeHaptics.heavyLongPress(context)
        assertEquals(1f, amplitudes().single(), .001f)
    }
    @Test @Config(sdk = [31]) fun confirmationKeepsSilentGapAndScalesBothPulses() {
        VibeHaptics.strength = HapticStrength.GENTLE
        VibeHaptics.successConfirm(context)
        assertArrayEquals(longArrayOf(0, 16, 45, 22), shadow.pattern)
        assertEquals(listOf(0f, 65 / 255f, 0f, 110 / 255f), amplitudes())
    }
    @Test fun deviceWithoutAmplitudeControlAdjustsPulseDurationButNotGap() {
        shadow.setHasAmplitudeControl(false)
        VibeHaptics.strength = HapticStrength.GENTLE
        VibeHaptics.selection(context)
        assertEquals(7L, shadow.milliseconds)
        VibeHaptics.strength = HapticStrength.STRONG
        VibeHaptics.warningAlert(context)
        assertArrayEquals(longArrayOf(0, 38, 45, 38), shadow.pattern)
    }
    @Test @Config(sdk = [24]) fun legacyDevicePreservesConfirmationPattern() {
        VibeHaptics.strength = HapticStrength.GENTLE
        VibeHaptics.successConfirm(context)
        assertArrayEquals(longArrayOf(0, 8, 45, 11), shadow.pattern)
    }
    @Test fun appAndSystemOffBothSuppressCustomFeedback() {
        VibeHaptics.strength = HapticStrength.STRONG
        VibeHaptics.enabled = false
        VibeHaptics.selection(context)
        assertFalse(shadow.isVibrating)
        VibeHaptics.enabled = true
        Settings.System.putInt(context.contentResolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 0)
        VibeHaptics.selection(context)
        assertFalse(shadow.isVibrating)
    }
    @Test @Config(sdk = [31]) fun composeFeedbackUsesSharedStrengthAndLongPressPattern() {
        VibeHaptics.strength = HapticStrength.GENTLE
        VibeHaptics.composeFeedback(View(context)).performHapticFeedback(HapticFeedbackType.LongPress)
        assertEquals(45L, shadow.milliseconds)
        assertEquals(128 / 255f, amplitudes().single(), .001f)
    }
    @Test fun unavailableHardwareAndNullContextAreSafe() {
        shadow.setHasVibrator(false)
        VibeHaptics.strength = HapticStrength.STRONG
        VibeHaptics.selection(context)
        VibeHaptics.successConfirm()
        assertFalse(shadow.isVibrating)
    }
    @Test fun absentOrUnknownPreferenceDefaultsToNativeStandard() {
        assertEquals(HapticStrength.STANDARD, HapticStrength.fromStored(null))
        assertEquals(HapticStrength.STANDARD, HapticStrength.fromStored("unknown"))
        assertEquals(HapticStrength.STRONG, HapticStrength.fromStored("STRONG"))
    }
}
