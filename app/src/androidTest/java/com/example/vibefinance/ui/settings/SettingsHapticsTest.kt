package com.example.vibefinance.ui.settings

import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vibefinance.ui.FinanceIntent
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.components.AppSwitch
import com.example.vibefinance.ui.preferences.MotionBlurSettings
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Settings drawer -> Customization -> Color scheme -> Palette style explored with ARTEMIS/ADB. */
@RunWith(AndroidJUnit4::class)
class SettingsHapticsTest {
    @get:Rule val rule = createComposeRule()
    private val feedback = mutableListOf<Int>()
    private lateinit var view: View

    private fun show(content: @Composable () -> Unit) {
        rule.setContent {
            val context = LocalContext.current
            val owner = LocalView.current
            view = remember(owner) {
                object : View(context) {
                    override fun performHapticFeedback(feedbackConstant: Int): Boolean {
                        feedback += feedbackConstant
                        return true
                    }
                }.apply { isHapticFeedbackEnabled = true }
            }
            // Material ripple needs an attached parent even for a haptic-capturing view.
            DisposableEffect(owner, view) {
                val host = owner.rootView.findViewById<ViewGroup>(android.R.id.content)
                host.addView(view, ViewGroup.LayoutParams(0, 0))
                onDispose { host.removeView(view) }
            }
            CompositionLocalProvider(LocalView provides view) { MaterialTheme { content() } }
        }
    }

    @Test fun cardFeedbackOccursOnceOnClick_neverOnCancelledPressOrRecomposition_andHonorsDisabledView() {
        var clicks by mutableIntStateOf(0)
        show {
            SettingsActionCard({ clicks++ }, RoundedCornerShape(20.dp), MaterialTheme.colorScheme.surfaceContainer,
                Modifier.testTag("SettingsCard")) {
                Text("Option $clicks", Modifier.padding(24.dp))
            }
        }
        val card = rule.onNodeWithTag("SettingsCard")
        card.performTouchInput { click() }
        rule.waitForIdle()
        assertEquals(1, clicks); assertEquals(1, feedback.size)
        card.performTouchInput { down(center); advanceEventTime(100); cancel() }
        rule.waitForIdle()
        assertEquals(1, clicks); assertEquals(1, feedback.size)
        rule.runOnIdle { clicks = 10 }
        assertEquals(1, feedback.size)
        rule.runOnIdle { view.isHapticFeedbackEnabled = false }
        card.performClick()
        assertEquals(11, clicks); assertEquals(1, feedback.size)
    }

    @Test fun nestedSwitchKeepsItsSingleFeedback_withoutClickingTheParentCard() {
        var cardClicks = 0; var changes = 0; var switchFeedback = 0
        val switchHaptic = object : HapticFeedback {
            override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) { switchFeedback++ }
        }
        show {
            CompositionLocalProvider(LocalHapticFeedback provides switchHaptic) {
                var checked by remember { mutableStateOf(false) }
                SettingsActionCard({ cardClicks++ }, RoundedCornerShape(20.dp), MaterialTheme.colorScheme.surfaceContainer) {
                    AppSwitch(checked, { checked = it; changes++ }, Modifier.padding(16.dp).testTag("SettingsSwitch"))
                }
            }
        }
        rule.onNodeWithTag("SettingsSwitch").performTouchInput { click() }
        rule.waitForIdle()
        assertEquals(1, changes); assertEquals(1, switchFeedback)
        assertEquals(0, cardClicks); assertTrue(feedback.isEmpty())
    }

    @Test fun motionChoiceAndCompletedSliderGestureEachGiveOneFeedback() {
        val intents = mutableListOf<FinanceIntent>()
        show { MotionBlurSettings(FinanceUiState(isLoading = false), { intents += it }) }
        rule.onNodeWithTag("Motion_MINIMAL").performClick()
        assertEquals(1, feedback.size)
        assertTrue(intents.single() is FinanceIntent.SetMotionLevel)
        rule.onNodeWithTag("BlurIntensity").performTouchInput {
            swipe(Offset(width * .8f, center.y), Offset(width * .2f, center.y), 500)
        }
        rule.waitForIdle()
        assertEquals(2, feedback.size)
        assertEquals(1, intents.filterIsInstance<FinanceIntent.SetBlurIntensity>().size)
    }
}
