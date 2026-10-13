package com.example.vibefinance.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Particle data for the Flying Cash Easter Egg with independent wing-flapping animation.
 */
data class FlyingCashParticle(
    val id: Int,
    val isWingedMoney: Boolean,
    val startXRatio: Float,
    val delayRatio: Float,
    val durationRatio: Float,
    val scale: Float,
    val swayAmplitudeDp: Float,
    val swayFrequency: Float,
    val baseRotation: Float,
    val flapFrequencyHz: Float,
    val flapPhaseOffset: Float
)

/**
 * Calculates current flapping angle in degrees based on time and frequency.
 */
fun calculateFlapAngleDeg(
    elapsedSeconds: Float,
    flapFrequencyHz: Float,
    flapPhaseOffset: Float,
    maxFlapDegrees: Float = 36f
): Float {
    val phase = (elapsedSeconds * flapFrequencyHz * 2 * PI.toFloat()) + flapPhaseOffset
    return sin(phase) * maxFlapDegrees
}

/**
 * Generates a randomized flock of floating money particles.
 */
fun generateFlyingCashParticles(count: Int = 22, seed: Long = System.currentTimeMillis()): List<FlyingCashParticle> {
    val random = Random(seed)
    return List(count) { id ->
        val isWinged = random.nextFloat() < 0.82f
        FlyingCashParticle(
            id = id,
            isWingedMoney = isWinged,
            startXRatio = 0.08f + random.nextFloat() * 0.84f,
            delayRatio = random.nextFloat() * 0.28f,
            durationRatio = 0.68f + random.nextFloat() * 0.24f,
            scale = if (isWinged) 0.95f + random.nextFloat() * 0.45f else 0.8f + random.nextFloat() * 0.35f,
            swayAmplitudeDp = 16f + random.nextFloat() * 26f,
            swayFrequency = 1.2f + random.nextFloat() * 1.0f,
            baseRotation = -12f + random.nextFloat() * 24f,
            flapFrequencyHz = 4.2f + random.nextFloat() * 2.2f, // 4.2 to 6.4 flaps per second
            flapPhaseOffset = random.nextFloat() * (2 * PI.toFloat())
        )
    }
}

/**
 * Controller to track multi-click trigger state for the app icon easter egg.
 */
class EasterEggTriggerDetector(
    private val requiredTaps: Int = 5,
    private val timeoutMillis: Long = 1800L
) {
    var tapCount: Int = 0
        private set
    private var lastTapUptime: Long = 0L

    fun registerTap(uptimeMillis: Long): Boolean {
        tapCount = if (uptimeMillis - lastTapUptime < timeoutMillis) {
            tapCount + 1
        } else {
            1
        }
        lastTapUptime = uptimeMillis

        return if (tapCount >= requiredTaps) {
            tapCount = 0
            true
        } else {
            false
        }
    }

    fun reset() {
        tapCount = 0
        lastTapUptime = 0L
    }
}

/**
 * Fullscreen non-blocking overlay that renders flying cash floating slowly upwards
 * with realistic flapping wings attached firmly to each banknote.
 */
