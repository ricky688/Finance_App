package com.example.vibefinance.ui.home

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.ceil

class WavyShape(
    private val period: Dp,
    private val amplitude: Dp,
    private val shift: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val amplitudePx = with(density) { amplitude.toPx() }
        if (amplitudePx <= 0.001f) {
            return Outline.Rectangle(Rect(offset = Offset.Zero, size = size))
        }
        return Outline.Generic(Path().apply {
            val halfPeriod = with(density) { period.toPx() } / 2
            val wavyPath = Path().apply {
                moveTo(x = 0f, y = 0f)
                lineTo(size.width - amplitudePx, -halfPeriod * 2.5f + halfPeriod * 2 * shift)
                repeat(ceil(size.height / halfPeriod + 3).toInt()) { i ->
                    relativeQuadraticTo(
                        dx1 = 2 * amplitudePx * (if (i % 2 == 0) 1 else -1),
                        dy1 = halfPeriod / 2,
                        dx2 = 0f,
                        dy2 = halfPeriod,
                    )
                }
                lineTo(0f, size.height)
            }
            val boundsPath = Path().apply {
                addRect(Rect(offset = Offset.Zero, size = size))
            }
            op(wavyPath, boundsPath, PathOperation.Intersect)
        })
    }
}
