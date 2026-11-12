package com.example.vibefinance.ui.settings

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
import com.example.vibefinance.R
import com.example.vibefinance.transit.MtrTransitSettings
import com.example.vibefinance.transit.NfcOriginStatus
import com.example.vibefinance.transit.NfcTransitMonitor
import com.example.vibefinance.ui.components.AppSwitch
import com.example.vibefinance.util.VibeHaptics

@Composable
internal fun NfcOriginSettings() {
    val context = LocalContext.current
    val view = LocalView.current
    var enabled by remember { mutableStateOf(MtrTransitSettings.isNfcEnabled(context)) }
    val status by NfcTransitMonitor.status.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        enabled = MtrTransitSettings.isNfcEnabled(context)
        NfcTransitMonitor.refresh()
    }
    val label = when {
        Build.VERSION.SDK_INT < 36 -> R.string.nfc_origin_unsupported
        !enabled -> R.string.nfc_origin_off
        else -> when (status) {
            NfcOriginStatus.OFF -> R.string.nfc_origin_logging_off
            NfcOriginStatus.UNSUPPORTED -> R.string.nfc_origin_unsupported
            NfcOriginStatus.DISCONNECTED -> R.string.nfc_origin_disconnected
            NfcOriginStatus.PERMISSION -> R.string.nfc_origin_permission
            NfcOriginStatus.NFC_OFF -> R.string.nfc_origin_nfc_off
            NfcOriginStatus.WAITING -> R.string.nfc_origin_waiting
            NfcOriginStatus.CONTACT -> R.string.nfc_origin_contact
            NfcOriginStatus.SAVED -> R.string.nfc_origin_saved
            NfcOriginStatus.NO_LOCATION -> R.string.nfc_origin_no_location
            NfcOriginStatus.ERROR -> R.string.nfc_origin_error
        }
    }
    Column(Modifier.fillMaxWidth().testTag("NfcOriginSettings"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.nfc_origin_title), style = MaterialTheme.typography.titleSmall)
                Text(stringResource(label), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            AppSwitch(checked = enabled, modifier = Modifier.testTag("NfcOriginSwitch"), enabled = Build.VERSION.SDK_INT >= 36, onCheckedChange = {
                VibeHaptics.lightTick(view)
                enabled = it
                MtrTransitSettings.setNfcEnabled(context, it)
            })
        }
        Text(stringResource(R.string.nfc_origin_body), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        HorizontalDivider(Modifier.padding(top = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    }
}
