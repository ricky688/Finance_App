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
 * 9. School: Graduation cap toss up with tassel sway and float back down
 */
@Composable
fun SchoolVisual(isSelected: Boolean, trigger: Int, tint: Color) {
    val motion = LocalExperience.current.motion
    val capY = remember { Animatable(0f) }
    val capAngle = remember { Animatable(0f) }
    val tasselAngle = remember { Animatable(0f) }

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
                    // Toss high up into the air
                    capY.animateTo(-7.5f, tween(110, easing = FastOutSlowInEasing))
                    // Float softly back down
                    capY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                }
                launch {
                    capAngle.animateTo(-16f, tween(110, easing = FastOutSlowInEasing))
                    capAngle.animateTo(10f, tween(90))
                    capAngle.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                }
                launch {
                    delay(80)
                    tasselAngle.animateTo(30f, tween(90))
                    tasselAngle.animateTo(-18f, tween(80))
                    tasselAngle.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                }
            }
        } else {
            capY.snapTo(0f)
            capAngle.snapTo(0f)
            tasselAngle.snapTo(0f)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val s = size.width / 24f

        translate(top = capY.value * s) {
            rotate(degrees = capAngle.value, pivot = Offset(12f * s, 12f * s)) {
                // Skull cap base below mortarboard
                val skull = Path().apply {
                    moveTo(7.5f * s, 11f * s)
                    lineTo(16.5f * s, 11f * s)
                    cubicTo(16.5f * s, 17f * s, 7.5f * s, 17f * s, 7.5f * s, 11f * s)
                    close()
                }
                drawPath(skull, tint)

                // Diamond mortarboard top
                val mortarboard = Path().apply {
                    moveTo(12f * s, 4.5f * s)
                    lineTo(21.5f * s, 9f * s)
                    lineTo(12f * s, 13.5f * s)
                    lineTo(2.5f * s, 9f * s)
                    close()
                }
                drawPath(mortarboard, tint)

                // Center button
                drawCircle(Color.White, radius = 1.2f * s, center = Offset(12f * s, 9f * s))

                // Swaying tassel
                rotate(degrees = tasselAngle.value, pivot = Offset(12f * s, 9f * s)) {
                    val tassel = Path().apply {
                        moveTo(12f * s, 9f * s)
                        lineTo(18.5f * s, 12f * s)
                        lineTo(18.5f * s, 17f * s)
                    }
                    drawPath(tassel, tint, style = Stroke(width = 1.3f * s, cap = StrokeCap.Round))
                    drawCircle(tint, radius = 1.2f * s, center = Offset(18.5f * s, 17f * s))
                }
            }
        }
    }
}

/**
 * 10. Fitness: Dumbbell lifted twice (lift 1, down, lift 2, down)
 */
@Composable
fun FitnessVisual(isSelected: Boolean, trigger: Int, tint: Color) {
    val motion = LocalExperience.current.motion
    val liftY = remember { Animatable(0f) }
    val tiltAngle = remember { Animatable(0f) }
    val bellScaleY = remember { Animatable(1f) }

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
                    // LIFT 1 (0..150ms)
                    tiltAngle.animateTo(-8f, tween(75, easing = FastOutSlowInEasing))
                    tiltAngle.animateTo(0f, tween(65, easing = FastOutSlowInEasing))
                    // LIFT 2 (150..340ms)
                    tiltAngle.animateTo(8f, tween(85, easing = FastOutSlowInEasing))
                    tiltAngle.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                }
                launch {
                    // LIFT 1 (up and down)
                    liftY.animateTo(-5f, tween(75, easing = FastOutSlowInEasing))
                    liftY.animateTo(0f, tween(65, easing = FastOutSlowInEasing))
                    // LIFT 2 (higher with hold and floor impact)
                    liftY.animateTo(-6.5f, tween(85, easing = FastOutSlowInEasing))
                    delay(35) // hold at top
                    liftY.animateTo(0f, tween(65, easing = FastOutSlowInEasing))
                    // Floor impact squash
                    bellScaleY.animateTo(0.85f, tween(40))
                    bellScaleY.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                }
            }
        } else {
            liftY.snapTo(0f)
            tiltAngle.snapTo(0f)
            bellScaleY.snapTo(1f)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val s = size.width / 24f

        translate(top = liftY.value * s) {
            rotate(degrees = tiltAngle.value, pivot = Offset(12f * s, 12f * s)) {
                scale(scaleX = 1f, scaleY = bellScaleY.value, pivot = Offset(12f * s, 12f * s)) {
                    // Center grip bar
                    drawLine(tint, Offset(7f * s, 12f * s), Offset(17f * s, 12f * s), strokeWidth = 2.2f * s, cap = StrokeCap.Round)

                    // Left Inner & Outer Weight Plates
                    drawRoundRect(tint, Offset(6.5f * s, 7.5f * s), Size(2.2f * s, 9f * s), CornerRadius(1f * s))
                    drawRoundRect(tint, Offset(3.5f * s, 8.5f * s), Size(2.2f * s, 7f * s), CornerRadius(1f * s))

                    // Right Inner & Outer Weight Plates
                    drawRoundRect(tint, Offset(15.3f * s, 7.5f * s), Size(2.2f * s, 9f * s), CornerRadius(1f * s))
                    drawRoundRect(tint, Offset(18.3f * s, 8.5f * s), Size(2.2f * s, 7f * s), CornerRadius(1f * s))
                }
            }
        }
    }
}

