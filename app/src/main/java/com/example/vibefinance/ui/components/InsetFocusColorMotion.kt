package com.example.vibefinance.ui.components

import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.luminance

/**
 * Paints inside a native button's clipped content, preserving its animated outer shape.
 *
 * Increasing [progress] fills inward from every edge. Reversing the same value clears the
 * center outward and fades the remaining perimeter, so interrupted motion stays continuous.
 * Content follows the same mask, keeping the center label legible before primary reaches it.
 */
internal fun Modifier.insetFocusColorMotion(
    progress: State<Float>,
    inactiveColor: Color,
    primaryColor: Color,
    inactiveContentColor: Color,
    primaryContentColor: Color,
    backdropColor: Color,
    preserveContentColors: Boolean = false
): Modifier = drawWithCache {
    val bounds = Rect(0f, 0f, size.width, size.height)
    val contentPaint = Paint()
    val inactiveBackdrop = inactiveColor.compositeOver(backdropColor)

    onDrawWithContent {
        // Read the animation only while drawing; its frames do not rebuild layout or the cache.
        val focus = progress.value.coerceIn(0f, 1f)
        drawRect(inactiveColor)

        when (focus) {
            0f -> drawTintedContent(inactiveContentColor, bounds, contentPaint, preserveContentColors)
            1f -> {
                drawRect(primaryColor)
                drawTintedContent(primaryContentColor, bounds, contentPaint, preserveContentColors)
            }
            else -> {
                val inset = Rect(
                    left = size.width * focus * 0.5f,
                    top = size.height * focus * 0.5f,
                    right = size.width * (1f - focus * 0.5f),
                    bottom = size.height * (1f - focus * 0.5f)
                )
                val washColor = primaryColor.copy(alpha = primaryColor.alpha * focus)

                // A pale, partially transparent primary wash can require the tonal text tint.
                val washBackdrop = washColor.compositeOver(inactiveBackdrop)
                val washContentColor = if (
                    contrastRatio(primaryContentColor, washBackdrop) >=
                    contrastRatio(inactiveContentColor, washBackdrop)
                ) primaryContentColor else inactiveContentColor

                clipRect(inset.left, inset.top, inset.right, inset.bottom) {
                    this@onDrawWithContent.drawTintedContent(inactiveContentColor, bounds, contentPaint, preserveContentColors)
                }
                clipRect(inset.left, inset.top, inset.right, inset.bottom, ClipOp.Difference) {
                    drawRect(washColor)
                    this@onDrawWithContent.drawTintedContent(washContentColor, bounds, contentPaint, preserveContentColors)
                }
            }
        }
    }
}

private fun ContentDrawScope.drawTintedContent(
    color: Color,
    bounds: Rect,
    paint: Paint,
    preserveContentColors: Boolean
) {
    // Emoji are multicolored glyphs. A SrcIn filter turns them into silhouettes.
    if (preserveContentColors) {
        drawContent()
        return
    }
    val canvas = drawContext.canvas
    canvas.save()
    try {
        paint.colorFilter = ColorFilter.tint(color, BlendMode.SrcIn)
        canvas.saveLayer(bounds, paint)
        try {
            drawContent()
        } finally {
            canvas.restore()
        }
    } finally {
        canvas.restore()
    }
}

private fun contrastRatio(content: Color, background: Color): Float {
    val contentLuminance = content.compositeOver(background).luminance()
    val backgroundLuminance = background.luminance()
    return (maxOf(contentLuminance, backgroundLuminance) + 0.05f) /
        (minOf(contentLuminance, backgroundLuminance) + 0.05f)
}
