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
import androidx.compose.ui.graphics.drawscope.DrawScope
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
 * 13. Movies: Clapperboard opens and closes twice (CLACK! CLACK!)
 */
@Composable
fun MoviesVisual(isSelected: Boolean, trigger: Int, tint: Color) {
    val motion = LocalExperience.current.motion
    val clapAngle = remember { Animatable(0f) }
    val clackImpact = remember { Animatable(0f) }

    var wasSelected by remember { mutableStateOf(isSelected) }
    var lastTrigger by remember { mutableIntStateOf(trigger) }

    LaunchedEffect(isSelected, trigger, motion) {
        val entering = isSelected && !wasSelected
        val reselected = isSelected && wasSelected && (trigger != lastTrigger)
        wasSelected = isSelected
        lastTrigger = trigger

        if ((entering || reselected) && motion == MotionLevel.FULL) {
            coroutineScope {
                // CLAP 1 (0..140ms)
                clapAngle.animateTo(-28f, tween(70, easing = FastOutSlowInEasing))
                clapAngle.animateTo(0f, tween(50, easing = FastOutSlowInEasing))
                clackImpact.snapTo(1f)
                launch { clackImpact.animateTo(0f, tween(50)) }

                // CLAP 2 (140..280ms)
                clapAngle.animateTo(-28f, tween(70, easing = FastOutSlowInEasing))
                clapAngle.animateTo(0f, tween(50, easing = FastOutSlowInEasing))
                clackImpact.snapTo(1f)
                launch { clackImpact.animateTo(0f, tween(60)) }
            }
        } else {
            clapAngle.snapTo(0f)
            clackImpact.snapTo(0f)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val s = size.width / 24f

        // Slate Body (bottom)
        drawRoundRect(
            color = tint,
            topLeft = Offset(4f * s, 10f * s),
            size = Size(16f * s, 10f * s),
            cornerRadius = CornerRadius(1.2f * s)
        )
        // Slate Chalk Lines
        drawLine(Color.White.copy(alpha = 0.5f), Offset(6f * s, 13.5f * s), Offset(18f * s, 13.5f * s), strokeWidth = 1f * s)
        drawLine(Color.White.copy(alpha = 0.5f), Offset(6f * s, 16.5f * s), Offset(14f * s, 16.5f * s), strokeWidth = 1f * s)

        // Top Clapper Arm (hinged at x=4, y=10)
        rotate(degrees = clapAngle.value, pivot = Offset(4f * s, 10f * s)) {
            drawRoundRect(
                color = tint,
                topLeft = Offset(4f * s, 6.5f * s),
                size = Size(16f * s, 3.5f * s),
                cornerRadius = CornerRadius(1f * s)
            )
            // Slanted stripes on top stick
            drawLine(Color.White.copy(alpha = 0.9f), Offset(7f * s, 10f * s), Offset(9f * s, 6.5f * s), strokeWidth = 1.4f * s)
            drawLine(Color.White.copy(alpha = 0.9f), Offset(11f * s, 10f * s), Offset(13f * s, 6.5f * s), strokeWidth = 1.4f * s)
            drawLine(Color.White.copy(alpha = 0.9f), Offset(15f * s, 10f * s), Offset(17f * s, 6.5f * s), strokeWidth = 1.4f * s)
        }

        // Clack impact flash lines
        if (clackImpact.value > 0.05f) {
            val impactColor = tint.copy(alpha = clackImpact.value)
            drawLine(impactColor, Offset(19.5f * s, 7f * s), Offset(22f * s, 5.5f * s), strokeWidth = 1.4f * s, cap = StrokeCap.Round)
            drawLine(impactColor, Offset(19.5f * s, 11f * s), Offset(22f * s, 12.5f * s), strokeWidth = 1.4f * s, cap = StrokeCap.Round)
        }
    }
}

/**
 * 14. Pets: Cat steps past it (3 paw prints step across in sequence)
 */
@Composable
fun PetsVisual(isSelected: Boolean, trigger: Int, tint: Color) {
    val motion = LocalExperience.current.motion
    val paw1Alpha = remember { Animatable(0.2f) }
    val paw2Alpha = remember { Animatable(0.2f) }
    val paw3Alpha = remember { Animatable(0.2f) }

    var wasSelected by remember { mutableStateOf(isSelected) }
    var lastTrigger by remember { mutableIntStateOf(trigger) }

    LaunchedEffect(isSelected, trigger, motion) {
        val entering = isSelected && !wasSelected
        val reselected = isSelected && wasSelected && (trigger != lastTrigger)
        wasSelected = isSelected
        lastTrigger = trigger

        if ((entering || reselected) && motion == MotionLevel.FULL) {
            coroutineScope {
                // Step 1: Left paw steps forward (0..90ms)
                launch {
                    paw1Alpha.animateTo(1f, tween(60, easing = FastOutSlowInEasing))
                    delay(120)
                    paw1Alpha.animateTo(0.25f, tween(100))
                }
                // Step 2: Middle paw steps forward (80..170ms)
                launch {
                    delay(75)
                    paw2Alpha.animateTo(1f, tween(60, easing = FastOutSlowInEasing))
                    delay(120)
                    paw2Alpha.animateTo(0.25f, tween(100))
                }
                // Step 3: Right paw steps forward (150..240ms)
                launch {
                    delay(150)
                    paw3Alpha.animateTo(1f, tween(60, easing = FastOutSlowInEasing))
                    delay(120)
                    paw3Alpha.animateTo(0.25f, tween(100))
                }
            }
        } else {
            paw1Alpha.snapTo(if (isSelected) 0.9f else 0.4f)
            paw2Alpha.snapTo(if (isSelected) 0.9f else 0.4f)
            paw3Alpha.snapTo(if (isSelected) 0.9f else 0.4f)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val s = size.width / 24f

        fun DrawScope.drawPaw(centerX: Float, centerY: Float, alpha: Float, scale: Float = 1f) {
            val color = tint.copy(alpha = alpha)
            // Center main pad
            drawOval(
                color = color,
                topLeft = Offset((centerX - 2.5f * scale) * s, (centerY - 1.8f * scale) * s),
                size = Size(5f * scale * s, 3.8f * scale * s)
            )
            // 4 Toes
            drawCircle(color, radius = 1.0f * scale * s, center = Offset((centerX - 2.5f * scale) * s, (centerY - 3.2f * scale) * s))
            drawCircle(color, radius = 1.1f * scale * s, center = Offset((centerX - 0.9f * scale) * s, (centerY - 4.2f * scale) * s))
            drawCircle(color, radius = 1.1f * scale * s, center = Offset((centerX + 0.9f * scale) * s, (centerY - 4.2f * scale) * s))
            drawCircle(color, radius = 1.0f * scale * s, center = Offset((centerX + 2.5f * scale) * s, (centerY - 3.2f * scale) * s))
        }

        // Paw 1: Bottom Left
        drawPaw(centerX = 6f, centerY = 16f, alpha = paw1Alpha.value, scale = 0.9f)
        // Paw 2: Upper Center
        drawPaw(centerX = 12f, centerY = 9.5f, alpha = paw2Alpha.value, scale = 1.05f)
        // Paw 3: Bottom Right
        drawPaw(centerX = 18f, centerY = 16f, alpha = paw3Alpha.value, scale = 0.9f)
    }
}

/**
 * 15. Work: Briefcase is lifted from the floor and swings as someone walks
 */
@Composable
fun WorkVisual(isSelected: Boolean, trigger: Int, tint: Color) {
    val motion = LocalExperience.current.motion
    val liftY = remember { Animatable(0f) }
    val swingAngle = remember { Animatable(0f) }

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
                    // Lifts up from floor (0..120ms)
                    liftY.animateTo(-5.5f, tween(110, easing = FastOutSlowInEasing))
                    // Walks / swings
                    delay(140)
                    // Sets down firmly at work
                    liftY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                }
                launch {
                    // Swings forward and back like walking
                    delay(90)
                    swingAngle.animateTo(-12f, tween(70, easing = FastOutSlowInEasing))
                    swingAngle.animateTo(14f, tween(80, easing = FastOutSlowInEasing))
                    swingAngle.animateTo(-6f, tween(60, easing = FastOutSlowInEasing))
                    swingAngle.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                }
            }
        } else {
            liftY.snapTo(0f)
            swingAngle.snapTo(0f)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val s = size.width / 24f

        translate(top = liftY.value * s) {
            rotate(degrees = swingAngle.value, pivot = Offset(12f * s, 6.5f * s)) {
                // Briefcase Handle on top
                val handle = Path().apply {
                    moveTo(9.5f * s, 9f * s)
                    lineTo(9.5f * s, 6.5f * s)
                    lineTo(14.5f * s, 6.5f * s)
                    lineTo(14.5f * s, 9f * s)
                }
                drawPath(handle, tint, style = Stroke(width = 1.6f * s, cap = StrokeCap.Round, join = StrokeJoin.Round))

                // Briefcase Body
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(4f * s, 9f * s),
                    size = Size(16f * s, 11.5f * s),
                    cornerRadius = CornerRadius(2f * s)
                )

                // Horizontal leather strap accent
                drawLine(Color.White.copy(alpha = 0.35f), Offset(4f * s, 14.5f * s), Offset(20f * s, 14.5f * s), strokeWidth = 1.2f * s)

                // Brass Center Lock / Clasp
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.9f),
                    topLeft = Offset(11f * s, 13.5f * s),
                    size = Size(2f * s, 2.5f * s),
                    cornerRadius = CornerRadius(0.5f * s)
                )
            }
        }
    }
}
