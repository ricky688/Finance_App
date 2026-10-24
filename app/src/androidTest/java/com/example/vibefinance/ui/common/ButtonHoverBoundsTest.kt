@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class, androidx.compose.ui.test.ExperimentalTestApi::class, androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import android.view.InputDevice
import android.view.MotionEvent
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vibefinance.ui.components.CompletePressButton
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

/** Date selector and Daily card interactions explored on Waydroid with ARTEMIS before these fixtures. */
@RunWith(AndroidJUnit4::class)
class ButtonHoverBoundsTest {
    @get:Rule val rule = createComposeRule()

    @Test fun lightButtonsUseOneUniformRoundedHover() = verifyHover(false)
    @Test fun darkButtonsUseOneUniformRoundedHover() = verifyHover(true)

    private fun verifyHover(dark: Boolean) {
        var clicks = 0
        val mouseEvents = mutableListOf<Triple<Int, Int, Int>>()
        rule.setContent {
            MaterialTheme(if (dark) darkColorScheme() else lightColorScheme()) {
                Surface {
                    Column(Modifier.motionEventSpy {
                        mouseEvents.add(Triple(it.source, it.actionMasked, it.buttonState))
                    }) {
                        val colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        Button(onClick = {}, elevation = com.example.vibefinance.ui.components.AppButtonDefaults.elevation(), colors = colors, shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.size(320.dp, 52.dp).testTag("native")) { Text("Select End Date") }
                        CompletePressButton(onClick = { clicks++ }, colors = colors, shapes = ButtonDefaults.shapes(
                            shape = RoundedCornerShape(16.dp), pressedShape = RoundedCornerShape(12.dp)),
                            modifier = Modifier.size(320.dp, 52.dp).testTag("expressive")) { Text("Select End Date") }
                    }
                }
            }
        }
        val hoverColors = mutableListOf<Color>()
        listOf("native", "expressive").forEach { tag ->
            val node = rule.onNodeWithTag(tag)
            val idle = node.captureToImage().toPixelMap()
            node.performMouseInput { enter(center) }
            rule.waitForIdle()
            val pixels = node.captureToImage().toPixelMap()
            val image = node.captureToImage()
            val target = java.io.File(androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null), "hover-$dark-$tag.png")
            target.outputStream().use { image.asAndroidBitmap().compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
            val upper = pixels[pixels.width / 2, pixels.height / 8]
            val middle = pixels[pixels.width / 8, pixels.height / 2]
            assertTrue("$tag hover covers padding and content uniformly: $upper / $middle", distance(upper, middle) < 0.03f)
            assertTrue("$tag hover remains visible", distance(upper, idle[idle.width / 2, idle.height / 8]) > 0.015f)
            assertTrue("$tag hover stays inside rounded corners", distance(pixels[0, 0], idle[0, 0]) < 0.01f)
            hoverColors.add(upper)
            if (tag == "expressive") {
                node.performMouseInput { click() }
                rule.runOnIdle { assertEquals("One left click submits once", 1, clicks) }
            }
            node.performMouseInput { exit() }
            rule.waitForIdle()
            val restored = node.captureToImage().toPixelMap()
            assertTrue("$tag clears hover on pointer exit", distance(
                restored[restored.width / 2, restored.height / 8], idle[idle.width / 2, idle.height / 8]) < 0.01f)
        }
        assertTrue("Shared buttons must not stack a second hover layer", distance(hoverColors[0], hoverColors[1]) < 0.03f)
        rule.onNodeWithTag("expressive").performMouseInput { enter(center); click(); exit() }
        rule.runOnIdle {
            assertEquals("A second mouse click is handled exactly once", 2, clicks)
            val fromMouse = mouseEvents.filter { (source, _, _) -> source and InputDevice.SOURCE_MOUSE == InputDevice.SOURCE_MOUSE }
            assertTrue("Hover is delivered as Android mouse input", fromMouse.any {
                it.second == MotionEvent.ACTION_HOVER_ENTER || it.second == MotionEvent.ACTION_HOVER_MOVE
            })
            assertTrue("Pointer exit reaches the Android view", fromMouse.any { it.second == MotionEvent.ACTION_HOVER_EXIT })
            assertTrue("Left click carries the primary mouse button", fromMouse.any {
                it.second == MotionEvent.ACTION_DOWN && it.third and MotionEvent.BUTTON_PRIMARY != 0
            })
        }
    }

    private fun distance(a: Color, b: Color) = abs(a.red - b.red) + abs(a.green - b.green) + abs(a.blue - b.blue)
}
