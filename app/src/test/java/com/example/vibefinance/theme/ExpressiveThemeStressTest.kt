@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpressiveThemeStressTest {

    private val density = Density(1.0f)
    private val testSize = Size(200f, 100f)

    @Test
    fun testExpressiveShapesTokensCornerRadius() {
        val extraSmall = ExpressiveShapes.extraSmall as CornerBasedShape
        val small = ExpressiveShapes.small as CornerBasedShape
        val medium = ExpressiveShapes.medium as CornerBasedShape
        val large = ExpressiveShapes.large as CornerBasedShape
        val extraLarge = ExpressiveShapes.extraLarge as CornerBasedShape

        assertEquals(4f, extraSmall.topStart.toPx(testSize, density), 0.01f)
        assertEquals(8f, small.topStart.toPx(testSize, density), 0.01f)
        assertEquals(12f, medium.topStart.toPx(testSize, density), 0.01f)
        assertEquals(16f, large.topStart.toPx(testSize, density), 0.01f)
        assertEquals(28f, extraLarge.topStart.toPx(testSize, density), 0.01f)
        assertEquals(20f, ExpressiveShapes.largeIncreased.topStart.toPx(testSize, density), 0.01f)
        assertEquals(32f, ExpressiveShapes.extraLargeIncreased.topStart.toPx(testSize, density), 0.01f)
        assertEquals(48f, ExpressiveShapes.extraExtraLarge.topStart.toPx(testSize, density), 0.01f)
        assertEquals(50f, MaterialCornerScale.full.topStart.toPx(testSize, density), 0.01f)
    }

    @Test
    fun testBentoAndContainerShapes() {
        val bentoCard = BentoCardShape as CornerBasedShape
        val bentoSubCard = BentoSubCardShape as CornerBasedShape
        val bentoSmallCard = BentoSmallCardShape as CornerBasedShape
        val heroCard = HeroCardShape as CornerBasedShape
        val floatingBar = FloatingBarShape as CornerBasedShape
        val sheetShape = ExpressiveSheetShape as CornerBasedShape

        assertEquals(28f, bentoCard.topStart.toPx(testSize, density), 0.01f)
        assertEquals(20f, bentoSubCard.topStart.toPx(testSize, density), 0.01f)
        assertEquals(16f, bentoSmallCard.topStart.toPx(testSize, density), 0.01f)
        assertEquals(28f, heroCard.topStart.toPx(testSize, density), 0.01f)
        assertEquals(32f, floatingBar.topStart.toPx(testSize, density), 0.01f)

        // ExpressiveSheetShape should only round the top corners
        assertEquals(28f, sheetShape.topStart.toPx(testSize, density), 0.01f)
        assertEquals(28f, sheetShape.topEnd.toPx(testSize, density), 0.01f)
        assertEquals(0f, sheetShape.bottomStart.toPx(testSize, density), 0.01f)
        assertEquals(0f, sheetShape.bottomEnd.toPx(testSize, density), 0.01f)

        assertEquals(CircleShape, PillShape)
        assertTrue(ExpressivePillShape is RoundedCornerShape)
    }

    @Test
    fun testShapeOutlineStressUnderEdgeDimensions() {
        val testDimensions = listOf(
            Size.Zero,
            Size(1f, 1f),
            Size(5f, 20f),
            Size(24f, 24f),
            Size(100f, 100f),
            Size(1000f, 500f),
            Size(5000f, 10000f)
        )

        val shapes = listOf(
            ExpressiveShapes.extraSmall,
            ExpressiveShapes.small,
            ExpressiveShapes.medium,
            ExpressiveShapes.large,
            ExpressiveShapes.extraLarge,
            ExpressiveShapes.largeIncreased,
            ExpressiveShapes.extraLargeIncreased,
            ExpressiveShapes.extraExtraLarge,
            MaterialCornerScale.full,
            BentoCardShape,
            BentoSubCardShape,
            BentoSmallCardShape,
            HeroCardShape,
            PillShape,
            ExpressivePillShape,
            ExpressiveSheetShape,
            FloatingBarShape
        )

        for (size in testDimensions) {
            for (shape in shapes) {
                val outline = shape.createOutline(size, LayoutDirection.Ltr, density)
                assertNotNull(outline)
                val bounds = outline.bounds
                assertFalse("Bounds left should not be NaN for size $size", bounds.left.isNaN())
                assertFalse("Bounds top should not be NaN for size $size", bounds.top.isNaN())
                assertFalse("Bounds width should not be NaN for size $size", bounds.width.isNaN())
                assertFalse("Bounds height should not be NaN for size $size", bounds.height.isNaN())
                assertTrue("Bounds width must match or be bounded by size width", bounds.width <= size.width + 0.01f)
                assertTrue("Bounds height must match or be bounded by size height", bounds.height <= size.height + 0.01f)
            }
        }
    }

    @Test
    fun testTypographyAll15TokensPresentAndComplete() {
        val styles = listOf(
            "displayLarge" to Typography.displayLarge,
            "displayMedium" to Typography.displayMedium,
            "displaySmall" to Typography.displaySmall,
            "headlineLarge" to Typography.headlineLarge,
            "headlineMedium" to Typography.headlineMedium,
            "headlineSmall" to Typography.headlineSmall,
            "titleLarge" to Typography.titleLarge,
            "titleMedium" to Typography.titleMedium,
            "titleSmall" to Typography.titleSmall,
            "bodyLarge" to Typography.bodyLarge,
            "bodyMedium" to Typography.bodyMedium,
            "bodySmall" to Typography.bodySmall,
            "labelLarge" to Typography.labelLarge,
            "labelMedium" to Typography.labelMedium,
            "labelSmall" to Typography.labelSmall
        )

        assertEquals("Typography must define all 15 M3 tokens", 15, styles.size)

        for ((name, style) in styles) {
            assertNotNull("$name must not be null", style)
            assertTrue("$name font size must be specified", style.fontSize.isSpecified)
            assertTrue("$name font size must be positive", style.fontSize.value > 0f)
            assertTrue("$name line height must be specified", style.lineHeight.isSpecified)
            assertTrue("$name line height must be >= font size", style.lineHeight.value >= style.fontSize.value)
            assertNotNull("$name font family must be assigned", style.fontFamily)
            assertNotNull("$name font weight must be assigned", style.fontWeight)
        }
    }

    @Test
    fun testTypographyTokenSpecificationValues() {
        // Display Tokens (JetBrains Mono, Bold)
        assertEquals(42.sp, Typography.displayLarge.fontSize)
        assertEquals(52.sp, Typography.displayLarge.lineHeight)
        assertEquals(JetBrainsMonoFontFamily, Typography.displayLarge.fontFamily)
        assertEquals(FontWeight.Bold, Typography.displayLarge.fontWeight)

        assertEquals(32.sp, Typography.displayMedium.fontSize)
        assertEquals(40.sp, Typography.displayMedium.lineHeight)
        assertEquals(JetBrainsMonoFontFamily, Typography.displayMedium.fontFamily)
        assertEquals(FontWeight.Bold, Typography.displayMedium.fontWeight)

        assertEquals(28.sp, Typography.displaySmall.fontSize)
        assertEquals(36.sp, Typography.displaySmall.lineHeight)
        assertEquals(JetBrainsMonoFontFamily, Typography.displaySmall.fontFamily)
        assertEquals(FontWeight.Bold, Typography.displaySmall.fontWeight)

        // Headline Tokens (Inter, SemiBold)
        assertEquals(32.sp, Typography.headlineLarge.fontSize)
        assertEquals(40.sp, Typography.headlineLarge.lineHeight)
        assertEquals(InterFontFamily, Typography.headlineLarge.fontFamily)
        assertEquals(FontWeight.SemiBold, Typography.headlineLarge.fontWeight)

        assertEquals(24.sp, Typography.headlineMedium.fontSize)
        assertEquals(32.sp, Typography.headlineMedium.lineHeight)
        assertEquals(InterFontFamily, Typography.headlineMedium.fontFamily)
        assertEquals(FontWeight.SemiBold, Typography.headlineMedium.fontWeight)

        assertEquals(20.sp, Typography.headlineSmall.fontSize)
        assertEquals(28.sp, Typography.headlineSmall.lineHeight)
        assertEquals(InterFontFamily, Typography.headlineSmall.fontFamily)
        assertEquals(FontWeight.SemiBold, Typography.headlineSmall.fontWeight)

        // Title Tokens (Inter, Medium)
        assertEquals(20.sp, Typography.titleLarge.fontSize)
        assertEquals(28.sp, Typography.titleLarge.lineHeight)
        assertEquals(InterFontFamily, Typography.titleLarge.fontFamily)
        assertEquals(FontWeight.Medium, Typography.titleLarge.fontWeight)

        assertEquals(16.sp, Typography.titleMedium.fontSize)
        assertEquals(24.sp, Typography.titleMedium.lineHeight)
        assertEquals(InterFontFamily, Typography.titleMedium.fontFamily)
        assertEquals(FontWeight.Medium, Typography.titleMedium.fontWeight)

        assertEquals(14.sp, Typography.titleSmall.fontSize)
        assertEquals(20.sp, Typography.titleSmall.lineHeight)
        assertEquals(InterFontFamily, Typography.titleSmall.fontFamily)
        assertEquals(FontWeight.Medium, Typography.titleSmall.fontWeight)

        // Body Tokens (Inter, Normal)
        assertEquals(16.sp, Typography.bodyLarge.fontSize)
        assertEquals(24.sp, Typography.bodyLarge.lineHeight)
        assertEquals(InterFontFamily, Typography.bodyLarge.fontFamily)
        assertEquals(FontWeight.Normal, Typography.bodyLarge.fontWeight)

        assertEquals(14.sp, Typography.bodyMedium.fontSize)
        assertEquals(20.sp, Typography.bodyMedium.lineHeight)
        assertEquals(InterFontFamily, Typography.bodyMedium.fontFamily)
        assertEquals(FontWeight.Normal, Typography.bodyMedium.fontWeight)

        assertEquals(12.sp, Typography.bodySmall.fontSize)
        assertEquals(16.sp, Typography.bodySmall.lineHeight)
        assertEquals(InterFontFamily, Typography.bodySmall.fontFamily)
        assertEquals(FontWeight.Normal, Typography.bodySmall.fontWeight)

        // Label Tokens
        assertEquals(14.sp, Typography.labelLarge.fontSize)
        assertEquals(20.sp, Typography.labelLarge.lineHeight)
        assertEquals(InterFontFamily, Typography.labelLarge.fontFamily)
        assertEquals(FontWeight.Medium, Typography.labelLarge.fontWeight)

        assertEquals(12.sp, Typography.labelMedium.fontSize)
        assertEquals(16.sp, Typography.labelMedium.lineHeight)
        assertEquals(InterFontFamily, Typography.labelMedium.fontFamily)
        assertEquals(FontWeight.Medium, Typography.labelMedium.fontWeight)

        assertEquals(12.sp, Typography.labelSmall.fontSize)
        assertEquals(16.sp, Typography.labelSmall.lineHeight)
        assertEquals(JetBrainsMonoFontFamily, Typography.labelSmall.fontFamily)
        assertEquals(FontWeight.Medium, Typography.labelSmall.fontWeight)
    }

    @Test
    fun testFontFamilyAliases() {
        assertEquals(Inter, InterFontFamily)
        assertEquals(JetBrainsMono, JetBrainsMonoFontFamily)
    }
}
