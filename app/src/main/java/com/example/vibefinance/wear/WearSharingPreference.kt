package com.example.vibefinance.wear

import android.content.SharedPreferences
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.vibefinance.R
import com.example.vibefinance.ui.preferences.PreferenceSwitch
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

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
    PreferenceSwitch(
        stringResource(R.string.wear_share_title),
        stringResource(if (failed) R.string.privacy_save_failed else R.string.wear_share_description),
        enabled, "WearBudgetSharing"
    ) { value ->
        failed = !prefs.edit().putBoolean(PhoneBudgetBridge.SHARE_KEY, value).commit()
    }
}

@Composable
internal fun WearCompanionControls(prefs: SharedPreferences = PhoneBudgetBridge.prefs(LocalContext.current)) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var sharingEnabled by remember(prefs) { mutableStateOf(prefs.getBoolean(PhoneBudgetBridge.SHARE_KEY, false)) }
    var pendingEnabled by remember(prefs) { mutableStateOf(prefs.getBoolean(PhoneBudgetBridge.PENDING_DECISIONS_KEY, false)) }
    var recentEnabled by remember(prefs) { mutableStateOf(prefs.getBoolean(PhoneBudgetBridge.RECENT_AND_QUICK_KEY, false)) }
    val lastSyncedAt by PhoneBudgetBridge.lastSyncedAt.collectAsState()
    var watches by remember { mutableStateOf<List<WearNodeStatus>>(emptyList()) }
    var syncing by remember { mutableStateOf(false) }

    DisposableEffect(prefs) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                PhoneBudgetBridge.SHARE_KEY -> sharingEnabled = prefs.getBoolean(key, false)
                PhoneBudgetBridge.PENDING_DECISIONS_KEY -> pendingEnabled = prefs.getBoolean(key, false)
                PhoneBudgetBridge.RECENT_AND_QUICK_KEY -> recentEnabled = prefs.getBoolean(key, false)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    LaunchedEffect(sharingEnabled) {
        if (sharingEnabled) {
            watches = PhoneBudgetBridge.queryConnectedWatches(context)
        }
    }

    AnimatedVisibility(
        visible = sharingEnabled,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        val watchName = watches.firstOrNull()?.displayName
                        Text(
                            text = if (watchName != null) {
                                stringResource(R.string.wear_status_connected, watchName)
                            } else {
                                stringResource(R.string.wear_status_disconnected)
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = if (watchName != null) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Text(
                            text = if (lastSyncedAt > 0L) {
                                stringResource(
                                    R.string.wear_last_synced,
                                    DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(lastSyncedAt))
                                )
                            } else {
                                stringResource(R.string.wear_never_synced)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FilledTonalButton(
                        onClick = {
                            if (!syncing) {
                                scope.launch {
                                    syncing = true
                                    try {
                                        watches = PhoneBudgetBridge.queryConnectedWatches(context)
                                        PhoneBudgetBridge.publishSafely(context)
                                    } finally {
                                        syncing = false
                                    }
                                }
                            }
                        },
                        enabled = !syncing,
                        modifier = Modifier.testTag("WearSyncNow")
                    ) {
                        Text(stringResource(if (syncing) R.string.wear_syncing else R.string.wear_sync_now))
                    }
                }
            }

            PreferenceSwitch(
                title = stringResource(R.string.wear_pending_title),
                description = stringResource(R.string.wear_pending_description),
                checked = pendingEnabled,
                tag = "WearPendingDecisions"
            ) { value ->
                prefs.edit().putBoolean(PhoneBudgetBridge.PENDING_DECISIONS_KEY, value).commit()
            }

            PreferenceSwitch(
                title = stringResource(R.string.wear_recent_title),
                description = stringResource(R.string.wear_recent_description),
                checked = recentEnabled,
                tag = "WearRecentAndQuick"
            ) { value ->
                prefs.edit().putBoolean(PhoneBudgetBridge.RECENT_AND_QUICK_KEY, value).commit()
            }
        }
    }
}