@Composable
fun FlyingCashOverlay(
    triggerKey: Long,
    modifier: Modifier = Modifier,
    onCompleted: () -> Unit = {}
) {
    if (triggerKey <= 0L) return

    val motionLevel = com.example.vibefinance.ui.preferences.LocalExperience.current.motion
    if (motionLevel == com.example.vibefinance.ui.preferences.MotionLevel.MINIMAL) {
        LaunchedEffect(triggerKey) {
            onCompleted()
        }
        return
    }

    val density = LocalDensity.current

    val particles = remember(triggerKey) {
        val count = if (motionLevel == com.example.vibefinance.ui.preferences.MotionLevel.REDUCED) 12 else 24
        generateFlyingCashParticles(count = count, seed = triggerKey)
    }

    val progress = remember(triggerKey) { Animatable(0f) }

    LaunchedEffect(triggerKey) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 3600, easing = LinearEasing)
        )
        onCompleted()
    }

    // Pre-create feather wing paths anchored at (0, 0) joint
    val leftWingPath = remember(density) {
        with(density) {
            Path().apply {
                moveTo(0f, 0f)
                cubicTo(-6.dp.toPx(), -9.dp.toPx(), -16.dp.toPx(), -18.dp.toPx(), -26.dp.toPx(), -14.dp.toPx())
                cubicTo(-23.dp.toPx(), -8.dp.toPx(), -21.dp.toPx(), -3.dp.toPx(), -22.dp.toPx(), 0f)
                cubicTo(-18.dp.toPx(), 2.dp.toPx(), -15.dp.toPx(), 6.dp.toPx(), -15.dp.toPx(), 9.dp.toPx())
                cubicTo(-10.dp.toPx(), 9.dp.toPx(), -5.dp.toPx(), 6.dp.toPx(), 0f, 0f)
                close()
            }
        }
    }

    val rightWingPath = remember(density) {
        with(density) {
            Path().apply {
                moveTo(0f, 0f)
                cubicTo(6.dp.toPx(), -9.dp.toPx(), 16.dp.toPx(), -18.dp.toPx(), 26.dp.toPx(), -14.dp.toPx())
                cubicTo(23.dp.toPx(), -8.dp.toPx(), 21.dp.toPx(), -3.dp.toPx(), 22.dp.toPx(), 0f)
                cubicTo(18.dp.toPx(), 2.dp.toPx(), 15.dp.toPx(), 6.dp.toPx(), 15.dp.toPx(), 9.dp.toPx())
                cubicTo(10.dp.toPx(), 9.dp.toPx(), 5.dp.toPx(), 6.dp.toPx(), 0f, 0f)
                close()
            }
        }
    }

    // Sparkle star path
    val sparklePath = remember(density) {
        with(density) {
            val s = 11.dp.toPx()
            Path().apply {
                moveTo(0f, -s)
                quadraticTo(0f, 0f, s, 0f)
                quadraticTo(0f, 0f, 0f, s)
                quadraticTo(0f, 0f, -s, 0f)
                quadraticTo(0f, 0f, 0f, -s)
                close()
            }
        }
    }

    val billGradient = remember {
        Brush.linearGradient(
            colors = listOf(Color(0xFF2E7D32), Color(0xFF4CAF50), Color(0xFF2E7D32))
        )
    }
    val billBorderColor = remember { Color(0xFF1B5E20) }
    val billInnerFrameColor = remember { Color(0xFFA5D6A7) }
    val emblemBgColor = remember { Color(0xFFE8F5E9) }
    val wingFillBrush = remember {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFFFFFFF), Color(0xFFF1F5F9))
        )
    }
    val wingOutlineColor = remember { Color(0xFF94A3B8) }
    val sparkleColor = remember { Color(0xFFFFD54F) }

    val billW = with(density) { 36.dp.toPx() }
    val billH = with(density) { 22.dp.toPx() }
    val wingBaseX = with(density) { 12.dp.toPx() }
    val wingBaseY = with(density) { 3.dp.toPx() }
    val cornerRadiusPx = with(density) { 4.dp.toPx() }
    val emblemRadiusPx = with(density) { 5.5.dp.toPx() }
    val strokeWidthPx = with(density) { 1.2.dp.toPx() }
    val textSizePx = with(density) { 9.sp.toPx() }

    val textPaint = remember(density) {
        android.graphics.Paint().apply {
            color = android.graphics.Color.argb(255, 27, 94, 32)
            textSize = textSizePx
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("FlyingCashOverlay")
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val bottomY = heightPx + with(density) { 50.dp.toPx() }
        val topY = with(density) { -80.dp.toPx() }

        val currentProgress = progress.value
        val elapsedSeconds = currentProgress * 3.6f

        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { particle ->
                val p = currentProgress
                val activeP = ((p - particle.delayRatio) / particle.durationRatio).coerceIn(0f, 1f)

                if (p >= particle.delayRatio && p <= (particle.delayRatio + particle.durationRatio)) {
                    val swayPx = with(density) { particle.swayAmplitudeDp.dp.toPx() }
                    val y = bottomY + (topY - bottomY) * activeP
                    val sway = sin(activeP * particle.swayFrequency * 2 * PI.toFloat()) * swayPx
                    val x = (particle.startXRatio * widthPx) + sway

                    val alpha = when {
                        activeP < 0.12f -> (activeP / 0.12f).coerceIn(0f, 1f)
                        activeP > 0.85f -> ((1f - activeP) / 0.15f).coerceIn(0f, 1f)
                        else -> 1f
                    }

                    if (particle.isWingedMoney) {
                        // Flapping angle & aerodynamic bobbing
                        val flapAngle = calculateFlapAngleDeg(
                            elapsedSeconds = elapsedSeconds,
                            flapFrequencyHz = particle.flapFrequencyHz,
                            flapPhaseOffset = particle.flapPhaseOffset,
                            maxFlapDegrees = 38f
                        )
                        val flapPhase = (elapsedSeconds * particle.flapFrequencyHz * 2 * PI.toFloat()) + particle.flapPhaseOffset
                        val bodyBobY = -sin(flapPhase) * with(density) { 3.dp.toPx() } * particle.scale
                        val wingScaleY = 0.82f + 0.18f * cos(flapPhase)
                        val totalRotation = particle.baseRotation + 10f * cos(activeP * particle.swayFrequency * 2 * PI.toFloat())

                        withTransform({
                            translate(x, y + bodyBobY)
                            rotate(totalRotation, pivot = Offset.Zero)
                            scale(particle.scale, particle.scale, pivot = Offset.Zero)
                        }) {
                            // 1. Left Wing (rotates around shoulder joint at (-wingBaseX, -wingBaseY))
                            withTransform({
                                translate(-wingBaseX, -wingBaseY)
                                rotate(flapAngle, pivot = Offset.Zero)
                                scale(1f, wingScaleY, pivot = Offset.Zero)
                            }) {
                                drawPath(leftWingPath, wingFillBrush, alpha = alpha * 0.95f)
                                drawPath(leftWingPath, wingOutlineColor, style = Stroke(width = strokeWidthPx), alpha = alpha * 0.85f)
                            }

                            // 2. Right Wing (rotates around shoulder joint at (wingBaseX, -wingBaseY))
                            withTransform({
                                translate(wingBaseX, -wingBaseY)
                                rotate(-flapAngle, pivot = Offset.Zero)
                                scale(1f, wingScaleY, pivot = Offset.Zero)
                            }) {
                                drawPath(rightWingPath, wingFillBrush, alpha = alpha * 0.95f)
                                drawPath(rightWingPath, wingOutlineColor, style = Stroke(width = strokeWidthPx), alpha = alpha * 0.85f)
                            }

                            // 3. Banknote Body in center
                            drawRoundRect(
                                brush = billGradient,
                                topLeft = Offset(-billW / 2, -billH / 2),
                                size = Size(billW, billH),
                                cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                                alpha = alpha
                            )

                            // Border
                            drawRoundRect(
                                color = billBorderColor,
                                topLeft = Offset(-billW / 2, -billH / 2),
                                size = Size(billW, billH),
                                cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                                style = Stroke(width = strokeWidthPx),
                                alpha = alpha
                            )

                            // Inner Frame Line
                            val framePadding = with(density) { 2.5.dp.toPx() }
                            drawRoundRect(
                                color = billInnerFrameColor,
                                topLeft = Offset(-billW / 2 + framePadding, -billH / 2 + framePadding),
                                size = Size(billW - framePadding * 2, billH - framePadding * 2),
                                cornerRadius = CornerRadius(cornerRadiusPx - 1f, cornerRadiusPx - 1f),
                                style = Stroke(width = strokeWidthPx * 0.6f),
                                alpha = alpha * 0.65f
                            )

                            // Center Circle Emblem
                            drawCircle(
                                color = emblemBgColor,
                                radius = emblemRadiusPx,
                                center = Offset.Zero,
                                alpha = alpha * 0.95f
                            )
                            drawCircle(
                                color = billBorderColor,
                                radius = emblemRadiusPx,
                                center = Offset.Zero,
                                style = Stroke(width = strokeWidthPx * 0.7f),
                                alpha = alpha
                            )

                            // Center "$" text mark
                            textPaint.alpha = (alpha * 255).toInt().coerceIn(0, 255)
                            drawContext.canvas.nativeCanvas.drawText(
                                "$",
                                0f,
                                with(density) { 3.2.dp.toPx() },
                                textPaint
                            )
                        }
                    } else {
                        // Sparkle / Magic Star
                        val sparkleRotation = (elapsedSeconds * 90f + particle.id * 30f) % 360f
                        withTransform({
                            translate(x, y)
                            rotate(sparkleRotation, pivot = Offset.Zero)
                            scale(particle.scale, particle.scale, pivot = Offset.Zero)
                        }) {
                            drawPath(sparklePath, sparkleColor, alpha = alpha * 0.9f)
                        }
                    }
                }
            }
        }
    }
}
