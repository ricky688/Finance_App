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
    fun pressedShape_isPercent32RoundedCornerShape() {
        assertEquals(RoundedCornerShape(percent = 32), AppButtonDefaults.PressedShape)
    }

    @Test
    fun verifyButtonSuiteArchitecturalContracts() {
        val file = resolveSourceFile("src/main/java/com/example/vibefinance/ui/components/CompletePressButtons.kt")
        assertTrue("CompletePressButtons.kt must exist", file.exists())
        val text = file.readText()

        // 1. Verify AppButtonDefaults shapes contract
        assertTrue("Must define RestingShape as CircleShape", text.contains("RestingShape: Shape = CircleShape"))
        assertTrue("Must define PressedShape as RoundedCornerShape(percent = 32)", text.contains("PressedShape: Shape = RoundedCornerShape(percent = 32)"))
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
}
