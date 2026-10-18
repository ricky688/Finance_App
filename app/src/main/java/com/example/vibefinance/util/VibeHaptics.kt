package com.example.vibefinance.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import androidx.core.view.HapticFeedbackConstantsCompat as H

enum class HapticStrength(val scale: Float) {
    GENTLE(0.5f), STANDARD(1f), STRONG(1.5f);

    companion object {
        fun fromStored(value: String?): HapticStrength = entries.firstOrNull { it.name == value } ?: STANDARD
    }
}

/** Dialogs create their own platform locals; bind app feedback to their actual view too. */
@Composable
fun rememberVibeHapticFeedback(): HapticFeedback {
    val view = LocalView.current
    return remember(view) { VibeHaptics.composeFeedback(view) }
}

/** High-fidelity tactile feedback engine: combines view haptics with precise hardware vibration fallback. */
object VibeHaptics {
    @Volatile
    var enabled: Boolean = true
    @Volatile
    var strength: HapticStrength = HapticStrength.STANDARD

    fun composeFeedback(view: View): HapticFeedback = object : HapticFeedback {
        override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
            when (hapticFeedbackType) {
                HapticFeedbackType.LongPress -> heavyLongPress(view)
                HapticFeedbackType.Confirm -> successConfirm(view)
                HapticFeedbackType.Reject -> warningAlert(view)
                HapticFeedbackType.ToggleOn, HapticFeedbackType.ToggleOff -> toggle(view)
                else -> selection(view)
            }
        }
    }

    private fun getVibrator(context: Context): Vibrator? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator ?: @Suppress("DEPRECATION") (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Throwable) {
            null
        }
    }

    private fun vibrateHardware(
        context: Context?,
        predefined: Int,
        fallbackDurationMs: Long,
        fallbackAmplitude: Int,
        waveformTimings: LongArray? = null,
        waveformAmplitudes: IntArray? = null
    ): Boolean {
        if (!enabled || context == null) return false
        val vibrator = getVibrator(context) ?: return false
        return try {
            if (!vibrator.hasVibrator()) return false
            val level = strength
            val amplitudeControl = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && vibrator.hasAmplitudeControl()
            fun amplitude(value: Int) = if (value == 0) 0 else (value * level.scale).roundToInt().coerceIn(1, 255)
            fun duration(value: Long) = if (value == 0L || amplitudeControl) value else (value * level.scale).roundToLong().coerceAtLeast(1)
            val timings = waveformTimings?.mapIndexed { index, value ->
                if (waveformAmplitudes?.get(index) == 0) value else duration(value)
            }?.toLongArray()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = when {
                    timings != null && waveformAmplitudes != null -> {
                        if (amplitudeControl) VibrationEffect.createWaveform(timings, waveformAmplitudes.map(::amplitude).toIntArray(), -1)
                        else VibrationEffect.createWaveform(timings, -1)
                    }
                    level == HapticStrength.STANDARD && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && predefined >= 0 -> {
                        VibrationEffect.createPredefined(predefined)
                    }
                    amplitudeControl -> {
                        VibrationEffect.createOneShot(fallbackDurationMs, amplitude(fallbackAmplitude))
                    }
                    else -> {
                        VibrationEffect.createOneShot(duration(fallbackDurationMs), VibrationEffect.DEFAULT_AMPLITUDE)
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val attrs = android.os.VibrationAttributes.Builder()
                        .setUsage(android.os.VibrationAttributes.USAGE_TOUCH)
                        .build()
                    vibrator.vibrate(effect, attrs)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(effect)
                }
            } else {
                @Suppress("DEPRECATION")
                if (timings != null) vibrator.vibrate(timings, -1)
                else vibrator.vibrate(duration(fallbackDurationMs))
            }
            true
        } catch (_: Throwable) {
            false
        }
    }

    private fun feedback(
        view: View?,
        constant: Int,
        predefinedEffect: Int = -1,
        fallbackDurationMs: Long = 12,
        fallbackAmplitude: Int = 100,
        waveformTimings: LongArray? = null,
        waveformAmplitudes: IntArray? = null,
        context: Context? = view?.context
    ) {
        if (!enabled) return
        // Direct vibration must honor the same system touch-feedback setting as native view feedback.
        if (context != null && Settings.System.getInt(context.contentResolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) == 0) return

        if (strength != HapticStrength.STANDARD && vibrateHardware(context, predefinedEffect,
                fallbackDurationMs, fallbackAmplitude, waveformTimings, waveformAmplitudes)) return

        var performed = false
        if (view != null) {
            try {
                view.isHapticFeedbackEnabled = true
                performed = view.performHapticFeedback(
                    constant,
                    HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                )
            } catch (_: Throwable) {
                performed = false
            }
        }

        if (!performed && context != null) {
            vibrateHardware(
                context = context,
                predefined = predefinedEffect,
                fallbackDurationMs = fallbackDurationMs,
                fallbackAmplitude = fallbackAmplitude,
                waveformTimings = waveformTimings,
                waveformAmplitudes = waveformAmplitudes
            )
        }
    }

    fun lightTick(view: View) = feedback(
        view = view,
        constant = H.SEGMENT_TICK,
        predefinedEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.EFFECT_TICK else -1,
        fallbackDurationMs = 10,
        fallbackAmplitude = 65
    )

    fun lightTick(context: Context? = null) = feedback(
        view = null,
        constant = H.SEGMENT_TICK,
        predefinedEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.EFFECT_TICK else -1,
        fallbackDurationMs = 10,
        fallbackAmplitude = 65,
        context = context
    )

    fun selection(view: View) = feedback(
        view = view,
        constant = H.TEXT_HANDLE_MOVE,
        predefinedEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.EFFECT_CLICK else -1,
        fallbackDurationMs = 14,
        fallbackAmplitude = 110
    )

    fun selection(context: Context? = null) = feedback(
        view = null,
        constant = H.TEXT_HANDLE_MOVE,
        predefinedEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.EFFECT_CLICK else -1,
        fallbackDurationMs = 14,
        fallbackAmplitude = 110,
        context = context
    )

    fun toggle(view: View) = feedback(
        view = view,
        constant = H.CONTEXT_CLICK,
        predefinedEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.EFFECT_CLICK else -1,
        fallbackDurationMs = 16,
        fallbackAmplitude = 135
    )

    fun toggle(context: Context? = null) = feedback(
        view = null,
        constant = H.CONTEXT_CLICK,
        predefinedEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.EFFECT_CLICK else -1,
        fallbackDurationMs = 16,
        fallbackAmplitude = 135,
        context = context
    )

    fun impactMedium(view: View) = feedback(
        view = view,
        constant = H.VIRTUAL_KEY,
        predefinedEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.EFFECT_HEAVY_CLICK else -1,
        fallbackDurationMs = 22,
        fallbackAmplitude = 180
    )

    fun impactMedium(context: Context? = null) = feedback(
        view = null,
        constant = H.VIRTUAL_KEY,
        predefinedEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.EFFECT_HEAVY_CLICK else -1,
        fallbackDurationMs = 22,
        fallbackAmplitude = 180,
        context = context
    )

    fun heavyLongPress(view: View) = feedback(
        view = view,
        constant = H.LONG_PRESS,
        predefinedEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.EFFECT_HEAVY_CLICK else -1,
        fallbackDurationMs = 45,
        fallbackAmplitude = 255
    )

    fun heavyLongPress(context: Context? = null) = feedback(
        view = null,
        constant = H.LONG_PRESS,
        predefinedEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.EFFECT_HEAVY_CLICK else -1,
        fallbackDurationMs = 45,
        fallbackAmplitude = 255,
        context = context
    )

    fun successConfirm(view: View) = feedback(
        view = view,
        constant = H.CONFIRM,
        predefinedEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.EFFECT_DOUBLE_CLICK else -1,
        fallbackDurationMs = 25,
        fallbackAmplitude = 200,
        waveformTimings = longArrayOf(0, 16, 45, 22),
        waveformAmplitudes = intArrayOf(0, 130, 0, 220)
    )

    fun successConfirm(context: Context? = null) = feedback(
        view = null,
        constant = H.CONFIRM,
        predefinedEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.EFFECT_DOUBLE_CLICK else -1,
        fallbackDurationMs = 25,
        fallbackAmplitude = 200,
        waveformTimings = longArrayOf(0, 16, 45, 22),
        waveformAmplitudes = intArrayOf(0, 130, 0, 220),
        context = context
    )

    fun warningAlert(view: View) = feedback(
        view = view,
        constant = H.REJECT,
        predefinedEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.EFFECT_DOUBLE_CLICK else -1,
        fallbackDurationMs = 30,
        fallbackAmplitude = 220,
        waveformTimings = longArrayOf(0, 25, 45, 25),
        waveformAmplitudes = intArrayOf(0, 200, 0, 200)
    )

    fun warningAlert(context: Context? = null) = feedback(
        view = null,
        constant = H.REJECT,
        predefinedEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.EFFECT_DOUBLE_CLICK else -1,
        fallbackDurationMs = 30,
        fallbackAmplitude = 220,
        waveformTimings = longArrayOf(0, 25, 45, 25),
        waveformAmplitudes = intArrayOf(0, 200, 0, 200),
        context = context
    )
}
