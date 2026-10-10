package com.example.vibefinance.ui.preferences

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.vibefinance.R
import com.example.vibefinance.util.appString
import com.example.vibefinance.util.CoordinatedPreferences

/** Authentication is local to this installation; no passwords or unlocked state are persisted. */
class AppPrivacyController(private val activity: ComponentActivity, private val saveEnabled: (Boolean) -> Unit) {
    private val prefs get() = CoordinatedPreferences.get(activity, "vibe_finance_prefs")
    private val keyguard get() = activity.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    val enabled get() = prefs.getBoolean("app_lock_enabled", false)
    var locked by mutableStateOf(enabled); private set
    var authenticating by mutableStateOf(false); private set
    var message by mutableStateOf<String?>(null); private set
    private var enabling = false
    private var signal: CancellationSignal? = null
    private val credential = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) success()
        else { authenticating = false; enabling = false; message = activity.appString(R.string.privacy_auth_cancelled) }
    }
    fun syncWindowProtection() {
        if (enabled) activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        else activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
    fun changeEnabled(value: Boolean) {
        if (!value) { enabling = false; saveEnabled(false); locked = false; return }
        if (!keyguard.isDeviceSecure) {
            Toast.makeText(activity, activity.appString(R.string.privacy_need_screen_lock), Toast.LENGTH_LONG).show()
            return
        }
        enabling = true
        authenticate()
    }
    fun onBackground() { if (enabled && !authenticating && !activity.isChangingConfigurations) locked = true }
    fun destroy() { signal?.cancel() }
    private fun success() {
        authenticating = false; locked = false; message = null
        if (enabling) { enabling = false; saveEnabled(true) }
    }
    @Suppress("DEPRECATION")
    private fun deviceCredential() {
        val intent = keyguard.createConfirmDeviceCredentialIntent(activity.appString(R.string.privacy_unlock),
            activity.appString(R.string.privacy_lock_desc))
        if (intent == null) { authenticating = false; message = activity.appString(R.string.privacy_need_screen_lock) }
        else credential.launch(intent)
    }
    fun authenticate() {
        if (authenticating) return
        if (!keyguard.isDeviceSecure) { message = activity.appString(R.string.privacy_need_screen_lock); return }
        authenticating = true; message = null
        if (Build.VERSION.SDK_INT < 28) { deviceCredential(); return }
        val builder = BiometricPrompt.Builder(activity).setTitle(activity.appString(R.string.privacy_unlock))
            .setSubtitle(activity.appString(R.string.privacy_lock_desc))
        if (Build.VERSION.SDK_INT >= 30) builder.setAllowedAuthenticators(
            android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG or
                android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        else builder.setNegativeButton(activity.appString(R.string.privacy_use_screen_lock), activity.mainExecutor) { _, _ -> deviceCredential() }
        signal = CancellationSignal()
        builder.build().authenticate(signal!!, activity.mainExecutor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { success() }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (activity.isDestroyed) return
                if (errorCode == BiometricPrompt.BIOMETRIC_ERROR_NO_BIOMETRICS ||
                    errorCode == BiometricPrompt.BIOMETRIC_ERROR_HW_NOT_PRESENT ||
                    errorCode == BiometricPrompt.BIOMETRIC_ERROR_HW_UNAVAILABLE) deviceCredential()
                else { authenticating = false; enabling = false; message = errString.toString() }
            }
            override fun onAuthenticationFailed() { message = activity.appString(R.string.privacy_auth_retry) }
        })
    }
}

@Composable
fun PrivacyLockScreen(controller: AppPrivacyController) {
    LaunchedEffect(Unit) { controller.authenticate() }
    Column(Modifier.fillMaxSize().padding(32.dp).testTag("PrivacyLockScreen"),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(stringResource(R.string.privacy_locked_title), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Text(controller.message ?: stringResource(R.string.privacy_lock_desc))
        Spacer(Modifier.height(24.dp))
        Button(onClick = controller::authenticate, enabled = !controller.authenticating,
            modifier = Modifier.testTag("PrivacyUnlock")) { Text(stringResource(R.string.privacy_unlock)) }
    }
}
