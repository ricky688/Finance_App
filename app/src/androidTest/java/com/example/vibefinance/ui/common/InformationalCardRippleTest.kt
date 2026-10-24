@file:OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)

package com.example.vibefinance.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

/** Daily card presses explored with ARTEMIS; native control calibrates Waydroid ripple rendering. */
@RunWith(AndroidJUnit4::class)
class InformationalCardRippleTest {
    @get:Rule val rule = createComposeRule()
    @Test fun lightCardRippleMatchesNativeGrayInsteadOfContentTint() = verifyRipple(Color.White)
    @Test fun darkCardRippleMatchesNativeGrayInsteadOfContentTint() = verifyRipple(Color(0xFF202020))

    private fun verifyRipple(background: Color) {
        rule.setContent { MaterialTheme {
            CompositionLocalProvider(LocalContentColor provides Color.Red) {
                Column {
                    val source = remember { MutableInteractionSource() }
                    Box(Modifier.size(160.dp).testTag("native")
                        .clickable(interactionSource = source, indication = ripple(color = Color.Gray), onClick = {})
                        .background(background))
                    val press = rememberExpressiveCardPress()
                    Box(Modifier.size(160.dp).testTag("card")
                        .expressiveCardClickable(press, hoverEnabled = false) {}
                        .background(background, press.shape))
                }
            }
        } }
        fun sample(node: SemanticsNodeInteraction): Color {
            val pixels = node.captureToImage().toPixelMap()
            return pixels[pixels.width / 2, pixels.height / 2]
        }
        fun heldColor(tag: String): Color {
            val target = rule.onNodeWithTag(tag)
            val idle = sample(target)
            target.performTouchInput { down(center) }
            try {
                rule.waitForIdle()
                var pressed = idle
                // Native Android ripple uses real time and may be only one 8-bit step on Waydroid.
                rule.waitUntil(2_000) {
                    pressed = sample(target)
                    abs(pressed.red - idle.red) >= 1f / 255f - 0.00001f
                }
                assertTrue("$tag ripple is neutral rather than red content tint",
                    abs(pressed.red - pressed.green) < 0.0001f && abs(pressed.green - pressed.blue) < 0.0001f)
                return pressed
            } finally {
                target.performTouchInput { up() }
            }
        }
        val native = heldColor("native")
        val card = heldColor("card")
        assertEquals("Passive cards retain native gray ripple intensity", native.red, card.red, 1f / 255f)
    }
}
