package com.example.vibefinance.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.ui.common.bouncyClickable
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.sqrt

data class MoneySegment(
    val id: String,
    val name: String,
    val amount: Double,
    val percentage: Float,
    val color: Color
)

@Composable
fun MoneySeparationChart(
    accounts: List<AccountEntity>,
    modifier: Modifier = Modifier
) {
    val totalAssets = accounts.filter { it.type != AccountType.CC }.sumOf { it.balance }
    val cashTotal = accounts.filter { it.type == AccountType.CASH }.sumOf { it.balance }
    val bankTotal = accounts.filter { it.type == AccountType.BANK }.sumOf { it.balance }
    val debtTotal = accounts.filter { it.type == AccountType.CC }.sumOf { it.balance }

    val validTotal = if (totalAssets > 0) totalAssets else 1.0

    val segments = remember(accounts, totalAssets) {
        val list = mutableListOf<MoneySegment>()
        if (cashTotal > 0) {
            list.add(
                MoneySegment(
                    id = "cash",
                    name = "Cash Wallet",
                    amount = cashTotal,
                    percentage = (cashTotal / validTotal).toFloat(),
                    color = Color(0xFF4CAF50) // Emerald Green (Matches History chart green)
                )
            )
        }
        if (bankTotal > 0) {
            list.add(
                MoneySegment(
                    id = "bank",
                    name = "Bank Accounts",
                    amount = bankTotal,
                    percentage = (bankTotal / validTotal).toFloat(),
                    color = Color(0xFF2196F3) // Ocean Blue (Matches History chart blue)
                )
            )
        }
        if (debtTotal > 0) {
            list.add(
                MoneySegment(
                    id = "debt",
                    name = "Credit Debt",
                    amount = debtTotal,
                    percentage = 0f, // Special non-positive asset segment
                    color = Color(0xFFE91E63) // Rose Pink / Red (Matches History chart accent)
                )
            )
        }
        list
    }

    var selectedSegmentId by remember { mutableStateOf<String?>(null) }
    val selectedSegment = segments.find { it.id == selectedSegmentId }

    // Haptic Feedback for scrubbing focus (Identical to History chart & Daily chart)
    val haptic = LocalHapticFeedback.current
    var prevSelectedId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(selectedSegmentId) {
        if (selectedSegmentId != prevSelectedId && selectedSegmentId != null) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        prevSelectedId = selectedSegmentId
    }

    val assetSegments = segments.filter { it.percentage > 0f }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header (Identical layout & style to History page CategoryBreakdownCard)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Money Allocation (資金分佈)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Focused Clear Pill with spring scaleIn & scaleOut (Identical to History page)
                            AnimatedVisibility(
                                visible = selectedSegmentId != null,
                                enter = fadeIn(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)) +
                                        scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)),
                                exit = fadeOut(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy)) +
                                       scaleOut(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy))
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = selectedSegment?.color?.copy(alpha = 0.2f) ?: MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clickable { selectedSegmentId = null }
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Focused",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = selectedSegment?.color ?: MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear",
                                            tint = selectedSegment?.color ?: MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Text(
                            text = if (selectedSegmentId != null) "Tap category to clear focus filter" else "Tap any category below to filter assets",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (selectedSegmentId != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Top Section: Cash & Bank Assets Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cash & Bank Assets",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = String.format(Locale.US, "$%,.2f", totalAssets),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                assetSegments.forEachIndexed { index, seg ->
                    val isSegmentSelected = selectedSegmentId == seg.id
                    val isSegmentDimmed = selectedSegmentId != null && !isSegmentSelected

                    val animatedFraction by animateFloatAsState(
                        targetValue = seg.percentage.coerceIn(0.01f, 1f),
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "segment_spring"
                    )

                    val segmentHeight by animateDpAsState(
                        targetValue = when {
                            isSegmentSelected -> 20.dp
                            isSegmentDimmed -> 8.dp
                            else -> 12.dp
                        },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "segment_height_spring"
                    )

                    val segmentAlpha by animateFloatAsState(
                        targetValue = if (isSegmentDimmed) 0.35f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "segment_alpha_spring"
                    )

                    val segmentScale by animateFloatAsState(
                        targetValue = if (isSegmentSelected) 1.05f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "segment_scale_spring"
                    )

                    Box(
                        modifier = Modifier
                            .weight(animatedFraction)
                            .height(segmentHeight)
                            .graphicsLayer {
                                scaleX = segmentScale
                                scaleY = segmentScale
                            }
                            .clip(CircleShape)
                            .background(seg.color.copy(alpha = segmentAlpha))
                            .clickable {
                                selectedSegmentId = if (isSegmentSelected) null else seg.id
                            }
                    )
                }
            }

            // Bottom Section: Credit Debt Bar (Appears right under the Cash/Bank bar!)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Credit Debt",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = String.format(Locale.US, "$%,.2f", debtTotal),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(4.dp))

            val debtFraction = if (totalAssets > 0) (debtTotal / totalAssets).toFloat().coerceIn(0.02f, 1f) else if (debtTotal > 0) 1f else 0f
            val debtAnimatedFraction by animateFloatAsState(
                targetValue = if (debtTotal > 0) debtFraction else 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "debt_bar_fraction"
            )
            val isDebtSelected = selectedSegmentId == "debt"
            val isDebtDimmed = selectedSegmentId != null && !isDebtSelected

            val debtBarHeight by animateDpAsState(
                targetValue = when {
                    isDebtSelected -> 18.dp
                    isDebtDimmed -> 6.dp
                    else -> 10.dp
                },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "debt_height"
            )

            val debtAlpha by animateFloatAsState(
                targetValue = if (isDebtDimmed) 0.35f else 1.0f,
                label = "debt_alpha"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                    .clickable {
                        selectedSegmentId = if (isDebtSelected) null else "debt"
                    }
            ) {
                if (debtTotal > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(debtAnimatedFraction)
                            .height(debtBarHeight)
                            .align(Alignment.CenterStart)
                            .clip(CircleShape)
                            .background(Color(0xFFE91E63).copy(alpha = debtAlpha))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Detailed Interactive Legend Grid with Spring Highlight Physics (EXACT History Page Style)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                segments.forEach { seg ->
                    val isSelected = selectedSegmentId == seg.id
                    val isDimmed = selectedSegmentId != null && !isSelected

                    val rowBgColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        else Color.Transparent,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "row_bg_spring"
                    )

                    val rowAlpha by animateFloatAsState(
                        targetValue = if (isDimmed) 0.35f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "row_alpha_spring"
                    )

                    val dotSize by animateDpAsState(
                        targetValue = if (isSelected) 14.dp else 10.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "dot_size_spring"
                    )

                    val horizontalPadding by animateDpAsState(
                        targetValue = if (isSelected) 12.dp else 8.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "row_h_padding_spring"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(rowBgColor)
                            .clickable {
                                selectedSegmentId = if (isSelected) null else seg.id
                            }
                            .padding(horizontal = horizontalPadding, vertical = 6.dp)
                            .alpha(rowAlpha),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(dotSize)
                                    .clip(CircleShape)
                                    .background(seg.color)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = seg.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (seg.percentage > 0f) {
                                Text(
                                    text = String.format(Locale.US, "%.1f%%", seg.percentage * 100),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            RollingNumberText(
                                text = String.format(Locale.US, "$%,.2f", seg.amount),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (seg.id == "debt") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveDonutChart(
    accounts: List<AccountEntity>,
    modifier: Modifier = Modifier,
    size: Float = 110f
) {
    val totalAssets = accounts.filter { it.type != AccountType.CC }.sumOf { it.balance }
    val cashTotal = accounts.filter { it.type == AccountType.CASH }.sumOf { it.balance }
    val bankTotal = accounts.filter { it.type == AccountType.BANK }.sumOf { it.balance }

    val validTotal = if (totalAssets > 0) totalAssets else 1.0

    val segments = remember(accounts, totalAssets) {
        val list = mutableListOf<MoneySegment>()
        if (cashTotal > 0) {
            list.add(
                MoneySegment(
                    id = "cash",
                    name = "Cash",
                    amount = cashTotal,
                    percentage = (cashTotal / validTotal).toFloat(),
                    color = Color(0xFF4CAF50)
                )
            )
        }
        if (bankTotal > 0) {
            list.add(
                MoneySegment(
                    id = "bank",
                    name = "Bank Accounts",
                    amount = bankTotal,
                    percentage = (bankTotal / validTotal).toFloat(),
                    color = Color(0xFF2196F3)
                )
            )
        }
        list
    }

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(accounts) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val haptic = LocalHapticFeedback.current
    var prevIndex by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(selectedIndex) {
        if (selectedIndex != prevIndex && selectedIndex != null) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        prevIndex = selectedIndex
    }

    val density = LocalDensity.current

    Box(
        modifier = modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(segments) {
                    detectTapGestures { offset ->
                        val sizePx = with(density) { size.dp.toPx() }
                        val marginPx = with(density) { 24.dp.toPx() }
                        val center = Offset(sizePx / 2f, sizePx / 2f)
                        val dx = offset.x - center.x
                        val dy = offset.y - center.y
                        val dist = sqrt(dx * dx + dy * dy)
                        val innerRadius = (sizePx / 2f) - marginPx
                        val outerRadius = sizePx / 2f

                        if (dist in innerRadius..outerRadius) {
                            var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            if (angle < 0) angle += 360f
                            val adjustedAngle = (angle + 90f) % 360f

                            var currentAngle = 0f
                            var tappedIdx: Int? = null
                            segments.forEachIndexed { idx, seg ->
                                val sweep = seg.percentage * 360f
                                if (adjustedAngle in currentAngle..(currentAngle + sweep)) {
                                    tappedIdx = idx
                                }
                                currentAngle += sweep
                            }
                            selectedIndex = if (selectedIndex == tappedIdx) null else tappedIdx
                        }
                    }
                }
                .pointerInput(segments) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val sizePx = with(density) { size.dp.toPx() }
                            val center = Offset(sizePx / 2f, sizePx / 2f)
                            val dx = offset.x - center.x
                            val dy = offset.y - center.y
                            var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            if (angle < 0) angle += 360f
                            val adjustedAngle = (angle + 90f) % 360f

                            var currentAngle = 0f
                            segments.forEachIndexed { idx, seg ->
                                val sweep = seg.percentage * 360f
                                if (adjustedAngle in currentAngle..(currentAngle + sweep)) {
                                    selectedIndex = idx
                                }
                                currentAngle += sweep
                            }
                        },
                        onDrag = { change, _ ->
                            val sizePx = with(density) { size.dp.toPx() }
                            val center = Offset(sizePx / 2f, sizePx / 2f)
                            val dx = change.position.x - center.x
                            val dy = change.position.y - center.y
                            var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            if (angle < 0) angle += 360f
                            val adjustedAngle = (angle + 90f) % 360f

                            var currentAngle = 0f
                            segments.forEachIndexed { idx, seg ->
                                val sweep = seg.percentage * 360f
                                if (adjustedAngle in currentAngle..(currentAngle + sweep)) {
                                    selectedIndex = idx
                                }
                                currentAngle += sweep
                            }
                        }
                    )
                }
        ) {
            val baseStrokeWidth = 14.dp.toPx()
            val canvasSize = Size(size.dp.toPx() - baseStrokeWidth - 8.dp.toPx(), size.dp.toPx() - baseStrokeWidth - 8.dp.toPx())
            val topLeft = Offset((baseStrokeWidth + 8.dp.toPx()) / 2f, (baseStrokeWidth + 8.dp.toPx()) / 2f)

            var startAngle = -90f

            if (segments.isEmpty()) {
                drawArc(
                    color = Color.Gray.copy(alpha = 0.2f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = canvasSize,
                    style = Stroke(width = baseStrokeWidth)
                )
            } else {
                segments.forEachIndexed { idx, seg ->
                    val sweepAngle = seg.percentage * 360f * animProgress.value
                    val isSelected = selectedIndex == idx

                    val strokeWidth = if (isSelected) baseStrokeWidth + 4.dp.toPx() else baseStrokeWidth
                    val currentAlpha = if (selectedIndex == null || isSelected) 1f else 0.35f

                    if (isSelected) {
                        drawArc(
                            color = seg.color.copy(alpha = 0.3f),
                            startAngle = startAngle - 2f,
                            sweepAngle = sweepAngle + 4f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = canvasSize,
                            style = Stroke(width = strokeWidth + 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    drawArc(
                        color = seg.color.copy(alpha = currentAlpha),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = topLeft,
                        size = canvasSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    startAngle += sweepAngle
                }
            }
        }

        val activeSeg = selectedIndex?.let { segments.getOrNull(it) }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = activeSeg?.name ?: "Total Cash",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold
            )
            RollingNumberText(
                text = String.format(Locale.US, "$%,.0f", activeSeg?.amount ?: totalAssets),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = activeSeg?.color ?: MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
