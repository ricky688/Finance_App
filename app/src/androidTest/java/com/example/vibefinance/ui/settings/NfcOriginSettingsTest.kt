package com.example.vibefinance.ui.settings

import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vibefinance.R
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.Locale

/** ARTEMIS/ADB observed the host; its mandatory rollover prompt was left untouched. Read-only fixture. */
class NfcOriginSettingsTest {
    @get:Rule val rule = createComposeRule()
    @Test fun unsupportedDeviceExplainsRequirementWithoutAllowingToggle() {
        assumeTrue(Build.VERSION.SDK_INT < 36)
        show(false)
        rule.onNodeWithTag("NfcOriginSwitch").assertIsNotEnabled()
        rule.onNodeWithText("Requires Android 16+ and compatible NFC hardware").assertIsDisplayed()
    }
    @Test fun traditionalChineseLargeFontDarkSectionRemainsReadable() {
        show(true)
        rule.onNodeWithText("NFC 輔助辨識起點").assertIsDisplayed()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
            .createConfigurationContext(Configuration().apply { setLocale(Locale.forLanguageTag("zh-Hant-HK")) })
        val body = rule.onNodeWithText(context.getString(R.string.nfc_origin_body))
        body.performScrollTo().assertIsDisplayed()
        val layouts = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
        body.performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult) { it(layouts) }
        assertFalse(layouts.single().didOverflowWidth)
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        File(context.getExternalFilesDir(null), "nfc-origin-settings-large-dark.png").outputStream().use {
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
    }
    private fun show(chinese: Boolean) {
        rule.setContent {
            val configuration = Configuration(LocalConfiguration.current).apply {
                setLocale(Locale.forLanguageTag(if (chinese) "zh-Hant-HK" else "en"))
            }
            val context = LocalContext.current.createConfigurationContext(configuration)
            val density = LocalDensity.current
            CompositionLocalProvider(LocalConfiguration provides configuration, LocalContext provides context,
                LocalDensity provides Density(density.density, if (chinese) 1.5f else density.fontScale)) {
                MaterialTheme(colorScheme = if (chinese) darkColorScheme() else lightColorScheme()) {
                    Surface { Column(Modifier.width(280.dp).verticalScroll(rememberScrollState()).padding(16.dp)) { NfcOriginSettings() } }
                }
            }
        }
        rule.waitForIdle()
    }
}
