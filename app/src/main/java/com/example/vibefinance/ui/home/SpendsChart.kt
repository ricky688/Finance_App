package com.example.vibefinance.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.vibefinance.data.entity.TransactionEntity
import kotlin.math.abs

@Composable
fun SpendsChart(
    modifier: Modifier = Modifier,
    spends: List<TransactionEntity>,
    markedTransaction: TransactionEntity? = null,
    showBeforeMarked: Int = spends.size,
    showAfterMarked: Int = spends.size,
    chartPadding: PaddingValues = PaddingValues(0.dp),
    colorMin: Color = Color(0xFF185ED6),
    colorMax: Color = Color(0xFFDD1414),
) {
    if (spends.isEmpty()) return

    val colors = listOf(colorMax, colorMin)
    val sortedSpends = remember(spends) { spends.sortedBy { it.timestamp } }

    val minSpentValue = remember(sortedSpends) { sortedSpends.minOfOrNull { it.amount } ?: 0.0 }
    val maxSpentValue = remember(sortedSpends) { sortedSpends.maxOfOrNull { it.amount } ?: 0.0 }
    val range = maxSpentValue - minSpentValue
    val localDensity = LocalDensity.current

    val layoutDirection = LayoutDirection.Ltr
    val topOffset = with(localDensity) { chartPadding.calculateTopPadding().toPx() }
    val bottomOffset = with(localDensity) { chartPadding.calculateBottomPadding().toPx() }
    val startOffset = with(localDensity) { chartPadding.calculateStartPadding(layoutDirection).toPx() }
    val endOffset = with(localDensity) { chartPadding.calculateEndPadding(layoutDirection).toPx() }

    val (indexMarked, firstShowIndex, lastShowIndex) = if (markedTransaction != null) {
        val index = sortedSpends.indexOfFirst { it.id == markedTransaction.id || (it.timestamp == markedTransaction.timestamp && it.amount == markedTransaction.amount) }
        Triple(
            index,
            (index - showBeforeMarked).coerceAtLeast(0),
            (index + showAfterMarked + 1).coerceAtMost(sortedSpends.size),
        )
    } else {
        Triple(null, 0, sortedSpends.size)
    }

    val displaySpends = remember(sortedSpends, firstShowIndex, lastShowIndex) {
        sortedSpends.subList(firstShowIndex, lastShowIndex)
    }

    Canvas(modifier = modifier) {
        if (displaySpends.isEmpty()) return@Canvas

        val width = this.size.width
        val height = this.size.height
        val heightWithPaddings = height - topOffset - bottomOffset
        val widthWithPaddings = width - startOffset - endOffset
        val size = (displaySpends.size - 1).toFloat().coerceAtLeast(1f)

        val trianglePath = Path().apply {
            var lastY = 0f

            displaySpends.forEachIndexed { index, spent ->
                val scale = if (range <= 0.0) {
                    0.5f
                } else {
                    ((spent.amount - minSpentValue) / range).toFloat()
                }

                if (index == 0) {
                    lastY = topOffset + heightWithPaddings * (1f - scale)
                    moveTo(0f, lastY)
                }

                cubicTo(
                    startOffset + widthWithPaddings * ((index - 0.5f).coerceAtLeast(0f) / size),
                    lastY,
                    startOffset + widthWithPaddings * ((index - 0.5f).coerceAtLeast(0f) / size),
                    topOffset + heightWithPaddings * (1f - scale),
                    startOffset + widthWithPaddings * (index / size),
                    topOffset + heightWithPaddings * (1f - scale),
                )

                lastY = topOffset + heightWithPaddings * (1f - scale)
            }

            lineTo(width, lastY)
            lineTo(width, height)
            lineTo(0f, height)
        }

        val chartColors = if (markedTransaction != null) {
            val scale = if (range <= 0.0) {
                0.5f
            } else {
                1f - ((markedTransaction.amount - minSpentValue) / range).toFloat()
            }

            colors.mapIndexed { index, color ->
                color.copy(alpha = 0.25f - abs(scale - (index.toFloat() / (colors.size - 1))) * 0.15f)
            }
        } else {
            colors.mapIndexed { index, color ->
                color.copy(alpha = 0.25f - (index.toFloat() / (colors.size - 1)) * 0.15f)
            }
        }

        drawPath(
            path = trianglePath,
            Brush.verticalGradient(colors = chartColors),
            style = Fill
        )

        if (markedTransaction != null && indexMarked != null && indexMarked >= firstShowIndex && indexMarked < lastShowIndex) {
            val scale = if (range <= 0.0) {
                0.5f
            } else {
                ((markedTransaction.amount - minSpentValue) / range).toFloat()
            }

            val color = if (markedTransaction.id == sortedSpends.maxByOrNull { it.amount }?.id) colorMax else colorMin

            val x = startOffset + widthWithPaddings * ((indexMarked - firstShowIndex) / size)
            val y = topOffset + heightWithPaddings * (1f - scale)

            drawCircle(
                color = color.copy(0.15f),
                radius = with(localDensity) { 8.dp.toPx() },
                center = Offset(x, y)
            )

            drawCircle(
                color = color,
                radius = with(localDensity) { 3.dp.toPx() },
                center = Offset(x, y)
            )
        }
    }
}
