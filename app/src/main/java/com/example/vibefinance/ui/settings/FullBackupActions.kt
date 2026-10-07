package com.example.vibefinance.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vibefinance.R
import com.example.vibefinance.util.BackupError
import java.text.DateFormat
import java.util.Date

/** Keep launchers/dialogs at the settings root even when its Data & privacy group is collapsed. */
@Composable
fun rememberFullBackupActions(onRestored: () -> Unit): @Composable () -> Unit {
    val application = LocalContext.current.applicationContext as android.app.Application
    val restoredMessage = stringResource(R.string.backup_restored)
    val viewModel: FullBackupViewModel = viewModel(factory = remember(application) {
        object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                require(modelClass == FullBackupViewModel::class.java)
                @Suppress("UNCHECKED_CAST")
                return FullBackupViewModel(application) as T
            }
        }
    })
    val state by viewModel.state.collectAsStateWithLifecycle()
    val save = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream"), viewModel::export)
    val open = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument(), viewModel::chooseRestore)
    var encrypt by remember(state.phase) { mutableStateOf(false) }
    var secret by remember(state.phase) { mutableStateOf("") }
    var confirmation by remember(state.phase) { mutableStateOf("") }
    val currentOnRestored by rememberUpdatedState(onRestored)
    LaunchedEffect(state.phase) {
        if (state.phase == BackupPhase.RESTORED) {
            android.widget.Toast.makeText(application, restoredMessage, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.reset(); currentOnRestored()
        }
    }
    val errorText = state.error?.let {
        stringResource(when (it) {
            BackupError.INVALID_FILE -> R.string.backup_error_invalid
            BackupError.NEWER_VERSION -> R.string.backup_error_newer
            BackupError.PASSWORD_REQUIRED -> R.string.backup_password_required
            BackupError.WRONG_PASSWORD -> R.string.backup_error_password
            BackupError.MISSING_IMAGE -> R.string.backup_error_image
            BackupError.INVALID_STATE -> R.string.backup_error_state
            BackupError.STORAGE -> R.string.backup_error_storage
        })
    }
    when (state.phase) {
        BackupPhase.OPTIONS, BackupPhase.PASSWORD -> {
            val export = state.phase == BackupPhase.OPTIONS
            AlertDialog(
                onDismissRequest = viewModel::reset,
                title = { Text(stringResource(if (export) R.string.backup_title else R.string.backup_password_required)) },
                text = {
                    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (export) {
                            Text(stringResource(R.string.backup_includes))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(stringResource(R.string.backup_encrypt), Modifier.weight(1f))
                                Switch(checked = encrypt, onCheckedChange = { encrypt = it }, modifier = Modifier.testTag("BackupEncrypt"))
                            }
                            if (!encrypt) Text(stringResource(R.string.backup_plain_notice), style = MaterialTheme.typography.bodySmall)
                        }
                        if (!export || encrypt) {
                            OutlinedTextField(secret, { secret = it }, label = { Text(stringResource(R.string.backup_password)) },
                                singleLine = true, visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth().testTag("BackupPassword"))
                            if (export) {
                                OutlinedTextField(confirmation, { confirmation = it }, label = { Text(stringResource(R.string.backup_confirm_password)) },
                                    singleLine = true, visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth().testTag("BackupPasswordConfirmation"))
                                Text(stringResource(R.string.backup_password_notice), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        errorText?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    }
                },
                confirmButton = {
                    TextButton(modifier = Modifier.testTag("BackupContinue"), enabled = if (export && !encrypt) true else secret.isNotEmpty() && (!export || secret == confirmation), onClick = {
                        val password = if (!export || encrypt) secret.toCharArray() else null
                        secret = ""; confirmation = ""
                        if (export) {
                            viewModel.awaitExport(password)
                            save.launch("VibeFinance_${java.time.LocalDate.now()}.vibebackup")
                        } else viewModel.prepare(password)
                    }) { Text(stringResource(if (export) R.string.backup_save else R.string.backup_continue)) }
                },
                dismissButton = { TextButton(modifier = Modifier.testTag("BackupCancel"), onClick = viewModel::reset) { Text(stringResource(R.string.backup_cancel)) } }
            )
        }
        BackupPhase.BUSY -> AlertDialog(
            onDismissRequest = {}, title = { Text(stringResource(if (state.restoring) R.string.backup_restoring else R.string.backup_saving)) },
            text = { Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) { CircularProgressIndicator(Modifier.size(24.dp)); Text(stringResource(R.string.backup_busy)) } },
            confirmButton = {}
        )
        BackupPhase.PREVIEW -> state.preview?.let { preview ->
            AlertDialog(
                onDismissRequest = viewModel::reset,
                title = { Text(stringResource(R.string.backup_restore_title)) },
                text = {
                    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(stringResource(R.string.backup_created, DateFormat.getDateTimeInstance().format(Date(preview.createdAt))))
                        Text(stringResource(R.string.backup_counts, preview.accounts, preview.transactions, preview.subscriptions, preview.images))
                        Text(stringResource(R.string.backup_includes))
                        if (preview.unavailableApps.isNotEmpty()) Text(stringResource(R.string.backup_unavailable_apps, preview.unavailableApps.joinToString("\n")))
                        Text(stringResource(R.string.backup_replace_notice), color = MaterialTheme.colorScheme.error)
                    }
                },
                confirmButton = { TextButton(onClick = viewModel::restore) { Text(stringResource(R.string.backup_replace)) } },
                dismissButton = { TextButton(onClick = viewModel::reset) { Text(stringResource(R.string.backup_cancel)) } }
            )
        }
        BackupPhase.ERROR, BackupPhase.EXPORTED -> AlertDialog(
            onDismissRequest = viewModel::reset,
            title = { Text(stringResource(if (state.phase == BackupPhase.EXPORTED) R.string.backup_saved else R.string.backup_failed)) },
            text = { Text(errorText ?: stringResource(R.string.backup_saved_notice)) },
            confirmButton = { TextButton(onClick = viewModel::reset) { Text(stringResource(R.string.backup_done)) } }
        )
        else -> Unit
    }
    return {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FilledTonalButton(viewModel::exportOptions, enabled = state.phase == BackupPhase.IDLE, modifier = Modifier.fillMaxWidth().testTag("BackupOpen")) {
                Text(stringResource(R.string.backup_title))
            }
            OutlinedButton(onClick = { viewModel.awaitRestore(); open.launch(arrayOf("*/*")) },
                enabled = state.phase == BackupPhase.IDLE, modifier = Modifier.fillMaxWidth().testTag("BackupRestore")) { Text(stringResource(R.string.backup_restore_title)) }
        }
    }
}
