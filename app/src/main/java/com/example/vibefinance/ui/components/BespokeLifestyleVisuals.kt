package com.example.vibefinance.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import com.example.vibefinance.ui.preferences.LocalExperience
import com.example.vibefinance.ui.preferences.MotionLevel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 5. Flight: Flies up off top and loops back in from bottom (Android flight mode)
 */
@Composable
fun FlightVisual(isSelected: Boolean, trigger: Int, tint: Color) {
    val motion = LocalExperience.current.motion
    val planeX = remember { Animatable(0f) }
    val planeY = remember { Animatable(0f) }
    val planeAlpha = remember { Animatable(1f) }

    var wasSelected by remember { mutableStateOf(isSelected) }
    var lastTrigger by remember { mutableIntStateOf(trigger) }

    LaunchedEffect(isSelected, trigger, motion) {
        val entering = isSelected && !wasSelected
        val reselected = isSelected && wasSelected && (trigger != lastTrigger)
        wasSelected = isSelected
        lastTrigger = trigger

        if ((entering || reselected) && motion == MotionLevel.FULL) {
            coroutineScope {
                // Phase 1: Fly up and out
                launch { planeAlpha.animateTo(0f, tween(140)) }
                launch { planeX.animateTo(12f, tween(150, easing = FastOutSlowInEasing)) }
                planeY.animateTo(-22f, tween(150, easing = FastOutSlowInEasing))

                // Phase 2: Snap to bottom-left and fly back in
                planeX.snapTo(-12f)
                planeY.snapTo(22f)
                launch { planeAlpha.animateTo(1f, tween(80)) }
                launch { planeX.animateTo(0f, tween(180, easing = FastOutSlowInEasing)) }
                planeY.animateTo(0f, tween(180, easing = FastOutSlowInEasing))
            }
        } else {
            planeX.snapTo(0f)
            planeY.snapTo(0f)
            planeAlpha.snapTo(1f)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val s = size.width / 24f
        translate(left = planeX.value * s, top = planeY.value * s) {
            rotate(degrees = -45f, pivot = Offset(12f * s, 12f * s)) {
                val planeColor = tint.copy(alpha = planeAlpha.value)
                val plane = Path().apply {
                    moveTo(12f * s, 3f * s)
                    lineTo(14f * s, 9f * s)
                    lineTo(21f * s, 12f * s)
                    lineTo(21f * s, 14f * s)
                    lineTo(14f * s, 13f * s)
                    lineTo(14f * s, 18f * s)
                    lineTo(16.5f * s, 20f * s)
                    lineTo(16.5f * s, 21.5f * s)
                    lineTo(12f * s, 20.5f * s)
                    lineTo(7.5f * s, 21.5f * s)
                    lineTo(7.5f * s, 20f * s)
                    lineTo(10f * s, 18f * s)
                    lineTo(10f * s, 13f * s)
                    lineTo(3f * s, 14f * s)
                    lineTo(3f * s, 12f * s)
                    lineTo(10f * s, 9f * s)
                    close()
                }
                drawPath(plane, planeColor)
            }
        }
    }
}

/**
 * 6. Home: Warm light turns on in the window + chimney smoke puff drifts up
 */
@Composable
fun HomeVisual(isSelected: Boolean, trigger: Int, tint: Color) {
    val motion = LocalExperience.current.motion
    val windowLight = remember { Animatable(if (isSelected) 1f else 0.2f) }
    val smokeY = remember { Animatable(0f) }
    val smokeAlpha = remember { Animatable(0f) }
    val homeScale = remember { Animatable(1f) }

    var wasSelected by remember { mutableStateOf(isSelected) }
    var lastTrigger by remember { mutableIntStateOf(trigger) }

    LaunchedEffect(isSelected, trigger, motion) {
        val entering = isSelected && !wasSelected
        val reselected = isSelected && wasSelected && (trigger != lastTrigger)
        wasSelected = isSelected
        lastTrigger = trigger

        if ((entering || reselected) && motion == MotionLevel.FULL) {
            coroutineScope {
                launch {
                    // Window light turns on
                    windowLight.snapTo(0f)
                    windowLight.animateTo(1f, tween(120, easing = FastOutSlowInEasing))
                }
                launch {
                    // Chimney smoke puff
                    delay(80)
                    smokeY.snapTo(0f)
                    smokeAlpha.snapTo(1f)
                    launch { smokeAlpha.animateTo(0f, tween(220)) }
                    smokeY.animateTo(-6.5f, tween(220, easing = LinearEasing))
                }
                launch {
                    homeScale.animateTo(1.10f, tween(90))
                    homeScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                }
            }
        } else if (isSelected) {
            windowLight.snapTo(1f)
            smokeAlpha.snapTo(0f)
            homeScale.snapTo(1f)
        } else {
            windowLight.snapTo(0.2f)
            smokeAlpha.snapTo(0f)
            homeScale.snapTo(1f)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val s = size.width / 24f

        // Chimney smoke
        if (smokeAlpha.value > 0.05f) {
            val smokeColor = tint.copy(alpha = smokeAlpha.value * 0.7f)
            drawCircle(smokeColor, radius = 1.6f * s, center = Offset(17f * s, (6f + smokeY.value) * s))
            drawCircle(smokeColor, radius = 2.2f * s, center = Offset(18f * s, (4f + smokeY.value * 1.3f) * s))
        }

        scale(homeScale.value, pivot = Offset(12f * s, 21f * s)) {
            // Chimney
            drawRect(tint, topLeft = Offset(16f * s, 5.5f * s), size = Size(2.5f * s, 5f * s))

            // Roof
            val roof = Path().apply {
                moveTo(3f * s, 11f * s)
                lineTo(12f * s, 3.5f * s)
                lineTo(21f * s, 11f * s)
                close()
            }
            drawPath(roof, tint)

            // House Walls
            val walls = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(5.5f * s, 10.5f * s, 18.5f * s, 20.5f * s),
                        bottomLeft = CornerRadius(1.5f * s),
                        bottomRight = CornerRadius(1.5f * s)
                    )
                )
            }
            drawPath(walls, tint)

            // Window / Door with warm light turn-on effect
            val warmGold = Color(0xFFFFD54F)
            val lightColor = warmGold.copy(alpha = windowLight.value)
            drawRoundRect(
                color = lightColor,
                topLeft = Offset(9.5f * s, 13f * s),
                size = Size(5f * s, 6.5f * s),
                cornerRadius = CornerRadius(1.2f * s)
            )
            // Window frame cross
            drawLine(tint, Offset(12f * s, 13f * s), Offset(12f * s, 19.5f * s), strokeWidth = 1f * s)
            drawLine(tint, Offset(9.5f * s, 16f * s), Offset(14.5f * s, 16f * s), strokeWidth = 1f * s)
        }
    }
}

