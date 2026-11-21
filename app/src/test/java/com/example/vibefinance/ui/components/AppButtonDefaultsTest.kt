@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.components

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AppButtonDefaultsTest {

    private fun resolveSourceFile(relativePath: String): File {
        return listOf(
            File(relativePath),
            File("app", relativePath),
            File("..", relativePath),
            File("../app", relativePath)
        ).firstOrNull { it.exists() } ?: File(relativePath)
    }

    @Test
    fun restingShape_isStadiumCircleShape() {
        assertSame(CircleShape, AppButtonDefaults.RestingShape)
    }

    @Test
    fun pressedShape_is8dpRoundedCornerShape() {
        assertEquals(androidx.compose.foundation.shape.RoundedCornerShape(androidx.compose.ui.unit.Dp(8f)), AppButtonDefaults.PressedShape)
        assertEquals(androidx.compose.foundation.shape.RoundedCornerShape(androidx.compose.ui.unit.Dp(12f)), AppButtonDefaults.LargePressedShape)
    }

    @Test
    fun verifyButtonSuiteArchitecturalContracts() {
        val file = resolveSourceFile("src/main/java/com/example/vibefinance/ui/components/CompletePressButtons.kt")
        assertTrue("CompletePressButtons.kt must exist", file.exists())
        val text = file.readText()

        // 1. Verify AppButtonDefaults shapes contract
        assertTrue("Must define RestingShape as CircleShape", text.contains("RestingShape: Shape = CircleShape"))
        assertTrue("Must define PressedShape as RoundedCornerShape(8.dp)", text.contains("PressedShape: Shape = RoundedCornerShape(8.dp)"))
        assertTrue("Must define LargePressedShape as RoundedCornerShape(12.dp)", text.contains("LargePressedShape: Shape = RoundedCornerShape(12.dp)"))
        assertTrue("Must provide shapes() builder", text.contains("fun shapes("))

        // 2. Verify LocalAppButtonShapes CompositionLocal
        assertTrue("Must define LocalAppButtonShapes", text.contains("val LocalAppButtonShapes = staticCompositionLocalOf<ButtonShapes?> { null }"))

        // 3. Verify public button suite overloads
        assertTrue("Must provide non-experimental AppButton", text.contains("fun AppButton("))
        assertTrue("Must provide non-experimental AppFilledTonalButton", text.contains("fun AppFilledTonalButton("))
        assertTrue("Must provide non-experimental AppOutlinedButton", text.contains("fun AppOutlinedButton("))
        assertTrue("Must provide non-experimental AppTextButton", text.contains("fun AppTextButton("))

        // 4. Verify default shapes resolution
        assertTrue("Must use defaultButtonShapes() fallback", text.contains("LocalAppButtonShapes.current ?: AppButtonDefaults.shapes()"))
    }

    @Test
    fun connectedButtonCornersAndListRowTargetCorners_followStandardSpec() {
        val standard = com.example.vibefinance.ui.main.ConnectedButtonCorners.Standard
        assertEquals(androidx.compose.ui.unit.Dp(24f), standard.selected)
        assertEquals(androidx.compose.ui.unit.Dp(24f), standard.outer)
        assertEquals(androidx.compose.ui.unit.Dp(8f), standard.inner)
        assertEquals(androidx.compose.ui.unit.Dp(6f), standard.pressed)

        val density = androidx.compose.ui.unit.Density(1f)
        val heights = listOf(40f, 48f, 58f, 72f)
        heights.forEach { h ->
            val size = androidx.compose.ui.geometry.Size(320f, h)
            val halfPill = h * 0.5f

            fun radiiFor(isFirst: Boolean, isLast: Boolean, isSelected: Boolean = false, isPressed: Boolean = false): Pair<Float, Float> {
                val target = com.example.vibefinance.ui.main.connectedListRowCornerTarget(
                    isFirst = isFirst,
                    isLast = isLast,
                    isSelected = isSelected,
                    isPressed = isPressed,
                )
                val shape = com.example.vibefinance.ui.main.connectedListRowShape(
                    topPillFraction = if (target.topIsFullPill) 1f else 0f,
                    bottomPillFraction = if (target.bottomIsFullPill) 1f else 0f,
                    innerDp = target.innerDp,
                )
                val outline = shape.createOutline(size, androidx.compose.ui.unit.LayoutDirection.Ltr, density)
                    as androidx.compose.ui.graphics.Outline.Rounded
                val rr = outline.roundRect
                return rr.topLeftCornerRadius.x to rr.bottomLeftCornerRadius.x
            }

            // First item at rest: 50% full pill top, 8.dp bottom
            assertEquals(halfPill to 8f, radiiFor(isFirst = true, isLast = false))
            // Middle item at rest: 8.dp on all four corners
            assertEquals(8f to 8f, radiiFor(isFirst = false, isLast = false))
            // Last item at rest: 8.dp top, 50% full pill bottom
            assertEquals(8f to halfPill, radiiFor(isFirst = false, isLast = true))
            // Single item at rest: 50% full pill on all four corners
            assertEquals(halfPill to halfPill, radiiFor(isFirst = true, isLast = true))

            // Selected item at any position: 50% full pill on all four corners (20/24/29/36.dp)
            assertEquals(halfPill to halfPill, radiiFor(isFirst = true, isLast = false, isSelected = true))
            assertEquals(halfPill to halfPill, radiiFor(isFirst = false, isLast = false, isSelected = true))
            assertEquals(halfPill to halfPill, radiiFor(isFirst = false, isLast = true, isSelected = true))

            // Pressed: inner seams tighten to 4.dp while outer ends stay 50% pill
            assertEquals(halfPill to 4f, radiiFor(isFirst = true, isLast = false, isPressed = true))
            assertEquals(4f to 4f, radiiFor(isFirst = false, isLast = false, isPressed = true))
            assertEquals(4f to halfPill, radiiFor(isFirst = false, isLast = true, isPressed = true))
            assertEquals(4f to 4f, radiiFor(isFirst = false, isLast = false, isSelected = true, isPressed = true))
        }
    }
}
