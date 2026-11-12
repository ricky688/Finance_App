package com.example.vibefinance.wear

import android.content.SharedPreferences
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.vibefinance.R
import com.example.vibefinance.ui.preferences.PreferenceSwitch

@Composable
internal fun WearSharingPreference(prefs: SharedPreferences = PhoneBudgetBridge.prefs(LocalContext.current)) {
    var enabled by remember(prefs) { mutableStateOf(prefs.getBoolean(PhoneBudgetBridge.SHARE_KEY, false)) }
    var failed by remember { mutableStateOf(false) }
    DisposableEffect(prefs) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == PhoneBudgetBridge.SHARE_KEY) enabled = prefs.getBoolean(key, false)
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    PreferenceSwitch(stringResource(R.string.wear_share_title),
        stringResource(if (failed) R.string.privacy_save_failed else R.string.wear_share_description),
        enabled, "WearBudgetSharing") { value ->
        failed = !prefs.edit().putBoolean(PhoneBudgetBridge.SHARE_KEY, value).commit()
    }
}
