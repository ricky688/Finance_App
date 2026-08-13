package com.example.vibefinance.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibefinance.ui.common.bouncyClickable
import kotlin.math.cos
import kotlin.math.sin

enum class IndicatorShape {
    FLAT,
    WAVY
}

/**
 * Material 3 Expressive Circular Progress Indicator for Remaining Days Widget.
 * Formatted according to official M3 Expressive Specification, dynamically scaled based on container size:
 * - Base Spec Reference Diameter: 48.dp
 * - Stroke Width: 4.dp * scaleFactor
 * - Stop / Gap Spacing: 4.dp * scaleFactor
 * - Wave Amplitude: 1.6.dp * scaleFactor
 * - Wave Wavelength: 15.dp * scaleFactor
 * - Supports M3 Expressive Flat and Wavy shapes with real-time morphing spring animations
 * - Interactive tap gesture to toggle between Flat and Wavy shapes
 */
@Composable
fun DaysLeftCard(
    modifier: Modifier = Modifier,
    daysLeft: Int,
    totalDays: Int
) {
    var indicatorShape by remember { mutableStateOf(IndicatorShape.WAVY) }

    // M3 Spec Base: Wave Amplitude = 1.6dp
    val targetAmplitude = if (indicatorShape == IndicatorShape.WAVY) 1.6.dp else 0.dp
    val animatedAmplitudeDp by animateDpAsState(
        targetValue = targetAmplitude,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "wavyAmplitude"
    )

    // Percentage = Days Left / Total Period Days
    val progressTarget = if (totalDays > 0) (daysLeft.toFloat() / totalDays).coerceIn(0f, 1f) else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "daysLeftProgress"
    )

    // Continuous smooth subtle flow animation for the wave phase
    val infiniteTransition = rememberInfiniteTransition(label = "wavePhase")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val localDensity = LocalDensity.current
    var heightDp by remember { mutableStateOf(0.dp) }

    val activeColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)

    Card(
        modifier = modifier
            .fillMaxHeight()
            .bouncyClickable {
                // Toggle between Flat and Wavy M3 Expressive shapes on tap
                indicatorShape = if (indicatorShape == IndicatorShape.FLAT) IndicatorShape.WAVY else IndicatorShape.FLAT
            }
            .onGloballyPositioned {
                heightDp = with(localDensity) { it.size.height.toDp() }
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            val circleDiameter = if (heightDp > 24.dp) (heightDp - 28.dp) else 116.dp

            Box(
                modifier = Modifier.size(circleDiameter),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // --- M3 EXPRESSIVE SPECIFICATION SCALED DYNAMICALLY BY CONTAINER SCALE ---
                    val baseSpecDiameterPx = 48.dp.toPx()
                    val scaleFactor = (size.minDimension / baseSpecDiameterPx).coerceAtLeast(1.0f)

                    val strokeWidthPx = 4.dp.toPx() * scaleFactor        // M3 Spec: 4dp stroke width (scaled)
                    val amplitudePx = animatedAmplitudeDp.toPx() * scaleFactor // M3 Spec: 1.6dp wave amplitude (scaled)
                    val maxAmplitudePx = 1.6.dp.toPx() * scaleFactor
                    val gapPx = 4.dp.toPx() * scaleFactor               // M3 Spec: 4dp gap spacing (scaled)
                    val wavelengthPx = 15.dp.toPx() * scaleFactor       // M3 Spec: 15dp wavelength cycle (scaled)

                    // Fixed base radius shared 100% identically by both track arc and active indicator arc
                    val baseRadius = (size.minDimension - strokeWidthPx - (maxAmplitudePx * 2f)) / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)

                    val topLeft = Offset(center.x - baseRadius, center.y - baseRadius)
                    val arcSize = Size(baseRadius * 2f, baseRadius * 2f)

                    // Calculate precise gap angle in degrees corresponding to scaled 4dp gap
                    val gapAngleDeg = Math.toDegrees((gapPx / baseRadius).toDouble()).toFloat().coerceAtLeast(4f)
                    val startAngleDeg = -90f
                    val sweepAngleDeg = (animatedProgress * 360f).coerceIn(0f, 360f)

                    // 1. Background Track Arc (Shared baseRadius & explicit topLeft/size)
                    if (sweepAngleDeg < 360f) {
                        val trackStartAngle = startAngleDeg + sweepAngleDeg + gapAngleDeg
                        val trackSweepAngle = (360f - sweepAngleDeg - (gapAngleDeg * 2)).coerceAtLeast(0f)

                        if (trackSweepAngle > 0f) {
                            drawArc(
                                color = trackColor,
                                startAngle = trackStartAngle,
                                sweepAngle = trackSweepAngle,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                            )
                        }
                    }

                    // 2. Active M3 Progress Arc (100% Concentrically Aligned)
                    if (sweepAngleDeg > 0f) {
                        if (amplitudePx <= 0.05f) {
                            // Perfect Flat Arc aligned on the exact same topLeft & arcSize as track
                            drawArc(
                                color = activeColor,
                                startAngle = startAngleDeg,
                                sweepAngle = sweepAngleDeg,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                            )
                        } else {
                            // Sinusoidal Wavy Arc: Scaled Wavelength (waveFrequency = 2*PI*R / wavelengthPx)
                            val circumference = 2.0 * Math.PI * baseRadius
                            val waveFrequency = (circumference / wavelengthPx).coerceAtLeast(4.0)

                            val startRad = Math.toRadians(startAngleDeg.toDouble())
                            val sweepRad = Math.toRadians(sweepAngleDeg.toDouble())
                            val stepCount = (sweepAngleDeg * 3).toInt().coerceAtLeast(30)
                            val stepRad = sweepRad / stepCount

                            val path = Path()
                            for (i in 0..stepCount) {
                                val currentAngleRad = startRad + (i * stepRad)
                                val waveVal = sin(currentAngleRad * waveFrequency + wavePhase)
                                val currentRadius = baseRadius + (amplitudePx * waveVal.toFloat())

                                val x = center.x + (currentRadius * cos(currentAngleRad).toFloat())
                                val y = center.y + (currentRadius * sin(currentAngleRad).toFloat())

                                if (i == 0) {
                                    path.moveTo(x, y)
                                } else {
                                    path.lineTo(x, y)
                                }
                            }

                            drawPath(
                                path = path,
                                color = activeColor,
                                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                            )
                        }
                    }
                }

                // Center Text: Days Left, Label & M3 Shape Mode Pill
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val circleVal = circleDiameter.value
                    Text(
                        text = daysLeft.toString(),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = (circleVal * 0.28f).coerceIn(18f, 38f).sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "剩餘天數",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = (circleVal * 0.095f).coerceIn(9f, 13f).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Surface(
                        shape = CircleShape,
                        color = activeColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = if (indicatorShape == IndicatorShape.WAVY) "Wavy" else "Flat",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = (circleVal * 0.075f).coerceIn(7f, 10f).sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = activeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}
