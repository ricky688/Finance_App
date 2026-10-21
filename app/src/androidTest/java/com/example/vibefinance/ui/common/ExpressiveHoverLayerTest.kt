@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.example.vibefinance.ui.common

import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vibefinance.ui.components.CompletePressButton
import com.example.vibefinance.ui.components.ExpressiveSwipeRow
import com.example.vibefinance.ui.preferences.ExperiencePreferences
import com.example.vibefinance.ui.preferences.LocalExperience
import com.example.vibefinance.ui.preferences.MotionLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

@Composable
private fun SampleHoverContent(counter: AtomicInteger) {
    counter.incrementAndGet()
    Text("Hover Target")
}

@RunWith(AndroidJUnit4::class)
class ExpressiveHoverLayerTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun hoverStateLayer_zeroContentRecompositionDuringAnimation() {
        val contentRecompositions = AtomicInteger(0)
        lateinit var interactionSource: MutableInteractionSource
        lateinit var scope: CoroutineScope

        rule.setContent {
            interactionSource = remember { MutableInteractionSource() }
            scope = rememberCoroutineScope()
            CompositionLocalProvider(
                LocalExperience provides ExperiencePreferences(motion = MotionLevel.FULL)
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .expressiveHoverLayer(
                            interactionSource = interactionSource,
                            enabled = true,
                            shape = RoundedCornerShape(16.dp),
                            liftDp = 1.5.dp,
                            highlightRim = true
                        )
                ) {
                    SampleHoverContent(contentRecompositions)
                }
            }
        }
        rule.waitForIdle()

        // Initial composition
        val initialCount = contentRecompositions.get()
        assertEquals(1, initialCount)

        // Hover enter
        var enterInteraction: HoverInteraction.Enter? = null
        rule.runOnIdle {
            scope.launch {
                val enter = HoverInteraction.Enter()
                enterInteraction = enter
                interactionSource.emit(enter)
            }
        }
        rule.waitForIdle()

        // Content must not recompose on continuous animation frames (GPU layer only)
        val afterHoverCount = contentRecompositions.get()
        assertTrue("Content is not repeatedly recomposed during animation", afterHoverCount <= 2)

        // Hover exit
        rule.runOnIdle {
            scope.launch {
                enterInteraction?.let { interactionSource.emit(HoverInteraction.Exit(it)) }
            }
        }
        rule.waitForIdle()

        val afterExitCount = contentRecompositions.get()
        assertTrue("Content is not repeatedly recomposed during exit animation", afterExitCount <= 3)
    }

    @Test
    fun hoverStateLayer_motionLevelsExecuteWithoutCrash() {
        lateinit var fullSource: MutableInteractionSource
        lateinit var reducedSource: MutableInteractionSource
        lateinit var minimalSource: MutableInteractionSource
        lateinit var scope: CoroutineScope

        rule.setContent {
            fullSource = remember { MutableInteractionSource() }
            reducedSource = remember { MutableInteractionSource() }
            minimalSource = remember { MutableInteractionSource() }
            scope = rememberCoroutineScope()

            androidx.compose.foundation.layout.Column {
                CompositionLocalProvider(
                    LocalExperience provides ExperiencePreferences(motion = MotionLevel.FULL)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .expressiveHoverLayer(
                                interactionSource = fullSource,
                                enabled = true,
                                shape = RoundedCornerShape(24.dp),
                                liftDp = 2.dp,
                                highlightRim = true
                            )
                    )
                }
                CompositionLocalProvider(
                    LocalExperience provides ExperiencePreferences(motion = MotionLevel.REDUCED)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .expressiveHoverLayer(
                                interactionSource = reducedSource,
                                enabled = true,
                                shape = RoundedCornerShape(24.dp),
                                liftDp = 2.dp,
                                highlightRim = true
                            )
                    )
                }
                CompositionLocalProvider(
                    LocalExperience provides ExperiencePreferences(motion = MotionLevel.MINIMAL)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .expressiveHoverLayer(
                                interactionSource = minimalSource,
                                enabled = true,
                                shape = RoundedCornerShape(24.dp),
                                liftDp = 2.dp,
                                highlightRim = true
                            )
                    )
                }
            }
        }
        rule.waitForIdle()

        rule.runOnIdle {
            scope.launch {
                val e1 = HoverInteraction.Enter()
                val e2 = HoverInteraction.Enter()
                val e3 = HoverInteraction.Enter()
                fullSource.emit(e1)
                reducedSource.emit(e2)
                minimalSource.emit(e3)
                fullSource.emit(HoverInteraction.Exit(e1))
                reducedSource.emit(HoverInteraction.Exit(e2))
                minimalSource.emit(HoverInteraction.Exit(e3))
            }
        }
        rule.waitForIdle()
    }

    @Test
    fun completePressButton_integratesExpressiveHoverLayer() {
        lateinit var interactionSource: MutableInteractionSource
        lateinit var scope: CoroutineScope
        val clicked = AtomicBoolean(false)

        rule.setContent {
            interactionSource = remember { MutableInteractionSource() }
            scope = rememberCoroutineScope()
            CompositionLocalProvider(
                LocalExperience provides ExperiencePreferences(motion = MotionLevel.FULL)
            ) {
                CompletePressButton(
                    onClick = { clicked.set(true) },
                    interactionSource = interactionSource,
                    modifier = Modifier.testTag("hover_btn")
                ) {
                    Text("Hover Button")
                }
            }
        }
        rule.waitForIdle()

        // Emit hover
        rule.runOnIdle {
            scope.launch {
                val enter = HoverInteraction.Enter()
                interactionSource.emit(enter)
            }
        }
        rule.waitForIdle()

        // Button is clickable
        rule.onNodeWithTag("hover_btn").performClick()
        rule.waitForIdle()
        assertTrue("Button click handled", clicked.get())
    }

    @Test
    fun expressiveSwipeRow_integratesExpressiveHoverLayer() {
        val editCalled = AtomicBoolean(false)

        rule.setContent {
            CompositionLocalProvider(
                LocalExperience provides ExperiencePreferences(motion = MotionLevel.FULL)
            ) {
                ExpressiveSwipeRow(
                    shape = RoundedCornerShape(16.dp),
                    onEdit = { editCalled.set(true) },
                    onDelete = {},
                    modifier = Modifier.testTag("hover_row")
                ) {
                    Text("Transaction Row")
                }
            }
        }
        rule.waitForIdle()

        rule.onNodeWithTag("hover_row").performClick()
        rule.waitForIdle()
        assertTrue("Row edit invoked on click", editCalled.get())
    }
}
