package com.example.vibefinance.wear

import android.content.SharedPreferences
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Real sharing component, isolated in-memory preferences; no account or device configuration writes. */
@RunWith(AndroidJUnit4::class)
class WearSharingPreferenceTest {
    @get:Rule val rule = createComposeRule()

    @Test fun defaultOffAndNativeTogglePersistAndObserveChanges() {
        val prefs = MemoryPreferences()
        rule.setContent { MaterialTheme { WearSharingPreference(prefs) } }
        val toggle = rule.onNodeWithTag("WearBudgetSharing")
        toggle.assertIsOff().assertHeightIsAtLeast(48.dp).assertWidthIsAtLeast(48.dp)
        toggle.performClick()
        rule.waitUntil(3_000) { prefs.enabled }
        toggle.assertIsOn()
        rule.runOnIdle { prefs.edit().putBoolean(PhoneBudgetBridge.SHARE_KEY, false).commit() }
        toggle.assertIsOff()
    }

    @Test fun failedWriteKeepsSharingOff() {
        val prefs = MemoryPreferences().apply { failWrites = true }
        rule.setContent { MaterialTheme { WearSharingPreference(prefs) } }
        rule.onNodeWithTag("WearBudgetSharing").assertIsOff().performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("WearBudgetSharing").assertIsOff()
        rule.runOnIdle { assertFalse(prefs.enabled); assertEquals(1, prefs.attempts) }
    }

    @Test fun narrowLargeFontKeepsActualSwitchInsideContainer() {
        val prefs = MemoryPreferences()
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.5f)) {
                MaterialTheme {
                    Box(Modifier.width(320.dp).testTag("SharingContainer")) { WearSharingPreference(prefs) }
                }
            }
        }
        val container = rule.onNodeWithTag("SharingContainer").fetchSemanticsNode().boundsInRoot
        val toggle = rule.onNodeWithTag("WearBudgetSharing")
        toggle.assertIsDisplayed().assertHeightIsAtLeast(48.dp)
        val bounds = toggle.fetchSemanticsNode().boundsInRoot
        assertTrue(bounds.left >= container.left && bounds.right <= container.right + 1)
        assertTrue(bounds.top >= container.top && bounds.bottom <= container.bottom + 1)
    }
}

private class MemoryPreferences : SharedPreferences {
    var enabled = false
    var failWrites = false
    var attempts = 0
    private val listeners = mutableSetOf<SharedPreferences.OnSharedPreferenceChangeListener>()
    override fun getBoolean(key: String?, defValue: Boolean) = if (key == PhoneBudgetBridge.SHARE_KEY) enabled else defValue
    override fun contains(key: String?) = key == PhoneBudgetBridge.SHARE_KEY
    override fun getAll(): MutableMap<String, *> = mutableMapOf(PhoneBudgetBridge.SHARE_KEY to enabled)
    override fun getString(key: String?, defValue: String?) = defValue
    override fun getStringSet(key: String?, defValues: MutableSet<String>?) = defValues
    override fun getInt(key: String?, defValue: Int) = defValue
    override fun getLong(key: String?, defValue: Long) = defValue
    override fun getFloat(key: String?, defValue: Float) = defValue
    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) { listeners.add(listener) }
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) { listeners.remove(listener) }
    override fun edit(): SharedPreferences.Editor = object : SharedPreferences.Editor {
        private var next = enabled
        override fun putBoolean(key: String?, value: Boolean) = apply { require(key == PhoneBudgetBridge.SHARE_KEY); next = value }
        override fun commit(): Boolean {
            attempts++
            if (failWrites) return false
            enabled = next
            listeners.toList().forEach { it.onSharedPreferenceChanged(this@MemoryPreferences, PhoneBudgetBridge.SHARE_KEY) }
            return true
        }
        override fun apply() { commit() }
        override fun putString(key: String?, value: String?): SharedPreferences.Editor = error("Boolean fixture only")
        override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor = error("Boolean fixture only")
        override fun putInt(key: String?, value: Int): SharedPreferences.Editor = error("Boolean fixture only")
        override fun putLong(key: String?, value: Long): SharedPreferences.Editor = error("Boolean fixture only")
        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor = error("Boolean fixture only")
        override fun remove(key: String?): SharedPreferences.Editor = error("Boolean fixture only")
        override fun clear(): SharedPreferences.Editor = error("Boolean fixture only")
    }
}