/**
 * 7. Bills: Minimal $ sign pops up from paper to left
 */
@Composable
fun BillsVisual(isSelected: Boolean, trigger: Int, tint: Color) {
    val motion = LocalExperience.current.motion
    val dollarX = remember { Animatable(0f) }
    val dollarY = remember { Animatable(0f) }
    val dollarAlpha = remember { Animatable(0f) }
    val dollarScale = remember { Animatable(0.7f) }

    var wasSelected by remember { mutableStateOf(isSelected) }
    var lastTrigger by remember { mutableIntStateOf(trigger) }

    LaunchedEffect(isSelected, trigger, motion) {
        val entering = isSelected && !wasSelected
        val reselected = isSelected && wasSelected && (trigger != lastTrigger)
        wasSelected = isSelected
        lastTrigger = trigger

        if ((entering || reselected) && motion == MotionLevel.FULL) {
            coroutineScope {
                dollarX.snapTo(0f)
                dollarY.snapTo(0f)
                dollarAlpha.snapTo(0f)
                dollarScale.snapTo(0.6f)

                launch {
                    dollarAlpha.animateTo(1f, tween(60))
                    dollarAlpha.animateTo(0f, tween(200))
                }
                launch {
                    dollarScale.animateTo(1.2f, tween(90))
                    dollarScale.animateTo(0.8f, tween(170))
                }
                launch {
                    dollarX.animateTo(-8.5f, tween(260, easing = FastOutSlowInEasing))
                }
                dollarY.animateTo(-11f, tween(260, easing = FastOutSlowInEasing))
            }
        } else {
            dollarAlpha.snapTo(0f)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val s = size.width / 24f

        // Receipt / Bill paper
        val paper = Path().apply {
            moveTo(6f * s, 4f * s)
            lineTo(18f * s, 4f * s)
            lineTo(18f * s, 20.5f * s)
            // Serrated bottom edge
            lineTo(15.5f * s, 19.5f * s)
            lineTo(13f * s, 20.5f * s)
            lineTo(10.5f * s, 19.5f * s)
            lineTo(8f * s, 20.5f * s)
            lineTo(6f * s, 19.5f * s)
            close()
        }
        drawPath(paper, tint, style = Stroke(width = 1.6f * s, join = StrokeJoin.Round))

        // Receipt content lines
        drawLine(tint, Offset(8.5f * s, 8f * s), Offset(15.5f * s, 8f * s), strokeWidth = 1.3f * s, cap = StrokeCap.Round)
        drawLine(tint, Offset(8.5f * s, 11.5f * s), Offset(15.5f * s, 11.5f * s), strokeWidth = 1.3f * s, cap = StrokeCap.Round)
        drawLine(tint, Offset(8.5f * s, 15f * s), Offset(13f * s, 15f * s), strokeWidth = 1.3f * s, cap = StrokeCap.Round)

        // Floating minimal $ sign to the left
        if (dollarAlpha.value > 0.05f) {
            val dollarColor = tint.copy(alpha = dollarAlpha.value)
            translate(left = (12f + dollarX.value) * s, top = (6f + dollarY.value) * s) {
                scale(dollarScale.value, pivot = Offset.Zero) {
                    // Minimal $ drawn via path
                    val sPath = Path().apply {
                        moveTo(1.5f * s, -3f * s)
                        cubicTo(-1f * s, -3f * s, -1.8f * s, -1f * s, 0f, 0f)
                        cubicTo(1.8f * s, 1f * s, 1f * s, 3f * s, -1.5f * s, 3f * s)
                    }
                    drawPath(sPath, dollarColor, style = Stroke(width = 1.3f * s, cap = StrokeCap.Round))
                    // Center vertical spine line
                    drawLine(dollarColor, Offset(0f, -4f * s), Offset(0f, 4f * s), strokeWidth = 1.1f * s, cap = StrokeCap.Round)
                }
            }
        }
    }
}

/**
 * 8. Health: Heartbeat ECG wave sweep + radiating healing ripple ring
 */
@Composable
fun HealthVisual(isSelected: Boolean, trigger: Int, tint: Color) {
    val motion = LocalExperience.current.motion
    val ecgProgress = remember { Animatable(0f) }
    val rippleRadius = remember { Animatable(3f) }
    val rippleAlpha = remember { Animatable(0f) }
    val crossScale = remember { Animatable(1f) }

    var wasSelected by remember { mutableStateOf(isSelected) }
    var lastTrigger by remember { mutableIntStateOf(trigger) }

    LaunchedEffect(isSelected, trigger, motion) {
        val entering = isSelected && !wasSelected
        val reselected = isSelected && wasSelected && (trigger != lastTrigger)
        wasSelected = isSelected
        lastTrigger = trigger

        if ((entering || reselected) && motion == MotionLevel.FULL) {
            coroutineScope {
                launch {
                    // Cardiac pulse
                    crossScale.animateTo(1.24f, tween(70, easing = FastOutSlowInEasing))
                    crossScale.animateTo(1.05f, tween(40))
                    crossScale.animateTo(1.28f, tween(70, easing = FastOutSlowInEasing))
                    crossScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                }
                launch {
                    // Radiating circular healing ripple ring
                    rippleRadius.snapTo(3f)
                    rippleAlpha.snapTo(0.9f)
                    launch { rippleAlpha.animateTo(0f, tween(260)) }
                    rippleRadius.animateTo(15f, tween(260, easing = FastOutSlowInEasing))
                }
                launch {
                    // ECG sweep
                    ecgProgress.snapTo(0f)
                    ecgProgress.animateTo(1f, tween(240, easing = LinearEasing))
                }
            }
        } else {
            crossScale.snapTo(1f)
            rippleAlpha.snapTo(0f)
            ecgProgress.snapTo(0f)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val s = size.width / 24f

        // Radiating healing ripple ring
        if (rippleAlpha.value > 0.05f) {
            drawCircle(
                color = tint.copy(alpha = rippleAlpha.value * 0.6f),
                radius = rippleRadius.value * s,
                center = Offset(12f * s, 12f * s),
                style = Stroke(width = 1.3f * s)
            )
        }

        // Cross
        scale(crossScale.value, pivot = Offset(12f * s, 12f * s)) {
            // Vertical bar
            drawRoundRect(
                color = tint,
                topLeft = Offset(9.5f * s, 3.5f * s),
                size = Size(5f * s, 17f * s),
                cornerRadius = CornerRadius(1.8f * s)
            )
            // Horizontal bar
            drawRoundRect(
                color = tint,
                topLeft = Offset(3.5f * s, 9.5f * s),
                size = Size(17f * s, 5f * s),
                cornerRadius = CornerRadius(1.8f * s)
            )
        }

        // ECG Heartbeat wave sweep overlay (white/contrasting pulse)
        if (ecgProgress.value > 0.05f && ecgProgress.value < 0.95f) {
            val ecgPath = Path().apply {
                moveTo(3f * s, 12f * s)
                lineTo(7f * s, 12f * s)
                lineTo(8.5f * s, 9f * s)   // P wave
                lineTo(10f * s, 12f * s)
                lineTo(11f * s, 15f * s)   // Q wave
                lineTo(12.5f * s, 6f * s)  // R wave spike
                lineTo(14f * s, 16f * s)   // S wave
                lineTo(15.5f * s, 12f * s)
                lineTo(17f * s, 10f * s)   // T wave
                lineTo(18.5f * s, 12f * s)
                lineTo(21f * s, 12f * s)
            }
            drawPath(ecgPath, Color.White.copy(alpha = 0.9f), style = Stroke(width = 1.4f * s, cap = StrokeCap.Round))
        }
    }
}
