package com.example.vibefinance.ui.preferences

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.MotionDurationScale
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import com.example.vibefinance.util.CoordinatedPreferences

enum class MotionLevel { FULL, REDUCED, MINIMAL;
    companion object { fun fromStored(value: String?) = entries.firstOrNull { it.name == value } ?: FULL }
}
data class ExperiencePreferences(val hideAmounts: Boolean = false, val motion: MotionLevel = MotionLevel.FULL, val blur: Float = 1f)
val LocalExperience = staticCompositionLocalOf { ExperiencePreferences() }
val LocalAppLockAction = staticCompositionLocalOf<(Boolean) -> Unit> { {} }

/** App-scoped animation speed: never writes Android's device animation or display settings. */
class AppMotionScale : MotionDurationScale {
    var systemScale by mutableFloatStateOf(1f)
    var appScale by mutableFloatStateOf(1f)
    override val scaleFactor: Float get() = systemScale * appScale
    fun update(context: Context) {
        val prefs = CoordinatedPreferences.get(context, "vibe_finance_prefs")
        appScale = when (MotionLevel.fromStored(prefs.getString("motion_level", null))) {
            MotionLevel.FULL -> 1f; MotionLevel.REDUCED -> 0.35f; MotionLevel.MINIMAL -> 0f
        }
        systemScale = android.provider.Settings.Global.getFloat(context.contentResolver,
            android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1f).coerceAtLeast(0f)
    }
}
