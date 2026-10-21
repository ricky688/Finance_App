@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.example.vibefinance.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class AnimatedCategoryIconTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun animatedCategoryIcon_zeroContentRecompositionDuringAnimation() {
        val parentRecompositions = AtomicInteger(0)
        var isSelected by mutableStateOf(false)
        var trigger by mutableIntStateOf(0)

        rule.setContent {
            CompositionLocalProvider(
                LocalExperience provides ExperiencePreferences(motion = MotionLevel.FULL)
            ) {
                Box(modifier = Modifier.size(60.dp)) {
                    parentRecompositions.incrementAndGet()
                    AnimatedCategoryIcon(
                        category = "Food",
                        isSelected = isSelected,
                        trigger = trigger,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("test_food_icon")
                    )
                }
            }
        }
        rule.waitForIdle()

        val initialCount = parentRecompositions.get()
        assertEquals(1, initialCount)

        // Select the icon
        rule.runOnIdle {
            trigger++
            isSelected = true
        }
        rule.waitForIdle()

        // Parent must only recompose once for the state change, not on animation frames
        val afterSelectCount = parentRecompositions.get()
        assertEquals(2, afterSelectCount)

        // Reselect already selected icon
        rule.runOnIdle {
            trigger++
        }
        rule.waitForIdle()

        val afterReselectCount = parentRecompositions.get()
        assertEquals(3, afterReselectCount)
    }

    @Test
    fun animatedCategoryIcon_motionLevelsRenderWithoutCrash() {
        var isSelected by mutableStateOf(false)
        var trigger by mutableIntStateOf(0)

        rule.setContent {
            Column {
                CompositionLocalProvider(LocalExperience provides ExperiencePreferences(motion = MotionLevel.FULL)) {
                    AnimatedCategoryIcon("Food", isSelected, trigger = trigger)
                    AnimatedCategoryIcon("Coffee", isSelected, trigger = trigger)
                    AnimatedCategoryIcon("Transport", isSelected, trigger = trigger)
                    AnimatedCategoryIcon("Entertainment", isSelected, trigger = trigger)
                    AnimatedCategoryIcon("Health", isSelected, trigger = trigger)
                    AnimatedCategoryIcon("Fitness", isSelected, trigger = trigger)
                    AnimatedCategoryIcon("Travel", isSelected, trigger = trigger)
                    AnimatedCategoryIcon("Savings", isSelected, trigger = trigger)
                    AnimatedCategoryIcon("Education", isSelected, trigger = trigger)
                    AnimatedCategoryIcon("Gift", isSelected, trigger = trigger)
                    AnimatedCategoryIcon("Investment", isSelected, trigger = trigger)
                    AnimatedCategoryIcon("Software", isSelected, trigger = trigger)
                    AnimatedCategoryIcon("Bills", isSelected, trigger = trigger)
                    AnimatedCategoryIcon("Pets", isSelected, trigger = trigger)
                    AnimatedCategoryIcon("Movies", isSelected, trigger = trigger)
                    AnimatedCategoryIcon("Work", isSelected, trigger = trigger)
                }
                CompositionLocalProvider(LocalExperience provides ExperiencePreferences(motion = MotionLevel.REDUCED)) {
                    AnimatedCategoryIcon("Shopping", isSelected, trigger = trigger)
                    AnimatedCategoryIcon("Game", isSelected, trigger = trigger)
                }
                CompositionLocalProvider(LocalExperience provides ExperiencePreferences(motion = MotionLevel.MINIMAL)) {
                    AnimatedCategoryIcon("Utilities", isSelected, trigger = trigger)
                    AnimatedCategoryIcon("Housing", isSelected, trigger = trigger)
                }
            }
        }
        rule.waitForIdle()

        rule.runOnIdle {
            isSelected = true
            trigger++
        }
        rule.waitForIdle()

        rule.runOnIdle {
            trigger++
        }
        rule.waitForIdle()

        rule.runOnIdle {
            isSelected = false
        }
        rule.waitForIdle()
    }

    @Test
    fun animatedAddCategoryIcon_togglesAndReplays() {
        var isExpanded by mutableStateOf(false)
        var trigger by mutableIntStateOf(0)

        rule.setContent {
            CompositionLocalProvider(
                LocalExperience provides ExperiencePreferences(motion = MotionLevel.FULL)
            ) {
                AnimatedAddCategoryIcon(
                    isExpanded = isExpanded,
                    trigger = trigger,
                    modifier = Modifier.testTag("test_add_icon")
                )
            }
        }
        rule.waitForIdle()

        // Toggle expand
        rule.runOnIdle {
            trigger++
            isExpanded = true
        }
        rule.waitForIdle()

        // Retrigger when expanded
        rule.runOnIdle {
            trigger++
        }
        rule.waitForIdle()

        // Toggle collapse
        rule.runOnIdle {
            trigger++
            isExpanded = false
        }
        rule.waitForIdle()
    }
}
