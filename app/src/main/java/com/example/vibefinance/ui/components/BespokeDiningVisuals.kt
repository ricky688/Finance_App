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
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import com.example.vibefinance.ui.preferences.LocalExperience
import com.example.vibefinance.ui.preferences.MotionLevel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 1. Food: Knife and fork touch each other twice (clink! clink!)
 */
@Composable
fun FoodVisual(isSelected: Boolean, trigger: Int, tint: Color) {
    val motion = LocalExperience.current.motion
    val forkAngle = remember { Animatable(0f) }
    val knifeAngle = remember { Animatable(0f) }
    val sparkAlpha = remember { Animatable(0f) }

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
                    // Touch 1
                    forkAngle.animateTo(18f, tween(75, easing = FastOutSlowInEasing))
                    forkAngle.animateTo(4f, tween(50, easing = LinearEasing))
                    // Touch 2
                    forkAngle.animateTo(20f, tween(65, easing = FastOutSlowInEasing))
                    forkAngle.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                }
                launch {
                    // Touch 1
                    knifeAngle.animateTo(-18f, tween(75, easing = FastOutSlowInEasing))
                    knifeAngle.animateTo(-4f, tween(50, easing = LinearEasing))
                    // Touch 2
                    knifeAngle.animateTo(-20f, tween(65, easing = FastOutSlowInEasing))
                    knifeAngle.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                }
                launch {
                    // Spark 1
                    delay(70)
                    sparkAlpha.snapTo(1f)
                    sparkAlpha.animateTo(0f, tween(60))
                    // Spark 2
                    delay(55)
                    sparkAlpha.snapTo(1f)
                    sparkAlpha.animateTo(0f, tween(80))
                }
            }
        } else {
            forkAngle.snapTo(0f)
            knifeAngle.snapTo(0f)
            sparkAlpha.snapTo(0f)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val s = size.width / 24f

        // Draw Left Fork
        rotate(degrees = forkAngle.value, pivot = Offset(7f * s, 20f * s)) {
            // Handle
            drawLine(tint, Offset(7f * s, 12f * s), Offset(7f * s, 21f * s), strokeWidth = 1.8f * s, cap = StrokeCap.Round)
            // Fork Head base
            val forkBase = Path().apply {
                moveTo(4.5f * s, 8f * s)
                quadraticBezierTo(7f * s, 13f * s, 9.5f * s, 8f * s)
            }
            drawPath(forkBase, tint, style = Stroke(width = 1.6f * s, cap = StrokeCap.Round))
            // Tines (prongs)
            drawLine(tint, Offset(4.8f * s, 8f * s), Offset(4.8f * s, 4f * s), strokeWidth = 1.3f * s, cap = StrokeCap.Round)
            drawLine(tint, Offset(7f * s, 9.5f * s), Offset(7f * s, 4f * s), strokeWidth = 1.3f * s, cap = StrokeCap.Round)
            drawLine(tint, Offset(9.2f * s, 8f * s), Offset(9.2f * s, 4f * s), strokeWidth = 1.3f * s, cap = StrokeCap.Round)
        }

        // Draw Right Knife
        rotate(degrees = knifeAngle.value, pivot = Offset(17f * s, 20f * s)) {
            // Handle
            drawLine(tint, Offset(17f * s, 13f * s), Offset(17f * s, 21f * s), strokeWidth = 1.8f * s, cap = StrokeCap.Round)
            // Blade
            val blade = Path().apply {
                moveTo(17f * s, 13f * s)
                lineTo(17f * s, 4f * s)
                cubicTo(19.5f * s, 5f * s, 20f * s, 10f * s, 17f * s, 13f * s)
                close()
            }
            drawPath(blade, tint)
        }

        // Touch contact spark
        if (sparkAlpha.value > 0.05f) {
            val sparkColor = tint.copy(alpha = sparkAlpha.value)
            drawLine(sparkColor, Offset(10f * s, 6f * s), Offset(14f * s, 6f * s), strokeWidth = 1.5f * s, cap = StrokeCap.Round)
            drawLine(sparkColor, Offset(12f * s, 4f * s), Offset(12f * s, 8f * s), strokeWidth = 1.5f * s, cap = StrokeCap.Round)
        }
    }
}

/**
 * 2. Coffee: Coffee cup fills up then smoke rises to show it is hot
 */
