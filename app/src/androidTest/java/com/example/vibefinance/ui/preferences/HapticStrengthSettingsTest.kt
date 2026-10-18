package com.example.vibefinance.ui.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vibefinance.ui.*
import com.example.vibefinance.util.HapticStrength
import com.example.vibefinance.util.VibeHaptics
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

/** Settings drawer, discrete slider and preview explored on Waydroid with ARTEMIS before authoring. */
@RunWith(AndroidJUnit4::class)
class HapticStrengthSettingsTest {
    @get:Rule val rule = createComposeRule()
    @After fun reset() { VibeHaptics.enabled = true; VibeHaptics.strength = HapticStrength.STANDARD }

    @Test fun allThreeLevelsDispatchAndPreviewKeepsTheChoice() {
        val changes = mutableListOf<HapticStrength>()
        rule.setContent {
            var state by remember { mutableStateOf(FinanceUiState()) }
            MaterialTheme {
                HapticStrengthSettings(state) { intent ->
                    if (intent is FinanceIntent.SetHapticStrength) {
                        changes += intent.strength
                        VibeHaptics.strength = intent.strength
                        state = state.copy(hapticStrength = intent.strength)
                    }
                }
            }
        }
        for (level in listOf(HapticStrength.GENTLE, HapticStrength.STRONG, HapticStrength.STANDARD)) {
            rule.onNodeWithTag("HapticStrength").performSemanticsAction(SemanticsActions.SetProgress) { it(level.ordinal.toFloat()) }
            rule.waitUntil(5_000) { changes.lastOrNull() == level }
            rule.onNodeWithTag("HapticStrength").assert(SemanticsMatcher.expectValue(
                SemanticsProperties.ProgressBarRangeInfo,
                androidx.compose.ui.semantics.ProgressBarRangeInfo(level.ordinal.toFloat(), 0f..2f, 1)))
            rule.onNodeWithTag("HapticStrengthPreview").performClick()
            rule.runOnIdle { assertEquals(level, VibeHaptics.strength) }
        }
        rule.runOnIdle { assertEquals(listOf(HapticStrength.GENTLE, HapticStrength.STRONG, HapticStrength.STANDARD), changes) }
    }
    @Test fun disabledFeedbackKeepsStrengthAndDisablesBothControls() {
        var dispatches = 0
        rule.setContent { MaterialTheme {
            HapticStrengthSettings(FinanceUiState(hapticFeedbackEnabled = false, hapticStrength = HapticStrength.STRONG)) { dispatches++ }
        } }
        rule.onNodeWithTag("HapticStrength").assertIsNotEnabled()
        rule.onNodeWithTag("HapticStrengthPreview").assertIsNotEnabled()
        rule.onNodeWithTag("HapticStrengthLabel").assertTextEquals("Strong")
        rule.runOnIdle { assertEquals(0, dispatches) }
    }
    @Test fun largeTextDarkLayoutKeepsControlsReachableWithoutChangingDeviceScale() {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.8f)) {
                MaterialTheme(colorScheme = darkColorScheme()) {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        HapticStrengthSettings(FinanceUiState()) {}
                    }
                }
            }
        }
        rule.onNodeWithTag("HapticStrength").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("HapticStrengthPreview").performScrollTo().assertIsDisplayed().assertIsEnabled().performClick()
    }
    @Test fun dialogFeedbackUsesTheSameAppEngine() {
        var feedback: androidx.compose.ui.hapticfeedback.HapticFeedback? = null
        var dialogFeedback: androidx.compose.ui.hapticfeedback.HapticFeedback? = null
        rule.setContent {
            val view = androidx.compose.ui.platform.LocalView.current
            val engine = remember(view) { VibeHaptics.composeFeedback(view) }
            CompositionLocalProvider(androidx.compose.ui.platform.LocalHapticFeedback provides engine) {
                feedback = androidx.compose.ui.platform.LocalHapticFeedback.current
                MaterialTheme {
                    androidx.compose.material3.AlertDialog(onDismissRequest = {}, confirmButton = {}, text = {
                        dialogFeedback = com.example.vibefinance.util.rememberVibeHapticFeedback()
                        androidx.compose.material3.Text("Feedback fixture")
                    })
                }
            }
        }
        rule.waitUntil(5_000) { dialogFeedback != null }
        rule.runOnIdle {
            assertEquals(feedback!!::class.java, dialogFeedback!!::class.java)
            assertNotSame("The dialog uses its own view", feedback, dialogFeedback)
        }
    }
}
