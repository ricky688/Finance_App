package com.example.vibefinance.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.example.vibefinance.ui.preferences.*
import kotlinx.coroutines.launch
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

/** Actual Daily/recalculate/entry/picker paths explored with ARTEMIS observation and ADB before authoring. */
class ActionIconMotionTest {
    @get:Rule val rule = createComposeRule()
    private lateinit var recalc: TapIconMotion
    private lateinit var scanner: TapIconMotion
    private var level by mutableStateOf(MotionLevel.FULL)
    private var expanded by mutableStateOf(false)
    private val compositions = AtomicInteger()
    private var actions = 0
    private var waitForScanner: () -> Unit = {}

    private fun show() {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            CompositionLocalProvider(LocalExperience provides ExperiencePreferences(motion = level)) {
                MaterialTheme {
                    recalc = rememberTapIconMotion(TapIconEffect.RECALCULATE)
                    scanner = rememberTapIconMotion(TapIconEffect.SCANNER)
                    val scope = rememberCoroutineScope()
                    waitForScanner = { scope.launch { scanner.awaitIdle(); actions++ } }
                    val fab = transactionFabIconMotion(expanded)
                    SideEffect { compositions.incrementAndGet() }
                    Column {
                        Icon(Icons.Default.Autorenew, null, Modifier.size(24.dp).testTag("Recalc").then(recalc.modifier))
                        Icon(Icons.Default.CameraAlt, null, Modifier.size(24.dp).testTag("Scanner").then(scanner.modifier))
                        Icon(Icons.Default.Add, null, Modifier.size(24.dp).testTag("Fab").then(fab))
                    }
                }
            }
        }
        advance(64)
    }
    private fun advance(ms: Long) { rule.mainClock.advanceTimeBy(ms); rule.waitForIdle() }
    private fun node(tag: String) = rule.onNodeWithTag(tag)

    @Test fun recalculateRotatesPulsesDebouncesAndDoesNotRecomposePerFrame() {
        show()
        val bounds = node("Recalc").getUnclippedBoundsInRoot()
        rule.runOnIdle {
            assertTrue(recalc.start()); actions++
            assertFalse(recalc.start())
        }
        advance(80)
        val count = compositions.get()
        advance(80)
        assertEquals(count, compositions.get())
        var maxRotation = 0f; var maxScale = 1f
        repeat(100) {
            rule.runOnIdle {
                maxRotation = maxOf(maxRotation, recalc.rotation.value)
                maxScale = maxOf(maxScale, recalc.scale.value)
            }
            advance(16)
        }
        assertTrue(maxRotation >= 359f); assertTrue(maxScale >= 1.17f)
        advance(2000)
        rule.runOnIdle {
            assertFalse(recalc.isRunning)
            assertEquals(0f, recalc.rotation.value, 0f); assertEquals(1f, recalc.scale.value, 0f)
            assertEquals(1, actions)
            assertTrue(recalc.start())
        }
        assertEquals(bounds, node("Recalc").getUnclippedBoundsInRoot())
    }

    @Test fun scannerClosesOpensAndSettlesBeforeOpeningPickerOnceWithin350ms() {
        show()
        val bounds = node("Scanner").getUnclippedBoundsInRoot()
        rule.runOnIdle {
            assertTrue(scanner.start()); waitForScanner()
            assertFalse(scanner.start())
        }
        advance(64)
        rule.runOnIdle { assertTrue(scanner.scale.value < 1f); assertEquals(0, actions) }
        advance(80)
        rule.runOnIdle { assertTrue(scanner.scale.value > 1f); assertEquals(0, actions) }
        advance(206)
        rule.runOnIdle {
            assertFalse("Shutter must finish within 350ms", scanner.isRunning)
            assertEquals(1f, scanner.scale.value, 0f); assertEquals(1, actions)
        }
        assertEquals(bounds, node("Scanner").getUnclippedBoundsInRoot())
    }

    @Test fun fabFollowsOpenAndCloseAndSleepsAfterSettling() {
        show()
        val bounds = node("Fab").getUnclippedBoundsInRoot()
        val closed = node("Fab").captureToImage().toPixelMap()
        rule.runOnIdle { expanded = true }; advance(128)
        val count = compositions.get()
        advance(96)
        assertEquals(count, compositions.get())
        advance(2000)
        val open = node("Fab").captureToImage().toPixelMap()
        assertTrue((0 until open.height).any { y -> (0 until open.width).any { x -> open[x,y] != closed[x,y] } })
        rule.runOnIdle { expanded = false }; advance(2000)
        val restored = node("Fab").captureToImage().toPixelMap()
        for (y in 0 until closed.height) for (x in 0 until closed.width) assertEquals(closed[x,y], restored[x,y])
        assertEquals(bounds, node("Fab").getUnclippedBoundsInRoot())
    }

    @Test fun motionPreferenceChangeResetsBusyEffectsAndMinimalSkipsSpatialAnimation() {
        show()
        rule.runOnIdle { recalc.start(); scanner.start(); expanded = true }; advance(64)
        rule.runOnIdle { level = MotionLevel.MINIMAL }; advance(64)
        rule.runOnIdle {
            assertFalse(recalc.isRunning); assertFalse(scanner.isRunning)
            assertEquals(0f, recalc.rotation.value, 0f); assertEquals(1f, scanner.scale.value, 0f)
        }
        for (motion in listOf(MotionLevel.MINIMAL, MotionLevel.REDUCED)) {
            rule.runOnIdle { level = motion }; advance(32)
            rule.runOnIdle { assertTrue(recalc.start()); assertTrue(scanner.start()) }; advance(32)
            rule.runOnIdle {
                assertFalse(recalc.isRunning); assertFalse(scanner.isRunning)
                assertEquals(0f, recalc.rotation.value, 0f)
                assertEquals(1f, recalc.scale.value, 0f); assertEquals(1f, scanner.scale.value, 0f)
            }
        }
    }
}
