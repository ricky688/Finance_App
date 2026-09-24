package com.example.vibefinance.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import com.example.vibefinance.theme.BentoCardShape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun WholeBudgetCard(
    modifier: Modifier = Modifier,
    budget: Double,
    startDate: Long,
    endDate: Long,
    colors: CardColors = CardDefaults.cardColors(),
) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault()) }
    val startLocalDate = remember(startDate) {
        Instant.ofEpochMilli(startDate).atZone(ZoneId.systemDefault()).toLocalDate()
    }
    val endLocalDate = remember(endDate) {
        Instant.ofEpochMilli(endDate).atZone(ZoneId.systemDefault()).toLocalDate()
    }
    val daysCount = remember(startLocalDate, endLocalDate) {
        ChronoUnit.DAYS.between(startLocalDate, endLocalDate) + 1
    }

    val startDateText = remember(startLocalDate) { startLocalDate.format(formatter) }
    val endDateText = remember(endLocalDate) { endLocalDate.format(formatter) }

    val amountText = remember(budget) { String.format(Locale.US, "$%,.2f", budget) }

    StatCard(
        modifier = modifier
            .fillMaxWidth()
            .clip(BentoCardShape),
        label = androidx.compose.ui.res.stringResource(com.example.vibefinance.R.string.start_budget),
        value = amountText,
        valueFontStyle = MaterialTheme.typography.displayMedium,
        valueFontSize = MaterialTheme.typography.headlineLarge.fontSize,
        colors = colors,
        content = {
            Spacer(modifier = Modifier.height(12.dp))
            Layout(
                modifier = Modifier.height(IntrinsicSize.Min),
                measurePolicy = growByMiddleChildRowMeasurePolicy(LocalDensity.current),
                content = {
                    Column {
                        Text(
                            text = startDateText,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    Box {
                        Arrow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .fillMaxHeight()
                        )
                        CountDaysChip(
                            Modifier.align(Alignment.Center),
                            daysCount = daysCount.toInt()
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = endDateText,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
            )
        }
    )
}

@Composable
fun CountDaysChip(modifier: Modifier = Modifier, daysCount: Int) {
    Surface(
        modifier = modifier.requiredHeight(24.dp),
        shape = CircleShape,
        color = LocalContentColor.current,
        contentColor = MaterialTheme.colorScheme.surface,
    ) {
        Box(
            contentAlignment = Alignment.Center,
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = androidx.compose.ui.res.stringResource(com.example.vibefinance.R.string.days_count_format, daysCount),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                maxLines = 1
            )
        }
    }
}

@Composable
fun Arrow(
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    Canvas(modifier = modifier) {
        val width = this.size.width
        val height = this.size.height
        val heightHalf = height / 2

        val thickness = 6
        val thicknessHalf = thickness / 2

        val trianglePath = Path().let {
            it.moveTo(11f, heightHalf - thicknessHalf)
            it.lineTo(width - 22.4f, heightHalf - thicknessHalf)
            it.lineTo(width - 37.4f, heightHalf - 18)
            it.lineTo(width - 33, heightHalf - 22.4f)
            it.lineTo(width - 10.5f, heightHalf)
            it.lineTo(width - 33, heightHalf + 22.4f)
            it.lineTo(width - 37.4f, heightHalf + 18)
            it.lineTo(width - 22.4f, heightHalf + thicknessHalf)
            it.lineTo(width - 22.4f, heightHalf + thicknessHalf)
            it.lineTo(11f, heightHalf + thicknessHalf)

            it.close()
            it
        }

        drawPath(
            path = trianglePath,
            SolidColor(tint),
            style = Fill
        )
    }
}

fun growByMiddleChildRowMeasurePolicy(localDensity: Density) =
    MeasurePolicy { measurables, constraints ->
        val minMiddleWidth = with(localDensity) { 48.dp.toPx().toInt() }

        val first = measurables[0]
            .measure(
                constraints.copy(
                    maxWidth = ((constraints.maxWidth - minMiddleWidth) / 2).coerceAtLeast(0)
                )
            )
        val last = measurables[2]
            .measure(
                constraints.copy(
                    maxWidth = ((constraints.maxWidth - minMiddleWidth) / 2).coerceAtLeast(0)
                )
            )

        val height = listOf(first, last).minOf { it.height }

        layout(constraints.maxWidth, height) {
            first.placeRelative(0, 0, 0f)

            val middleWidth =
                (constraints.maxWidth - first.width - last.width).coerceAtLeast(minMiddleWidth)

            val middle = measurables[1]
                .measure(
                    constraints.copy(
                        maxWidth = middleWidth,
                        minWidth = middleWidth,
                    )
                )

            middle.placeRelative(first.width, 0, 0f)
            last.placeRelative(constraints.maxWidth - last.width, 0, 0f)
        }
    }
