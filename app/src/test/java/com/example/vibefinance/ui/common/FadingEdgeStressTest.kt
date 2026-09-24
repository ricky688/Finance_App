package com.example.vibefinance.ui.common

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.lang.reflect.Field

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FadingEdgeStressTest {

    @Test
    fun testModifierStructureAndCompositingStrategy() {
        val mod = Modifier.horizontalFadingEdge(16.dp, 16.dp)
        val elements = mod.foldIn(mutableListOf<Modifier.Element>()) { acc, elem -> acc.apply { add(elem) } }

        assertEquals(2, elements.size)

        val graphicsElem = elements[0]
        val strategy = getFieldValue(graphicsElem, "compositingStrategy")
        val isOffscreen = when (strategy) {
            is CompositingStrategy -> strategy == CompositingStrategy.Offscreen
            is Int -> strategy == 1
            else -> strategy.toString().contains("1")
        }
        assertTrue("CompositingStrategy must be Offscreen", isOffscreen)

        val drawElem = elements[1]
        assertTrue("Second element should be DrawWithContent element", drawElem.javaClass.simpleName.contains("Draw"))
    }

    @Test
    fun testVerticalFadingEdgeModifierStructure() {
        val mod = Modifier.verticalFadingEdge(20.dp, 20.dp)
        val elements = mod.foldIn(mutableListOf<Modifier.Element>()) { acc, elem -> acc.apply { add(elem) } }

        assertEquals(2, elements.size)
        val graphicsElem = elements[0]
        val strategy = getFieldValue(graphicsElem, "compositingStrategy")
        val isOffscreen = when (strategy) {
            is CompositingStrategy -> strategy == CompositingStrategy.Offscreen
            is Int -> strategy == 1
            else -> strategy.toString().contains("1")
        }
        assertTrue("CompositingStrategy must be Offscreen", isOffscreen)
    }

    @Test
    fun testHorizontalFadingEdgeRealCanvasExecution() {
        val mod = Modifier.horizontalFadingEdge(16.dp, 16.dp)
        val onDraw = extractOnDrawLambda(mod)

        // Normal size: 100 x 50
        val rendered = executeRealDraw(onDraw, Size(100f, 50f))
        assertTrue("drawContent must be called", rendered.contentDrawn)
    }

    @Test
    fun testHorizontalFadingEdgeZeroDimensionResilience() {
        val mod = Modifier.horizontalFadingEdge(16.dp, 16.dp)
        val onDraw = extractOnDrawLambda(mod)

        // Zero size: 0 x 50
        val zeroWidth = executeRealDraw(onDraw, Size(0f, 50f))
        assertTrue("drawContent must be called even on 0-width container", zeroWidth.contentDrawn)

        // Zero size: 0 x 0
        val zeroAll = executeRealDraw(onDraw, Size.Zero)
        assertTrue("drawContent must be called on 0x0 container", zeroAll.contentDrawn)
    }

    @Test
    fun testHorizontalFadingEdgeContainerSmallerThanFadeWidth() {
        // Container width (8px) is smaller than fade width (16px)
        val mod = Modifier.horizontalFadingEdge(16.dp, 16.dp)
        val onDraw = extractOnDrawLambda(mod)

        val rendered = executeRealDraw(onDraw, Size(8f, 50f))
        assertTrue("Must smoothly handle container width smaller than fade width", rendered.contentDrawn)
    }

    @Test
    fun testHorizontalFadingEdgeExtremeAndBoundaryFadeWidths() {
        val boundaryCases = listOf(
            0.dp to 16.dp,        // One-sided: only end fade
            16.dp to 0.dp,        // One-sided: only start fade
            0.dp to 0.dp,         // No fade
            (-10).dp to 16.dp,    // Negative start fade
            16.dp to (-10).dp,    // Negative end fade
            (-10).dp to (-10).dp, // Both negative
            1000.dp to 1000.dp    // Fade width substantially larger than container
        )

        for ((startFade, endFade) in boundaryCases) {
            val mod = Modifier.horizontalFadingEdge(startFade, endFade)
            val onDraw = extractOnDrawLambda(mod)
            val rendered = executeRealDraw(onDraw, Size(100f, 50f))
            assertTrue("Boundary case ($startFade, $endFade) should execute without error", rendered.contentDrawn)
        }
    }

    @Test
    fun testVerticalFadingEdgeRealCanvasExecution() {
        val mod = Modifier.verticalFadingEdge(16.dp, 16.dp)
        val onDraw = extractOnDrawLambda(mod)

        // Normal size: 50 x 100
        val rendered = executeRealDraw(onDraw, Size(50f, 100f))
        assertTrue("drawContent must be called", rendered.contentDrawn)

        // Zero height
        val zeroHeight = executeRealDraw(onDraw, Size(50f, 0f))
        assertTrue("Zero height must execute without error", zeroHeight.contentDrawn)

        // Height (8px) smaller than fade height (16px)
        val smallHeight = executeRealDraw(onDraw, Size(50f, 8f))
        assertTrue("Height smaller than fade height must execute cleanly", smallHeight.contentDrawn)

        // Asymmetric and negative heights
        val modAsym = Modifier.verticalFadingEdge(topFadeHeight = 0.dp, bottomFadeHeight = 24.dp)
        val onDrawAsym = extractOnDrawLambda(modAsym)
        val renderedAsym = executeRealDraw(onDrawAsym, Size(50f, 100f))
        assertTrue("Asymmetric vertical fade must execute cleanly", renderedAsym.contentDrawn)
    }

    @Test
    fun testResponsiveVerticalFadingEdgeModifierStructure() {
        val mod = Modifier.responsiveVerticalFadingEdge(
            topFadeHeight = 20.dp,
            bottomFadeHeight = 20.dp,
            bottomBarHeightProvider = { 100f },
            navBarOffsetProvider = { 0f }
        )
        val elements = mod.foldIn(mutableListOf<Modifier.Element>()) { acc, elem -> acc.apply { add(elem) } }

        assertEquals(2, elements.size)
        val graphicsElem = elements[0]
        val strategy = getFieldValue(graphicsElem, "compositingStrategy")
        val isOffscreen = when (strategy) {
            is CompositingStrategy -> strategy == CompositingStrategy.Offscreen
            is Int -> strategy == 1
            else -> strategy.toString().contains("1")
        }
        assertTrue("CompositingStrategy must be Offscreen", isOffscreen)

        val drawElem = elements[1]
        assertTrue("Second element should be DrawWithContent element", drawElem.javaClass.simpleName.contains("Draw"))
    }

    @Test
    fun testResponsiveVerticalFadingEdgeDynamicStates() {
        var barHeight = 80f
        var navOffset = 0f

        val mod = Modifier.responsiveVerticalFadingEdge(
            topFadeHeight = 16.dp,
            bottomFadeHeight = 24.dp,
            bottomBarHeightProvider = { barHeight },
            navBarOffsetProvider = { navOffset }
        )
        val onDraw = extractOnDrawLambda(mod)

        // State 1: Nav bar fully visible (offset = 0)
        val visibleDraw = executeRealDraw(onDraw, Size(100f, 200f))
        assertTrue("Visible nav bar draw must execute cleanly", visibleDraw.contentDrawn)

        // State 2: Nav bar half collapsed (offset = 40)
        navOffset = 40f
        val halfCollapsedDraw = executeRealDraw(onDraw, Size(100f, 200f))
        assertTrue("Half-collapsed nav bar draw must execute cleanly", halfCollapsedDraw.contentDrawn)

        // State 3: Nav bar fully collapsed (offset = 120 >= barHeight)
        navOffset = 120f
        val collapsedDraw = executeRealDraw(onDraw, Size(100f, 200f))
        assertTrue("Fully collapsed nav bar draw must execute cleanly", collapsedDraw.contentDrawn)

        // State 4: Edge case - zero size
        val zeroDraw = executeRealDraw(onDraw, Size.Zero)
        assertTrue("Zero size draw must execute cleanly", zeroDraw.contentDrawn)

        // State 5: Container smaller than fade height
        val smallContainerDraw = executeRealDraw(onDraw, Size(100f, 15f))
        assertTrue("Small container draw must execute cleanly", smallContainerDraw.contentDrawn)

        // State 6: Extreme offset / negative offset
        navOffset = -20f
        val negOffsetDraw = executeRealDraw(onDraw, Size(100f, 200f))
        assertTrue("Negative offset draw must execute cleanly", negOffsetDraw.contentDrawn)

        navOffset = 1000f
        val hugeOffsetDraw = executeRealDraw(onDraw, Size(100f, 200f))
        assertTrue("Huge offset draw must execute cleanly", hugeOffsetDraw.contentDrawn)
    }

    private class DrawExecutionResult(val contentDrawn: Boolean)

    private fun executeRealDraw(
        onDraw: ContentDrawScope.() -> Unit,
        size: Size
    ): DrawExecutionResult {
        val canvasDrawScope = CanvasDrawScope()
        val bmpWidth = maxOf(1, size.width.toInt())
        val bmpHeight = maxOf(1, size.height.toInt())
        val bitmap = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888)
        val androidCanvas = AndroidCanvas(bitmap)
        val composeCanvas = Canvas(androidCanvas)

        var contentDrawn = false

        canvasDrawScope.draw(
            density = Density(1f),
            layoutDirection = LayoutDirection.Ltr,
            canvas = composeCanvas,
            size = size
        ) {
            val contentDrawScope = object : ContentDrawScope, DrawScope by this {
                override fun drawContent() {
                    contentDrawn = true
                }
            }
            onDraw.invoke(contentDrawScope)
        }

        return DrawExecutionResult(contentDrawn)
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractOnDrawLambda(modifier: Modifier): ContentDrawScope.() -> Unit {
        val elements = modifier.foldIn(mutableListOf<Modifier.Element>()) { acc, elem -> acc.apply { add(elem) } }
        val drawElem = elements.find { it.javaClass.simpleName.contains("Draw") }
        assertNotNull("Draw element must be present in modifier", drawElem)
        val onDraw = getFieldValue(drawElem!!, "onDraw") as? (ContentDrawScope.() -> Unit)
        assertNotNull("onDraw lambda must be extractable from draw element", onDraw)
        return onDraw!!
    }

    private fun getFieldValue(obj: Any, fieldName: String): Any? {
        var clazz: Class<*>? = obj.javaClass
        while (clazz != null) {
            try {
                val field: Field = clazz.getDeclaredField(fieldName)
                field.isAccessible = true
                return field.get(obj)
            } catch (e: NoSuchFieldException) {
                clazz = clazz.superclass
            }
        }
        return null
    }
}
