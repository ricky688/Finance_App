@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.theme

import androidx.compose.material3.MaterialShapes
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** ARTEMIS explored Settings -> Customization -> Icon shape before this suite was authored. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class IconShapeModeTest {
    @Test
    fun catalogUsesOnlyTheRequestedOfficialLibraryShapes() {
        val expected = listOf(
            MaterialShapes.Circle, MaterialShapes.Square, MaterialShapes.Slanted,
            MaterialShapes.Arch, MaterialShapes.SemiCircle, MaterialShapes.Oval, MaterialShapes.Pill,
            MaterialShapes.Triangle, MaterialShapes.Arrow, MaterialShapes.Fan, MaterialShapes.Diamond,
            MaterialShapes.ClamShell, MaterialShapes.Pentagon, MaterialShapes.Gem,
            MaterialShapes.Sunny, MaterialShapes.VerySunny,
            MaterialShapes.Cookie4Sided, MaterialShapes.Cookie6Sided, MaterialShapes.Cookie7Sided,
            MaterialShapes.Cookie9Sided, MaterialShapes.Cookie12Sided,
            MaterialShapes.Clover4Leaf, MaterialShapes.Clover8Leaf,
            MaterialShapes.Ghostish, MaterialShapes.Bun
        )
        assertEquals(expected, IconShapeMode.roundedModes.map { it.materialShape })
        val excluded = listOf(MaterialShapes.Burst, MaterialShapes.SoftBurst, MaterialShapes.Boom,
            MaterialShapes.SoftBoom, MaterialShapes.Flower, MaterialShapes.Puffy,
            MaterialShapes.PuffyDiamond, MaterialShapes.PixelCircle, MaterialShapes.PixelTriangle,
            MaterialShapes.Heart)
        assertTrue(expected.none { it in excluded })
    }

    @Test
    fun savedLegacyShapesMigrateIntoTheAllowedCatalog() {
        val migration = mapOf(
            "CLOVER" to IconShapeMode.COOKIE_4,
            "SQUIRCLE" to IconShapeMode.SQUARE,
            "ROUNDED_SQUARE" to IconShapeMode.SQUARE,
            "SCALLOP" to IconShapeMode.COOKIE_12,
            "ROUNDED_HEXAGON" to IconShapeMode.CLAMSHELL,
            "EGG" to IconShapeMode.OVAL,
            "SHIELD" to IconShapeMode.ARCH,
            "STAR" to IconShapeMode.COOKIE_4,
            "FLOWER" to IconShapeMode.COOKIE_4,
            "PUFFY" to IconShapeMode.COOKIE_4,
            "PUFFY_DIAMOND" to IconShapeMode.COOKIE_4,
            "HEART" to IconShapeMode.COOKIE_4
        )
        migration.forEach { (old, new) -> assertEquals(new, IconShapeMode.fromStoredName(old)) }
        IconShapeMode.entries.forEach { assertEquals(it, IconShapeMode.fromStoredName(it.name)) }
        assertEquals(IconShapeMode.COOKIE_4, IconShapeMode.fromStoredName(null))
        assertEquals(IconShapeMode.COOKIE_4, IconShapeMode.fromStoredName("unknown"))
    }

    @Test
    fun randomAssignmentRemainsStableWhenItemsAreReordered() {
        val keys = (1..200).map { "history.$it" } + listOf(
            "settings.palette", "settings.dynamic-color", "settings.pure-black", "settings.icon-shape"
        )
        val before = keys.associateWith(IconShapeMode::roundedModeFor)
        val after = keys.reversed().associateWith(IconShapeMode::roundedModeFor)
        assertEquals(before, after)
        assertFalse(before.values.contains(IconShapeMode.RANDOM))
        assertTrue("Random mode must visibly mix silhouettes", before.values.toSet().size > 10)
    }

    @Test
    fun sharedShapeDoesNotAccumulateTransformsAcrossPreviewAndIconSizes() {
        val density = Density(1f)
        val iconSize = Size(44f, 44f)
        IconShapeMode.roundedModes.forEach { mode ->
            val shape = mode.shape
            val initial = shape.createOutline(iconSize, LayoutDirection.Ltr, density).bounds
            listOf(Size(13f, 13f), Size(54f, 54f), Size(38f, 38f), Size.Zero).forEach { size ->
                val bounds = shape.createOutline(size, LayoutDirection.Ltr, density).bounds
                assertTrue("$mode: outline exceeds $size", bounds.width <= size.width + 0.01f
                    && bounds.height <= size.height + 0.01f)
                assertTrue("$mode: invalid outline", bounds.left.isFinite() && bounds.top.isFinite())
            }
            val restored = shape.createOutline(iconSize, LayoutDirection.Rtl, density).bounds
            assertEquals("$mode: a smaller preview must not mutate a shared shape", initial, restored)
            assertTrue("$mode: empty outline", initial.width > 0 && initial.height > 0)
        }
    }
}