@Composable
fun CoffeeVisual(isSelected: Boolean, trigger: Int, tint: Color) {
    val motion = LocalExperience.current.motion
    val fillLevel = remember { Animatable(if (isSelected) 1f else 0.85f) }
    val steamY = remember { Animatable(0f) }
    val steamAlpha = remember { Animatable(0f) }

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
                    // Fill cup from bottom
                    fillLevel.snapTo(0f)
                    fillLevel.animateTo(1f, tween(180, easing = FastOutSlowInEasing))
                }
                launch {
                    // Smoke/Steam curls up after cup fills
                    delay(160)
                    steamY.snapTo(0f)
                    steamAlpha.snapTo(0f)
                    launch { steamAlpha.animateTo(1f, tween(60)); steamAlpha.animateTo(0f, tween(160)) }
                    steamY.animateTo(-6f, tween(220, easing = LinearEasing))
                }
            }
        } else if (isSelected) {
            fillLevel.snapTo(1f)
            steamAlpha.snapTo(0f)
        } else {
            fillLevel.snapTo(0.85f)
            steamAlpha.snapTo(0f)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val s = size.width / 24f

        // Steam waves above cup rim (y = 8f)
        if (steamAlpha.value > 0.05f) {
            val steamColor = tint.copy(alpha = steamAlpha.value * 0.8f)
            translate(top = steamY.value * s) {
                // Wave 1
                val wave1 = Path().apply {
                    moveTo(9f * s, 7f * s)
                    cubicTo(7.5f * s, 4.5f * s, 10.5f * s, 3f * s, 9f * s, 1f * s)
                }
                drawPath(wave1, steamColor, style = Stroke(width = 1.3f * s, cap = StrokeCap.Round))
                // Wave 2
                val wave2 = Path().apply {
                    moveTo(13f * s, 7f * s)
                    cubicTo(11.5f * s, 4.5f * s, 14.5f * s, 3f * s, 13f * s, 1f * s)
                }
                drawPath(wave2, steamColor, style = Stroke(width = 1.3f * s, cap = StrokeCap.Round))
            }
        }

        // Cup Body Path
        val cupPath = Path().apply {
            moveTo(5f * s, 8.5f * s)
            lineTo(17f * s, 8.5f * s)
            cubicTo(17f * s, 17f * s, 15f * s, 19f * s, 11f * s, 19f * s)
            cubicTo(7f * s, 19f * s, 5f * s, 17f * s, 5f * s, 8.5f * s)
            close()
        }

        // Fill liquid inside cup
        clipPath(cupPath) {
            val liquidHeight = 10.5f * fillLevel.value
            drawRect(
                color = tint,
                topLeft = Offset(4f * s, (19f - liquidHeight) * s),
                size = Size(14f * s, liquidHeight * s)
            )
        }

        // Cup Outline
        drawPath(cupPath, tint, style = Stroke(width = 1.6f * s, join = StrokeJoin.Round))

        // Handle
        val handle = Path().apply {
            moveTo(16.5f * s, 10f * s)
            cubicTo(20.5f * s, 10f * s, 20.5f * s, 16f * s, 15f * s, 16f * s)
        }
        drawPath(handle, tint, style = Stroke(width = 1.6f * s, cap = StrokeCap.Round))

        // Saucer
        drawLine(tint, Offset(4f * s, 20.5f * s), Offset(18f * s, 20.5f * s), strokeWidth = 1.6f * s, cap = StrokeCap.Round)
    }
}

/**
 * 3. Shopping: Something drops into the bag and the bag stretches/bounces
 */
