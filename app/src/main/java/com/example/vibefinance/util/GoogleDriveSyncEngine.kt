package com.example.vibefinance.util

import android.accounts.Account
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.vibefinance.data.InMemoryDatabase
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

sealed interface GoogleDriveSyncStatus {
    data class Idle(val lastSyncedTime: Long?) : GoogleDriveSyncStatus
    data object Syncing : GoogleDriveSyncStatus
    data class Success(val syncedTime: Long) : GoogleDriveSyncStatus
    data class Error(val message: String, val lastSyncedTime: Long? = null) : GoogleDriveSyncStatus
    data object Unauthenticated : GoogleDriveSyncStatus
}

data class GoogleDriveAuthState(
    val isAuthenticated: Boolean,
    val email: String? = null,
    val displayName: String? = null
)

object GoogleDriveSyncEngine {
    private const val TAG = "GoogleDriveSyncEngine"
    private const val PREFS_NAME = "vibe_finance_prefs"
    private const val PREF_KEY_AUTO_SYNC = "google_drive_auto_sync_enabled"
    private const val PREF_KEY_LAST_SYNCED = "google_drive_last_synced_time"
    private const val PREF_KEY_ACCOUNT_EMAIL = "google_drive_account_email"

    private const val DRIVE_APPDATA_SCOPE = "https://www.googleapis.com/auth/drive.appdata"
    private const val SYNC_FILE_NAME = "vibefinance_sync.json"

    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _syncStatus = MutableStateFlow<GoogleDriveSyncStatus>(GoogleDriveSyncStatus.Unauthenticated)
    val syncStatus: StateFlow<GoogleDriveSyncStatus> = _syncStatus.asStateFlow()

    private val _authState = MutableStateFlow(GoogleDriveAuthState(isAuthenticated = false))
    val authState: StateFlow<GoogleDriveAuthState> = _authState.asStateFlow()

    private val driveScope = Scope(DRIVE_APPDATA_SCOPE)

