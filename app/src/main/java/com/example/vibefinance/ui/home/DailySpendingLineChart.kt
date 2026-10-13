@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.home

import com.example.vibefinance.ui.components.connectedSpatialMotion
import com.example.vibefinance.ui.components.rememberCompletePressProgress

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.vibefinance.ui.preferences.PrivacyText as Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibefinance.R
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.components.rememberConnectedButtonColorMotion
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

enum class DailyChartMode {
    CUMULATIVE,
    DAILY
}

@Composable
fun DailySpendingLineChart(
    state: FinanceUiState,
    modifier: Modifier = Modifier
) {
    // 1. Gather daily spending for the last 7 days including today
    val today = LocalDate.now()
    val last7Days = remember {
        (0..6).map { today.minusDays(it.toLong()) }.reversed()
    }

    val dailySpending = remember(state.transactions, last7Days) {
        val map = mutableMapOf<LocalDate, Double>()
        last7Days.forEach { map[it] = 0.0 }
        
        state.transactions.forEach { tx ->
            if (tx.toAccountId == null && !tx.isExcludedFromDailyBudget && tx.amount > 0) {
                val date = Instant.ofEpochMilli(tx.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
                if (map.containsKey(date)) {
                    map[date] = (map[date] ?: 0.0) + tx.amount
                }
            }
        }
        last7Days.map { date ->
            val spending = map[date] ?: 0.0
            date to spending
        }
    }

    val cumulativeSpending = remember(dailySpending) {
        var cumSum = 0.0
        dailySpending.map { (date, spending) ->
            cumSum += spending
            date to cumSum
        }
    }

    // Chart Mode State: Cumulative vs Single-Day spending
    var chartMode by remember { mutableStateOf(DailyChartMode.CUMULATIVE) }

    val activeDataset = if (chartMode == DailyChartMode.CUMULATIVE) cumulativeSpending else dailySpending

    val maxSpending = remember(activeDataset) {
        val maxVal = activeDataset.maxOfOrNull { it.second } ?: 0.0
        if (maxVal < 10.0) 50.0 else maxVal
    }

    val motion = com.example.vibefinance.ui.preferences.LocalExperience.current.motion
    val chartMotionSpec = when (motion) {
        com.example.vibefinance.ui.preferences.MotionLevel.FULL -> MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
        com.example.vibefinance.ui.preferences.MotionLevel.REDUCED -> MaterialTheme.motionScheme.fastEffectsSpec<Float>()
        com.example.vibefinance.ui.preferences.MotionLevel.MINIMAL -> snap<Float>()
    }
    val detailMotionSpec = if (motion == com.example.vibefinance.ui.preferences.MotionLevel.MINIMAL)
        snap<Float>() else MaterialTheme.motionScheme.fastEffectsSpec<Float>()
    // Read the point states in Canvas so curve morphing invalidates drawing rather than the whole card.
    val pointRatios = activeDataset.mapIndexed { index, (_, spending) ->
        animateFloatAsState(
            targetValue = (spending / maxSpending).toFloat().coerceIn(0f, 1f),
            animationSpec = chartMotionSpec,
            label = "spendingTrendPoint_$index",
        )
    }
    val dailyDetailAlpha = animateFloatAsState(
        targetValue = if (chartMode == DailyChartMode.DAILY) 1f else 0f,
        animationSpec = detailMotionSpec,
        label = "spendingTrendDailyDetails",
    )

    val avgDailySpending = remember(dailySpending) {
        val sum = dailySpending.sumOf { it.second }
        if (dailySpending.isNotEmpty()) sum / dailySpending.size else 0.0
    }

    val averageRatio = animateFloatAsState(
        targetValue = (avgDailySpending / maxSpending).toFloat().coerceIn(0f, 1f),
        animationSpec = chartMotionSpec,
        label = "spendingTrendAverage",
    )

    // 2. Interactive Touch/Scrub state
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var isDragging by remember { mutableStateOf(false) }

    val activeIndex = selectedIndex ?: 6 // default to last item (today)
    val activeItem = cumulativeSpending.getOrNull(activeIndex)
    val activeDailyItem = dailySpending.getOrNull(activeIndex)

    val haptic = LocalHapticFeedback.current
    var previousIdx by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(selectedIndex) {
        if (selectedIndex != previousIdx && selectedIndex != null) {
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
            previousIdx = selectedIndex
        }
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row: Title & Subtitle on left, M3 Expressive Mode Switch on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = stringResource(R.string.chart_spending_trend),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.chart_last_7_days),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                var pressedIndex by remember { mutableStateOf<Int?>(null) }
                // Material 3 Expressive Mode Switch Pill
                val colors = MaterialTheme.colorScheme
                val trackColor = colors.surfaceContainerHigh.copy(alpha = 0.65f)
                val trackBackdrop = trackColor.compositeOver(
                    colors.surfaceVariant.copy(alpha = 0.4f).compositeOver(colors.background)
                )
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(trackColor)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), CircleShape)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    DailyChartMode.entries.forEachIndexed { index, mode ->
                        val isSel = chartMode == mode
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        LaunchedEffect(isPressed) {
                            if (isPressed) pressedIndex = index
                            else if (pressedIndex == index) pressedIndex = null
                        }
                        DisposableEffect(interactionSource) {
                            onDispose { if (pressedIndex == index) pressedIndex = null }
                        }
                        val spatialMotion = connectedSpatialMotion(index, pressedIndex, isPressed)
                        val buttonDensity = LocalDensity.current
                        var measuredButtonHeight by remember { mutableStateOf(28.dp) }
                        val shapeProgress by rememberCompletePressProgress(interactionSource)
                        val cornerRadius = androidx.compose.ui.unit.lerp(measuredButtonHeight / 2, 6.dp, shapeProgress)
                        val buttonShape = RoundedCornerShape(
                            topStart = if (index == 0) measuredButtonHeight / 2 else cornerRadius,
                            bottomStart = if (index == 0) measuredButtonHeight / 2 else cornerRadius,
                            topEnd = if (index == DailyChartMode.entries.lastIndex) measuredButtonHeight / 2 else cornerRadius,
                            bottomEnd = if (index == DailyChartMode.entries.lastIndex) measuredButtonHeight / 2 else cornerRadius
                        )
                        val colorMotion = rememberConnectedButtonColorMotion(
                            isSelected = isSel,
                            isPressed = isPressed,
                            inactiveContainerColor = Color.Transparent,
                            inactiveContentColor = colors.onSurfaceVariant,
                            backdropColor = trackBackdrop
                        )
                        Box(
                            modifier = Modifier
                                .testTag("SpendingTrendMode_${mode.name}")
                                .semantics { selected = isSel }
                                .onSizeChanged { size ->
                                    measuredButtonHeight = with(buttonDensity) { size.height.toDp() }
                                }
                                .then(spatialMotion)
                                .clip(buttonShape)
                                .background(colorMotion.containerColor)
                                .clickable(
                                    interactionSource = interactionSource,
                                    role = Role.RadioButton,
                                    indication = ripple(bounded = true, color = colorMotion.rippleColor)
                                ) {
                                    chartMode = mode
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                }
                                .then(colorMotion.contentModifier)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (mode) {
                                    DailyChartMode.CUMULATIVE -> stringResource(R.string.chart_mode_cumulative)
                                    DailyChartMode.DAILY -> stringResource(R.string.chart_mode_daily)
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = colorMotion.contentColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tooltip Display Area with Rolling Numbers
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.55f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                if (activeItem != null && activeDailyItem != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val formattedDate = activeItem.first.format(DateTimeFormatter.ofPattern("EEE, d MMM", androidx.compose.ui.platform.LocalConfiguration.current.locales[0]))
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val labelText = if (activeItem.first == today) stringResource(R.string.loc_today_prefix) else stringResource(R.string.loc_daily_prefix)
                                Text(
                                    text = labelText,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                                com.example.vibefinance.ui.components.RollingNumberText(
                                    text = String.format(Locale.US, "$%,.2f", activeDailyItem.second),
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (chartMode == DailyChartMode.CUMULATIVE) stringResource(R.string.loc_cumulative_label) else stringResource(R.string.loc_day_spend_label),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                            )
                            com.example.vibefinance.ui.components.RollingNumberText(
                                modifier = Modifier.testTag("SpendingTrendSummaryValue"),
                                text = String.format(Locale.US, "$%,.2f", if (chartMode == DailyChartMode.CUMULATIVE) activeItem.second else activeDailyItem.second),
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bezier Chart Canvas & Interactive Scrubbing Layer
            val strokeColor = MaterialTheme.colorScheme.primary
            val gradientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
            val highlightColor = MaterialTheme.colorScheme.tertiary
            val avgGuideColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)
            
            val targetXRatio = activeIndex / 6f
            val currentTargetVal = activeDataset.getOrNull(activeIndex)?.second ?: 0.0
            val targetYRatio = ((currentTargetVal / maxSpending).coerceIn(0.0, 1.0)).toFloat()

            val animatedRatioX by animateFloatAsState(
                targetValue = targetXRatio,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "scrubRatioX"
            )

            val animatedRatioY by animateFloatAsState(
                targetValue = targetYRatio,
                animationSpec = chartMotionSpec,
                label = "scrubRatioY"
            )

            val animatedGlowRadius by animateDpAsState(
                targetValue = if (isDragging) 18.dp else 12.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "glowRadius"
            )

            val animatedDotRadius by animateDpAsState(
                targetValue = if (isDragging) 9.dp else 6.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "dotRadius"
            )

            var chartWidthPx by remember { mutableFloatStateOf(0f) }
            val density = LocalDensity.current

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .testTag("SpendingTrendCanvas")
                    .onGloballyPositioned { coordinates ->
                        chartWidthPx = coordinates.size.width.toFloat()
                    }
                    .pointerInput(activeDataset) {
                        detectTapGestures(
                            onPress = { offset ->
                                isDragging = true
                                val dayWidth = size.width / 6f
                                val idx = (offset.x / dayWidth).roundToInt().coerceIn(0, 6)
                                selectedIndex = idx
                                tryAwaitRelease()
                                isDragging = false
                            }
                        )
                    }
                    .pointerInput(activeDataset) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                isDragging = true
                                val dayWidth = size.width / 6f
                                selectedIndex = (offset.x / dayWidth).roundToInt().coerceIn(0, 6)
                            },
                            onDragEnd = {
                                isDragging = false
                            },
                            onDragCancel = {
                                isDragging = false
                            },
                            onDrag = { change, _ ->
                                val dayWidth = size.width / 6f
                                selectedIndex = (change.position.x / dayWidth).roundToInt().coerceIn(0, 6)
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    val dayWidth = width / 6f
                    val points = pointRatios.mapIndexed { idx, ratio ->
                        val x = idx * dayWidth
                        val y = height * (1f - ratio.value.coerceIn(0f, 1f))
                        Offset(x, y)
                    }

                    // 1. Draw horizontal grid lines
                    val gridLinesCount = 4
                    for (i in 0 until gridLinesCount) {
                        val gridY = (height / (gridLinesCount - 1)) * i
                        drawLine(
                            color = strokeColor.copy(alpha = 0.08f),
                            start = Offset(0f, gridY),
                            end = Offset(width, gridY),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // 1.5 Draw daily average reference guide line in DAILY mode
                    val detailAlpha = dailyDetailAlpha.value.coerceIn(0f, 1f)
                    if (detailAlpha > 0f && avgDailySpending > 0) {
                        val avgY = height * (1f - averageRatio.value.coerceIn(0f, 1f))
                        if (avgY in 0f..height) {
                            drawLine(
                                color = avgGuideColor.copy(alpha = avgGuideColor.alpha * detailAlpha),
                                start = Offset(0f, avgY),
                                end = Offset(width, avgY),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                            )
                        }
                    }

                    // 2. In Daily Mode: Draw subtle vertical bar pillars for each day
                    if (detailAlpha > 0f) {
                        points.forEachIndexed { idx, pt ->
                            val isSel = idx == activeIndex
                            val barWidth = 14.dp.toPx()
                            val barTop = pt.y
                            val barHeight = (height - barTop).coerceAtLeast(0f)
                            val barAlpha = if (isSel) 0.35f else 0.12f
                            drawRoundRect(
                                color = strokeColor.copy(alpha = barAlpha * detailAlpha),
                                topLeft = Offset(pt.x - barWidth / 2, barTop),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )
                        }
                    }

                    // 3. Draw smooth Bezier curve line
                    val linePath = Path()
                    val fillPath = Path()

                    if (points.isNotEmpty()) {
                        linePath.moveTo(points[0].x, points[0].y)
                        fillPath.moveTo(points[0].x, height)
                        fillPath.lineTo(points[0].x, points[0].y)

                        for (i in 0 until points.size - 1) {
                            val p1 = points[i]
                            val p2 = points[i + 1]
                            val conX1 = p1.x + (p2.x - p1.x) / 2f
                            val conY1 = p1.y
                            val conX2 = p1.x + (p2.x - p1.x) / 2f
                            val conY2 = p2.y

                            linePath.cubicTo(conX1, conY1, conX2, conY2, p2.x, p2.y)
                            fillPath.cubicTo(conX1, conY1, conX2, conY2, p2.x, p2.y)
                        }

                        fillPath.lineTo(points.last().x, height)
                        fillPath.close()

                        // Draw background gradient fill under line
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(gradientColor, Color.Transparent)
                            )
                        )

                        // Draw bezier line path
                        drawPath(
                            path = linePath,
                            color = strokeColor,
                            style = Stroke(
                                width = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        )
                    }

                    // 4. Draw vertical scrubbing indicator line & smooth animated gliding dot
                    val animPointX = animatedRatioX * width
                    val animPointY = height * (1f - animatedRatioY.coerceIn(0f, 1f))
                    val animPoint = Offset(animPointX, animPointY)

                    drawLine(
                        color = highlightColor.copy(alpha = 0.55f),
                        start = Offset(animPointX, 0f),
                        end = Offset(animPointX, height),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )

                    // Draw outer glow circle with Spring scale transition
                    drawCircle(
                        color = highlightColor.copy(alpha = 0.28f),
                        radius = animatedGlowRadius.toPx(),
                        center = animPoint
                    )

                    // Draw inner highlight circle with Spring transition
                    drawCircle(
                        color = highlightColor,
                        radius = animatedDotRadius.toPx(),
                        center = animPoint
                    )
                }

                // 5. Interactive Floating Scrub Tooltip Badge
                androidx.compose.animation.AnimatedVisibility(
                    visible = isDragging,
                    enter = fadeIn(tween(120)) + scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
                    exit = fadeOut(tween(120)) + scaleOut(spring(stiffness = Spring.StiffnessMediumLow)),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    if (chartWidthPx > 0f) {
                        val badgeWidth = with(density) { 92.dp.toPx() }
                        val currX = animatedRatioX * chartWidthPx
                        val clampedX = (currX - badgeWidth / 2f).coerceIn(4f, (chartWidthPx - badgeWidth - 4f).coerceAtLeast(4f))
                        val animY = 180.dp.value * (1f - animatedRatioY)
                        val clampedY = with(density) { (animY - 50).dp.toPx().coerceIn(4f, 100f) }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shadowElevation = 6.dp,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .offset { IntOffset(clampedX.roundToInt(), clampedY.roundToInt()) }
                                .width(92.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val currentVal = if (chartMode == DailyChartMode.CUMULATIVE) activeItem?.second ?: 0.0 else activeDailyItem?.second ?: 0.0
                                Text(
                                    text = activeItem?.first?.format(DateTimeFormatter.ofPattern("EEE, d MMM", androidx.compose.ui.platform.LocalConfiguration.current.locales[0])) ?: "",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                                Text(
                                    text = String.format(Locale.US, "$%,.0f", currentVal),
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // X-Axis Labels (Day name abbreviations with Spring scale & color transition)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                last7Days.forEachIndexed { idx, date ->
                    val dayAbbrev = date.format(DateTimeFormatter.ofPattern("E", androidx.compose.ui.platform.LocalConfiguration.current.locales[0])).take(3)
                    val isSelected = activeIndex == idx

                    val labelColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                        animationSpec = androidx.compose.animation.core.tween(200),
                        label = "labelColor_$idx"
                    )
                    val labelScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.2f else 1.0f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "labelScale_$idx"
                    )

                    Text(
                        text = dayAbbrev,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                        color = labelColor,
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = labelScale
                                scaleY = labelScale
                            }
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                selectedIndex = idx
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            }
                    )
                }
            }
        }
    }
}
