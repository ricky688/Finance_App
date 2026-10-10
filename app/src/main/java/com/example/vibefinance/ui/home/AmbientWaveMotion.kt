package com.example.vibefinance.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.MotionDurationScale
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

/** Ambient drawing pauses under menus/dialogs without resetting the card or its phase. */
internal val LocalAmbientMotionEnabled = compositionLocalOf { true }

/** A repeating wave phase, consumed from draw/layer blocks rather than card composition. */
@Composable
internal fun rememberAmbientWavePhase(
    enabled: Boolean = true,
    periodMillis: Int = 5_000,
): State<Float> {
    val phase = remember { mutableFloatStateOf(0f) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val running = enabled && LocalAmbientMotionEnabled.current &&
        com.example.vibefinance.ui.preferences.LocalExperience.current.motion == com.example.vibefinance.ui.preferences.MotionLevel.FULL

    LaunchedEffect(lifecycleOwner, running, periodMillis) {
        if (!running) return@LaunchedEffect
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            val durationScale = coroutineContext[MotionDurationScale]
            snapshotFlow { durationScale?.scaleFactor ?: 1f }.collectLatest { scale ->
                // A disabled system animation scale must suspend, never spin a repeat loop.
                if (scale <= 0f) return@collectLatest
                var previousFrame = 0L
                while (isActive) {
                    withFrameNanos { frameTime ->
                        if (previousFrame != 0L) {
                            val advance = (frameTime - previousFrame) /
                                (periodMillis * 1_000_000.0 * scale)
                            phase.floatValue = (phase.floatValue + advance.toFloat()) % 1f
                        }
                        previousFrame = frameTime
                    }
                }
            }
        }
    }
    return phase
}
