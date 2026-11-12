package com.example.vibefinance.ui.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.vibefinance.R
import com.example.vibefinance.util.VibeHaptics

@Composable
internal fun NotificationLocationSettingsCard() {
    val context = LocalContext.current
    val view = LocalView.current
    val owner = LocalLifecycleOwner.current
    var status by remember { mutableIntStateOf(notificationLocationStatus(context)) }
    DisposableEffect(owner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) status = notificationLocationStatus(context)
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.notification_location_title), style = MaterialTheme.typography.titleSmall)
            Text(stringResource(status), style = MaterialTheme.typography.labelLarge,
                color = if (status == R.string.notification_location_ready) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error)
            Text(stringResource(R.string.notification_location_body), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = {
                VibeHaptics.lightTick(view)
                val intent = if (status == R.string.notification_location_off) {
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                } else {
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
                }
                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.notification_location_settings))
            }
        }
    }
}

private fun notificationLocationStatus(context: Context): Int {
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    if (manager == null || !LocationManagerCompat.isLocationEnabled(manager)) return R.string.notification_location_off
    fun granted(permission: String) = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    if (!granted(Manifest.permission.ACCESS_FINE_LOCATION) && !granted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
        return R.string.notification_location_missing
    }
    return if (Build.VERSION.SDK_INT >= 29 && !granted(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
        R.string.notification_location_foreground
    } else R.string.notification_location_ready
}
