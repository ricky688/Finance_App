package com.example.vibefinance.ui.settings

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsDrawerTokensTest {
    @Test fun standardWindows_keepPreferredWidth_andTouchableScrim() {
        mapOf(336 to 280, 360 to 304, 400 to 344, 416 to 360, 600 to 360).forEach { (window, expected) ->
            val width = settingsDrawerWidth(window.dp)
            assertEquals(expected.dp, width)
            assertTrue(window.dp - width >= 56.dp)
        }
    }

    @Test fun tinyWindows_prioritizeScrim_withoutNegativeOrOverflowingWidth() {
        mapOf(320 to 264, 280 to 224, 100 to 44, 56 to 0, 40 to 0, 0 to 0).forEach { (window, expected) ->
            val width = settingsDrawerWidth(window.dp)
            assertEquals(expected.dp, width)
            assertTrue(width >= 0.dp && width <= window.dp)
            assertTrue(window.dp - width >= minOf(window.dp, 56.dp))
        }
    }

    @Test fun rightBezelCorners_staySquare_evenInsideRtlHost() {
        for (direction in listOf(LayoutDirection.Ltr, LayoutDirection.Rtl)) {
            val outline = SettingsDrawerTokens.ContainerShape.createOutline(
                Size(720f, 1200f), direction, Density(2f)
            ) as Outline.Rounded
            val rect = outline.roundRect
            assertEquals(CornerRadius(56f), rect.topLeftCornerRadius)
            assertEquals(CornerRadius(56f), rect.bottomLeftCornerRadius)
            assertEquals(CornerRadius.Zero, rect.topRightCornerRadius)
            assertEquals(CornerRadius.Zero, rect.bottomRightCornerRadius)
        }
    }
}
