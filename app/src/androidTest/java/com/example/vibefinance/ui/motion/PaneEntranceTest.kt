package com.example.vibefinance.ui.motion

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vibefinance.ui.preferences.ExperiencePreferences
import com.example.vibefinance.ui.preferences.LocalExperience
import com.example.vibefinance.ui.preferences.MotionLevel
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PaneEntranceTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun paneEntrance_entersUpwardAndExitsUpward() {
        rule.mainClock.autoAdvance = false
        var exiting by mutableStateOf(false)
        rule.setContent {
            CompositionLocalProvider(
                LocalExperience provides ExperiencePreferences(motion = MotionLevel.FULL)
            ) {
                MaterialTheme {
                    StaggeredPane(
                        modifier = Modifier.fillMaxSize().padding(top = 64.dp),
                        exiting = exiting,
                    ) {
                        Column(Modifier.fillMaxWidth()) {
                            Box(Modifier.paneEntrance(0).fillMaxWidth().height(48.dp).testTag("PaneSection0")) {
                                Text("Section 0")
                            }
                            Box(Modifier.paneEntrance(1).fillMaxWidth().height(48.dp).testTag("PaneSection1")) {
                                Text("Section 1")
                            }
                        }
                    }
                }
            }
        }
        rule.mainClock.advanceTimeByFrame()
        val initialTop = rule.onNodeWithTag("PaneSection0").fetchSemanticsNode().boundsInRoot.top

        rule.mainClock.advanceTimeBy(1_500)
        rule.waitForIdle()
        val restingTop = rule.onNodeWithTag("PaneSection0").fetchSemanticsNode().boundsInRoot.top
        assertTrue("Entrance must rise from below ($initialTop -> $restingTop)", initialTop > restingTop + 1f)

        rule.runOnIdle { exiting = true }
        rule.mainClock.advanceTimeBy(60)
        val exitMidTop = rule.onNodeWithTag("PaneSection0").fetchSemanticsNode().boundsInRoot.top
        assertTrue("Fast exit must already move upward ($restingTop -> $exitMidTop)", exitMidTop < restingTop - 1f)
    }
}
