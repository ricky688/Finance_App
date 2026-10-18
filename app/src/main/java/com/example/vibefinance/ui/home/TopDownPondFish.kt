package com.example.vibefinance.ui.home

import android.os.SystemClock
import androidx.compose.runtime.withFrameNanos
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Top-down swimming koi/pond fish entity for interactive liquid water cards.
 * Supports both smooth underwater swimming and frantic out-of-water floundering.
 */
data class PondFish(
    val id: Long,
    val spawnTimeMs: Long,
    val lifetimeMs: Long = 3600L,
    val startX: Float,
    val startY: Float,
    val initialAngleRad: Float,
    val speedPxPerSec: Float,
    val turnRate: Float,
    val lengthPx: Float,
    val widthPx: Float,
    val baseColor: Color,
    val spotColor: Color = Color.Transparent,
    val wiggleFreq: Float = 14f,
    val wigglePhase: Float = Random.nextFloat() * 6.28f,
    val isFloundering: Boolean = false,
    val waterBoundaryX: Float = Float.MAX_VALUE,
    var rescued: Boolean = false,
    var rescuedTimeMs: Long = 0L,
)

/**
 * Expanding water ripple generated at tap location.
 */
data class WaterRipple(
    val id: Long,
    val spawnTimeMs: Long,
    val x: Float,
    val y: Float,
    val durationMs: Long = 900L,
    val maxRadiusPx: Float,
    val color: Color
)

/**
 * Splashing water droplet bead or dry dust particle bursting from tap location.
 */
data class WaterDroplet(
    val id: Long,
    val spawnTimeMs: Long,
    val startX: Float,
    val startY: Float,
    val vx: Float,
    val vy: Float,
    val radiusPx: Float,
    val durationMs: Long = 520L,
    val color: Color,
    val isDryDust: Boolean = false,
)

/**
 * Minimalist slate & neutral gray palette for understated, zen koi aesthetics.
 */
val MinimalGrayFishPaletteLight: List<Color> = listOf(
    Color(0xFF475569), // Slate 600 - Deep slate gray
    Color(0xFF64748B), // Slate 500 - Balanced slate gray
    Color(0xFF52525B), // Zinc 600 - Neutral stone gray
    Color(0xFF6B7280), // Cool Gray 500 - Refined mid gray
    Color(0xFF71717A), // Zinc 500 - Soft modern gray
)

val MinimalGrayFishPaletteDark: List<Color> = listOf(
    Color(0xFF94A3B8), // Slate 400 - Cool silver gray
    Color(0xFFA1A1AA), // Zinc 400 - Mist stone gray
    Color(0xFFCBD5E1), // Slate 300 - Pale slate frost
    Color(0xFF8E9BAE), // Mineral Gray
    Color(0xFF78889B), // Soft Frosted Slate
)

val MinimalFishPalette: List<Color> = MinimalGrayFishPaletteLight

val MinimalSpotPalette: List<Color> = listOf(
    Color(0x40FFFFFF), // Subtle Pearl Sheen
    Color(0x3064748B), // Soft Ink Slate
    Color(0x25334155), // Delicate Charcoal Wash
    Color(0x2894A3B8), // Silvery Dorsal Mist
)

private val fishIdGenerator = AtomicLong(1L)
private val rippleIdGenerator = AtomicLong(1L)
private val dropletIdGenerator = AtomicLong(1L)

/**
 * Top-down swimming fishes and ripple canvas overlay with tap gesture handler.
 */
