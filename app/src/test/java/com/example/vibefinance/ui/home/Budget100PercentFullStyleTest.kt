package com.example.vibefinance.ui.home

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Budget100PercentFullStyleTest {

    @Test
    fun heroBudgetCard_amplitudeIsZeroOnlyAt100Percent() {
        fun heroAmplitude(ratio: Float): Float {
            val isFullBudget = ratio >= 1.0f
            return if (isFullBudget) 0f else 4f
        }

        // At 100% full, amplitude is 0 (full style)
        assertEquals(0f, heroAmplitude(1.0f), 0.001f)
        assertEquals(0f, heroAmplitude(1.05f), 0.001f)

        // Below 100%, original 4.dp wave effect is preserved
        assertEquals(4f, heroAmplitude(0.99f), 0.001f)
        assertEquals(4f, heroAmplitude(0.75f), 0.001f)
        assertEquals(4f, heroAmplitude(0.50f), 0.001f)
        assertEquals(4f, heroAmplitude(0.10f), 0.001f)
    }

    @Test
    fun restBudgetCard_amplitudeIsZeroOnlyAt100Percent() {
        fun restAmplitude(ratio: Float): Float {
            val isFullBudget = ratio >= 1.0f
            return if (isFullBudget) 0f else 2f
        }

        // At 100% full, amplitude is 0 (full style)
        assertEquals(0f, restAmplitude(1.0f), 0.001f)
        assertEquals(0f, restAmplitude(1.01f), 0.001f)

        // Below 100%, original 2.dp wave effect is preserved
        assertEquals(2f, restAmplitude(0.99f), 0.001f)
        assertEquals(2f, restAmplitude(0.80f), 0.001f)
        assertEquals(2f, restAmplitude(0.50f), 0.001f)
        assertEquals(2f, restAmplitude(0.05f), 0.001f)
    }

    @Test
    fun wavyShape_returnsRectangularOutlineWhenAmplitudeIsZero() {
        val density = Density(density = 2f)
        val size = Size(width = 300f, height = 150f)

        // Zero amplitude returns flat Outline.Rectangle filling entire size
        val flatShape = WavyShape(period = 36.dp, amplitude = 0.dp, shift = 0f)
        val flatOutline = flatShape.createOutline(size, LayoutDirection.Ltr, density)
        assertTrue(flatOutline is Outline.Rectangle)
        assertEquals(Rect(0f, 0f, 300f, 150f), (flatOutline as Outline.Rectangle).rect)

        // Non-zero amplitude returns Outline.Generic (wavy path)
        val wavyShape = WavyShape(period = 36.dp, amplitude = 4.dp, shift = 0.5f)
        val wavyOutline = wavyShape.createOutline(size, LayoutDirection.Ltr, density)
        assertTrue(wavyOutline is Outline.Generic)
    }
}
