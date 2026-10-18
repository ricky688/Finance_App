package com.example.vibefinance.ui.recurring

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.vibefinance.R
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.util.CalendarSyncHelper
import com.example.vibefinance.util.appString
import kotlinx.coroutines.launch

@Composable
internal fun CalendarExportMenu(state: FinanceUiState) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    var confirm by remember { mutableStateOf(false) }
    var exporting by remember { mutableStateOf(false) }
    val performExport: () -> Unit = {
        if (!exporting) {
            confirm = false; exporting = true
            scope.launch {
                try { CalendarSyncHelper.exportAndShareIcs(context, state.accounts, state.subscriptions) }
                catch (e: kotlinx.coroutines.CancellationException) { throw e }
                catch (_: Exception) { android.widget.Toast.makeText(context, context.appString(R.string.calendar_export_failed), android.widget.Toast.LENGTH_LONG).show() }
                finally { exporting = false }
            }
        }
    }
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        if (exporting) CircularProgressIndicator(Modifier.size(24.dp))
        Box {
            IconButton(onClick = { expanded = true }, enabled = !exporting, modifier = Modifier.testTag("RecurringCalendarOptions")) {
                Icon(Icons.Default.MoreVert, stringResource(R.string.calendar_options))
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(text = { Text(stringResource(R.string.calendar_export)) }, onClick = {
                    expanded = false
                    if (state.subscriptions.isEmpty() && state.accounts.none { it.type == com.example.vibefinance.data.entity.AccountType.CC && com.example.vibefinance.util.CreditCardCycleCalculator().paymentDay(it) != null }) {
                        android.widget.Toast.makeText(context, context.appString(R.string.calendar_export_empty), android.widget.Toast.LENGTH_SHORT).show()
                    } else if (state.subscriptions.any { !it.frequency.equals("Weekly", true) && java.time.Instant.ofEpochMilli(it.nextPaymentDate).atZone(java.time.ZoneId.systemDefault()).dayOfMonth > 28 }) confirm = true
                    else performExport()
                }, modifier = Modifier.testTag("RecurringCalendarExport"))
            }
        }
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false }, title = { Text(stringResource(R.string.calendar_export)) },
        text = { Text(stringResource(R.string.calendar_export_note)) },
        confirmButton = { TextButton(onClick = performExport) { Text(stringResource(R.string.calendar_export)) } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text(stringResource(R.string.btn_cancel)) } })
}
