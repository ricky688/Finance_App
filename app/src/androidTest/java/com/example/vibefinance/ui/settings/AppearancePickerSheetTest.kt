package com.example.vibefinance.ui.settings

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vibefinance.theme.AppearancePalette
import com.example.vibefinance.theme.PaletteStyle
import com.example.vibefinance.ui.FinanceIntent
import com.example.vibefinance.ui.FinanceUiState
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CopyOnWriteArrayList

/** Style selection, editing and horizontal custom-slot scrolling explored with ARTEMIS first.
 * State and save callbacks are memory-only; these checks never change the user's preferences.
 */
@RunWith(AndroidJUnit4::class)
class AppearancePickerSheetTest {
    @get:Rule val rule = createComposeRule()
    private val intents = CopyOnWriteArrayList<FinanceIntent>()
    private var state by mutableStateOf(FinanceUiState(isLoading = false))

    private fun show(fontScale: Float = 1f): StateRestorationTester {
        val restoration = StateRestorationTester(rule)
        restoration.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                MaterialTheme {
                    AppearancePickerSheet(state, onIntent = { intent ->
                        intents += intent
                        state = when (intent) {
                            is FinanceIntent.SetPaletteStyle -> state.copy(paletteStyle = intent.style, dynamicColorEnabled = false)
                            is FinanceIntent.SetPaletteSeed -> state.copy(appearancePalette = intent.palette,
                                paletteSeeds = state.paletteSeeds + (intent.palette to intent.seed), dynamicColorEnabled = false)
                            else -> state
                        }
                    }, onDismiss = {})
                }
            }
        }
        waitFor("PaletteStyleOpen")
        return restoration
    }

    private fun waitFor(tag: String) {
        rule.waitUntil(5_000) { rule.onAllNodesWithTag(tag).fetchSemanticsNodes().size == 1 }
        rule.waitForIdle()
    }
    private fun click(tag: String) {
        val node = rule.onNodeWithTag(tag)
        try { node.assertIsDisplayed() } catch (_: AssertionError) { node.performScrollTo() }
        node.performClick()
        rule.waitForIdle()
    }
    private fun edit(hex: String) {
        rule.onNodeWithTag("PaletteHex").performScrollTo().performTextReplacement(hex)
        rule.waitForIdle()
    }

    @Test fun styleChooser_selectsEachStyleOnce_andShowsCurrentSelection() {
        show()
        PaletteStyle.entries.forEach { style ->
            click("PaletteStyleOpen")
            waitFor("PaletteStyle_${style.name}")
            click("PaletteStyle_${style.name}")
            assertEquals(style, state.paletteStyle)
            assertFalse(state.dynamicColorEnabled)
            rule.onNodeWithTag("PaletteStyle_${style.name}").assertDoesNotExist()
        }
        assertEquals(PaletteStyle.entries.map { FinanceIntent.SetPaletteStyle(it) }, intents.toList())
    }

    @Test fun variantEditor_rejectsInvalidHex_cancelDiscardsDraft_andSaveUpdatesSelectedVariant() {
        show()
        click("PaletteEdit")
        waitFor("PaletteHex")
        edit("not-a-color")
        rule.onNodeWithTag("PaletteVariantSave").assertIsNotEnabled()
        click("PaletteVariantCancel")
        assertTrue(intents.isEmpty())
        click("PaletteEdit")
        waitFor("PaletteHex")
        rule.onNodeWithTag("PaletteHex").assertTextContains("006C4C")
        rule.onNodeWithTag("PaletteChannel_8").performScrollTo()
            .performSemanticsAction(SemanticsActions.SetProgress) { it(255f) }
        rule.onNodeWithTag("PaletteHex").performScrollTo().assertTextContains("00FF4C")
        edit("3355aa")
        click("PaletteVariantSave")
        assertEquals(listOf(FinanceIntent.SetPaletteSeed(AppearancePalette.ORIGINAL, 0xFF3355AA.toInt())), intents.toList())
        assertEquals(0xFF3355AA.toInt(), state.paletteSeeds[AppearancePalette.ORIGINAL])
        click("PaletteEdit")
        waitFor("PaletteHex")
        rule.onNodeWithTag("PaletteHex").assertTextContains("3355AA")
    }

    @Test fun customVariant_draftSurvivesRecreation_andSavesAtLargeFontWithoutChangingDeviceScale() {
        val restoration = show(fontScale = 1.5f)
        click("PaletteAddCustom")
        waitFor("PaletteHex")
        edit("A12BCD")
        restoration.emulateSavedInstanceStateRestore()
        waitFor("PaletteHex")
        rule.onNodeWithTag("PaletteHex").assertTextContains("A12BCD")
        assertTrue(intents.isEmpty())
        click("PaletteVariantSave")
        assertEquals(listOf(FinanceIntent.SetPaletteSeed(AppearancePalette.CUSTOM, 0xFFA12BCD.toInt())), intents.toList())
        rule.onNodeWithTag("PaletteVariant_CUSTOM").performScrollTo().assertIsSelected()
        click("PaletteEdit")
        waitFor("PaletteHex")
        rule.onNodeWithTag("PaletteHex").assertTextContains("A12BCD")
    }
}
