package com.example.vibefinance.util

import android.view.View
import androidx.core.view.HapticFeedbackConstantsCompat as H
import androidx.core.view.ViewCompat

/** View feedback respects system haptic settings and never requires vibration permission. */
object VibeHaptics {
    private fun feedback(view: View, constant: Int) { if (view.isHapticFeedbackEnabled) ViewCompat.performHapticFeedback(view, constant) }
    fun lightTick(view: View) = feedback(view, H.SEGMENT_TICK)
    fun selection(view: View) = feedback(view, H.TEXT_HANDLE_MOVE)
    fun toggle(view: View) = feedback(view, H.CONTEXT_CLICK)
    fun impactMedium(view: View) = feedback(view, H.VIRTUAL_KEY)
    fun heavyLongPress(view: View) = feedback(view, H.LONG_PRESS)
    fun successConfirm(view: View) = feedback(view, H.CONFIRM)
    fun warningAlert(view: View) = feedback(view, H.REJECT)
}
