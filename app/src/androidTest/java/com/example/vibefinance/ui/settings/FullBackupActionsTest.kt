package com.example.vibefinance.ui.settings

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.core.app.ActivityOptionsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vibefinance.theme.VibeFinanceTheme
import org.junit.*
import org.junit.runner.RunWith

/** ARTEMIS verified Data & privacy -> Back up app -> options -> Android save picker. */
@RunWith(AndroidJUnit4::class)
class FullBackupActionsTest {
    @get:Rule val rule = createComposeRule()
    private var launches = 0
    private fun show() {
        val registry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(requestCode: Int, contract: ActivityResultContract<I, O>, input: I, options: ActivityOptionsCompat?) {
                launches++
                dispatchResult(requestCode, Activity.RESULT_CANCELED, Intent())
            }
        }
        rule.setContent {
            CompositionLocalProvider(LocalActivityResultRegistryOwner provides object : ActivityResultRegistryOwner {
                override val activityResultRegistry = registry
            }) {
                VibeFinanceTheme(darkTheme = false, dynamicColorEnabled = false) {
                    val actions = rememberFullBackupActions(onRestored = {})
                    actions()
                }
            }
        }
        waitFor("BackupOpen")
    }
    private fun waitFor(tag: String) = rule.waitUntil(5000) { rule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }

    @Test fun passwordMustBeNonemptyAndMatchBeforeSave() {
        show()
        rule.onNodeWithTag("BackupOpen").performClick(); waitFor("BackupEncrypt")
        rule.onNodeWithTag("BackupContinue").assertIsEnabled()
        rule.onNodeWithTag("BackupEncrypt").performClick(); waitFor("BackupPassword")
        rule.onNodeWithTag("BackupContinue").assertIsNotEnabled()
        rule.onNodeWithTag("BackupPassword").performScrollTo().performTextInput("測試-password")
        rule.onNodeWithTag("BackupPasswordConfirmation").performScrollTo().performTextInput("wrong")
        rule.onNodeWithTag("BackupContinue").assertIsNotEnabled()
        rule.onNodeWithTag("BackupPasswordConfirmation").performTextReplacement("測試-password")
        rule.onNodeWithTag("BackupContinue").assertIsEnabled()
        rule.onNodeWithTag("BackupCancel").performClick(); rule.waitForIdle()
        rule.onNodeWithTag("BackupOpen").assertIsEnabled()
        rule.onNodeWithTag("BackupOpen").performClick(); waitFor("BackupEncrypt")
        rule.onNodeWithTag("BackupEncrypt").assertIsOff()
        rule.onNodeWithTag("BackupPassword").assertDoesNotExist()
    }
    @Test fun cancelledDocumentPickersReturnToIdleWithoutWritingData() {
        show()
        rule.onNodeWithTag("BackupOpen").performClick(); waitFor("BackupContinue")
        rule.onNodeWithTag("BackupContinue").performClick()
        rule.waitUntil(5000) { launches == 1 && rule.onAllNodesWithTag("BackupEncrypt").fetchSemanticsNodes().isEmpty() }
        rule.onNodeWithTag("BackupOpen").assertIsEnabled()
        rule.onNodeWithTag("BackupRestore").performClick()
        rule.waitUntil(5000) { launches == 2 }
        rule.onNodeWithTag("BackupRestore").assertIsEnabled()
    }
}
