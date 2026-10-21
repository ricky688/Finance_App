package com.example.vibefinance.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.example.vibefinance.ui.preferences.LocalExperience
import com.example.vibefinance.ui.preferences.MotionLevel
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

internal enum class TapIconEffect { RECALCULATE, SCANNER }

@Stable
internal class TapIconMotion {
    internal val rotation = Animatable(0f)
    internal val scale = Animatable(1f)
    internal var request by mutableIntStateOf(0)
    var isRunning by mutableStateOf(false)
        internal set

    // Acquire synchronously: two clicks in the same frame cannot schedule two actions.
    fun start(): Boolean {
        if (isRunning) return false
        isRunning = true
        request++
        return true
    }

    suspend fun awaitIdle() = snapshotFlow { isRunning }.first { !it }

    val modifier: Modifier
        get() = Modifier.graphicsLayer {
            rotationZ = rotation.value
            scaleX = scale.value
            scaleY = scale.value
        }
}

@Composable
internal fun rememberTapIconMotion(effect: TapIconEffect): TapIconMotion {
    val state = remember { TapIconMotion() }
    val motion = LocalExperience.current.motion
    LaunchedEffect(state.request, motion) {
        if (!state.isRunning) return@LaunchedEffect
        try {
            if (motion == MotionLevel.FULL) {
                when (effect) {
                    TapIconEffect.RECALCULATE -> coroutineScope {
                        var pulsed = false
                        state.rotation.animateTo(360f, spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        )) {
                            // AnimationScope.value is not a Compose-state read or a recomposition.
                            if (!pulsed && value >= 288f) {
                                pulsed = true
                                launch {
                                    state.scale.animateTo(1.18f, tween(90))
                                    state.scale.animateTo(1f, spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMediumLow,
                                    ))
                                }
                            }
                        }
                    }
                    TapIconEffect.SCANNER -> {
                        state.scale.animateTo(0.88f, tween(55))
                        state.scale.animateTo(1.15f, tween(75))
                        state.scale.animateTo(1f, spring(dampingRatio = 0.75f,
                            stiffness = 1600f, visibilityThreshold = 0.01f))
                    }
                }
            }
        } finally {
            // Direct state reset also runs when a preference change cancels the effect.
            withContext(NonCancellable) {
                state.rotation.snapTo(0f)
                state.scale.snapTo(1f)
                state.isRunning = false
            }
        }
    }
    return state
}

@Composable
internal fun transactionFabIconMotion(expanded: Boolean): Modifier {
    val motion = LocalExperience.current.motion
    val rotation = remember { Animatable(if (expanded) 45f else 0f) }
    val scale = remember { Animatable(1f) }
    var previous by remember { mutableStateOf(expanded) }
    LaunchedEffect(expanded, motion) {
        val opening = expanded && !previous
        previous = expanded
        scale.snapTo(1f)
        if (motion != MotionLevel.FULL) {
            rotation.snapTo(if (expanded) 45f else 0f)
        } else coroutineScope {
            launch { rotation.animateTo(if (expanded) 45f else 0f, spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow)) }
            if (opening) {
                scale.animateTo(1.12f, tween(90))
                scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow))
            }
        }
    }
    return Modifier.graphicsLayer {
        rotationZ = rotation.value
        scaleX = scale.value
        scaleY = scale.value
    }
}
