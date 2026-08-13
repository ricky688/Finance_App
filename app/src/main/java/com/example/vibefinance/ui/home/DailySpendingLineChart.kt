package com.example.vibefinance.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import kotlin.math.roundToInt
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.ui.FinanceUiState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

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
        // Initialize
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

    val maxSpending = remember(cumulativeSpending) {
        val maxVal = cumulativeSpending.maxOfOrNull { it.second } ?: 0.0
        if (maxVal < 100.0) 100.0 else maxVal
    }

    // 2. Interactive Touch/Scrub state
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var isDragging by remember { mutableStateOf(false) }

    val activeIndex = selectedIndex ?: 6 // default to last item (today)
    val activeItem = cumulativeSpending.getOrNull(activeIndex)
    val activeDailyItem = dailySpending.getOrNull(activeIndex)

    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    var previousIdx by remember { mutableStateOf<Int?>(null) }
    androidx.compose.runtime.LaunchedEffect(selectedIndex) {
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Weekly Spending Trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Cumulative expenses last 7 days (Drag to scrub)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                
                // Tooltip trigger info indicator
                Icon(
                    imageVector = Icons.Default.Info,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tooltip Display Area with Rolling Numbers
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (activeItem != null && activeDailyItem != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val formattedDate = activeItem.first.format(DateTimeFormatter.ofPattern("EEEE, dd MMM", Locale.US))
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Today's Spent: ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                                com.example.vibefinance.ui.components.RollingNumberText(
                                    text = String.format(Locale.US, "$%,.2f", activeDailyItem.second),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "CUMULATIVE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            com.example.vibefinance.ui.components.RollingNumberText(
                                text = String.format(Locale.US, "$%,.2f", activeItem.second),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bezier Chart Canvas
            val strokeColor = MaterialTheme.colorScheme.primary
            val gradientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            val highlightColor = MaterialTheme.colorScheme.tertiary
            
            // Smooth animated position gliding for active crosshair indicator & glow
            val targetXRatio = activeIndex / 6f
            val targetYRatio = (((activeItem?.second ?: 0.0) / maxSpending).coerceIn(0.0, 1.0)).toFloat()

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
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "scrubRatioY"
            )

            val animatedGlowRadius by animateDpAsState(
                targetValue = if (isDragging) 16.dp else 12.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "glowRadius"
            )

            val animatedDotRadius by animateDpAsState(
                targetValue = if (isDragging) 8.dp else 6.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "dotRadius"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .pointerInput(cumulativeSpending) {
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
                    .pointerInput(cumulativeSpending) {
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
                    val points = cumulativeSpending.mapIndexed { idx, pair ->
                        val x = idx * dayWidth
                        val y = height - ((pair.second / maxSpending) * height).toFloat()
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

                    // 2. Draw smooth Bezier curve line
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

                    // 3. Draw vertical scrubbing indicator line & smooth animated gliding dot
                    val animPointX = animatedRatioX * width
                    val animPointY = height - (animatedRatioY * height)
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
            }

            Spacer(modifier = Modifier.height(8.dp))

            // X-Axis Labels (Day name abbreviations with Spring scale & color transition)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                last7Days.forEachIndexed { idx, date ->
                    val dayAbbrev = date.format(DateTimeFormatter.ofPattern("E", Locale.US)).take(3)
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
                        modifier = Modifier.graphicsLayer {
                            scaleX = labelScale
                            scaleY = labelScale
                        }
                    )
                }
            }
        }
    }
}