    fun getGoogleSignInOptions(): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestScopes(driveScope)
            .build()
    }

    fun getSignInClient(context: Context): GoogleSignInClient {
        return GoogleSignIn.getClient(context, getGoogleSignInOptions())
    }

    fun initialize(context: Context) {
        val prefs = CoordinatedPreferences.get(context, PREFS_NAME)
        val lastSynced = prefs.getLong(PREF_KEY_LAST_SYNCED, 0L).takeIf { it > 0L }
        val account = GoogleSignIn.getLastSignedInAccount(context)

        if (account != null && GoogleSignIn.hasPermissions(account, driveScope)) {
            _authState.value = GoogleDriveAuthState(
                isAuthenticated = true,
                email = account.email,
                displayName = account.displayName
            )
            _syncStatus.value = GoogleDriveSyncStatus.Idle(lastSynced)
        } else {
            _authState.value = GoogleDriveAuthState(isAuthenticated = false)
            _syncStatus.value = GoogleDriveSyncStatus.Unauthenticated
        }
    }

    fun onSignInSuccess(context: Context, account: GoogleSignInAccount) {
        val prefs = CoordinatedPreferences.get(context, PREFS_NAME)
        prefs.edit().putString(PREF_KEY_ACCOUNT_EMAIL, account.email).apply()

        _authState.value = GoogleDriveAuthState(
            isAuthenticated = true,
            email = account.email,
            displayName = account.displayName
        )
        val lastSynced = prefs.getLong(PREF_KEY_LAST_SYNCED, 0L).takeIf { it > 0L }
        _syncStatus.value = GoogleDriveSyncStatus.Idle(lastSynced)

        // Trigger immediate sync on sign-in
        syncScope.launch {
            sync(context, silent = false)
        }
    }

    fun signOut(context: Context, onComplete: () -> Unit = {}) {
        val client = getSignInClient(context)
        client.signOut().addOnCompleteListener {
            val prefs = CoordinatedPreferences.get(context, PREFS_NAME)
            prefs.edit().remove(PREF_KEY_ACCOUNT_EMAIL).apply()
            _authState.value = GoogleDriveAuthState(isAuthenticated = false)
            _syncStatus.value = GoogleDriveSyncStatus.Unauthenticated
            onComplete()
        }
    }

    fun isAutoSyncEnabled(context: Context): Boolean {
        return CoordinatedPreferences.get(context, PREFS_NAME).getBoolean(PREF_KEY_AUTO_SYNC, true)
    }

    fun setAutoSyncEnabled(context: Context, enabled: Boolean) {
        CoordinatedPreferences.get(context, PREFS_NAME).edit().putBoolean(PREF_KEY_AUTO_SYNC, enabled).apply()
    }

    fun getLastSyncedTime(context: Context): Long {
        return CoordinatedPreferences.get(context, PREFS_NAME).getLong(PREF_KEY_LAST_SYNCED, 0L)
    }

    fun triggerAutoSync(context: Context) {
        if (!isAutoSyncEnabled(context)) return
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return
        if (!GoogleSignIn.hasPermissions(account, driveScope)) return

        val lastSynced = getLastSyncedTime(context)
        // Throttle auto-sync to once every 20 seconds
        if (System.currentTimeMillis() - lastSynced < 20_000L && _syncStatus.value is GoogleDriveSyncStatus.Success) {
            return
        }

        syncScope.launch {
            sync(context, silent = true)
        }
    }

    suspend fun sync(context: Context, silent: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        if (_syncStatus.value is GoogleDriveSyncStatus.Syncing) {
            return@withContext false
        }

        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account == null || !GoogleSignIn.hasPermissions(account, driveScope)) {
            _syncStatus.value = GoogleDriveSyncStatus.Unauthenticated
            return@withContext false
        }

        if (!isNetworkAvailable(context)) {
            val lastSynced = getLastSyncedTime(context).takeIf { it > 0L }
            if (!silent) {
                _syncStatus.value = GoogleDriveSyncStatus.Error("無網路連線", lastSynced)
            }
            return@withContext false
        }

        _syncStatus.value = GoogleDriveSyncStatus.Syncing

        try {
            val token = GoogleAuthUtil.getToken(context, account.account ?: Account(account.email ?: "", "com.google"), "oauth2:$DRIVE_APPDATA_SCOPE")

            // 1. Check if sync file exists in appDataFolder
            val existingFileId = findSyncFile(token)

            val localSnapshot = InMemoryDatabase.snapshotForBackup()

            if (existingFileId != null) {
                // 2. Download remote file
                val remoteContent = downloadFile(token, existingFileId)
                val remoteJson = JSONObject(remoteContent)

                // 3. Two-way merge
                val mergeResult = SyncMergeEngine.merge(localSnapshot, remoteJson)

                // 4. Update local if needed
                if (mergeResult.localChanged) {
                    InMemoryDatabase.applySyncedData(context, mergeResult.mergedJson)
                }

                // 5. Update remote if needed
                if (mergeResult.remoteChanged || mergeResult.localChanged) {
                    updateFile(token, existingFileId, mergeResult.mergedJson.toString())
                }
            } else {
                // First sync: Upload local snapshot to appDataFolder
                createFile(token, localSnapshot.toString())
            }

            val now = System.currentTimeMillis()
            CoordinatedPreferences.get(context, PREFS_NAME).edit().putLong(PREF_KEY_LAST_SYNCED, now).apply()
            _syncStatus.value = GoogleDriveSyncStatus.Success(now)
            Log.d(TAG, "Google Drive sync completed successfully at $now")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Google Drive sync failed", e)
            val lastSynced = getLastSyncedTime(context).takeIf { it > 0L }
            _syncStatus.value = GoogleDriveSyncStatus.Error(e.localizedMessage ?: "同步失敗", lastSynced)
            false
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun findSyncFile(token: String): String? {
        val query = "name%3D'$SYNC_FILE_NAME'+and+trashed%3Dfalse"
        val url = URL("https://www.googleapis.com/drive/v3/files?spaces=appDataFolder&q=$query&fields=files(id%2Cname)")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.connectTimeout = 15000
        conn.readTimeout = 15000

        try {
            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = reader.readText()
                reader.close()
                val json = JSONObject(response)
                val files = json.optJSONArray("files")
                if (files != null && files.length() > 0) {
                    return files.getJSONObject(0).getString("id")
                }
            }
        } finally {
            conn.disconnect()
        }
        return null
    }

    private fun downloadFile(token: String, fileId: String): String {
        val url = URL("https://www.googleapis.com/drive/v3/files/$fileId?alt=media")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.connectTimeout = 15000
        conn.readTimeout = 15000

        try {
            if (conn.responseCode in 200..299) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8))
                val content = reader.readText()
                reader.close()
                return content
            } else {
                throw Exception("Drive download failed with HTTP ${conn.responseCode}")
            }
        } finally {
            conn.disconnect()
        }
    }

    private fun createFile(token: String, content: String) {
        val boundary = "====" + UUID.randomUUID().toString() + "===="
        val url = URL("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.setRequestProperty("Content-Type", "multipart/related; boundary=$boundary")
        conn.doOutput = true
        conn.connectTimeout = 15000
        conn.readTimeout = 15000

        try {
            val metadata = JSONObject().apply {
                put("name", SYNC_FILE_NAME)
                put("parents", org.json.JSONArray().put("appDataFolder"))
            }.toString()

            val writer = PrintWriter(OutputStreamWriter(conn.outputStream, Charsets.UTF_8), true)
            // Metadata part
            writer.append("--$boundary\r\n")
            writer.append("Content-Type: application/json; charset=UTF-8\r\n\r\n")
            writer.append(metadata).append("\r\n")

            // Content part
            writer.append("--$boundary\r\n")
            writer.append("Content-Type: application/json; charset=UTF-8\r\n\r\n")
            writer.append(content).append("\r\n")
            writer.append("--$boundary--\r\n")
            writer.flush()
            writer.close()

            if (conn.responseCode !in 200..299) {
                throw Exception("Drive create failed with HTTP ${conn.responseCode}")
            }
        } finally {
            conn.disconnect()
        }
    }

    private fun updateFile(token: String, fileId: String, content: String) {
        val url = URL("https://www.googleapis.com/upload/drive/v3/files/$fileId?uploadType=media")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "PATCH"
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        conn.doOutput = true
        conn.connectTimeout = 15000
        conn.readTimeout = 15000

        try {
            val writer = OutputStreamWriter(conn.outputStream, Charsets.UTF_8)
            writer.write(content)
            writer.flush()
            writer.close()

            if (conn.responseCode !in 200..299) {
                throw Exception("Drive update failed with HTTP ${conn.responseCode}")
            }
        } finally {
            conn.disconnect()
        }
    }
}
