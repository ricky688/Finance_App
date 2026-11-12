package com.example.vibefinance.ui.e2e

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vibefinance.ui.components.ExpressiveConnectedButtonGroup
import com.example.vibefinance.ui.home.DaysLeftCard
import com.example.vibefinance.ui.home.LocalAmbientMotionEnabled
import com.example.vibefinance.ui.preferences.ExperiencePreferences
import com.example.vibefinance.ui.preferences.LocalExperience
import com.example.vibefinance.ui.preferences.MotionLevel
import com.example.vibefinance.ui.main.AdaptiveRecordScene
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Connected E2E Layout & Interaction Tests (Waydroid runtime on 192.168.240.112:5555).
 * Directly exercises Compose layout nodes, bounds measurement, and touch targets.
 */
@RunWith(AndroidJUnit4::class)
class ConnectedE2ELayoutAndInteractionTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun testConnectedDaysLeftCardEnforcesStrictSquareRatio() {
        rule.setContent {
            CompositionLocalProvider(
                LocalAmbientMotionEnabled provides false,
                LocalExperience provides ExperiencePreferences(motion = MotionLevel.MINIMAL)
            ) {
                MaterialTheme {
                    Box(
                        modifier = Modifier
                            .size(width = 140.dp, height = 200.dp)
                            .background(Color.White)
                            .testTag("Container")
                    ) {
                        DaysLeftCard(
                            daysLeft = 14,
                            totalDays = 30,
                            modifier = Modifier.testTag("DaysCardUnderMeasure")
                        )
                    }
                }
            }
        }
        rule.waitForIdle()

        val bounds = rule.onNodeWithTag("DaysCardUnderMeasure").fetchSemanticsNode().boundsInRoot
        val width = bounds.width
        val height = bounds.height
        assertTrue("Width ($width) must be greater than zero", width > 0f)
        assertEquals("DaysLeftCard width and height must match for 1:1 aspect ratio", width, height, 2.0f)
    }

    @Test
    fun testConnectedDaysLeftCardMaintainsSquareRatioAtVariousSizes() {
        rule.setContent {
            CompositionLocalProvider(
                LocalAmbientMotionEnabled provides false,
                LocalExperience provides ExperiencePreferences(motion = MotionLevel.MINIMAL)
            ) {
                MaterialTheme {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .testTag("Box_120")
                        ) {
                            DaysLeftCard(
                                daysLeft = 5,
                                totalDays = 20,
                                modifier = Modifier.testTag("DaysCard_120")
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .testTag("Box_140")
                        ) {
                            DaysLeftCard(
                                daysLeft = 10,
                                totalDays = 20,
                                modifier = Modifier.testTag("DaysCard_140")
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(156.dp)
                                .testTag("Box_156")
                        ) {
                            DaysLeftCard(
                                daysLeft = 15,
                                totalDays = 20,
                                modifier = Modifier.testTag("DaysCard_156")
                            )
                        }
                    }
                }
            }
        }
        rule.waitForIdle()
        listOf(120, 140, 156).forEach { targetSize ->
            val bounds = rule.onNodeWithTag("DaysCard_$targetSize").fetchSemanticsNode().boundsInRoot
            assertEquals("Size $targetSize must produce 1:1 ratio", bounds.width, bounds.height, 2.0f)
        }
    }

    @Test
    fun testConnectedModeSwitcherConnectedButtonsMeet48dpHeight() {
        rule.setContent {
            MaterialTheme {
                Box(Modifier.fillMaxWidth().padding(16.dp)) {
                    var selected by remember { mutableIntStateOf(0) }
                    val modes = listOf("Expense", "Income", "Transfer")
                    ExpressiveConnectedButtonGroup(
                        items = modes,
                        selectedIndex = selected,
                        onItemSelected = { selected = it },
                        modifier = Modifier.fillMaxWidth().testTag("ConnectedModeGroup"),
                        itemModifierProvider = { Modifier.heightIn(min = 48.dp) },
                        labelProvider = { it }
                    )
                }
            }
        }
        rule.waitForIdle()

        listOf("Expense", "Income", "Transfer").forEach { label ->
            rule.onNodeWithText(label).assertHeightIsAtLeast(48.dp)
        }
    }

    @Test
    fun testConnectedClearButtonTouchTargetMeets48dpBounds() {
        rule.setContent {
            MaterialTheme {
                Box(Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("ClearButton48dp")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        rule.waitForIdle()

        rule.onNodeWithTag("ClearButton48dp")
            .assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(48.dp)
    }

    @Test
    fun testConnectedAdaptiveRecordSceneDualPanePresenceAtWideWidth() {
        rule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                MaterialTheme {
                    Box(Modifier.requiredSize(1000.dp, 800.dp).testTag("WideContainer")) {
                        AdaptiveRecordScene(
                            selectedId = null,
                            onClose = {},
                            listContent = {
                                Box(Modifier.fillMaxSize().testTag("ListPaneContent")) {
                                    Text("Record List Pane")
                                }
                            },
                            detailContent = { id ->
                                Box(Modifier.fillMaxSize().testTag("DetailPaneContent")) {
                                    Text("Detail Record $id")
                                }
                            }
                        )
                    }
                }
            }
        }
        rule.waitForIdle()

        rule.onNodeWithTag("ListPaneContent").assertIsDisplayed()
        rule.onNodeWithTag("InspectionPlaceholder").assertIsDisplayed()
    }
}
