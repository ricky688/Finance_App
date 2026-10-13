@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vibefinance.R
import com.example.vibefinance.ui.components.AppSwitch
import com.example.vibefinance.ui.preferences.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

/** ARTEMIS/ADB verified Settings -> Smart logging -> toggle -> recovery -> Android access -> Back. */
@RunWith(AndroidJUnit4::class)
class AutoLoggingPanelTest {
    @get:Rule val rule = createComposeRule()
    private var enabled by mutableStateOf(false)
    private var accessRequests = 0

    private fun show(motion: MotionLevel = MotionLevel.FULL, chineseLargeDark: Boolean = false) {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            val density = LocalDensity.current
            val configuration = Configuration(LocalConfiguration.current).apply {
                setLocale(Locale.forLanguageTag(if (chineseLargeDark) "zh-Hant-HK" else "en"))
            }
            val context = LocalContext.current.createConfigurationContext(configuration)
            CompositionLocalProvider(LocalExperience provides ExperiencePreferences(motion = motion),
                LocalConfiguration provides configuration, LocalContext provides context,
                LocalDensity provides Density(density.density, if (chineseLargeDark) 1.5f else density.fontScale)) {
                MaterialTheme(colorScheme = if (chineseLargeDark) darkColorScheme() else lightColorScheme(),
                    motionScheme = MotionScheme.expressive()) {
                    Column(Modifier.width(300.dp).background(MaterialTheme.colorScheme.background)) {
                        AppSwitch(enabled, { enabled = it }, Modifier.testTag("LoggingSwitchFixture"))
                        AutoLoggingPanel(enabled, { accessRequests++ }) {
                            Box(Modifier.fillMaxWidth().height(180.dp).background(Color.Red)
                                .testTag("LoggingAppsFixture"))
                        }
                    }
                }
            }
        }
        advance(100)
    }
    private fun advance(ms: Long) { rule.mainClock.advanceTimeBy(ms); rule.waitForIdle() }
    private fun toggle() = rule.onNodeWithTag("LoggingSwitchFixture").performClick()
    private fun height() = rule.onNodeWithTag("AutoLoggingDetails").fetchSemanticsNode().boundsInRoot.height

    @Test fun fullMotionExpandsAndShrinksOverTime_andRapidReversalKeepsFinalState() {
        show()
        rule.onNodeWithTag("LoggingAppsFixture").assertDoesNotExist()
        rule.onNodeWithTag("AutoLoggingRecoveryGuide").assertIsDisplayed()
        toggle(); advance(80)
        val entering = height()
        advance(2_000)
        val full = height()
        assertTrue("Opening must pass through an intermediate height", entering > 0f && entering < full)
        toggle(); advance(80)
        val closing = height()
        assertTrue("Closing must pass through an intermediate height", closing > 0f && closing < full)
        toggle(); advance(2_000)
        rule.onNodeWithTag("LoggingAppsFixture").assertIsDisplayed()
        assertEquals(full, height(), 1f)
        toggle(); advance(2_000)
        rule.onNodeWithTag("LoggingAppsFixture").assertDoesNotExist()
        rule.onNodeWithTag("AutoLoggingRecoveryGuide").assertIsDisplayed()
        rule.onNodeWithTag("AutoLoggingNotificationAccess").performClick()
        assertEquals(1, accessRequests)
    }

    @Test fun reducedMotionFadesWithoutAnimatingHeight() {
        show(MotionLevel.REDUCED)
        toggle(); advance(32)
        val initial = height()
        advance(2_000)
        assertEquals(height(), initial, 1f)
        toggle(); advance(16)
        rule.onNodeWithTag("LoggingAppsFixture").assertExists()
        advance(2_000)
        rule.onNodeWithTag("LoggingAppsFixture").assertDoesNotExist()
    }

    @Test fun minimalMotionChangesImmediately_andKeepsRecoveryReachable() {
        show(MotionLevel.MINIMAL)
        toggle(); advance(32)
        rule.onNodeWithTag("LoggingAppsFixture").assertIsDisplayed()
        toggle(); advance(32)
        rule.onNodeWithTag("LoggingAppsFixture").assertDoesNotExist()
        rule.onNodeWithTag("AutoLoggingNotificationAccess").assertIsDisplayed().performClick()
        assertEquals(1, accessRequests)
    }

    @Test fun traditionalChineseGuideFitsLargerFontsAndDarkTheme_withoutEnablingLogging() {
        show(chineseLargeDark = true)
        rule.onNodeWithText("自動記帳疑難排解").assertIsDisplayed()
        rule.onNodeWithTag("AutoLoggingNotificationAccess").assertIsDisplayed().performClick()
        assertEquals(1, accessRequests)
        assertFalse(enabled)
        rule.onNodeWithTag("LoggingSwitchFixture").assertIsOff()
    }
}