@Composable
fun TopDownFishPondOverlay(
    modifier: Modifier = Modifier,
    waterRatio: Float = 1.0f,
    fishColorPalette: List<Color>? = null,
    spotPalette: List<Color> = MinimalSpotPalette,
    waterColor: Color = Color.Unspecified,
    onTapFish: ((Offset) -> Unit)? = null
) {
    val density = LocalDensity.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f || isSystemInDarkTheme()
    val activePalette = fishColorPalette ?: if (isDark) MinimalGrayFishPaletteDark else MinimalGrayFishPaletteLight
    val activeWaterColor = if (waterColor != Color.Unspecified) waterColor else if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    val fishes = remember { mutableStateListOf<PondFish>() }
    val ripples = remember { mutableStateListOf<WaterRipple>() }
    val droplets = remember { mutableStateListOf<WaterDroplet>() }
    var currentClockMs by remember { mutableStateOf(SystemClock.uptimeMillis()) }
    var cardWidthPx by remember { mutableFloatStateOf(0f) }

    // Lightweight continuous frame ticker active only when fish, ripples, or droplets exist
    LaunchedEffect(fishes.isNotEmpty() || ripples.isNotEmpty() || droplets.isNotEmpty()) {
        if (fishes.isNotEmpty() || ripples.isNotEmpty() || droplets.isNotEmpty()) {
            while (true) {
                withFrameNanos { _ ->
                    currentClockMs = SystemClock.uptimeMillis()
                }
                // Prune dead fishes, ripples, and droplets
                val now = currentClockMs
                fishes.removeAll { now - it.spawnTimeMs >= it.lifetimeMs }
                ripples.removeAll { now - it.spawnTimeMs >= it.durationMs }
                droplets.removeAll { now - it.spawnTimeMs >= it.durationMs }
                if (fishes.isEmpty() && ripples.isEmpty() && droplets.isEmpty()) break
            }
        }
    }

    val maxRadius = with(density) { 54.dp.toPx() }
    val fishLength = with(density) { 32.dp.toPx() }
    val fishWidth = with(density) { 8.5.dp.toPx() }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                cardWidthPx = size.width.toFloat()
            }
            .pointerInput(waterRatio) {
                detectTapGestures { offset ->
                    val now = SystemClock.uptimeMillis()
                    val currentWidth = if (cardWidthPx > 0f) cardWidthPx else size.width.toFloat()
                    val clampedRatio = waterRatio.coerceIn(0f, 1f)
                    val waterlineX = currentWidth * clampedRatio
                    val isWater = offset.x <= waterlineX

                    if (isWater) {
                        // 1. Add minimal gray water ripple
                        ripples.add(
                            WaterRipple(
                                id = rippleIdGenerator.incrementAndGet(),
                                spawnTimeMs = now,
                                x = offset.x,
                                y = offset.y,
                                maxRadiusPx = maxRadius,
                                color = activeWaterColor.copy(alpha = if (isDark) 0.40f else 0.35f)
                            )
                        )

                        // 2. Add 4 to 6 splashing minimal gray water droplets
                        val dropletCount = Random.nextInt(4, 7)
                        for (d in 0 until dropletCount) {
                            val dropAngle = Random.nextFloat() * (2f * PI.toFloat())
                            val dropSpeed = Random.nextFloat() * 40f + 25f
                            droplets.add(
                                WaterDroplet(
                                    id = dropletIdGenerator.incrementAndGet(),
                                    spawnTimeMs = now,
                                    startX = offset.x,
                                    startY = offset.y,
                                    vx = cos(dropAngle) * dropSpeed,
                                    vy = sin(dropAngle) * dropSpeed,
                                    radiusPx = with(density) { (Random.nextFloat() * 1.2f + 1.6f).dp.toPx() },
                                    durationMs = Random.nextLong(420L, 580L),
                                    color = activeWaterColor.copy(alpha = if (isDark) 0.85f else 0.80f),
                                    isDryDust = false
                                )
                            )
                        }

                        // 3. Spawn 1 to 3 swimming fish away from tap with minimal gray colors
                        val count = Random.nextInt(1, 4)
                        val baseAngle = Random.nextFloat() * (2f * PI.toFloat())
                        for (i in 0 until count) {
                            val angleOffset = (i - (count - 1) / 2f) * 0.7f + Random.nextFloat() * 0.3f
                            val angle = baseAngle + angleOffset
                            val baseColor = activePalette[Random.nextInt(activePalette.size)]
                            val spotColor = spotPalette[Random.nextInt(spotPalette.size)]
                            val speed = Random.nextFloat() * 50f + 70f // 70 to 120 px/s

                            fishes.add(
                                PondFish(
                                    id = fishIdGenerator.incrementAndGet(),
                                    spawnTimeMs = now + (i * 60L),
                                    startX = offset.x + cos(angle) * 8f,
                                    startY = offset.y + sin(angle) * 8f,
                                    initialAngleRad = angle,
                                    speedPxPerSec = speed,
                                    turnRate = (Random.nextFloat() - 0.5f) * 0.35f,
                                    lengthPx = fishLength * (0.85f + Random.nextFloat() * 0.3f),
                                    widthPx = fishWidth * (0.85f + Random.nextFloat() * 0.3f),
                                    baseColor = baseColor,
                                    spotColor = spotColor,
                                    isFloundering = false
                                )
                            )
                        }
                    } else {
                        // SPACE WITH NO WATER: Floundering fish out of water!
                        // 1. Dry dust particles puffing on impact (no water ripples)
                        val dustCount = Random.nextInt(3, 6)
                        for (d in 0 until dustCount) {
                            val dropAngle = Random.nextFloat() * (2f * PI.toFloat())
                            val dropSpeed = Random.nextFloat() * 25f + 15f
                            droplets.add(
                                WaterDroplet(
                                    id = dropletIdGenerator.incrementAndGet(),
                                    spawnTimeMs = now,
                                    startX = offset.x,
                                    startY = offset.y,
                                    vx = cos(dropAngle) * dropSpeed,
                                    vy = sin(dropAngle) * dropSpeed,
                                    radiusPx = with(density) { (Random.nextFloat() * 0.8f + 1.0f).dp.toPx() },
                                    durationMs = Random.nextLong(280L, 400L),
                                    color = activeWaterColor.copy(alpha = if (isDark) 0.45f else 0.35f),
                                    isDryDust = true
                                )
                            )
                        }

                        // 2. Spawn 1 or 2 floundering fish that flop desperately on dry ground
                        val count = Random.nextInt(1, 3)
                        for (i in 0 until count) {
                            val baseColor = activePalette[Random.nextInt(activePalette.size)]
                            val spotColor = spotPalette[Random.nextInt(spotPalette.size)]
                            // Face predominantly leftwards (towards water at waterlineX)
                            val dryAngle = PI.toFloat() + (Random.nextFloat() - 0.5f) * 0.9f

                            fishes.add(
                                PondFish(
                                    id = fishIdGenerator.incrementAndGet(),
                                    spawnTimeMs = now + (i * 120L),
                                    startX = offset.x + (Random.nextFloat() - 0.5f) * 10f,
                                    startY = offset.y + (Random.nextFloat() - 0.5f) * 10f,
                                    initialAngleRad = dryAngle,
                                    speedPxPerSec = 45f,
                                    turnRate = 0f,
                                    lengthPx = fishLength * (0.9f + Random.nextFloat() * 0.25f),
                                    widthPx = fishWidth * (0.9f + Random.nextFloat() * 0.25f),
                                    baseColor = baseColor,
                                    spotColor = spotColor,
                                    isFloundering = true,
                                    waterBoundaryX = waterlineX
                                )
                            )
                        }
                    }

                    // Keep total fish and droplets within reasonable cap
                    if (fishes.size > 8) {
                        val excess = fishes.size - 8
                        repeat(excess) { fishes.removeAt(0) }
                    }
                    if (droplets.size > 18) {
                        val excess = droplets.size - 18
                        repeat(excess) { droplets.removeAt(0) }
                    }

                    onTapFish?.invoke(offset)
                }
            }
    ) {
        val now = currentClockMs

        // 1. Draw ripples (minimal translucent water)
        ripples.forEach { ripple ->
            val elapsed = (now - ripple.spawnTimeMs).coerceAtLeast(0L)
            val progress = (elapsed / ripple.durationMs.toFloat()).coerceIn(0f, 1f)
            val currentRadius = ripple.maxRadiusPx * progress
            val alpha = (1f - progress) * 0.45f
            if (alpha > 0.01f) {
                drawCircle(
                    color = ripple.color.copy(alpha = alpha),
                    radius = currentRadius,
                    center = Offset(ripple.x, ripple.y),
                    style = Stroke(width = (2.2f * (1f - progress)).coerceAtLeast(0.5f))
                )
                // Secondary concentric ring
                if (progress > 0.15f) {
                    val secondaryProgress = (progress - 0.15f) / 0.85f
                    drawCircle(
                        color = ripple.color.copy(alpha = (1f - secondaryProgress) * 0.25f),
                        radius = ripple.maxRadiusPx * 0.7f * secondaryProgress,
                        center = Offset(ripple.x, ripple.y),
                        style = Stroke(width = 1.0f)
                    )
                }
            }
        }

        // 2. Draw splashing water droplets or dry dust
        droplets.forEach { droplet ->
            val elapsed = (now - droplet.spawnTimeMs).coerceAtLeast(0L)
            val progress = (elapsed / droplet.durationMs.toFloat()).coerceIn(0f, 1f)
            val tSec = elapsed / 1000f
            val currentX = droplet.startX + (droplet.vx * tSec)
            val currentY = droplet.startY + (droplet.vy * tSec) + (0.5f * (if (droplet.isDryDust) 120f else 80f) * tSec * tSec)
            val alpha = (1f - progress) * (if (droplet.isDryDust) 0.45f else if (isDark) 0.85f else 0.80f)
            val r = droplet.radiusPx * (1f - progress * (if (droplet.isDryDust) 0.5f else 0.35f))
            if (alpha > 0.01f) {
                drawCircle(
                    color = droplet.color.copy(alpha = alpha),
                    radius = r,
                    center = Offset(currentX, currentY)
                )
            }
        }

        // 3. Draw Top-down swimming or floundering fishes
        fishes.forEach { fish ->
            val elapsed = (now - fish.spawnTimeMs).coerceAtLeast(0L)
            if (elapsed in 0..fish.lifetimeMs) {
                val progress = elapsed / fish.lifetimeMs.toFloat()
                // Smooth fade-in during first 12% and fade-out in last 25%
                val alpha = when {
                    progress < 0.12f -> progress / 0.12f
                    progress > 0.75f -> (1f - progress) / 0.25f
                    else -> 1f
                }

                val tSec = elapsed / 1000f

                if (fish.isFloundering && !fish.rescued) {
                    // Floundering physics on dry ground!
                    val flopFreq = 4.2f
                    val cycle = tSec * flopFreq
                    val hopPhase = cycle % 1.0f
                    // Parabolic hop off ground
                    val hopHeight = sin(hopPhase * PI.toFloat()) * (fish.widthPx * 1.5f)
                    // Desperate flops kick the fish toward water (leftwards)
                    val kickSpeed = 48f
                    val currentX = fish.startX - (kickSpeed * tSec)
                    val currentY = fish.startY + sin(cycle * 0.85f) * (fish.widthPx * 0.45f) - hopHeight
                    val flopAngle = fish.initialAngleRad + sin(cycle * PI.toFloat()) * 0.65f

                    // If fish reached the water line, rescue it!
                    if (currentX <= fish.waterBoundaryX) {
                        fish.rescued = true
                        fish.rescuedTimeMs = now
                        // Splash when hitting water!
                        ripples.add(
                            WaterRipple(
                                id = rippleIdGenerator.incrementAndGet(),
                                spawnTimeMs = now,
                                x = currentX,
                                y = currentY,
                                maxRadiusPx = maxRadius * 0.75f,
                                color = activeWaterColor.copy(alpha = if (isDark) 0.45f else 0.40f)
                            )
                        )
                        for (d in 0 until 4) {
                            val dropAngle = Random.nextFloat() * (2f * PI.toFloat())
                            val dropSpeed = Random.nextFloat() * 30f + 20f
                            droplets.add(
                                WaterDroplet(
                                    id = dropletIdGenerator.incrementAndGet(),
                                    spawnTimeMs = now,
                                    startX = currentX,
                                    startY = currentY,
                                    vx = cos(dropAngle) * dropSpeed,
                                    vy = sin(dropAngle) * dropSpeed,
                                    radiusPx = with(density) { 1.5.dp.toPx() },
                                    durationMs = 450L,
                                    color = activeWaterColor.copy(alpha = 0.85f),
                                    isDryDust = false
                                )
                            )
                        }
                    }

                    drawTopDownFish(
                        fish = fish,
                        center = Offset(currentX, currentY),
                        angleRad = flopAngle,
                        tSec = tSec,
                        alpha = alpha,
                        isDark = isDark,
                        isFloundering = true,
                        hopPhase = hopPhase
                    )
                } else if (fish.rescued) {
                    // Rescued fish smoothly swims deep into water
                    val swimSec = (now - fish.rescuedTimeMs).coerceAtLeast(0L) / 1000f
                    val currentX = fish.waterBoundaryX - 6f - (fish.speedPxPerSec * swimSec)
                    val currentY = fish.startY + sin(swimSec * 2.2f) * 12f
                    drawTopDownFish(
                        fish = fish,
                        center = Offset(currentX, currentY),
                        angleRad = PI.toFloat() + sin(swimSec * 1.5f) * 0.2f,
                        tSec = swimSec,
                        alpha = alpha,
                        isDark = isDark,
                        isFloundering = false
                    )
                } else {
                    // Normal swimming fish
                    val currentAngle = fish.initialAngleRad + (fish.turnRate * tSec)
                    val currentX = fish.startX + cos(currentAngle) * (fish.speedPxPerSec * tSec)
                    val currentY = fish.startY + sin(currentAngle) * (fish.speedPxPerSec * tSec)

                    drawTopDownFish(
                        fish = fish,
                        center = Offset(currentX, currentY),
                        angleRad = currentAngle,
                        tSec = tSec,
                        alpha = alpha,
                        isDark = isDark,
                        isFloundering = false
                    )
                }
            }
        }
    }
}

