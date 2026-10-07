package com.example.vibefinance.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import android.os.SystemClock
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.WindowManager
import com.example.vibefinance.data.entity.ExpenseIcon
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** ARTEMIS verified: choose -> symbol/emoji -> Save, scroll to keyboard input, Cancel and default.
 * Tests host the real picker with transient state and never replace the device's financial data.
 */
@RunWith(AndroidJUnit4::class)
class ExpenseIconPickerTest {
    @get:Rule val compose = createComposeRule()

    private fun host(initial: String? = null) {
        compose.setContent {
            var value by remember { mutableStateOf(initial) }
            MaterialTheme {
                ExpenseIconChoice(value, "Food", { value = it })
                Text(value ?: "default", Modifier.testTag("ChosenIcon"))
            }
        }
    }

    private fun open() {
        compose.onNodeWithTag("ExpenseIconChoose").performClick()
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("ExpenseIconApply").fetchSemanticsNodes().isNotEmpty() }
    }

    /** Compose locators first; fallback is limited to the ARTEMIS-explored 480x1000 layout. */
    private fun click(tag: String, x: Int, y: Int) {
        try {
            compose.onNodeWithTag(tag).performClick()
        } catch (failure: AssertionError) {
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            val metrics = DisplayMetrics()
            instrumentation.targetContext.getSystemService(WindowManager::class.java).defaultDisplay.getRealMetrics(metrics)
            if (metrics.widthPixels != 480 || metrics.heightPixels != 1000 ||
                instrumentation.targetContext.resources.configuration.fontScale != 1f) throw failure
            val downTime = SystemClock.uptimeMillis()
            listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP).forEach { action ->
                MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), action, x.toFloat(), y.toFloat(), 0).let { event ->
                    try { instrumentation.sendPointerSync(event) } finally { event.recycle() }
                }
            }
        }
        compose.waitForIdle()
    }

    @Test fun savingSymbolAndEmojiCommitsOnlyTheConfirmedChoice() {
        host()
        open()
        click("ExpenseIconSymbol_COFFEE", 201, 367)
        click("ExpenseIconApply", 360, 893)
        compose.onNodeWithTag("ChosenIcon").assertTextEquals("symbol:COFFEE")
        open()
        compose.onNodeWithTag("ExpenseIconEmoji_☕").performScrollTo()
        click("ExpenseIconEmoji_☕", 201, 751)
        click("ExpenseIconApply", 360, 893)
        compose.onNodeWithTag("ChosenIcon").assertTextEquals("emoji:☕")
    }

    @Test fun cancelKeepsPreviousChoiceAndDefaultRemovesCustomization() {
        host("emoji:☕")
        open()
        click("ExpenseIconSymbol_SHOPPING", 278, 367)
        click("ExpenseIconCancel", 280, 893)
        compose.onNodeWithTag("ChosenIcon").assertTextEquals("emoji:☕")
        open()
        click("ExpenseIconDefault", 214, 196)
        click("ExpenseIconApply", 360, 893)
        compose.onNodeWithTag("ChosenIcon").assertTextEquals("default")
    }

    @Test fun keyboardEmojiRejectsTextAndMultipleIconsButAcceptsJoinedSequence() {
        host()
        open()
        val input = compose.onNodeWithTag("ExpenseIconEmojiInput").performScrollTo()
        input.performTextReplacement("coffee")
        compose.onNodeWithTag("ExpenseIconApply").assertIsNotEnabled()
        input.performTextReplacement("☕🍜")
        compose.onNodeWithTag("ExpenseIconApply").assertIsNotEnabled()
        val family = "👨‍👩‍👧‍👦"
        input.performTextReplacement(family)
        compose.onNodeWithTag("ExpenseIconApply").assertIsEnabled().performClick()
        compose.waitForIdle()
        compose.onNodeWithTag("ChosenIcon").assertTextEquals(ExpenseIcon.emoji(family)!!)
    }
}