/**
 * 11. Gift: Gift box lid opens up with sparkles bursting out
 */
@Composable
fun GiftVisual(isSelected: Boolean, trigger: Int, tint: Color) {
    val motion = LocalExperience.current.motion
    val lidY = remember { Animatable(0f) }
    val lidAngle = remember { Animatable(0f) }
    val confettiAlpha = remember { Animatable(0f) }
    val confettiY = remember { Animatable(0f) }
    val boxAngle = remember { Animatable(0f) }

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
                    // Anticipation box wiggle
                    boxAngle.animateTo(-7f, tween(35))
                    boxAngle.animateTo(7f, tween(40))
                    boxAngle.animateTo(0f, tween(30))
                }
                launch {
                    delay(70)
                    // Lid pops up and hinges open
                    launch { lidAngle.animateTo(-22f, tween(110, easing = FastOutSlowInEasing)) }
                    lidY.animateTo(-6.5f, tween(110, easing = FastOutSlowInEasing))
                    delay(30)
                    launch { lidAngle.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
                    lidY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                }
                launch {
                    // Confetti burst
                    delay(90)
                    confettiY.snapTo(0f)
                    confettiAlpha.snapTo(1f)
                    launch { confettiAlpha.animateTo(0f, tween(180)) }
                    confettiY.animateTo(-9f, tween(180, easing = FastOutSlowInEasing))
                }
            }
        } else {
            lidY.snapTo(0f)
            lidAngle.snapTo(0f)
            confettiAlpha.snapTo(0f)
            boxAngle.snapTo(0f)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val s = size.width / 24f

        // Confetti sparkles
        if (confettiAlpha.value > 0.05f) {
            val cColor = tint.copy(alpha = confettiAlpha.value)
            val cY = confettiY.value * s
            drawCircle(cColor, radius = 1.3f * s, center = Offset(12f * s, (10f * s) + cY))
            drawCircle(cColor, radius = 1.0f * s, center = Offset(8f * s, (11f * s) + cY * 0.8f))
            drawCircle(cColor, radius = 1.0f * s, center = Offset(16f * s, (11f * s) + cY * 0.8f))
        }

        rotate(degrees = boxAngle.value, pivot = Offset(12f * s, 21f * s)) {
            // Box Body
            drawRoundRect(
                color = tint,
                topLeft = Offset(5.5f * s, 11f * s),
                size = Size(13f * s, 10f * s),
                cornerRadius = CornerRadius(1.2f * s)
            )
            // Vertical ribbon on box
            drawRect(Color.White.copy(alpha = 0.85f), Offset(11f * s, 11f * s), Size(2f * s, 10f * s))

            // Box Lid (with opening animation)
            translate(top = lidY.value * s) {
                rotate(degrees = lidAngle.value, pivot = Offset(4f * s, 10f * s)) {
                    // Lid Rim
                    drawRoundRect(
                        color = tint,
                        topLeft = Offset(4f * s, 8f * s),
                        size = Size(16f * s, 3.5f * s),
                        cornerRadius = CornerRadius(1f * s)
                    )
                    // Lid ribbon
                    drawRect(Color.White.copy(alpha = 0.85f), Offset(11f * s, 8f * s), Size(2f * s, 3.5f * s))

                    // Top Bow Loops
                    val bowLeft = Path().apply {
                        moveTo(12f * s, 8f * s)
                        cubicTo(7.5f * s, 4f * s, 8f * s, 8f * s, 12f * s, 8f * s)
                    }
                    drawPath(bowLeft, tint, style = Stroke(width = 1.5f * s))
                    val bowRight = Path().apply {
                        moveTo(12f * s, 8f * s)
                        cubicTo(16.5f * s, 4f * s, 16f * s, 8f * s, 12f * s, 8f * s)
                    }
                    drawPath(bowRight, tint, style = Stroke(width = 1.5f * s))
                }
            }
        }
    }
}

