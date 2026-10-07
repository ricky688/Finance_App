package com.example.vibefinance.ui.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vibefinance.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

enum class BackupPhase { IDLE, OPTIONS, WAIT_EXPORT, WAIT_RESTORE, PASSWORD, BUSY, PREVIEW, EXPORTED, RESTORED, ERROR }
data class FullBackupUiState(
    val phase: BackupPhase = BackupPhase.IDLE,
    val preview: FullBackupPreview? = null,
    val error: BackupError? = null,
    val restoring: Boolean = false
)

@HiltViewModel
class FullBackupViewModel @Inject constructor(private val application: Application) : ViewModel() {
    private val mutableState = MutableStateFlow(FullBackupUiState())
    val state = mutableState.asStateFlow()
    private var password: CharArray? = null
    private var restoreUri: Uri? = null
    private var prepared: PreparedFullBackup? = null

    fun exportOptions() { if (mutableState.value.phase == BackupPhase.IDLE) mutableState.value = FullBackupUiState(BackupPhase.OPTIONS) }
    fun awaitExport(secret: CharArray?) {
        password?.fill('\u0000'); password = secret
        mutableState.value = FullBackupUiState(BackupPhase.WAIT_EXPORT)
    }
    fun awaitRestore() { if (mutableState.value.phase == BackupPhase.IDLE) mutableState.value = FullBackupUiState(BackupPhase.WAIT_RESTORE) }
    fun reset() {
        if (mutableState.value.phase == BackupPhase.BUSY) return
        password?.fill('\u0000'); password = null; discardPrepared(); restoreUri = null
        mutableState.value = FullBackupUiState()
    }
    fun export(uri: Uri?) {
        if (mutableState.value.phase != BackupPhase.WAIT_EXPORT) return
        if (uri == null) { reset(); return }
        val secret = password; password = null
        mutableState.value = FullBackupUiState(BackupPhase.BUSY)
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { FullAppBackupEngine.export(application, uri, secret) }
                mutableState.value = FullBackupUiState(BackupPhase.EXPORTED)
            } catch (e: Exception) { fail(e, false) }
            finally { secret?.fill('\u0000') }
        }
    }
    fun chooseRestore(uri: Uri?) {
        if (mutableState.value.phase != BackupPhase.WAIT_RESTORE) return
        if (uri == null) { reset(); return }
        restoreUri = uri
        prepare(null)
    }
    fun prepare(secret: CharArray?) {
        val uri = restoreUri ?: return
        if (mutableState.value.phase == BackupPhase.BUSY) { secret?.fill('\u0000'); return }
        mutableState.value = FullBackupUiState(BackupPhase.BUSY, restoring = true)
        viewModelScope.launch {
            try {
                discardPrepared()
                var staged: PreparedFullBackup? = null
                try {
                    val validated = withContext(Dispatchers.IO) {
                        FullAppBackupEngine.prepare(application, uri, secret).also { staged = it }
                    }
                    prepared = validated
                    staged = null
                    mutableState.value = FullBackupUiState(BackupPhase.PREVIEW, validated.preview, restoring = true)
                } finally {
                    // A cancelled view model must still remove a completed private staging directory.
                    withContext(NonCancellable + Dispatchers.IO) { staged?.close() }
                }
            } catch (e: BackupException) {
                if (e.reason == BackupError.PASSWORD_REQUIRED || e.reason == BackupError.WRONG_PASSWORD) {
                    mutableState.value = FullBackupUiState(BackupPhase.PASSWORD, error = if (e.reason == BackupError.WRONG_PASSWORD) e.reason else null, restoring = true)
                } else fail(e, true)
            } catch (e: Exception) { fail(e, true) }
            finally { secret?.fill('\u0000') }
        }
    }
    fun restore() {
        if (mutableState.value.phase != BackupPhase.PREVIEW) return
        val snapshot = prepared ?: return
        prepared = null
        mutableState.value = FullBackupUiState(BackupPhase.BUSY, restoring = true)
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { FullAppBackupEngine.restore(application, snapshot) }
                mutableState.value = FullBackupUiState(BackupPhase.RESTORED)
            } catch (e: Exception) { fail(e, true) }
        }
    }
    private fun fail(error: Exception, restoring: Boolean) {
        mutableState.value = FullBackupUiState(BackupPhase.ERROR,
            error = (error as? BackupException)?.reason ?: BackupError.STORAGE, restoring = restoring)
    }
    private fun discardPrepared() {
        val previous = prepared ?: return
        prepared = null
        // Cleanup outlives a settings/activity dismissal and never blocks the UI thread.
        CoroutineScope(Dispatchers.IO).launch { previous.close() }
    }
    override fun onCleared() { password?.fill('\u0000'); discardPrepared() }
}
