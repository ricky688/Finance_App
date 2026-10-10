package com.example.vibefinance.theme

import androidx.compose.ui.graphics.toArgb
import com.example.vibefinance.ui.settings.parsePaletteHex
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppearancePaletteTest {
    @Test fun stylesGenerateDistinctTonalRoles_inBothThemesAndAllContrastLevels() {
        listOf(false, true).forEach { dark ->
            (-1..1).forEach { contrast ->
                val schemes = PaletteStyle.entries.map { paletteColorScheme(AppearancePalette.SUNSET, dark, contrast, it) }
                assertTrue(schemes.map { Triple(it.primary, it.secondary, it.tertiary) }.distinct().size >= 5)
                schemes.forEach {
                    assertEquals(1f, it.primary.alpha, 0f)
                    assertEquals(1f, it.surface.alpha, 0f)
                    assertNotEquals(it.primary, it.onPrimary)
                    assertNotEquals(it.surface, it.onSurface)
                }
                val mono = schemes[PaletteStyle.MONOCHROME.ordinal].primary.toArgb()
                assertEquals((mono ushr 16) and 255, (mono ushr 8) and 255)
                assertEquals((mono ushr 8) and 255, mono and 255)
            }
        }
    }

    @Test fun editedSeedChangesScheme_withoutChangingBuiltinPreset() {
        val default = paletteColorScheme(AppearancePalette.ORIGINAL, false, 0)
        val edited = paletteColorScheme(AppearancePalette.ORIGINAL, false, 0, seedArgb = 0xFF3355AA.toInt())
        assertNotEquals(default.primary, edited.primary)
        assertNotEquals(default.tertiary, edited.tertiary)
        assertEquals(default.primary, paletteColorScheme(AppearancePalette.ORIGINAL, false, 0).primary)
    }

    @Test fun seedOverridesRoundTrip_andRejectUnknownOrNonOpaqueValues() {
        val seeds = mapOf(AppearancePalette.ORIGINAL to 0xFF3355AA.toInt(), AppearancePalette.CUSTOM to 0xFFA12BCD.toInt())
        assertEquals(seeds, decodePaletteSeeds(encodePaletteSeeds(seeds)))
        assertTrue(decodePaletteSeeds("{}").isEmpty())
        listOf("{\"UNKNOWN\":-1}", "{\"ORIGINAL\":1}", "{\"CUSTOM\":-1.5}", "{\"CUSTOM\":\"blue\"}", "[]", "bad").forEach {
            assertThrows(Exception::class.java) { decodePaletteSeeds(it) }
        }
    }

    @Test fun hexEditorRequiresSixDigits_andAcceptsHashAndMixedCase() {
        assertEquals(0xFF3355AA.toInt(), parsePaletteHex("3355aA"))
        assertEquals(0xFF006C4C.toInt(), parsePaletteHex("#006c4c"))
        listOf("", "123", "FF3355AA", "GGGGGG", " 3355AA", "3355AA ", "＃3355AA").forEach { assertNull(parsePaletteHex(it)) }
    }
}