@Composable
fun ShoppingVisual(isSelected: Boolean, trigger: Int, tint: Color) {
    val motion = LocalExperience.current.motion
    val itemY = remember { Animatable(0f) }
    val itemAlpha = remember { Animatable(0f) }
    val bagScaleX = remember { Animatable(1f) }
    val bagScaleY = remember { Animatable(1f) }

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
                    // Item drops from top into bag
                    itemY.snapTo(-5f)
                    itemAlpha.snapTo(1f)
                    itemY.animateTo(9f, tween(130, easing = FastOutSlowInEasing))
                    itemAlpha.snapTo(0f)
                }
                launch {
                    // Bag squash and stretch on impact
                    delay(125)
                    bagScaleX.animateTo(1.20f, tween(60, easing = FastOutSlowInEasing))
                    bagScaleY.animateTo(0.82f, tween(60, easing = FastOutSlowInEasing))
                    launch { bagScaleX.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
                    bagScaleY.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                }
            }
        } else {
            itemAlpha.snapTo(0f)
            bagScaleX.snapTo(1f)
            bagScaleY.snapTo(1f)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val s = size.width / 24f

        // Dropping Item
        if (itemAlpha.value > 0.05f) {
            drawCircle(
                color = tint.copy(alpha = itemAlpha.value),
                radius = 2.2f * s,
                center = Offset(12f * s, itemY.value * s)
            )
        }

        // Bag with squash and stretch
        scale(scaleX = bagScaleX.value, scaleY = bagScaleY.value, pivot = Offset(12f * s, 21f * s)) {
            // Handles
            val handle = Path().apply {
                moveTo(9f * s, 9f * s)
                cubicTo(9f * s, 4f * s, 15f * s, 4f * s, 15f * s, 9f * s)
            }
            drawPath(handle, tint, style = Stroke(width = 1.6f * s, cap = StrokeCap.Round))

            // Bag Body
            val bag = Path().apply {
                moveTo(6f * s, 9f * s)
                lineTo(18f * s, 9f * s)
                lineTo(19.5f * s, 21f * s)
                lineTo(4.5f * s, 21f * s)
                close()
            }
            drawPath(bag, tint, style = Stroke(width = 1.6f * s, join = StrokeJoin.Round))
        }
    }
}

/**
 * 4. Transit / Car: Looks like it is starting (rumble, exhaust smoke, rev & launch)
 */
@Composable
fun TransitCarVisual(isSelected: Boolean, trigger: Int, tint: Color) {
    val motion = LocalExperience.current.motion
    val rumbleY = remember { Animatable(0f) }
    val carX = remember { Animatable(0f) }
    val exhaustX = remember { Animatable(0f) }
    val exhaustAlpha = remember { Animatable(0f) }

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
                    // Engine vibration rumble (100ms)
                    repeat(3) {
                        rumbleY.animateTo(-1.3f, tween(16, easing = LinearEasing))
                        rumbleY.animateTo(1.3f, tween(16, easing = LinearEasing))
                    }
                    rumbleY.animateTo(0f, tween(16))
                }
                launch {
                    // Exhaust puff dots
                    exhaustX.snapTo(0f)
                    exhaustAlpha.snapTo(1f)
                    launch { exhaustX.animateTo(-4f, tween(140, easing = LinearEasing)) }
                    exhaustAlpha.animateTo(0f, tween(140))
                }
                launch {
                    // Car rev & launch forward
                    delay(90)
                    carX.animateTo(-2f, tween(60, easing = FastOutSlowInEasing))
                    carX.animateTo(6.5f, tween(110, easing = FastOutSlowInEasing))
                    carX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                }
            }
        } else {
            rumbleY.snapTo(0f)
            carX.snapTo(0f)
            exhaustAlpha.snapTo(0f)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val s = size.width / 24f

        // Exhaust smoke puffs behind car
        if (exhaustAlpha.value > 0.05f) {
            val exhaustColor = tint.copy(alpha = exhaustAlpha.value * 0.7f)
            drawCircle(exhaustColor, radius = 1.4f * s, center = Offset((3f + exhaustX.value) * s, 17f * s))
            drawCircle(exhaustColor, radius = 2.0f * s, center = Offset((1.5f + exhaustX.value * 1.3f) * s, 16f * s))
        }

        translate(left = carX.value * s, top = rumbleY.value * s) {
            // Car Body
            val carBody = Path().apply {
                moveTo(3f * s, 14f * s)
                lineTo(6.5f * s, 14f * s)
                lineTo(9f * s, 8f * s)
                lineTo(16f * s, 8f * s)
                lineTo(19f * s, 14f * s)
                lineTo(21f * s, 14f * s)
                quadraticBezierTo(21.5f * s, 17f * s, 19f * s, 17f * s)
                lineTo(3f * s, 17f * s)
                close()
            }
            drawPath(carBody, tint)

            // Wheels
            drawCircle(Color.White, radius = 2.2f * s, center = Offset(7f * s, 17.5f * s))
            drawCircle(tint, radius = 1.2f * s, center = Offset(7f * s, 17.5f * s))

            drawCircle(Color.White, radius = 2.2f * s, center = Offset(17f * s, 17.5f * s))
            drawCircle(tint, radius = 1.2f * s, center = Offset(17f * s, 17.5f * s))
        }
    }
}
