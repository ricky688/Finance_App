package com.example.vibefinance.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Caller-owned amplitude lets a tap reverse an in-flight morph immediately. Phase and amplitude
 * are read only while drawing; cached angular samples and one reusable path bound the frame work.
 */
@Composable
internal fun BudgetWaveProgressIndicator(
    progress: () -> Float,
    amplitude: () -> Float,
    phase: () -> Float,
    color: Color,
    trackColor: Color,
    stroke: Stroke,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.drawWithCache {
            val maxAmplitude = 1.6.dp.toPx()
            val radius = (size.minDimension / 2f - stroke.width / 2f - maxAmplitude)
                .coerceAtLeast(0f)
            val center = Offset(size.width / 2f, size.height / 2f)
            val circleTopLeft = Offset(center.x - radius, center.y - radius)
            val circleSize = Size(radius * 2f, radius * 2f)
            val waveCount = (2f * PI.toFloat() * radius / 15.dp.toPx())
                .roundToInt().coerceAtLeast(5)
            val sampleCount = waveCount * 16
            val cosAngles = FloatArray(sampleCount + 1)
            val sinAngles = FloatArray(sampleCount + 1)
            val cosWaves = FloatArray(sampleCount + 1)
            val sinWaves = FloatArray(sampleCount + 1)
            for (index in 0..sampleCount) {
                val angle = index.toDouble() / sampleCount * 2.0 * PI - PI / 2.0
                cosAngles[index] = cos(angle).toFloat()
                sinAngles[index] = sin(angle).toFloat()
                cosWaves[index] = cos(angle * waveCount).toFloat()
                sinWaves[index] = sin(angle * waveCount).toFloat()
            }
            val activePath = Path()
            // Account for rounded caps as well as Material's visible 4 dp endpoint gap.
            val gapDegrees = if (radius > 0f) {
                (4.dp.toPx() + stroke.width) / radius * 180f / PI.toFloat()
            } else 0f

            onDrawBehind {
                val fraction = progress().coerceIn(0f, 1f)
                val sweep = fraction * 360f
                val waveAmplitude = amplitude().coerceIn(0f, 1f) * maxAmplitude

                if (fraction == 0f) {
                    drawCircle(trackColor, radius, center, style = stroke)
                } else if (fraction < 1f) {
                    val gap = gapDegrees.coerceAtMost((360f - sweep) / 2f)
                    drawArc(
                        color = trackColor,
                        startAngle = -90f + sweep + gap,
                        sweepAngle = (360f - sweep - 2f * gap).coerceAtLeast(0f),
                        useCenter = false,
                        topLeft = circleTopLeft,
                        size = circleSize,
                        style = stroke,
                    )
                }

                if (fraction > 0f) {
                    if (waveAmplitude == 0f) {
                        if (fraction == 1f) {
                            drawCircle(color, radius, center, style = stroke)
                        } else {
                            drawArc(color, -90f, sweep, false, circleTopLeft, circleSize, style = stroke)
                        }
                    } else {
                        val phaseAngle = phase() * 2f * PI.toFloat()
                        val phaseSin = sin(phaseAngle)
                        val phaseCos = cos(phaseAngle)
                        val lastWholeSample = (fraction * sampleCount).toInt()
                        activePath.rewind()
                        for (index in 0..lastWholeSample) {
                            val r = radius + waveAmplitude *
                                (sinWaves[index] * phaseCos - cosWaves[index] * phaseSin)
                            val x = center.x + cosAngles[index] * r
                            val y = center.y + sinAngles[index] * r
                            if (index == 0) activePath.moveTo(x, y) else activePath.lineTo(x, y)
                        }
                        if (fraction < 1f) {
                            val endAngle = fraction * 2f * PI.toFloat() - PI.toFloat() / 2f
                            val r = radius + waveAmplitude * sin(endAngle * waveCount - phaseAngle)
                            activePath.lineTo(center.x + cos(endAngle) * r, center.y + sin(endAngle) * r)
                        } else {
                            activePath.close()
                        }
                        drawPath(activePath, color, style = stroke)
                    }
                }
            }
        }
    )
}
