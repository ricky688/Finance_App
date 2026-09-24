package com.example.vibefinance.ui.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Applies an alpha fade mask to the start (left) and end (right) edges of a horizontally scrollable container
 * using offscreen compositing and BlendMode.DstIn to prevent abrupt visual clipping at screen boundaries.
 *
 * @param startFadeWidth Width of the fade gradient on the start edge.
 * @param endFadeWidth Width of the fade gradient on the end edge.
 */
fun Modifier.horizontalFadingEdge(
    startFadeWidth: Dp = 16.dp,
    endFadeWidth: Dp = 16.dp
): Modifier = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()

        val startFadePx = startFadeWidth.toPx()
        val endFadePx = endFadeWidth.toPx()

        if (startFadePx > 0f && size.width > 0f) {
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, Color.Black),
                    startX = 0f,
                    endX = startFadePx.coerceAtMost(size.width)
                ),
                blendMode = BlendMode.DstIn
            )
        }

        if (endFadePx > 0f && size.width > 0f) {
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Black, Color.Transparent),
                    startX = (size.width - endFadePx).coerceAtLeast(0f),
                    endX = size.width
                ),
                blendMode = BlendMode.DstIn
            )
        }
    }

/**
 * Applies an alpha fade mask to the top and bottom edges of a vertically scrollable container
 * using offscreen compositing and BlendMode.DstIn to prevent abrupt visual clipping at screen boundaries.
 *
 * @param topFadeHeight Height of the fade gradient on the top edge.
 * @param bottomFadeHeight Height of the fade gradient on the bottom edge.
 */
fun Modifier.verticalFadingEdge(
    topFadeHeight: Dp = 16.dp,
    bottomFadeHeight: Dp = 16.dp
): Modifier = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()

        val topFadePx = topFadeHeight.toPx()
        val bottomFadePx = bottomFadeHeight.toPx()

        if (topFadePx > 0f && size.height > 0f) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black),
                    startY = 0f,
                    endY = topFadePx.coerceAtMost(size.height)
                ),
                blendMode = BlendMode.DstIn
            )
        }

        if (bottomFadePx > 0f && size.height > 0f) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Black, Color.Transparent),
                    startY = (size.height - bottomFadePx).coerceAtLeast(0f),
                    endY = size.height
                ),
                blendMode = BlendMode.DstIn
            )
        }
    }

/**
 * Applies an alpha fade mask that dynamically tracks an offset bottom bar (e.g. collapsing NavigationBar).
 * When nav bar is visible, the bottom fade ends at the top of the nav bar.
 * As the nav bar collapses/translates down, the fade tracks its top edge in lockstep.
 * When the nav bar is fully collapsed, the fade applies directly to the bottom of the container.
 *
 * @param topFadeHeight Height of the fade gradient on the top edge.
 * @param bottomFadeHeight Height of the fade gradient on the bottom edge.
 * @param bottomBarHeightProvider Pixel height of the bottom bar when fully visible.
 * @param navBarOffsetProvider Current vertical translation/offset in pixels of the bottom bar.
 */
fun Modifier.responsiveVerticalFadingEdge(
    topFadeHeight: Dp = 100.dp,
    bottomFadeHeight: Dp = 110.dp,
    bottomBarHeightProvider: () -> Float = { 0f },
    navBarOffsetProvider: () -> Float = { 0f }
): Modifier = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()

        val topFadePx = topFadeHeight.toPx()
        val bottomFadePx = bottomFadeHeight.toPx()

        // 1. Top fade under pinned pill top bar
        if (topFadePx > 0f && size.height > 0f) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black),
                    startY = 0f,
                    endY = topFadePx.coerceAtMost(size.height)
                ),
                blendMode = BlendMode.DstIn
            )
        }

        // 2. Dynamic bottom fade following the bottom navigation bar
        if (bottomFadePx > 0f && size.height > 0f) {
            val barHeightPx = bottomBarHeightProvider()
            val navOffsetYPx = navBarOffsetProvider()

            // When nav bar is visible (navOffsetYPx = 0): effectiveBarTop = size.height - barHeightPx
            // As nav bar translates down (navOffsetYPx > 0): effectiveBarTop translates down in lockstep
            // When nav bar is collapsed (navOffsetYPx >= barHeightPx): effectiveBarTop = size.height (bottom of app)
            val effectiveBarTop = (size.height - (barHeightPx - navOffsetYPx).coerceAtLeast(0f))
                .coerceIn(0f, size.height)

            val fadeStart = (effectiveBarTop - bottomFadePx).coerceAtLeast(0f)

            if (effectiveBarTop > fadeStart) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Black, Color.Transparent),
                        startY = fadeStart,
                        endY = effectiveBarTop
                    ),
                    topLeft = Offset(0f, fadeStart),
                    size = Size(size.width, effectiveBarTop - fadeStart),
                    blendMode = BlendMode.DstIn
                )
            }

            // Clear any content residing beneath the bottom navigation bar
            if (effectiveBarTop < size.height) {
                drawRect(
                    color = Color.Transparent,
                    topLeft = Offset(0f, effectiveBarTop),
                    size = Size(size.width, size.height - effectiveBarTop),
                    blendMode = BlendMode.DstIn
                )
            }
        }
    }