/**
 * Renders a single top-down fish with a pure minimalist fluid vector silhouette.
 * Eliminates fussy anatomical details (eyes, spots, bulky fins) in favor of a sleek,
 * aerodynamic hydrodynamic form with an ethereal translucent gradient and gentle motion.
 */
private fun DrawScope.drawTopDownFish(
    fish: PondFish,
    center: Offset,
    angleRad: Float,
    tSec: Float,
    alpha: Float,
    isDark: Boolean = false,
    isFloundering: Boolean = false,
    hopPhase: Float = 0f
) {
    val degrees = Math.toDegrees(angleRad.toDouble()).toFloat()
    val L = fish.lengthPx
    val W = fish.widthPx

    val spineSway: Float
    val tailSway: Float
    val finFlutter: Float

    if (isFloundering) {
        // Dramatic C-curve arching and frantic tail whip on dry land
        val archBend = sin(hopPhase * PI.toFloat()) * (W * 0.95f)
        val thrash = sin(tSec * 32f + fish.wigglePhase)
        spineSway = archBend + thrash * (W * 0.40f)
        tailSway = -archBend * 1.4f + sin(tSec * 32f + fish.wigglePhase - 1.2f) * (W * 1.35f)
        finFlutter = cos(tSec * 36f) * 0.28f
    } else {
        val phase = fish.wigglePhase + (tSec * fish.wiggleFreq)
        spineSway = sin(phase) * (W * 0.22f)
        tailSway = sin(phase - 0.75f) * (W * 0.58f)
        finFlutter = cos(phase * 0.8f) * 0.08f
    }

    rotate(degrees = degrees, pivot = center) {
        val cx = center.x
        val cy = center.y

        val noseX = cx + L * 0.50f
        val noseY = cy
        val tailRootX = cx - L * 0.44f
        val tailRootY = cy + spineSway
        val tailTipX = cx - L * 0.76f
        val tailTipY = cy + tailSway

        // A. Minimalist Fluid Pectoral Fins (Slender swept aerodynamic winglets)
        val leftFinPath = Path().apply {
            moveTo(cx + L * 0.14f, cy - W * 0.38f)
            quadraticTo(
                cx + L * 0.04f, cy - W * (0.82f + finFlutter),
                cx - L * 0.12f, cy - W * (0.72f + finFlutter)
            )
            quadraticTo(
                cx - L * 0.04f, cy - W * 0.42f,
                cx + L * 0.04f, cy - W * 0.34f
            )
            close()
        }
        val rightFinPath = Path().apply {
            moveTo(cx + L * 0.14f, cy + W * 0.38f)
            quadraticTo(
                cx + L * 0.04f, cy + W * (0.82f + finFlutter),
                cx - L * 0.12f, cy + W * (0.72f + finFlutter)
            )
            quadraticTo(
                cx - L * 0.04f, cy + W * 0.42f,
                cx + L * 0.04f, cy + W * 0.34f
            )
            close()
        }
        val finColor = fish.baseColor.copy(alpha = alpha * (if (isFloundering) 0.48f else 0.36f))
        drawPath(leftFinPath, color = finColor)
        drawPath(rightFinPath, color = finColor)

        // B. Minimalist Crescent Caudal Fin (Streamlined sweeping tail)
        val tailPath = Path().apply {
            moveTo(tailRootX, tailRootY)
            // Upper swept curve
            cubicTo(
                tailRootX - L * 0.10f, tailRootY - W * 0.20f,
                tailTipX + L * 0.06f, tailTipY - W * 0.44f,
                tailTipX, tailTipY - W * 0.44f
            )
            // Inward elegant notch
            quadraticTo(
                tailTipX + L * 0.10f, tailTipY,
                tailTipX, tailTipY + W * 0.44f
            )
            // Lower swept curve back to root
            cubicTo(
                tailTipX + L * 0.06f, tailTipY + W * 0.44f,
                tailRootX - L * 0.10f, tailRootY + W * 0.20f,
                tailRootX, tailRootY
            )
            close()
        }
        drawPath(
            tailPath,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    fish.baseColor.copy(alpha = alpha * 0.70f),
                    fish.baseColor.copy(alpha = alpha * 0.18f)
                ),
                startX = tailRootX,
                endX = tailTipX
            )
        )

        // C. Minimalist Hydrodynamic Spindle Body (Pure continuous fluid silhouette)
        val bodyPath = Path().apply {
            moveTo(noseX, noseY)
            // Smooth top flank from nose to tail root
            cubicTo(
                cx + L * 0.36f, cy - W * 0.48f,
                cx + L * 0.10f, cy - W * 0.46f,
                cx - L * 0.12f, cy - W * 0.30f + spineSway * 0.35f
            )
            quadraticTo(
                cx - L * 0.28f, cy - W * 0.12f + spineSway * 0.75f,
                tailRootX, tailRootY
            )
            // Smooth bottom flank from tail root back to nose
            quadraticTo(
                cx - L * 0.28f, cy + W * 0.12f + spineSway * 0.75f,
                cx - L * 0.12f, cy + W * 0.30f + spineSway * 0.35f
            )
            cubicTo(
                cx + L * 0.10f, cy + W * 0.46f,
                cx + L * 0.36f, cy + W * 0.48f,
                noseX, noseY
            )
            close()
        }

        // Pure fluid gradient from dense head to translucent tail
        val bodyBrush = Brush.linearGradient(
            colors = listOf(
                fish.baseColor.copy(alpha = alpha * 0.92f),
                fish.baseColor.copy(alpha = alpha * 0.78f),
                fish.baseColor.copy(alpha = alpha * 0.52f)
            ),
            start = Offset(noseX, noseY),
            end = Offset(tailRootX, tailRootY)
        )
        drawPath(bodyPath, brush = bodyBrush)

        // Hairline micro-outline for razor-sharp definition against wave ripples
        val outlineColor = if (isDark) {
            Color(0x35FFFFFF).copy(alpha = alpha * 0.18f)
        } else {
            Color(0x250F172A).copy(alpha = alpha * 0.22f)
        }
        drawPath(
            bodyPath,
            color = outlineColor,
            style = Stroke(width = 0.55f)
        )
    }
}