/**
 * 12. Games: Console being played (D-pad tilts, action buttons tap)
 */
@Composable
fun GamesVisual(isSelected: Boolean, trigger: Int, tint: Color) {
    val motion = LocalExperience.current.motion
    val dpadShiftX = remember { Animatable(0f) }
    val dpadShiftY = remember { Animatable(0f) }
    val btn1Alpha = remember { Animatable(0.4f) }
    val btn2Alpha = remember { Animatable(0.4f) }
    val btn3Alpha = remember { Animatable(0.4f) }
    val rumbleScale = remember { Animatable(1f) }

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
                    // D-pad thumb taps (left -> up -> right)
                    dpadShiftX.animateTo(-1.5f, tween(40))
                    dpadShiftX.animateTo(0f, tween(30))
                    dpadShiftY.animateTo(-1.5f, tween(40))
                    dpadShiftY.animateTo(0f, tween(30))
                }
                launch {
                    // Right action buttons press in rapid combo
                    delay(70)
                    btn1Alpha.animateTo(1f, tween(40)); btn1Alpha.animateTo(0.4f, tween(40))
                    btn2Alpha.animateTo(1f, tween(40)); btn2Alpha.animateTo(0.4f, tween(40))
                    btn3Alpha.animateTo(1f, tween(40)); btn3Alpha.animateTo(0.4f, tween(40))
                }
                launch {
                    delay(200)
                    // Controller victory rumble pop
                    rumbleScale.animateTo(1.18f, tween(70))
                    rumbleScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                }
            }
        } else {
            dpadShiftX.snapTo(0f)
            dpadShiftY.snapTo(0f)
            btn1Alpha.snapTo(0.4f)
            btn2Alpha.snapTo(0.4f)
            btn3Alpha.snapTo(0.4f)
            rumbleScale.snapTo(1f)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val s = size.width / 24f

        scale(rumbleScale.value, pivot = Offset(12f * s, 12f * s)) {
            // Gamepad body
            val pad = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(3f * s, 7f * s, 21f * s, 17f * s),
                        topLeft = CornerRadius(4f * s),
                        topRight = CornerRadius(4f * s),
                        bottomLeft = CornerRadius(5.5f * s),
                        bottomRight = CornerRadius(5.5f * s)
                    )
                )
            }
            drawPath(pad, tint)

            // Screen/center grip contour
            drawRoundRect(
                color = Color.White.copy(alpha = 0.25f),
                topLeft = Offset(10f * s, 9f * s),
                size = Size(4f * s, 3f * s),
                cornerRadius = CornerRadius(1f * s)
            )

            // Left D-pad (with shift motion)
            translate(left = dpadShiftX.value * s, top = dpadShiftY.value * s) {
                // Vertical arm
                drawRect(Color.White, Offset(6.8f * s, 9.5f * s), Size(1.4f * s, 4f * s))
                // Horizontal arm
                drawRect(Color.White, Offset(5.5f * s, 10.8f * s), Size(4f * s, 1.4f * s))
            }

            // Right Action Buttons (with pressing glow)
            drawCircle(Color.White.copy(alpha = btn1Alpha.value), radius = 1.1f * s, center = Offset(17f * s, 9.8f * s))  // Top (Y)
            drawCircle(Color.White.copy(alpha = btn2Alpha.value), radius = 1.1f * s, center = Offset(15.4f * s, 11.5f * s)) // Left (X)
            drawCircle(Color.White.copy(alpha = btn3Alpha.value), radius = 1.1f * s, center = Offset(18.6f * s, 11.5f * s)) // Right (B)
            drawCircle(Color.White.copy(alpha = 0.4f), radius = 1.1f * s, center = Offset(17f * s, 13.2f * s)) // Bottom (A)
        }
    }
}
